package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.DeleteMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class DeleteMessageAdapter implements DeleteMessageService {
    private final TelegramApiClient telegramApiClient;

    public DeleteMessageAdapter(TelegramApiClient telegramApiClient) {
        this.telegramApiClient = telegramApiClient;
    }

    @Override
    public void delete(Long chatId, Integer messageId) {
        try {
            DeleteMessage deleteMessage = DeleteMessage.builder()
                .chatId(chatId.toString())
                .messageId(messageId)
                .build();

            telegramApiClient.execute(deleteMessage);
            log.info("Сообщение {} удалено", messageId);
        } catch (TelegramApiException e) {
            log.error("Ошибка удаления сообщения: chatId={}, messageId={}", chatId, messageId, e);
        }

    }
}
