package com.pechatnikov.numbermnemocardsgeneratorbot.application.service;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.PreCheckoutService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendPreCheckoutService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.PreCheckout;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.order.Order;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.order.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class PreCheckoutServiceImpl implements PreCheckoutService {
    private final SendPreCheckoutService preCheckoutService;
    private final OrderService orderService;

    public PreCheckoutServiceImpl(SendPreCheckoutService preCheckoutService, OrderService orderService) {
        this.preCheckoutService = preCheckoutService;
        this.orderService = orderService;
    }

    @Override
    public void handle(PreCheckout preCheckout) {
        log.info("Пришел PreCheckout {}", preCheckout);

        orderService.findById(preCheckout.getOrderId()).ifPresentOrElse(
            order -> {
                if (isValidOrder(order, preCheckout)) {
                    preCheckoutService.send(preCheckout.getId(), true);
                    updateOrderStatus(order, OrderStatus.IN_PROGRESS);
                } else {
                    preCheckoutService.send(preCheckout.getId(), false);
                }

            },
            () -> preCheckoutService.send(preCheckout.getId(), false)
        );
    }

    private boolean isValidOrder(Order order, PreCheckout preCheckout) {
        List<OrderStatus> validStatuses = List.of(OrderStatus.NEW, OrderStatus.IN_PROGRESS);
        log.debug("Order validation started; order: {}; preCheckout: {}", order, preCheckout);

        return order.getPaymentAmount().equals(preCheckout.getPaymentAmount())
            && validStatuses.contains(order.getStatus());
    }

    private void updateOrderStatus(Order order, OrderStatus orderStatus) {
        try {
            orderService.updateStatus(order, orderStatus);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
