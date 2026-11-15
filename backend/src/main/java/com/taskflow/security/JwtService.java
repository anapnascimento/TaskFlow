package com.taskflow.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Serviço responsável pela emissão e validação de Access Tokens JWT assinados com HS256.
 */
@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long accessExpirationSeconds;
    private final String issuer;
    private final String audience;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-expiration-seconds}") long accessExpirationSeconds,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.audience}") String audience) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret must be at least 256 bits (32 bytes)");
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessExpirationSeconds = accessExpirationSeconds;
        this.issuer = issuer;
        this.audience = audience;
    }

    public String generateAccessToken(Long userId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(accessExpirationSeconds);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(userId))
                .issuer(issuer)
                .audience().add(audience).and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public Long parseAndValidateUserId(String token) throws JwtException {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(issuer)
                .requireAudience(audience)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.parseLong(claims.getSubject());
    }

    public long getAccessExpirationSeconds() {
        return accessExpirationSeconds;
    }
}
