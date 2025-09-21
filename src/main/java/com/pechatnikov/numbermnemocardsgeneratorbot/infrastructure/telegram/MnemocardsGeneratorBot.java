package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.NumericMessageHandler;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration.BotProperties;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.mapper.TelegramUpdateMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class MnemocardsGeneratorBot extends TelegramLongPollingBot {
    private final BotProperties props;
    private final ExecutorService executorService;
    private final TelegramUpdateMapper telegramUpdateMapper;
    private final NumericMessageHandler numericMessageHandler;

    public MnemocardsGeneratorBot(
        BotProperties props,
        TelegramUpdateMapper telegramUpdateMapper,
        NumericMessageHandler numericMessageHandler
    ) {
        this.props = props;
        this.numericMessageHandler = numericMessageHandler;
        this.telegramUpdateMapper = telegramUpdateMapper;
        this.executorService = Executors.newFixedThreadPool(10); // Пул потоков для обработки
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        // Асинхронная обработка для избежания блокировки
        executorService.submit(() -> processUpdate(update));
    }

    private void processUpdate(Update update) {
        try {
            if (containsDigits(update)) {
                handleNumericMessage(update);
//            } else if (update.hasCallbackQuery()) {
//                handleCallbackQuery(update);
//            } else if (update.hasEditedMessage()) {
//                handleEditedMessage(update);
//            } else if (update.hasChannelPost()) {
//                handleChannelPost(update);
            } else {
                log.warn("Unsupported update type: {}", update);
            }
        } catch (Exception e) {
            log.error("Error processing update: {}", update.getUpdateId(), e);
            handleError(update, e);
        }
    }

    private boolean containsDigits(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();

            return text.matches(".*\\d.*");
        } else {
            return false;
        }
    }

    private void handleTextMessage(Update update) {
//        try {
//            SendMessage response = messageHandler.handleMessage(update);
//            if (response != null) {
//                executeSafely(response);
//            }
//        } catch (Exception e) {
//            log.error("Error handling text message", e);
//            sendErrorMessage(update.getMessage().getChatId(), "Ошибка обработки сообщения");
//        }
    }

    private void handleNumericMessage(Update update) {
        try {
            numericMessageHandler.handle(
                telegramUpdateMapper.toGetOrCreateUserCommand(update),
                telegramUpdateMapper.toMessage(update, null)
            );
        } catch (Exception e) {
            log.error("Error handling text message", e);
            sendErrorMessage(update.getMessage().getChatId(), "Ошибка обработки сообщения");
        }
    }

    private void handleError(Update update, Exception exception) {
        log.error("Unhandled error processing update {}", update.getUpdateId(), exception);

        try {
            if (update.hasMessage()) {
                sendErrorMessage(update.getMessage().getChatId(), "Произошла непредвиденная ошибка");
            } else if (update.hasCallbackQuery()) {
                sendCallbackError(update.getCallbackQuery().getId());
            }
        } catch (Exception e) {
            log.error("Error sending error message", e);
        }
    }

    private void sendErrorMessage(Long chatId, String errorMessage) {
        try {
            SendMessage errorResponse = SendMessage.builder()
                .chatId(chatId.toString())
                .text("❌ " + errorMessage)
                .build();
            executeSafely(errorResponse);
        } catch (Exception e) {
            log.error("Failed to send error message", e);
        }
    }

    private void sendCallbackError(String callbackId) {
        try {
            AnswerCallbackQuery errorResponse = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackId)
                .text("❌ Ошибка обработки запроса")
                .showAlert(true)
                .build();
            executeSafely(errorResponse);
        } catch (Exception e) {
            log.error("Failed to send callback error", e);
        }
    }

    private void executeSafely(BotApiMethod<?> method) {
        try {
            execute(method);
        } catch (TelegramApiException e) {
            log.error("Failed to execute BotApiMethod: {}", method, e);
        }
    }

    @Override
    public String getBotUsername() {
        return props.getName();
    }

    @Override
    public String getBotToken() {
        return props.getToken();
    }

    @Override
    public void onClosing() {
        log.info("Bot is shutting down...");
        executorService.shutdown();
        super.onClosing();
    }
}
