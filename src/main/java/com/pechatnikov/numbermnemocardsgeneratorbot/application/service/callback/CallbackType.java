package com.pechatnikov.numbermnemocardsgeneratorbot.application.service.callback;

public enum CallbackType {
    SHOW_PRICES,
    CREATE_INVOICE,
    /** Кнопка, выполняющая команду; value — имя {@link com.pechatnikov.numbermnemocardsgeneratorbot.domain.command.Command} */
    RUN_COMMAND
}
