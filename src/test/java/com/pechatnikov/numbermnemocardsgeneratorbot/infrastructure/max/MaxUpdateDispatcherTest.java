package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.CallbackService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.CommandHandler;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.GetOrCreateUserCommand;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.NumericMessageHandler;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.callback.CallbackType;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Callback;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Message;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.command.Command;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.command.CommandContext;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxUpdate;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.mapper.MaxUpdateMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.MaxTestData.CHAT_ID;
import static com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.MaxTestData.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MaxUpdateDispatcherTest {
    private MaxClient maxClient;
    private NumericMessageHandler numericMessageHandler;
    private CommandHandler commandHandler;
    private CallbackService callbackService;
    private MaxUpdateDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        maxClient = mock(MaxClient.class);
        numericMessageHandler = mock(NumericMessageHandler.class);
        commandHandler = mock(CommandHandler.class);
        callbackService = mock(CallbackService.class);
        dispatcher = new MaxUpdateDispatcher(
            maxClient,
            new MaxUpdateMapper(new ObjectMapper()),
            numericMessageHandler,
            commandHandler,
            callbackService
        );
    }

    @Test
    void shouldHandleNumericMessage() throws Exception {
        dispatcher.dispatch(MaxTestData.messageCreated("(968) 666 33-44"));

        ArgumentCaptor<GetOrCreateUserCommand> command = ArgumentCaptor.forClass(GetOrCreateUserCommand.class);
        ArgumentCaptor<Message> message = ArgumentCaptor.forClass(Message.class);
        verify(numericMessageHandler).handle(command.capture(), message.capture());
        assertThat(command.getValue().getPlatform()).isEqualTo(BotPlatform.MAX);
        assertThat(command.getValue().getExternalId()).isEqualTo(USER_ID);
        assertThat(message.getValue().getMessage()).isEqualTo("(968) 666 33-44");
        assertThat(message.getValue().getChatId()).isEqualTo(CHAT_ID);
        verifyNoInteractions(commandHandler, callbackService);
    }

    @Test
    void shouldHandleCommand() {
        dispatcher.dispatch(MaxTestData.messageCreated("/balance"));

        verify(commandHandler).handle(Command.BALANCE, expectedContext());
        verifyNoInteractions(numericMessageHandler);
    }

    @Test
    void shouldTreatBotStartedAsStartCommand() {
        dispatcher.dispatch(MaxTestData.botStarted());

        verify(commandHandler).handle(Command.START, expectedContext());
    }

    @Test
    void shouldIgnorePlainTextWithoutDigitsOrCommand() {
        dispatcher.dispatch(MaxTestData.messageCreated("привет"));

        verifyNoInteractions(numericMessageHandler, commandHandler, callbackService, maxClient);
    }

    @Test
    void shouldIgnoreMessageWithoutText() {
        dispatcher.dispatch(MaxTestData.messageCreated(null));

        verifyNoInteractions(numericMessageHandler, commandHandler, maxClient);
    }

    @Test
    void shouldIgnoreUnsupportedUpdateType() {
        dispatcher.dispatch(MaxUpdate.builder().updateType("dialog_muted").build());

        verifyNoInteractions(numericMessageHandler, commandHandler, callbackService, maxClient);
    }

    @Test
    void shouldReplyWithErrorOnUnknownCommand() {
        dispatcher.dispatch(MaxTestData.messageCreated("/unknown"));

        verify(maxClient).sendText(CHAT_ID, MaxUpdateDispatcher.UNEXPECTED_ERROR_MESSAGE);
    }

    @Test
    void shouldReplyWithErrorWhenNumericHandlerFails() throws Exception {
        doThrow(new RuntimeException("boom")).when(numericMessageHandler).handle(any(), any());

        dispatcher.dispatch(MaxTestData.messageCreated("1234"));

        verify(maxClient).sendText(CHAT_ID, MaxUpdateDispatcher.MESSAGE_ERROR_MESSAGE);
    }

    @Test
    void shouldProcessCallback() {
        dispatcher.dispatch(MaxTestData.callback("{\"type\":\"SHOW_PRICES\"}"));

        ArgumentCaptor<Callback> callback = ArgumentCaptor.forClass(Callback.class);
        verify(callbackService).process(callback.capture());
        assertThat(callback.getValue().getType()).isEqualTo(CallbackType.SHOW_PRICES);
        assertThat(callback.getValue().getPlatform()).isEqualTo(BotPlatform.MAX);
        assertThat(callback.getValue().getMessageId()).isEqualTo("mid.2");
    }

    @Test
    void shouldAnswerStaleCallbackWithoutMessage() {
        MaxUpdate update = MaxTestData.callback("{\"type\":\"SHOW_PRICES\"}");
        update.setMessage(null);

        dispatcher.dispatch(update);

        verify(maxClient).answerCallback("cb-1", MaxUpdateDispatcher.STALE_BUTTON_MESSAGE);
        verifyNoInteractions(callbackService);
    }

    @Test
    void shouldAnswerCallbackWithErrorOnInvalidPayload() {
        dispatcher.dispatch(MaxTestData.callback("not-json"));

        verify(maxClient).answerCallback("cb-1", MaxUpdateDispatcher.CALLBACK_ERROR_MESSAGE);
        verifyNoInteractions(callbackService);
    }

    @Test
    void shouldAnswerCallbackWithErrorWhenProcessingFails() {
        doThrow(new RuntimeException("boom")).when(callbackService).process(any());

        dispatcher.dispatch(MaxTestData.callback("{\"type\":\"SHOW_PRICES\"}"));

        verify(maxClient).answerCallback("cb-1", MaxUpdateDispatcher.CALLBACK_ERROR_MESSAGE);
    }

    @Test
    void shouldNotFailWhenErrorReplyFails() {
        doThrow(new MaxApiException(500, "x", "y", null)).when(maxClient).sendText(any(), any());

        dispatcher.dispatch(MaxTestData.messageCreated("/unknown"));

        verify(maxClient).sendText(CHAT_ID, MaxUpdateDispatcher.UNEXPECTED_ERROR_MESSAGE);
    }

    private static CommandContext expectedContext() {
        return CommandContext.builder().platform(BotPlatform.MAX).externalUserId(USER_ID).chatId(CHAT_ID).build();
    }
}
