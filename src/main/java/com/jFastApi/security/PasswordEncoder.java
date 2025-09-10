package com.jFastApi.security;

public interface PasswordEncoder {
    /**
     * Encode the raw password.
     */
    String encode(String rawPassword);

    /**
     * Verify if the raw password matches the encoded password.
     */
    boolean matches(String rawPassword, String encodedPassword);
}
