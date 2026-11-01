package com.pechatnikov.numbermnemocardsgeneratorbot.application.service;


import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.ShowPricesButtonUseCase;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.PaymentAvailabilityPort;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendButtonMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.callback.CallbackType;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.ButtonMessage;
import org.springframework.stereotype.Service;

@Service
public class ShowPricesButtonUseCaseImpl implements ShowPricesButtonUseCase {
    public static final String BUTTON_TEXT = "💳 Показать цены";
    public static final String MESSAGE_TEXT = "Нажмите кнопку ниже для выбора вариантов оплаты:";

    private final SendButtonMessageService sendButtonMessageService;
    private final SendMessageService sendMessageService;
    private final PaymentAvailabilityPort paymentAvailabilityPort;

    public ShowPricesButtonUseCaseImpl(
        SendButtonMessageService sendButtonMessageService,
        SendMessageService sendMessageService,
        PaymentAvailabilityPort paymentAvailabilityPort
    ) {
        this.sendButtonMessageService = sendButtonMessageService;
        this.sendMessageService = sendMessageService;
        this.paymentAvailabilityPort = paymentAvailabilityPort;
    }

    @Override
    public void showPricesButton(BotPlatform platform, Long chatId) {
        if (!paymentAvailabilityPort.isAvailable(platform)) {
            sendMessageService.sendMessage(platform, chatId, PaymentAvailabilityPort.PAYMENTS_UNAVAILABLE_MESSAGE);
            return;
        }

        ButtonMessage message = ButtonMessage.builder()
            .platform(platform)
            .chatId(chatId)
            .buttonText(BUTTON_TEXT)
            .callbackType(CallbackType.SHOW_PRICES)
            .message(MESSAGE_TEXT)
            .build();

        sendButtonMessageService.sendButtonMessage(message);
    }
}
