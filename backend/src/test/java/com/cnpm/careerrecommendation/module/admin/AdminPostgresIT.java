package com.cnpm.careerrecommendation.module.admin;

import com.cnpm.careerrecommendation.common.entity.Role;
import com.cnpm.careerrecommendation.common.entity.User;
import com.cnpm.careerrecommendation.common.repository.UserRepository;
import com.cnpm.careerrecommendation.common.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class AdminPostgresIT {
    private static final String TEST_SECRET = "integration-test-only-private-key-never-deploy-123456789";
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    @Autowired
    private UserRepository users;
    @Autowired
    private JwtTokenProvider tokens;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private JdbcTemplate jdbc;
    @LocalServerPort
    private int port;

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void defaultAdminUsersHttpRequestWorksOnPostgres() throws Exception {
        User admin = save(uniquePrefix() + "@test.invalid", Role.ROLE_ADMIN, true);
        User inactiveCandidate = save(uniquePrefix() + "-filter@test.invalid", Role.ROLE_CANDIDATE, false);
        try {
            HttpResponse<String> response = request("GET", "/api/v1/admin/users", null, admin);

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).contains("\"content\"", "\"totalElements\"")
                    .doesNotContain("passwordHash");
            assertThat(request("GET", "/api/v1/admin/users?email=%20%20", null, admin).statusCode())
                    .isEqualTo(200);
            String filtered = request("GET", "/api/v1/admin/users?email=FILTER&role=ROLE_CANDIDATE&active=false",
                    null, admin).body();
            assertThat(filtered).contains(inactiveCandidate.getEmail()).doesNotContain("passwordHash");
            for (String path : List.of(
                    "/api/v1/admin/users?page=abc",
                    "/api/v1/admin/users?size=abc",
                    "/api/v1/admin/users?active=invalid",
                    "/api/v1/admin/users?status=UNKNOWN",
                    "/api/v1/admin/users?active=true&status=INACTIVE",
                    "/api/v1/admin/users?page=21474837&size=100")) {
                assertThat(request("GET", path, null, admin).statusCode()).isEqualTo(400);
            }
        } finally {
            users.deleteById(inactiveCandidate.getId());
            users.deleteById(admin.getId());
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void adminMutationsStatsAndSecurityWorkOnPostgres() throws Exception {
        User admin = save(uniquePrefix() + "-admin@test.invalid", Role.ROLE_ADMIN, true);
        User candidate = save(uniquePrefix() + "-candidate@test.invalid", Role.ROLE_CANDIDATE, true);
        try {
            assertThat(request("GET", "/api/v1/admin/stats", null, null).statusCode()).isEqualTo(401);
            assertThat(request("GET", "/api/v1/admin/stats", null, candidate).statusCode()).isEqualTo(403);
            assertThat(request("PATCH", "/api/v1/admin/users/invalid/status",
                    "{\"active\":false}", admin).statusCode()).isEqualTo(400);
            assertThat(request("PATCH", "/api/v1/admin/users/999999999/role",
                    "{\"role\":\"ROLE_ADMIN\"}", admin).statusCode()).isEqualTo(404);
            assertThat(request("PATCH", "/api/v1/admin/users/" + candidate.getId() + "/role",
                    "{\"role\":\"MASTER\"}", admin).statusCode()).isEqualTo(400);

            var status = request("PATCH", "/api/v1/admin/users/" + candidate.getId() + "/status",
                    "{\"active\":false}", admin);
            assertThat(status.statusCode()).isEqualTo(200);
            assertThat(users.findById(candidate.getId()).orElseThrow().isActive()).isFalse();

            var role = request("PATCH", "/api/v1/admin/users/" + candidate.getId() + "/role",
                    "{\"role\":\"ROLE_ADMIN\"}", admin);
            assertThat(role.statusCode()).isEqualTo(200);
            assertThat(users.findById(candidate.getId()).orElseThrow().getRole()).isEqualTo(Role.ROLE_ADMIN);

            assertThat(request("PATCH", "/api/v1/admin/users/" + admin.getId() + "/status",
                    "{\"active\":false}", admin).statusCode()).isEqualTo(400);
            assertThat(request("PATCH", "/api/v1/admin/users/" + admin.getId() + "/role",
                    "{\"role\":\"ROLE_CANDIDATE\"}", admin).statusCode()).isEqualTo(400);

            JsonNode stats = mapper.readTree(request("GET", "/api/v1/admin/stats", null, admin).body());
            assertThat(stats.path("totalUsers").asLong())
                    .isEqualTo(jdbc.queryForObject("SELECT COUNT(*) FROM users", Long.class));
            assertThat(stats.path("totalProfiles").asLong())
                    .isEqualTo(jdbc.queryForObject("SELECT COUNT(*) FROM candidate_profiles", Long.class));
            assertThat(stats.path("totalOccupations").asLong())
                    .isEqualTo(jdbc.queryForObject("SELECT COUNT(*) FROM occupations", Long.class));
            assertThat(stats.path("totalSkills").asLong())
                    .isEqualTo(jdbc.queryForObject("SELECT COUNT(*) FROM skills", Long.class));
            assertThat(stats.path("totalLmiRecords").asLong())
                    .isEqualTo(jdbc.queryForObject(
                            "SELECT COUNT(*) FROM market_demand_indicators", Long.class));
            assertThat(stats.path("totalRecommendationLogs").asLong())
                    .isEqualTo(jdbc.queryForObject("SELECT COUNT(*) FROM recommendation_logs", Long.class));
        } finally {
            users.deleteById(candidate.getId());
            users.deleteById(admin.getId());
        }
    }

    @Test
    void supportsDefaultPageWithAllNullableFilters() {
        String prefix = uniquePrefix();
        for (int index = 0; index < 3; index++) {
            save(prefix + "-" + index + "@test.invalid", Role.ROLE_CANDIDATE, true);
        }
        long expectedTotal = users.count();

        var result = users.searchAdminUsers(null, null, null,
                PageRequest.of(0, 2, Sort.by("id")));

        assertThat(result.getTotalElements()).isEqualTo(expectedTotal);
        assertThat(result.getContent()).hasSize(Math.min(2, Math.toIntExact(expectedTotal)));
    }

    @Test
    void supportsNullEmailWithRoleAndActiveFilters() {
        String prefix = uniquePrefix();
        User inactiveCandidate = save(prefix + "-inactive@test.invalid", Role.ROLE_CANDIDATE, false);
        save(prefix + "-active@test.invalid", Role.ROLE_CANDIDATE, true);
        save(prefix + "-admin@test.invalid", Role.ROLE_ADMIN, false);

        var result = users.searchAdminUsers(null, Role.ROLE_CANDIDATE, false,
                PageRequest.of(0, 100, Sort.by("id")));

        assertThat(result.getContent()).contains(inactiveCandidate);
        assertThat(result.getContent()).allMatch(user ->
                user.getRole() == Role.ROLE_CANDIDATE && !user.isActive());
    }

    @Test
    void supportsNullEmailWithRoleOnly() {
        User admin = save(uniquePrefix() + "@test.invalid", Role.ROLE_ADMIN, true);

        var result = users.searchAdminUsers(null, Role.ROLE_ADMIN, null,
                PageRequest.of(0, 100, Sort.by("id")));

        assertThat(result.getContent()).contains(admin);
        assertThat(result.getContent()).allMatch(user -> user.getRole() == Role.ROLE_ADMIN);
    }

    @Test
    void supportsNullEmailWithEachActiveFilter() {
        String prefix = uniquePrefix();
        User active = save(prefix + "-active@test.invalid", Role.ROLE_CANDIDATE, true);
        User inactive = save(prefix + "-inactive@test.invalid", Role.ROLE_CANDIDATE, false);

        var activeResult = users.searchAdminUsers(null, null, true,
                PageRequest.of(0, 100, Sort.by("id")));
        var inactiveResult = users.searchAdminUsers(null, null, false,
                PageRequest.of(0, 100, Sort.by("id")));

        assertThat(activeResult.getContent()).contains(active).allMatch(User::isActive);
        assertThat(inactiveResult.getContent()).contains(inactive).allMatch(user -> !user.isActive());
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> required("AUTH_TEST_DATABASE_URL"));
        registry.add("spring.datasource.username", () -> required("AUTH_TEST_DATABASE_USER"));
        registry.add("spring.datasource.password", () -> required("AUTH_TEST_DATABASE_PASSWORD"));
        registry.add("security.jwt.secret", () -> TEST_SECRET);
    }

    @Test
    void combinesEmailRoleAndActiveFilters() {
        String prefix = uniquePrefix();
        save(prefix + "-matching@test.invalid", Role.ROLE_ADMIN, true);
        save(prefix + "-wrong-role@test.invalid", Role.ROLE_CANDIDATE, true);
        save(prefix + "-inactive@test.invalid", Role.ROLE_ADMIN, false);
        save("unrelated-" + UUID.randomUUID() + "@test.invalid", Role.ROLE_ADMIN, true);

        var result = users.searchAdminUsers(
                prefix.toUpperCase(),
                Role.ROLE_ADMIN,
                true,
                PageRequest.of(0, 10, Sort.by("email")));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).extracting(User::getEmail)
                .containsExactly(prefix + "-matching@test.invalid");
    }

    @Test
    void paginatesFilteredUsersInDeterministicOrder() {
        String prefix = uniquePrefix();
        for (int index = 0; index < 5; index++) {
            save(prefix + "-" + index + "@test.invalid", Role.ROLE_CANDIDATE, true);
        }

        var result = users.searchAdminUsers(
                prefix,
                Role.ROLE_CANDIDATE,
                true,
                PageRequest.of(1, 2, Sort.by("email")));

        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getContent()).extracting(User::getEmail)
                .containsExactly(prefix + "-2@test.invalid", prefix + "-3@test.invalid");
    }

    @Test
    void usesIdDescendingAsTieBreakerAcrossPages() {
        String prefix = uniquePrefix();
        Instant sameCreatedAt = Instant.parse("2026-01-01T00:00:00Z");
        List<User> created = List.of(
                save(prefix + "-1@test.invalid", Role.ROLE_CANDIDATE, true, sameCreatedAt),
                save(prefix + "-2@test.invalid", Role.ROLE_CANDIDATE, true, sameCreatedAt),
                save(prefix + "-3@test.invalid", Role.ROLE_CANDIDATE, true, sameCreatedAt),
                save(prefix + "-4@test.invalid", Role.ROLE_CANDIDATE, true, sameCreatedAt));
        Sort stableSort = Sort.by(Sort.Direction.DESC, "createdAt")
                .and(Sort.by(Sort.Direction.DESC, "id"));

        var first = users.searchAdminUsers(prefix, null, null, PageRequest.of(0, 2, stableSort));
        var second = users.searchAdminUsers(prefix, null, null, PageRequest.of(1, 2, stableSort));
        var repeated = users.searchAdminUsers(prefix, null, null, PageRequest.of(0, 2, stableSort));

        assertThat(first.getContent()).extracting(User::getId)
                .containsExactly(created.get(3).getId(), created.get(2).getId());
        assertThat(second.getContent()).extracting(User::getId)
                .containsExactly(created.get(1).getId(), created.get(0).getId());
        assertThat(repeated.getContent()).extracting(User::getId)
                .containsExactlyElementsOf(first.getContent().stream().map(User::getId).toList());
    }

    @Test
    void returnsEmptyPageWhenFiltersDoNotMatch() {
        var result = users.searchAdminUsers(
                "missing-" + UUID.randomUUID(),
                Role.ROLE_ADMIN,
                false,
                PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    private User save(String email, Role role, boolean active) {
        return save(email, role, active, null);
    }

    private User save(String email, Role role, boolean active, Instant createdAt) {
        User user = new User(email, "unused-test-hash", "Admin query fixture", role);
        user.setActive(active);
        user.setCreatedAt(createdAt);
        return users.save(user);
    }

    private static String uniquePrefix() {
        return "admin-query-" + UUID.randomUUID();
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Set " + name + " to an isolated test PostgreSQL database");
        }
        return value;
    }

    private HttpResponse<String> request(String method, String path, String body, User user) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .method(method, body == null
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(body));
        if (body != null) {
            builder.header("Content-Type", "application/json");
        }
        if (user != null) {
            builder.header("Authorization", "Bearer " + tokens.generateToken(user));
        }
        return HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

}
