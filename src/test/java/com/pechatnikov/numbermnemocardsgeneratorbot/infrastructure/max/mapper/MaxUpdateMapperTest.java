package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.GetOrCreateUserCommand;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.callback.CallbackType;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Callback;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Message;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.command.CommandContext;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.MaxTestData;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxUpdate;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxUser;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MaxUpdateMapperTest {
    private final MaxUpdateMapper mapper = new MaxUpdateMapper(new ObjectMapper());

    @Test
    void shouldMapUserWithoutUsernameAndLastname() {
        GetOrCreateUserCommand command = mapper.toGetOrCreateUserCommand(MaxTestData.user());

        assertThat(command.getPlatform()).isEqualTo(BotPlatform.MAX);
        assertThat(command.getExternalId()).isEqualTo(MaxTestData.USER_ID);
        assertThat(command.getFirstname()).isEqualTo("Иван");
        assertThat(command.getLastname()).isNull();
        assertThat(command.getUsername()).isNull();
    }

    @Test
    void shouldMapUsernameWhenPresent() {
        MaxUser user = MaxUser.builder().userId(1L).firstName("A").lastName("B").username("nick").build();

        GetOrCreateUserCommand command = mapper.toGetOrCreateUserCommand(user);

        assertThat(command.getUsername()).isEqualTo("nick");
        assertThat(command.getLastname()).isEqualTo("B");
    }

    @Test
    void shouldMapMessageTextAndChat() {
        Message message = mapper.toMessage(MaxTestData.message("968 666", "mid.1"));

        assertThat(message.getMessage()).isEqualTo("968 666");
        assertThat(message.getChatId()).isEqualTo(MaxTestData.CHAT_ID);
        assertThat(message.getUser()).isNull();
    }

    @Test
    void shouldMapCommandContext() {
        CommandContext context = mapper.toCommandContext(MaxTestData.CHAT_ID, MaxTestData.user());

        assertThat(context.getPlatform()).isEqualTo(BotPlatform.MAX);
        assertThat(context.getExternalUserId()).isEqualTo(MaxTestData.USER_ID);
        assertThat(context.getChatId()).isEqualTo(MaxTestData.CHAT_ID);
    }

    @Test
    void shouldMapCallbackWithStringMessageId() throws Exception {
        MaxUpdate update = MaxTestData.callback("{\"type\":\"CREATE_INVOICE\",\"value\":\"100\"}");

        Callback callback = mapper.toCallback(update.getCallback(), update.getMessage());

        assertThat(callback.getPlatform()).isEqualTo(BotPlatform.MAX);
        assertThat(callback.getType()).isEqualTo(CallbackType.CREATE_INVOICE);
        assertThat(callback.getValue()).isEqualTo("100");
        assertThat(callback.getExternalUserId()).isEqualTo(MaxTestData.USER_ID);
        assertThat(callback.getChatId()).isEqualTo(MaxTestData.CHAT_ID);
        assertThat(callback.getMessageId()).isEqualTo("mid.2");
        assertThat(callback.getCallbackQueryId()).isEqualTo("cb-1");
    }

    @Test
    void shouldFailOnInvalidCallbackPayload() {
        MaxUpdate update = MaxTestData.callback("not-json");

        assertThatThrownBy(() -> mapper.toCallback(update.getCallback(), update.getMessage()))
            .isInstanceOf(JsonProcessingException.class);
    }
}
