package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.jpa;

import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.repository.CrudRepository;

public interface SpringDataOrderRepository extends CrudRepository<OrderEntity, Long> {
}
