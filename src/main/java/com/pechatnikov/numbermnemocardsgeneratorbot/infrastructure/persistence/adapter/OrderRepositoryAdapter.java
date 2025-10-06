package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.adapter;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.OrderRepositoryPort;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.order.Order;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.jpa.SpringDataOrderRepository;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.mapper.OrderPersistenceMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderRepositoryAdapter implements OrderRepositoryPort {
    private final SpringDataOrderRepository springDataOrderRepository;
    private final OrderPersistenceMapper orderPersistenceMapper;

    public OrderRepositoryAdapter(SpringDataOrderRepository springDataOrderRepository, OrderPersistenceMapper messagePersistenceMapper) {
        this.springDataOrderRepository = springDataOrderRepository;
        this.orderPersistenceMapper = messagePersistenceMapper;
    }

    @Override
    public Order save(Order order) {
        var entity = springDataOrderRepository.save(
            orderPersistenceMapper.toEntity(order)
        );

        return orderPersistenceMapper.toDomain(entity);
    }

    @Override
    public Optional<Order> findById(Long orderId) {
        return springDataOrderRepository.findById(orderId).map(orderPersistenceMapper::toDomain);
    }
}
