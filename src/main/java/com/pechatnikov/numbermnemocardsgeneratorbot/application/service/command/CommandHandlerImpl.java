package com.pechatnikov.numbermnemocardsgeneratorbot.application.service.command;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.CommandHandler;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.command.Command;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.command.CommandContext;
import org.springframework.stereotype.Service;

@Service
public class CommandHandlerImpl implements CommandHandler {
    private final CommandProcessorRegistrar commandProcessorRegistrar;

    public CommandHandlerImpl(CommandProcessorRegistrar commandProcessorRegistrar, SendMessageService sendMessageService) {
        this.commandProcessorRegistrar = commandProcessorRegistrar;
    }

    @Override
    public void handle(Command command, CommandContext context) {
        var processor = commandProcessorRegistrar.getCommandProcessor(command);
        processor.process(context);
    }
}
