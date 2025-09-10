package com.jFastApi.util;

import com.jFastApi.annotation.Bean;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Bean
public class JwtHelper {
    private final SecretKey key;

    public JwtHelper(String secret) {
        this.key = getSecretKey(secret);
    }

    public String generateToken(String username, Map<String, Object> extraClaims, long expirationDate) {
        JwtBuilder builder = Jwts.builder()
                .claims(extraClaims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationDate))
                .signWith(key);

        return builder.compact();
    }

    public boolean isValidToken(String token, String username) throws MalformedJwtException {

        boolean isValid = extractUsername(token).equalsIgnoreCase(username) && !isExpired(token);
        if (!isValid) {
            throw new MalformedJwtException("Invalid Token");
        }
        return true;
    }

    private boolean isExpired(String token) {

        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) throws MalformedJwtException {

        return parseSingleClaim(token, Claims::getExpiration);
    }

    public String extractUsername(String token) throws ExpiredJwtException, UnsupportedJwtException,
            MalformedJwtException, SignatureException, IllegalArgumentException {

        return parseSingleClaim(token, Claims::getSubject);
    }

    public Object extractClaim(String token, String claim) throws MalformedJwtException {

        return parseSingleClaim(token, claims -> claims.get(claim, Object.class));
    }

    private <T> T parseSingleClaim(String token, Function<Claims, T> resolver) throws ExpiredJwtException,
            UnsupportedJwtException, MalformedJwtException, SignatureException, IllegalArgumentException {

        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) throws ExpiredJwtException, UnsupportedJwtException,
            MalformedJwtException, SignatureException, IllegalArgumentException {

        JwtParser parser = Jwts.parser()
                .verifyWith(key).build();
        return parser.parseSignedClaims(token).getPayload();
    }

    private SecretKey getSecretKey(String secret) {

        byte[] bytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(bytes);
    }
}
