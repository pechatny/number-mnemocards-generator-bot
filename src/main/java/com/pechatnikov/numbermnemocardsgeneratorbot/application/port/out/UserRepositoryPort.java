package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.user.User;

import java.util.Optional;

public interface UserRepositoryPort {
    Optional<User> findByPlatformAndExternalId(BotPlatform platform, Long externalId);
    User save(User user);
}
