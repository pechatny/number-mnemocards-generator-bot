package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration;

import com.pechatnikov.numbermnemocardsgeneratorbot.BaseIntegrationTest;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.MaxClient;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.messaging.MessengerRouter;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.MnemocardsGeneratorBot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Оба бота включены одновременно. Автоконфигурация telegrambots выключена, чтобы не ходить в Telegram API.
 */
@TestPropertySource(properties = {
    "bot.telegram.enabled=true",
    "bot.telegram.token=test-token",
    "telegrambots.enabled=false",
    "bot.max.enabled=true",
    "bot.max.token=test-token"
})
class BothBotsEnabledContextTest extends BaseIntegrationTest {

    @MockBean
    private MaxClient maxClient;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MessengerRouter messengerRouter;

    @Test
    void shouldRouteToBothPlatforms() {
        assertThat(messengerRouter.enabledPlatforms()).containsExactlyInAnyOrder(BotPlatform.TELEGRAM, BotPlatform.MAX);
        assertThat(context.getBeansOfType(MnemocardsGeneratorBot.class)).hasSize(1);
    }
}
