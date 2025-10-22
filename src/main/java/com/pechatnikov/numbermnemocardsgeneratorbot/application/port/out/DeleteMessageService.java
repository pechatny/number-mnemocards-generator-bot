package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out;

public interface DeleteMessageService {
    void delete(Long chatId, Integer messageId);
}
