package com.cnpm.careerrecommendation.entity;

import com.cnpm.careerrecommendation.repository.*;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verification test suite for JPA Entity definitions, Table annotations,
 * relationships, FetchType.LAZY rules, and matching Spring Data JPA Repositories.
 */
class EntityMappingTest {

    @Test
    @DisplayName("Verify all 10 JPA entities are annotated with @Entity and correct @Table names")
    void testEntityTableNames() {
        Map<Class<?>, String> expectedTables = Map.of(
            User.class, "users",
            CandidateProfile.class, "candidate_profiles",
            SkillCategory.class, "skill_categories",
            Skill.class, "skills",
            UserSkill.class, "user_skills",
            OccupationGroup.class, "occupation_groups",
            Occupation.class, "occupations",
            OccupationSkill.class, "occupation_skills",
            MarketDemandIndicator.class, "market_demand_indicators",
            RecommendationLog.class, "recommendation_logs"
        );

        for (Map.Entry<Class<?>, String> entry : expectedTables.entrySet()) {
            Class<?> clazz = entry.getKey();
            String expectedTable = entry.getValue();

            assertTrue(clazz.isAnnotationPresent(Entity.class), clazz.getSimpleName() + " must have @Entity");
            assertTrue(clazz.isAnnotationPresent(Table.class), clazz.getSimpleName() + " must have @Table");
            assertEquals(expectedTable, clazz.getAnnotation(Table.class).name(),
                clazz.getSimpleName() + " table name mismatch");
        }
    }

    @Test
    @DisplayName("Verify all @ManyToOne and @OneToOne relationships enforce FetchType.LAZY per conventions")
    void testLazyLoadingConvention() {
        List<Class<?>> entityClasses = List.of(
            User.class, CandidateProfile.class, SkillCategory.class, Skill.class,
            UserSkill.class, OccupationGroup.class, Occupation.class, OccupationSkill.class,
            MarketDemandIndicator.class, RecommendationLog.class
        );

        for (Class<?> clazz : entityClasses) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(ManyToOne.class)) {
                    ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
                    assertEquals(FetchType.LAZY, manyToOne.fetch(),
                        clazz.getSimpleName() + "." + field.getName() + " must have fetch = FetchType.LAZY");
                }
                if (field.isAnnotationPresent(OneToOne.class)) {
                    OneToOne oneToOne = field.getAnnotation(OneToOne.class);
                    assertEquals(FetchType.LAZY, oneToOne.fetch(),
                        clazz.getSimpleName() + "." + field.getName() + " must have fetch = FetchType.LAZY");
                }
            }
        }
    }

    @Test
    @DisplayName("Verify composite unique constraints match V1 schema DDL")
    void testUniqueConstraints() {
        // UserSkill
        Table userSkillTable = UserSkill.class.getAnnotation(Table.class);
        boolean hasUserSkillUnique = Arrays.stream(userSkillTable.uniqueConstraints())
            .anyMatch(uc -> "uq_user_skills_user_skill".equals(uc.name())
                && Arrays.equals(uc.columnNames(), new String[]{"user_id", "skill_id"}));
        assertTrue(hasUserSkillUnique, "UserSkill must have uq_user_skills_user_skill constraint");

        // OccupationSkill
        Table occSkillTable = OccupationSkill.class.getAnnotation(Table.class);
        boolean hasOccSkillUnique = Arrays.stream(occSkillTable.uniqueConstraints())
            .anyMatch(uc -> "uq_occupation_skills_occ_skill".equals(uc.name())
                && Arrays.equals(uc.columnNames(), new String[]{"occupation_id", "skill_id"}));
        assertTrue(hasOccSkillUnique, "OccupationSkill must have uq_occupation_skills_occ_skill constraint");

        // MarketDemandIndicator
        Table marketTable = MarketDemandIndicator.class.getAnnotation(Table.class);
        boolean hasMarketUnique = Arrays.stream(marketTable.uniqueConstraints())
            .anyMatch(uc -> "uq_market_demand_occ_region_date".equals(uc.name())
                && Arrays.equals(uc.columnNames(), new String[]{"occupation_id", "region", "snapshot_date"}));
        assertTrue(hasMarketUnique, "MarketDemandIndicator must have uq_market_demand_occ_region_date constraint");
    }

    @Test
    @DisplayName("Verify primary keys are identity generated on Long id")
    void testPrimaryKeys() throws NoSuchFieldException {
        List<Class<?>> entityClasses = List.of(
            User.class, CandidateProfile.class, SkillCategory.class, Skill.class,
            UserSkill.class, OccupationGroup.class, Occupation.class, OccupationSkill.class,
            MarketDemandIndicator.class, RecommendationLog.class
        );

        for (Class<?> clazz : entityClasses) {
            Field idField = clazz.getDeclaredField("id");
            assertEquals(Long.class, idField.getType(), clazz.getSimpleName() + ".id must be Long");
            assertTrue(idField.isAnnotationPresent(Id.class), clazz.getSimpleName() + ".id must have @Id");
            assertTrue(idField.isAnnotationPresent(GeneratedValue.class), clazz.getSimpleName() + ".id must have @GeneratedValue");
            assertEquals(GenerationType.IDENTITY, idField.getAnnotation(GeneratedValue.class).strategy(),
                clazz.getSimpleName() + ".id strategy must be IDENTITY");
        }
    }

    @Test
    @DisplayName("Verify numeric column precision and scale match V1 DDL")
    void testNumericPrecisionAndScale() throws NoSuchFieldException {
        // CandidateProfile.yearsOfExperience -> NUMERIC(4,1)
        Field expField = CandidateProfile.class.getDeclaredField("yearsOfExperience");
        Column expCol = expField.getAnnotation(Column.class);
        assertEquals(4, expCol.precision());
        assertEquals(1, expCol.scale());

        // OccupationSkill.importanceWeight -> NUMERIC(3,2)
        Field weightField = OccupationSkill.class.getDeclaredField("importanceWeight");
        Column weightCol = weightField.getAnnotation(Column.class);
        assertEquals(3, weightCol.precision());
        assertEquals(2, weightCol.scale());

        // MarketDemandIndicator.demandScore -> NUMERIC(5,2)
        Field scoreField = MarketDemandIndicator.class.getDeclaredField("demandScore");
        Column scoreCol = scoreField.getAnnotation(Column.class);
        assertEquals(5, scoreCol.precision());
        assertEquals(2, scoreCol.scale());

        // RecommendationLog.matchScore -> NUMERIC(5,2)
        Field matchField = RecommendationLog.class.getDeclaredField("matchScore");
        Column matchCol = matchField.getAnnotation(Column.class);
        assertEquals(5, matchCol.precision());
        assertEquals(2, matchCol.scale());
    }

    @Test
    @DisplayName("Verify all 10 Repositories extend JpaRepository with correct types")
    void testRepositories() {
        Map<Class<?>, Class<?>> repoToEntityMap = Map.of(
            UserRepository.class, User.class,
            CandidateProfileRepository.class, CandidateProfile.class,
            SkillCategoryRepository.class, SkillCategory.class,
            SkillRepository.class, Skill.class,
            UserSkillRepository.class, UserSkill.class,
            OccupationGroupRepository.class, OccupationGroup.class,
            OccupationRepository.class, Occupation.class,
            OccupationSkillRepository.class, OccupationSkill.class,
            MarketDemandIndicatorRepository.class, MarketDemandIndicator.class,
            RecommendationLogRepository.class, RecommendationLog.class
        );

        for (Class<?> repoInterface : repoToEntityMap.keySet()) {
            assertTrue(repoInterface.isInterface(), repoInterface.getSimpleName() + " must be an interface");
            assertTrue(JpaRepository.class.isAssignableFrom(repoInterface),
                repoInterface.getSimpleName() + " must extend JpaRepository");
        }
    }

    @Test
    @DisplayName("Verify entity instantiation and default properties")
    void testEntityLifecycleAndDefaults() {
        User user = new User("test@example.com", "hash", "Nguyen Van A", null);
        assertEquals(User.ROLE_CANDIDATE, user.getRole());
        assertTrue(user.getIsActive());

        CandidateProfile profile = new CandidateProfile(user);
        assertEquals(BigDecimal.ZERO, profile.getYearsOfExperience());
        assertEquals(CandidateProfile.WORK_MODE_HYBRID, profile.getPreferredWorkMode());

        Skill skill = new Skill("SK-001", "Java", null, null);
        assertEquals(Skill.TYPE_TECHNICAL, skill.getSkillType());

        MarketDemandIndicator indicator = new MarketDemandIndicator(null, null, new BigDecimal("85.50"), LocalDate.now());
        assertEquals(MarketDemandIndicator.REGION_NATIONAL, indicator.getRegion());
        assertEquals(0, indicator.getOpenJobPostingsCount());
    }
}

