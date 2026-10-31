package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration;

import com.pechatnikov.numbermnemocardsgeneratorbot.BaseIntegrationTest;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendInvoiceService;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.messaging.MessengerRouter;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.MnemocardsGeneratorBot;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.TelegramApiClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Профиль test: оба бота выключены, токены не заданы.
 */
class BotsDisabledContextTest extends BaseIntegrationTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MessengerRouter messengerRouter;

    @Test
    void shouldStartWithoutTelegramBeans() {
        assertThat(context.getBeansOfType(MnemocardsGeneratorBot.class)).isEmpty();
        assertThat(context.getBeansOfType(TelegramApiClient.class)).isEmpty();
        assertThat(context.getBeansOfType(TelegramBotsApi.class)).isEmpty();
    }

    @Test
    void shouldHaveNoEnabledMessengers() {
        assertThat(messengerRouter.enabledPlatforms()).isEmpty();
    }

    @Test
    void shouldUsePaymentStubWhenTelegramDisabled() {
        assertThatThrownBy(() -> context.getBean(SendInvoiceService.class).send(null))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
