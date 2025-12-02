package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.command.CommandProcessorRegistrar;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.command.Command;
import org.springframework.beans.factory.annotation.Autowired;

public interface CommandProcessor {
    Command getCommand();

    @Autowired
    default void selfRegister(CommandProcessorRegistrar commandProcessorRegistrar){
        commandProcessorRegistrar.register(this);
    }

    void process(Long telegramId, Long chatId);
}
