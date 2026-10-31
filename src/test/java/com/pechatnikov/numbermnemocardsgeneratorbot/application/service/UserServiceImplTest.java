package com.pechatnikov.numbermnemocardsgeneratorbot.application.service;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.mapper.GetOrGetOrCreateUserCommandMapperImpl;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.GetOrCreateUserCommand;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.UserRepositoryPort;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.validation.ValidationException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceImplTest {
    private UserRepositoryPort userRepositoryPort;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepositoryPort = mock(UserRepositoryPort.class);
        userService = new UserServiceImpl(userRepositoryPort, new GetOrGetOrCreateUserCommandMapperImpl());
    }

    @Test
    void shouldReturnExistingUserOfSamePlatform() {
        User existing = User.builder().setId(1L).setPlatform(BotPlatform.MAX).setExternalId(10L).build();
        when(userRepositoryPort.findByPlatformAndExternalId(BotPlatform.MAX, 10L)).thenReturn(Optional.of(existing));

        User user = userService.getOrCreateUser(command(BotPlatform.MAX, 10L));

        assertThat(user).isSameAs(existing);
        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    void shouldCreateUserWithPlatformWhenNotFound() {
        when(userRepositoryPort.findByPlatformAndExternalId(BotPlatform.MAX, 10L)).thenReturn(Optional.empty());
        when(userRepositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        userService.getOrCreateUser(command(BotPlatform.MAX, 10L));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepositoryPort).save(captor.capture());
        assertThat(captor.getValue().getPlatform()).isEqualTo(BotPlatform.MAX);
        assertThat(captor.getValue().getExternalId()).isEqualTo(10L);
        assertThat(captor.getValue().getUsername()).isNull();
    }

    @Test
    void shouldRejectCommandWithoutPlatform() {
        when(userRepositoryPort.findByPlatformAndExternalId(null, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getOrCreateUser(command(null, 10L)))
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldRejectCommandWithoutExternalId() {
        when(userRepositoryPort.findByPlatformAndExternalId(BotPlatform.TELEGRAM, null)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getOrCreateUser(command(BotPlatform.TELEGRAM, null)))
            .isInstanceOf(ValidationException.class);
    }

    private static GetOrCreateUserCommand command(BotPlatform platform, Long externalId) {
        return GetOrCreateUserCommand.builder()
            .setPlatform(platform)
            .setExternalId(externalId)
            .setFirstname("firstname")
            .build();
    }
}
