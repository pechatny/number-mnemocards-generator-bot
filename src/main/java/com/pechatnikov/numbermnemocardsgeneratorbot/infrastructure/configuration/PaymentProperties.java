package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("payment")
public class PaymentProperties {
    private String token;
}
