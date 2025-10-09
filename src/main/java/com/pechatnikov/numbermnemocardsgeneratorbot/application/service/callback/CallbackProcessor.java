package com.pechatnikov.numbermnemocardsgeneratorbot.application.service.callback;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Callback;
import org.springframework.beans.factory.annotation.Autowired;

public interface CallbackProcessor {
    CallbackType getCallBacktype();

    void process(Callback callback);

    @Autowired
    default void selfRegister(CallbackRegistry callbackRegistry) {
        callbackRegistry.register(this);
    }
}
