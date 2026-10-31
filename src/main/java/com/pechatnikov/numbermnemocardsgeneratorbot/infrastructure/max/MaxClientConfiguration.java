package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration.ConditionalOnMaxBot;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration.MaxBotProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.time.Duration;

@ConditionalOnMaxBot
@Configuration
public class MaxClientConfiguration {
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    // Запас поверх long polling timeout, чтобы клиент не обрывал ожидающий /updates
    private static final Duration READ_TIMEOUT_MARGIN = Duration.ofSeconds(15);

    @Bean
    public MaxClient maxClient(RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper, MaxBotProperties properties) {
        Duration readTimeout = Duration.ofSeconds(properties.getPolling().getTimeoutSeconds()).plus(READ_TIMEOUT_MARGIN);

        var apiRestTemplate = restTemplateBuilder
            .uriTemplateHandler(apiUriTemplateHandler(properties.getBaseUrl()))
            .defaultHeader(HttpHeaders.AUTHORIZATION, properties.getToken())
            .setConnectTimeout(CONNECT_TIMEOUT)
            .setReadTimeout(readTimeout)
            .build();

        var uploadRestTemplate = restTemplateBuilder
            .setConnectTimeout(CONNECT_TIMEOUT)
            .setReadTimeout(readTimeout)
            .build();

        return new MaxClient(apiRestTemplate, uploadRestTemplate, objectMapper, properties);
    }

    /**
     * Строгое кодирование значений переменных шаблона: иначе, например, {@code +} в {@code callback_id}
     * ушёл бы как есть и был бы прочитан сервером как пробел.
     */
    static DefaultUriBuilderFactory apiUriTemplateHandler(String baseUrl) {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.TEMPLATE_AND_VALUES);
        return factory;
    }
}
