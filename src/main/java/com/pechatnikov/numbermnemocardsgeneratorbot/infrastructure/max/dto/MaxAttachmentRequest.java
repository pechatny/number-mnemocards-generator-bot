package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/** Вложение исходящего сообщения ({@code AttachmentRequest}). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaxAttachmentRequest {
    private String type;
    private Object payload;

    /**
     * Картинка. {@code uploadResult} — JSON, который вернул сервер загрузки
     * (вида {@code {"photos": {...}}}), передаётся как есть.
     */
    public static MaxAttachmentRequest image(JsonNode uploadResult) {
        return new MaxAttachmentRequest("image", uploadResult);
    }

    public static MaxAttachmentRequest inlineKeyboard(List<List<MaxButton>> rows) {
        return new MaxAttachmentRequest("inline_keyboard", Map.of("buttons", rows));
    }
}
