package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.SuccessfulPayment;

public interface SuccessfulPaymentUseCase {
    void handle(SuccessfulPayment successfulPayment);
}
