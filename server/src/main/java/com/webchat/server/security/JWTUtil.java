package com.webchat.server.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JWTUtil {

    private final SecretKey key;

    public JWTUtil() {
        // Generate a secure key for HS256
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    // Generate JWT token using UUID userId
    public String generateToken(UUID userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId); // Store UUID in claims
        return createToken(claims, userId);
    }

    // Create the token
    private String createToken(Map<String, Object> claims, UUID userId) {
        return Jwts.builder()
                .claims(claims)
                .subject(userId.toString())  // Store UUID as String in the subject
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))  // 10 hours
                .signWith(key) // Updated to use byte[] for signing
                .compact();
    }

    // Extract userId from token (UUID)
    public UUID extractUserId(String token) {
        return UUID.fromString(extractClaim(token, Claims::getSubject)); // Convert String back to UUID
    }

    // Extract the expiration date from the token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extract claims from the token
    private <T> T extractClaim(String token, ClaimsResolver<T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.resolve(claims);
    }

    // Extract all claims from the token
    private Claims extractAllClaims(String token) {
        JwtParser jwtParser = Jwts.parser() // Using Jwts.parser() instead of parserBuilder
                .setSigningKey(key) // Updated to use byte[] for signing key
                .build();
        return jwtParser.parseClaimsJws(token).getBody(); // `getBody()` is still available but check deprecation warnings
    }

    // Check if the token is expired
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Validate the token
    public boolean validateToken(String token, UUID userId) {
        return (userId.equals(extractUserId(token)) && !isTokenExpired(token));
    }

    // Functional interface to extract a claim
    @FunctionalInterface
    interface ClaimsResolver<T> {
        T resolve(Claims claims);
    }
}


























