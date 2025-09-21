package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.mapper;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Message;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.entity.MessageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = UserPersistenceMapper.class)
public interface MessagePersistenceMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "message", source = "message")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "chatId", source = "chatId")
    MessageEntity toEntity(Message message);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "message", source = "message")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "chatId", source = "chatId")
    Message toDomain(MessageEntity messageEntity);

}
