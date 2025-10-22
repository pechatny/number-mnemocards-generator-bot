package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.mapper;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.InvoiceMessage;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.entity.InvoiceMessageValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InvoiceMessagePersistenceMapper {
    @Mapping(target = "chatId", source = "chatId")
    @Mapping(target = "messageId", source = "messageId")
    InvoiceMessageValue toEntity(InvoiceMessage domain);

    @Mapping(target = "chatId", source = "chatId")
    @Mapping(target = "messageId", source = "messageId")
    InvoiceMessage toDomain(InvoiceMessageValue entity);
}
