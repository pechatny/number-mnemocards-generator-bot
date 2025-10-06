package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendPreCheckoutService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class SendPreCheckoutAdapter implements SendPreCheckoutService {
    private final TelegramApiClient telegramApiClient;


    public SendPreCheckoutAdapter(TelegramApiClient telegramApiClient) {
        this.telegramApiClient = telegramApiClient;
    }

    @Override
    public void send(String preCheckoutId, boolean result) {
        AnswerPreCheckoutQuery answer = new AnswerPreCheckoutQuery();
        answer.setPreCheckoutQueryId(preCheckoutId);
        answer.setOk(result);

        sendSafely(answer);
    }

    private void sendSafely(AnswerPreCheckoutQuery answer) {
        try {
            log.info("AnswerPreCheckoutQuery отправлен!");
            telegramApiClient.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки AnswerPreCheckoutQuery: {}", answer, e);
        }
    }

}
