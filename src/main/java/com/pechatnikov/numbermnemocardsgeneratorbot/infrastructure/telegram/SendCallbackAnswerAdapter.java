package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram;

import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration.ConditionalOnTelegramBot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@ConditionalOnTelegramBot
@Component
public class SendCallbackAnswerAdapter {
    private final TelegramApiClient telegramApiClient;

    public SendCallbackAnswerAdapter(TelegramApiClient telegramApiClient) {
        this.telegramApiClient = telegramApiClient;
    }

    public void sendCallbackAnswer(String callbackId, String message) {
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callbackId);
        answer.setText(message);

        try {
            telegramApiClient.execute(answer);
            log.debug("Отправлен ответ на callback с id = {}", callbackId);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки ответа на callback с id = {}", callbackId);
            throw new RuntimeException(e);
        }
    }
}
