package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.mapper;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.order.Order;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.entity.OrderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserPersistenceMapper.class, MoneyPersistenceMapper.class, InvoiceMessagePersistenceMapper.class})
public interface OrderPersistenceMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "tokenAmount", source = "tokenAmount")
    @Mapping(target = "paymentAmount", source = "paymentAmount")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "invoiceMessage", source = "invoiceMessage")
    OrderEntity toEntity(Order domain);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "tokenAmount", source = "tokenAmount")
    @Mapping(target = "paymentAmount", source = "paymentAmount")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "invoiceMessage", source = "invoiceMessage")
    Order toDomain(OrderEntity entity);

}
