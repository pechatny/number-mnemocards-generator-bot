package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out;

public interface SendPreCheckoutService {
    void send(String preCheckoutId, boolean result);
}
