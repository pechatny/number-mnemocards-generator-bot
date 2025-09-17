package com.pechatnikov.numbermnemocardsgeneratorbot.application.service;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.mapper.GetOrCreateUserCommandMapper;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.GetOrCreateUserCommand;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.UserService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.UserRepositoryPort;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.User;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepositoryPort userRepositoryPort;
    private final GetOrCreateUserCommandMapper getOrCreateUserCommandMapper;

    public UserServiceImpl(UserRepositoryPort userRepositoryPort, GetOrCreateUserCommandMapper getOrCreateUserCommandMapper) {
        this.userRepositoryPort = userRepositoryPort;
        this.getOrCreateUserCommandMapper = getOrCreateUserCommandMapper;
    }

    private User create(GetOrCreateUserCommand getOrCreateUserCommand) {
        return userRepositoryPort.save(getOrCreateUserCommandMapper.toDomain(getOrCreateUserCommand));
    }

    @Override
    public User getOrCreateUser(GetOrCreateUserCommand getOrCreateUserCommand) {
        return userRepositoryPort.findByTelegramId(getOrCreateUserCommand.getTelegramId()).orElseGet(() ->
            create(getOrCreateUserCommand)
        );
    }
}
