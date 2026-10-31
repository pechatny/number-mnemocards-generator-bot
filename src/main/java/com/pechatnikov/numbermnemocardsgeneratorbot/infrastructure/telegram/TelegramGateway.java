package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.ButtonMessage;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.ButtonsMessage;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration.ConditionalOnTelegramBot;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.messaging.MessengerGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;

@RequiredArgsConstructor
@ConditionalOnTelegramBot
@Component
public class TelegramGateway implements MessengerGateway {
    private final SendMessageAdapter sendMessageAdapter;
    private final SendPhotoAdapter sendPhotoAdapter;
    private final SendButtonMessageAdapter sendButtonMessageAdapter;
    private final SendButtonsMessageAdapter sendButtonsMessageAdapter;
    private final SendCallbackAnswerAdapter sendCallbackAnswerAdapter;
    private final DeleteMessageAdapter deleteMessageAdapter;

    @Override
    public BotPlatform platform() {
        return BotPlatform.TELEGRAM;
    }

    @Override
    public void sendMessage(Long chatId, String message) {
        sendMessageAdapter.sendMessage(chatId, message);
    }

    @Override
    public void sendPhoto(Long chatId, File photo) {
        sendPhotoAdapter.sendPhoto(chatId, photo);
    }

    @Override
    public void sendButtonMessage(ButtonMessage buttonMessage) {
        sendButtonMessageAdapter.sendButtonMessage(buttonMessage);
    }

    @Override
    public void sendButtonsMessage(ButtonsMessage buttonsMessage) {
        sendButtonsMessageAdapter.sendButtonsMessage(buttonsMessage);
    }

    @Override
    public void sendCallbackAnswer(String callbackId, String message) {
        sendCallbackAnswerAdapter.sendCallbackAnswer(callbackId, message);
    }

    @Override
    public void deleteMessage(Long chatId, String messageId) {
        deleteMessageAdapter.delete(chatId, messageId);
    }
}
