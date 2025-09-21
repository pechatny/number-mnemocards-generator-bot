package com.pechatnikov.numbermnemocardsgeneratorbot.application.service;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.GetOrCreateUserCommand;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.MessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.NumericMessageHandler;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.TokenBalanceService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.TokenTransactionService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.UserService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendPhotoService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Message;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.TokenTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Slf4j
@Service
public class NumericMessageHandlerImpl implements NumericMessageHandler {
    private final UserService userService;
    private final MessageService messageService;
    private final NumberSplitter splitter;
    private final ImageMerger imageMerger;
    private final SendPhotoService sendPhotoService;
    private final TokenTransactionService tokenTransactionService;
    private final TokenBalanceService tokenBalanceService;

    public NumericMessageHandlerImpl(UserService userService, MessageService messageService, NumberSplitter splitter, ImageMerger imageMerger, SendPhotoService sendPhotoService, TokenTransactionService tokenTransactionService, TokenBalanceService tokenBalanceService) {
        this.userService = userService;
        this.messageService = messageService;
        this.splitter = splitter;
        this.imageMerger = imageMerger;
        this.sendPhotoService = sendPhotoService;
        this.tokenTransactionService = tokenTransactionService;
        this.tokenBalanceService = tokenBalanceService;
    }

    @Override
    public void handle(GetOrCreateUserCommand getOrCreateUserCommand, Message message) throws IOException {
        log.info("Получить или получить пользователя");
        var user = userService.getOrCreateUser(getOrCreateUserCommand);
        var messageWithUser = Message.toBuilder(message).user(user).build();

        log.info("Сохранить сообщение");
        message = messageService.save(messageWithUser);

        log.info("Создать картинку");
        List<String> splittedNumberList = splitter.split(message.getMessage());
        var file = imageMerger.mergeImages(splittedNumberList);

        log.info("Отправить ответ");
        sendPhotoService.sendPhoto(message.getChatId(), file);

        log.info("Удалить файл картинки");
        deleteFile(file);

        log.info("Записать транзакцию потраченных токенов");
        var tokenTransaction = TokenTransaction.builder()
            .user(user)
            .message(message)
            .count(countTokens(splittedNumberList))
            .build();

        tokenTransaction = tokenTransactionService.save(tokenTransaction);

        log.info("Уменьшить баланс токенов пользователя");
        tokenBalanceService.decrease(tokenTransaction);
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
