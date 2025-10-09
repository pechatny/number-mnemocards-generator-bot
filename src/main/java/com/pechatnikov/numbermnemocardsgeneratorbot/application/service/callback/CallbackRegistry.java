package com.pechatnikov.numbermnemocardsgeneratorbot.application.service.callback;

public interface CallbackRegistry {
    CallbackProcessor getCallbackProcessor(CallbackType callbackType);

    void register(CallbackProcessor callbackProcessor);
}
