package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration;

import com.pechatnikov.numbermnemocardsgeneratorbot.BaseIntegrationTest;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.MaxClient;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.messaging.MessengerPlatformNotEnabledException;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.messaging.MessengerRouter;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.MnemocardsGeneratorBot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

/**
 * Конфигурация по умолчанию: Max включён, Telegram выключен.
 */
@TestPropertySource(properties = {
    "bot.max.enabled=true",
    "bot.max.token=test-token"
})
class MaxEnabledContextTest extends BaseIntegrationTest {

    @MockBean
    private MaxClient maxClient;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MessengerRouter messengerRouter;

    @Autowired
    private SendMessageService sendMessageService;

    @Test
    void shouldRouteToMaxOnly() {
        assertThat(messengerRouter.enabledPlatforms()).containsExactly(BotPlatform.MAX);
        assertThat(context.getBeansOfType(MnemocardsGeneratorBot.class)).isEmpty();
    }

    @Test
    void shouldDeliverMaxMessagesThroughApplicationPort() {
        sendMessageService.sendMessage(BotPlatform.MAX, 42L, "*Ваш баланс*");

        verify(maxClient).sendText(42L, "**Ваш баланс**");
    }

    @Test
    void shouldRejectTelegramMessagesWhenTelegramDisabled() {
        assertThatThrownBy(() -> sendMessageService.sendMessage(BotPlatform.TELEGRAM, 42L, "text"))
            .isInstanceOf(MessengerPlatformNotEnabledException.class);
    }
}
