package com.jFastApi.security;

public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(Exception ex) {
        super(ex);
    }

    public AuthenticationException(String msg, Exception ex) {
        super(msg, ex);
    }
}
