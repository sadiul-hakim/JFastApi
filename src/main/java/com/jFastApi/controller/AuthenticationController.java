package com.jFastApi.controller;

import com.jFastApi.annotation.Bean;
import com.jFastApi.annotation.ExceptionHandler;
import com.jFastApi.annotation.HttpRoute;
import com.jFastApi.annotation.RequestBody;
import com.jFastApi.enumeration.ContentType;
import com.jFastApi.enumeration.HttpMethod;
import com.jFastApi.enumeration.HttpStatus;
import com.jFastApi.http.Response;
import com.jFastApi.security.AuthUser;
import com.jFastApi.security.AuthenticationException;
import com.jFastApi.security.AuthenticationManager;
import com.jFastApi.security.AuthenticationToken;
import com.jFastApi.util.JwtHelper;
import com.jFastApi.util.PropertiesUtil;

import java.util.HashMap;
import java.util.Map;

@Bean
public class AuthenticationController {

    private final JwtHelper jwtHelper;
    private final AuthenticationManager authenticationManager;

    public AuthenticationController(JwtHelper jwtHelper, AuthenticationManager authenticationManager) {
        this.jwtHelper = jwtHelper;
        this.authenticationManager = authenticationManager;
    }

    @HttpRoute(path = "/login", method = HttpMethod.POST)
    public Response<Map<String, String>> login(@RequestBody AuthenticationToken token) {

        AuthUser authenticate = authenticationManager.authenticate(token);

        int accessTokenExpiration = PropertiesUtil.getPropertyInteger(PropertiesUtil.SECURITY_ACCESS_TOKEN_TIMEOUT,
                18000000);

        int refreshTokenExpiration = PropertiesUtil.getPropertyInteger(PropertiesUtil.SECURITY_ACCESS_TOKEN_TIMEOUT,
                604800000);

        Map<String, Object> claims = new HashMap<>();
        claims.put("authority", authenticate.getRoles());
        String accessToken = jwtHelper.generateToken(authenticate.getUsername(), claims, accessTokenExpiration);
        String refreshToken = jwtHelper.generateToken(authenticate.getUsername(), claims, refreshTokenExpiration);

        Map<String, String> tokens = new HashMap<>() {{
            put("accessToken", accessToken);
            put("refreshToken", refreshToken);
        }};
        return new Response.Builder<Map<String, String>>()
                .contentType(ContentType.JSON)
                .status(HttpStatus.OK)
                .body(tokens)
                .build();
    }

    @ExceptionHandler(exception = {AuthenticationException.class})
    public Response<Map<String, String>> handleAuthenticationException(AuthenticationException ex) {
        return new Response.Builder<Map<String, String>>()
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", ex.getMessage()))
                .build();
    }
}
