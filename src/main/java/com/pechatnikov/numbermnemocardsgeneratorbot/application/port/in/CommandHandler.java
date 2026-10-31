package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.command.CommandContext;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.command.Command;

public interface CommandHandler {
    void handle(Command command, CommandContext context);
}
