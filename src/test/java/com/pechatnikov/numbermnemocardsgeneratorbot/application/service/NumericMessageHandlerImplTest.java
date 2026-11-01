package com.pechatnikov.numbermnemocardsgeneratorbot.application.service;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.GetOrCreateUserCommand;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.MessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.ShowPricesButtonUseCase;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.TokenBalanceService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.TokenTransactionService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.UserService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendMessageService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.SendPhotoService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Message;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.TokenBalance;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class NumericMessageHandlerImplTest {
    private static final Long CHAT_ID = 42L;

    private UserService userService;
    private MessageService messageService;
    private NumberSplitter splitter;
    private ImageMerger imageMerger;
    private SendPhotoService sendPhotoService;
    private TokenTransactionService tokenTransactionService;
    private TokenBalanceService tokenBalanceService;
    private SendMessageService sendMessageService;
    private ShowPricesButtonUseCase showPricesButtonUseCase;
    private NumericMessageHandlerImpl handler;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        messageService = mock(MessageService.class);
        splitter = mock(NumberSplitter.class);
        imageMerger = mock(ImageMerger.class);
        sendPhotoService = mock(SendPhotoService.class);
        tokenTransactionService = mock(TokenTransactionService.class);
        tokenBalanceService = mock(TokenBalanceService.class);
        sendMessageService = mock(SendMessageService.class);
        showPricesButtonUseCase = mock(ShowPricesButtonUseCase.class);

        handler = new NumericMessageHandlerImpl(
            userService,
            messageService,
            splitter,
            imageMerger,
            sendPhotoService,
            tokenTransactionService,
            tokenBalanceService,
            sendMessageService,
            showPricesButtonUseCase
        );

        when(messageService.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenTransactionService.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldSendPhotoToUserPlatform() throws Exception {
        User user = givenUser(BotPlatform.MAX);
        givenBalance(user, 10L);
        when(splitter.split("1234")).thenReturn(List.of("12", "34"));
        File image = Files.createFile(tempDir.resolve("image.png")).toFile();
        when(imageMerger.mergeImages(List.of("12", "34"))).thenReturn(image);

        handler.handle(command(BotPlatform.MAX), message("1234"));

        verify(sendPhotoService).sendPhoto(BotPlatform.MAX, CHAT_ID, image);
        verify(tokenBalanceService).decrease(any());
    }

    @Test
    void shouldShowPricesOnUserPlatformWhenNotEnoughTokens() throws Exception {
        User user = givenUser(BotPlatform.MAX);
        givenBalance(user, 1L);
        when(splitter.split("1234")).thenReturn(List.of("12", "34"));

        handler.handle(command(BotPlatform.MAX), message("1234"));

        verify(sendMessageService).sendMessage(eq(BotPlatform.MAX), eq(CHAT_ID), anyString());
        verify(showPricesButtonUseCase).showPricesButton(BotPlatform.MAX, CHAT_ID);
        verify(sendPhotoService, never()).sendPhoto(any(), anyLong(), any());
    }

    @Test
    void shouldRejectTooLongMessageOnUserPlatform() throws Exception {
        givenUser(BotPlatform.TELEGRAM);

        handler.handle(command(BotPlatform.TELEGRAM), message("1".repeat(101)));

        verify(sendMessageService).sendMessage(eq(BotPlatform.TELEGRAM), eq(CHAT_ID), anyString());
        verify(messageService, never()).save(any());
        verify(sendPhotoService, never()).sendPhoto(any(), anyLong(), any());
    }

    @Test
    void shouldRejectTooShortNumberOnUserPlatform() throws Exception {
        givenUser(BotPlatform.MAX);
        when(splitter.split("1")).thenReturn(List.of());

        handler.handle(command(BotPlatform.MAX), message("1"));

        verify(sendMessageService).sendMessage(eq(BotPlatform.MAX), eq(CHAT_ID), anyString());
        verify(sendPhotoService, never()).sendPhoto(any(), anyLong(), any());
    }

    private User givenUser(BotPlatform platform) {
        User user = User.builder().setId(1L).setPlatform(platform).setExternalId(7L).build();
        when(userService.getOrCreateUser(any())).thenReturn(user);
        return user;
    }

    private void givenBalance(User user, Long balance) {
        when(tokenBalanceService.findOrCreateTokenBalanceForUser(user))
            .thenReturn(TokenBalance.builder().user(user).balance(balance).build());
    }

    private static GetOrCreateUserCommand command(BotPlatform platform) {
        return GetOrCreateUserCommand.builder().setPlatform(platform).setExternalId(7L).build();
    }

    private static Message message(String text) {
        return Message.builder().message(text).chatId(CHAT_ID).build();
    }
}
