package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.mapper;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.User;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserPersistenceMapperImpl extends UserPersistenceMapper {
    @Override
    @Mapping(source = "entity", target = ".", qualifiedByName = "mapEntityToDomain")
    User toDomain(UserEntity entity);

    @Override
    @Mapping(source = "domain", target = ".", qualifiedByName = "mapDomainToEntity")
    UserEntity toEntity(User domain);
}
