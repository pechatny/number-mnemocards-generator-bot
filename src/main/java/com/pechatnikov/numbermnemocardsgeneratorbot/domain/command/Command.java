package com.pechatnikov.numbermnemocardsgeneratorbot.domain.command;

import java.util.HashMap;
import java.util.Map;

public enum Command {
    START("/start", "Как пользоваться ботом"),
    BALANCE("/balance", "Баланс цифр"),
    BALANCE_REFILL("/balance_refill", "Пополнить баланс");

    private final String value;
    /** Подпись команды в меню мессенджера */
    private final String description;

    private static final Map<String, Command> VALUE_MAP = new HashMap<>();

    static {
        for (Command command : Command.values()) {
            VALUE_MAP.put(command.value, command);
        }
    }

    Command(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static Command fromValue(String value) {
        Command command = VALUE_MAP.get(value);
        if (command == null) {
            throw new IllegalArgumentException("Unknown command: " + value);
        }

        return command;
    }
}
