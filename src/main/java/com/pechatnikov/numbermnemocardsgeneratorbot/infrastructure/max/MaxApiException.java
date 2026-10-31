package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.max;

import lombok.Getter;

@Getter
public class MaxApiException extends RuntimeException {
    private final int status;
    private final String code;

    public MaxApiException(int status, String code, String message, Throwable cause) {
        super("Max API error " + status + " " + code + ": " + message, cause);
        this.status = status;
        this.code = code;
    }

    public MaxApiException(String message, Throwable cause) {
        super(message, cause);
        this.status = 0;
        this.code = null;
    }
}
