package com.pechatnikov.numbermnemocardsgeneratorbot.application.port.in;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.BotPlatform;
public class GetOrCreateUserCommand {
    private final BotPlatform platform;
    private final Long externalId;
    private final String username;
    private final String firstname;
    private final String lastname;

    private GetOrCreateUserCommand(BotPlatform platform, Long externalId, String username, String firstname, String lastname) {
        this.platform = platform;
        this.externalId = externalId;
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
    }

    public static GetOrCreateUserCommand.GetOrCreateUserCommandBuilder builder() {
        return new GetOrCreateUserCommand.GetOrCreateUserCommandBuilder();
    }

    public static final class GetOrCreateUserCommandBuilder {
        private BotPlatform platform;
        private Long externalId;
        private String username;
        private String firstname;
        private String lastname;

        public GetOrCreateUserCommand.GetOrCreateUserCommandBuilder setPlatform(BotPlatform platform) {
            this.platform = platform;
            return this;
        }

        public GetOrCreateUserCommand.GetOrCreateUserCommandBuilder setExternalId(Long externalId) {
            this.externalId = externalId;
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
            return new GetOrCreateUserCommand(platform, externalId, username, firstname, lastname);
        }
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
}
