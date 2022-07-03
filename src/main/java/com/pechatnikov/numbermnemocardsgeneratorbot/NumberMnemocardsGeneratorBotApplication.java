package com.pechatnikov.numbermnemocardsgeneratorbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class NumberMnemocardsGeneratorBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(NumberMnemocardsGeneratorBotApplication.class, args);
    }
}
