package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("bot")
public class BotProperties {
    private String name;
    private String token;
    private int chatId;
}
