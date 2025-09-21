package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.mapper;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.GetOrCreateUserCommand;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Slf4j
@Component
public class TelegramUpdateMapperImpl implements TelegramUpdateMapper {

    @Override
    public GetOrCreateUserCommand toGetOrCreateUserCommand(Update update) {
        User telegramUser = extractTelegramUser(update);

        return GetOrCreateUserCommand.builder()
            .setTelegramId(telegramUser.getId())
            .setFirstname(telegramUser.getFirstName())
            .setLastname(telegramUser.getLastName())
            .setUsername(telegramUser.getUserName())
            .build();
    }

    @Override
    public Message toMessage(Update update, com.pechatnikov.numbermnemocardsgeneratorbot.domain.User user) {
        Message.MessageBuilder messageBuilder = Message.builder();
        if (update.hasMessage() && update.getMessage().hasText()) {
            messageBuilder.message(update.getMessage().getText());
        }

        if (update.hasMessage()) {
            messageBuilder.chatId(update.getMessage().getChatId());
        }

        messageBuilder.user(user);

        return messageBuilder.build();
    }

    private User extractTelegramUser(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().getFrom() != null) {
                return update.getMessage().getFrom();
            } else if (update.hasCallbackQuery() && update.getCallbackQuery().getFrom() != null) {
                return update.getCallbackQuery().getFrom();
            } else if (update.hasEditedMessage() && update.getEditedMessage().getFrom() != null) {
                return update.getEditedMessage().getFrom();
            } else if (update.hasChannelPost() && update.getChannelPost().getFrom() != null) {
                return update.getChannelPost().getFrom();
            } else if (update.hasMyChatMember() && update.getMyChatMember().getFrom() != null) {
                return update.getMyChatMember().getFrom();
            } else if (update.hasChatMember() && update.getChatMember().getFrom() != null) {
                return update.getChatMember().getFrom();
            }
        } catch (Exception e) {
            log.warn("Failed to extract user from update: {}", update, e);
        }

        return null;
    }
}
