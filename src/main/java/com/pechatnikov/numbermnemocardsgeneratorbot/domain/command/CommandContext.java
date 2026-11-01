package com.pechatnikov.numbermnemocardsgeneratorbot.domain.command;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CommandContext {
    BotPlatform platform;
    Long externalUserId;
    Long chatId;
}
