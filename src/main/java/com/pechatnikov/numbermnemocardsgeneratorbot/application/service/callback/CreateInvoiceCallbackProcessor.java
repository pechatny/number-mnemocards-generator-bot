package com.pechatnikov.numbermnemocardsgeneratorbot.application.service.callback;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.SaveInvoiceMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.UserService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.DeleteMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.InvoiceService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.PaymentService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Callback;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Invoice;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.InvoiceMessage;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Money;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.user.User;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.TelegramApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Currency;

@Slf4j
@Component
public class CreateInvoiceCallbackProcessor implements CallbackProcessor {
    private final TelegramApiClient telegramApiClient;
    private final PaymentService paymentService;
    private final UserService userService;
    private final InvoiceService invoiceService;
    private final SaveInvoiceMessageService saveInvoiceMessageService;
    private final DeleteMessageService deleteMessageService;

    public CreateInvoiceCallbackProcessor(
        TelegramApiClient telegramApiClient,
        PaymentService paymentService,
        UserService userService,
        InvoiceService invoiceService,
        SaveInvoiceMessageService saveInvoiceMessageService,
        DeleteMessageService deleteMessageService
    ) {
        this.telegramApiClient = telegramApiClient;
        this.paymentService = paymentService;
        this.userService = userService;
        this.invoiceService = invoiceService;
        this.saveInvoiceMessageService = saveInvoiceMessageService;
        this.deleteMessageService = deleteMessageService;
    }

    @Override
    public CallbackType getCallBacktype() {
        return CallbackType.CREATE_INVOICE;
    }

    @Override
    public void process(Callback callback) {
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callback.getCallbackQueryId());
        answer.setText("Запуск процесса оплаты...");
        try {
            telegramApiClient.execute(answer);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

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
