package com.pechatnikov.numbermnemocardsgeneratorbot.domain;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.callback.CallbackType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Callback {
    CallbackType type;
    Long telegramId;
    Long chatId;
    Integer messageId;
    String callbackQueryId;
    String value;
}
