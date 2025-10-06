package com.pechatnikov.numbermnemocardsgeneratorbot.application.service;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.PreCheckoutService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendPreCheckoutService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.PreCheckout;
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
            order -> preCheckoutService.send(preCheckout.getId(), true),
            () -> preCheckoutService.send(preCheckout.getId(), false)
        );
    }
}
