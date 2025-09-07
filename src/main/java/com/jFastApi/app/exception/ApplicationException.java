package com.jFastApi.app.exception;

public class ApplicationException extends RuntimeException {
    public ApplicationException(String msg) {
        super(msg);
    }
}
