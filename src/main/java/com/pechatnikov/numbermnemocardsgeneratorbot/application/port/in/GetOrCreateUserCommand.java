package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in;

public class GetOrCreateUserCommand {
    private final Long telegramId;
    private final String username;
    private final String firstname;
    private final String lastname;

    private GetOrCreateUserCommand(Long telegramId, String username, String firstname, String lastname) {
        this.telegramId = telegramId;
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
    }

    public static GetOrCreateUserCommand.GetOrCreateUserCommandBuilder builder() {
        return new GetOrCreateUserCommand.GetOrCreateUserCommandBuilder();
    }

    public static final class GetOrCreateUserCommandBuilder {
        private Long telegramId;
        private String username;
        private String firstname;
        private String lastname;

        public GetOrCreateUserCommand.GetOrCreateUserCommandBuilder setTelegramId(Long telegramId) {
            this.telegramId = telegramId;
            return this;
        }

        public GetOrCreateUserCommand.GetOrCreateUserCommandBuilder setUsername(String username) {
            this.username = username;
            return this;
        }

        public GetOrCreateUserCommand.GetOrCreateUserCommandBuilder setFirstname(String firstname) {
            this.firstname = firstname;
            return this;
        }

        public GetOrCreateUserCommand.GetOrCreateUserCommandBuilder setLastname(String lastname) {
            this.lastname = lastname;
            return this;
        }

        public GetOrCreateUserCommand build() {
            return new GetOrCreateUserCommand(telegramId, username, firstname, lastname);
        }
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
}
