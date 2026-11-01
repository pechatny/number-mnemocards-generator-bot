package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.exception.SendPhotoException;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.callback.CallbackType;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.ButtonData;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.ButtonMessage;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.ButtonsMessage;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxButton;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.dto.CallbackData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MaxGatewayTest {
    private static final MaxApiException API_ERROR = new MaxApiException(500, "internal", "boom", null);

    private MaxClient maxClient;
    private ObjectMapper objectMapper;
    private MaxGateway gateway;

    @BeforeEach
    void setUp() {
        maxClient = mock(MaxClient.class);
        objectMapper = new ObjectMapper();
        gateway = new MaxGateway(maxClient, objectMapper);
    }

    @Test
    void shouldServeMaxPlatform() {
        assertThat(gateway.platform()).isEqualTo(BotPlatform.MAX);
    }

    @Test
    void shouldSendTextConvertedToMaxMarkdown() {
        gateway.sendMessage(42L, "*Заголовок*\nтекст");

        verify(maxClient).sendText(42L, "**Заголовок**\nтекст");
    }

    @Test
    void shouldLogAndSwallowTextErrors() {
        doThrow(API_ERROR).when(maxClient).sendText(anyLong(), anyString());

        assertThatCode(() -> gateway.sendMessage(42L, "text")).doesNotThrowAnyException();
    }

    @Test
    void shouldSendPhoto() {
        File photo = new File("card.png");

        gateway.sendPhoto(42L, photo);

        verify(maxClient).sendImage(42L, photo);
    }

    @Test
    void shouldWrapPhotoErrorsInSendPhotoException() {
        doThrow(API_ERROR).when(maxClient).sendImage(anyLong(), any());

        assertThatThrownBy(() -> gateway.sendPhoto(42L, new File("card.png")))
            .isInstanceOf(SendPhotoException.class)
            .hasCause(API_ERROR);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSendSingleButtonWithCallbackTypePayload() throws Exception {
        ButtonMessage message = ButtonMessage.builder()
            .platform(BotPlatform.MAX)
            .chatId(42L)
            .message("Нажмите кнопку")
            .buttonText("💳 Показать цены")
            .callbackType(CallbackType.SHOW_PRICES)
            .build();

        gateway.sendButtonMessage(message);

        ArgumentCaptor<List<List<MaxButton>>> rows = ArgumentCaptor.forClass(List.class);
        verify(maxClient).sendButtons(eq(42L), eq("Нажмите кнопку"), rows.capture());
        assertThat(rows.getValue()).hasSize(1);
        MaxButton button = rows.getValue().get(0).get(0);
        assertThat(button.getType()).isEqualTo("callback");
        assertThat(button.getText()).isEqualTo("💳 Показать цены");
        assertThat(objectMapper.readValue(button.getPayload(), CallbackData.class).getType())
            .isEqualTo(CallbackType.SHOW_PRICES);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSendOneButtonPerRow() {
        ButtonsMessage message = ButtonsMessage.builder()
            .platform(BotPlatform.MAX)
            .chatId("42")
            .message("Выберите вариант")
            .buttons(List.of(
                ButtonData.builder().buttonText("60₽").buttonCallbackData("p60").build(),
                ButtonData.builder().buttonText("100₽").buttonCallbackData("p100").build()
            ))
            .build();

        gateway.sendButtonsMessage(message);

        ArgumentCaptor<List<List<MaxButton>>> rows = ArgumentCaptor.forClass(List.class);
        verify(maxClient).sendButtons(eq(42L), eq("Выберите вариант"), rows.capture());
        assertThat(rows.getValue()).hasSize(2);
        assertThat(rows.getValue().get(0)).containsExactly(MaxButton.callback("60₽", "p60"));
        assertThat(rows.getValue().get(1)).containsExactly(MaxButton.callback("100₽", "p100"));
    }

    @Test
    void shouldConvertButtonsMessageToMaxMarkdown() {
        ButtonsMessage message = ButtonsMessage.builder()
            .chatId("42")
            .message("*Добро пожаловать*")
            .buttons(List.of(ButtonData.builder().buttonText("💰 Баланс").buttonCallbackData("p").build()))
            .build();

        gateway.sendButtonsMessage(message);

        verify(maxClient).sendButtons(eq(42L), eq("**Добро пожаловать**"), anyList());
    }

    @Test
    void shouldLogAndSwallowButtonErrors() {
        doThrow(API_ERROR).when(maxClient).sendButtons(anyLong(), anyString(), anyList());
        ButtonMessage message = ButtonMessage.builder()
            .chatId(42L).message("m").buttonText("b").callbackType(CallbackType.SHOW_PRICES).build();

        assertThatCode(() -> gateway.sendButtonMessage(message)).doesNotThrowAnyException();
    }

    @Test
    void shouldAnswerCallbackAndPropagateErrors() {
        gateway.sendCallbackAnswer("cb-1", "Готово");
        verify(maxClient).answerCallback("cb-1", "Готово");

        doThrow(API_ERROR).when(maxClient).answerCallback(anyString(), anyString());
        assertThatThrownBy(() -> gateway.sendCallbackAnswer("cb-2", "x")).isSameAs(API_ERROR);
    }

    @Test
    void shouldDeleteMessageByMidAndSwallowErrors() {
        gateway.deleteMessage(42L, "mid.1");
        verify(maxClient).deleteMessage("mid.1");

        doThrow(API_ERROR).when(maxClient).deleteMessage(anyString());
        assertThatCode(() -> gateway.deleteMessage(42L, "mid.2")).doesNotThrowAnyException();
    }
}
