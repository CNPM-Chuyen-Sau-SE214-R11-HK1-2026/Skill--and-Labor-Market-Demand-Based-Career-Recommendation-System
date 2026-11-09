package com.cnpm.careerrecommendation.module.taxonomy.service;

import com.cnpm.careerrecommendation.common.dto.PageResponse;
import com.cnpm.careerrecommendation.common.entity.Skill;
import com.cnpm.careerrecommendation.common.exception.ApiException;
import com.cnpm.careerrecommendation.common.exception.ResourceNotFoundException;
import com.cnpm.careerrecommendation.common.repository.SkillRepository;
import com.cnpm.careerrecommendation.entity.MarketDemandIndicator;
import com.cnpm.careerrecommendation.entity.Occupation;
import com.cnpm.careerrecommendation.entity.OccupationSkill;
import com.cnpm.careerrecommendation.module.taxonomy.dto.OccupationDetailResponse;
import com.cnpm.careerrecommendation.module.taxonomy.dto.OccupationSummaryResponse;
import com.cnpm.careerrecommendation.module.taxonomy.dto.SkillDictionaryResponse;
import com.cnpm.careerrecommendation.repository.MarketDemandIndicatorRepository;
import com.cnpm.careerrecommendation.repository.OccupationRepository;
import com.cnpm.careerrecommendation.repository.OccupationSkillRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for standard occupations and skills taxonomy.
 * Complies with docs/api/taxonomy.md specification.
 */
@Service
@Transactional(readOnly = true)
public class TaxonomyService {

    private static final BigDecimal SHORTAGE_HIGH_THRESHOLD = new BigDecimal("85.00");
    private static final BigDecimal SHORTAGE_MEDIUM_THRESHOLD = new BigDecimal("70.00");
    private static final String DEFAULT_CURRENCY = "VND";
    private static final String DEFAULT_SOURCE = "Cục Thống kê & Báo cáo WEF 2025";
    private static final String DEFAULT_DATA_PERIOD = "2025-2026";
    private static final int MAX_PAGE_SIZE = 100;

    private final OccupationRepository occupationRepository;
    private final OccupationSkillRepository occupationSkillRepository;
    private final MarketDemandIndicatorRepository marketDemandIndicatorRepository;
    private final SkillRepository skillRepository;

    public TaxonomyService(OccupationRepository occupationRepository,
                           OccupationSkillRepository occupationSkillRepository,
                           MarketDemandIndicatorRepository marketDemandIndicatorRepository,
                           SkillRepository skillRepository) {
        this.occupationRepository = occupationRepository;
        this.occupationSkillRepository = occupationSkillRepository;
        this.marketDemandIndicatorRepository = marketDemandIndicatorRepository;
        this.skillRepository = skillRepository;
    }

    /**
     * Search and paginate occupations by keyword and optional group filter.
     * Uses batch LMI fetching to avoid N+1 queries.
     */
    public PageResponse<OccupationSummaryResponse> searchOccupations(String query, Long groupId, Pageable pageable) {
        validatePagination(pageable);

        String keyword = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        Page<Occupation> page = occupationRepository.searchOccupations(groupId, keyword, pageable);

        List<Long> occIds = page.getContent().stream()
                .map(Occupation::getId)
                .filter(id -> id != null)
                .toList();

        Map<Long, MarketDemandIndicator> lmiMap = occIds.isEmpty() ? Map.of() :
                marketDemandIndicatorRepository.findLatestByOccupationIds(occIds).stream()
                        .collect(Collectors.toMap(
                                m -> m.getOccupation().getId(),
                                m -> m,
                                (existing, replacement) -> existing
                        ));

        Page<OccupationSummaryResponse> summaryPage = page.map(occ -> toSummaryResponse(occ, lmiMap.get(occ.getId())));
        return PageResponse.from(summaryPage);
    }

    /**
     * Get detailed information for an occupation including salary, LMI, and skills.
     */
    public OccupationDetailResponse getOccupationDetail(Long id) {
        Occupation occ = occupationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Nghề nghiệp", "id", id));

        // Required and optional skills with weights (eager fetched via JOIN FETCH)
        List<OccupationSkill> occSkills = occupationSkillRepository.findByOccupationIdWithSkills(id);
        List<OccupationDetailResponse.OccupationSkillRequirementDto> skillDtos = occSkills.stream()
            .map(os -> new OccupationDetailResponse.OccupationSkillRequirementDto(
                os.getSkill().getId(),
                os.getSkill().getCode(),
                os.getSkill().getName(),
                Boolean.TRUE.equals(os.getIsRequired()) ? "ESSENTIAL" : "OPTIONAL",
                os.getImportanceWeight() != null ? os.getImportanceWeight().doubleValue() : 1.0
            ))
            .toList();

        // Latest Labor Market Indicator (LMI)
        Optional<MarketDemandIndicator> lmiOpt = marketDemandIndicatorRepository
            .findFirstByOccupationIdOrderBySnapshotDateDesc(id);

        OccupationDetailResponse.MarketIndicatorDto marketDto = lmiOpt
            .map(lmi -> new OccupationDetailResponse.MarketIndicatorDto(
                lmi.getDemandScore(),
                lmi.getGrowthRatePct(),
                calculateShortageLevel(lmi.getDemandScore()),
                lmi.getOpenJobPostingsCount(),
                DEFAULT_DATA_PERIOD,
                DEFAULT_SOURCE
            ))
            .orElseGet(() -> new OccupationDetailResponse.MarketIndicatorDto(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "LOW",
                0,
                DEFAULT_DATA_PERIOD,
                DEFAULT_SOURCE
            ));

        OccupationDetailResponse.SalaryRangeDto salaryRange = new OccupationDetailResponse.SalaryRangeDto(
            occ.getEntrySalaryAvg() != null ? occ.getEntrySalaryAvg() : BigDecimal.ZERO,
            occ.getMidSalaryAvg() != null ? occ.getMidSalaryAvg() : BigDecimal.ZERO,
            DEFAULT_CURRENCY
        );

        String groupName = (occ.getGroup() != null) ? occ.getGroup().getName() : null;

        return new OccupationDetailResponse(
            occ.getId(),
            occ.getCode(),
            occ.getTitle(),
            groupName,
            occ.getDescription(),
            salaryRange,
            marketDto,
            skillDtos
        );
    }

    /**
     * Search standardized skills dictionary by keyword and optional category ID.
     */
    public List<SkillDictionaryResponse> searchSkills(String query, Long categoryId) {
        String keyword = null;
        if (query != null) {
            String trimmed = query.trim();
            if (!trimmed.isEmpty() && trimmed.length() < 2) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_QUERY",
                    "Từ khóa tìm kiếm phải có tối thiểu 2 ký tự");
            }
            if (!trimmed.isEmpty()) {
                keyword = trimmed;
            }
        }

        List<Skill> skills;
        if (categoryId != null && keyword != null) {
            skills = skillRepository.findByCategoryIdAndNameContainingIgnoreCase(categoryId, keyword);
        } else if (categoryId != null) {
            skills = skillRepository.findByCategoryId(categoryId);
        } else if (keyword != null) {
            skills = skillRepository.findByNameContainingIgnoreCase(keyword);
        } else {
            skills = skillRepository.findAll();
        }

        return skills.stream()
            .map(s -> new SkillDictionaryResponse(
                s.getId(),
                s.getCode(),
                s.getName(),
                s.getCategory() != null ? s.getCategory().getName() : null,
                deriveStandardSource(s.getCode())
            ))
            .toList();
    }

    private OccupationSummaryResponse toSummaryResponse(Occupation occ, MarketDemandIndicator lmi) {
        BigDecimal demandScore = (lmi != null && lmi.getDemandScore() != null) ? lmi.getDemandScore() : BigDecimal.ZERO;
        BigDecimal growthRate = (lmi != null && lmi.getGrowthRatePct() != null) ? lmi.getGrowthRatePct() : BigDecimal.ZERO;
        String groupName = (occ.getGroup() != null) ? occ.getGroup().getName() : null;

        return new OccupationSummaryResponse(
            occ.getId(),
            occ.getCode(),
            occ.getTitle(),
            groupName,
            occ.getEntrySalaryAvg(),
            occ.getMidSalaryAvg(),
            demandScore,
            growthRate
        );
    }

    private void validatePagination(Pageable pageable) {
        if (pageable.getPageNumber() < 0 || pageable.getPageSize() <= 0 || pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PAGINATION",
                "Tham số phân trang không hợp lệ (size từ 1 đến " + MAX_PAGE_SIZE + ")");
        }
    }

    private String calculateShortageLevel(BigDecimal demandScore) {
        if (demandScore == null) {
            return "LOW";
        }
        if (demandScore.compareTo(SHORTAGE_HIGH_THRESHOLD) >= 0) {
            return "HIGH";
        }
        if (demandScore.compareTo(SHORTAGE_MEDIUM_THRESHOLD) >= 0) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private String deriveStandardSource(String code) {
        if (code != null && code.startsWith("SOC-")) {
            return "O*NET";
        }
        return "ESCO";
    }
}
