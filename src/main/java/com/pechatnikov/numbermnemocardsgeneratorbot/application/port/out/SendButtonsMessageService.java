package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.ButtonsMessage;

public interface SendButtonsMessageService {
    void sendButtonsMessage(ButtonsMessage buttonsMessage);
}
