package com.pechatnikov.numbermnemocardsgeneratorbot.application.service.command;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.CommandProcessor;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.command.Command;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class CommandProcessorRegistrar {
    private final Map<Command, CommandProcessor> registry = new HashMap<>();

    public void register(CommandProcessor commandProcessor) {
        registry.put(commandProcessor.getCommand(), commandProcessor);
    }

    public CommandProcessor getCommandProcessor(Command command) {
        CommandProcessor commandProcessor = registry.get(command);
        if (commandProcessor == null) {
            log.debug("Команда не поддерживается {}", command);
            throw new IllegalArgumentException("Command not supported");
        }

        return commandProcessor;
    }

}
