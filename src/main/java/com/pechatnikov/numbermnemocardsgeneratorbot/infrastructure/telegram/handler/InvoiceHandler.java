package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.UserService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.InvoiceService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.PaymentService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Money;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration.PaymentProperties;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.mapper.TelegramUpdateMapper;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.math.BigDecimal;
import java.util.Currency;

@Component
public class InvoiceHandler {
    private final PaymentService paymentService;
    private final InvoiceService invoiceService;
    private final UserService userService;
    private final TelegramUpdateMapper telegramUpdateMapper;
    private final PaymentProperties paymentProperties;
    private final ObjectMapper objectMapper;

    public InvoiceHandler(PaymentService paymentService, InvoiceService invoiceService, UserService userService, TelegramUpdateMapper telegramUpdateMapper, PaymentProperties paymentProperties, ObjectMapper objectMapper) {
        this.paymentService = paymentService;
        this.invoiceService = invoiceService;
        this.userService = userService;
        this.telegramUpdateMapper = telegramUpdateMapper;
        this.paymentProperties = paymentProperties;
        this.objectMapper = objectMapper;
    }

    public void handle(Update update) throws JsonProcessingException {
        var user = userService.getOrCreateUser(telegramUpdateMapper.toGetOrCreateUserCommand(update));

        var invoice = invoiceService.create(
            user,
            update.getMessage().getChatId().toString(),
            "Хотите купить дополнительные токены для мнемокарточек?",
            "Токен - это цифра в числе. Курс 1 токен (цифра) = 50 копеек",
            new Money(BigDecimal.valueOf(100), Currency.getInstance("RUB"))
        );

        var payload = objectMapper.writeValueAsString(invoice.getPayload());

        paymentService.sendInvoice(
            invoice.getChatId(),
            invoice.getTitle(),
            invoice.getDescription(),
            payload,
            paymentProperties.getToken(),
            invoice.getPrice().getCurrency().getCurrencyCode(),
            invoice.getPrice().getAmount().intValue()
        );
    }
}
