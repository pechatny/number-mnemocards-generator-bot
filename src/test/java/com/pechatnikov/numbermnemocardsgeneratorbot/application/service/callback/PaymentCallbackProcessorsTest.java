package com.pechatnikov.numbermnemocardsgeneratorbot.application.service.callback;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.SaveInvoiceMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.UserService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.DeleteMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.PaymentAvailabilityPort;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendButtonsMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendCallbackAnswerService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendInvoiceService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.InvoiceService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.ButtonsMessage;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Callback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PaymentCallbackProcessorsTest {
    private DeleteMessageService deleteMessageService;
    private SendCallbackAnswerService sendCallbackAnswerService;
    private SendButtonsMessageService sendButtonsMessageService;
    private PaymentAvailabilityPort paymentAvailabilityPort;
    private UserService userService;
    private InvoiceService invoiceService;
    private SaveInvoiceMessageService saveInvoiceMessageService;
    private SendInvoiceService sendInvoiceService;

    private ShowPricesCallbackProcessor showPricesProcessor;
    private CreateInvoiceCallbackProcessor createInvoiceProcessor;

    @BeforeEach
    void setUp() {
        deleteMessageService = mock(DeleteMessageService.class);
        sendCallbackAnswerService = mock(SendCallbackAnswerService.class);
        sendButtonsMessageService = mock(SendButtonsMessageService.class);
        paymentAvailabilityPort = mock(PaymentAvailabilityPort.class);
        userService = mock(UserService.class);
        invoiceService = mock(InvoiceService.class);
        saveInvoiceMessageService = mock(SaveInvoiceMessageService.class);
        sendInvoiceService = mock(SendInvoiceService.class);

        showPricesProcessor = new ShowPricesCallbackProcessor(
            new ObjectMapper(), deleteMessageService, sendCallbackAnswerService, sendButtonsMessageService, paymentAvailabilityPort
        );
        createInvoiceProcessor = new CreateInvoiceCallbackProcessor(
            userService, invoiceService, saveInvoiceMessageService, deleteMessageService,
            sendCallbackAnswerService, sendInvoiceService, paymentAvailabilityPort
        );
    }

    @Test
    void shouldShowPricesOnPlatformWithPayments() {
        when(paymentAvailabilityPort.isAvailable(BotPlatform.TELEGRAM)).thenReturn(true);

        showPricesProcessor.process(callback(BotPlatform.TELEGRAM, CallbackType.SHOW_PRICES, null));

        ArgumentCaptor<ButtonsMessage> captor = ArgumentCaptor.forClass(ButtonsMessage.class);
        verify(sendButtonsMessageService).sendButtonsMessage(captor.capture());
        assertThat(captor.getValue().getPlatform()).isEqualTo(BotPlatform.TELEGRAM);
        assertThat(captor.getValue().getButtons()).hasSize(3);
        verify(deleteMessageService).delete(BotPlatform.TELEGRAM, 42L, "55");
    }

    @Test
    void shouldNotShowPricesOnPlatformWithoutPayments() {
        when(paymentAvailabilityPort.isAvailable(BotPlatform.MAX)).thenReturn(false);

        showPricesProcessor.process(callback(BotPlatform.MAX, CallbackType.SHOW_PRICES, null));

        verify(sendCallbackAnswerService).sendCallbackAnswer(BotPlatform.MAX, "cb-1", PaymentAvailabilityPort.PAYMENTS_UNAVAILABLE_MESSAGE);
        verifyNoInteractions(sendButtonsMessageService, deleteMessageService);
    }

    @Test
    void shouldNotCreateOrderOrInvoiceOnPlatformWithoutPayments() {
        when(paymentAvailabilityPort.isAvailable(BotPlatform.MAX)).thenReturn(false);

        createInvoiceProcessor.process(callback(BotPlatform.MAX, CallbackType.CREATE_INVOICE, "100"));

        verify(sendCallbackAnswerService).sendCallbackAnswer(BotPlatform.MAX, "cb-1", PaymentAvailabilityPort.PAYMENTS_UNAVAILABLE_MESSAGE);
        verify(sendCallbackAnswerService, never()).sendCallbackAnswer(eq(BotPlatform.MAX), eq("cb-1"), eq("Создается счёт на оплату..."));
        verifyNoInteractions(userService, invoiceService, sendInvoiceService, saveInvoiceMessageService, deleteMessageService);
    }

    @Test
    void shouldStartInvoiceFlowOnPlatformWithPayments() {
        when(paymentAvailabilityPort.isAvailable(BotPlatform.TELEGRAM)).thenReturn(true);
        // Пользователь не найден: проверяем только, что поток оплаты начался, дальше он в этом тесте не нужен
        when(userService.findByPlatformAndExternalId(BotPlatform.TELEGRAM, 7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createInvoiceProcessor.process(callback(BotPlatform.TELEGRAM, CallbackType.CREATE_INVOICE, "100")))
            .isInstanceOf(NoSuchElementException.class);

        verify(sendCallbackAnswerService).sendCallbackAnswer(BotPlatform.TELEGRAM, "cb-1", "Создается счёт на оплату...");
        verify(userService).findByPlatformAndExternalId(BotPlatform.TELEGRAM, 7L);
    }

    private static Callback callback(BotPlatform platform, CallbackType type, String value) {
        return Callback.builder()
            .type(type)
            .value(value)
            .platform(platform)
            .externalUserId(7L)
            .chatId(42L)
            .messageId("55")
            .callbackQueryId("cb-1")
            .build();
    }
}
