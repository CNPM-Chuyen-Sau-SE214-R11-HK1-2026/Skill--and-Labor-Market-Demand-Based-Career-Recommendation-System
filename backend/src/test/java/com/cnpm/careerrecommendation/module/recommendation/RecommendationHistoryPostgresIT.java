package com.cnpm.careerrecommendation.module.recommendation;

import static org.assertj.core.api.Assertions.assertThat;

import com.cnpm.careerrecommendation.common.entity.Role;
import com.cnpm.careerrecommendation.common.entity.Skill;
import com.cnpm.careerrecommendation.common.entity.User;
import com.cnpm.careerrecommendation.common.repository.SkillRepository;
import com.cnpm.careerrecommendation.common.repository.UserRepository;
import com.cnpm.careerrecommendation.common.security.JwtTokenProvider;
import com.cnpm.careerrecommendation.entity.Occupation;
import com.cnpm.careerrecommendation.entity.OccupationGroup;
import com.cnpm.careerrecommendation.entity.RecommendationLog;
import com.cnpm.careerrecommendation.entity.UserSkill;
import com.cnpm.careerrecommendation.repository.OccupationGroupRepository;
import com.cnpm.careerrecommendation.repository.OccupationRepository;
import com.cnpm.careerrecommendation.repository.RecommendationLogRepository;
import com.cnpm.careerrecommendation.repository.UserSkillRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RecommendationHistoryPostgresIT {

  private static final String HISTORY_PATH = "/api/v1/recommendations/history";
  private static final String RECOMMENDATIONS_PATH = "/api/v1/recommendations";
  private final HttpClient client =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

  @LocalServerPort private int port;

  @Autowired private ObjectMapper mapper;
  @Autowired private UserRepository users;
  @Autowired private OccupationGroupRepository occupationGroups;
  @Autowired private OccupationRepository occupations;
  @Autowired private SkillRepository skills;
  @Autowired private UserSkillRepository userSkills;
  @Autowired private RecommendationLogRepository logs;
  @Autowired private JwtTokenProvider tokens;

  private User candidateA;
  private User candidateB;
  private User adminUser;
  private OccupationGroup testGroup;
  private Occupation occ1;
  private Occupation occ2;
  private Occupation occ3;

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", () -> required("AUTH_TEST_DATABASE_URL"));
    registry.add("spring.datasource.username", () -> required("AUTH_TEST_DATABASE_USER"));
    registry.add("spring.datasource.password", () -> required("AUTH_TEST_DATABASE_PASSWORD"));
    registry.add(
        "security.jwt.secret",
        () -> "recommendation-history-it-only-private-key-123456789");
  }

  private static String required(String name) {
    String value = System.getenv(name);
    if (value == null || value.isBlank()) {
      throw new IllegalStateException(
          "Set " + name + " to an isolated test PostgreSQL database");
    }
    return value;
  }

  @BeforeEach
  void createFixtures() {
    String suffix = UUID.randomUUID().toString().substring(0, 8);
    candidateA =
        users.save(
            new User(
                "rec-hist-a-" + suffix + "@test.invalid",
                "unused-test-hash",
                "Candidate A",
                Role.ROLE_CANDIDATE));
    candidateB =
        users.save(
            new User(
                "rec-hist-b-" + suffix + "@test.invalid",
                "unused-test-hash",
                "Candidate B",
                Role.ROLE_CANDIDATE));
    adminUser =
        users.save(
            new User(
                "rec-hist-admin-" + suffix + "@test.invalid",
                "unused-test-hash",
                "History Admin",
                Role.ROLE_ADMIN));
    testGroup =
        occupationGroups.save(
            new OccupationGroup(
                "REC-GRP-" + suffix, "IT History Group " + suffix, "Test occupation group"));
    occ1 =
        occupations.save(
            new Occupation("REC-OCC-1-" + suffix, "Backend Engineer", "IT", testGroup));
    occ2 =
        occupations.save(
            new Occupation("REC-OCC-2-" + suffix, "Data Engineer", "IT", testGroup));
    occ3 =
        occupations.save(
            new Occupation("REC-OCC-3-" + suffix, "Cloud Architect", "IT", null));
  }

  @AfterEach
  void removeFixtures() {
    for (User user : new User[] {candidateA, candidateB, adminUser}) {
      if (user != null && user.getId() != null) {
        List<RecommendationLog> userLogs = logs.findByUserId(user.getId());
        if (!userLogs.isEmpty()) {
          logs.deleteAll(userLogs);
        }
        List<UserSkill> claimed = userSkills.findByUserId(user.getId());
        if (!claimed.isEmpty()) {
          userSkills.deleteAll(claimed);
        }
      }
    }
    for (Occupation occ : new Occupation[] {occ1, occ2, occ3}) {
      if (occ != null && occ.getId() != null) {
        occupations.deleteById(occ.getId());
      }
    }
    if (testGroup != null && testGroup.getId() != null) {
      occupationGroups.deleteById(testGroup.getId());
    }
    for (User user : new User[] {candidateA, candidateB, adminUser}) {
      if (user != null && user.getId() != null) {
        users.deleteById(user.getId());
      }
    }
  }

  @Test
  void enforcesCandidateRbacForHistoryEndpoint() throws Exception {
    HttpResponse<String> anonymous = get(HISTORY_PATH, null);
    assertThat(anonymous.statusCode()).isEqualTo(401);
    JsonNode anonymousBody = mapper.readTree(anonymous.body());
    assertThat(anonymousBody.path("status").asInt()).isEqualTo(401);
    assertThat(anonymousBody.path("error").asText()).isEqualTo("Unauthorized");
    assertThat(anonymousBody.path("path").asText()).isEqualTo(HISTORY_PATH);

    HttpResponse<String> invalidJwt = getWithRawToken(HISTORY_PATH, "not-a-valid-jwt");
    assertThat(invalidJwt.statusCode()).isEqualTo(401);
    JsonNode invalidBody = mapper.readTree(invalidJwt.body());
    assertThat(invalidBody.path("status").asInt()).isEqualTo(401);
    assertThat(invalidBody.path("error").asText()).isEqualTo("Unauthorized");
    assertThat(invalidBody.path("path").asText()).isEqualTo(HISTORY_PATH);

    HttpResponse<String> adminResp = get(HISTORY_PATH, adminUser);
    assertThat(adminResp.statusCode()).isEqualTo(403);
    JsonNode adminBody = mapper.readTree(adminResp.body());
    assertThat(adminBody.path("status").asInt()).isEqualTo(403);
    assertThat(adminBody.path("error").asText()).isEqualTo("Forbidden");
    assertThat(adminBody.path("path").asText()).isEqualTo(HISTORY_PATH);
  }

  @Test
  void returnsEmptyHistoryPageAndRejectsInvalidPaginationAndOverflow() throws Exception {
    HttpResponse<String> emptyResp = get(HISTORY_PATH + "?page=0&size=20", candidateA);
    assertThat(emptyResp.statusCode()).isEqualTo(200);
    JsonNode emptyPage = mapper.readTree(emptyResp.body());
    assertThat(emptyPage.path("content").isArray()).isTrue();
    assertThat(emptyPage.path("content")).isEmpty();
    assertThat(emptyPage.path("pageNumber").asInt()).isZero();
    assertThat(emptyPage.path("pageSize").asInt()).isEqualTo(20);
    assertThat(emptyPage.path("totalElements").asLong()).isZero();
    assertThat(emptyPage.path("totalPages").asInt()).isZero();
    assertThat(emptyPage.path("last").asBoolean()).isTrue();

    HttpResponse<String> offsetOverflowResp =
        get(HISTORY_PATH + "?page=107374183&size=20", candidateA);
    assertThat(offsetOverflowResp.statusCode()).isEqualTo(400);
    JsonNode offsetOverflowBody = mapper.readTree(offsetOverflowResp.body());
    assertThat(offsetOverflowBody.path("status").asInt()).isEqualTo(400);
    assertThat(offsetOverflowBody.path("error").asText()).isEqualTo("INVALID_PAGINATION");
    assertThat(offsetOverflowBody.path("path").asText()).isEqualTo(HISTORY_PATH);

    HttpResponse<String> intOverflowResp =
        get(HISTORY_PATH + "?size=99999999999", candidateA);
    assertThat(intOverflowResp.statusCode()).isEqualTo(400);
    JsonNode intOverflowBody = mapper.readTree(intOverflowResp.body());
    assertThat(intOverflowBody.path("status").asInt()).isEqualTo(400);
    assertThat(intOverflowBody.path("error").asText()).isEqualTo("INVALID_PARAMETER");
    assertThat(intOverflowBody.path("path").asText()).isEqualTo(HISTORY_PATH);
  }

  @Test
  void returnsNewestFirstPaginatedHistoryIsolatedByCandidate() throws Exception {
    OffsetDateTime olderTime = OffsetDateTime.parse("2026-11-24T08:00:00Z");
    OffsetDateTime tieTime = OffsetDateTime.parse("2026-11-25T12:00:00Z");

    RecommendationLog logOlder =
        saveLog(
            candidateA,
            occ1,
            new BigDecimal("72.50"),
            3,
            "{\"limit\":10,\"userSkillIds\":[1]}",
            olderTime);
    RecommendationLog logTieFirst =
        saveLog(
            candidateA,
            occ2,
            new BigDecimal("81.00"),
            2,
            "{\"limit\":10,\"userSkillIds\":[1,2]}",
            tieTime);
    RecommendationLog logTieSecond =
        saveLog(
            candidateA,
            occ3,
            new BigDecimal("91.25"),
            1,
            "{\"limit\":5,\"userSkillIds\":[1,2,3]}",
            tieTime);
    RecommendationLog logOtherCandidate =
        saveLog(
            candidateB,
            occ1,
            new BigDecimal("99.00"),
            0,
            "{\"limit\":10,\"userSkillIds\":[9]}",
            OffsetDateTime.parse("2026-11-26T00:00:00Z"));

    HttpResponse<String> pageZeroResp = get(HISTORY_PATH + "?page=0&size=2", candidateA);
    assertThat(pageZeroResp.statusCode()).isEqualTo(200);
    JsonNode pageZero = mapper.readTree(pageZeroResp.body());
    assertThat(pageZero.path("pageNumber").asInt()).isZero();
    assertThat(pageZero.path("pageSize").asInt()).isEqualTo(2);
    assertThat(pageZero.path("totalElements").asLong()).isEqualTo(3L);
    assertThat(pageZero.path("totalPages").asInt()).isEqualTo(2);
    assertThat(pageZero.path("last").asBoolean()).isFalse();
    assertThat(pageZero.path("content")).hasSize(2);

    JsonNode firstItem = pageZero.path("content").get(0);
    assertThat(firstItem.path("id").asLong()).isEqualTo(logTieSecond.getId());
    assertThat(firstItem.path("occupationId").asLong()).isEqualTo(occ3.getId());
    assertThat(firstItem.path("occupationCode").asText()).isEqualTo(occ3.getCode());
    assertThat(firstItem.path("occupationTitle").asText()).isEqualTo("Cloud Architect");
    assertThat(firstItem.path("groupName").isNull()).isTrue();
    assertThat(firstItem.path("matchScore").decimalValue()).isEqualByComparingTo("91.25");
    assertThat(firstItem.path("skillGapCount").asInt()).isEqualTo(1);
    assertThat(firstItem.path("recommendationDate").isTextual()).isTrue();
    assertThat(firstItem.path("createdAt").isTextual()).isTrue();
    assertThat(Instant.parse(firstItem.path("recommendationDate").asText()))
        .isEqualTo(tieTime.toInstant());
    assertThat(Instant.parse(firstItem.path("createdAt").asText()))
        .isEqualTo(tieTime.toInstant());
    assertThat(firstItem.path("inputSnapshot").path("limit").asInt()).isEqualTo(5);

    JsonNode secondItem = pageZero.path("content").get(1);
    assertThat(secondItem.path("id").asLong()).isEqualTo(logTieFirst.getId());
    assertThat(secondItem.path("occupationCode").asText()).isEqualTo(occ2.getCode());
    assertThat(secondItem.path("groupName").asText()).isEqualTo(testGroup.getName());
    assertThat(secondItem.path("recommendationDate").isTextual()).isTrue();
    assertThat(secondItem.path("createdAt").isTextual()).isTrue();
    assertThat(Instant.parse(secondItem.path("recommendationDate").asText()))
        .isEqualTo(tieTime.toInstant());

    HttpResponse<String> pageOneResp = get(HISTORY_PATH + "?page=1&size=2", candidateA);
    assertThat(pageOneResp.statusCode()).isEqualTo(200);
    JsonNode pageOne = mapper.readTree(pageOneResp.body());
    assertThat(pageOne.path("last").asBoolean()).isTrue();
    assertThat(pageOne.path("content")).hasSize(1);
    JsonNode olderItem = pageOne.path("content").get(0);
    assertThat(olderItem.path("id").asLong()).isEqualTo(logOlder.getId());
    assertThat(olderItem.path("occupationCode").asText()).isEqualTo(occ1.getCode());
    assertThat(olderItem.path("recommendationDate").isTextual()).isTrue();
    assertThat(Instant.parse(olderItem.path("recommendationDate").asText()))
        .isEqualTo(olderTime.toInstant());

    // IDOR check: passing candidateA's userId as a query parameter must still only return candidateB's logs.
    HttpResponse<String> idorAttemptResp =
        get(HISTORY_PATH + "?userId=" + candidateA.getId(), candidateB);
    assertThat(idorAttemptResp.statusCode()).isEqualTo(200);
    JsonNode candidateBPage = mapper.readTree(idorAttemptResp.body());
    assertThat(candidateBPage.path("totalElements").asLong()).isEqualTo(1L);
    assertThat(candidateBPage.path("content")).hasSize(1);
    assertThat(candidateBPage.path("content").get(0).path("id").asLong())
        .isEqualTo(logOtherCandidate.getId());
  }

  @Test
  void persistsRecommendationRunAndExposesItInHistory() throws Exception {
    Skill javaSkill = skills.findByCode("SK-JAVA").orElseThrow();
    userSkills.save(new UserSkill(candidateA, javaSkill, "ADVANCED", new BigDecimal("2.0")));

    HttpResponse<String> recResp = get(RECOMMENDATIONS_PATH + "?limit=5", candidateA);
    assertThat(recResp.statusCode()).isEqualTo(200);
    JsonNode recs = mapper.readTree(recResp.body());
    assertThat(recs.isArray()).isTrue();
    assertThat(recs.size()).isGreaterThan(0);
    long topOccupationId = recs.get(0).path("occupationId").asLong();
    String topOccupationCode = recs.get(0).path("occupationCode").asText();

    HttpResponse<String> historyResp = get(HISTORY_PATH + "?page=0&size=10", candidateA);
    assertThat(historyResp.statusCode()).isEqualTo(200);
    JsonNode history = mapper.readTree(historyResp.body());
    assertThat(history.path("totalElements").asLong()).isEqualTo(1L);
    JsonNode latest = history.path("content").get(0);
    assertThat(latest.path("occupationId").asLong()).isEqualTo(topOccupationId);
    assertThat(latest.path("occupationCode").asText()).isEqualTo(topOccupationCode);
    assertThat(latest.path("recommendationDate").isTextual()).isTrue();
    assertThat(latest.path("createdAt").isTextual()).isTrue();
    assertThat(Instant.parse(latest.path("recommendationDate").asText())).isNotNull();
    assertThat(Instant.parse(latest.path("createdAt").asText())).isNotNull();
    assertThat(latest.path("inputSnapshot").path("limit").asInt()).isEqualTo(5);
    assertThat(latest.path("inputSnapshot").path("topRecommendations").get(0).path("occupationId").asLong())
        .isEqualTo(topOccupationId);
  }

  private RecommendationLog saveLog(
      User owner,
      Occupation occupation,
      BigDecimal score,
      int gapCount,
      String snapshotJson,
      OffsetDateTime timestamp) {
    RecommendationLog log =
        new RecommendationLog(owner, occupation, score, gapCount, snapshotJson);
    log.setRecommendationDate(timestamp);
    log.setCreatedAt(timestamp);
    return logs.save(log);
  }

  private HttpResponse<String> get(String path, User actor) throws Exception {
    String token = actor == null ? null : tokens.generateToken(actor);
    return getWithRawToken(path, token);
  }

  private HttpResponse<String> getWithRawToken(String path, String rawToken) throws Exception {
    var builder =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
            .timeout(Duration.ofSeconds(15))
            .header("Accept", "application/json")
            .GET();
    if (rawToken != null) {
      builder.header("Authorization", "Bearer " + rawToken);
    }
    return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
  }
}
