package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PropertiesPaymentAvailabilityAdapterTest {

    @Test
    void shouldAllowTelegramPaymentsWhenBotAndPaymentsEnabled() {
        assertThat(adapter(true, true).isAvailable(BotPlatform.TELEGRAM)).isTrue();
    }

    @Test
    void shouldDenyTelegramPaymentsWhenPaymentsDisabled() {
        assertThat(adapter(true, false).isAvailable(BotPlatform.TELEGRAM)).isFalse();
    }

    @Test
    void shouldDenyTelegramPaymentsWhenBotDisabled() {
        assertThat(adapter(false, true).isAvailable(BotPlatform.TELEGRAM)).isFalse();
    }

    @Test
    void shouldDenyMaxPaymentsByDefault() {
        assertThat(adapter(true, true).isAvailable(BotPlatform.MAX)).isFalse();
    }

    private static PropertiesPaymentAvailabilityAdapter adapter(boolean telegramEnabled, boolean telegramPayments) {
        TelegramBotProperties telegram = new TelegramBotProperties();
        telegram.setEnabled(telegramEnabled);
        telegram.setPaymentsEnabled(telegramPayments);
        MaxBotProperties max = new MaxBotProperties();
        max.setEnabled(true);
        return new PropertiesPaymentAvailabilityAdapter(telegram, max);
    }
}
