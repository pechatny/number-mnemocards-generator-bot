package com.pechatnikov.numbermnemocardsgeneratorbot.application.mapper;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.GetOrCreateUserCommand;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.user.User;

public interface GetOrCreateUserCommandMapper {

    User toDomain(GetOrCreateUserCommand command);
}
