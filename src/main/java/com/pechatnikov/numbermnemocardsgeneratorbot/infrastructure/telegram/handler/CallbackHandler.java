package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.callback.CallbackService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Callback;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.TelegramApiClient;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.dto.CallbackData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
public class CallbackHandler {
    private final TelegramApiClient telegramApiClient;
    private final CallbackService callbackService;
    private final ObjectMapper objectMapper;

    public CallbackHandler(TelegramApiClient telegramApiClient, CallbackService callbackService, ObjectMapper objectMapper) {
        this.telegramApiClient = telegramApiClient;
        this.callbackService = callbackService;
        this.objectMapper = objectMapper;
    }

    public void handle(Update update) throws JsonProcessingException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        String callbackQueryId = update.getCallbackQuery().getId();
        CallbackData callbackData = objectMapper.readValue(update.getCallbackQuery().getData(), CallbackData.class);
        Long telegramId = update.getCallbackQuery().getFrom().getId();

        Callback callback = Callback.builder()
            .type(callbackData.getType())
            .telegramId(telegramId)
            .value(callbackData.getValue())
            .chatId(chatId)
            .messageId(messageId)
            .callbackQueryId(callbackQueryId)
            .build();

        callbackService.process(callback);
    }
}
