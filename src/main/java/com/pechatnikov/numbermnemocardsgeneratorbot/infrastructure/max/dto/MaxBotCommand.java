package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Команда в меню бота ({@code BotCommand}): имя без "/" до 64 символов, описание до 128. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaxBotCommand {
    private String name;
    private String description;
}
