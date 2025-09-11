package com.jFastApi.security;

import com.jFastApi.annotation.SystemInterceptorBean;
import com.jFastApi.exception.ForbiddenException;
import com.jFastApi.exception.TooManyRequestException;
import com.jFastApi.http.Route;
import com.jFastApi.http.interceptor.Interceptor;
import com.jFastApi.util.JwtHelper;
import com.jFastApi.util.RequestUtility;
import com.jFastApi.util.StringUtility;
import com.jFastApi.util.TimeUtility;
import com.sun.net.httpserver.HttpExchange;

import java.time.Duration;
import java.util.Collection;

@SystemInterceptorBean(order = 1)
public class AuthorizationInterceptor implements Interceptor {

    private final AuthUserService authUserService;
    private final JwtHelper jwtHelper;
    private final ApiRateLimiter rateLimiter;

    public AuthorizationInterceptor(AuthUserService authUserService, JwtHelper jwtHelper, ApiRateLimiter rateLimiter) {
        this.authUserService = authUserService;
        this.jwtHelper = jwtHelper;
        this.rateLimiter = rateLimiter;
    }

    /**
     * Intercepts incoming requests to enforce authentication and authorization
     * based on JWT tokens and route-level security metadata.
     *
     * @param exchange The current HTTP exchange
     * @param route    The route metadata (contains secured flag and required authorities)
     * @return true if the request is allowed to proceed, false otherwise
     * @throws AuthenticationException if authentication fails
     * @throws ForbiddenException      if the authenticated user lacks required authority
     */
    @Override
    public boolean preHandle(HttpExchange exchange, Route route) {

        String path = exchange.getRequestURI().getPath();
        Duration duration = TimeUtility.toDuration(route.time(), route.timeUnit());

        // Allow unauthenticated access to login endpoint,
        // or if security is globally disabled,
        // or if the route is not marked as secured
        if ("/login".equals(path) || !SecurityContext.isEnabled() || !route.authorized()) {
            String ip = RequestUtility.getClientIp(exchange);
            if (!rateLimiter.canLoad(ip, route.limit(), duration) && !route.disableRateLimiter()) {
                throw new TooManyRequestException("Too Many request!");
            }

            return true;
        }

        // Validate Authorization header format
        String authorization = RequestUtility.getHeader(exchange, "Authorization");
        if (StringUtility.isEmpty(authorization) || !authorization.startsWith("Bearer ")) {
            throw new AuthenticationException("Unauthorized");
        }

        // Extract token and validate user identity
        String token = authorization.substring(7);
        String username = jwtHelper.extractUsername(token);

        AuthUser user = authUserService.loadUserByUsername(username);

        // Reject invalid or expired token
        if (!jwtHelper.isValidToken(token, user.getUsername())) {
            throw new AuthenticationException("Unauthorized");
        }

        if (!rateLimiter.canLoad(username, route.limit(), duration) && !route.disableRateLimiter()) {
            throw new TooManyRequestException("Too Many request!");
        }

        if (route.authorities().isEmpty()) {
            SecurityContext.setCurrentUser(user);
            return true;
        }

        // Check if user has at least one of the required authorities
        Collection<String> userRoles = user.getRoles();
        boolean hasAccess = userRoles.stream().anyMatch(role -> route.authorities().contains(role));

        if (!hasAccess) {
            throw new ForbiddenException("Access Denied");
        }

        // Store the authenticated user in the security context for downstream usage
        SecurityContext.setCurrentUser(user);
        return true;
    }

    @Override
    public Object postHandle(HttpExchange exchange, Route route, Object result) {
        return result;
    }

    @Override
    public boolean onException(HttpExchange exchange, Route route, Throwable ex) {
        return false;
    }
}
