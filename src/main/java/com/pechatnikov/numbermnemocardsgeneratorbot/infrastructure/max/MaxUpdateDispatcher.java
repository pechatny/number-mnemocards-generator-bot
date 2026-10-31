package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.CallbackService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.CommandHandler;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.NumericMessageHandler;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.command.Command;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration.ConditionalOnMaxBot;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxMessage;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxUpdate;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.mapper.MaxUpdateMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Разбор событий Max во входящие порты. Порядок проверок повторяет Telegram-бота:
 * сообщение с цифрами → мнемокарточка, сообщение с "/" → команда, нажатие кнопки → callback.
 */
@Slf4j
@ConditionalOnMaxBot
@Component
public class MaxUpdateDispatcher {
    static final String UNEXPECTED_ERROR_MESSAGE = "❌ Произошла непредвиденная ошибка";
    static final String MESSAGE_ERROR_MESSAGE = "❌ Ошибка обработки сообщения";
    static final String CALLBACK_ERROR_MESSAGE = "❌ Ошибка обработки запроса";
    static final String STALE_BUTTON_MESSAGE = "Кнопка устарела, повторите команду";

    private final MaxClient maxClient;
    private final MaxUpdateMapper maxUpdateMapper;
    private final NumericMessageHandler numericMessageHandler;
    private final CommandHandler commandHandler;
    private final CallbackService callbackService;

    public MaxUpdateDispatcher(
        MaxClient maxClient,
        MaxUpdateMapper maxUpdateMapper,
        NumericMessageHandler numericMessageHandler,
        CommandHandler commandHandler,
        CallbackService callbackService
    ) {
        this.maxClient = maxClient;
        this.maxUpdateMapper = maxUpdateMapper;
        this.numericMessageHandler = numericMessageHandler;
        this.commandHandler = commandHandler;
        this.callbackService = callbackService;
    }

    public void dispatch(MaxUpdate update) {
        try {
            String updateType = update.getUpdateType() == null ? "" : update.getUpdateType();
            switch (updateType) {
                case MaxUpdate.MESSAGE_CREATED:
                    handleMessage(update.getMessage());
                    break;
                case MaxUpdate.MESSAGE_CALLBACK:
                    handleCallback(update);
                    break;
                case MaxUpdate.BOT_STARTED:
                    commandHandler.handle(Command.START, maxUpdateMapper.toCommandContext(update.getChatId(), update.getUser()));
                    break;
                default:
                    log.warn("Unsupported Max update type: {}", update);
            }
        } catch (Exception e) {
            log.error("Error processing Max update: {}", update, e);
            handleError(update);
        }
    }

    private void handleMessage(MaxMessage message) throws Exception {
        if (message == null || message.getSender() == null || message.getBody() == null || message.getBody().getText() == null) {
            log.warn("Unsupported Max message: {}", message);
            return;
        }

        String text = message.getBody().getText();
        if (containsDigits(text)) {
            handleNumericMessage(message);
        } else if (text.startsWith("/")) {
            commandHandler.handle(
                Command.fromValue(text),
                maxUpdateMapper.toCommandContext(message.getRecipient().getChatId(), message.getSender())
            );
        } else {
            log.warn("Unsupported Max message text: {}", message);
        }
    }

    private void handleNumericMessage(MaxMessage message) {
        try {
            numericMessageHandler.handle(
                maxUpdateMapper.toGetOrCreateUserCommand(message.getSender()),
                maxUpdateMapper.toMessage(message)
            );
        } catch (Exception e) {
            log.error("Error handling Max text message", e);
            sendErrorMessage(message.getRecipient().getChatId(), MESSAGE_ERROR_MESSAGE);
        }
    }

    private void handleCallback(MaxUpdate update) throws Exception {
        if (update.getMessage() == null) {
            // Сообщение с кнопкой уже удалено: обработать нажатие негде
            log.warn("Max callback without message: {}", update.getCallback());
            maxClient.answerCallback(update.getCallback().getCallbackId(), STALE_BUTTON_MESSAGE);
            return;
        }

        callbackService.process(maxUpdateMapper.toCallback(update.getCallback(), update.getMessage()));
    }

    private void handleError(MaxUpdate update) {
        try {
            if (MaxUpdate.MESSAGE_CALLBACK.equals(update.getUpdateType()) && update.getCallback() != null) {
                maxClient.answerCallback(update.getCallback().getCallbackId(), CALLBACK_ERROR_MESSAGE);
            } else if (MaxUpdate.BOT_STARTED.equals(update.getUpdateType())) {
                sendErrorMessage(update.getChatId(), UNEXPECTED_ERROR_MESSAGE);
            } else if (update.getMessage() != null && update.getMessage().getRecipient() != null) {
                sendErrorMessage(update.getMessage().getRecipient().getChatId(), UNEXPECTED_ERROR_MESSAGE);
            }
        } catch (Exception e) {
            log.error("Error sending Max error message", e);
        }
    }

    private void sendErrorMessage(Long chatId, String errorMessage) {
        try {
            maxClient.sendText(chatId, errorMessage);
        } catch (Exception e) {
            log.error("Failed to send Max error message", e);
        }
    }

    private static boolean containsDigits(String text) {
        return text.matches("(?s).*\\d.*");
    }
}
