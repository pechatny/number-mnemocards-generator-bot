package com.pechatnikov.numbermnemocardsgeneratorbot.application.service;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.GetOrCreateUserCommand;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.MessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.NumericMessageHandler;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.TokenBalanceService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.TokenTransactionService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.UserService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendPayButtonService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendPhotoService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendPricesButtonsService;
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
    private final static String NOT_ENOUGH_TOKENS_MESSAGE = "Ваше число состоит из %d цифр. Вам доступно %d цифр";

    private final UserService userService;
    private final MessageService messageService;
    private final NumberSplitter splitter;
    private final ImageMerger imageMerger;
    private final SendPhotoService sendPhotoService;
    private final TokenTransactionService tokenTransactionService;
    private final TokenBalanceService tokenBalanceService;
    private final SendMessageService sendMessageService;
    private final PaymentService paymentService;
    private final SendPayButtonService sendPayButtonService;
//    private final SendPricesButtonsService sendPricesButtonsService;

    public NumericMessageHandlerImpl(
        UserService userService,
        MessageService messageService,
        NumberSplitter splitter,
        ImageMerger imageMerger,
        SendPhotoService sendPhotoService,
        TokenTransactionService tokenTransactionService,
        TokenBalanceService tokenBalanceService,
        SendMessageService sendMessageService,
        PaymentService paymentService,
        SendPayButtonService sendPayButtonService
    ) {
        this.userService = userService;
        this.messageService = messageService;
        this.splitter = splitter;
        this.imageMerger = imageMerger;
        this.sendPhotoService = sendPhotoService;
        this.tokenTransactionService = tokenTransactionService;
        this.tokenBalanceService = tokenBalanceService;
        this.sendMessageService = sendMessageService;
        this.paymentService = paymentService;
        this.sendPayButtonService = sendPayButtonService;
    }

    @Override
    public void handle(GetOrCreateUserCommand getOrCreateUserCommand, Message message) throws IOException {
        log.info("Получить или получить пользователя");
        var user = userService.getOrCreateUser(getOrCreateUserCommand);
        var messageWithUser = Message.toBuilder(message).user(user).build();

        log.info("Сохранить сообщение");
        message = messageService.save(messageWithUser);

        log.info("Расчет токенов");
        List<String> splittedNumberList = splitter.split(message.getMessage());
        long countedTokens = countTokens(splittedNumberList);
        log.info("Необходимо {} токенов", countedTokens);

        if (checkTokenBalance(message, user, countedTokens)) {
//            sendPayButtonService.sendPayButton(message.getChatId());
            paymentService.showPricesButton(message.getChatId());
            // Перенести создание инвойса на обработку команды pay
//            paymentService.sendInvoice(message.getChatId().toString(), "Хотите купить дополнительные токены для мнемокарточек?",
//                "Токен - это цифра в числе. Курс 1 токен (цифра) = 50 копеек",
//                "{}", "381764678:TEST:128329", "RUB", 100);
            return;
        }

        log.info("Создать картинку");
        var file = imageMerger.mergeImages(splittedNumberList);

        log.info("Отправить ответ");
        sendPhotoService.sendPhoto(message.getChatId(), file);

        log.info("Удалить файл картинки");
        deleteFile(file);

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
