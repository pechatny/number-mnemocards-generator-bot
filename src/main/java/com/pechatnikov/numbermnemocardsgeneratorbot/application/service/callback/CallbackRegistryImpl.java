package com.pechatnikov.numbermnemocardsgeneratorbot.application.service.callback;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class CallbackRegistryImpl implements CallbackRegistry {
    private static final Map<CallbackType, CallbackProcessor> REGISTRY = new HashMap<>();

    @Override
    public CallbackProcessor getCallbackProcessor(CallbackType callbackType) {
        return Optional.of(REGISTRY.get(callbackType)).get();
    }

    @Override
    public void register(CallbackProcessor callbackProcessor) {
       REGISTRY.put(callbackProcessor.getCallBacktype(), callbackProcessor);
    }
}
