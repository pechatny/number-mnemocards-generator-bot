package com.pechatnikov.numbermnemocardsgeneratorbot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.InvoicePayload;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InvoicePayloadObjectMapperTest {
    @Test
    void shouldConvertInvoicePayloadSuccessfully() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String payload = "{\"orderId\": \"1\"}";
        InvoicePayload invoicePayload = objectMapper.readValue(payload, InvoicePayload.class);

        Assertions.assertEquals(1L, invoicePayload.getOrderId());
    }
}
