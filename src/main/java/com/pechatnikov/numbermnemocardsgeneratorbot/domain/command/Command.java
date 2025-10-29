package com.pechatnikov.numbermnemocardsgeneratorbot.domain.command;

public enum Command {
    START("/start"),
    BALANCE("/balance");

    private final String value;

    Command(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
