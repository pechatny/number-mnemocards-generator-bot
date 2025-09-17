package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.jpa;

import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SpringDataUserRepository extends CrudRepository<UserEntity, Long> {
    Optional<UserEntity> findByTelegramId(Long telegramId);
}
