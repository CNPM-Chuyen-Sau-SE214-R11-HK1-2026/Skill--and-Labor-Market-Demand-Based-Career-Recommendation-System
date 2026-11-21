package com.cnpm.careerrecommendation.module.lmi;

import com.cnpm.careerrecommendation.common.entity.Role;
import com.cnpm.careerrecommendation.common.entity.User;
import com.cnpm.careerrecommendation.common.repository.UserRepository;
import com.cnpm.careerrecommendation.common.security.JwtTokenProvider;
import com.cnpm.careerrecommendation.entity.MarketDemandIndicator;
import com.cnpm.careerrecommendation.entity.Occupation;
import com.cnpm.careerrecommendation.repository.MarketDemandIndicatorRepository;
import com.cnpm.careerrecommendation.repository.OccupationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LmiPostgresIT {

    private static final String IMPORT_PATH = "/api/v1/admin/lmi/import";
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UserRepository users;

    @Autowired
    private OccupationRepository occupations;

    @Autowired
    private MarketDemandIndicatorRepository indicators;

    @Autowired
    private JwtTokenProvider tokens;

    private User adminUser;
    private User candidateUser;
    private Occupation testOccupation;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> required("AUTH_TEST_DATABASE_URL"));
        registry.add("spring.datasource.username", () -> required("AUTH_TEST_DATABASE_USER"));
        registry.add("spring.datasource.password", () -> required("AUTH_TEST_DATABASE_PASSWORD"));
        registry.add("security.jwt.secret", () -> "lmi-integration-test-only-private-key-123456789");
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Set " + name + " to an isolated test PostgreSQL database");
        }
        return value;
    }

    @BeforeEach
    void createFixtures() {
        adminUser = users.save(new User(
                "lmi-admin-" + UUID.randomUUID() + "@test.invalid",
                "unused-test-hash",
                "LMI Admin",
                Role.ROLE_ADMIN));
        candidateUser = users.save(new User(
                "lmi-candidate-" + UUID.randomUUID() + "@test.invalid",
                "unused-test-hash",
                "LMI Candidate",
                Role.ROLE_CANDIDATE));
        testOccupation = occupations.save(new Occupation(
                "LMI-TEST-" + UUID.randomUUID().toString().substring(0, 8),
                "Test LMI Occupation",
                "IT",
                null));
    }

    @AfterEach
    void removeFixtures() {
        if (testOccupation != null) {
            occupations.deleteById(testOccupation.getId());
        }
        for (User user : new User[]{adminUser, candidateUser}) {
            if (user != null) {
                users.deleteById(user.getId());
            }
        }
    }

    @Test
    void enforcesAdminRbacForUnauthenticatedAndCandidateUsers() throws Exception {
        String body = """
                {"items":[{"occupationId":%d,"region":"NATIONAL","demandScore":80.0,"snapshotDate":"2026-11-01"}]}
                """.formatted(testOccupation.getId());

        HttpResponse<String> unauth = post(body, null);
        assertThat(unauth.statusCode()).isEqualTo(401);

        HttpResponse<String> candidateResp = post(body, candidateUser);
        assertThat(candidateResp.statusCode()).isEqualTo(403);
        assertThat(indicators.findByOccupationId(testOccupation.getId())).isEmpty();
    }

    @Test
    void insertsAndUpsertsIdempotentlyOnNaturalKeyInPostgres() throws Exception {
        LocalDate snapshot = LocalDate.of(2026, 11, 1);
        String insertBody = """
                {
                  "items": [
                    {
                      "occupationId": %d,
                      "region": "hcmc",
                      "demandScore": 84.50,
                      "growthRatePct": -4.25,
                      "openJobPostingsCount": 420,
                      "competitiveIndex": 2.45,
                      "snapshotDate": "%s"
                    }
                  ]
                }
                """.formatted(testOccupation.getId(), snapshot);

        HttpResponse<String> firstResp = post(insertBody, adminUser);
        assertThat(firstResp.statusCode()).isEqualTo(200);
        JsonNode firstJson = mapper.readTree(firstResp.body());
        assertThat(firstJson.path("totalRecordsProcessed").asInt()).isEqualTo(1);
        assertThat(firstJson.path("recordsInserted").asInt()).isEqualTo(1);
        assertThat(firstJson.path("recordsUpdated").asInt()).isZero();

        String updateBody = """
                {
                  "items": [
                    {
                      "occupationCode": "%s",
                      "region": "HCMC",
                      "demandScore": 91.75,
                      "growthRatePct": 14.80,
                      "openJobPostingsCount": 580,
                      "competitiveIndex": 3.15,
                      "snapshotDate": "%s"
                    }
                  ]
                }
                """.formatted(testOccupation.getCode(), snapshot);

        HttpResponse<String> secondResp = post(updateBody, adminUser);
        assertThat(secondResp.statusCode()).isEqualTo(200);
        JsonNode secondJson = mapper.readTree(secondResp.body());
        assertThat(secondJson.path("totalRecordsProcessed").asInt()).isEqualTo(1);
        assertThat(secondJson.path("recordsInserted").asInt()).isZero();
        assertThat(secondJson.path("recordsUpdated").asInt()).isEqualTo(1);

        var persisted = indicators.findByOccupationIdAndRegionAndSnapshotDate(
                testOccupation.getId(), "HCMC", snapshot).orElseThrow();
        assertThat(persisted.getDemandScore()).isEqualByComparingTo("91.75");
        assertThat(persisted.getOpenJobPostingsCount()).isEqualTo(580);
        assertThat(countRowsForOccupation()).isEqualTo(1);
    }

    @Test
    void upsertsExistingV3SeededIndicatorAndRestoresOriginalValues() throws Exception {
        Occupation seededOcc = occupations.findByCode("SOC-15-1252").orElseThrow();
        LocalDate seededDate = LocalDate.of(2026, 1, 1);
        MarketDemandIndicator original = indicators
                .findByOccupationIdAndRegionAndSnapshotDate(seededOcc.getId(), "NATIONAL", seededDate)
                .orElseThrow();

        BigDecimal origDemand = original.getDemandScore();
        BigDecimal origGrowth = original.getGrowthRatePct();
        Integer origPostings = original.getOpenJobPostingsCount();
        BigDecimal origCompetitive = original.getCompetitiveIndex();

        try {
            String upsertV3Body = """
                    {
                      "items": [
                        {
                          "occupationCode": "SOC-15-1252",
                          "region": "NATIONAL",
                          "demandScore": 94.25,
                          "growthRatePct": 16.50,
                          "openJobPostingsCount": 1490,
                          "competitiveIndex": 3.25,
                          "snapshotDate": "2026-01-01"
                        }
                      ]
                    }
                    """;

            HttpResponse<String> resp = post(upsertV3Body, adminUser);
            assertThat(resp.statusCode()).isEqualTo(200);
            JsonNode json = mapper.readTree(resp.body());
            assertThat(json.path("recordsInserted").asInt()).isZero();
            assertThat(json.path("recordsUpdated").asInt()).isEqualTo(1);

            MarketDemandIndicator updated = indicators
                    .findByOccupationIdAndRegionAndSnapshotDate(seededOcc.getId(), "NATIONAL", seededDate)
                    .orElseThrow();
            assertThat(updated.getDemandScore()).isEqualByComparingTo("94.25");
            assertThat(updated.getOpenJobPostingsCount()).isEqualTo(1490);
        } finally {
            jdbc.update(
                    """
                    UPDATE market_demand_indicators
                    SET demand_score = ?, growth_rate_pct = ?, open_job_postings_count = ?, competitive_index = ?
                    WHERE occupation_id = ? AND region = 'NATIONAL' AND snapshot_date = ?
                    """,
                    origDemand, origGrowth, origPostings, origCompetitive, seededOcc.getId(), seededDate);
        }
    }

    @Test
    void rejectsValidationErrorsFractionalIntegersAndMissingIdentifierOverHttp() throws Exception {
        String overMaxBody = """
                {"items":[{"occupationId":%d,"demandScore":100.01,"snapshotDate":"2026-11-01"}]}
                """.formatted(testOccupation.getId());
        assertThat(post(overMaxBody, adminUser).statusCode()).isEqualTo(400);

        String negativeDemandBody = """
                {"items":[{"occupationId":%d,"demandScore":-0.50,"snapshotDate":"2026-11-01"}]}
                """.formatted(testOccupation.getId());
        assertThat(post(negativeDemandBody, adminUser).statusCode()).isEqualTo(400);

        String fractionalIdBody = """
                {"items":[{"occupationId":%d.5,"demandScore":80.00,"openJobPostingsCount":5.5,"snapshotDate":"2026-11-01"}]}
                """.formatted(testOccupation.getId());
        assertThat(post(fractionalIdBody, adminUser).statusCode()).isEqualTo(400);

        String missingIdentifierBody = """
                {"items":[{"demandScore":80.00,"snapshotDate":"2026-11-01"}]}
                """;
        HttpResponse<String> missingResp = post(missingIdentifierBody, adminUser);
        assertThat(missingResp.statusCode()).isEqualTo(400);
        assertThat(mapper.readTree(missingResp.body()).path("error").asText()).isEqualTo("INVALID_LMI_REQUEST");

        assertThat(countRowsForOccupation()).isZero();
    }

    @Test
    void rollsBackEntireBatchWhenAnItemHasInvalidOccupation() throws Exception {
        String invalidBatch = """
                {
                  "items": [
                    {
                      "occupationId": %d,
                      "region": "NATIONAL",
                      "demandScore": 85.00,
                      "openJobPostingsCount": 100,
                      "snapshotDate": "2026-11-01"
                    },
                    {
                      "occupationId": 999999999,
                      "region": "NATIONAL",
                      "demandScore": 90.00,
                      "openJobPostingsCount": 50,
                      "snapshotDate": "2026-11-01"
                    }
                  ]
                }
                """.formatted(testOccupation.getId());

        HttpResponse<String> response = post(invalidBatch, adminUser);
        assertThat(response.statusCode()).isEqualTo(400);
        JsonNode error = mapper.readTree(response.body());
        assertThat(error.path("error").asText()).isEqualTo("INVALID_OCCUPATION");
        assertThat(countRowsForOccupation()).isZero();
    }

    private int countRowsForOccupation() {
        Integer count = jdbc.queryForObject(
                "select count(*) from market_demand_indicators where occupation_id = ?",
                Integer.class,
                testOccupation.getId());
        return count != null ? count : 0;
    }

    private HttpResponse<String> post(String body, User actor) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + IMPORT_PATH))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (actor != null) {
            builder.header("Authorization", "Bearer " + tokens.generateToken(actor));
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
