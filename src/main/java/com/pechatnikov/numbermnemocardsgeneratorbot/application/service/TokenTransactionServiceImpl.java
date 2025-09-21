package com.pechatnikov.numbermnemocardsgeneratorbot.application.service;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.TokenTransactionService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.TokenTransactionRepositoryPort;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.TokenTransaction;
import org.springframework.stereotype.Service;

@Service
public class TokenTransactionServiceImpl implements TokenTransactionService {
    private final TokenTransactionRepositoryPort tokenTransactionRepositoryPort;

    public TokenTransactionServiceImpl(TokenTransactionRepositoryPort tokenTransactionRepositoryPort) {
        this.tokenTransactionRepositoryPort = tokenTransactionRepositoryPort;
    }

    @Override
    public TokenTransaction save(TokenTransaction tokenTransaction) {
        return tokenTransactionRepositoryPort.save(tokenTransaction);
    }
}
