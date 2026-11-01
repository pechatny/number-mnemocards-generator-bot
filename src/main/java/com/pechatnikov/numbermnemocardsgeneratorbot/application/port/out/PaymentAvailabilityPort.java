package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;

public interface PaymentAvailabilityPort {
    String PAYMENTS_UNAVAILABLE_MESSAGE = "Пополнение баланса в этом мессенджере пока недоступно";

    boolean isAvailable(BotPlatform platform);
}
