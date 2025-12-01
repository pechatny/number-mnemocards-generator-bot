package com.pechatnikov.numbermnemocardsgeneratorbot.application.service.callback;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.SaveInvoiceMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.UserService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.DeleteMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendCallbackAnswerService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.InvoiceService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.PaymentService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Callback;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Invoice;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.InvoiceMessage;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Money;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Slf4j
@Component
public class CreateInvoiceCallbackProcessor implements CallbackProcessor {
    private final PaymentService paymentService;
    private final UserService userService;
    private final InvoiceService invoiceService;
    private final SaveInvoiceMessageService saveInvoiceMessageService;
    private final DeleteMessageService deleteMessageService;
    private final SendCallbackAnswerService sendCallbackAnswerService;

    public CreateInvoiceCallbackProcessor(
        PaymentService paymentService,
        UserService userService,
        InvoiceService invoiceService,
        SaveInvoiceMessageService saveInvoiceMessageService,
        DeleteMessageService deleteMessageService, SendCallbackAnswerService sendCallbackAnswerService
    ) {
        this.paymentService = paymentService;
        this.userService = userService;
        this.invoiceService = invoiceService;
        this.saveInvoiceMessageService = saveInvoiceMessageService;
        this.deleteMessageService = deleteMessageService;
        this.sendCallbackAnswerService = sendCallbackAnswerService;
    }

    @Override
    public CallbackType getCallBacktype() {
        return CallbackType.CREATE_INVOICE;
    }

    @Override
    public void process(Callback callback) {
        sendCallbackAnswerService.sendCallbackAnswer(callback.getCallbackQueryId(),  "Создается счёт на оплату...");

        User user = userService.findByTelegramId(callback.getTelegramId()).orElseThrow();

        Money price = Money.of(callback.getValue(), Currency.getInstance("RUB"));

        int tokenCount = convertMoneyToTokenCount(price);
        Invoice invoice = invoiceService.create(
            user,
            callback.getChatId().toString(),
            "Пополнение баланса",
            "Пополнение баланса цифрами для преобразования в мнемокарточки в количестве: " + tokenCount + " шт.",
            price
        );

        log.debug("Удаляю сообщение callback. chatId={};messageId={}", callback.getChatId(), callback.getMessageId());
        deleteMessageService.delete(callback.getChatId(), callback.getMessageId());

        var telegramInvoiceMessage = paymentService.sendInvoice(invoice);

        var invoiceMessage = InvoiceMessage.builder()
            .chatId(telegramInvoiceMessage.getChatId())
            .messageId(telegramInvoiceMessage.getMessageId())
            .build();

        saveInvoiceMessageService.saveInvoiceMessage(invoice.getPayload().getOrderId(), invoiceMessage);
    }

    private int convertMoneyToTokenCount(Money money) {
        final int TOKEN_RATE = 2;

        return money.getIntegerPart().intValue() * TOKEN_RATE;
    }
}
