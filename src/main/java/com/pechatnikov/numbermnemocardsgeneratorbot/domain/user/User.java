package com.pechatnikov.numbermnemocardsgeneratorbot.domain.user;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
public class User {
    private final Long id;
    private final BotPlatform platform;
    private final Long externalId;
    private final String username;
    private final String firstname;
    private final String lastname;
    private final UserState state;

    private User(Long id, BotPlatform platform, Long externalId, String username, String firstname, String lastname, UserState state) {
        this.id = id;
        this.platform = platform;
        this.externalId = externalId;
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
        private BotPlatform platform;
        private Long externalId;
        private String username;
        private String firstname;
        private String lastname;
        private UserState state;

        public UserBuilder setId(Long id) {
            this.id = id;
            return this;
        }

        public UserBuilder setPlatform(BotPlatform platform) {
            this.platform = platform;
            return this;
        }

        public UserBuilder setExternalId(Long externalId) {
            this.externalId = externalId;
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
            return new User(id, platform, externalId, username, firstname, lastname, state);
        }
    }

    public Long getId() {
        return id;
    }

    public BotPlatform getPlatform() {
        return platform;
    }

    public Long getExternalId() {
        return externalId;
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