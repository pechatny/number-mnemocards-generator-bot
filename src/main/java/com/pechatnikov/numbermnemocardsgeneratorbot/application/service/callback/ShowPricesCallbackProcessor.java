package com.pechatnikov.numbermnemocardsgeneratorbot.application.service.callback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.DeleteMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendButtonsMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendCallbackAnswerService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.ButtonData;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.ButtonsMessage;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Callback;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.dto.CallbackData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class ShowPricesCallbackProcessor implements CallbackProcessor {
    private final ObjectMapper objectMapper;
    private final DeleteMessageService deleteMessageService;
    private final SendCallbackAnswerService sendCallbackAnswerService;
    private final SendButtonsMessageService sendButtonsMessageService;

    private static final int TOKEN_RATE = 2;
    private static final String BUTTON_TEXT_TEMPLATE = "%s₽ (%d цифр)";
    private static final String SHOW_PRICES_MESSAGE_TEXT = "Выберите вариант оплаты. Курс: 1₽ = 2 цифры для преобразования в мнемокарточку.";

    public ShowPricesCallbackProcessor(ObjectMapper objectMapper, DeleteMessageService deleteMessageService, SendCallbackAnswerService sendCallbackAnswerService, SendButtonsMessageService sendButtonsMessageService) {
        this.objectMapper = objectMapper;
        this.deleteMessageService = deleteMessageService;
        this.sendCallbackAnswerService = sendCallbackAnswerService;
        this.sendButtonsMessageService = sendButtonsMessageService;
    }

    @Override
    public CallbackType getCallBacktype() {
        return CallbackType.SHOW_PRICES;
    }

    @Override
    public void process(Callback callback) {
        sendCallbackAnswerService.sendCallbackAnswer(callback.getCallbackQueryId(), "Запуск процесса оплаты...");

        deleteMessageService.delete(callback.getChatId(), callback.getMessageId());

        showPrices(callback.getChatId().toString());
    }

    private void showPrices(String chatId) {
        var buttons = Stream.of(60, 100, 200).map(price -> {
            var buttonDataBuilder = ButtonData.builder();
            int tokenCount = price * TOKEN_RATE;
            String buttonText = String.format(BUTTON_TEXT_TEMPLATE, price, tokenCount);

            buttonDataBuilder.buttonText(buttonText);
            try {
                String callbackData = objectMapper.writeValueAsString(
                    CallbackData.builder()
                        .type(CallbackType.CREATE_INVOICE)
                        .value(price.toString())
                        .build()
                );

                buttonDataBuilder.buttonCallbackData(callbackData);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            return buttonDataBuilder.build();
        }).collect(Collectors.toList());

        var buttonsMessage = ButtonsMessage.builder()
            .chatId(chatId)
            .message(SHOW_PRICES_MESSAGE_TEXT)
            .buttons(buttons)
            .build();

        sendButtonsMessageService.sendButtonsMessage(buttonsMessage);
    }

}
