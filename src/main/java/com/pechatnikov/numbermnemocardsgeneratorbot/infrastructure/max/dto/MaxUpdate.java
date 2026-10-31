package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Событие Max ({@code Update}). Вместо иерархии по {@code update_type} одно плоское DTO
 * с полями нужных нам типов: {@code message_created}, {@code message_callback}, {@code bot_started}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MaxUpdate {
    public static final String MESSAGE_CREATED = "message_created";
    public static final String MESSAGE_CALLBACK = "message_callback";
    public static final String BOT_STARTED = "bot_started";

    private String updateType;
    private Long timestamp;
    /** message_created, message_callback */
    private MaxMessage message;
    /** message_callback */
    private MaxCallback callback;
    /** bot_started */
    private Long chatId;
    /** bot_started */
    private MaxUser user;
}
