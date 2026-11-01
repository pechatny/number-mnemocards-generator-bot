package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Тело нового сообщения ({@code NewMessageBody}). Текст — до 4000 символов. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaxNewMessageBody {
    public static final String FORMAT_MARKDOWN = "markdown";

    private String text;
    private List<MaxAttachmentRequest> attachments;
    private String format;
}
