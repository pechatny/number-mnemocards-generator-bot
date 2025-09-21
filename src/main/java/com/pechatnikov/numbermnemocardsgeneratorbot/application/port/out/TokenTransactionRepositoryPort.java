package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.TokenTransaction;

public interface TokenTransactionRepositoryPort {
    TokenTransaction save(TokenTransaction tokenTransaction);
}
