package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out;

public interface SendCallbackAnswerService {
    void sendCallbackAnswer(String callbackId, String message);
}
