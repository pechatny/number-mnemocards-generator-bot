package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out;

public interface SendMessageService {
    void sendMessage(Long chatId, String message);
}
