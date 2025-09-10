package com.jFastApi.security;

import java.util.Set;

public record RoutePermission(boolean isPublic, Set<String> allowedRoles) {
    public static RoutePermission publicRoute() {
        return new RoutePermission(true, Set.of());
    }

    public static RoutePermission restricted(Set<String> roles) {
        return new RoutePermission(false, Set.copyOf(roles));
    }
}
