package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.exception.SendPhotoException;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.ButtonMessage;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.ButtonsMessage;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration.ConditionalOnMaxBot;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxButton;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.messaging.MessengerGateway;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.dto.CallbackData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Отправка в Max. Обработка ошибок повторяет Telegram-адаптеры: ошибки текстовых сообщений,
 * кнопок и удаления логируются, ошибки отправки картинки и ответа на callback пробрасываются.
 */
@Slf4j
@ConditionalOnMaxBot
@Component
public class MaxGateway implements MessengerGateway {
    private final MaxClient maxClient;
    private final ObjectMapper objectMapper;

    public MaxGateway(MaxClient maxClient, ObjectMapper objectMapper) {
        this.maxClient = maxClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public BotPlatform platform() {
        return BotPlatform.MAX;
    }

    @Override
    public void sendMessage(Long chatId, String message) {
        try {
            maxClient.sendText(chatId, MaxMarkdown.fromTelegramMarkdown(message));
            log.info("Отправлено сообщение пользователю Max: {}", message);
        } catch (MaxApiException e) {
            log.error("Ошибка отправки сообщения в Max: chatId={}", chatId, e);
        }
    }

    @Override
    public void sendPhoto(Long chatId, File photo) {
        try {
            log.info("Начало отправки картинки пользователю Max");
            maxClient.sendImage(chatId, photo);
            log.info("Картинка отправлена пользователю Max!");
        } catch (MaxApiException e) {
            log.error("Ошибка при отправке картинки в Max: chatId={}", chatId, e);
            throw new SendPhotoException("Ошибка при отправке картинки: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendButtonMessage(ButtonMessage buttonMessage) {
        String payload = toJson(CallbackData.builder().type(buttonMessage.getCallbackType()).build());
        List<List<MaxButton>> rows = List.of(List.of(MaxButton.callback(buttonMessage.getButtonText(), payload)));

        sendButtons(buttonMessage.getChatId(), buttonMessage.getMessage(), rows);
    }

    @Override
    public void sendButtonsMessage(ButtonsMessage buttonsMessage) {
        List<List<MaxButton>> rows = buttonsMessage.getButtons().stream()
            .map(button -> List.of(MaxButton.callback(button.getButtonText(), button.getButtonCallbackData())))
            .collect(Collectors.toList());

        sendButtons(Long.valueOf(buttonsMessage.getChatId()), buttonsMessage.getMessage(), rows);
    }

    @Override
    public void sendCallbackAnswer(String callbackId, String message) {
        maxClient.answerCallback(callbackId, message);
        log.debug("Отправлен ответ на callback Max с id = {}", callbackId);
    }

    @Override
    public void deleteMessage(Long chatId, String messageId) {
        try {
            maxClient.deleteMessage(messageId);
            log.info("Сообщение Max {} удалено", messageId);
        } catch (MaxApiException e) {
            log.error("Ошибка удаления сообщения Max: chatId={}, messageId={}", chatId, messageId, e);
        }
    }

    private void sendButtons(Long chatId, String message, List<List<MaxButton>> rows) {
        try {
            maxClient.sendButtons(chatId, MaxMarkdown.fromTelegramMarkdown(message), rows);
            log.debug("Отправлено сообщение c кнопками пользователю Max: chatId={}", chatId);
        } catch (MaxApiException e) {
            log.error("Ошибка отправки сообщения с кнопками в Max: chatId={}", chatId, e);
        }
    }

    private String toJson(CallbackData callbackData) {
        try {
            return objectMapper.writeValueAsString(callbackData);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
