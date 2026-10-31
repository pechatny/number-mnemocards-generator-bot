package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.PaymentAvailabilityPort;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import org.springframework.stereotype.Component;

/**
 * Оплата доступна, если бот платформы включён и для него выставлен {@code bot.<platform>.payments-enabled=true}.
 */
@Component
public class PropertiesPaymentAvailabilityAdapter implements PaymentAvailabilityPort {
    private final TelegramBotProperties telegramBotProperties;
    private final MaxBotProperties maxBotProperties;

    public PropertiesPaymentAvailabilityAdapter(TelegramBotProperties telegramBotProperties, MaxBotProperties maxBotProperties) {
        this.telegramBotProperties = telegramBotProperties;
        this.maxBotProperties = maxBotProperties;
    }

    @Override
    public boolean isAvailable(BotPlatform platform) {
        switch (platform) {
            case TELEGRAM:
                return telegramBotProperties.isEnabled() && telegramBotProperties.isPaymentsEnabled();
            case MAX:
                return maxBotProperties.isEnabled() && maxBotProperties.isPaymentsEnabled();
            default:
                return false;
        }
    }
}
