package com.pechatnikov.numbermnemocardsgeneratorbot.application.service;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Invoice;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Money;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.order.Order;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.user.User;
import org.springframework.stereotype.Service;

@Service
public class InvoiceServiceImpl implements InvoiceService{
    private final static Long TOKEN_RATE = 2L;
    private final OrderService orderService;

    public InvoiceServiceImpl(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public Invoice create(User user, String chatId, String title, String description, Money price) {
        Long tokenAmount = price.getAmount().longValue() * TOKEN_RATE;
        Order order = orderService.create(user, tokenAmount);

        return Invoice.builder()
            .chatId(chatId)
            .title(title)
            .description(description)
            .payload(new Invoice.Payload(order.getId()))
            .price(price)
            .build();
    }
}
