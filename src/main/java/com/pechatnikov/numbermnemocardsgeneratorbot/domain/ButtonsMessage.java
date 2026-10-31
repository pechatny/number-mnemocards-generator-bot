package com.pechatnikov.numbermnemocardsgeneratorbot.domain;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ButtonsMessage {
    BotPlatform platform;
    String chatId;
    String message;
    List<ButtonData> buttons;
}
