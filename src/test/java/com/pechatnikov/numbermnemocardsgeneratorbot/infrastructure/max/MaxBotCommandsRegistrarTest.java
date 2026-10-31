package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.PaymentAvailabilityPort;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxBotCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class MaxBotCommandsRegistrarTest {
    private MaxClient maxClient;
    private PaymentAvailabilityPort paymentAvailabilityPort;
    private MaxBotCommandsRegistrar registrar;

    @BeforeEach
    void setUp() {
        maxClient = mock(MaxClient.class);
        paymentAvailabilityPort = mock(PaymentAvailabilityPort.class);
        registrar = new MaxBotCommandsRegistrar(maxClient, paymentAvailabilityPort);
    }

    @Test
    void shouldRegisterCommandsWithoutRefillWhenPaymentsUnavailable() {
        when(paymentAvailabilityPort.isAvailable(BotPlatform.MAX)).thenReturn(false);

        registrar.registerCommands();

        verify(maxClient).setCommands(List.of(
            new MaxBotCommand("start", "Как пользоваться ботом"),
            new MaxBotCommand("balance", "Баланс цифр")
        ));
    }

    @Test
    void shouldIncludeRefillWhenPaymentsAvailable() {
        when(paymentAvailabilityPort.isAvailable(BotPlatform.MAX)).thenReturn(true);

        assertThat(registrar.commands())
            .extracting(MaxBotCommand::getName)
            .containsExactly("start", "balance", "balance_refill");
    }

    @Test
    void shouldNotFailStartupWhenRegistrationFails() {
        doThrow(new MaxApiException(401, "verify.token", "Invalid access_token", null))
            .when(maxClient).setCommands(anyList());

        assertThatCode(() -> registrar.registerCommands()).doesNotThrowAnyException();
    }
}
