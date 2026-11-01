package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
public interface SendMessageService {
    void sendMessage(BotPlatform platform, Long chatId, String message);
}
