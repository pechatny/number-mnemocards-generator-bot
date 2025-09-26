package com.pechatnikov.numbermnemocardsgeneratorbot.domain.order;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.user.User;

public class Order {
    private final Long id;
    private final User user;
    private final OrderStatus status;
    private final Long tokenAmount;

    Order(Long id, User user, OrderStatus status, Long tokenAmount) {
        this.id = id;
        this.user = user;
        this.status = status;
        this.tokenAmount = tokenAmount;
    }

    public static OrderBuilder builder() {
        return new OrderBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public User getUser() {
        return this.user;
    }

    public OrderStatus getStatus() {
        return this.status;
    }

    public Long getTokenAmount() {
        return this.tokenAmount;
    }

    public OrderBuilder toBuilder() {
        return new OrderBuilder().id(this.id).user(this.user).status(this.status).tokenAmount(this.tokenAmount);
    }

    public static class OrderBuilder {
        private Long id;
        private User user;
        private OrderStatus status;
        private Long tokenAmount;

        OrderBuilder() {
        }

        public OrderBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public OrderBuilder user(User user) {
            this.user = user;
            return this;
        }

        public OrderBuilder status(OrderStatus status) {
            this.status = status;
            return this;
        }

        public OrderBuilder tokenAmount(Long tokenAmount) {
            this.tokenAmount = tokenAmount;
            return this;
        }

        public Order build() {
            return new Order(this.id, this.user, this.status, this.tokenAmount);
        }

        public String toString() {
            return "Order.OrderBuilder(id=" + this.id + ", user=" + this.user + ", status=" + this.status + ", tokenAmount=" + this.tokenAmount + ")";
        }
    }
}
