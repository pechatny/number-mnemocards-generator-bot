package com.pechatnikov.numbermnemocardsgeneratorbot.domain;

public class Message {
    private final Long id;
    private final User user;
    private final String message;
    private final Long chatId;

    private Message(Long id, User user, String message, Long chatId) {
        this.id = id;
        this.user = user;
        this.message = message;
        this.chatId = chatId;
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

    public Long getChatId() {
        return chatId;
    }

    public static MessageBuilder builder() {
        return new MessageBuilder();
    }

    public static final class MessageBuilder {
        private Long id;
        private User user;
        private String message;
        private Long chatId;

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

        public MessageBuilder chatId(Long chatId) {
            this.chatId = chatId;
            return this;
        }

        public Message build() {
            return new Message(this.id, this.user, this.message, this.chatId);
        }
    }
}
