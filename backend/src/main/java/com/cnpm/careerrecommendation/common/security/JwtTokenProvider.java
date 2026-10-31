package com.cnpm.careerrecommendation.common.security;

import com.cnpm.careerrecommendation.module.auth.entity.Role;
import com.cnpm.careerrecommendation.module.auth.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    private final SecretKey key;
    private final long expirationMs;
    private final JwtParser parser;

    public JwtTokenProvider(@Value("${security.jwt.secret}") String secret,
                            @Value("${security.jwt.expiration-ms}") long expirationMs) {
        if (secret == null || secret.isBlank() || secret.startsWith("your_")
                || secret.startsWith("default_")
                || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT_SECRET phải là khóa ngẫu nhiên riêng, tối thiểu 32 byte UTF-8");
        }
        if (expirationMs < 1000 || expirationMs % 1000 != 0) {
            throw new IllegalArgumentException("JWT_EXPIRATION_MS phải là bội số dương của 1000");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.parser = Jwts.parser().verifyWith(key).build();
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder().subject(user.getEmail())
                .claim("userId", user.getId()).claim("role", user.getRole().name())
                .issuedAt(Date.from(now)).expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(key, Jwts.SIG.HS256).compact();
    }

    public AuthenticatedUser parseToken(String token) {
        Jws<Claims> signed = parser.parseSignedClaims(token);
        if (!Jwts.SIG.HS256.getId().equals(signed.getHeader().getAlgorithm())) {
            throw new UnsupportedJwtException("Unexpected signing algorithm");
        }
        Claims claims = signed.getPayload();
        Object rawId = claims.get("userId");
        if (!(rawId instanceof Integer || rawId instanceof Long)
                || ((Number) rawId).longValue() <= 0
                || claims.getSubject() == null || claims.getSubject().isBlank()
                || claims.getExpiration() == null || claims.getIssuedAt() == null
                || !claims.getExpiration().after(claims.getIssuedAt())
                || claims.getIssuedAt().after(new Date())) {
            throw new MalformedJwtException("Invalid identity claims");
        }
        Role role;
        try {
            role = Role.valueOf(claims.get("role", String.class));
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new MalformedJwtException("Invalid role claim");
        }
        return new AuthenticatedUser(((Number) rawId).longValue(), claims.getSubject(), role);
    }

    public long getExpiresInSeconds() { return expirationMs / 1000; }
}
