package com.pechatnikov.numbermnemocardsgeneratorbot.application.service;


import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.ShowPricesButtonUseCase;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendButtonMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.callback.CallbackType;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.ButtonMessage;
import org.springframework.stereotype.Service;

@Service
public class ShowPricesButtonUseCaseImpl implements ShowPricesButtonUseCase {
    public static final String BUTTON_TEXT = "💳 Показать цены";
    public static final String MESSAGE_TEXT = "Нажмите кнопку ниже для выбора вариантов оплаты:";

    private final SendButtonMessageService sendButtonMessageService;

    public ShowPricesButtonUseCaseImpl(SendButtonMessageService sendButtonMessageService) {
        this.sendButtonMessageService = sendButtonMessageService;
    }

    @Override
    public void showPricesButton(Long chatId) {
        ButtonMessage message = ButtonMessage.builder()
            .chatId(chatId)
            .buttonText(BUTTON_TEXT)
            .callbackType(CallbackType.SHOW_PRICES)
            .message(MESSAGE_TEXT)
            .build();

        sendButtonMessageService.sendButtonMessage(message);
    }
}
