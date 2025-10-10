package com.pechatnikov.numbermnemocardsgeneratorbot.application.service;

import com.pechatnikov.numbermnemocardsgeneratorbot.BaseIntegrationTest;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.GetOrCreateUserCommand;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.UserService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.order.Order;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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

        Order order = orderService.create(user, tokenAmount);

        assertEquals(tokenAmount, order.getTokenAmount());
    }

    @Test
    void findById() {
    }
}