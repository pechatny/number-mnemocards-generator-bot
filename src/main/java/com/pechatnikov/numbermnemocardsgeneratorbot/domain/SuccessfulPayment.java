package com.pechatnikov.numbermnemocardsgeneratorbot.domain;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class SuccessfulPayment {
    Money amount;
    InvoicePayload invoicePayload;
}
