package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendInvoiceService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Invoice;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.InvoiceMessage;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration.PaymentProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendInvoice;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
@Component
public class SendInvoiceAdapter implements SendInvoiceService {
    private final PaymentProperties paymentProperties;
    private final TelegramApiClient telegramApiClient;
    private final ObjectMapper objectMapper;

    public SendInvoiceAdapter(PaymentProperties paymentProperties, TelegramApiClient telegramApiClient, ObjectMapper objectMapper) {
        this.paymentProperties = paymentProperties;
        this.telegramApiClient = telegramApiClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public InvoiceMessage send(Invoice invoice) {
        try {
            String payload = objectMapper.writeValueAsString(invoice.getPayload());
            String providerData = objectMapper.writeValueAsString(invoice.getProviderData());

            List<LabeledPrice> prices = List.of(
                new LabeledPrice(invoice.getTitle(), (int) invoice.getPrice().toMinorUnits())
            );

            // Создаем инвойс
            SendInvoice sendInvoice = new SendInvoice();
            sendInvoice.setChatId(invoice.getChatId());
            sendInvoice.setTitle(invoice.getTitle());
            sendInvoice.setDescription(invoice.getDescription());
            sendInvoice.setPayload(payload);
            sendInvoice.setProviderToken(paymentProperties.getToken()); // Токен платежной системы
            sendInvoice.setCurrency(invoice.getPrice().getCurrency().getCurrencyCode()); // "RUB", "USD", "EUR" и т.д.
            sendInvoice.setPrices(prices);
            sendInvoice.setNeedEmail(true);
            sendInvoice.setSendEmailToProvider(true);
            sendInvoice.setProviderData(providerData);

            log.debug("SendInvoice Command {}", objectMapper.writeValueAsString(sendInvoice));

            var telegramInvoiceMessage = telegramApiClient.execute(sendInvoice);

            return InvoiceMessage.builder()
                .chatId(telegramInvoiceMessage.getChatId())
                .messageId(telegramInvoiceMessage.getMessageId())
                .build();
        } catch (JsonProcessingException | TelegramApiException e) {
            log.error("Ошибка отправки ответа счета: {}", invoice);
            throw new RuntimeException(e);
        }
    }
}
