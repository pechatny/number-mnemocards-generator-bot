package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendInvoiceService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendPreCheckoutService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Оплата сейчас реализована только через Telegram. Когда Telegram-бот выключен,
 * платёжные порты получают заглушки, чтобы зависящие от них сервисы могли стартовать.
 */
@Configuration
@ConditionalOnProperty(prefix = "bot.telegram", name = "enabled", havingValue = "false", matchIfMissing = true)
public class TelegramPaymentsDisabledConfiguration {
    private static final String MESSAGE = "Оплата недоступна: Telegram-бот выключен";

    @Bean
    public SendInvoiceService sendInvoiceService() {
        return invoice -> {
            throw new UnsupportedOperationException(MESSAGE);
        };
    }

    @Bean
    public SendPreCheckoutService sendPreCheckoutService() {
        return (preCheckoutId, result) -> {
            throw new UnsupportedOperationException(MESSAGE);
        };
    }
}
