package com.pechatnikov.numbermnemocardsgeneratorbot.application.service.command;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.CommandProcessor;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.command.Command;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.command.CommandContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CommandHandlerImplTest {
    private CommandProcessorRegistrar registrar;
    private CommandHandlerImpl commandHandler;

    @BeforeEach
    void setUp() {
        registrar = new CommandProcessorRegistrar();
        commandHandler = new CommandHandlerImpl(registrar, mock(SendMessageService.class));
    }

    @Test
    void shouldPassContextToRegisteredProcessor() {
        CommandProcessor processor = mock(CommandProcessor.class);
        when(processor.getCommand()).thenReturn(Command.BALANCE);
        registrar.register(processor);
        CommandContext context = CommandContext.builder()
            .platform(BotPlatform.MAX)
            .externalUserId(7L)
            .chatId(42L)
            .build();

        commandHandler.handle(Command.BALANCE, context);

        verify(processor).process(context);
    }

    @Test
    void shouldFailForUnregisteredCommand() {
        CommandContext context = CommandContext.builder().platform(BotPlatform.TELEGRAM).chatId(1L).build();

        assertThatThrownBy(() -> commandHandler.handle(Command.START, context))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
