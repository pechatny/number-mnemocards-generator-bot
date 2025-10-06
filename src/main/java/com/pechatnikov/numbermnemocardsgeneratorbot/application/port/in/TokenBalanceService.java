package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.TokenBalance;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.TokenTransaction;
import com.pechatnikov.numbermnemocardsgeneratorbot.domain.user.User;

public interface TokenBalanceService {
    TokenBalance findOrCreateTokenBalanceForUser(User user);
    void increase(TokenTransaction tokenTransaction);
    void decrease(TokenTransaction tokenTransaction);
}
