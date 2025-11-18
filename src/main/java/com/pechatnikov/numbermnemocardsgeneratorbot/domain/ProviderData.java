package com.pechatnikov.numbermnemocardsgeneratorbot.domain;

import lombok.Value;
import lombok.Builder;

import java.util.List;

@Value
@Builder
public class ProviderData {
    Receipt receipt;
    Integer tax_system_code;

    @Value
    @Builder
    public static class Receipt {
        List<ReceiptItem> items;

    }

    @Value
    @Builder
    public static class ReceiptItem {
        String description;
        Long quantity;
        Amount amount;
        Integer vat_code;
        String payment_mode;
        String payment_subject;
    }

    @Value
    @Builder
    public static class Amount {
        Long value;
        String currency;
    }
}
