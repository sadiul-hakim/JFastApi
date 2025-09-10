package com.jFastApi.security;

import com.jFastApi.annotation.Bean;

@Bean
public class AuthenticationManager {
    private final AuthUserService authUserService;

    public AuthenticationManager(AuthUserService authUserService) {
        this.authUserService = authUserService;
    }

    public void authenticate(AuthUser user) throws AuthenticationException {
        AuthUser authUser = authUserService.loadUserByUsername(user.getUsername());
        PasswordEncoder passwordEncoder = SecurityContext.getPasswordEncoder();
        if (passwordEncoder.matches(user.getPassword(), authUser.getPassword())) {
            throw new AuthenticationException("Authentication failed, Invalid Credentials.");
        }
    }

    public AuthUser authenticate(AuthenticationToken user) throws AuthenticationException {
        AuthUser authUser = authUserService.loadUserByUsername(user.username());
        PasswordEncoder passwordEncoder = SecurityContext.getPasswordEncoder();
        if (passwordEncoder.matches(user.password(), authUser.getPassword())) {
            throw new AuthenticationException("Authentication failed, Invalid Credentials.");
        }

        return authUser;
    }
}
