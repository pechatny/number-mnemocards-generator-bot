package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.exception.SendPhotoException;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendPhotoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;

@Slf4j
@Component
public class SendPhotoAdapter implements SendPhotoService {
    private final TelegramApiClient telegramApiClient;


    public SendPhotoAdapter(TelegramApiClient telegramApiClient) {
        this.telegramApiClient = telegramApiClient;
    }

    public void sendPhoto(Long chatId, File photo) {
        var sendPhotoCommand = getSendPhotoCommand(chatId, photo);

        try {
            log.info("Начало отправки картинки пользователю");
            telegramApiClient.execute(sendPhotoCommand);
            log.info("Картинка отправлена пользователю!");
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке картинки. SendPhoto: {}", sendPhotoCommand, e);
            throw new SendPhotoException("Ошибка при отправке картинки: " + e.getMessage(), e);
        }
    }

    private SendPhoto getSendPhotoCommand(Long chatId, File photo) {
        return new SendPhoto(
            String.valueOf(chatId),
            new InputFile(photo)
        );
    }
}
