package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out;

import java.io.File;

public interface SendPhotoService {
    void sendPhoto(Long chatId, File file);
}
