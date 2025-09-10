package com.jFastApi.security;

import java.util.Collection;

public interface AuthUser {
    String getUsername();
    String getPassword();
    Collection<String> getRoles();
}
