package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.mapper;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.TokenBalance;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.entity.TokenBalanceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserPersistenceMapper.class})
public interface TokenBalancePersistenceMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "user", source = "user")
    TokenBalanceEntity toEntity(TokenBalance domain);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "user", source = "user")
    TokenBalance toDomain(TokenBalanceEntity entity);

}
