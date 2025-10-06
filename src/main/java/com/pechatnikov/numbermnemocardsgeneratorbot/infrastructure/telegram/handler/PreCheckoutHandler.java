package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.PreCheckoutService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.InvoicePayload;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.PreCheckout;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.payments.PreCheckoutQuery;

import java.io.IOException;

@Component
public class PreCheckoutHandler {
    private final ObjectMapper objectMapper;
    private final PreCheckoutService preCheckoutService;

    public PreCheckoutHandler(ObjectMapper objectMapper, PreCheckoutService preCheckoutService) {
        this.objectMapper = objectMapper;
        this.preCheckoutService = preCheckoutService;
    }

    public void handle(PreCheckoutQuery preCheckoutQuery) throws IOException {
        var orderId = objectMapper.readValue(preCheckoutQuery.getInvoicePayload(), InvoicePayload.class).getOrderId();
        var preCheckOut = PreCheckout.builder()
            .id(preCheckoutQuery.getId())
            .orderId(orderId)
            .build();

        preCheckoutService.handle(preCheckOut);
    }
}
