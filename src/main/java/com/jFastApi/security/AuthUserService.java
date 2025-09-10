package com.jFastApi.security;

import com.jFastApi.exception.UsernameNotFoundException;

public interface AuthUserService {
    AuthUser loadUserByUsername(String username) throws UsernameNotFoundException;
}