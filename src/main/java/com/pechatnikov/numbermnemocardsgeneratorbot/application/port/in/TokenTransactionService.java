package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.TokenTransaction;

public interface TokenTransactionService {
    TokenTransaction save(TokenTransaction tokenTransaction);
}
