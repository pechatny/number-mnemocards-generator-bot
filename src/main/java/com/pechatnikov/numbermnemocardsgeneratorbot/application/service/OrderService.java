package com.pechatnikov.numbermnemocardsgeneratorbot.application.service;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.OrderRepositoryPort;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.order.Order;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.order.OrderStatus;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.user.User;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private final OrderRepositoryPort orderRepositoryPort;

    public OrderService(OrderRepositoryPort orderRepositoryPort) {
        this.orderRepositoryPort = orderRepositoryPort;
    }

    public Order create(User user, Long tokenAmount) {
        Order order = Order.builder()
            .status(OrderStatus.NEW)
            .tokenAmount(tokenAmount)
            .user(user)
            .build();

        return orderRepositoryPort.save(order);
    }
}
