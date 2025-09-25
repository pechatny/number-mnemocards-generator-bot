package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.adapter;

import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.mapper.UserPersistenceMapper;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.UserRepositoryPort;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.user.User;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.jpa.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UserRepositoryAdapter implements UserRepositoryPort {
    private final SpringDataUserRepository springDataUserRepository;
    private final UserPersistenceMapper userPersistenceMapper;

    @Override
    public Optional<User> findByTelegramId(Long telegramId) {
        return springDataUserRepository.findByTelegramId(telegramId)
            .map(userPersistenceMapper::toDomain);
    }

    @Override
    public User save(User user) {
        var entity = userPersistenceMapper.toEntity(user);
        var savedEntity = springDataUserRepository.save(entity);
        return userPersistenceMapper.toDomain(savedEntity);
    }

}
