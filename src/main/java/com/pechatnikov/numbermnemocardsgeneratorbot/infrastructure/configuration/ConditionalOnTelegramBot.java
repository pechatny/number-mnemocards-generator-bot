package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Бин создаётся только при {@code bot.telegram.enabled=true}.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ConditionalOnProperty(prefix = "bot.telegram", name = "enabled", havingValue = "true")
public @interface ConditionalOnTelegramBot {
}
