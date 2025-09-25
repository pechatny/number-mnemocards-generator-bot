package com.pechatnikov.numbermnemocardsgeneratorbot.domain.order;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.user.User;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.entity.OrderEntity;

/**
 * DTO for {@link OrderEntity}
 */
public class Order {
    private final Long id;
    private final User user;
    private final OrderStatus status;
    private final Long tokenAmount;

    public Order(Long id, User user, OrderStatus status, Long tokenAmount) {
        this.id = id;
        this.user = user;
        this.status = status;
        this.tokenAmount = tokenAmount;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Long getTokenAmount() {
        return tokenAmount;
    }
}
