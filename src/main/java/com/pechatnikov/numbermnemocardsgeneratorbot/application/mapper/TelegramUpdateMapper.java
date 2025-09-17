package com.pechatnikov.numbermnemocardsgeneratorbot.application.mapper;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.GetOrCreateUserCommand;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface TelegramUpdateMapper {
    GetOrCreateUserCommand toGetOrCreateUserCommand(Update update);
}
