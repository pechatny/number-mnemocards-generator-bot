package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max;

import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration.MaxBotProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class MaxClientConfigurationTest {

    @EnableConfigurationProperties(MaxBotProperties.class)
    static class PropertiesConfig {
    }

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(
            JacksonAutoConfiguration.class,
            HttpMessageConvertersAutoConfiguration.class,
            RestTemplateAutoConfiguration.class
        ))
        .withUserConfiguration(PropertiesConfig.class, MaxClientConfiguration.class);

    @Test
    void shouldCreateClientWhenMaxEnabled() {
        runner
            .withPropertyValues("bot.max.enabled=true", "bot.max.token=secret")
            .run(context -> assertThat(context).hasSingleBean(MaxClient.class));
    }

    @Test
    void shouldNotCreateClientWhenMaxDisabled() {
        runner
            .withPropertyValues("bot.max.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(MaxClient.class));
    }
}
