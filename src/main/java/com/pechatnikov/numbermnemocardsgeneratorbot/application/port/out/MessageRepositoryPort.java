package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Message;

public interface MessageRepositoryPort {
    Message save(Message message);
}
