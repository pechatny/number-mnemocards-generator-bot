package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
public interface DeleteMessageService {
    void delete(BotPlatform platform, Long chatId, String messageId);
}
