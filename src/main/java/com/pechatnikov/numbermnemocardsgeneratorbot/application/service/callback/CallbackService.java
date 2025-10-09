package com.pechatnikov.numbermnemocardsgeneratorbot.application.service.callback;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.Callback;

public interface CallbackService {
    void process(Callback callback);
}
