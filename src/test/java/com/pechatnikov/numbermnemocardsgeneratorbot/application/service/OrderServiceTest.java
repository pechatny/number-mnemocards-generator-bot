package com.pechatnikov.numbermnemocardsgeneratorbot.application.service;

import com.pechatnikov.numbermnemocardsgeneratorbot.BaseIntegrationTest;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.GetOrCreateUserCommand;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.UserService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Money;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.order.Order;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.order.OrderStatus;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.user.User;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest extends BaseIntegrationTest {

    @Autowired
    OrderService orderService;

    @Autowired
    UserService userService;

    @Test
    void create() {
        GetOrCreateUserCommand getOrCreateUserCommand = GetOrCreateUserCommand.builder()
            .setUsername("username")
            .setFirstname("firstname")
            .setTelegramId(1L)
            .setLastname("lastname")
            .build();

        User user = userService.getOrCreateUser(getOrCreateUserCommand);
        Long tokenAmount = 100L;
        Money price = new Money(200L, Currency.getInstance("RUB"));

        Order order = orderService.create(user, tokenAmount, price);

        assertEquals(tokenAmount, order.getTokenAmount());
        assertEquals(OrderStatus.NEW, order.getStatus());
        assertEquals(price, order.getPaymentAmount());
    }

    @SneakyThrows
    @Test
    void moveToInProgressStatusSuccessfully() {
        GetOrCreateUserCommand getOrCreateUserCommand = GetOrCreateUserCommand.builder()
            .setUsername("username")
            .setFirstname("firstname")
            .setTelegramId(1L)
            .setLastname("lastname")
            .build();

        User user = userService.getOrCreateUser(getOrCreateUserCommand);
        Long tokenAmount = 100L;
        Money price = new Money(200L, Currency.getInstance("RUB"));

        Order order = orderService.create(user, tokenAmount, price);
        Order inProgressOrder = orderService.updateStatus(order, OrderStatus.IN_PROGRESS);

        assertEquals(tokenAmount, order.getTokenAmount());
        assertEquals(OrderStatus.IN_PROGRESS, inProgressOrder.getStatus());
        assertEquals(price, order.getPaymentAmount());
    }

    @Test
    void findById() {
        GetOrCreateUserCommand getOrCreateUserCommand = GetOrCreateUserCommand.builder()
            .setUsername("username")
            .setFirstname("firstname")
            .setTelegramId(1L)
            .setLastname("lastname")
            .build();

        User user = userService.getOrCreateUser(getOrCreateUserCommand);
        Long tokenAmount = 100L;
        Money price = new Money(200L, Currency.getInstance("RUB"));
        Order order = orderService.create(user, tokenAmount, price);

        Order foundOrder = orderService.findById(order.getId()).get();
        assertEquals(order.getId(), foundOrder.getId());
        assertEquals(order.getTokenAmount(), foundOrder.getTokenAmount());
        assertEquals(order.getStatus(), foundOrder.getStatus());
        assertEquals(price, order.getPaymentAmount());
    }
}