package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.telegram.dto;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.callback.CallbackType;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class CallbackData {
    CallbackType type;
    String value;
}
