package com.jFastApi.exception;

public class ApplicationException extends RuntimeException {
    public ApplicationException(String msg) {
        super(msg);
    }

    public ApplicationException(Exception e) {
        super(e);
    }

    public ApplicationException(String msg, Exception e) {
        super(msg, e);
    }
}
