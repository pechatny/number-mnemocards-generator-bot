package com.pechatnikov.numbermnemocardsgeneratorbot.application.service;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.PaymentAvailabilityPort;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendButtonMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.callback.CallbackType;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.ButtonMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ShowPricesButtonUseCaseImplTest {
    private SendButtonMessageService sendButtonMessageService;
    private SendMessageService sendMessageService;
    private PaymentAvailabilityPort paymentAvailabilityPort;
    private ShowPricesButtonUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        sendButtonMessageService = mock(SendButtonMessageService.class);
        sendMessageService = mock(SendMessageService.class);
        paymentAvailabilityPort = mock(PaymentAvailabilityPort.class);
        useCase = new ShowPricesButtonUseCaseImpl(sendButtonMessageService, sendMessageService, paymentAvailabilityPort);
    }

    @Test
    void shouldShowPricesButtonWhenPaymentsAvailable() {
        when(paymentAvailabilityPort.isAvailable(BotPlatform.TELEGRAM)).thenReturn(true);

        useCase.showPricesButton(BotPlatform.TELEGRAM, 42L);

        ArgumentCaptor<ButtonMessage> captor = ArgumentCaptor.forClass(ButtonMessage.class);
        verify(sendButtonMessageService).sendButtonMessage(captor.capture());
        assertThat(captor.getValue().getPlatform()).isEqualTo(BotPlatform.TELEGRAM);
        assertThat(captor.getValue().getChatId()).isEqualTo(42L);
        assertThat(captor.getValue().getCallbackType()).isEqualTo(CallbackType.SHOW_PRICES);
        verifyNoInteractions(sendMessageService);
    }

    @Test
    void shouldSendUnavailableMessageWhenPaymentsUnavailable() {
        when(paymentAvailabilityPort.isAvailable(BotPlatform.MAX)).thenReturn(false);

        useCase.showPricesButton(BotPlatform.MAX, 42L);

        verify(sendMessageService).sendMessage(BotPlatform.MAX, 42L, PaymentAvailabilityPort.PAYMENTS_UNAVAILABLE_MESSAGE);
        verifyNoInteractions(sendButtonMessageService);
    }
}
