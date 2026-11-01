package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.command.CommandProcessorRegistrar;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.command.Command;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.command.CommandContext;
import org.springframework.beans.factory.annotation.Autowired;

public interface CommandProcessor {
    Command getCommand();

    @Autowired
    default void selfRegister(CommandProcessorRegistrar commandProcessorRegistrar){
        commandProcessorRegistrar.register(this);
    }

    void process(CommandContext context);
}
