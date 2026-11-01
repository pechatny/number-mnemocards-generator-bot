package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.messaging;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.ButtonMessage;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.ButtonsMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class MessengerRouterTest {
    private MessengerGateway telegram;
    private MessengerGateway max;
    private MessengerRouter router;

    @BeforeEach
    void setUp() {
        telegram = gateway(BotPlatform.TELEGRAM);
        max = gateway(BotPlatform.MAX);
        router = new MessengerRouter(List.of(telegram, max));
    }

    @Test
    void shouldRouteMessageToPlatformGateway() {
        router.sendMessage(BotPlatform.MAX, 1L, "text");

        verify(max).sendMessage(1L, "text");
        verify(telegram, never()).sendMessage(any(), any());
    }

    @Test
    void shouldRoutePhotoToPlatformGateway() {
        File photo = new File("photo.png");

        router.sendPhoto(BotPlatform.TELEGRAM, 1L, photo);

        verify(telegram).sendPhoto(1L, photo);
        verify(max, never()).sendPhoto(any(), any());
    }

    @Test
    void shouldRouteButtonMessageByItsPlatform() {
        ButtonMessage message = ButtonMessage.builder().platform(BotPlatform.MAX).chatId(1L).build();

        router.sendButtonMessage(message);

        verify(max).sendButtonMessage(message);
        verify(telegram, never()).sendButtonMessage(any());
    }

    @Test
    void shouldRouteButtonsMessageByItsPlatform() {
        ButtonsMessage message = ButtonsMessage.builder().platform(BotPlatform.TELEGRAM).chatId("1").build();

        router.sendButtonsMessage(message);

        verify(telegram).sendButtonsMessage(message);
        verify(max, never()).sendButtonsMessage(any());
    }

    @Test
    void shouldRouteCallbackAnswerToPlatformGateway() {
        router.sendCallbackAnswer(BotPlatform.MAX, "cb", "ok");

        verify(max).sendCallbackAnswer("cb", "ok");
        verify(telegram, never()).sendCallbackAnswer(any(), any());
    }

    @Test
    void shouldRouteDeleteToPlatformGateway() {
        router.delete(BotPlatform.MAX, 1L, "mid.1");

        verify(max).deleteMessage(1L, "mid.1");
        verify(telegram, never()).deleteMessage(any(), any());
    }

    @Test
    void shouldFailForDisabledPlatform() {
        MessengerRouter maxOnlyRouter = new MessengerRouter(List.of(max));

        assertThatThrownBy(() -> maxOnlyRouter.sendMessage(BotPlatform.TELEGRAM, 1L, "text"))
            .isInstanceOf(MessengerPlatformNotEnabledException.class)
            .hasMessageContaining("TELEGRAM");
    }

    @Test
    void shouldFailOnDuplicatePlatformGateways() {
        MessengerGateway anotherMax = gateway(BotPlatform.MAX);

        assertThatThrownBy(() -> new MessengerRouter(List.of(max, anotherMax)))
            .isInstanceOf(IllegalStateException.class);
    }

    private static MessengerGateway gateway(BotPlatform platform) {
        MessengerGateway gateway = mock(MessengerGateway.class);
        when(gateway.platform()).thenReturn(platform);
        return gateway;
    }
}
