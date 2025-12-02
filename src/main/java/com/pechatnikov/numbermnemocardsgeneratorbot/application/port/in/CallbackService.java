package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Callback;

public interface CallbackService {
    void process(Callback callback);
}
