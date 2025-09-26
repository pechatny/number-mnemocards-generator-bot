package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.order.Order;

public interface OrderRepositoryPort {
    Order save(Order order);
}
