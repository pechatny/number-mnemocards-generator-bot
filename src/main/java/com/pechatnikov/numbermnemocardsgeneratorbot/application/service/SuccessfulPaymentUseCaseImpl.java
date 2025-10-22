package com.pechatnikov.numbermnemocardsgeneratorbot.application.service;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.SuccessfulPaymentUseCase;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.TokenBalanceService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.TokenTransactionService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.DeleteMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.SuccessfulPayment;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.TokenTransaction;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.order.Order;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.order.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class SuccessfulPaymentUseCaseImpl implements SuccessfulPaymentUseCase {
    private final OrderService orderService;
    private final TokenBalanceService tokenBalanceService;
    private final TokenTransactionService tokenTransactionService;
    private final DeleteMessageService deleteMessageService;

    public SuccessfulPaymentUseCaseImpl(OrderService orderService, TokenBalanceService tokenBalanceService, TokenTransactionService tokenTransactionService, DeleteMessageService deleteMessageService) {
        this.orderService = orderService;
        this.tokenBalanceService = tokenBalanceService;
        this.tokenTransactionService = tokenTransactionService;
        this.deleteMessageService = deleteMessageService;
    }

    @Override
    @Transactional
    public void handle(SuccessfulPayment successfulPayment) {
        Long orderId = successfulPayment.getInvoicePayload().getOrderId();
        Order order = orderService.findById(orderId).orElseThrow();

        log.info("Записать транзакцию начисления токенов");
        var tokenTransaction = TokenTransaction.builder()
            .user(order.getUser())
            .count(order.getTokenAmount())
            .build();

        tokenTransaction = tokenTransactionService.save(tokenTransaction);
        tokenBalanceService.increase(tokenTransaction);

        try {
            orderService.updateStatus(order, OrderStatus.COMPLETED);

            log.debug("Удаляю сообщение с кнопкой на оплату");
            deleteMessageService.delete(order.getInvoiceMessage().getChatId(), order.getInvoiceMessage().getMessageId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
