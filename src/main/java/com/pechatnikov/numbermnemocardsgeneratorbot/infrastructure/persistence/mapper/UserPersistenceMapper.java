package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.mapper;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.user.User;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.entity.UserEntity;

public interface UserPersistenceMapper {
    User toDomain(UserEntity entity);
    UserEntity toEntity(User domain);
}
