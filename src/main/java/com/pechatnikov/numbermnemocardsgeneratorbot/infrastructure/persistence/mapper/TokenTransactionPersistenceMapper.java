package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.mapper;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.TokenTransaction;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.entity.TokenTransactionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserPersistenceMapper.class, MessagePersistenceMapper.class})
public interface TokenTransactionPersistenceMapper {

    TokenTransactionEntity toEntity(TokenTransaction domain);

    TokenTransaction toDomain(TokenTransactionEntity entity);

}
