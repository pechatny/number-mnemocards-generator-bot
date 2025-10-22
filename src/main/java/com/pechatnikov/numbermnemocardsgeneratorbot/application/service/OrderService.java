package com.pechatnikov.numbermnemocardsgeneratorbot.application.service;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.SaveInvoiceMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.OrderRepositoryPort;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.InvoiceMessage;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Money;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.order.Order;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.order.OrderStatus;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class OrderService implements SaveInvoiceMessageService {
    private final OrderRepositoryPort orderRepositoryPort;

    public OrderService(OrderRepositoryPort orderRepositoryPort) {
        this.orderRepositoryPort = orderRepositoryPort;
    }

    public Order create(User user, Long tokenAmount, Money price) {
        Order order = Order.builder()
            .status(OrderStatus.NEW)
            .tokenAmount(tokenAmount)
            .paymentAmount(price)
            .user(user)
            .build();

        return orderRepositoryPort.save(order);
    }

    public Order updateStatus(Order order, OrderStatus nextStatus) throws Exception {
        checkStatus(order, nextStatus);
        Order updatedOrder = order.toBuilder()
            .status(nextStatus)
            .build();

        return orderRepositoryPort.save(updatedOrder);
    }

    private void checkStatus(Order order, OrderStatus nextStatus) throws Exception {
        if (order.getStatus().equals(nextStatus)) {
            log.debug("Order status {} already set", nextStatus);
            return;
        }

        if (order.getStatus().getNext() != nextStatus) {
            throw new Exception("Order Status not supported");
        }

        if (order.getStatus().getNext() == null) {
            throw new Exception("Order Status already final, can't change status");
        }
    }

    public Optional<Order> findById(Long orderId) {
        return orderRepositoryPort.findById(orderId);
    }

    @Override
    public void saveInvoiceMessage(Long orderId, InvoiceMessage message) {
        Order order = orderRepositoryPort.findById(orderId).orElseThrow();
        Order updatedOrder = order.toBuilder()
            .invoiceMessage(message)
            .build();

        orderRepositoryPort.save(updatedOrder);
    }
}
