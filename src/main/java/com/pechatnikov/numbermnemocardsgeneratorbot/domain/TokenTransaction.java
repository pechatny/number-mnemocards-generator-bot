package com.pechatnikov.numbermnemocardsgeneratorbot.domain;

public class TokenTransaction {
    private final Long id;
    private final User user;
    private final Message message;
    private final Long count;

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Message getMessage() {
        return message;
    }

    public Long getCount() {
        return count;
    }

    private TokenTransaction(TokenTransactionBuilder builder) {
        id = builder.id;
        user = builder.user;
        message = builder.message;
        count = builder.count;
    }

    public static TokenTransactionBuilder builder() {
        return new TokenTransactionBuilder();
    }

    public static final class TokenTransactionBuilder {
        private Long id;
        private User user;
        private Message message;
        private Long count;

        private TokenTransactionBuilder() {
        }

        public TokenTransactionBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public TokenTransactionBuilder user(User user) {
            this.user = user;
            return this;
        }

        public TokenTransactionBuilder message(Message message) {
            this.message = message;
            return this;
        }

        public TokenTransactionBuilder count(Long count) {
            this.count = count;
            return this;
        }

        public TokenTransaction build() {
            return new TokenTransaction(this);
        }
    }
}
