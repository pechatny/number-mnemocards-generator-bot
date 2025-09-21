package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Message;

public interface MessageService {
    Message save(Message message);
}
