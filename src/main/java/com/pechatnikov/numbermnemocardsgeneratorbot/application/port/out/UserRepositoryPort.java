package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.user.User;

import java.util.Optional;

public interface UserRepositoryPort {
    Optional<User> findByTelegramId(Long telegramId);
    User save(User user);
}
