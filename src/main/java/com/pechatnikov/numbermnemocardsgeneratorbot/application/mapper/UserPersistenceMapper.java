package com.pechatnikov.numbermnemocardsgeneratorbot.application.mapper;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.User;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.entity.UserEntity;

public interface UserPersistenceMapper {
    User toDomain(UserEntity entity);
    UserEntity toEntity(User domain);
}
