package com.cnpm.careerrecommendation.module.taxonomy;

import com.cnpm.careerrecommendation.common.entity.Skill;
import com.cnpm.careerrecommendation.common.entity.SkillCategory;
import com.cnpm.careerrecommendation.common.exception.ApiException;
import com.cnpm.careerrecommendation.common.exception.ResourceNotFoundException;
import com.cnpm.careerrecommendation.common.repository.SkillCategoryRepository;
import com.cnpm.careerrecommendation.common.repository.SkillRepository;
import com.cnpm.careerrecommendation.entity.Occupation;
import com.cnpm.careerrecommendation.entity.OccupationGroup;
import com.cnpm.careerrecommendation.entity.OccupationSkill;
import com.cnpm.careerrecommendation.module.taxonomy.dto.AdminOccupationRequest;
import com.cnpm.careerrecommendation.module.taxonomy.dto.AdminOccupationResponse;
import com.cnpm.careerrecommendation.module.taxonomy.dto.AdminSkillRequest;
import com.cnpm.careerrecommendation.module.taxonomy.dto.AdminSkillResponse;
import com.cnpm.careerrecommendation.module.taxonomy.dto.OccupationSkillRequest;
import com.cnpm.careerrecommendation.module.taxonomy.dto.UpdateOccupationSkillRequest;
import com.cnpm.careerrecommendation.module.taxonomy.service.AdminTaxonomyService;
import com.cnpm.careerrecommendation.repository.OccupationGroupRepository;
import com.cnpm.careerrecommendation.repository.OccupationRepository;
import com.cnpm.careerrecommendation.repository.OccupationSkillRepository;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminTaxonomyServiceTest {

    @Mock
    private OccupationRepository occupationRepository;

    @Mock
    private OccupationGroupRepository occupationGroupRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private SkillCategoryRepository skillCategoryRepository;

    @Mock
    private OccupationSkillRepository occupationSkillRepository;

    private AdminTaxonomyService service;

    @BeforeEach
    void setUp() {
        service = new AdminTaxonomyService(
                occupationRepository,
                occupationGroupRepository,
                skillRepository,
                skillCategoryRepository,
                occupationSkillRepository
        );
    }

    @Test
    @DisplayName("createOccupation: trims fields, resolves group, normalizes salary scales, and returns response")
    void createOccupation_Success() {
        OccupationGroup group = new OccupationGroup("GRP-SWE", "Kỹ thuật phần mềm", "Desc");
        group.setId(2L);

        when(occupationRepository.existsByCode("SOC-NEW-01")).thenReturn(false);
        when(occupationGroupRepository.findById(2L)).thenReturn(Optional.of(group));
        when(occupationRepository.saveAndFlush(any(Occupation.class))).thenAnswer(inv -> {
            Occupation occ = inv.getArgument(0);
            occ.setId(50L);
            occ.setCreatedAt(OffsetDateTime.parse("2026-11-26T10:00:00Z"));
            occ.setUpdatedAt(OffsetDateTime.parse("2026-11-26T10:00:00Z"));
            return occ;
        });

        AdminOccupationRequest request = new AdminOccupationRequest(
                "  SOC-NEW-01 ",
                " Platform Engineer ",
                new BigDecimal("2"),
                "   ",
                " Cloud & DevOps ",
                new BigDecimal("20000000"),
                new BigDecimal("42000000.5")
        );

        AdminOccupationResponse response = service.createOccupation(request);

        assertEquals(50L, response.id());
        assertEquals("SOC-NEW-01", response.code());
        assertEquals("Platform Engineer", response.title());
        assertEquals(2L, response.groupId());
        assertEquals("Kỹ thuật phần mềm", response.groupName());
        assertNull(response.description());
        assertEquals("Cloud & DevOps", response.industry());
        assertEquals(new BigDecimal("20000000.00"), response.entrySalaryAvg());
        assertEquals(new BigDecimal("42000000.50"), response.midSalaryAvg());
    }

    @Test
    @DisplayName("createOccupation: duplicate code throws 409 Conflict")
    void createOccupation_DuplicateCodeThrows409() {
        when(occupationRepository.existsByCode("SOC-15-1252")).thenReturn(true);

        AdminOccupationRequest request = new AdminOccupationRequest(
                "SOC-15-1252", "Duplicate", null, null, null, null, null);

        ApiException ex = assertThrows(ApiException.class, () -> service.createOccupation(request));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("OCCUPATION_CODE_ALREADY_EXISTS", ex.getErrorCode());
    }

    @Test
    @DisplayName("createOccupation: concurrent DB unique constraint translates to 409 Conflict")
    void createOccupation_ConcurrentConstraintViolationThrows409() {
        when(occupationRepository.existsByCode("SOC-RACE")).thenReturn(false);
        ConstraintViolationException cve = new ConstraintViolationException(
                "duplicate", new SQLException("duplicate"), "occupations_code_key");
        when(occupationRepository.saveAndFlush(any(Occupation.class)))
                .thenThrow(new DataIntegrityViolationException("conflict", cve));

        AdminOccupationRequest request = new AdminOccupationRequest(
                "SOC-RACE", "Race Occ", null, null, null, null, null);

        ApiException ex = assertThrows(ApiException.class, () -> service.createOccupation(request));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("OCCUPATION_CODE_ALREADY_EXISTS", ex.getErrorCode());
    }

    @Test
    @DisplayName("createOccupation: entrySalaryAvg > midSalaryAvg throws 400 Bad Request")
    void createOccupation_InvalidSalaryRangeThrows400() {
        AdminOccupationRequest request = new AdminOccupationRequest(
                "SOC-SAL-ERR", "Test", null, null, null,
                new BigDecimal("40000000"), new BigDecimal("20000000"));

        ApiException ex = assertThrows(ApiException.class, () -> service.createOccupation(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("INVALID_OCCUPATION_REQUEST", ex.getErrorCode());
    }

    @Test
    @DisplayName("createOccupation: missing groupId throws 404 Not Found")
    void createOccupation_GroupNotFoundThrows404() {
        when(occupationRepository.existsByCode("SOC-NO-GRP")).thenReturn(false);
        when(occupationGroupRepository.findById(999L)).thenReturn(Optional.empty());

        AdminOccupationRequest request = new AdminOccupationRequest(
                "SOC-NO-GRP", "Test", new BigDecimal("999"), null, null, null, null);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> service.createOccupation(request));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    @DisplayName("updateOccupation: keeps own code successfully and clears optional group when null")
    void updateOccupation_SameCodeSuccess() {
        OccupationGroup oldGroup = new OccupationGroup("GRP-SWE", "SWE", "Desc");
        oldGroup.setId(1L);
        Occupation existing = new Occupation("SOC-15-1252", "Old Title", "IT", oldGroup);
        existing.setId(10L);
        existing.setCreatedAt(OffsetDateTime.parse("2026-11-20T00:00:00Z"));
        existing.setUpdatedAt(OffsetDateTime.parse("2026-11-20T00:00:00Z"));

        when(occupationRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(occupationRepository.findByCode("SOC-15-1252")).thenReturn(Optional.of(existing));
        when(occupationRepository.saveAndFlush(existing)).thenReturn(existing);

        AdminOccupationRequest request = new AdminOccupationRequest(
                "SOC-15-1252", "Updated Title", null, "Updated desc", "Fintech",
                new BigDecimal("18000000"), new BigDecimal("36000000"));

        AdminOccupationResponse response = service.updateOccupation(10L, request);

        assertEquals(10L, response.id());
        assertEquals("Updated Title", response.title());
        assertNull(response.groupId());
        assertNull(response.groupName());
        assertEquals("Fintech", response.industry());
    }

    @Test
    @DisplayName("updateOccupation: colliding code owned by another occupation throws 409 Conflict")
    void updateOccupation_CollidingCodeThrows409() {
        Occupation target = new Occupation("SOC-1", "Target", "IT", null);
        target.setId(10L);
        Occupation other = new Occupation("SOC-2", "Other", "IT", null);
        other.setId(20L);

        when(occupationRepository.findById(10L)).thenReturn(Optional.of(target));
        when(occupationRepository.findByCode("SOC-2")).thenReturn(Optional.of(other));

        AdminOccupationRequest request = new AdminOccupationRequest(
                "SOC-2", "Updated", null, null, null, null, null);

        ApiException ex = assertThrows(ApiException.class, () -> service.updateOccupation(10L, request));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("OCCUPATION_CODE_ALREADY_EXISTS", ex.getErrorCode());
    }

    @Test
    @DisplayName("deleteOccupation: deletes and flushes existing occupation, throws 404 when missing")
    void deleteOccupation_SuccessAndNotFound() {
        Occupation existing = new Occupation("SOC-DEL", "Delete Me", "IT", null);
        existing.setId(77L);
        when(occupationRepository.findById(77L)).thenReturn(Optional.of(existing));
        when(occupationRepository.findById(88L)).thenReturn(Optional.empty());

        service.deleteOccupation(77L);
        verify(occupationRepository).delete(existing);
        verify(occupationRepository).flush();

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> service.deleteOccupation(88L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    @DisplayName("createSkill: normalizes skillType, resolves category, derives standardSource")
    void createSkill_Success() {
        SkillCategory cat = new SkillCategory("CAT-CLOUD", "Cloud & DevOps", "Desc");
        cat.setId(4L);

        when(skillRepository.existsByCode("SK-TERRAFORM")).thenReturn(false);
        when(skillCategoryRepository.findById(4L)).thenReturn(Optional.of(cat));
        when(skillRepository.saveAndFlush(any(Skill.class))).thenAnswer(inv -> {
            Skill skill = inv.getArgument(0);
            skill.setId(99L);
            skill.setCreatedAt(OffsetDateTime.parse("2026-11-26T10:00:00Z"));
            skill.setUpdatedAt(OffsetDateTime.parse("2026-11-26T10:00:00Z"));
            return skill;
        });

        AdminSkillRequest request = new AdminSkillRequest(
                " SK-TERRAFORM ",
                " Terraform IaC ",
                new BigDecimal("4"),
                " domain ",
                " Infrastructure provisioning "
        );

        AdminSkillResponse response = service.createSkill(request);

        assertEquals(99L, response.id());
        assertEquals("SK-TERRAFORM", response.code());
        assertEquals("Terraform IaC", response.name());
        assertEquals(4L, response.categoryId());
        assertEquals("Cloud & DevOps", response.categoryName());
        assertEquals("DOMAIN", response.skillType());
        assertEquals("ESCO", response.standardSource());
    }

    @Test
    @DisplayName("createSkill: invalid skillType throws 400 and duplicate code throws 409")
    void createSkill_ValidationAndDuplicate() {
        AdminSkillRequest badType = new AdminSkillRequest("SK-1", "Skill 1", null, "INVALID", null);
        ApiException badEx = assertThrows(ApiException.class, () -> service.createSkill(badType));
        assertEquals(HttpStatus.BAD_REQUEST, badEx.getStatus());
        assertEquals("INVALID_SKILL_REQUEST", badEx.getErrorCode());

        when(skillRepository.existsByCode("SK-JAVA")).thenReturn(true);
        AdminSkillRequest dup = new AdminSkillRequest("SK-JAVA", "Java", null, null, null);
        ApiException dupEx = assertThrows(ApiException.class, () -> service.createSkill(dup));
        assertEquals(HttpStatus.CONFLICT, dupEx.getStatus());
        assertEquals("SKILL_CODE_ALREADY_EXISTS", dupEx.getErrorCode());
    }

    @Test
    @DisplayName("updateSkill & deleteSkill: updates existing skill and throws 404 when not found")
    void updateAndDeleteSkill() {
        Skill existing = new Skill("SK-OLD", "Old Skill", Skill.TYPE_TECHNICAL, null);
        existing.setId(15L);
        existing.setCreatedAt(OffsetDateTime.parse("2026-11-20T00:00:00Z"));
        existing.setUpdatedAt(OffsetDateTime.parse("2026-11-20T00:00:00Z"));

        when(skillRepository.findById(15L)).thenReturn(Optional.of(existing));
        when(skillRepository.findByCode("SOC-2B1")).thenReturn(Optional.empty());
        when(skillRepository.saveAndFlush(existing)).thenReturn(existing);

        AdminSkillRequest req = new AdminSkillRequest("SOC-2B1", "Systems Analysis", null, null, null);
        AdminSkillResponse updated = service.updateSkill(15L, req);

        assertEquals("SOC-2B1", updated.code());
        assertEquals("TECHNICAL", updated.skillType());
        assertEquals("O*NET", updated.standardSource());

        service.deleteSkill(15L);
        verify(skillRepository).delete(existing);
        verify(skillRepository).flush();

        when(skillRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.deleteSkill(404L));
    }

    @Test
    @DisplayName("assignOccupationSkill: creates new mapping with defaults and upserts existing mapping")
    void assignOccupationSkill_CreateAndUpsert() {
        Occupation occ = new Occupation("SOC-15-1252", "Software Developer", "IT", null);
        occ.setId(1L);
        Skill skill = new Skill("SK-JAVA", "Java", Skill.TYPE_TECHNICAL, null);
        skill.setId(12L);

        when(occupationRepository.findById(1L)).thenReturn(Optional.of(occ));
        when(skillRepository.findById(12L)).thenReturn(Optional.of(skill));
        when(occupationSkillRepository.findByOccupationIdAndSkillId(1L, 12L)).thenReturn(Optional.empty());
        when(occupationSkillRepository.saveAndFlush(any(OccupationSkill.class))).thenAnswer(inv -> {
            OccupationSkill mapping = inv.getArgument(0);
            mapping.setId(501L);
            mapping.setCreatedAt(OffsetDateTime.parse("2026-11-26T10:00:00Z"));
            mapping.setUpdatedAt(OffsetDateTime.parse("2026-11-26T10:00:00Z"));
            return mapping;
        });

        AdminTaxonomyService.OccupationSkillAssignment first = service.assignOccupationSkill(
                1L, new OccupationSkillRequest(new BigDecimal("12"), null, null));

        assertTrue(first.created());
        assertEquals(new BigDecimal("1.00"), first.mapping().importanceWeight());
        assertTrue(first.mapping().isRequired());

        OccupationSkill persisted = new OccupationSkill(occ, skill, new BigDecimal("1.00"), true);
        persisted.setId(501L);
        when(occupationSkillRepository.findByOccupationIdAndSkillId(1L, 12L))
                .thenReturn(Optional.of(persisted));

        AdminTaxonomyService.OccupationSkillAssignment second = service.assignOccupationSkill(
                1L, new OccupationSkillRequest(new BigDecimal("12"), new BigDecimal("0.00"), false));

        assertFalse(second.created());
        assertEquals(new BigDecimal("0.00"), second.mapping().importanceWeight());
        assertFalse(second.mapping().isRequired());
    }

    @Test
    @DisplayName("assignOccupationSkill: weight outside [0.0, 1.0] or > 2 decimals throws 400")
    void assignOccupationSkill_InvalidWeightThrows400() {
        for (String badWeight : List.of("-0.01", "1.01", "0.555")) {
            OccupationSkillRequest req = new OccupationSkillRequest(
                    new BigDecimal("12"), new BigDecimal(badWeight), true);
            ApiException ex = assertThrows(ApiException.class,
                    () -> service.assignOccupationSkill(1L, req));
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
            assertEquals("INVALID_OCCUPATION_SKILL_REQUEST", ex.getErrorCode());
        }
    }

    @Test
    @DisplayName("updateOccupationSkill & removeOccupationSkill: handles valid updates/deletes and missing resources")
    void updateAndRemoveOccupationSkill() {
        Occupation occ = new Occupation("SOC-1", "Dev", "IT", null);
        occ.setId(1L);
        Skill skill = new Skill("SK-1", "Java", Skill.TYPE_TECHNICAL, null);
        skill.setId(2L);
        OccupationSkill mapping = new OccupationSkill(occ, skill, new BigDecimal("0.80"), true);
        mapping.setId(300L);

        when(occupationRepository.findById(1L)).thenReturn(Optional.of(occ));
        when(occupationSkillRepository.findByOccupationIdAndSkillId(1L, 2L)).thenReturn(Optional.of(mapping));
        when(occupationSkillRepository.saveAndFlush(mapping)).thenReturn(mapping);

        var updated = service.updateOccupationSkill(
                1L, 2L, new UpdateOccupationSkillRequest(new BigDecimal("0.45"), false));
        assertEquals(new BigDecimal("0.45"), updated.importanceWeight());
        assertFalse(updated.isRequired());

        service.removeOccupationSkill(1L, 2L);
        verify(occupationSkillRepository).delete(mapping);
        verify(occupationSkillRepository).flush();

        when(occupationSkillRepository.findByOccupationIdAndSkillId(1L, 99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.removeOccupationSkill(1L, 99L));
    }

    @Test
    @DisplayName("listOccupationGroups & listSkillCategories: sort ascending by id")
    void listGroupsAndCategories() {
        OccupationGroup group = new OccupationGroup("GRP-SWE", "SWE", "Desc");
        group.setId(1L);
        SkillCategory cat = new SkillCategory("CAT-PROG", "Programming", "Desc");
        cat.setId(1L);

        when(occupationGroupRepository.findAll(any(Sort.class))).thenReturn(List.of(group));
        when(skillCategoryRepository.findAll(any(Sort.class))).thenReturn(List.of(cat));

        assertEquals(1, service.listOccupationGroups().size());
        assertEquals(1, service.listSkillCategories().size());

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(occupationGroupRepository).findAll(sortCaptor.capture());
        assertNotNull(sortCaptor.getValue().getOrderFor("id"));
    }
}
