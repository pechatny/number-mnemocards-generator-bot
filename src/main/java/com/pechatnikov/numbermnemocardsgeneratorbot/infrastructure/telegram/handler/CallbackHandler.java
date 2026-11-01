package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.CallbackService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Callback;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration.ConditionalOnTelegramBot;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.dto.CallbackData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@ConditionalOnTelegramBot
@Component
public class CallbackHandler {
    private final CallbackService callbackService;
    private final ObjectMapper objectMapper;

    public CallbackHandler(CallbackService callbackService, ObjectMapper objectMapper) {
        this.callbackService = callbackService;
        this.objectMapper = objectMapper;
    }

    public void handle(Update update) throws JsonProcessingException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String messageId = String.valueOf(update.getCallbackQuery().getMessage().getMessageId());
        String callbackQueryId = update.getCallbackQuery().getId();
        CallbackData callbackData = objectMapper.readValue(update.getCallbackQuery().getData(), CallbackData.class);
        Long telegramId = update.getCallbackQuery().getFrom().getId();

        Callback callback = Callback.builder()
            .type(callbackData.getType())
            .platform(BotPlatform.TELEGRAM)
            .externalUserId(telegramId)
            .value(callbackData.getValue())
            .chatId(chatId)
            .messageId(messageId)
            .callbackQueryId(callbackQueryId)
            .build();

        callbackService.process(callback);
    }
}
