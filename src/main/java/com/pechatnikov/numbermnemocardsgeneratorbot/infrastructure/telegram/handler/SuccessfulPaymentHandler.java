package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.SuccessfulPaymentUseCase;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.InvoicePayload;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Money;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.SuccessfulPayment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Currency;

@Slf4j
@Component
public class SuccessfulPaymentHandler {
    private final SuccessfulPaymentUseCase successfulPaymentUseCase;
    private final ObjectMapper objectMapper;

    public SuccessfulPaymentHandler(SuccessfulPaymentUseCase successfulPaymentUseCase, ObjectMapper objectMapper) {
        this.successfulPaymentUseCase = successfulPaymentUseCase;
        this.objectMapper = objectMapper;
    }

    public void handle(Message message) {
        try {
            log.info("Пришло сообщение об успешной оплате {}", message);
            var telegramSuccessfulPayment = message.getSuccessfulPayment();
            InvoicePayload invoicePayload = objectMapper.readValue(
                telegramSuccessfulPayment.getInvoicePayload(), InvoicePayload.class
            );
            var paymentAmount = Money.fromMinorUnits(
                telegramSuccessfulPayment.getTotalAmount(),
                Currency.getInstance(telegramSuccessfulPayment.getCurrency())
            );
            var successfulPayment = SuccessfulPayment.builder()
                .invoicePayload(invoicePayload)
                .amount(paymentAmount)
                .build();
            successfulPaymentUseCase.handle(successfulPayment);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
