package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

@Data
@ConfigurationProperties("bot.max")
public class MaxBotProperties {
    private boolean enabled;
    private String token;
    private boolean paymentsEnabled;
    private String baseUrl = "https://platform-api.max.ru";
    private Polling polling = new Polling();
    private Upload upload = new Upload();

    @PostConstruct
    void validate() {
        if (enabled && !StringUtils.hasText(token)) {
            throw new IllegalStateException("bot.max.token обязателен, когда bot.max.enabled=true");
        }
        // SendInvoiceService реализован только для Telegram: включённый флаг отправил бы инвойс Max-чата в Telegram
        if (paymentsEnabled) {
            throw new IllegalStateException("Оплата в Max ещё не реализована: bot.max.payments-enabled должен быть false");
        }
    }

    @Data
    public static class Polling {
        private int timeoutSeconds = 30;
        private int limit = 100;
        /** Пауза после ошибки /updates; удваивается до maxErrorBackoffMillis */
        private long errorBackoffMillis = 1000;
        private long maxErrorBackoffMillis = 30000;
        private int workerThreads = 10;
    }

    /** Повторная отправка сообщения, пока загруженная картинка обрабатывается ({@code attachment.not.ready}). */
    @Data
    public static class Upload {
        private int retryAttempts = 5;
        private long retryDelayMillis = 1000;
    }
}
