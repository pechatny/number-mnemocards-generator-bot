package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max;

import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration.ConditionalOnMaxBot;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration.MaxBotProperties;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxUpdate;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxUpdateList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Long polling {@code GET /updates}: один поток опрашивает Max, события обрабатываются в пуле потоков.
 * {@code marker} из ответа передаётся в следующий запрос и подтверждает полученные события.
 */
@Slf4j
@ConditionalOnMaxBot
@Component
public class MaxBotPoller implements SmartLifecycle {
    static final String POLLING_THREAD_NAME = "max-poller";

    private final MaxClient maxClient;
    private final MaxUpdateDispatcher dispatcher;
    private final MaxBotProperties.Polling polling;

    private volatile boolean running;
    private volatile Thread pollingThread;
    private ExecutorService executorService;
    private volatile Long marker;

    public MaxBotPoller(MaxClient maxClient, MaxUpdateDispatcher dispatcher, MaxBotProperties properties) {
        this.maxClient = maxClient;
        this.dispatcher = dispatcher;
        this.polling = properties.getPolling();
    }

    @Override
    public synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        executorService = Executors.newFixedThreadPool(polling.getWorkerThreads());
        pollingThread = new Thread(this::pollLoop, POLLING_THREAD_NAME);
        // Не daemon: без веб-сервера и Telegram-бота именно этот поток держит JVM запущенной
        pollingThread.setDaemon(false);
        pollingThread.start();
        log.info("Max bot polling started");
    }

    @Override
    public synchronized void stop() {
        if (!running) {
            return;
        }
        log.info("Max bot is shutting down...");
        running = false;
        pollingThread.interrupt();
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    private void pollLoop() {
        long backoff = polling.getErrorBackoffMillis();
        while (running) {
            try {
                MaxUpdateList updates = maxClient.getUpdates(marker, polling.getTimeoutSeconds(), polling.getLimit());
                if (updates == null) {
                    log.warn("Empty response from Max /updates");
                    sleep(backoff);
                    continue;
                }
                updates.getUpdates().forEach(this::submit);
                if (updates.getMarker() != null) {
                    marker = updates.getMarker();
                }
                backoff = polling.getErrorBackoffMillis();
            } catch (Exception e) {
                if (!running) {
                    break;
                }
                log.error("Error polling Max updates, retry in {} ms", backoff, e);
                sleep(backoff);
                backoff = Math.min(backoff * 2, polling.getMaxErrorBackoffMillis());
            }
        }
        log.info("Max bot polling stopped");
    }

    private void submit(MaxUpdate update) {
        try {
            executorService.submit(() -> dispatcher.dispatch(update));
        } catch (RejectedExecutionException e) {
            log.warn("Max update skipped, bot is shutting down: {}", update);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            running = false;
        }
    }

    Long getMarker() {
        return marker;
    }
}
