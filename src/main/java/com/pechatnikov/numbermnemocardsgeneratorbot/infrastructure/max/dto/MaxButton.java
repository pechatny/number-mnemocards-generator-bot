package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Кнопка inline-клавиатуры ({@code CallbackButton}). Payload — до 1024 символов. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaxButton {
    public static final int MAX_PAYLOAD_LENGTH = 1024;

    private String type;
    private String text;
    private String payload;

    public static MaxButton callback(String text, String payload) {
        if (payload != null && payload.length() > MAX_PAYLOAD_LENGTH) {
            throw new IllegalArgumentException("Payload кнопки длиннее " + MAX_PAYLOAD_LENGTH + " символов");
        }
        return new MaxButton("callback", text, payload);
    }
}
