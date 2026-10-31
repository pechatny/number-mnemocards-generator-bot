package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.configuration.MaxBotProperties;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxBotCommand;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxButton;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxUpdate;
import com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto.MaxUpdateList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MaxClientTest {
    private static final String BASE_URL = "https://max.test";
    private static final String UPLOAD_URL = "https://upload.test/upload.do?sig=a%2Fb";
    private static final String TOKEN = "secret-token";
    private static final String SUCCESS = "{\"success\":true}";

    private MockRestServiceServer api;
    private MockRestServiceServer uploads;
    private MaxBotProperties properties;
    private MaxClient client;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        RestTemplate apiRestTemplate = new RestTemplateBuilder()
            .uriTemplateHandler(MaxClientConfiguration.apiUriTemplateHandler(BASE_URL))
            .defaultHeader(HttpHeaders.AUTHORIZATION, TOKEN)
            .build();
        RestTemplate uploadRestTemplate = new RestTemplateBuilder().build();
        api = MockRestServiceServer.bindTo(apiRestTemplate).build();
        uploads = MockRestServiceServer.bindTo(uploadRestTemplate).build();

        properties = new MaxBotProperties();
        properties.getUpload().setRetryAttempts(3);
        properties.getUpload().setRetryDelayMillis(0);

        client = new MaxClient(apiRestTemplate, uploadRestTemplate, new ObjectMapper(), properties);
    }

    @Test
    void shouldGetUpdatesWithoutMarker() {
        api.expect(requestTo(startsWith(BASE_URL + "/updates")))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(HttpHeaders.AUTHORIZATION, TOKEN))
            .andExpect(queryParam("limit", "100"))
            .andExpect(queryParam("timeout", "30"))
            .andExpect(queryParam("types", "message_created%2Cmessage_callback%2Cbot_started"))
            .andExpect(request -> assertThat(request.getURI().getQuery()).doesNotContain("marker"))
            .andRespond(withSuccess(updatesJson(), MediaType.APPLICATION_JSON));

        MaxUpdateList result = client.getUpdates(null, 30, 100);

        api.verify();
        assertThat(result.getMarker()).isEqualTo(777L);
        assertThat(result.getUpdates()).hasSize(3);

        MaxUpdate created = result.getUpdates().get(0);
        assertThat(created.getUpdateType()).isEqualTo(MaxUpdate.MESSAGE_CREATED);
        assertThat(created.getMessage().getSender().getUserId()).isEqualTo(7L);
        assertThat(created.getMessage().getSender().getFirstName()).isEqualTo("Иван");
        assertThat(created.getMessage().getSender().getUsername()).isNull();
        assertThat(created.getMessage().getRecipient().getChatId()).isEqualTo(42L);
        assertThat(created.getMessage().getBody().getMid()).isEqualTo("mid.1");
        assertThat(created.getMessage().getBody().getText()).isEqualTo("1234");

        MaxUpdate callback = result.getUpdates().get(1);
        assertThat(callback.getUpdateType()).isEqualTo(MaxUpdate.MESSAGE_CALLBACK);
        assertThat(callback.getCallback().getCallbackId()).isEqualTo("cb-1");
        assertThat(callback.getCallback().getPayload()).isEqualTo("{\"type\":\"SHOW_PRICES\"}");
        assertThat(callback.getCallback().getUser().getUserId()).isEqualTo(7L);

        MaxUpdate started = result.getUpdates().get(2);
        assertThat(started.getUpdateType()).isEqualTo(MaxUpdate.BOT_STARTED);
        assertThat(started.getChatId()).isEqualTo(42L);
        assertThat(started.getUser().getUserId()).isEqualTo(7L);
    }

    @Test
    void shouldPassMarkerToNextPoll() {
        api.expect(requestTo(startsWith(BASE_URL + "/updates")))
            .andExpect(queryParam("marker", "777"))
            .andRespond(withSuccess("{\"updates\":[],\"marker\":778}", MediaType.APPLICATION_JSON));

        MaxUpdateList result = client.getUpdates(777L, 30, 100);

        api.verify();
        assertThat(result.getUpdates()).isEmpty();
        assertThat(result.getMarker()).isEqualTo(778L);
    }

    @Test
    void shouldSendMarkdownText() {
        api.expect(requestTo(BASE_URL + "/messages?chat_id=42"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.AUTHORIZATION, TOKEN))
            .andExpect(content().json("{\"text\":\"*hi*\",\"format\":\"markdown\"}", true))
            .andRespond(withSuccess(SUCCESS, MediaType.APPLICATION_JSON));

        client.sendText(42L, "*hi*");

        api.verify();
    }

    @Test
    void shouldSendInlineKeyboard() {
        api.expect(requestTo(BASE_URL + "/messages?chat_id=42"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(jsonPath("$.text").value("Выберите"))
            .andExpect(jsonPath("$.attachments[0].type").value("inline_keyboard"))
            .andExpect(jsonPath("$.attachments[0].payload.buttons[0][0].type").value("callback"))
            .andExpect(jsonPath("$.attachments[0].payload.buttons[0][0].text").value("60₽"))
            .andExpect(jsonPath("$.attachments[0].payload.buttons[0][0].payload").value("p1"))
            .andExpect(jsonPath("$.attachments[0].payload.buttons[1][0].text").value("100₽"))
            .andRespond(withSuccess(SUCCESS, MediaType.APPLICATION_JSON));

        client.sendButtons(42L, "Выберите", List.of(
            List.of(MaxButton.callback("60₽", "p1")),
            List.of(MaxButton.callback("100₽", "p2"))
        ));

        api.verify();
    }

    @Test
    void shouldUploadImageAndRetryWhileAttachmentNotReady() throws Exception {
        File image = givenImage();
        expectUploadUrl();
        expectUpload();
        api.expect(once(), requestTo(BASE_URL + "/messages?chat_id=42"))
            .andRespond(attachmentNotReady());
        api.expect(once(), requestTo(BASE_URL + "/messages?chat_id=42"))
            .andExpect(jsonPath("$.attachments[0].type").value("image"))
            .andExpect(jsonPath("$.attachments[0].payload.photos.p1.token").value("photo-token"))
            .andRespond(withSuccess(SUCCESS, MediaType.APPLICATION_JSON));

        client.sendImage(42L, image);

        api.verify();
        uploads.verify();
    }

    @Test
    void shouldFailWhenAttachmentNeverBecomesReady() throws Exception {
        File image = givenImage();
        expectUploadUrl();
        expectUpload();
        api.expect(times(3), requestTo(BASE_URL + "/messages?chat_id=42"))
            .andRespond(attachmentNotReady());

        assertThatThrownBy(() -> client.sendImage(42L, image))
            .isInstanceOfSatisfying(MaxApiException.class, e -> {
                assertThat(e.getStatus()).isEqualTo(400);
                assertThat(e.getCode()).isEqualTo(MaxClient.ATTACHMENT_NOT_READY);
            });
        api.verify();
    }

    @Test
    void shouldNotRetryOtherErrors() throws Exception {
        File image = givenImage();
        expectUploadUrl();
        expectUpload();
        api.expect(once(), requestTo(BASE_URL + "/messages?chat_id=42"))
            .andRespond(withStatus(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"chat.denied\",\"message\":\"denied\"}"));

        assertThatThrownBy(() -> client.sendImage(42L, image))
            .isInstanceOfSatisfying(MaxApiException.class, e -> assertThat(e.getCode()).isEqualTo("chat.denied"));
        api.verify();
    }

    @Test
    void shouldAnswerCallbackWithNotification() {
        api.expect(requestTo(BASE_URL + "/answers?callback_id=cb%2B1"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().json("{\"notification\":\"Готово\"}", true))
            .andRespond(withSuccess(SUCCESS, MediaType.APPLICATION_JSON));

        client.answerCallback("cb+1", "Готово");

        api.verify();
    }

    @Test
    void shouldSetBotCommands() {
        api.expect(requestTo(BASE_URL + "/me/commands"))
            .andExpect(method(HttpMethod.PATCH))
            .andExpect(header(HttpHeaders.AUTHORIZATION, TOKEN))
            .andExpect(content().json(
                "{\"commands\":[{\"name\":\"start\",\"description\":\"Как пользоваться ботом\"},{\"name\":\"balance\"}]}", true))
            .andRespond(withSuccess("{\"commands\":[]}", MediaType.APPLICATION_JSON));

        client.setCommands(List.of(
            new MaxBotCommand("start", "Как пользоваться ботом"),
            new MaxBotCommand("balance", null)
        ));

        api.verify();
    }

    @Test
    void shouldDeleteMessage() {
        api.expect(requestTo(BASE_URL + "/messages?message_id=mid.1"))
            .andExpect(method(HttpMethod.DELETE))
            .andExpect(header(HttpHeaders.AUTHORIZATION, TOKEN))
            .andRespond(withSuccess(SUCCESS, MediaType.APPLICATION_JSON));

        client.deleteMessage("mid.1");

        api.verify();
    }

    @Test
    void shouldWrapHttpErrorWithMaxErrorCode() {
        api.expect(requestTo(BASE_URL + "/messages?chat_id=42"))
            .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"verify.token\",\"message\":\"Invalid access_token\"}"));

        assertThatThrownBy(() -> client.sendText(42L, "hi"))
            .isInstanceOfSatisfying(MaxApiException.class, e -> {
                assertThat(e.getStatus()).isEqualTo(401);
                assertThat(e.getCode()).isEqualTo("verify.token");
                assertThat(e.getMessage()).contains("Invalid access_token");
            });
    }

    @Test
    void shouldWrapHttpErrorWithNonJsonBody() {
        api.expect(requestTo(BASE_URL + "/messages?chat_id=42"))
            .andRespond(withStatus(HttpStatus.BAD_GATEWAY).body("<html>bad gateway</html>"));

        assertThatThrownBy(() -> client.sendText(42L, "hi"))
            .isInstanceOfSatisfying(MaxApiException.class, e -> {
                assertThat(e.getStatus()).isEqualTo(502);
                assertThat(e.getCode()).isNull();
            });
    }

    @Test
    void shouldRejectTooLongButtonPayload() {
        assertThatThrownBy(() -> MaxButton.callback("text", "x".repeat(MaxButton.MAX_PAYLOAD_LENGTH + 1)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private File givenImage() throws Exception {
        Path image = tempDir.resolve("card.png");
        Files.write(image, new byte[]{1, 2, 3});
        return image.toFile();
    }

    private void expectUploadUrl() {
        api.expect(once(), requestTo(BASE_URL + "/uploads?type=image"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.AUTHORIZATION, TOKEN))
            .andRespond(withSuccess("{\"url\":\"" + UPLOAD_URL + "\"}", MediaType.APPLICATION_JSON));
    }

    private void expectUpload() {
        uploads.expect(once(), requestTo(UPLOAD_URL))
            .andExpect(method(HttpMethod.POST))
            .andExpect(headerDoesNotExist(HttpHeaders.AUTHORIZATION))
            .andExpect(header(HttpHeaders.CONTENT_TYPE, startsWith(MediaType.MULTIPART_FORM_DATA_VALUE)))
            .andRespond(withSuccess("{\"photos\":{\"p1\":{\"token\":\"photo-token\"}}}", MediaType.APPLICATION_JSON));
    }

    private static org.springframework.test.web.client.ResponseCreator attachmentNotReady() {
        return withStatus(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"code\":\"attachment.not.ready\",\"message\":\"Key: errors.process.attachment.file.not.processed\"}");
    }

    private static String updatesJson() {
        return "{\"updates\":["
            + "{\"update_type\":\"message_created\",\"timestamp\":1,"
            + "\"message\":{\"sender\":{\"user_id\":7,\"first_name\":\"Иван\",\"username\":null,\"is_bot\":false},"
            + "\"recipient\":{\"chat_id\":42,\"chat_type\":\"dialog\"},\"timestamp\":1,"
            + "\"body\":{\"mid\":\"mid.1\",\"seq\":1,\"text\":\"1234\"}},\"user_locale\":\"ru\"},"
            + "{\"update_type\":\"message_callback\",\"timestamp\":2,"
            + "\"callback\":{\"timestamp\":2,\"callback_id\":\"cb-1\",\"payload\":\"{\\\"type\\\":\\\"SHOW_PRICES\\\"}\","
            + "\"user\":{\"user_id\":7,\"first_name\":\"Иван\",\"is_bot\":false}},"
            + "\"message\":{\"recipient\":{\"chat_id\":42,\"chat_type\":\"dialog\"},\"timestamp\":2,"
            + "\"body\":{\"mid\":\"mid.2\",\"seq\":2}}},"
            + "{\"update_type\":\"bot_started\",\"timestamp\":3,\"chat_id\":42,"
            + "\"user\":{\"user_id\":7,\"first_name\":\"Иван\",\"is_bot\":false},\"some_new_field\":true}"
            + "],\"marker\":777}";
    }
}
