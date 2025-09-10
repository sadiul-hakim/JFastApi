package com.jFastApi.security;

import com.jFastApi.annotation.SystemInterceptorBean;
import com.jFastApi.exception.ForbiddenException;
import com.jFastApi.http.Route;
import com.jFastApi.http.interceptor.Interceptor;
import com.jFastApi.util.JwtHelper;
import com.jFastApi.util.RequestUtility;
import com.jFastApi.util.StringUtility;
import com.sun.net.httpserver.HttpExchange;

import java.util.Collection;
import java.util.List;

@SystemInterceptorBean(order = 1)
public class AuthorizationInterceptor implements Interceptor {

    private final AuthUserService authUserService;
    private final JwtHelper jwtHelper;

    public AuthorizationInterceptor(AuthUserService authUserService,JwtHelper jwtHelper) {
        this.authUserService = authUserService;
        this.jwtHelper = jwtHelper;
    }

    @Override
    public boolean preHandle(HttpExchange exchange, Route route) {

        String path = exchange.getRequestURI().getPath();
        if (path.equals("/login") || !SecurityContext.isEnabled() || route.authorities().isEmpty()) {
            return true;
        }

        String authorization = RequestUtility.getHeader(exchange, "Authorization");
        if (StringUtility.isEmpty(authorization) || !authorization.startsWith("Bearer ")) {
            throw new AuthenticationException("Unauthorized");
        }

        authorization = authorization.substring(7);
        String username = jwtHelper.extractUsername(authorization);
        AuthUser user = authUserService.loadUserByUsername(username);

        if(!jwtHelper.isValidToken(authorization,user.getUsername())){
            throw new AuthenticationException("Unauthorized");
        }

        List<String> authorities = route.authorities();
        if (authorities.isEmpty()) {
            SecurityContext.setCurrentUser(user);
            return true;
        }

        boolean hasAccess = false;
        Collection<String> userRoles = user.getRoles();
        for (String role : userRoles) {
            if (authorities.contains(role)) {
                hasAccess = true;
                break;
            }
        }

        if (!hasAccess) {
            throw new ForbiddenException("Access Denied");
        }

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
