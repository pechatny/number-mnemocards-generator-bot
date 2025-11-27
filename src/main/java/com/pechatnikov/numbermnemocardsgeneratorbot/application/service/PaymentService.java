package com.pechatnikov.numbermnemocardsgeneratorbot.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Invoice;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration.PaymentProperties;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.TelegramApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendInvoice;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

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
            String providerData = objectMapper.writeValueAsString(invoice.getProviderData());
            return sendInvoice(
                invoice.getChatId(),
                invoice.getTitle(),
                invoice.getDescription(),
                payload,
                providerData,
                paymentProperties.getToken(),
                invoice.getPrice().getCurrency().getCurrencyCode(),
                (int) invoice.getPrice().toMinorUnits()
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
        String providerData,
        String providerToken,
        String currency,
        Integer priceAmount
    ) {
        List<LabeledPrice> prices = List.of(
            new LabeledPrice(title, priceAmount)
        );

        // Создаем инвойс
        SendInvoice sendInvoice = new SendInvoice();
        sendInvoice.setChatId(chatId);
        sendInvoice.setTitle(title);
        sendInvoice.setDescription(description);
        sendInvoice.setPayload(payload);
        sendInvoice.setProviderToken(providerToken); // Токен платежной системы
        sendInvoice.setCurrency(currency); // "RUB", "USD", "EUR" и т.д.
        sendInvoice.setPrices(prices);
        sendInvoice.setNeedEmail(true);
        sendInvoice.setSendEmailToProvider(true);
        sendInvoice.setProviderData(providerData);

        try {
            log.debug("SendInvoice Command {}", objectMapper.writeValueAsString(sendInvoice));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        // Кнопка для оплаты (создается автоматически)
        // Дополнительные кнопки можно добавить через ReplyMarkup

        try {
            return telegramApiClient.execute(sendInvoice);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        return null;
    }
}
