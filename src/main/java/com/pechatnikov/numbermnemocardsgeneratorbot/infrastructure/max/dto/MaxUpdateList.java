package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/** Страница событий long polling ({@code UpdateList}). {@code marker} передаётся в следующий запрос. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MaxUpdateList {
    @Builder.Default
    private List<MaxUpdate> updates = new ArrayList<>();
    private Long marker;
}
