package com.pechatnikov.numbermnemocardsgeneratorbot.domain;

public class Message {
    private final Long id;
    private final User user;
    private final String message;

    private Message(Long id, User user, String message) {
        this.id = id;
        this.user = user;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }

    public static MessageBuilder builder() {
        return new MessageBuilder();
    }

    public static final class MessageBuilder {
        private Long id;
        private User user;
        private String message;

        private MessageBuilder() {}

        public MessageBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public MessageBuilder user(User user) {
            this.user = user;
            return this;
        }

        public MessageBuilder message(String message) {
            this.message = message;
            return this;
        }

        public Message build() {
            return new Message(this.id, this.user, this.message);
        }
    }
}
