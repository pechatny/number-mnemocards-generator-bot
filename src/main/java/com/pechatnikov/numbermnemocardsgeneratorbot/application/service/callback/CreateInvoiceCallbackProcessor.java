package com.pechatnikov.numbermnemocardsgeneratorbot.application.service.callback;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.UserService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.InvoiceService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.PaymentService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Callback;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Invoice;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Money;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.user.User;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.TelegramApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.util.Currency;

@Slf4j
@Component
public class CreateInvoiceCallbackProcessor implements CallbackProcessor {
    private final TelegramApiClient telegramApiClient;
    private final PaymentService paymentService;
    private final UserService userService;
    private final InvoiceService invoiceService;
    private final ObjectMapper objectMapper;

    public CreateInvoiceCallbackProcessor(TelegramApiClient telegramApiClient, PaymentService paymentService, UserService userService, InvoiceService invoiceService, ObjectMapper objectMapper) {
        this.telegramApiClient = telegramApiClient;
        this.paymentService = paymentService;
        this.userService = userService;
        this.invoiceService = invoiceService;
        this.objectMapper = objectMapper;
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

        // Обновляем сообщение
        DeleteMessage deleteMessage = DeleteMessage.builder()
            .chatId(callback.getChatId().toString())
            .messageId(callback.getMessageId())
            .build();

        User user = userService.findByTelegramId(callback.getTelegramId()).orElseThrow();

        Money price = new Money(BigDecimal.valueOf(
            Long.valueOf(callback.getValue())),
            Currency.getInstance("RUB")
        );

        Invoice invoice = invoiceService.create(
            user,
            callback.getChatId().toString(),
            "Покупка цифр для преобразования в мнемокарточки в количестве: " + callback.getValue() + " шт",
            "",
            price

        );

//        EditMessageText newMessage = new EditMessageText();
//        newMessage.setChatId(callback.getChatId().toString());
//        newMessage.setMessageId(callback.getMessageId());
//        newMessage.setText("💳 Введите сумму оплаты:");

        try {
            telegramApiClient.execute(deleteMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

        paymentService.sendInvoice(invoice);
    }
}
