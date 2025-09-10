package com.jFastApi.exception;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Exception ex) {
        super(message, ex);
    }
}
