package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.mapper;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.order.Order;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.entity.OrderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = UserPersistenceMapper.class)
public interface OrderPersistenceMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "tokenAmount", source = "tokenAmount")
    @Mapping(target = "status", source = "status")
    OrderEntity toEntity(Order domain);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "tokenAmount", source = "tokenAmount")
    @Mapping(target = "status", source = "status")
    Order toDomain(OrderEntity entity);

}
