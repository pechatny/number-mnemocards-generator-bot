package com.pechatnikov.numbermnemocardsgeneratorbot.application.service;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.exception.SendPhotoException;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.GetOrCreateUserCommand;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.MessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.NumericMessageHandler;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.ShowPricesButtonUseCase;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.TokenBalanceService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.TokenTransactionService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.UserService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendPhotoService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Message;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.TokenBalance;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.TokenTransaction;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Slf4j
@Service
public class NumericMessageHandlerImpl implements NumericMessageHandler {
    private static final String NOT_ENOUGH_TOKENS_MESSAGE = "Ваше число состоит из %d цифр. Вам доступно %d цифр";
    private static final String MINIMAL_NUMBER_LENGTH_MESSAGE = "Минимальное число должно состоять из двух цифр. Например: 00, 01, 02";
    private static final String MAX_MESSAGE_LENGTH_MESSAGE = "Максимальная длина сообщения не должна превышать 100 символов";
    private static final int MAX_MESSAGE_LENGTH = 100;

    private final UserService userService;
    private final MessageService messageService;
    private final NumberSplitter splitter;
    private final ImageMerger imageMerger;
    private final SendPhotoService sendPhotoService;
    private final TokenTransactionService tokenTransactionService;
    private final TokenBalanceService tokenBalanceService;
    private final SendMessageService sendMessageService;
    private final ShowPricesButtonUseCase showPricesButtonUseCase;

    public NumericMessageHandlerImpl(
        UserService userService,
        MessageService messageService,
        NumberSplitter splitter,
        ImageMerger imageMerger,
        SendPhotoService sendPhotoService,
        TokenTransactionService tokenTransactionService,
        TokenBalanceService tokenBalanceService,
        SendMessageService sendMessageService,
        ShowPricesButtonUseCase showPricesButtonUseCase
    ) {
        this.userService = userService;
        this.messageService = messageService;
        this.splitter = splitter;
        this.imageMerger = imageMerger;
        this.sendPhotoService = sendPhotoService;
        this.tokenTransactionService = tokenTransactionService;
        this.tokenBalanceService = tokenBalanceService;
        this.sendMessageService = sendMessageService;
        this.showPricesButtonUseCase = showPricesButtonUseCase;
    }

    @Override
    public void handle(GetOrCreateUserCommand getOrCreateUserCommand, Message message) throws IOException {
        log.info("Получить или получить пользователя");
        var user = userService.getOrCreateUser(getOrCreateUserCommand);
        var messageWithUser = Message.toBuilder(message).user(user).build();

        log.debug("Валидация максимального размера сообщения");
        if (message.getMessage().length() > MAX_MESSAGE_LENGTH) {
            sendMessageService.sendMessage(message.getChatId(), MAX_MESSAGE_LENGTH_MESSAGE);
            return;
        }

        log.info("Сохранить сообщение");
        message = messageService.save(messageWithUser);

        log.info("Расчет токенов");
        List<String> splittedNumberList = splitter.split(message.getMessage());

        if (splittedNumberList.isEmpty()) {
            sendMessageService.sendMessage(message.getChatId(), MINIMAL_NUMBER_LENGTH_MESSAGE);
            return;
        }

        long countedTokens = countTokens(splittedNumberList);
        log.info("Необходимо {} токенов", countedTokens);

        if (checkTokenBalance(message, user, countedTokens)) {
            showPricesButtonUseCase.showPricesButton(message.getChatId());
            return;
        }

        log.info("Создать картинку");
        var file = imageMerger.mergeImages(splittedNumberList);

        try {
            log.info("Отправить ответ");
            sendPhotoService.sendPhoto(message.getChatId(), file);
        } catch (SendPhotoException e) {
            throw new SendPhotoException(e);
        } finally {
            log.info("Удалить файл картинки");
            deleteFile(file);
        }

        log.info("Записать транзакцию потраченных токенов");
        var tokenTransaction = TokenTransaction.builder()
            .user(user)
            .message(message)
            .count(countedTokens)
            .build();

        tokenTransaction = tokenTransactionService.save(tokenTransaction);

        log.info("Уменьшить баланс токенов пользователя");
        tokenBalanceService.decrease(tokenTransaction);
    }

    private boolean checkTokenBalance(Message message, User user, long countedTokens) {
        TokenBalance tokenBalance = tokenBalanceService.findOrCreateTokenBalanceForUser(user);
        if (tokenBalance.getBalance() < countedTokens) {
            String notEnoughTokensMessage = String.format(NOT_ENOUGH_TOKENS_MESSAGE, countedTokens, tokenBalance.getBalance());
            log.info(notEnoughTokensMessage);
            sendMessageService.sendMessage(message.getChatId(), notEnoughTokensMessage);

            return true;
        }
        return false;
    }

    private long countTokens(List<String> numberList) {
        return numberList.stream().mapToLong(String::length).sum();
    }

    private void deleteFile(File mergedPhoto) throws IOException {
        boolean deleted = Files.deleteIfExists(mergedPhoto.toPath());
        if (deleted) {
            log.info("File deleted: {}", mergedPhoto.getName());
        } else {
            log.error("Unable to delete file: {}", mergedPhoto.getName());
        }
    }
}
