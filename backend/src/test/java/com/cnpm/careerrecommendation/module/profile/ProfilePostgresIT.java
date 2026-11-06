package com.cnpm.careerrecommendation.module.profile;

import com.cnpm.careerrecommendation.common.entity.Role;
import com.cnpm.careerrecommendation.common.entity.Skill;
import com.cnpm.careerrecommendation.common.entity.User;
import com.cnpm.careerrecommendation.common.repository.SkillRepository;
import com.cnpm.careerrecommendation.common.repository.UserRepository;
import com.cnpm.careerrecommendation.common.security.JwtTokenProvider;
import com.cnpm.careerrecommendation.repository.UserSkillRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
class ProfilePostgresIT {
    private static final String PROFILE = "/api/v1/profile";
    private static final String SKILLS = PROFILE + "/skills";
    private static final String PROFILE_BODY = """
            {"headline":"Backend developer","yearsOfExperience":1.5,"expectedSalaryMin":100,
             "expectedSalaryMax":200,"preferredLocation":"HCM","workMode":"REMOTE","targetIndustry":"IT"}
            """;
    private static final String LEVEL_UPDATE = """
            {"proficiencyLevel":"EXPERT","yearsExperience":2.5}
            """;

    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    @LocalServerPort int port;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;
    @Autowired UserRepository users;
    @Autowired SkillRepository skills;
    @Autowired UserSkillRepository userSkills;
    @Autowired JwtTokenProvider tokens;

    private User candidateA;
    private User candidateB;
    private User admin;
    private Skill skill;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> required("AUTH_TEST_DATABASE_URL"));
        registry.add("spring.datasource.username", () -> required("AUTH_TEST_DATABASE_USER"));
        registry.add("spring.datasource.password", () -> required("AUTH_TEST_DATABASE_PASSWORD"));
        registry.add("security.jwt.secret", () -> "profile-integration-test-only-key-123456789");
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
        candidateA = createUser(Role.ROLE_CANDIDATE);
        candidateB = createUser(Role.ROLE_CANDIDATE);
        admin = createUser(Role.ROLE_ADMIN);
        skill = skills.save(new Skill("profile-" + UUID.randomUUID(), "Test skill", null, null));
    }

    @AfterEach
    void removeFixtures() {
        for (User user : new User[]{candidateA, candidateB, admin}) {
            if (user != null) {
                users.deleteById(user.getId());
            }
        }
        if (skill != null) {
            skills.deleteById(skill.getId());
        }
    }

    @Test
    void missingProfileReturnsStandard404WithoutCreatingProfile() throws Exception {
        JsonNode error = send("GET", PROFILE, null, candidateA, 404);
        assertThat(error.path("error").asText()).isEqualTo("PROFILE_NOT_FOUND");
        assertThat(error.path("status").asInt()).isEqualTo(404);
        assertThat(error.path("path").asText()).isEqualTo(PROFILE);
        assertThat(error.path("timestamp").asText()).isNotBlank();
        assertThat(error.path("errors").isArray()).isTrue();
        assertThat(profileCount(candidateA)).isZero();
    }

    @Test
    void createsAndReadsOwnProfile() throws Exception {
        JsonNode created = send("PUT", PROFILE, PROFILE_BODY, candidateA, 200);
        JsonNode read = send("GET", PROFILE, null, candidateA, 200);
        assertThat(read.path("id")).isEqualTo(created.path("id"));
        assertThat(read.path("userId").asLong()).isEqualTo(candidateA.getId());
        assertThat(read.path("headline").asText()).isEqualTo("Backend developer");
        assertThat(read.path("yearsOfExperience").asDouble()).isEqualTo(1.5);
        assertThat(read.path("expectedSalaryMin").asInt()).isEqualTo(100);
        assertThat(read.path("expectedSalaryMax").asInt()).isEqualTo(200);
        assertThat(read.path("preferredLocation").asText()).isEqualTo("HCM");
        assertThat(read.path("targetIndustry").asText()).isEqualTo("IT");
        assertThat(read.path("workMode").asText()).isEqualTo("REMOTE");
        assertThat(read.path("updatedAt").asText()).endsWith("Z");
        send("GET", PROFILE, null, candidateB, 404);
    }

    @Test
    void updatesExistingProfileAndAppliesDefaults() throws Exception {
        JsonNode created = send("PUT", PROFILE, PROFILE_BODY, candidateA, 200);
        JsonNode updated = send("PUT", PROFILE, "{\"headline\":\"Updated\"}", candidateA, 200);
        assertThat(updated.path("id")).isEqualTo(created.path("id"));
        JsonNode read = send("GET", PROFILE, null, candidateA, 200);
        assertThat(read.path("headline").asText()).isEqualTo("Updated");
        assertThat(read.path("expectedSalaryMin").isNull()).isTrue();
        assertThat(read.path("workMode").asText()).isEqualTo("HYBRID");
        assertThat(read.path("yearsOfExperience").asDouble()).isZero();
        assertThat(profileCount(candidateA)).isEqualTo(1);
    }

    @Test
    void acceptsEqualAndUnpairedSalaryBounds() throws Exception {
        send("PUT", PROFILE, "{\"expectedSalaryMin\":200.00,\"expectedSalaryMax\":200}", candidateA, 200);
        JsonNode read = send("GET", PROFILE, null, candidateA, 200);
        assertThat(read.path("expectedSalaryMin").asInt()).isEqualTo(200);
        assertThat(read.path("expectedSalaryMax").asInt()).isEqualTo(200);
        send("PUT", PROFILE, "{\"expectedSalaryMin\":300,\"expectedSalaryMax\":null}", candidateA, 200);
        read = send("GET", PROFILE, null, candidateA, 200);
        assertThat(read.path("expectedSalaryMin").asInt()).isEqualTo(300);
        assertThat(read.path("expectedSalaryMax").isNull()).isTrue();
    }

    @Test
    void rejectsReversedSalaryWithoutChangingStoredProfile() throws Exception {
        send("PUT", PROFILE, PROFILE_BODY, candidateA, 200);
        JsonNode before = send("GET", PROFILE, null, candidateA, 200);
        JsonNode error = send("PUT", PROFILE, "{\"headline\":\"Invalid\",\"expectedSalaryMin\":200,\"expectedSalaryMax\":100}",
                candidateA, 400);
        assertThat(error.path("message").asText()).contains("expectedSalaryMin");
        assertThat(send("GET", PROFILE, null, candidateA, 200)).isEqualTo(before);
    }

    @Test
    void listsOnlyOwnedSkillsAndWorksBeforeProfileExists() throws Exception {
        assertThat(send("GET", SKILLS, null, candidateB, 200).size()).isZero();
        send("POST", SKILLS, addBody(skill.getId()), candidateA, 201);
        assertThat(send("GET", SKILLS, null, candidateA, 200).size()).isEqualTo(1);
        assertThat(send("GET", SKILLS, null, candidateB, 200).size()).isZero();
        assertThat(profileCount(candidateA)).isZero();
        JsonNode profile = send("PUT", PROFILE, PROFILE_BODY, candidateA, 200);
        assertThat(profile.path("skillsCount").asLong()).isEqualTo(1);
    }

    @Test
    void createsSkillLinkedToOwnerAndCatalog() throws Exception {
        JsonNode created = send("POST", SKILLS, addBody(skill.getId()), candidateA, 201);
        assertThat(created.path("skillId").asLong()).isEqualTo(skill.getId());
        assertThat(created.path("skillName").asText()).isEqualTo(skill.getName());
        assertThat(created.path("createdAt").asText()).endsWith("Z");
        var row = userSkills.findByUserIdAndSkillId(candidateA.getId(), skill.getId()).orElseThrow();
        assertThat(row.getYearsOfExperience()).isEqualByComparingTo("1.5");
        JsonNode listed = send("GET", SKILLS, null, candidateA, 200).get(0);
        assertThat(listed.path("skillCode").asText()).isEqualTo(skill.getCode());
        assertThat(listed.path("verified").asBoolean()).isFalse();
    }

    @Test
    void rejectsMissingCatalogSkill() throws Exception {
        Skill removed = skills.save(new Skill("missing-" + UUID.randomUUID(), "Removed skill", null, null));
        skills.deleteById(removed.getId());
        send("POST", SKILLS, addBody(removed.getId()), candidateA, 404);
        assertThat(userSkills.countByUserId(candidateA.getId())).isZero();
    }

    @Test
    void rejectsDuplicateForSameOwnerButAllowsOtherOwner() throws Exception {
        send("POST", SKILLS, addBody(skill.getId()), candidateA, 201);
        send("POST", SKILLS, addBody(skill.getId()), candidateA, 409);
        send("POST", SKILLS, addBody(skill.getId()), candidateB, 201);
        assertThat(userSkills.countByUserId(candidateA.getId())).isEqualTo(1);
        assertThat(userSkills.countByUserId(candidateB.getId())).isEqualTo(1);
    }

    @Test
    void updatesByCatalogIdAndOnlyChangesAllowedFields() throws Exception {
        send("POST", SKILLS, addBody(skill.getId()), candidateA, 201);
        JsonNode updated = send("PUT", skillPath(), LEVEL_UPDATE, candidateA, 200);
        assertThat(updated.path("skillId").asLong()).isEqualTo(skill.getId());
        assertThat(updated.path("proficiencyLevel").asText()).isEqualTo("EXPERT");
        assertThat(updated.path("yearsExperience").asDouble()).isEqualTo(2.5);
        send("PUT", skillPath(), "{\"yearsExperience\":null,\"verified\":true}", candidateA, 400);
        send("PUT", skillPath(), "{\"proficiencyLevel\":\"ADVANCED\"}", candidateA, 200);
        JsonNode listed = send("GET", SKILLS, null, candidateA, 200).get(0);
        assertThat(listed.path("proficiencyLevel").asText()).isEqualTo("ADVANCED");
        assertThat(listed.path("yearsExperience").asDouble()).isEqualTo(2.5);
        assertThat(listed.path("verified").asBoolean()).isFalse();
    }

    @Test
    void deletesOnlyOwnSkillAndRepeatReturns404() throws Exception {
        send("POST", SKILLS, addBody(skill.getId()), candidateA, 201);
        send("POST", SKILLS, addBody(skill.getId()), candidateB, 201);
        send("DELETE", skillPath(), null, candidateA, 204);
        send("DELETE", skillPath(), null, candidateA, 404);
        assertThat(send("GET", SKILLS, null, candidateA, 200).size()).isZero();
        assertThat(send("GET", SKILLS, null, candidateB, 200).size()).isEqualTo(1);
        assertThat(skills.existsById(skill.getId())).isTrue();
    }

    @Test
    void candidateCannotUpdateOrDeleteAnotherCandidatesSkill() throws Exception {
        send("POST", SKILLS, addBody(skill.getId()), candidateA, 201);
        JsonNode before = send("GET", SKILLS, null, candidateA, 200);
        send("PUT", skillPath(), LEVEL_UPDATE, candidateB, 404);
        send("DELETE", skillPath(), null, candidateB, 404);
        assertThat(send("GET", SKILLS, null, candidateA, 200)).isEqualTo(before);
    }

    @Test
    void rejectsMissingAuthenticationAndWrongRole() throws Exception {
        assertThat(send("GET", PROFILE, null, null, 401).path("status").asInt()).isEqualTo(401);
        assertThat(send("PUT", PROFILE, PROFILE_BODY, admin, 403).path("status").asInt()).isEqualTo(403);
        assertThat(send("POST", SKILLS, addBody(skill.getId()), admin, 403).path("status").asInt()).isEqualTo(403);
        assertThat(profileCount(admin)).isZero();
    }

    @Test
    void rejectsInvalidInputsAndOwnerInjection() throws Exception {
        send("PUT", PROFILE, "{\"userId\":" + candidateB.getId() + "}", candidateA, 400);
        send("PUT", PROFILE, "{\"workMode\":\"UNKNOWN\",\"yearsOfExperience\":-1}", candidateA, 400);
        send("POST", SKILLS, "{\"skillId\":" + skill.getId() + "}", candidateA, 400);
        send("POST", SKILLS, "{\"skillId\":" + skill.getId() + ",\"proficiencyLevel\":\"UNKNOWN\"}", candidateA, 400);
        send("POST", SKILLS, "{\"skillId\":" + skill.getId() + ".5,\"proficiencyLevel\":\"EXPERT\"}", candidateA, 400);
        send("PUT", SKILLS + "/invalid", LEVEL_UPDATE, candidateA, 400);
        assertThat(profileCount(candidateA)).isZero();
        assertThat(profileCount(candidateB)).isZero();
        assertThat(userSkills.countByUserId(candidateA.getId())).isZero();
    }

    @Test
    void concurrentWritesKeepSingleProfileAndSkill() throws Exception {
        var pool = Executors.newFixedThreadPool(2);
        try {
            var first = pool.submit(() -> send("PUT", PROFILE, PROFILE_BODY, candidateA, 200));
            var second = pool.submit(() -> send("PUT", PROFILE, PROFILE_BODY, candidateA, 200));
            assertThat(first.get(20, TimeUnit.SECONDS).path("id"))
                    .isEqualTo(second.get(20, TimeUnit.SECONDS).path("id"));
            assertThat(profileCount(candidateA)).isEqualTo(1);

            var firstAdd = pool.submit(() -> request("POST", SKILLS, addBody(skill.getId()), candidateA).statusCode());
            var secondAdd = pool.submit(() -> request("POST", SKILLS, addBody(skill.getId()), candidateA).statusCode());
            assertThat(java.util.List.of(firstAdd.get(20, TimeUnit.SECONDS), secondAdd.get(20, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(201, 409);
            assertThat(userSkills.countByUserId(candidateA.getId())).isEqualTo(1);
        } finally {
            pool.shutdownNow();
        }
    }

    private User createUser(Role role) {
        return users.save(new User(UUID.randomUUID() + "@profile.test", "unused-test-hash", "Profile test", role));
    }

    private int profileCount(User user) {
        return jdbc.queryForObject("select count(*) from candidate_profiles where user_id = ?",
                Integer.class, user.getId());
    }

    private String skillPath() {
        return SKILLS + "/" + skill.getId();
    }

    private String addBody(Long skillId) {
        return """
                {"skillId":%d,"proficiencyLevel":"INTERMEDIATE","yearsExperience":1.5}
                """.formatted(skillId);
    }

    private JsonNode send(String method, String path, String body, User user, int expectedStatus) throws Exception {
        var response = request(method, path, body, user);
        assertThat(response.statusCode()).as("%s %s -> %s", method, path, response.body()).isEqualTo(expectedStatus);
        if (expectedStatus == 204) {
            assertThat(response.body()).isEmpty();
            return mapper.nullNode();
        }
        return mapper.readTree(response.body());
    }

    private HttpResponse<String> request(String method, String path, String body, User user) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(15))
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        if (body != null) {
            builder.header("Content-Type", "application/json");
        }
        if (user != null) {
            builder.header("Authorization", "Bearer " + tokens.generateToken(user));
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
