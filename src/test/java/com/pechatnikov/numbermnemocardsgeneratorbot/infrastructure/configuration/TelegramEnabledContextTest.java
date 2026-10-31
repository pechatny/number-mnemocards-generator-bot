package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration;

import com.pechatnikov.numbermnemocardsgeneratorbot.BaseIntegrationTest;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendInvoiceService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.messaging.MessengerRouter;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.MnemocardsGeneratorBot;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.SendInvoiceAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Telegram-бот включён. Автоконфигурация стартера выключена, чтобы бот не регистрировался в Telegram API.
 */
@TestPropertySource(properties = {
    "bot.telegram.enabled=true",
    "bot.telegram.token=test-token",
    "telegrambots.enabled=false"
})
class TelegramEnabledContextTest extends BaseIntegrationTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MessengerRouter messengerRouter;

    @Test
    void shouldCreateTelegramBeans() {
        assertThat(context.getBeansOfType(MnemocardsGeneratorBot.class)).hasSize(1);
    }

    @Test
    void shouldRouteToTelegramOnly() {
        assertThat(messengerRouter.enabledPlatforms()).containsExactly(BotPlatform.TELEGRAM);
    }

    @Test
    void shouldUseTelegramInvoiceAdapter() {
        assertThat(context.getBean(SendInvoiceService.class)).isInstanceOf(SendInvoiceAdapter.class);
    }
}
