package com.pechatnikov.numbermnemocardsgeneratorbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("bot")
public class BotProperties {
    private String name;
    private String token;
}
