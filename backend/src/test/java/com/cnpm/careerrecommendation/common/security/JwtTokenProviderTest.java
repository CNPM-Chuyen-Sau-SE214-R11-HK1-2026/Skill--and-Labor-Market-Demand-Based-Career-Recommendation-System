package com.cnpm.careerrecommendation.common.security;

import com.cnpm.careerrecommendation.module.auth.entity.Role;
import com.cnpm.careerrecommendation.module.auth.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;
import org.springframework.test.util.ReflectionTestUtils;

class JwtTokenProviderTest {
    static final String SECRET = "unit-test-only-secret-32-bytes-minimum-never-deploy";
    private final JwtTokenProvider provider = new JwtTokenProvider(SECRET, 86_400_000);

    @Test
    void generatedTokenMatchesContractAndDoesNotContainPassword() {
        User user = User.candidate("candidate@example.com", "unused-test-hash", "Khang", null);
        ReflectionTestUtils.setField(user, "id", 42L);
        String token = provider.generateToken(user);
        Claims claims = Jwts.parser().verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .build().parseSignedClaims(token).getPayload();
        assertThat(claims.getSubject()).isEqualTo("candidate@example.com");
        assertThat(claims.get("userId")).isInstanceOf(Number.class);
        assertThat(claims.get("role")).isEqualTo("ROLE_CANDIDATE");
        assertThat(claims.getExpiration().getTime() - claims.getIssuedAt().getTime()).isEqualTo(86_400_000L);
        assertThat(claims).doesNotContainKeys("password", "passwordHash", "password_hash");
        assertThat(provider.parseToken(token)).isEqualTo(new AuthenticatedUser(42L,
                "candidate@example.com", Role.ROLE_CANDIDATE));
        assertThat(provider.getExpiresInSeconds()).isEqualTo(86_400);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "short", "your_random_secret_key_at_least_32_utf8_bytes_here",
            "default_development_secret_key_to_be_replaced_with_env_var"})
    void rejectsMissingWeakAndPlaceholderSecrets(String secret) {
        assertThatThrownBy(() -> new JwtTokenProvider(secret, 86_400_000))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 0, 999, 1501})
    void rejectsInvalidExpirationConfiguration(long duration) {
        assertThatThrownBy(() -> new JwtTokenProvider(SECRET, duration)).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-a-jwt", "a.b.c", "", "eyJhbGciOiJub25lIn0.eyJzdWIiOiJ4In0."})
    void rejectsMalformedAndUnsignedTokens(String token) {
        assertThatThrownBy(() -> provider.parseToken(token)).isInstanceOfAny(JwtException.class, IllegalArgumentException.class);
    }

    @Test
    void rejectsExpiredToken() {
        assertThatThrownBy(() -> provider.parseToken(signed(base().expiration(Date.from(Instant.now().minusSeconds(1))))))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void rejectsSignatureFromAnotherKey() {
        String token = base().signWith(Keys.hmacShaKeyFor(
                "another-unit-test-key-never-used-in-production-1234".getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256).compact();
        assertThatThrownBy(() -> provider.parseToken(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsStringUserId() {
        assertThatThrownBy(() -> provider.parseToken(signed(base().claim("userId", "42"))))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsMissingExpiry() {
        assertThatThrownBy(() -> provider.parseToken(signed(base().expiration(null))))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsFutureIssuedAt() {
        assertThatThrownBy(() -> provider.parseToken(signed(base().issuedAt(Date.from(Instant.now().plusSeconds(60))))))
                .isInstanceOf(JwtException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CANDIDATE", "ROLE_OWNER", "", "null"})
    void rejectsUnknownRoles(String role) {
        assertThatThrownBy(() -> provider.parseToken(signed(base().claim("role", role))))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsUnexpectedAlgorithmEvenWithSameKey() {
        String longSecret = SECRET + SECRET;
        JwtTokenProvider longKeyProvider = new JwtTokenProvider(longSecret, 86_400_000);
        String token = base().signWith(Keys.hmacShaKeyFor(longSecret.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS512).compact();
        assertThatThrownBy(() -> longKeyProvider.parseToken(token)).isInstanceOf(JwtException.class);
    }

    private JwtBuilder base() {
        return Jwts.builder().subject("candidate@example.com").claim("userId", 42L)
                .claim("role", "ROLE_CANDIDATE").issuedAt(Date.from(Instant.now().minusSeconds(10)))
                .expiration(Date.from(Instant.now().plusSeconds(60)));
    }

    private String signed(JwtBuilder builder) {
        return builder.signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256).compact();
    }
}
