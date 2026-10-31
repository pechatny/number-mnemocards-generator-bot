package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendInvoiceService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendPreCheckoutService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BotEnablementTest {

    @Configuration
    static class BotBeans {
        @Bean
        @ConditionalOnTelegramBot
        TelegramMarker telegramMarker() {
            return new TelegramMarker();
        }

        @Bean
        @ConditionalOnMaxBot
        MaxMarker maxMarker() {
            return new MaxMarker();
        }
    }

    static class TelegramMarker {
    }

    static class MaxMarker {
    }

    @EnableConfigurationProperties({TelegramBotProperties.class, MaxBotProperties.class})
    static class PropertiesConfig {
    }

    private final ApplicationContextRunner conditionsRunner = new ApplicationContextRunner()
        .withUserConfiguration(BotBeans.class, TelegramPaymentsDisabledConfiguration.class);

    private final ApplicationContextRunner propertiesRunner = new ApplicationContextRunner()
        .withUserConfiguration(PropertiesConfig.class);

    @Test
    void shouldCreateNoBotBeansWhenPropertiesMissing() {
        conditionsRunner.run(context -> {
            assertThat(context).doesNotHaveBean(TelegramMarker.class);
            assertThat(context).doesNotHaveBean(MaxMarker.class);
        });
    }

    @Test
    void shouldCreateOnlyMaxBeansWhenOnlyMaxEnabled() {
        conditionsRunner
            .withPropertyValues("bot.telegram.enabled=false", "bot.max.enabled=true")
            .run(context -> {
                assertThat(context).doesNotHaveBean(TelegramMarker.class);
                assertThat(context).hasSingleBean(MaxMarker.class);
            });
    }

    @Test
    void shouldCreateBothBotBeansWhenBothEnabled() {
        conditionsRunner
            .withPropertyValues("bot.telegram.enabled=true", "bot.max.enabled=true")
            .run(context -> {
                assertThat(context).hasSingleBean(TelegramMarker.class);
                assertThat(context).hasSingleBean(MaxMarker.class);
            });
    }

    @Test
    void shouldProvidePaymentStubsWhenTelegramDisabled() {
        conditionsRunner
            .withPropertyValues("bot.telegram.enabled=false")
            .run(context -> {
                assertThat(context).hasSingleBean(SendInvoiceService.class);
                assertThat(context).hasSingleBean(SendPreCheckoutService.class);
                assertThatThrownBy(() -> context.getBean(SendInvoiceService.class).send(null))
                    .isInstanceOf(UnsupportedOperationException.class);
                assertThatThrownBy(() -> context.getBean(SendPreCheckoutService.class).send("id", true))
                    .isInstanceOf(UnsupportedOperationException.class);
            });
    }

    @Test
    void shouldNotProvidePaymentStubsWhenTelegramEnabled() {
        conditionsRunner
            .withPropertyValues("bot.telegram.enabled=true")
            .run(context -> {
                assertThat(context).doesNotHaveBean(SendInvoiceService.class);
                assertThat(context).doesNotHaveBean(SendPreCheckoutService.class);
            });
    }

    @Test
    void shouldFailWhenEnabledTelegramBotHasNoToken() {
        propertiesRunner
            .withPropertyValues("bot.telegram.enabled=true")
            .run(context -> assertThat(context).getFailure()
                .hasRootCauseMessage("bot.telegram.token обязателен, когда bot.telegram.enabled=true"));
    }

    @Test
    void shouldFailWhenEnabledMaxBotHasNoToken() {
        propertiesRunner
            .withPropertyValues("bot.max.enabled=true", "bot.max.token=")
            .run(context -> assertThat(context).getFailure()
                .hasRootCauseMessage("bot.max.token обязателен, когда bot.max.enabled=true"));
    }

    @Test
    void shouldFailWhenMaxPaymentsEnabled() {
        propertiesRunner
            .withPropertyValues("bot.max.enabled=true", "bot.max.token=secret", "bot.max.payments-enabled=true")
            .run(context -> assertThat(context).getFailure()
                .hasRootCauseMessage("Оплата в Max ещё не реализована: bot.max.payments-enabled должен быть false"));
    }

    @Test
    void shouldStartWithoutTokensWhenBotsDisabled() {
        propertiesRunner
            .withPropertyValues("bot.telegram.enabled=false", "bot.max.enabled=false")
            .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    void shouldBindMaxProperties() {
        propertiesRunner
            .withPropertyValues(
                "bot.max.enabled=true",
                "bot.max.token=secret",
                "bot.max.payments-enabled=false",
                "bot.max.polling.timeout-seconds=45",
                "bot.max.polling.limit=50"
            )
            .run(context -> {
                MaxBotProperties properties = context.getBean(MaxBotProperties.class);
                assertThat(properties.getToken()).isEqualTo("secret");
                assertThat(properties.isPaymentsEnabled()).isFalse();
                assertThat(properties.getBaseUrl()).isEqualTo("https://platform-api.max.ru");
                assertThat(properties.getPolling().getTimeoutSeconds()).isEqualTo(45);
                assertThat(properties.getPolling().getLimit()).isEqualTo(50);
            });
    }
}
