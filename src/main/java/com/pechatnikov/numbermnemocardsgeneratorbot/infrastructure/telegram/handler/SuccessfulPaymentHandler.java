package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Slf4j
@Component
public class SuccessfulPaymentHandler {
    public void handle(Message message) {
        log.info("Пришло сообщение об успешной оплате {}", message);

    }
}
