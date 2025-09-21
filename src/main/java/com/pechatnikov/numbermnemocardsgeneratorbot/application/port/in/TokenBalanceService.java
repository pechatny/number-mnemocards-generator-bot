package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.TokenTransaction;

public interface TokenBalanceService {
    void increase(TokenTransaction tokenTransaction);
    void decrease(TokenTransaction tokenTransaction);
}
