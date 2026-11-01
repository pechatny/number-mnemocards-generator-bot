package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.PaymentAvailabilityPort;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.command.Command;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration.ConditionalOnMaxBot;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxBotCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Меню команд Max-бота (аналог /setcommands в Telegram BotFather). Задаётся при каждом старте
 * из {@link Command}, поэтому меню не расходится с кодом и настраивается для любого токена.
 */
@Slf4j
@ConditionalOnMaxBot
@Component
public class MaxBotCommandsRegistrar {
    private final MaxClient maxClient;
    private final PaymentAvailabilityPort paymentAvailabilityPort;

    public MaxBotCommandsRegistrar(MaxClient maxClient, PaymentAvailabilityPort paymentAvailabilityPort) {
        this.maxClient = maxClient;
        this.paymentAvailabilityPort = paymentAvailabilityPort;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void registerCommands() {
        List<MaxBotCommand> commands = commands();
        try {
            maxClient.setCommands(commands);
            log.info("Команды Max-бота зарегистрированы: {}", commands);
        } catch (Exception e) {
            // Меню — удобство: без него бот продолжает принимать команды, текстом
            log.error("Не удалось зарегистрировать команды Max-бота", e);
        }
    }

    List<MaxBotCommand> commands() {
        boolean paymentsAvailable = paymentAvailabilityPort.isAvailable(BotPlatform.MAX);

        return Arrays.stream(Command.values())
            // Пункт пополнения в меню только там, где оплата работает
            .filter(command -> command != Command.BALANCE_REFILL || paymentsAvailable)
            .map(command -> new MaxBotCommand(command.getValue().substring(1), command.getDescription()))
            .collect(Collectors.toList());
    }
}
