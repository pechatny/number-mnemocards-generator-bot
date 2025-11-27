package com.pechatnikov.numbermnemocardsgeneratorbot.domain.command;

import java.util.HashMap;
import java.util.Map;

public enum Command {
    START("/start"),
    BALANCE("/balance"),
    BALANCE_REFILL("/balance_refill");

    private final String value;

    private static final Map<String, Command> VALUE_MAP = new HashMap<>();

    static {
        for (Command command : Command.values()) {
            VALUE_MAP.put(command.value, command);
        }
    }

    Command(String value) {
        this.value = value;
    }

    public static Command fromValue(String value) {
        Command command = VALUE_MAP.get(value);
        if (command == null) {
            throw new IllegalArgumentException("Unknown command: " + value);
        }

        return command;
    }
}
