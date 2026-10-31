package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;

import java.io.File;

public interface SendPhotoService {
    void sendPhoto(BotPlatform platform, Long chatId, File file);
}
