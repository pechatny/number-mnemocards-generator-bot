package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.mapper;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Money;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.entity.MoneyValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = UserPersistenceMapper.class)
public interface MoneyPersistenceMapper {

    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currencyCode", source = "currency.currencyCode")
    MoneyValue toEntity(Money domain);

    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currency", source = "currencyCode")
    @Mapping(target = "scale", ignore = true)
    Money toDomain(MoneyValue entity);

}
