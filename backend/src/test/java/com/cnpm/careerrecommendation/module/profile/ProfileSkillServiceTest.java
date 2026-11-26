package com.cnpm.careerrecommendation.module.profile;

import com.cnpm.careerrecommendation.common.entity.Skill;
import com.cnpm.careerrecommendation.common.entity.User;
import com.cnpm.careerrecommendation.common.exception.ApiException;
import com.cnpm.careerrecommendation.common.repository.SkillRepository;
import com.cnpm.careerrecommendation.entity.UserSkill;
import com.cnpm.careerrecommendation.module.profile.dto.AddSkillRequest;
import com.cnpm.careerrecommendation.module.profile.dto.UpdateSkillRequest;
import com.cnpm.careerrecommendation.module.profile.service.ProfileService;
import com.cnpm.careerrecommendation.repository.CandidateProfileRepository;
import com.cnpm.careerrecommendation.repository.UserSkillRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileSkillServiceTest {

    private static final long USER_ID = 10L;
    private static final long OTHER_USER_ID = 20L;
    private static final long SKILL_ID = 30L;

    @Mock CandidateProfileRepository profiles;
    @Mock UserSkillRepository userSkills;
    @Mock SkillRepository skills;
    @Mock EntityManager entityManager;

    private ProfileService service;
    private User owner;
    private Skill catalogSkill;

    @BeforeEach
    void setUp() {
        service = new ProfileService(profiles, userSkills, skills, entityManager);
        owner = user(USER_ID);
        catalogSkill = skill(SKILL_ID);
    }

    @Test
    void listsOnlySkillsForCurrentUser() {
        UserSkill owned = userSkill(owner, catalogSkill);
        when(userSkills.findByUserId(USER_ID)).thenReturn(List.of(owned));

        var result = service.getSkills(USER_ID);

        assertThat(result).singleElement().satisfies(skill -> {
            assertThat(skill.skillId()).isEqualTo(SKILL_ID);
            assertThat(skill.skillCode()).isEqualTo("SK-JAVA");
            assertThat(skill.proficiencyLevel()).isEqualTo(UserSkill.LEVEL_INTERMEDIATE);
        });
        verify(userSkills).findByUserId(USER_ID);
        verify(userSkills, never()).findByUserId(OTHER_USER_ID);
    }

    @Test
    void addsSkillSuccessfully() {
        lock(USER_ID, owner);
        when(skills.findById(SKILL_ID)).thenReturn(Optional.of(catalogSkill));
        when(userSkills.existsByUserIdAndSkillId(USER_ID, SKILL_ID)).thenReturn(false);
        when(userSkills.saveAndFlush(any(UserSkill.class))).thenAnswer(invocation -> {
            UserSkill saved = invocation.getArgument(0);
            saved.setCreatedAt(OffsetDateTime.now());
            return saved;
        });

        var result = service.addSkill(USER_ID, addRequest("ADVANCED", "2.50"));

        assertThat(result.skillId()).isEqualTo(SKILL_ID);
        assertThat(result.skillName()).isEqualTo("Java");
        assertThat(result.proficiencyLevel()).isEqualTo("ADVANCED");
        assertThat(result.yearsExperience()).isEqualByComparingTo("2.5");
    }

    @Test
    void rejectsDuplicateSkillWithBusinessCode() {
        lock(USER_ID, owner);
        when(skills.findById(SKILL_ID)).thenReturn(Optional.of(catalogSkill));
        when(userSkills.existsByUserIdAndSkillId(USER_ID, SKILL_ID)).thenReturn(true);

        assertApiException(() -> service.addSkill(USER_ID, addRequest("BEGINNER", "0")),
                HttpStatus.CONFLICT, "SKILL_ALREADY_EXISTS");
        verify(userSkills, never()).saveAndFlush(any());
    }

    @Test
    void rejectsMissingCatalogSkill() {
        lock(USER_ID, owner);
        when(skills.findById(SKILL_ID)).thenReturn(Optional.empty());

        assertApiException(() -> service.addSkill(USER_ID, addRequest("BEGINNER", "0")),
                HttpStatus.NOT_FOUND, "SKILL_NOT_FOUND");
    }

    @Test
    void updatesOwnedSkill() {
        UserSkill owned = userSkill(owner, catalogSkill);
        lock(USER_ID, owner);
        when(userSkills.findByUserIdAndSkillId(USER_ID, SKILL_ID)).thenReturn(Optional.of(owned));

        var result = service.updateSkill(USER_ID, SKILL_ID,
                new UpdateSkillRequest("EXPERT", new BigDecimal("4.5")));

        assertThat(result.proficiencyLevel()).isEqualTo("EXPERT");
        assertThat(result.yearsExperience()).isEqualByComparingTo("4.5");
    }

    @Test
    void updateMissingSkillReturns404() {
        lock(USER_ID, owner);
        when(userSkills.findByUserIdAndSkillId(USER_ID, SKILL_ID)).thenReturn(Optional.empty());

        assertApiException(() -> service.updateSkill(USER_ID, SKILL_ID,
                        new UpdateSkillRequest("EXPERT", null)),
                HttpStatus.NOT_FOUND, "SKILL_NOT_FOUND");
    }

    @Test
    void updateOtherUsersSkillReturns404() {
        User other = user(OTHER_USER_ID);
        lock(OTHER_USER_ID, other);
        when(userSkills.findByUserIdAndSkillId(OTHER_USER_ID, SKILL_ID)).thenReturn(Optional.empty());

        assertApiException(() -> service.updateSkill(OTHER_USER_ID, SKILL_ID,
                        new UpdateSkillRequest("EXPERT", null)),
                HttpStatus.NOT_FOUND, "SKILL_NOT_FOUND");
        verify(userSkills, never()).findById(any());
    }

    @Test
    void deletesOwnedSkill() {
        UserSkill owned = userSkill(owner, catalogSkill);
        lock(USER_ID, owner);
        when(userSkills.findByUserIdAndSkillId(USER_ID, SKILL_ID)).thenReturn(Optional.of(owned));

        service.deleteSkill(USER_ID, SKILL_ID);

        verify(userSkills).delete(owned);
    }

    @Test
    void deleteMissingSkillReturns404() {
        lock(USER_ID, owner);
        when(userSkills.findByUserIdAndSkillId(USER_ID, SKILL_ID)).thenReturn(Optional.empty());

        assertApiException(() -> service.deleteSkill(USER_ID, SKILL_ID),
                HttpStatus.NOT_FOUND, "SKILL_NOT_FOUND");
        verify(userSkills, never()).delete(any());
    }

    @Test
    void deleteOtherUsersSkillReturns404() {
        User other = user(OTHER_USER_ID);
        lock(OTHER_USER_ID, other);
        when(userSkills.findByUserIdAndSkillId(OTHER_USER_ID, SKILL_ID)).thenReturn(Optional.empty());

        assertApiException(() -> service.deleteSkill(OTHER_USER_ID, SKILL_ID),
                HttpStatus.NOT_FOUND, "SKILL_NOT_FOUND");
        verify(userSkills, never()).findById(any());
    }

    @Test
    void rejectsNegativeYearsInsideService() {
        assertApiException(() -> service.addSkill(USER_ID, addRequest("BEGINNER", "-0.1")),
                HttpStatus.BAD_REQUEST, "INVALID_SKILL_REQUEST");
        verify(entityManager, never()).find(any(), any(), any(LockModeType.class));
    }

    @Test
    void rejectsInvalidProficiencyInsideService() {
        assertApiException(() -> service.addSkill(USER_ID, addRequest("MASTER", "1")),
                HttpStatus.BAD_REQUEST, "INVALID_SKILL_REQUEST");
        verify(entityManager, never()).find(any(), any(), any(LockModeType.class));
    }

    @Test
    void rejectsNullAddRequestBeforeDatabaseAccess() {
        assertApiException(() -> service.addSkill(USER_ID, null),
                HttpStatus.BAD_REQUEST, "INVALID_SKILL_REQUEST");
        verify(entityManager, never()).find(any(), any(), any(LockModeType.class));
    }

    @Test
    void rejectsInvalidAddSkillIdsBeforeDatabaseAccess() {
        for (BigDecimal invalidId : List.of(BigDecimal.ZERO, new BigDecimal("1.5"),
                new BigDecimal("9223372036854775808"))) {
            assertApiException(() -> service.addSkill(USER_ID,
                            new AddSkillRequest(invalidId, "BEGINNER", BigDecimal.ZERO)),
                    HttpStatus.BAD_REQUEST, "INVALID_SKILL_REQUEST");
        }
        verify(entityManager, never()).find(any(), any(), any(LockModeType.class));
    }

    @Test
    void rejectsMissingRequiredAddFieldsBeforeDatabaseAccess() {
        assertApiException(() -> service.addSkill(USER_ID,
                        new AddSkillRequest(null, "BEGINNER", null)),
                HttpStatus.BAD_REQUEST, "INVALID_SKILL_REQUEST");
        assertApiException(() -> service.addSkill(USER_ID,
                        new AddSkillRequest(BigDecimal.valueOf(SKILL_ID), null, null)),
                HttpStatus.BAD_REQUEST, "INVALID_SKILL_REQUEST");
        verify(entityManager, never()).find(any(), any(), any(LockModeType.class));
    }

    @Test
    void rejectsYearsAboveDatabasePrecisionAndExtraFractionBeforeDatabaseAccess() {
        for (String years : List.of("1000", "1.55")) {
            assertApiException(() -> service.addSkill(USER_ID, addRequest("BEGINNER", years)),
                    HttpStatus.BAD_REQUEST, "INVALID_SKILL_REQUEST");
        }
        verify(entityManager, never()).find(any(), any(), any(LockModeType.class));
    }

    @Test
    void acceptsMaximumYearsAtServiceBoundary() {
        lock(USER_ID, owner);
        when(skills.findById(SKILL_ID)).thenReturn(Optional.of(catalogSkill));
        when(userSkills.existsByUserIdAndSkillId(USER_ID, SKILL_ID)).thenReturn(false);
        when(userSkills.saveAndFlush(any(UserSkill.class))).thenAnswer(invocation -> {
            UserSkill saved = invocation.getArgument(0);
            saved.setCreatedAt(OffsetDateTime.now());
            return saved;
        });

        var result = service.addSkill(USER_ID, addRequest("EXPERT", "999.9"));

        assertThat(result.yearsExperience()).isEqualByComparingTo("999.9");
    }

    @Test
    void updateRejectsNegativeYearsBeforeDatabaseAccess() {
        assertApiException(() -> service.updateSkill(USER_ID, SKILL_ID,
                        new UpdateSkillRequest(null, new BigDecimal("-0.1"))),
                HttpStatus.BAD_REQUEST, "INVALID_SKILL_REQUEST");
        verify(entityManager, never()).find(any(), any(), any(LockModeType.class));
        verify(userSkills, never()).findByUserIdAndSkillId(any(), any());
    }

    @Test
    void updateRejectsInvalidProficiencyBeforeDatabaseAccess() {
        assertApiException(() -> service.updateSkill(USER_ID, SKILL_ID,
                        new UpdateSkillRequest("MASTER", null)),
                HttpStatus.BAD_REQUEST, "INVALID_SKILL_REQUEST");
        verify(entityManager, never()).find(any(), any(), any(LockModeType.class));
        verify(userSkills, never()).findByUserIdAndSkillId(any(), any());
    }

    @Test
    void updateRejectsNullRequestAndInvalidYearsBeforeDatabaseAccess() {
        assertApiException(() -> service.updateSkill(USER_ID, SKILL_ID, null),
                HttpStatus.BAD_REQUEST, "INVALID_SKILL_REQUEST");
        for (String years : List.of("1000", "1.55")) {
            assertApiException(() -> service.updateSkill(USER_ID, SKILL_ID,
                            new UpdateSkillRequest(null, new BigDecimal(years))),
                    HttpStatus.BAD_REQUEST, "INVALID_SKILL_REQUEST");
        }
        verify(entityManager, never()).find(any(), any(), any(LockModeType.class));
        verify(userSkills, never()).findByUserIdAndSkillId(any(), any());
    }

    @Test
    void rejectsNullAuthenticatedUserBeforeEntityManagerAccess() {
        assertApiException(() -> service.addSkill(null, addRequest("BEGINNER", "0")),
                HttpStatus.UNAUTHORIZED, null);
        verify(entityManager, never()).find(any(), any(), any(LockModeType.class));
        verify(userSkills, never()).saveAndFlush(any());
    }

    @Test
    void mapsDatabaseDuplicateRaceToConflict() {
        lock(USER_ID, owner);
        when(skills.findById(SKILL_ID)).thenReturn(Optional.of(catalogSkill));
        when(userSkills.existsByUserIdAndSkillId(USER_ID, SKILL_ID)).thenReturn(false);
        when(userSkills.saveAndFlush(any(UserSkill.class)))
                .thenThrow(integrityViolation("uq_user_skills_user_skill"));

        assertApiException(() -> service.addSkill(USER_ID, addRequest("BEGINNER", "0")),
                HttpStatus.CONFLICT, "SKILL_ALREADY_EXISTS");
    }

    @Test
    void mapsDatabaseDuplicateConstraintCaseInsensitively() {
        lock(USER_ID, owner);
        when(skills.findById(SKILL_ID)).thenReturn(Optional.of(catalogSkill));
        when(userSkills.existsByUserIdAndSkillId(USER_ID, SKILL_ID)).thenReturn(false);
        when(userSkills.saveAndFlush(any(UserSkill.class)))
                .thenThrow(integrityViolation("UQ_USER_SKILLS_USER_SKILL"));

        assertApiException(() -> service.addSkill(USER_ID, addRequest("BEGINNER", "0")),
                HttpStatus.CONFLICT, "SKILL_ALREADY_EXISTS");
    }

    @Test
    void rethrowsNonDuplicateDatabaseIntegrityViolation() {
        lock(USER_ID, owner);
        when(skills.findById(SKILL_ID)).thenReturn(Optional.of(catalogSkill));
        when(userSkills.existsByUserIdAndSkillId(USER_ID, SKILL_ID)).thenReturn(false);
        DataIntegrityViolationException failure = integrityViolation("fk_user_skills_skill_id");
        when(userSkills.saveAndFlush(any(UserSkill.class))).thenThrow(failure);

        assertThatThrownBy(() -> service.addSkill(USER_ID, addRequest("BEGINNER", "0")))
                .isSameAs(failure);
    }

    @Test
    void rethrowsIntegrityViolationWithoutConstraintName() {
        lock(USER_ID, owner);
        when(skills.findById(SKILL_ID)).thenReturn(Optional.of(catalogSkill));
        when(userSkills.existsByUserIdAndSkillId(USER_ID, SKILL_ID)).thenReturn(false);
        DataIntegrityViolationException failure = integrityViolation(null);
        when(userSkills.saveAndFlush(any(UserSkill.class))).thenThrow(failure);

        assertThatThrownBy(() -> service.addSkill(USER_ID, addRequest("BEGINNER", "0")))
                .isSameAs(failure);
    }

    private void lock(long userId, User user) {
        when(entityManager.find(User.class, userId, LockModeType.PESSIMISTIC_WRITE)).thenReturn(user);
    }

    private AddSkillRequest addRequest(String level, String years) {
        return new AddSkillRequest(BigDecimal.valueOf(SKILL_ID), level,
                years == null ? null : new BigDecimal(years));
    }

    private DataIntegrityViolationException integrityViolation(String constraintName) {
        ConstraintViolationException cause = new ConstraintViolationException(
                "constraint violation", new SQLException("test constraint"), constraintName);
        return new DataIntegrityViolationException("write failed", cause);
    }

    private User user(long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private Skill skill(long id) {
        Skill skill = new Skill();
        skill.setId(id);
        skill.setCode("SK-JAVA");
        skill.setName("Java");
        return skill;
    }

    private UserSkill userSkill(User user, Skill skill) {
        return new UserSkill(user, skill, UserSkill.LEVEL_INTERMEDIATE, new BigDecimal("1.5"));
    }

    private void assertApiException(Runnable action, HttpStatus status, String errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(ApiException.class, exception -> {
                    assertThat(exception.getStatus()).isEqualTo(status);
                    assertThat(exception.getErrorCode()).isEqualTo(errorCode);
                });
    }
}
