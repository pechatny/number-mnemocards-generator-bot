package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.PreCheckout;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public interface PreCheckoutService {
    void handle(PreCheckout preCheckout) throws IOException;
}
