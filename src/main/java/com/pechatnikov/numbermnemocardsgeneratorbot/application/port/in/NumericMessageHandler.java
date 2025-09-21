package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Message;

import java.io.IOException;

public interface NumericMessageHandler {
    void handle(GetOrCreateUserCommand getOrCreateUserCommand, Message message) throws IOException;
}
