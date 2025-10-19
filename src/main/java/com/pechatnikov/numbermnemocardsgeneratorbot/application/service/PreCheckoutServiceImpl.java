package com.pechatnikov.numbermnemocardsgeneratorbot.application.service;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.PreCheckoutService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendPreCheckoutService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.PreCheckout;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.order.Order;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.order.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
                validatePaymentAmount(order, preCheckout);

                preCheckoutService.send(preCheckout.getId(), true);
                updateOrderStatus(order);
            },
            () -> preCheckoutService.send(preCheckout.getId(), false)
        );
    }

    // TODO сделать валидацию суммы
    private void validatePaymentAmount(Order order, PreCheckout preCheckout) {
//        if (order.getPaymentAmount().equals(preCheckout))

    }

    private void updateOrderStatus(Order order) {
        try {
            orderService.updateStatus(order, OrderStatus.IN_PROGRESS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
