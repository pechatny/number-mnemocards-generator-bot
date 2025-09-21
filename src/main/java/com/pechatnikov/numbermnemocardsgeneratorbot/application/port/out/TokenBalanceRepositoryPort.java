package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.out;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.TokenBalance;

import java.util.Optional;

public interface TokenBalanceRepositoryPort {
    TokenBalance save(TokenBalance tokenBalance);
    Optional<TokenBalance> findByUserId(Long userId);
}
