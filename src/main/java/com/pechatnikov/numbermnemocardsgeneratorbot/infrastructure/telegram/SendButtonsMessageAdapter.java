package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendButtonsMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.ButtonsMessage;
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
public class SendButtonsMessageAdapter implements SendButtonsMessageService {
    private final TelegramApiClient telegramApiClient;

    public SendButtonsMessageAdapter(TelegramApiClient telegramApiClient) {
        this.telegramApiClient = telegramApiClient;
    }

    @Override
    public void sendButtonsMessage(ButtonsMessage buttonsMessage) {

        SendMessage message = new SendMessage();
        message.setChatId(buttonsMessage.getChatId());

        // Создаем inline клавиатуру
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        buttonsMessage.getButtons().forEach(button -> {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            InlineKeyboardButton pricesButton = new InlineKeyboardButton();
            pricesButton.setText(button.getButtonText());
            pricesButton.setCallbackData(button.getButtonCallbackData());

            rowInline.add(pricesButton);
            rowsInline.add(rowInline);
        });

        markupInline.setKeyboard(rowsInline);

        message.setReplyMarkup(markupInline);
        message.setText(buttonsMessage.getMessage());

        try {
            telegramApiClient.execute(message);
            log.debug("Отправлено сообщение c кнопками пользователю: {}", buttonsMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки кнопки оплаты. {}", e.getMessage());
        }
    }
}
