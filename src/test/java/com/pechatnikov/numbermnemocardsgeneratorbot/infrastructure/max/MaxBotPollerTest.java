package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max;

import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration.MaxBotProperties;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxUpdate;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxUpdateList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

class MaxBotPollerTest {
    private MaxClient maxClient;
    private MaxUpdateDispatcher dispatcher;
    private MaxBotPoller poller;

    @BeforeEach
    void setUp() {
        maxClient = mock(MaxClient.class);
        dispatcher = mock(MaxUpdateDispatcher.class);
        MaxBotProperties properties = new MaxBotProperties();
        properties.getPolling().setTimeoutSeconds(30);
        properties.getPolling().setLimit(100);
        properties.getPolling().setErrorBackoffMillis(10);
        properties.getPolling().setMaxErrorBackoffMillis(20);
        properties.getPolling().setWorkerThreads(2);
        poller = new MaxBotPoller(maxClient, dispatcher, properties);
    }

    @AfterEach
    void tearDown() {
        poller.stop();
    }

    @Test
    void shouldDispatchUpdatesAndPassMarkerToNextPoll() throws Exception {
        MaxUpdate first = MaxTestData.messageCreated("12");
        MaxUpdate second = MaxTestData.botStarted();
        CountDownLatch secondPoll = new CountDownLatch(1);
        when(maxClient.getUpdates(isNull(), eq(30), eq(100)))
            .thenReturn(MaxUpdateList.builder().updates(List.of(first, second)).marker(5L).build());
        when(maxClient.getUpdates(eq(5L), anyInt(), anyInt())).thenAnswer(invocation -> {
            secondPoll.countDown();
            return MaxUpdateList.builder().updates(List.of()).marker(5L).build();
        });

        poller.start();

        assertThat(secondPoll.await(5, TimeUnit.SECONDS)).isTrue();
        verify(dispatcher, timeout(5000)).dispatch(first);
        verify(dispatcher, timeout(5000)).dispatch(second);
        assertThat(poller.getMarker()).isEqualTo(5L);
    }

    @Test
    void shouldKeepPollingAfterError() throws Exception {
        CountDownLatch recovered = new CountDownLatch(1);
        when(maxClient.getUpdates(any(), anyInt(), anyInt()))
            .thenThrow(new MaxApiException("network down", null))
            .thenAnswer(invocation -> {
                recovered.countDown();
                return MaxUpdateList.builder().updates(List.of()).build();
            });

        poller.start();

        assertThat(recovered.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(poller.isRunning()).isTrue();
    }

    @Test
    void shouldContinueWhenDispatcherFails() throws Exception {
        MaxUpdate bad = MaxTestData.messageCreated("1");
        MaxUpdate good = MaxTestData.messageCreated("2");
        doThrow(new RuntimeException("boom")).when(dispatcher).dispatch(bad);
        when(maxClient.getUpdates(any(), anyInt(), anyInt()))
            .thenReturn(MaxUpdateList.builder().updates(List.of(bad)).marker(1L).build())
            .thenReturn(MaxUpdateList.builder().updates(List.of(good)).marker(2L).build())
            .thenReturn(MaxUpdateList.builder().updates(List.of()).marker(2L).build());

        poller.start();

        verify(dispatcher, timeout(5000)).dispatch(good);
    }

    @Test
    void shouldStopPolling() throws Exception {
        CountDownLatch polled = new CountDownLatch(1);
        when(maxClient.getUpdates(any(), anyInt(), anyInt())).thenAnswer(invocation -> {
            polled.countDown();
            return MaxUpdateList.builder().updates(List.of()).build();
        });
        poller.start();
        assertThat(polled.await(5, TimeUnit.SECONDS)).isTrue();

        poller.stop();

        assertThat(poller.isRunning()).isFalse();
        Thread.sleep(100);
        clearInvocations(maxClient);
        Thread.sleep(100);
        verifyNoInteractions(maxClient);
    }

    @Test
    void shouldBeIdempotentOnRepeatedStartAndStop() {
        when(maxClient.getUpdates(any(), anyInt(), anyInt()))
            .thenReturn(MaxUpdateList.builder().updates(List.of()).build());

        poller.start();
        poller.start();
        poller.stop();
        poller.stop();

        assertThat(poller.isRunning()).isFalse();
    }

    @Test
    void shouldKeepJvmAliveWithNonDaemonPollingThread() throws Exception {
        CountDownLatch polled = new CountDownLatch(1);
        when(maxClient.getUpdates(any(), anyInt(), anyInt())).thenAnswer(invocation -> {
            polled.countDown();
            Thread.sleep(20);
            return MaxUpdateList.builder().updates(List.of()).build();
        });

        poller.start();
        assertThat(polled.await(5, TimeUnit.SECONDS)).isTrue();

        assertThat(Thread.getAllStackTraces().keySet())
            .filteredOn(thread -> thread.getName().equals(MaxBotPoller.POLLING_THREAD_NAME))
            .isNotEmpty()
            .allMatch(thread -> !thread.isDaemon());
    }
}

