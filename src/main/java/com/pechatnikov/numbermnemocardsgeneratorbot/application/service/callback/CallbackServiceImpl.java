package com.pechatnikov.numbermnemocardsgeneratorbot.application.service.callback;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.CallbackService;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Callback;
import org.springframework.stereotype.Service;

@Service
public class CallbackServiceImpl implements CallbackService {
    private final CallbackRegistry callbackRegistry;

    public CallbackServiceImpl(CallbackRegistry callbackRegistry) {
        this.callbackRegistry = callbackRegistry;
    }

    @Override
    public void process(Callback callback) {
        CallbackProcessor callbackProcessor = callbackRegistry.getCallbackProcessor(callback.getType());
        callbackProcessor.process(callback);
    }
}
