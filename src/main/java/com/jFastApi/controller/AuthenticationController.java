package com.jFastApi.controller;

import com.jFastApi.annotation.Bean;
import com.jFastApi.annotation.HttpRoute;
import com.jFastApi.annotation.RequestBody;
import com.jFastApi.enumeration.ContentType;
import com.jFastApi.enumeration.HttpMethod;
import com.jFastApi.enumeration.HttpStatus;
import com.jFastApi.exception.ApplicationException;
import com.jFastApi.http.Response;
import com.jFastApi.security.AuthUser;
import com.jFastApi.security.AuthenticationManager;
import com.jFastApi.security.AuthenticationToken;
import com.jFastApi.util.JwtHelper;
import com.jFastApi.util.PropertiesUtil;
import com.jFastApi.util.RequestUtility;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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

        Map<String,String> tokens = new HashMap<>(){{
            put("accessToken",accessToken);
            put("refreshToken",refreshToken);
        }};
        return new Response.Builder<Map<String, String>>()
                .contentType(ContentType.JSON)
                .status(HttpStatus.OK)
                .body(tokens)
                .build();
    }
}
