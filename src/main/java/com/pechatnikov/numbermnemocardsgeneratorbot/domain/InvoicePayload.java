package com.pechatnikov.numbermnemocardsgeneratorbot.domain;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class InvoicePayload {
    Long orderId;
}
