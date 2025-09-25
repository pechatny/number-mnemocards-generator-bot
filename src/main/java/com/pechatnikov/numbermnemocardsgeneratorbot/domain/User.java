package com.pechatnikov.numbermnemocardsgeneratorbot.domain;

public class User {
    private final Long id;
    private final Long telegramId;
    private final String username;
    private final String firstname;
    private final String lastname;
    private final UserState state;

    private User(Long id, Long telegramId, String username, String firstname, String lastname, UserState state) {
        this.id = id;
        this.telegramId = telegramId;
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.state = state;
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static final class UserBuilder {
        private Long id;
        private Long telegramId;
        private String username;
        private String firstname;
        private String lastname;
        private UserState state;

        public UserBuilder setId(Long id) {
            this.id = id;
            return this;
        }

        public UserBuilder setTelegramId(Long telegramId) {
            this.telegramId = telegramId;
            return this;
        }

        public UserBuilder setUsername(String username) {
            this.username = username;
            return this;
        }

        public UserBuilder setFirstname(String firstname) {
            this.firstname = firstname;
            return this;
        }

        public UserBuilder setLastname(String lastname) {
            this.lastname = lastname;
            return this;
        }

        public UserBuilder setState(UserState state) {
            this.state = state;
            return this;
        }

        public User build() {
            return new User(id, telegramId, username, firstname, lastname, state);
        }
    }

    public Long getId() {
        return id;
    }

    public Long getTelegramId() {
        return telegramId;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public UserState getState() {
        return state;
    }

}