package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class SendMessageAdapter implements SendMessageService {
    private final TelegramApiClient telegramApiClient;

    public SendMessageAdapter(TelegramApiClient telegramApiClient) {
        this.telegramApiClient = telegramApiClient;
    }

    private void sendMessageSafely(SendMessage method) {
        try {
            telegramApiClient.execute(method);
            log.info("Отправлено сообщение пользователю: {}", method.getText());
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения: {}", method, e);
        }
    }

    @Override
    public void sendMessage(Long chatId, String message) {
        sendMessageSafely(
            SendMessage.builder()
                .text(message)
                .chatId(chatId.toString())
                .parseMode("Markdown")
                .build()
        );
    }
}
