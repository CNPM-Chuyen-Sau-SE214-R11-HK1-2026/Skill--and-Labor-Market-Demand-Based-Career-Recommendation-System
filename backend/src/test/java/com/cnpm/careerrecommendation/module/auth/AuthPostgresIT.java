package com.cnpm.careerrecommendation.module.auth;

import com.cnpm.careerrecommendation.common.security.AuthenticatedUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.web.bind.annotation.*;
import static org.assertj.core.api.Assertions.*;

/** Real HTTP + real PostgreSQL + real BCrypt/JWT. Never runs against an implicit application database. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(AuthPostgresIT.SecurityProbes.class)
// These HTTP integration tests use no mocks, transactions or servlet test context.
@TestExecutionListeners(listeners = DependencyInjectionTestExecutionListener.class, inheritListeners = false)
class AuthPostgresIT {
    private static final String TEST_SECRET = "integration-test-only-private-key-never-deploy-123456789";
    private static final String PASSWORD = "TestPassword123!";
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    @LocalServerPort int port;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder passwords;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> required("AUTH_TEST_DATABASE_URL"));
        registry.add("spring.datasource.username", () -> required("AUTH_TEST_DATABASE_USER"));
        registry.add("spring.datasource.password", () -> required("AUTH_TEST_DATABASE_PASSWORD"));
        registry.add("security.jwt.secret", () -> TEST_SECRET);
        registry.add("security.jwt.expiration-ms", () -> "86400000");
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) { throw new IllegalStateException("Set " + name + " to an isolated test PostgreSQL database"); }
        return value;
    }

    @Test
    void migratesRealPostgresSchemaAndValidatesHibernateMappings() {
        assertThat(jdbc.queryForObject("select count(*) from flyway_schema_history where success and version in ('1','2')", Integer.class)).isEqualTo(2);
        assertThat(jdbc.queryForObject("select count(*) from information_schema.tables where table_schema='public' and table_name <> 'flyway_schema_history'", Integer.class)).isEqualTo(10);
        assertThat(jdbc.queryForObject("select character_maximum_length from information_schema.columns where table_name='users' and column_name='phone'", Integer.class)).isEqualTo(20);
    }

    @Test
    void registrationPersistsOnlyBcryptAndMeReturnsTheAuthenticatedAccount() throws Exception {
        String email = email();
        var registration = register(email, "0901234567");
        assertThat(registration.statusCode()).isEqualTo(201);
        JsonNode registered = json(registration);
        assertThat(registered.path("role").asText()).isEqualTo("ROLE_CANDIDATE");
        assertThat(registered.path("status").asText()).isEqualTo("ACTIVE");
        assertThat(registered.path("createdAt").asText()).isNotBlank();
        String hash = jdbc.queryForObject("select password_hash from users where email=?", String.class, email);
        assertThat(hash).startsWith("$2a$12$").isNotEqualTo(PASSWORD);
        assertThat(passwords.matches(PASSWORD, hash)).isTrue();
        var login = login(email, PASSWORD);
        assertThat(login.statusCode()).isEqualTo(200);
        JsonNode data = json(login);
        assertThat(data.path("expiresIn").asInt()).isEqualTo(86400);
        assertThat(data.path("tokenType").asText()).isEqualTo("Bearer");
        String token = data.path("accessToken").asText();
        JsonNode claims = mapper.readTree(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
        assertThat(claims.path("sub").asText()).isEqualTo(email);
        assertThat(claims.path("userId").isIntegralNumber()).isTrue();
        var me = send("GET", "/api/v1/auth/me?userId=999999", null, token);
        assertThat(me.statusCode()).isEqualTo(200);
        assertThat(json(me).path("id")).isEqualTo(registered.path("id"));
        assertThat(json(me).path("phone").asText()).isEqualTo("0901234567");
        for (var response : List.of(registration, login, me)) {
            assertThat(response.body()).doesNotContain("password", "password_hash", hash, PASSWORD);
            assertThat(response.headers().firstValue("set-cookie")).isEmpty();
            assertThat(response.headers().firstValue("cache-control").orElse("")).contains("no-store");
        }
    }

    @Test
    void normalizesCaseAndRejectsDuplicateEmails() throws Exception {
        String email = email();
        assertThat(register(email.toUpperCase(Locale.ROOT), null).statusCode()).isEqualTo(201);
        assertThat(register(" " + email + " ", null).statusCode()).isEqualTo(409);
        assertThat(login(email.toUpperCase(Locale.ROOT), PASSWORD).statusCode()).isEqualTo(200);
    }

    @Test
    void concurrentRegistrationReturnsOneCreatedAndOneConflict() throws Exception {
        String email = email();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            var first = executor.submit(() -> register(email, null).statusCode());
            var second = executor.submit(() -> register(email, null).statusCode());
            assertThat(List.of(first.get(30, TimeUnit.SECONDS), second.get(30, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(201, 409);
            assertThat(jdbc.queryForObject("select count(*) from users where email=?", Integer.class, email)).isEqualTo(1);
        } finally { executor.shutdownNow(); }
    }

    @Test
    void wrongPasswordAndUnknownAndDisabledUsersHaveSameError() throws Exception {
        String email = email(); register(email, null);
        var wrong = login(email, "WrongPassword123!");
        var unknown = login(email(), PASSWORD);
        jdbc.update("update users set is_active=false where email=?", email);
        var disabled = login(email, PASSWORD);
        for (var response : List.of(wrong, unknown, disabled)) {
            assertError(response, 401, "/api/v1/auth/login");
            assertThat(json(response).path("message")).isEqualTo(json(wrong).path("message"));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/v1/auth/me", "/api/v1/profile", "/api/v1/recommendations", "/api/v1/admin/_auth-test"})
    void anonymousCannotAccessProtectedRoutes(String path) throws Exception {
        assertError(send("GET", path, null, null), 401, path);
    }

    @ParameterizedTest
    @ValueSource(strings = {"garbage", "a.b.c", "eyJhbGciOiJub25lIn0.eyJzdWIiOiJ4In0."})
    void rejectsMalformedTokensWithJson401(String token) throws Exception {
        assertError(send("GET", "/api/v1/auth/me", null, token), 401, "/api/v1/auth/me");
    }

    @Test
    void rejectsExpiredWrongSignatureAndTamperedRole() throws Exception {
        String email = email(); long id = json(register(email, null)).path("id").asLong();
        var now = Instant.now();
        String expired = Jwts.builder().subject(email).claim("userId", id).claim("role", "ROLE_CANDIDATE")
                .issuedAt(Date.from(now.minusSeconds(120))).expiration(Date.from(now.minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256).compact();
        String wrongKey = Jwts.builder().subject(email).claim("userId", id).claim("role", "ROLE_CANDIDATE")
                .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(120)))
                .signWith(Keys.hmacShaKeyFor((TEST_SECRET + "wrong").getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256).compact();
        String original = token(email);
        String[] parts = original.split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8)
                .replace("ROLE_CANDIDATE", "ROLE_ADMIN");
        String tampered = parts[0] + "." + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8)) + "." + parts[2];
        for (String jwt : List.of(expired, wrongKey, tampered)) {
            assertError(send("GET", "/api/v1/auth/me", null, jwt), 401, "/api/v1/auth/me");
        }
    }

    @Test
    void databaseRoleEnforcesCandidateAndAdminBoundaries() throws Exception {
        String email = email(); long id = json(register(email, null)).path("id").asLong();
        String candidate = token(email);
        assertError(send("GET", "/api/v1/admin/_auth-test", null, candidate), 403, "/api/v1/admin/_auth-test");
        var profile = send("GET", "/api/v1/profile/_auth-test?userId=999999", null, candidate);
        assertThat(profile.statusCode()).isEqualTo(200);
        assertThat(json(profile).path("userId").asLong()).isEqualTo(id);
        jdbc.update("update users set role='ROLE_ADMIN' where id=?", id);
        assertThat(send("GET", "/api/v1/auth/me", null, candidate).statusCode()).isEqualTo(401);
        String admin = token(email);
        assertThat(send("GET", "/api/v1/admin/_auth-test", null, admin).statusCode()).isEqualTo(200);
        assertThat(send("GET", "/api/v1/auth/me", null, admin).statusCode()).isEqualTo(200);
        assertThat(send("GET", "/api/v1/profile/_auth-test", null, admin).statusCode()).isEqualTo(403);
    }

    @Test
    void disabledAndDeletedAccountsCannotReuseAnIssuedToken() throws Exception {
        String email = email(); register(email, null); String token = token(email);
        jdbc.update("update users set is_active=false where email=?", email);
        assertThat(send("GET", "/api/v1/auth/me", null, token).statusCode()).isEqualTo(401);
        jdbc.update("delete from users where email=?", email);
        assertThat(send("GET", "/api/v1/auth/me", null, token).statusCode()).isEqualTo(401);
    }

    @Test
    void rejectsMassAssignmentAndMalformedJsonWithoutLeakingPasswords() throws Exception {
        var role = send("POST", "/api/v1/auth/register", "{\"email\":\"" + email()
                + "\",\"password\":\"" + PASSWORD + "\",\"fullName\":\"Khang\",\"role\":\"ROLE_ADMIN\"}", null);
        assertError(role, 400, "/api/v1/auth/register");
        assertThat(role.body()).doesNotContain(PASSWORD);
        assertError(send("POST", "/api/v1/auth/register", "{bad-json", null), 400, "/api/v1/auth/register");
    }

    @Test
    void validationRedactsRejectedPasswordAndChecksRequiredFields() throws Exception {
        var response = send("POST", "/api/v1/auth/register",
                "{\"email\":\"bad\",\"password\":\"short\",\"fullName\":\" \"}", null);
        assertError(response, 400, "/api/v1/auth/register");
        assertThat(response.body()).doesNotContain("short");
        assertThat(json(response).path("errors").size()).isGreaterThanOrEqualTo(3);
        for (JsonNode error : json(response).path("errors")) { assertThat(error.path("rejectedValue").isNull()).isTrue(); }
    }

    @Test
    void unicodePasswordWithinLimitWorksAndOverByteLimitIsRejected() throws Exception {
        String email = email();
        String unicode = " MậtKhẩu123! ";
        var body = mapper.writeValueAsString(Map.of("email", email, "password", unicode, "fullName", "Việt Khang"));
        assertThat(send("POST", "/api/v1/auth/register", body, null).statusCode()).isEqualTo(201);
        assertThat(login(email, unicode).statusCode()).isEqualTo(200);
        assertThat(login(email, unicode.strip()).statusCode()).isEqualTo(401);
        String excessive = "ậ".repeat(25);
        var invalid = send("POST", "/api/v1/auth/register", mapper.writeValueAsString(Map.of(
                "email", email(), "password", excessive, "fullName", "Khang")), null);
        assertThat(invalid.statusCode()).isEqualTo(400);
        assertThat(invalid.body()).doesNotContain(excessive);
    }

    @Test
    void allowsOnlyPublicReadMethodsAndFailsClosedForOtherRoutes() throws Exception {
        assertThat(send("GET", "/api/v1/occupations", null, null).statusCode()).isEqualTo(200);
        assertThat(send("GET", "/api/v1/skills", null, null).statusCode()).isEqualTo(200);
        assertThat(send("POST", "/api/v1/occupations", "{}", null).statusCode()).isEqualTo(401);
        assertThat(send("GET", "/api/v1/auth/arbitrary", null, null).statusCode()).isEqualTo(401);
    }

    @Test
    void corsPreflightAllowsConfiguredOriginAndRejectsUntrustedOrigin() throws Exception {
        var allowed = preflight("http://localhost:3000");
        assertThat(allowed.statusCode()).isEqualTo(200);
        assertThat(allowed.headers().firstValue("access-control-allow-origin")).contains("http://localhost:3000");
        var denied = preflight("https://untrusted.example");
        assertError(denied, 403, "/api/v1/auth/me");
        assertThat(denied.headers().firstValue("access-control-allow-origin")).isEmpty();
    }

    @Test
    void ignoresStaleBearerOnLoginSoUserCanAuthenticateAgain() throws Exception {
        String email = email(); register(email, null);
        var body = mapper.writeValueAsString(Map.of("email", email, "password", PASSWORD));
        assertThat(send("POST", "/api/v1/auth/login", body, "expired-token").statusCode()).isEqualTo(200);
    }

    private HttpResponse<String> register(String email, String phone) throws Exception {
        Map<String, String> body = new HashMap<>(Map.of("email", email, "password", PASSWORD, "fullName", "Việt Khang"));
        if (phone != null) { body.put("phone", phone); }
        return send("POST", "/api/v1/auth/register", mapper.writeValueAsString(body), null);
    }

    private HttpResponse<String> login(String email, String password) throws Exception {
        return send("POST", "/api/v1/auth/login", mapper.writeValueAsString(Map.of("email", email, "password", password)), null);
    }

    private String token(String email) throws Exception {
        var response = login(email, PASSWORD);
        assertThat(response.statusCode()).isEqualTo(200);
        return json(response).path("accessToken").asText();
    }

    private HttpResponse<String> send(String method, String path, String body, String token) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        if (token != null) { request.header("Authorization", "Bearer " + token); }
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> preflight(String origin) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/auth/me"))
                .header("Origin", origin).header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "authorization,content-type")
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody()).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode json(HttpResponse<String> response) throws Exception { return mapper.readTree(response.body()); }

    private void assertError(HttpResponse<String> response, int status, String path) throws Exception {
        assertThat(response.statusCode()).isEqualTo(status);
        JsonNode error = json(response);
        assertThat(error.path("status").asInt()).isEqualTo(status);
        assertThat(error.path("path").asText()).isEqualTo(path);
        assertThat(error.path("timestamp").asText()).isNotBlank();
        assertThat(error.path("errors").isArray()).isTrue();
    }

    private String email() { return "test-" + UUID.randomUUID() + "@example.com"; }

    /** Test-only probes exercise URL authorization before future module controllers exist. */
    @RestController
    static class SecurityProbes {
        @GetMapping("/api/v1/admin/_auth-test")
        Map<String, String> admin() { return Map.of("status", "allowed"); }

        @GetMapping("/api/v1/profile/_auth-test")
        AuthenticatedUser profile(@AuthenticationPrincipal AuthenticatedUser identity) { return identity; }

        @GetMapping({"/api/v1/occupations", "/api/v1/skills"})
        List<String> publicTaxonomy() { return List.of(); }
    }
}
