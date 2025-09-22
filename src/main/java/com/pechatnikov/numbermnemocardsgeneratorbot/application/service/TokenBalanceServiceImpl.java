package com.pechatnikov.numbermnemocardsgeneratorbot.application.service;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in.TokenBalanceService;
import com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out.TokenBalanceRepositoryPort;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.TokenBalance;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.TokenTransaction;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenBalanceServiceImpl implements TokenBalanceService {
    public static final long NEW_USER_INIT_TOKENS = 100L;

    private final TokenBalanceRepositoryPort tokenBalanceRepositoryPort;

    public TokenBalanceServiceImpl(TokenBalanceRepositoryPort tokenBalanceRepositoryPort) {
        this.tokenBalanceRepositoryPort = tokenBalanceRepositoryPort;
    }

    private void save(TokenBalance tokenBalance) {
        tokenBalanceRepositoryPort.save(tokenBalance);
    }

    @Override
    @Transactional
    public TokenBalance findOrCreateTokenBalanceForUser(User user) {
        return tokenBalanceRepositoryPort
            .findByUserId(user.getId())
            .orElse(newTokenBalance(user));
    }

    @Override
    public void increase(TokenTransaction tokenTransaction) {
        TokenBalance tokenBalance = getTokenBalanceByTransaction(tokenTransaction);

        Long newTokenBalance = tokenBalance.getBalance() + tokenTransaction.getCount();

        TokenBalance tokenBalanceForUpdate = TokenBalance.toBuilder(tokenBalance).balance(newTokenBalance).build();

        save(tokenBalanceForUpdate);
    }

    @Override
    @Transactional
    public void decrease(TokenTransaction tokenTransaction) {
        TokenBalance tokenBalance = getTokenBalanceByTransaction(tokenTransaction);

        Long newTokenBalance = tokenBalance.getBalance() - tokenTransaction.getCount();

        TokenBalance tokenBalanceForUpdate = TokenBalance.toBuilder(tokenBalance).balance(newTokenBalance).build();

        save(tokenBalanceForUpdate);
    }

    private TokenBalance getTokenBalanceByTransaction(TokenTransaction tokenTransaction) {
        return tokenBalanceRepositoryPort
            .findByUserId(tokenTransaction.getUser().getId())
            .orElse(newTokenBalance(tokenTransaction.getUser()));
    }

    private TokenBalance newTokenBalance(User user) {
        return TokenBalance.builder()
            .balance(NEW_USER_INIT_TOKENS)
            .user(user)
            .build();
    }
}
