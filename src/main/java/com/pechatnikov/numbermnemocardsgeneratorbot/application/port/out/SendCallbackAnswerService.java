package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
public interface SendCallbackAnswerService {
    void sendCallbackAnswer(BotPlatform platform, String callbackId, String message);
}
