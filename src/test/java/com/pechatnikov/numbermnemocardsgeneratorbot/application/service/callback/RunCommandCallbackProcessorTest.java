package com.pechatnikov.numbermnemocardsgeneratorbot.application.service.callback;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.CommandHandler;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendCallbackAnswerService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Callback;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.command.Command;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.command.CommandContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RunCommandCallbackProcessorTest {
    private CommandHandler commandHandler;
    private SendCallbackAnswerService sendCallbackAnswerService;
    private RunCommandCallbackProcessor processor;

    @BeforeEach
    void setUp() {
        commandHandler = mock(CommandHandler.class);
        sendCallbackAnswerService = mock(SendCallbackAnswerService.class);
        processor = new RunCommandCallbackProcessor(commandHandler, sendCallbackAnswerService);
    }

    @Test
    void shouldRunCommandFromButtonInUserContext() {
        processor.process(callback("BALANCE"));

        verify(sendCallbackAnswerService).sendCallbackAnswer(BotPlatform.MAX, "cb-1", "Баланс цифр");
        verify(commandHandler).handle(Command.BALANCE, CommandContext.builder()
            .platform(BotPlatform.MAX)
            .externalUserId(7L)
            .chatId(42L)
            .build());
    }

    @Test
    void shouldRejectUnknownCommand() {
        assertThatThrownBy(() -> processor.process(callback("DROP_DATABASE")))
            .isInstanceOf(IllegalArgumentException.class);
        verify(commandHandler, never()).handle(any(), any());
    }

    private static Callback callback(String value) {
        return Callback.builder()
            .type(CallbackType.RUN_COMMAND)
            .value(value)
            .platform(BotPlatform.MAX)
            .externalUserId(7L)
            .chatId(42L)
            .messageId("mid.1")
            .callbackQueryId("cb-1")
            .build();
    }
}
