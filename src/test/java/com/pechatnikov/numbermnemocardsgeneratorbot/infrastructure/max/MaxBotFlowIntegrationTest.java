package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max;

import com.pechatnikov.numbermnemocardsgeneratorbot.BaseIntegrationTest;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.PaymentAvailabilityPort;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxUpdate;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxUpdateList;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.jpa.SpringDataOrderRepository;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.jpa.SpringDataUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;

/**
 * Сквозные сценарии Max на реальном контексте и БД: событие из /updates → application-слой → ответ в Max.
 * <p>
 * Поллер работает в своём потоке, поэтому мок {@link MaxClient} не перестраивается во время теста
 * (стабинг Mockito не потокобезопасен): события отдаются из очереди ответом по умолчанию.
 */
@TestPropertySource(properties = {
    "bot.max.enabled=true",
    "bot.max.token=test-token",
    "bot.max.polling.error-backoff-millis=50"
})
class MaxBotFlowIntegrationTest extends BaseIntegrationTest {
    private static final long TIMEOUT_MILLIS = 10_000;
    private static final BlockingQueue<MaxUpdate> UPDATES = new LinkedBlockingQueue<>();

    @TestConfiguration
    static class FakeMaxApi {
        @Bean
        @Primary
        MaxClient fakeMaxClient() {
            return Mockito.mock(MaxClient.class, invocation -> {
                if (invocation.getMethod().getName().equals("getUpdates")) {
                    // Как настоящий long polling: ждём событие, а не отвечаем мгновенно
                    MaxUpdate update = UPDATES.poll(50, TimeUnit.MILLISECONDS);
                    return MaxUpdateList.builder().updates(update == null ? List.of() : List.of(update)).marker(1L).build();
                }
                return null;
            });
        }
    }

    @Autowired
    private MaxClient maxClient;

    @Autowired
    private SpringDataUserRepository userRepository;

    @Autowired
    private SpringDataOrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        UPDATES.clear();
        clearInvocations(maxClient);
    }

    @Test
    void shouldSendWelcomeOnStartButton() {
        deliver(MaxTestData.botStarted());

        verify(maxClient, timeout(TIMEOUT_MILLIS)).sendText(eq(MaxTestData.CHAT_ID), startsWith("**Добро пожаловать"));
    }

    @Test
    void shouldGenerateCardForNumberFromMaxUser() {
        long userId = 900_001L;
        var update = MaxTestData.messageCreated("12");
        update.getMessage().getSender().setUserId(userId);

        deliver(update);

        verify(maxClient, timeout(TIMEOUT_MILLIS)).sendImage(eq(MaxTestData.CHAT_ID), any(File.class));
        assertThat(userRepository.findByPlatformAndExternalId(BotPlatform.MAX, userId)).isPresent();
        assertThat(userRepository.findByPlatformAndExternalId(BotPlatform.TELEGRAM, userId)).isEmpty();
    }

    @Test
    void shouldReplyBalanceToNewMaxUser() {
        var update = MaxTestData.messageCreated("/balance");
        update.getMessage().getSender().setUserId(900_002L);

        deliver(update);

        verify(maxClient, timeout(TIMEOUT_MILLIS)).sendText(MaxTestData.CHAT_ID, "Ваш баланс цифр 100 шт.");
    }

    @Test
    void shouldReplyPaymentsUnavailableOnBalanceRefillCommand() {
        deliver(MaxTestData.messageCreated("/balance_refill"));

        verify(maxClient, timeout(TIMEOUT_MILLIS)).sendText(MaxTestData.CHAT_ID, PaymentAvailabilityPort.PAYMENTS_UNAVAILABLE_MESSAGE);
        verify(maxClient, never()).sendButtons(anyLong(), anyString(), anyList());
    }

    @Test
    void shouldNotCreateOrderWhenMaxUserPressesPriceButton() {
        long ordersBefore = orderRepository.count();

        deliver(MaxTestData.callback("{\"type\":\"CREATE_INVOICE\",\"value\":\"100\"}"));

        verify(maxClient, timeout(TIMEOUT_MILLIS)).answerCallback("cb-1", PaymentAvailabilityPort.PAYMENTS_UNAVAILABLE_MESSAGE);
        assertThat(orderRepository.count()).isEqualTo(ordersBefore);
    }

    private static void deliver(MaxUpdate update) {
        UPDATES.add(update);
    }
}
