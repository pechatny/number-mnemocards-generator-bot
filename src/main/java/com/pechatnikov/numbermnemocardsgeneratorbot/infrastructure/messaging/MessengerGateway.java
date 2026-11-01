package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.messaging;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.ButtonMessage;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.ButtonsMessage;

import java.io.File;

/**
 * Отправка сообщений в конкретный мессенджер. Реализуется по одному бину на платформу,
 * выбор нужной реализации делает {@link MessengerRouter}.
 */
public interface MessengerGateway {
    BotPlatform platform();

    void sendMessage(Long chatId, String message);

    void sendPhoto(Long chatId, File photo);

    void sendButtonMessage(ButtonMessage buttonMessage);

    void sendButtonsMessage(ButtonsMessage buttonsMessage);

    void sendCallbackAnswer(String callbackId, String message);

    void deleteMessage(Long chatId, String messageId);
}
