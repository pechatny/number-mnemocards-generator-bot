package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.adapter;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.OrderRepositoryPort;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.order.Order;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.jpa.SpringDataOrderRepository;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.mapper.OrderPersistenceMapper;
import org.springframework.stereotype.Component;

@Component
public class OrderRepositoryAdapter implements OrderRepositoryPort {
    private final SpringDataOrderRepository springDataOrderRepository;
    private final OrderPersistenceMapper messagePersistenceMapper;

    public OrderRepositoryAdapter(SpringDataOrderRepository springDataOrderRepository, OrderPersistenceMapper messagePersistenceMapper) {
        this.springDataOrderRepository = springDataOrderRepository;
        this.messagePersistenceMapper = messagePersistenceMapper;
    }

    @Override
    public Order save(Order order) {
        var entity = springDataOrderRepository.save(
            messagePersistenceMapper.toEntity(order)
        );

        return messagePersistenceMapper.toDomain(entity);
    }
}
