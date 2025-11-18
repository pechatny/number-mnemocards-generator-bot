package com.pechatnikov.numbermnemocardsgeneratorbot.application.service;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Invoice;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Money;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.ProviderData;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.order.Order;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
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
        Order order = orderService.create(user, tokenAmount, price);
        ProviderData providerData = getProviderData(description, price, tokenAmount);

        var invoice = Invoice.builder()
            .chatId(chatId)
            .title(title)
            .description(description)
            .payload(new Invoice.Payload(order.getId()))
            .price(price)
            .providerData(providerData)
            .build();

        log.debug("Данные для чеков у провайера providerData = {}", providerData.toString());

        return invoice;
    }

    private static ProviderData getProviderData(String description, Money price, Long tokenAmount) {
        return ProviderData.builder()
            .receipt(ProviderData.Receipt.builder()
                .items(List.of(
                    ProviderData.ReceiptItem.builder()
                        .description(description)
                        .quantity(1L)
                        .amount(ProviderData.Amount.builder()
                            .value(price.getAmount().longValue())
                            .currency(price.getCurrency().getCurrencyCode())
                            .build()
                        )
                        .vat_code(1)
                        .payment_mode("full_payment")
                        .payment_subject("commodity")
                        .build()
                ))
                .build())
            .tax_system_code(1)
            .build();
    }
}
