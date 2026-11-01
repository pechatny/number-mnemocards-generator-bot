package com.pechatnikov.numbermnemocardsgeneratorbot.application.service.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.PaymentAvailabilityPort;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendButtonsMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.callback.CallbackType;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.ButtonData;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.ButtonsMessage;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.command.CommandContext;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.dto.CallbackData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class StartCommandProcessorTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private SendButtonsMessageService sendButtonsMessageService;
    private PaymentAvailabilityPort paymentAvailabilityPort;
    private StartCommandProcessor processor;

    @BeforeEach
    void setUp() {
        sendButtonsMessageService = mock(SendButtonsMessageService.class);
        paymentAvailabilityPort = mock(PaymentAvailabilityPort.class);
        processor = new StartCommandProcessor(mock(SendMessageService.class), sendButtonsMessageService, paymentAvailabilityPort, objectMapper);
    }

    @Test
    void shouldAttachCommandButtonsToWelcomeMessage() throws Exception {
        when(paymentAvailabilityPort.isAvailable(BotPlatform.MAX)).thenReturn(false);

        processor.process(context(BotPlatform.MAX));

        ButtonsMessage message = sentMessage();
        assertThat(message.getPlatform()).isEqualTo(BotPlatform.MAX);
        assertThat(message.getChatId()).isEqualTo("42");
        assertThat(message.getMessage()).startsWith("*Добро пожаловать");
        assertThat(message.getButtons())
            .extracting(ButtonData::getButtonText)
            .containsExactly(StartCommandProcessor.BALANCE_BUTTON_TEXT, StartCommandProcessor.HELP_BUTTON_TEXT);

        CallbackData balance = objectMapper.readValue(message.getButtons().get(0).getButtonCallbackData(), CallbackData.class);
        assertThat(balance.getType()).isEqualTo(CallbackType.RUN_COMMAND);
        assertThat(balance.getValue()).isEqualTo("BALANCE");
        CallbackData help = objectMapper.readValue(message.getButtons().get(1).getButtonCallbackData(), CallbackData.class);
        assertThat(help.getValue()).isEqualTo("START");
    }

    @Test
    void shouldAddRefillButtonWhenPaymentsAvailable() throws Exception {
        when(paymentAvailabilityPort.isAvailable(BotPlatform.TELEGRAM)).thenReturn(true);

        processor.process(context(BotPlatform.TELEGRAM));

        ButtonsMessage message = sentMessage();
        assertThat(message.getButtons())
            .extracting(ButtonData::getButtonText)
            .containsExactly(
                StartCommandProcessor.BALANCE_BUTTON_TEXT,
                StartCommandProcessor.REFILL_BUTTON_TEXT,
                StartCommandProcessor.HELP_BUTTON_TEXT
            );
        CallbackData refill = objectMapper.readValue(message.getButtons().get(1).getButtonCallbackData(), CallbackData.class);
        assertThat(refill.getValue()).isEqualTo("BALANCE_REFILL");
    }

    @Test
    void shouldFitTelegramCallbackDataLimit() {
        when(paymentAvailabilityPort.isAvailable(BotPlatform.TELEGRAM)).thenReturn(true);

        processor.process(context(BotPlatform.TELEGRAM));

        // Telegram ограничивает callback_data 64 байтами
        assertThat(sentMessage().getButtons())
            .allSatisfy(button -> assertThat(button.getButtonCallbackData().getBytes(StandardCharsets.UTF_8))
                .hasSizeLessThanOrEqualTo(64));
    }

    private ButtonsMessage sentMessage() {
        ArgumentCaptor<ButtonsMessage> captor = ArgumentCaptor.forClass(ButtonsMessage.class);
        verify(sendButtonsMessageService).sendButtonsMessage(captor.capture());
        return captor.getValue();
    }

    private static CommandContext context(BotPlatform platform) {
        return CommandContext.builder().platform(platform).externalUserId(7L).chatId(42L).build();
    }
}
