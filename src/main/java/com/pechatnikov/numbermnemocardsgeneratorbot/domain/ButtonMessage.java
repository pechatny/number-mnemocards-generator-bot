package com.pechatnikov.numbermnemocardsgeneratorbot.domain;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.callback.CallbackType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ButtonMessage {
    BotPlatform platform;
    Long chatId;
    String message;
    String buttonText;
    CallbackType callbackType;
}
