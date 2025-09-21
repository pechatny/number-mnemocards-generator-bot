package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.mapper;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.GetOrCreateUserCommand;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Message;
import org.mapstruct.Mapping;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface TelegramUpdateMapper {
    GetOrCreateUserCommand toGetOrCreateUserCommand(Update update);

    Message toMessage(Update update, com.pechatnikov.numbermnemocardsgeneratorbot.domain.User user);
}
