package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.user.User;

import java.util.Optional;

public interface UserService {
    User getOrCreateUser(GetOrCreateUserCommand getOrCreateUserCommand);
    Optional<User> findByPlatformAndExternalId(BotPlatform platform, Long externalId);
}
