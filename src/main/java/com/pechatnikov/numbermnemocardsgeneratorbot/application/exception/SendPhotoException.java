package com.pechatnikov.numbermnemocardsgeneratorbot.application.exception;

public class SendPhotoException extends RuntimeException {
    public SendPhotoException(String message, Exception e) {
        super(message, e);

    }

    public SendPhotoException(SendPhotoException e) {
        super(e);
    }
}
