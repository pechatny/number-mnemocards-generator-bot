package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.CallbackService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.GetOrCreateUserCommand;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.callback.CallbackType;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Callback;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.handler.CallbackHandler;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.mapper.TelegramUpdateMapperImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TelegramInboundMappingTest {

    @Test
    void shouldMapTelegramUserToTelegramPlatform() {
        Update update = new Update();
        update.setMessage(message(55));

        GetOrCreateUserCommand command = new TelegramUpdateMapperImpl().toGetOrCreateUserCommand(update);

        assertThat(command.getPlatform()).isEqualTo(BotPlatform.TELEGRAM);
        assertThat(command.getExternalId()).isEqualTo(7L);
        assertThat(command.getUsername()).isEqualTo("tg_user");
    }

    @Test
    void shouldMapCallbackWithTelegramPlatformAndStringMessageId() throws Exception {
        CallbackService callbackService = mock(CallbackService.class);
        CallbackHandler handler = new CallbackHandler(callbackService, new ObjectMapper());
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setId("cb-1");
        callbackQuery.setFrom(user());
        callbackQuery.setMessage(message(55));
        callbackQuery.setData("{\"type\":\"SHOW_PRICES\"}");
        Update update = new Update();
        update.setCallbackQuery(callbackQuery);

        handler.handle(update);

        ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);
        verify(callbackService).process(captor.capture());
        Callback callback = captor.getValue();
        assertThat(callback.getPlatform()).isEqualTo(BotPlatform.TELEGRAM);
        assertThat(callback.getExternalUserId()).isEqualTo(7L);
        assertThat(callback.getChatId()).isEqualTo(42L);
        assertThat(callback.getMessageId()).isEqualTo("55");
        assertThat(callback.getCallbackQueryId()).isEqualTo("cb-1");
        assertThat(callback.getType()).isEqualTo(CallbackType.SHOW_PRICES);
    }

    private static Message message(int messageId) {
        Chat chat = new Chat();
        chat.setId(42L);
        Message message = new Message();
        message.setMessageId(messageId);
        message.setChat(chat);
        message.setFrom(user());
        message.setText("1234");
        return message;
    }

    private static User user() {
        User user = new User();
        user.setId(7L);
        user.setFirstName("first");
        user.setUserName("tg_user");
        return user;
    }
}
