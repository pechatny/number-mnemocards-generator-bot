package com.pechatnikov.numbermnemocardsgeneratorbot.application.mapper;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.GetOrCreateUserCommand;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.User;
import org.springframework.stereotype.Component;

import javax.validation.ValidationException;

@Component
public class GetOrGetOrCreateUserCommandMapperImpl implements GetOrCreateUserCommandMapper {

    @Override
    public User toDomain(GetOrCreateUserCommand command) {
        // Явный контроль и валидация
        if (command.getTelegramId() == null) {
            throw new ValidationException("Telegram ID required");
        }

        return User.builder()
            .setTelegramId(command.getTelegramId())
            .setFirstname(command.getFirstname())
            .setLastname(command.getLastname())
            .setUsername(command.getUsername())
            .build();

    }

}
