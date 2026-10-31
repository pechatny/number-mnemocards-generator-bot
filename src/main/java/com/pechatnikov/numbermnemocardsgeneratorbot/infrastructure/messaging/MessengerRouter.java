package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.messaging;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.DeleteMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendButtonMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendButtonsMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendCallbackAnswerService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendPhotoService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.ButtonMessage;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.ButtonsMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Реализует исходящие порты сообщений и направляет каждый вызов в шлюз мессенджера,
 * из которого пришёл пользователь. Шлюзы регистрируются только для включённых ботов.
 */
@Slf4j
@Component
public class MessengerRouter implements
    SendMessageService,
    SendPhotoService,
    SendButtonMessageService,
    SendButtonsMessageService,
    SendCallbackAnswerService,
    DeleteMessageService {

    private final Map<BotPlatform, MessengerGateway> gateways = new EnumMap<>(BotPlatform.class);

    public MessengerRouter(List<MessengerGateway> gateways) {
        gateways.forEach(gateway -> {
            MessengerGateway previous = this.gateways.put(gateway.platform(), gateway);
            if (previous != null) {
                throw new IllegalStateException("Несколько шлюзов для платформы " + gateway.platform());
            }
        });
        log.info("Включённые мессенджеры: {}", this.gateways.keySet());
    }

    public Set<BotPlatform> enabledPlatforms() {
        return Set.copyOf(gateways.keySet());
    }

    @Override
    public void sendMessage(BotPlatform platform, Long chatId, String message) {
        gateway(platform).sendMessage(chatId, message);
    }

    @Override
    public void sendPhoto(BotPlatform platform, Long chatId, File file) {
        gateway(platform).sendPhoto(chatId, file);
    }

    @Override
    public void sendButtonMessage(ButtonMessage buttonMessage) {
        gateway(buttonMessage.getPlatform()).sendButtonMessage(buttonMessage);
    }

    @Override
    public void sendButtonsMessage(ButtonsMessage buttonsMessage) {
        gateway(buttonsMessage.getPlatform()).sendButtonsMessage(buttonsMessage);
    }

    @Override
    public void sendCallbackAnswer(BotPlatform platform, String callbackId, String message) {
        gateway(platform).sendCallbackAnswer(callbackId, message);
    }

    @Override
    public void delete(BotPlatform platform, Long chatId, String messageId) {
        gateway(platform).deleteMessage(chatId, messageId);
    }

    private MessengerGateway gateway(BotPlatform platform) {
        MessengerGateway gateway = gateways.get(platform);
        if (gateway == null) {
            throw new MessengerPlatformNotEnabledException(platform);
        }
        return gateway;
    }
}
