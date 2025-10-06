package com.pechatnikov.numbermnemocardsgeneratorbot.application.service;

import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.TelegramApiClient;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendInvoice;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class PaymentService {

    private final TelegramApiClient telegramApiClient; // Ваш бот, расширяющий TelegramLongPollingBot

    public PaymentService(TelegramApiClient telegramApiClient) {
        this.telegramApiClient = telegramApiClient;
    }

    public void sendInvoice(
        String chatId,
        String title,
        String description,
        String payload,
        String providerToken,
        String currency,
        Integer priceAmount
    ) {

        // Создаем цену (может быть несколько цен для разных товаров)
        List<LabeledPrice> prices = IntStream.of(100).mapToObj(amount -> {
                String priceTitle = amount * 0.5 + " цифр";
                // в копейках/центах
                return new LabeledPrice(priceTitle, amount * 100);
            })
            .collect(Collectors.toList());


        // Создаем инвойс
        SendInvoice sendInvoice = new SendInvoice();
        sendInvoice.setChatId(chatId);
        sendInvoice.setTitle(title);
        sendInvoice.setDescription(description);
        sendInvoice.setPayload(payload);
        sendInvoice.setProviderToken(providerToken); // Токен платежной системы
        sendInvoice.setCurrency(currency); // "RUB", "USD", "EUR" и т.д.
        sendInvoice.setPrices(prices);
        sendInvoice.setStartParameter("start_parameter");

        // Опционально: настройки
//        sendInvoice.setNeedEmail(true);
//        sendInvoice.setNeedPhoneNumber(false);
//        sendInvoice.setNeedShippingAddress(false);
//        sendInvoice.setIsFlexible(false); // Фиксированная стоимость доставки
//        sendInvoice.setSendEmailToProvider(false);
//        sendInvoice.setSendPhoneNumberToProvider(false);

        // Кнопка для оплаты (создается автоматически)
        // Дополнительные кнопки можно добавить через ReplyMarkup

        try {
            telegramApiClient.execute(sendInvoice);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
