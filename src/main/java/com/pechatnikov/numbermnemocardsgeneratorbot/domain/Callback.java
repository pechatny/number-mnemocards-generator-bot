package com.pechatnikov.numbermnemocardsgeneratorbot.domain;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.callback.CallbackType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Callback {
    CallbackType type;
    BotPlatform platform;
    Long externalUserId;
    Long chatId;
    String messageId;
    String callbackQueryId;
    String value;
}
