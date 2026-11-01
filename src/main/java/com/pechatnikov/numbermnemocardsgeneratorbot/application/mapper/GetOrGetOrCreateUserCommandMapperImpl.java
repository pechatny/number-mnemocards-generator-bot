package com.pechatnikov.numbermnemocardsgeneratorbot.application.mapper;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.GetOrCreateUserCommand;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.user.User;
import org.springframework.stereotype.Component;

import javax.validation.ValidationException;

@Component
public class GetOrGetOrCreateUserCommandMapperImpl implements GetOrCreateUserCommandMapper {

    @Override
    public User toDomain(GetOrCreateUserCommand command) {
        // Явный контроль и валидация
        if (command.getPlatform() == null) {
            throw new ValidationException("Platform required");
        }
        if (command.getExternalId() == null) {
            throw new ValidationException("External ID required");
        }

        return User.builder()
            .setPlatform(command.getPlatform())
            .setExternalId(command.getExternalId())
            .setFirstname(command.getFirstname())
            .setLastname(command.getLastname())
            .setUsername(command.getUsername())
            .build();

    }

}
