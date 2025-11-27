package com.pechatnikov.numbermnemocardsgeneratorbot.application.service.callback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Callback;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.TelegramApiClient;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.dto.CallbackData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Component
public class ShowPricesCallbackProcessor implements CallbackProcessor {
    private final TelegramApiClient telegramApiClient;
    private final ObjectMapper objectMapper;

    public ShowPricesCallbackProcessor(TelegramApiClient telegramApiClient, ObjectMapper objectMapper) {
        this.telegramApiClient = telegramApiClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public CallbackType getCallBacktype() {
        return CallbackType.SHOW_PRICES;
    }

    @Override
    public void process(Callback callback) {
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callback.getCallbackQueryId());
        answer.setText("Запуск процесса оплаты...");
        try {
            telegramApiClient.execute(answer);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

        // Обновляем сообщение
        DeleteMessage deleteMessage = DeleteMessage.builder()
            .chatId(callback.getChatId().toString())
            .messageId(callback.getMessageId())
            .build();

        try {
            telegramApiClient.execute(deleteMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

        showPrices(callback.getChatId().toString());
    }

    private void showPrices(String chatId) {
        final int tokenRate = 2;
        SendMessage message = new SendMessage();
        message.setChatId(chatId);

        // Создаем inline клавиатуру
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        Stream.of(60, 100, 200).forEach(price -> {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            InlineKeyboardButton pricesButton = new InlineKeyboardButton();
            int tokenCount = price * tokenRate;
            pricesButton.setText(price + "₽ (" + tokenCount + " цифр)" );

            String callbackData = null;
            try {
                callbackData = objectMapper.writeValueAsString(
                    CallbackData.builder()
                        .type(CallbackType.CREATE_INVOICE)
                        .value(price.toString())
                        .build()
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            pricesButton.setCallbackData(callbackData);

            rowInline.add(pricesButton);
            rowsInline.add(rowInline);
        });


        markupInline.setKeyboard(rowsInline);

        message.setReplyMarkup(markupInline);
        message.setText("Выберите вариант оплаты. Курс: 1₽ = 2 цифры для преобразования в мнемокарточку.");

        try {
            telegramApiClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки кнопки оплаты. {}", e.getMessage());
        }
    }
}
