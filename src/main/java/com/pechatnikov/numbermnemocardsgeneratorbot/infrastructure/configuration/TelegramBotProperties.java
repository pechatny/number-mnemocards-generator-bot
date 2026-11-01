package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

@Data
@ConfigurationProperties("bot.telegram")
public class TelegramBotProperties {
    private boolean enabled;
    private String name;
    private String token;
    private boolean paymentsEnabled;

    @PostConstruct
    void validate() {
        if (enabled && !StringUtils.hasText(token)) {
            throw new IllegalStateException("bot.telegram.token обязателен, когда bot.telegram.enabled=true");
        }
    }
}
