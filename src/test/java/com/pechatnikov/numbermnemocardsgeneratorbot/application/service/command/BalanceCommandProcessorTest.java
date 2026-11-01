package com.pechatnikov.numbermnemocardsgeneratorbot.application.service.command;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.TokenBalanceService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.UserService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.TokenBalance;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.command.CommandContext;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.user.User;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.*;

class BalanceCommandProcessorTest {

    @Test
    void shouldLookUpUserAndReplyOnContextPlatform() {
        SendMessageService sendMessageService = mock(SendMessageService.class);
        TokenBalanceService tokenBalanceService = mock(TokenBalanceService.class);
        UserService userService = mock(UserService.class);
        User user = User.builder().setId(1L).setPlatform(BotPlatform.MAX).setExternalId(7L).build();
        when(userService.findByPlatformAndExternalId(BotPlatform.MAX, 7L)).thenReturn(Optional.of(user));
        when(tokenBalanceService.findOrCreateTokenBalanceForUser(user))
            .thenReturn(TokenBalance.builder().user(user).balance(15L).build());
        BalanceCommandProcessor processor = new BalanceCommandProcessor(sendMessageService, tokenBalanceService, userService);

        processor.process(CommandContext.builder().platform(BotPlatform.MAX).externalUserId(7L).chatId(42L).build());

        verify(sendMessageService).sendMessage(BotPlatform.MAX, 42L, "Ваш баланс цифр 15 шт.");
    }

    @Test
    void shouldReplyInitialBalanceWhenUserNotCreatedYet() {
        SendMessageService sendMessageService = mock(SendMessageService.class);
        TokenBalanceService tokenBalanceService = mock(TokenBalanceService.class);
        UserService userService = mock(UserService.class);
        when(userService.findByPlatformAndExternalId(BotPlatform.MAX, 7L)).thenReturn(Optional.empty());
        BalanceCommandProcessor processor = new BalanceCommandProcessor(sendMessageService, tokenBalanceService, userService);

        processor.process(CommandContext.builder().platform(BotPlatform.MAX).externalUserId(7L).chatId(42L).build());

        verify(sendMessageService).sendMessage(BotPlatform.MAX, 42L, "Ваш баланс цифр 100 шт.");
        verifyNoInteractions(tokenBalanceService);
    }
}
