package com.jFastApi.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Exception ex) {
        super(message, ex);
    }
}
