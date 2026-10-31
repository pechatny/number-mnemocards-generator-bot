package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.messaging;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;

public class MessengerPlatformNotEnabledException extends RuntimeException {
    public MessengerPlatformNotEnabledException(BotPlatform platform) {
        super("Бот для платформы " + platform + " не включён");
    }
}
