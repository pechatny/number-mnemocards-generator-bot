package com.pechatnikov.numbermnemocardsgeneratorbot.domain;

public class TokenBalance {
    private final Long id;
    private final User user;
    private final Long balance;

    private TokenBalance(TokenBalanceBuilder builder) {
        id = builder.id;
        user = builder.user;
        balance = builder.balance;
    }

    public static TokenBalanceBuilder toBuilder(TokenBalance copy) {
        TokenBalanceBuilder builder = new TokenBalanceBuilder();
        builder.id = copy.getId();
        builder.user = copy.getUser();
        builder.balance = copy.getBalance();

        return builder;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Long getBalance() {
        return balance;
    }

    public static TokenBalanceBuilder builder() {
        return new TokenBalanceBuilder();
    }

    public static final class TokenBalanceBuilder {
        private Long id;
        private User user;
        private Long balance;

        private TokenBalanceBuilder() {
        }

        public TokenBalanceBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public TokenBalanceBuilder user(User user) {
            this.user = user;
            return this;
        }

        public TokenBalanceBuilder balance(Long balance) {
            this.balance = balance;
            return this;
        }

        public TokenBalance build() {
            return new TokenBalance(this);
        }
    }
}
