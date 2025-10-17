package com.pechatnikov.numbermnemocardsgeneratorbot.domain.order;

public enum OrderStatus {
    COMPLETED(null),
    IN_PROGRESS(COMPLETED),
    NEW(IN_PROGRESS),
    CANCELED(null);

    private final OrderStatus next;

    OrderStatus(OrderStatus next) {
        this.next = next;
    }

    public OrderStatus getNext() {
        return next;
    }
}
