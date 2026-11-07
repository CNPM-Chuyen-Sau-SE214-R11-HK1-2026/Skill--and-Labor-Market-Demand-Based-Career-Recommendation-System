package com.cnpm.careerrecommendation.module.taxonomy;

import com.cnpm.careerrecommendation.common.dto.PageResponse;
import com.cnpm.careerrecommendation.common.entity.Skill;
import com.cnpm.careerrecommendation.common.entity.SkillCategory;
import com.cnpm.careerrecommendation.common.exception.ApiException;
import com.cnpm.careerrecommendation.common.exception.ResourceNotFoundException;
import com.cnpm.careerrecommendation.common.repository.SkillRepository;
import com.cnpm.careerrecommendation.entity.MarketDemandIndicator;
import com.cnpm.careerrecommendation.entity.Occupation;
import com.cnpm.careerrecommendation.entity.OccupationGroup;
import com.cnpm.careerrecommendation.entity.OccupationSkill;
import com.cnpm.careerrecommendation.module.taxonomy.dto.OccupationDetailResponse;
import com.cnpm.careerrecommendation.module.taxonomy.dto.OccupationSummaryResponse;
import com.cnpm.careerrecommendation.module.taxonomy.dto.SkillDictionaryResponse;
import com.cnpm.careerrecommendation.module.taxonomy.service.TaxonomyService;
import com.cnpm.careerrecommendation.repository.MarketDemandIndicatorRepository;
import com.cnpm.careerrecommendation.repository.OccupationRepository;
import com.cnpm.careerrecommendation.repository.OccupationSkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaxonomyServiceTest {

    @Mock
    private OccupationRepository occupationRepository;

    @Mock
    private OccupationSkillRepository occupationSkillRepository;

    @Mock
    private MarketDemandIndicatorRepository marketDemandIndicatorRepository;

    @Mock
    private SkillRepository skillRepository;

    private TaxonomyService taxonomyService;

    @BeforeEach
    void setUp() {
        taxonomyService = new TaxonomyService(
            occupationRepository,
            occupationSkillRepository,
            marketDemandIndicatorRepository,
            skillRepository
        );
    }

    @Test
    @DisplayName("searchOccupations: successfully returns paginated list with demand metrics")
    void searchOccupations_SuccessWithPaginationAndKeywords() {
        OccupationGroup group = new OccupationGroup("GRP-SWE", "Software Engineering", "Desc");
        group.setId(1L);

        Occupation occ = new Occupation("SOC-15-1252", "Software Developer", "IT", group);
        occ.setId(101L);
        occ.setEntrySalaryAvg(new BigDecimal("15000000.00"));
        occ.setMidSalaryAvg(new BigDecimal("35000000.00"));

        Pageable pageable = PageRequest.of(0, 10);
        when(occupationRepository.searchOccupations(eq(1L), eq("Developer"), eq(pageable)))
            .thenReturn(new PageImpl<>(List.of(occ), pageable, 1));

        MarketDemandIndicator lmi = new MarketDemandIndicator(occ, "NATIONAL", new BigDecimal("88.50"), LocalDate.now());
        lmi.setGrowthRatePct(new BigDecimal("12.40"));
        when(marketDemandIndicatorRepository.findFirstByOccupationIdOrderBySnapshotDateDesc(101L))
            .thenReturn(Optional.of(lmi));

        PageResponse<OccupationSummaryResponse> response = taxonomyService.searchOccupations("Developer", 1L, pageable);

        assertNotNull(response);
        assertEquals(1, response.totalElements());
        assertEquals(1, response.content().size());

        OccupationSummaryResponse summary = response.content().get(0);
        assertEquals(101L, summary.id());
        assertEquals("SOC-15-1252", summary.code());
        assertEquals("Software Developer", summary.title());
        assertEquals("Software Engineering", summary.groupName());
        assertEquals(new BigDecimal("88.50"), summary.demandScore());
        assertEquals(new BigDecimal("12.40"), summary.growthRatePct());
    }

    @Test
    @DisplayName("searchOccupations: invalid page size or negative page throws ApiException BAD_REQUEST")
    void searchOccupations_InvalidPaginationThrowsApiException() {
        Pageable invalidSize = PageRequest.of(0, 101);
        ApiException ex = assertThrows(ApiException.class, () ->
            taxonomyService.searchOccupations(null, null, invalidSize));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("INVALID_PAGINATION", ex.getErrorCode());
    }

    @Test
    @DisplayName("getOccupationDetail: returns complete occupation detail with skills and high shortage level")
    void getOccupationDetail_Success() {
        OccupationGroup group = new OccupationGroup("GRP-SWE", "Software Engineering", "Desc");
        Occupation occ = new Occupation("SOC-15-1252", "Software Developer", "IT", group);
        occ.setId(101L);
        occ.setDescription("Develop enterprise software");
        occ.setEntrySalaryAvg(new BigDecimal("15000000.00"));
        occ.setMidSalaryAvg(new BigDecimal("35000000.00"));

        when(occupationRepository.findById(101L)).thenReturn(Optional.of(occ));

        Skill skillJava = new Skill("SK-JAVA", "Java", "TECHNICAL", null);
        skillJava.setId(12L);
        OccupationSkill occSkill = new OccupationSkill(occ, skillJava, new BigDecimal("1.00"), true);
        when(occupationSkillRepository.findByOccupationIdWithSkills(101L))
            .thenReturn(List.of(occSkill));

        MarketDemandIndicator lmi = new MarketDemandIndicator(occ, "NATIONAL", new BigDecimal("89.00"), LocalDate.now());
        lmi.setGrowthRatePct(new BigDecimal("14.00"));
        lmi.setOpenJobPostingsCount(1200);
        when(marketDemandIndicatorRepository.findFirstByOccupationIdOrderBySnapshotDateDesc(101L))
            .thenReturn(Optional.of(lmi));

        OccupationDetailResponse response = taxonomyService.getOccupationDetail(101L);

        assertNotNull(response);
        assertEquals(101L, response.id());
        assertEquals("Software Developer", response.title());
        assertEquals("Software Engineering", response.groupName());
        assertEquals("HIGH", response.marketIndicators().shortageLevel());
        assertEquals(new BigDecimal("89.00"), response.marketIndicators().demandScore());
        assertEquals(1200, response.marketIndicators().openingsCount());
        assertEquals(1, response.skills().size());

        var skillRequirement = response.skills().get(0);
        assertEquals(12L, skillRequirement.skillId());
        assertEquals("Java", skillRequirement.skillName());
        assertEquals("ESSENTIAL", skillRequirement.importanceType());
        assertEquals(1.0, skillRequirement.weight());
    }

    @Test
    @DisplayName("getOccupationDetail: non-existent ID throws ResourceNotFoundException")
    void getOccupationDetail_NotFoundThrowsResourceNotFoundException() {
        when(occupationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            taxonomyService.getOccupationDetail(999L));
    }

    @Test
    @DisplayName("getOccupationDetail: missing LMI records returns default indicators without NPE")
    void getOccupationDetail_WithoutLmiReturnsSensibleDefaults() {
        Occupation occ = new Occupation("SOC-15-1253", "Frontend Engineer", "IT", null);
        occ.setId(102L);
        when(occupationRepository.findById(102L)).thenReturn(Optional.of(occ));
        when(occupationSkillRepository.findByOccupationIdWithSkills(102L)).thenReturn(List.of());
        when(marketDemandIndicatorRepository.findFirstByOccupationIdOrderBySnapshotDateDesc(102L))
            .thenReturn(Optional.empty());

        OccupationDetailResponse response = taxonomyService.getOccupationDetail(102L);

        assertNotNull(response);
        assertEquals(BigDecimal.ZERO, response.marketIndicators().demandScore());
        assertEquals("LOW", response.marketIndicators().shortageLevel());
        assertEquals(0, response.marketIndicators().openingsCount());
        assertTrue(response.skills().isEmpty());
    }

    @Test
    @DisplayName("searchSkills: successfully searches skills by keyword and category")
    void searchSkills_SuccessWithCategoryAndQuery() {
        SkillCategory cat = new SkillCategory("CAT-PROG", "Programming Languages", "Desc");
        Skill skill = new Skill("SK-JAVA", "Java", "TECHNICAL", cat);
        skill.setId(12L);

        when(skillRepository.findByCategoryIdAndNameContainingIgnoreCase(1L, "java"))
            .thenReturn(List.of(skill));

        List<SkillDictionaryResponse> results = taxonomyService.searchSkills("java", 1L);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Java", results.get(0).name());
        assertEquals("Programming Languages", results.get(0).categoryName());
        assertEquals("ESCO", results.get(0).standardSource());
    }

    @Test
    @DisplayName("searchSkills: query with length < 2 throws ApiException BAD_REQUEST")
    void searchSkills_QueryTooShortThrowsApiException() {
        ApiException ex = assertThrows(ApiException.class, () ->
            taxonomyService.searchSkills("a", null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("INVALID_QUERY", ex.getErrorCode());
    }

    @Test
    @DisplayName("searchSkills: empty filters returns all skills")
    void searchSkills_AllSkillsWhenNoFilters() {
        Skill skill1 = new Skill("SK-1", "Python", "TECHNICAL", null);
        skill1.setId(1L);
        Skill skill2 = new Skill("SOC-SK-2", "SQL", "TECHNICAL", null);
        skill2.setId(2L);

        when(skillRepository.findAll()).thenReturn(List.of(skill1, skill2));

        List<SkillDictionaryResponse> results = taxonomyService.searchSkills(null, null);

        assertEquals(2, results.size());
        assertEquals("ESCO", results.get(0).standardSource());
        assertEquals("O*NET", results.get(1).standardSource());
    }
}
