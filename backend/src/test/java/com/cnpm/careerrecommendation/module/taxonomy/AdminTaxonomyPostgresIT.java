package com.cnpm.careerrecommendation.module.taxonomy;

import com.cnpm.careerrecommendation.common.entity.Role;
import com.cnpm.careerrecommendation.common.entity.User;
import com.cnpm.careerrecommendation.common.repository.SkillCategoryRepository;
import com.cnpm.careerrecommendation.common.repository.SkillRepository;
import com.cnpm.careerrecommendation.common.repository.UserRepository;
import com.cnpm.careerrecommendation.common.security.JwtTokenProvider;
import com.cnpm.careerrecommendation.entity.MarketDemandIndicator;
import com.cnpm.careerrecommendation.entity.Occupation;
import com.cnpm.careerrecommendation.entity.OccupationGroup;
import com.cnpm.careerrecommendation.repository.MarketDemandIndicatorRepository;
import com.cnpm.careerrecommendation.repository.OccupationGroupRepository;
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
import java.util.ArrayList;
import java.util.List;
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
class AdminTaxonomyPostgresIT {

    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final List<Long> createdOccupationIds = new ArrayList<>();
    private final List<Long> createdSkillIds = new ArrayList<>();

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
    private OccupationGroupRepository occupationGroups;

    @Autowired
    private SkillRepository skills;

    @Autowired
    private SkillCategoryRepository skillCategories;

    @Autowired
    private MarketDemandIndicatorRepository indicators;

    @Autowired
    private JwtTokenProvider tokens;

    private User adminUser;
    private User candidateUser;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> required("AUTH_TEST_DATABASE_URL"));
        registry.add("spring.datasource.username", () -> required("AUTH_TEST_DATABASE_USER"));
        registry.add("spring.datasource.password", () -> required("AUTH_TEST_DATABASE_PASSWORD"));
        registry.add("security.jwt.secret", () -> "taxonomy-admin-integration-test-key-123456789");
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Set " + name + " to an isolated test PostgreSQL database");
        }
        return value;
    }

    @BeforeEach
    void createUsers() {
        createdOccupationIds.clear();
        createdSkillIds.clear();
        adminUser = users.save(new User(
                "tax-admin-" + UUID.randomUUID() + "@test.invalid",
                "unused-test-hash",
                "Taxonomy Admin",
                Role.ROLE_ADMIN));
        candidateUser = users.save(new User(
                "tax-candidate-" + UUID.randomUUID() + "@test.invalid",
                "unused-test-hash",
                "Taxonomy Candidate",
                Role.ROLE_CANDIDATE));
    }

    @AfterEach
    void removeFixtures() {
        for (Long occId : createdOccupationIds) {
            if (occId != null && occupations.existsById(occId)) {
                occupations.deleteById(occId);
            }
        }
        for (Long skillId : createdSkillIds) {
            if (skillId != null && skills.existsById(skillId)) {
                skills.deleteById(skillId);
            }
        }
        for (User user : new User[]{adminUser, candidateUser}) {
            if (user != null && users.existsById(user.getId())) {
                users.deleteById(user.getId());
            }
        }
    }

    @Test
    void enforcesRbacOnAdminTaxonomyEndpointsInPostgres() throws Exception {
        String occBody = "{\"code\":\"SOC-IT-RBAC\",\"title\":\"RBAC Test\"}";

        HttpResponse<String> unauth = send("POST", "/api/v1/admin/occupations", occBody, null);
        assertThat(unauth.statusCode()).isEqualTo(401);

        HttpResponse<String> candidatePost = send("POST", "/api/v1/admin/occupations", occBody, candidateUser);
        assertThat(candidatePost.statusCode()).isEqualTo(403);

        HttpResponse<String> candidateGroups = send("GET", "/api/v1/admin/occupation-groups", null, candidateUser);
        assertThat(candidateGroups.statusCode()).isEqualTo(403);
    }

    @Test
    void crudLifecycleAndMappingsReflectImmediatelyInPublicOccupationsAndRecommendations() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String skillCode = "SK-IT-" + suffix;
        String occCode = "SOC-IT-" + suffix;

        Long groupId = occupationGroups.findAll().stream()
                .map(OccupationGroup::getId)
                .findFirst()
                .orElseThrow();
        Long categoryId = skillCategories.findAll().stream()
                .map(c -> c.getId())
                .findFirst()
                .orElseThrow();

        // 1. Create Skill + verify duplicate 409 + update + visibility in GET /api/v1/skills
        String createSkillJson = """
                {
                  "code": "%s",
                  "name": "Quantum Compiler %s",
                  "categoryId": %d,
                  "skillType": "TECHNICAL",
                  "description": "Integration test skill"
                }
                """.formatted(skillCode, suffix, categoryId);
        HttpResponse<String> createSkillResp = send("POST", "/api/v1/admin/skills", createSkillJson, adminUser);
        assertThat(createSkillResp.statusCode()).isEqualTo(201);
        JsonNode skillJson = mapper.readTree(createSkillResp.body());
        long skillId = skillJson.path("id").asLong();
        createdSkillIds.add(skillId);
        assertThat(skillJson.path("code").asText()).isEqualTo(skillCode);
        assertThat(skillJson.path("categoryId").asLong()).isEqualTo(categoryId);

        HttpResponse<String> dupSkillResp = send("POST", "/api/v1/admin/skills", createSkillJson, adminUser);
        assertThat(dupSkillResp.statusCode()).isEqualTo(409);
        assertThat(mapper.readTree(dupSkillResp.body()).path("error").asText())
                .isEqualTo("SKILL_CODE_ALREADY_EXISTS");

        String updateSkillJson = """
                {
                  "code": "%s",
                  "name": "Quantum Compiler Updated %s",
                  "categoryId": %d,
                  "skillType": "DOMAIN",
                  "description": "Updated skill desc"
                }
                """.formatted(skillCode, suffix, categoryId);
        HttpResponse<String> updateSkillResp = send("PUT", "/api/v1/admin/skills/" + skillId, updateSkillJson, adminUser);
        assertThat(updateSkillResp.statusCode()).isEqualTo(200);
        assertThat(mapper.readTree(updateSkillResp.body()).path("name").asText())
                .isEqualTo("Quantum Compiler Updated " + suffix);

        HttpResponse<String> publicSkillsResp = send("GET", "/api/v1/skills?query=" + suffix, null, null);
        assertThat(publicSkillsResp.statusCode()).isEqualTo(200);
        assertThat(publicSkillsResp.body()).contains("Quantum Compiler Updated " + suffix);

        // 2. Create Occupation + verify duplicate 409 + update + visibility in GET /api/v1/occupations
        String createOccJson = """
                {
                  "code": "%s",
                  "title": "Quantum Systems Architect %s",
                  "groupId": %d,
                  "description": "IT occupation test",
                  "industry": "Technology",
                  "entrySalaryAvg": 30000000,
                  "midSalaryAvg": 60000000
                }
                """.formatted(occCode, suffix, groupId);
        HttpResponse<String> createOccResp = send("POST", "/api/v1/admin/occupations", createOccJson, adminUser);
        assertThat(createOccResp.statusCode()).isEqualTo(201);
        JsonNode occJson = mapper.readTree(createOccResp.body());
        long occId = occJson.path("id").asLong();
        createdOccupationIds.add(occId);

        HttpResponse<String> dupOccResp = send("POST", "/api/v1/admin/occupations", createOccJson, adminUser);
        assertThat(dupOccResp.statusCode()).isEqualTo(409);
        assertThat(mapper.readTree(dupOccResp.body()).path("error").asText())
                .isEqualTo("OCCUPATION_CODE_ALREADY_EXISTS");

        String updateOccJson = """
                {
                  "code": "%s",
                  "title": "Quantum Principal Architect %s",
                  "groupId": %d,
                  "description": "Updated IT occupation test",
                  "industry": "Quantum",
                  "entrySalaryAvg": 35000000,
                  "midSalaryAvg": 70000000
                }
                """.formatted(occCode, suffix, groupId);
        HttpResponse<String> updateOccResp = send("PUT", "/api/v1/admin/occupations/" + occId, updateOccJson, adminUser);
        assertThat(updateOccResp.statusCode()).isEqualTo(200);

        HttpResponse<String> publicOccListResp = send("GET", "/api/v1/occupations?query=" + suffix, null, null);
        assertThat(publicOccListResp.statusCode()).isEqualTo(200);
        assertThat(publicOccListResp.body()).contains("Quantum Principal Architect " + suffix);

        // 3. Assign Skill + test invalid weight 400 + upsert weight
        String badWeightJson = """
                {"skillId": %d, "importanceWeight": 1.20, "isRequired": true}
                """.formatted(skillId);
        HttpResponse<String> badWeightResp = send(
                "POST", "/api/v1/admin/occupations/" + occId + "/skills", badWeightJson, adminUser);
        assertThat(badWeightResp.statusCode()).isEqualTo(400);

        String assignJson = """
                {"skillId": %d, "importanceWeight": 0.90, "isRequired": true}
                """.formatted(skillId);
        HttpResponse<String> assignResp = send(
                "POST", "/api/v1/admin/occupations/" + occId + "/skills", assignJson, adminUser);
        assertThat(assignResp.statusCode()).isEqualTo(201);

        String upsertWeightJson = """
                {"skillId": %d, "importanceWeight": 0.95, "isRequired": true}
                """.formatted(skillId);
        HttpResponse<String> upsertResp = send(
                "POST", "/api/v1/admin/occupations/" + occId + "/skills", upsertWeightJson, adminUser);
        assertThat(upsertResp.statusCode()).isEqualTo(200);
        assertThat(mapper.readTree(upsertResp.body()).path("importanceWeight").asDouble()).isEqualTo(0.95);

        HttpResponse<String> occDetailResp = send("GET", "/api/v1/occupations/" + occId, null, null);
        assertThat(occDetailResp.statusCode()).isEqualTo(200);
        assertThat(mapper.readTree(occDetailResp.body()).path("skills").get(0).path("weight").asDouble())
                .isEqualTo(0.95);

        // Seed high LMI and candidate preference + skill so the new occupation ranks #1 in recommendations
        Occupation createdOccEntity = occupations.findById(occId).orElseThrow();
        MarketDemandIndicator lmi = new MarketDemandIndicator(
                createdOccEntity, "NATIONAL", new BigDecimal("99.50"), LocalDate.of(2026, 11, 20));
        lmi.setGrowthRatePct(new BigDecimal("25.00"));
        indicators.saveAndFlush(lmi);

        String profileJson = """
                {
                  "headline": "Quantum Dev",
                  "yearsOfExperience": 2.0,
                  "expectedSalaryMin": 30000000,
                  "expectedSalaryMax": 80000000,
                  "preferredLocation": "NATIONAL",
                  "workMode": "REMOTE",
                  "targetIndustry": "Quantum"
                }
                """;
        assertThat(send("PUT", "/api/v1/profile", profileJson, candidateUser).statusCode()).isEqualTo(200);

        String addCandidateSkillJson = """
                {"skillId": %d, "proficiencyLevel": "EXPERT", "yearsExperience": 2.0}
                """.formatted(skillId);
        assertThat(send("POST", "/api/v1/profile/skills", addCandidateSkillJson, candidateUser).statusCode())
                .isEqualTo(201);

        // 4. Verify GET /api/v1/recommendations immediately reflects new occupation + skill mapping + writes audit log
        HttpResponse<String> recsResp = send("GET", "/api/v1/recommendations?limit=5", null, candidateUser);
        assertThat(recsResp.statusCode()).isEqualTo(200);
        JsonNode recsJson = mapper.readTree(recsResp.body());
        assertThat(recsJson.isArray()).isTrue();
        assertThat(recsJson.get(0).path("occupationId").asLong()).isEqualTo(occId);
        assertThat(recsJson.get(0).path("matchedSkillsCount").asInt()).isEqualTo(1);
        assertThat(recsJson.get(0).path("totalRequiredSkillsCount").asInt()).isEqualTo(1);

        // 5. Remove skill mapping via query-param alias & verify GET /api/v1/recommendations/{id}/skill-gap updates
        HttpResponse<String> unlinkResp = send(
                "DELETE", "/api/v1/admin/occupations/" + occId + "/skills?skillId=" + skillId, null, adminUser);
        assertThat(unlinkResp.statusCode()).isEqualTo(204);

        HttpResponse<String> gapResp = send(
                "GET", "/api/v1/recommendations/" + occId + "/skill-gap", null, candidateUser);
        assertThat(gapResp.statusCode()).isEqualTo(200);
        assertThat(mapper.readTree(gapResp.body()).path("matchedSkills")).isEmpty();

        // Re-link, test PUT and path DELETE, then re-link before deleting occupation -> verify PostgreSQL cascade
        assertThat(send("POST", "/api/v1/admin/occupations/" + occId + "/skills", assignJson, adminUser).statusCode())
                .isEqualTo(201);
        HttpResponse<String> adminSkillsList = send(
                "GET", "/api/v1/admin/occupations/" + occId + "/skills", null, adminUser);
        assertThat(adminSkillsList.statusCode()).isEqualTo(200);
        assertThat(mapper.readTree(adminSkillsList.body())).hasSize(1);

        HttpResponse<String> putMappingResp = send(
                "PUT",
                "/api/v1/admin/occupations/" + occId + "/skills/" + skillId,
                "{\"importanceWeight\":0.60,\"isRequired\":false}",
                adminUser);
        assertThat(putMappingResp.statusCode()).isEqualTo(200);
        assertThat(mapper.readTree(putMappingResp.body()).path("isRequired").asBoolean()).isFalse();

        HttpResponse<String> deleteMappingByPathResp = send(
                "DELETE", "/api/v1/admin/occupations/" + occId + "/skills/" + skillId, null, adminUser);
        assertThat(deleteMappingByPathResp.statusCode()).isEqualTo(204);
        assertThat(send(
                "DELETE", "/api/v1/admin/occupations/" + occId + "/skills/" + skillId, null, adminUser).statusCode())
                .isEqualTo(404);

        assertThat(send("POST", "/api/v1/admin/occupations/" + occId + "/skills", assignJson, adminUser).statusCode())
                .isEqualTo(201);
        Integer logsBefore = jdbc.queryForObject(
                "SELECT COUNT(*) FROM recommendation_logs WHERE occupation_id = ?", Integer.class, occId);
        assertThat(logsBefore).isGreaterThanOrEqualTo(1);

        HttpResponse<String> deleteOccResp = send("DELETE", "/api/v1/admin/occupations/" + occId, null, adminUser);
        assertThat(deleteOccResp.statusCode()).isEqualTo(204);
        assertThat(send("GET", "/api/v1/occupations/" + occId, null, null).statusCode()).isEqualTo(404);
        assertThat(send("DELETE", "/api/v1/admin/occupations/" + occId, null, adminUser).statusCode()).isEqualTo(404);

        Integer logsAfter = jdbc.queryForObject(
                "SELECT COUNT(*) FROM recommendation_logs WHERE occupation_id = ?", Integer.class, occId);
        assertThat(logsAfter).isZero();

        // 6. Delete skill -> cascades user_skills cleanly
        HttpResponse<String> deleteSkillResp = send("DELETE", "/api/v1/admin/skills/" + skillId, null, adminUser);
        assertThat(deleteSkillResp.statusCode()).isEqualTo(204);
        assertThat(send("DELETE", "/api/v1/admin/skills/" + skillId, null, adminUser).statusCode()).isEqualTo(404);

        Integer userSkillsAfter = jdbc.queryForObject(
                "SELECT COUNT(*) FROM user_skills WHERE skill_id = ?", Integer.class, skillId);
        assertThat(userSkillsAfter).isZero();

        // 7. Admin taxonomy groups & categories list endpoints
        HttpResponse<String> groupsResp = send("GET", "/api/v1/admin/occupation-groups", null, adminUser);
        assertThat(groupsResp.statusCode()).isEqualTo(200);
        assertThat(mapper.readTree(groupsResp.body()).size()).isGreaterThanOrEqualTo(3);

        HttpResponse<String> categoriesResp = send("GET", "/api/v1/admin/skill-categories", null, adminUser);
        assertThat(categoriesResp.statusCode()).isEqualTo(200);
        assertThat(mapper.readTree(categoriesResp.body()).size()).isGreaterThanOrEqualTo(6);
    }

    private HttpResponse<String> send(String method, String path, String body, User actor) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json");
        if (actor != null) {
            builder.header("Authorization", "Bearer " + tokens.generateToken(actor));
        }
        if (body != null) {
            builder.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(body));
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
