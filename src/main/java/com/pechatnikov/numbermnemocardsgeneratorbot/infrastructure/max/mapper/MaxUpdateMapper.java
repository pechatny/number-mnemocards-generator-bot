package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.GetOrCreateUserCommand;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Callback;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Message;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.command.CommandContext;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration.ConditionalOnMaxBot;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxCallback;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxMessage;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxUser;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.dto.CallbackData;
import org.springframework.stereotype.Component;

/**
 * Перевод объектов Max API во входные данные application-слоя.
 */
@ConditionalOnMaxBot
@Component
public class MaxUpdateMapper {
    private final ObjectMapper objectMapper;

    public MaxUpdateMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public GetOrCreateUserCommand toGetOrCreateUserCommand(MaxUser user) {
        return GetOrCreateUserCommand.builder()
            .setPlatform(BotPlatform.MAX)
            .setExternalId(user.getUserId())
            .setFirstname(user.getFirstName())
            .setLastname(user.getLastName())
            .setUsername(user.getUsername())
            .build();
    }

    public Message toMessage(MaxMessage message) {
        return Message.builder()
            .message(message.getBody().getText())
            .chatId(message.getRecipient().getChatId())
            .build();
    }

    public CommandContext toCommandContext(Long chatId, MaxUser user) {
        return CommandContext.builder()
            .platform(BotPlatform.MAX)
            .externalUserId(user.getUserId())
            .chatId(chatId)
            .build();
    }

    /**
     * @param message сообщение с нажатой кнопкой; должно быть не {@code null}
     */
    public Callback toCallback(MaxCallback callback, MaxMessage message) throws JsonProcessingException {
        CallbackData callbackData = objectMapper.readValue(callback.getPayload(), CallbackData.class);

        return Callback.builder()
            .type(callbackData.getType())
            .value(callbackData.getValue())
            .platform(BotPlatform.MAX)
            .externalUserId(callback.getUser().getUserId())
            .chatId(message.getRecipient().getChatId())
            .messageId(message.getBody().getMid())
            .callbackQueryId(callback.getCallbackId())
            .build();
    }
}
