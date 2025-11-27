package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.ButtonMessage;

public interface SendButtonMessageService {
    void sendButtonMessage(ButtonMessage buttonMessage);
}
