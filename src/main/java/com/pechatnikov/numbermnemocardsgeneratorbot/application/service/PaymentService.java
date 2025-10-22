package com.pechatnikov.numbermnemocardsgeneratorbot.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.SaveInvoiceMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.callback.CallbackType;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Invoice;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration.PaymentProperties;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.TelegramApiClient;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.dto.CallbackData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendInvoice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class PaymentService {
    private final PaymentProperties paymentProperties;
    private final TelegramApiClient telegramApiClient; // Ваш бот, расширяющий TelegramLongPollingBot
    private final ObjectMapper objectMapper;

    public PaymentService(PaymentProperties paymentProperties, TelegramApiClient telegramApiClient, ObjectMapper objectMapper) {
        this.paymentProperties = paymentProperties;
        this.telegramApiClient = telegramApiClient;
        this.objectMapper = objectMapper;
    }

    public Message sendInvoice(Invoice invoice) {
        try {
            String payload = objectMapper.writeValueAsString(invoice.getPayload());
            return sendInvoice(
                invoice.getChatId(),
                invoice.getTitle(),
                invoice.getDescription(),
                payload,
                paymentProperties.getToken(),
                invoice.getPrice().getCurrency().getCurrencyCode(),
                invoice.getPrice().getAmount().intValue()
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    public Message sendInvoice(
        String chatId,
        String title,
        String description,
        String payload,
        String providerToken,
        String currency,
        Integer priceAmount
    ) {
        // Создаем цену (может быть несколько цен для разных товаров)
        List<LabeledPrice> prices = IntStream.of(priceAmount).mapToObj(amount -> {
                String priceTitle = amount * 2 + " цифр";
                // в копейках/центах
                return new LabeledPrice(priceTitle, amount * 100);
            })
            .collect(Collectors.toList());


        // Создаем инвойс
        SendInvoice sendInvoice = new SendInvoice();
        sendInvoice.setChatId(chatId);
        sendInvoice.setTitle(title);
        sendInvoice.setDescription(description);
        sendInvoice.setPayload(payload);
        sendInvoice.setProviderToken(providerToken); // Токен платежной системы
        sendInvoice.setCurrency(currency); // "RUB", "USD", "EUR" и т.д.
        sendInvoice.setPrices(prices);

        // Кнопка для оплаты (создается автоматически)
        // Дополнительные кнопки можно добавить через ReplyMarkup

        try {
            return telegramApiClient.execute(sendInvoice);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void showPricesButton(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());

        // Создаем inline клавиатуру
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        // Создаем кнопку
        InlineKeyboardButton pricesButton = new InlineKeyboardButton();
        pricesButton.setText("💳 Показать цены");
        String callbackData = null;
        try {
            callbackData = objectMapper.writeValueAsString(
                CallbackData.builder()
                    .type(CallbackType.SHOW_PRICES)
                    .build()
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        pricesButton.setCallbackData(callbackData);

        rowInline.add(pricesButton);
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);

        message.setReplyMarkup(markupInline);
        message.setText("Нажмите кнопку ниже для выбора вариантов оплаты:");

        try {
            telegramApiClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки кнопки оплаты. {}", e.getMessage());
        }
    }

}
