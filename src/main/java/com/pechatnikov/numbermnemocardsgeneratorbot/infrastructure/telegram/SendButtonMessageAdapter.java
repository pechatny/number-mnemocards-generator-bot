package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendButtonMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.callback.CallbackType;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.ButtonMessage;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.dto.CallbackData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class SendButtonMessageAdapter implements SendButtonMessageService {
    private final TelegramApiClient telegramApiClient;
    private final ObjectMapper objectMapper;

    public SendButtonMessageAdapter(TelegramApiClient telegramApiClient, ObjectMapper objectMapper) {
        this.telegramApiClient = telegramApiClient;
        this.objectMapper = objectMapper;
    }

    private void sendMessageSafely(SendMessage method) {
        try {
            telegramApiClient.execute(method);
            log.info("Отправлено сообщение c кнопкой пользователю: {}", method.getText());
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения с кнопкой: {}", method, e);
        }
    }

    @Override
    public void sendButtonMessage(ButtonMessage buttonMessage) {
        SendMessage message = new SendMessage();
        message.setChatId(buttonMessage.getChatId().toString());

        // Создаем inline клавиатуру
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        // Создаем кнопку
        InlineKeyboardButton pricesButton = new InlineKeyboardButton();
        pricesButton.setText(buttonMessage.getButtonText());
        try {
            String callbackData = objectMapper.writeValueAsString(
                CallbackData.builder()
                    .type(CallbackType.SHOW_PRICES)
                    .build()
            );

            pricesButton.setCallbackData(callbackData);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        rowInline.add(pricesButton);
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);

        message.setReplyMarkup(markupInline);
        message.setText(buttonMessage.getMessage());

        sendMessageSafely(
            message
        );
    }
}
