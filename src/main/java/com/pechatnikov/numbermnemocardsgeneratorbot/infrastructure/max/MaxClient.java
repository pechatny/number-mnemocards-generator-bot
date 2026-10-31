package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration.MaxBotProperties;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxAttachmentRequest;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxBotCommand;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxButton;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxCallbackAnswer;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxError;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxNewMessageBody;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxUpdateList;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxUploadEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Тонкий клиент Max Bot API (https://github.com/max-messenger/api-schema) — только методы, нужные боту.
 * <p>
 * {@code apiRestTemplate} настроен на {@code bot.max.base-url} и передаёт токен в заголовке {@code Authorization}.
 * {@code uploadRestTemplate} — без токена: файл загружается на сторонний URL, выданный {@code POST /uploads}.
 */
@Slf4j
public class MaxClient {
    static final String ATTACHMENT_NOT_READY = "attachment.not.ready";
    private static final String UPDATE_TYPES = "message_created,message_callback,bot_started";

    private final RestTemplate apiRestTemplate;
    private final RestTemplate uploadRestTemplate;
    private final ObjectMapper objectMapper;
    private final MaxBotProperties.Upload uploadProperties;

    public MaxClient(
        RestTemplate apiRestTemplate,
        RestTemplate uploadRestTemplate,
        ObjectMapper objectMapper,
        MaxBotProperties properties
    ) {
        this.apiRestTemplate = apiRestTemplate;
        this.uploadRestTemplate = uploadRestTemplate;
        this.objectMapper = objectMapper;
        this.uploadProperties = properties.getUpload();
    }

    /** {@code GET /updates} — long polling. {@code marker = null} — получить все неподтверждённые события. */
    public MaxUpdateList getUpdates(Long marker, int timeoutSeconds, int limit) {
        if (marker == null) {
            return call(() -> apiRestTemplate.getForObject(
                "/updates?limit={limit}&timeout={timeout}&types={types}",
                MaxUpdateList.class, limit, timeoutSeconds, UPDATE_TYPES
            ));
        }
        return call(() -> apiRestTemplate.getForObject(
            "/updates?limit={limit}&timeout={timeout}&types={types}&marker={marker}",
            MaxUpdateList.class, limit, timeoutSeconds, UPDATE_TYPES, marker
        ));
    }

    public void sendText(Long chatId, String text) {
        sendMessage(chatId, MaxNewMessageBody.builder()
            .text(text)
            .format(MaxNewMessageBody.FORMAT_MARKDOWN)
            .build());
    }

    public void sendButtons(Long chatId, String text, List<List<MaxButton>> rows) {
        sendMessage(chatId, MaxNewMessageBody.builder()
            .text(text)
            .attachments(List.of(MaxAttachmentRequest.inlineKeyboard(rows)))
            .build());
    }

    /**
     * Загрузка картинки в три шага: получить URL загрузки, загрузить файл, отправить сообщение с вложением.
     * Пока сервер обрабатывает файл, отправка падает с {@code attachment.not.ready} — тогда повторяем.
     */
    public void sendImage(Long chatId, File image) {
        MaxUploadEndpoint endpoint = call(() -> apiRestTemplate.postForObject("/uploads?type=image", null, MaxUploadEndpoint.class));
        JsonNode uploadResult = upload(endpoint.getUrl(), image);

        MaxNewMessageBody body = MaxNewMessageBody.builder()
            .attachments(List.of(MaxAttachmentRequest.image(uploadResult)))
            .build();

        for (int attempt = 1; ; attempt++) {
            try {
                sendMessage(chatId, body);
                return;
            } catch (MaxApiException e) {
                if (!ATTACHMENT_NOT_READY.equals(e.getCode()) || attempt >= uploadProperties.getRetryAttempts()) {
                    throw e;
                }
                log.debug("Картинка ещё обрабатывается, повтор {} из {}", attempt, uploadProperties.getRetryAttempts());
                sleep(uploadProperties.getRetryDelayMillis());
            }
        }
    }

    /** {@code POST /answers} — всплывающее уведомление пользователю, нажавшему кнопку. */
    public void answerCallback(String callbackId, String notification) {
        call(() -> apiRestTemplate.postForObject(
            "/answers?callback_id={callbackId}", new MaxCallbackAnswer(notification), JsonNode.class, callbackId
        ));
    }

    /** {@code PATCH /me/commands} — меню команд бота, заменяет текущий список целиком. */
    public void setCommands(List<MaxBotCommand> commands) {
        call(() -> apiRestTemplate.exchange(
            "/me/commands", HttpMethod.PATCH, new HttpEntity<>(Map.of("commands", commands)), JsonNode.class
        ));
    }

    public void deleteMessage(String messageId) {
        call(() -> apiRestTemplate.exchange(
            "/messages?message_id={messageId}", HttpMethod.DELETE, null, JsonNode.class, messageId
        ));
    }

    private void sendMessage(Long chatId, MaxNewMessageBody body) {
        call(() -> apiRestTemplate.postForObject("/messages?chat_id={chatId}", body, JsonNode.class, chatId));
    }

    private JsonNode upload(String url, File file) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("data", new FileSystemResource(file));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // URL выдан сервером уже закодированным — передаём как URI, чтобы RestTemplate не кодировал его повторно
        return call(() -> uploadRestTemplate.postForObject(URI.create(url), new HttpEntity<>(form, headers), JsonNode.class));
    }

    private <T> T call(Supplier<T> request) {
        try {
            return request.get();
        } catch (RestClientResponseException e) {
            MaxError error = parseError(e.getResponseBodyAsString());
            throw new MaxApiException(e.getRawStatusCode(), error.getCode(), error.getMessage(), e);
        } catch (ResourceAccessException e) {
            throw new MaxApiException("Max API недоступен: " + e.getMessage(), e);
        }
    }

    private MaxError parseError(String body) {
        try {
            MaxError error = objectMapper.readValue(body, MaxError.class);
            return error != null ? error : new MaxError(null, body);
        } catch (Exception e) {
            return new MaxError(null, body);
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MaxApiException("Отправка картинки прервана", e);
        }
    }
}
