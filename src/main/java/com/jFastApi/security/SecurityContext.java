package com.jFastApi.security;

import com.jFastApi.exception.ApplicationException;
import com.jFastApi.util.PropertiesUtil;


public final class SecurityContext {

    private SecurityContext() {
    }

    private static final ThreadLocal<AuthUser> currentUser = new ThreadLocal<>();

    private static PasswordEncoder passwordEncoder = new DefaultPasswordEncoder();

    private static final boolean enabled = PropertiesUtil.getPropertyBoolean(PropertiesUtil.SECURITY_ENABLE, false);
    private static final boolean rateLimitEnabled = PropertiesUtil.getPropertyBoolean(PropertiesUtil.SECURITY_RATE_LIMIT_ENABLED, true);

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean isRateLimitEnabled() {
        return rateLimitEnabled;
    }

    public static void setCurrentUser(AuthUser user) {
        currentUser.set(user);
    }

    public static AuthUser getCurrentUser() {
        return currentUser.get();
    }

    public static PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    public static void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        if (passwordEncoder == null) throw new ApplicationException("Encoder cannot be null");
        SecurityContext.passwordEncoder = passwordEncoder;
    }

    public static void clear() {
        currentUser.remove();
    }
}
