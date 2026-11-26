package com.cnpm.careerrecommendation.module.taxonomy.service;

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
import com.cnpm.careerrecommendation.module.taxonomy.dto.OccupationGroupResponse;
import com.cnpm.careerrecommendation.module.taxonomy.dto.OccupationSkillMappingResponse;
import com.cnpm.careerrecommendation.module.taxonomy.dto.OccupationSkillRequest;
import com.cnpm.careerrecommendation.module.taxonomy.dto.SkillCategoryResponse;
import com.cnpm.careerrecommendation.module.taxonomy.dto.UpdateOccupationSkillRequest;
import com.cnpm.careerrecommendation.repository.OccupationGroupRepository;
import com.cnpm.careerrecommendation.repository.OccupationRepository;
import com.cnpm.careerrecommendation.repository.OccupationSkillRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Administrative service handling CRUD operations for occupations, skills,
 * and occupation-skill weighted mappings inside the taxonomy module.
 */
@Service
public class AdminTaxonomyService {

    private static final int MAX_CODE_LENGTH = 50;
    private static final int MAX_OCCUPATION_TITLE_LENGTH = 200;
    private static final int MAX_INDUSTRY_LENGTH = 100;
    private static final int MAX_SKILL_NAME_LENGTH = 150;
    private static final int MAX_SKILL_TYPE_LENGTH = 30;
    private static final int SALARY_MAX_SCALE = 2;
    private static final int WEIGHT_MAX_SCALE = 2;
    private static final BigDecimal MAX_SALARY = new BigDecimal("9999999999.99");
    private static final BigDecimal MIN_WEIGHT = BigDecimal.ZERO;
    private static final BigDecimal MAX_WEIGHT = BigDecimal.ONE;
    private static final Set<String> VALID_SKILL_TYPES = Set.of(
            Skill.TYPE_TECHNICAL,
            Skill.TYPE_SOFT,
            Skill.TYPE_DOMAIN
    );
    private static final String OCCUPATIONS_CODE_CONSTRAINT = "occupations_code_key";
    private static final String SKILLS_CODE_CONSTRAINT = "skills_code_key";
    private static final String OCC_SKILL_UNIQUE_CONSTRAINT = "uq_occupation_skills_occ_skill";

    private final OccupationRepository occupationRepository;
    private final OccupationGroupRepository occupationGroupRepository;
    private final SkillRepository skillRepository;
    private final SkillCategoryRepository skillCategoryRepository;
    private final OccupationSkillRepository occupationSkillRepository;

    public AdminTaxonomyService(OccupationRepository occupationRepository,
                                OccupationGroupRepository occupationGroupRepository,
                                SkillRepository skillRepository,
                                SkillCategoryRepository skillCategoryRepository,
                                OccupationSkillRepository occupationSkillRepository) {
        this.occupationRepository = occupationRepository;
        this.occupationGroupRepository = occupationGroupRepository;
        this.skillRepository = skillRepository;
        this.skillCategoryRepository = skillCategoryRepository;
        this.occupationSkillRepository = occupationSkillRepository;
    }

    /**
     * Creates a new standardized occupation.
     */
    @Transactional
    public AdminOccupationResponse createOccupation(AdminOccupationRequest request) {
        ValidatedOccupation input = validateOccupationRequest(request);
        if (occupationRepository.existsByCode(input.code())) {
            throw duplicateOccupationCode(input.code());
        }

        OccupationGroup group = resolveOccupationGroup(input.groupId());
        Occupation occupation = new Occupation();
        applyOccupationFields(occupation, input, group);

        Occupation saved = saveOccupationSafely(occupation, input.code());
        return toOccupationResponse(saved);
    }

    /**
     * Updates an existing occupation by ID.
     */
    @Transactional
    public AdminOccupationResponse updateOccupation(Long id, AdminOccupationRequest request) {
        requirePositiveId(id, "occupationId");
        ValidatedOccupation input = validateOccupationRequest(request);

        Occupation existing = findOccupationOrThrow(id);
        occupationRepository.findByCode(input.code())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw duplicateOccupationCode(input.code());
                });

        OccupationGroup group = resolveOccupationGroup(input.groupId());
        applyOccupationFields(existing, input, group);

        Occupation saved = saveOccupationSafely(existing, input.code());
        return toOccupationResponse(saved);
    }

    /**
     * Deletes an occupation by ID; dependent mappings, LMI records, and logs cascade in PostgreSQL.
     */
    @Transactional
    public void deleteOccupation(Long id) {
        requirePositiveId(id, "occupationId");
        Occupation occupation = findOccupationOrThrow(id);
        occupationRepository.delete(occupation);
        occupationRepository.flush();
    }

    /**
     * Lists all occupation groups ordered by ID for admin forms.
     */
    @Transactional(readOnly = true)
    public List<OccupationGroupResponse> listOccupationGroups() {
        return occupationGroupRepository.findAll(Sort.by("id").ascending()).stream()
                .map(group -> new OccupationGroupResponse(
                        group.getId(),
                        group.getCode(),
                        group.getName(),
                        group.getDescription()
                ))
                .toList();
    }

    /**
     * Creates a new standardized skill in the dictionary.
     */
    @Transactional
    public AdminSkillResponse createSkill(AdminSkillRequest request) {
        ValidatedSkill input = validateSkillRequest(request);
        if (skillRepository.existsByCode(input.code())) {
            throw duplicateSkillCode(input.code());
        }

        SkillCategory category = resolveSkillCategory(input.categoryId());
        Skill skill = new Skill();
        applySkillFields(skill, input, category);

        Skill saved = saveSkillSafely(skill, input.code());
        return toSkillResponse(saved);
    }

    /**
     * Updates an existing standardized skill by ID.
     */
    @Transactional
    public AdminSkillResponse updateSkill(Long id, AdminSkillRequest request) {
        requirePositiveId(id, "skillId");
        ValidatedSkill input = validateSkillRequest(request);

        Skill existing = findSkillOrThrow(id);
        skillRepository.findByCode(input.code())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw duplicateSkillCode(input.code());
                });

        SkillCategory category = resolveSkillCategory(input.categoryId());
        applySkillFields(existing, input, category);

        Skill saved = saveSkillSafely(existing, input.code());
        return toSkillResponse(saved);
    }

    /**
     * Deletes a standardized skill by ID; dependent occupation_skills and user_skills cascade in PostgreSQL.
     */
    @Transactional
    public void deleteSkill(Long id) {
        requirePositiveId(id, "skillId");
        Skill skill = findSkillOrThrow(id);
        skillRepository.delete(skill);
        skillRepository.flush();
    }

    /**
     * Lists all skill categories ordered by ID for admin forms.
     */
    @Transactional(readOnly = true)
    public List<SkillCategoryResponse> listSkillCategories() {
        return skillCategoryRepository.findAll(Sort.by("id").ascending()).stream()
                .map(category -> new SkillCategoryResponse(
                        category.getId(),
                        category.getCode(),
                        category.getName(),
                        category.getDescription()
                ))
                .toList();
    }

    /**
     * Lists all skill mappings configured for an occupation.
     */
    @Transactional(readOnly = true)
    public List<OccupationSkillMappingResponse> listOccupationSkills(Long occupationId) {
        requirePositiveId(occupationId, "occupationId");
        findOccupationOrThrow(occupationId);
        return occupationSkillRepository.findByOccupationIdWithSkills(occupationId).stream()
                .map(this::toMappingResponse)
                .toList();
    }

    /**
     * Assigns a skill to an occupation, or updates weight/required flag if the mapping already exists.
     */
    @Transactional
    public OccupationSkillAssignment assignOccupationSkill(Long occupationId, OccupationSkillRequest request) {
        requirePositiveId(occupationId, "occupationId");
        if (request == null) {
            throw invalidOccSkillRequest("Dữ liệu ánh xạ kỹ năng không được null.");
        }

        Long skillId = parseRequiredPositiveLong(
                request.skillId(), "skillId", "INVALID_OCCUPATION_SKILL_REQUEST");
        BigDecimal weight = validateWeight(request.importanceWeight());
        boolean isRequired = request.isRequired() == null || request.isRequired();

        Occupation occupation = findOccupationOrThrow(occupationId);
        Skill skill = findSkillOrThrow(skillId);

        var existingOpt = occupationSkillRepository.findByOccupationIdAndSkillId(occupationId, skillId);
        boolean created = existingOpt.isEmpty();
        OccupationSkill mapping = existingOpt.orElseGet(() ->
                new OccupationSkill(occupation, skill, weight, isRequired));
        mapping.setImportanceWeight(weight);
        mapping.setIsRequired(isRequired);

        OccupationSkill saved = saveMappingSafely(mapping);
        return new OccupationSkillAssignment(toMappingResponse(saved), created);
    }

    /**
     * Explicitly updates weight or required flag for an existing occupation-skill mapping.
     */
    @Transactional
    public OccupationSkillMappingResponse updateOccupationSkill(Long occupationId,
                                                                Long skillId,
                                                                UpdateOccupationSkillRequest request) {
        requirePositiveId(occupationId, "occupationId");
        requirePositiveId(skillId, "skillId");
        if (request == null) {
            throw invalidOccSkillRequest("Dữ liệu cập nhật ánh xạ kỹ năng không được null.");
        }
        if (request.importanceWeight() == null && request.isRequired() == null) {
            throw invalidOccSkillRequest("Phải cung cấp ít nhất một trường importanceWeight hoặc isRequired.");
        }

        findOccupationOrThrow(occupationId);
        OccupationSkill mapping = occupationSkillRepository.findByOccupationIdAndSkillId(occupationId, skillId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Ánh xạ kỹ năng không tìm thấy với occupationId='%d', skillId='%d'",
                                occupationId, skillId)));

        if (request.importanceWeight() != null) {
            mapping.setImportanceWeight(validateWeight(request.importanceWeight()));
        }
        if (request.isRequired() != null) {
            mapping.setIsRequired(request.isRequired());
        }

        OccupationSkill saved = saveMappingSafely(mapping);
        return toMappingResponse(saved);
    }

    /**
     * Removes a skill mapping from an occupation.
     */
    @Transactional
    public void removeOccupationSkill(Long occupationId, Long skillId) {
        requirePositiveId(occupationId, "occupationId");
        requirePositiveId(skillId, "skillId");
        findOccupationOrThrow(occupationId);

        OccupationSkill mapping = occupationSkillRepository.findByOccupationIdAndSkillId(occupationId, skillId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Ánh xạ kỹ năng không tìm thấy với occupationId='%d', skillId='%d'",
                                occupationId, skillId)));

        occupationSkillRepository.delete(mapping);
        occupationSkillRepository.flush();
    }

    private ValidatedOccupation validateOccupationRequest(AdminOccupationRequest request) {
        if (request == null) {
            throw invalidOccupationRequest("Dữ liệu nghề nghiệp không được null.");
        }
        String code = requireText(request.code(), "code", MAX_CODE_LENGTH, "INVALID_OCCUPATION_REQUEST");
        String title = requireText(request.title(), "title", MAX_OCCUPATION_TITLE_LENGTH, "INVALID_OCCUPATION_REQUEST");
        Long groupId = parseOptionalPositiveLong(request.groupId(), "groupId", "INVALID_OCCUPATION_REQUEST");
        String description = normalizeOptionalText(request.description(), "description", null, "INVALID_OCCUPATION_REQUEST");
        String industry = normalizeOptionalText(request.industry(), "industry", MAX_INDUSTRY_LENGTH, "INVALID_OCCUPATION_REQUEST");
        BigDecimal entrySalary = normalizeSalary(request.entrySalaryAvg(), "entrySalaryAvg");
        BigDecimal midSalary = normalizeSalary(request.midSalaryAvg(), "midSalaryAvg");

        if (entrySalary != null && midSalary != null && entrySalary.compareTo(midSalary) > 0) {
            throw invalidOccupationRequest("entrySalaryAvg phải nhỏ hơn hoặc bằng midSalaryAvg.");
        }
        return new ValidatedOccupation(code, title, groupId, description, industry, entrySalary, midSalary);
    }

    private ValidatedSkill validateSkillRequest(AdminSkillRequest request) {
        if (request == null) {
            throw invalidSkillRequest("Dữ liệu kỹ năng không được null.");
        }
        String code = requireText(request.code(), "code", MAX_CODE_LENGTH, "INVALID_SKILL_REQUEST");
        String name = requireText(request.name(), "name", MAX_SKILL_NAME_LENGTH, "INVALID_SKILL_REQUEST");
        Long categoryId = parseOptionalPositiveLong(request.categoryId(), "categoryId", "INVALID_SKILL_REQUEST");
        String skillType = normalizeSkillType(request.skillType());
        String description = normalizeOptionalText(request.description(), "description", null, "INVALID_SKILL_REQUEST");
        return new ValidatedSkill(code, name, categoryId, skillType, description);
    }

    private void applyOccupationFields(Occupation occ, ValidatedOccupation input, OccupationGroup group) {
        occ.setCode(input.code());
        occ.setTitle(input.title());
        occ.setGroup(group);
        occ.setDescription(input.description());
        occ.setIndustry(input.industry());
        occ.setEntrySalaryAvg(input.entrySalaryAvg());
        occ.setMidSalaryAvg(input.midSalaryAvg());
    }

    private void applySkillFields(Skill skill, ValidatedSkill input, SkillCategory category) {
        skill.setCode(input.code());
        skill.setName(input.name());
        skill.setCategory(category);
        skill.setSkillType(input.skillType());
        skill.setDescription(input.description());
    }

    private Occupation findOccupationOrThrow(Long id) {
        return occupationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nghề nghiệp", "id", id));
    }

    private Skill findSkillOrThrow(Long id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kỹ năng", "id", id));
    }

    private OccupationGroup resolveOccupationGroup(Long groupId) {
        if (groupId == null) {
            return null;
        }
        return occupationGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhóm nghề nghiệp", "id", groupId));
    }

    private SkillCategory resolveSkillCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return skillCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục kỹ năng", "id", categoryId));
    }

    private Occupation saveOccupationSafely(Occupation occ, String code) {
        try {
            return occupationRepository.saveAndFlush(occ);
        } catch (DataIntegrityViolationException ex) {
            if (isConstraintViolation(ex, OCCUPATIONS_CODE_CONSTRAINT)) {
                throw duplicateOccupationCode(code);
            }
            throw ex;
        }
    }

    private Skill saveSkillSafely(Skill skill, String code) {
        try {
            return skillRepository.saveAndFlush(skill);
        } catch (DataIntegrityViolationException ex) {
            if (isConstraintViolation(ex, SKILLS_CODE_CONSTRAINT)) {
                throw duplicateSkillCode(code);
            }
            throw ex;
        }
    }

    private OccupationSkill saveMappingSafely(OccupationSkill mapping) {
        try {
            return occupationSkillRepository.saveAndFlush(mapping);
        } catch (DataIntegrityViolationException ex) {
            if (isConstraintViolation(ex, OCC_SKILL_UNIQUE_CONSTRAINT)) {
                throw new ApiException(HttpStatus.CONFLICT, "OCCUPATION_SKILL_CONFLICT",
                        "Xung đột đồng thời khi cập nhật ánh xạ kỹ năng cho nghề nghiệp.");
            }
            throw ex;
        }
    }

    private AdminOccupationResponse toOccupationResponse(Occupation occ) {
        OccupationGroup group = occ.getGroup();
        return new AdminOccupationResponse(
                occ.getId(),
                occ.getCode(),
                occ.getTitle(),
                group != null ? group.getId() : null,
                group != null ? group.getName() : null,
                occ.getDescription(),
                occ.getIndustry(),
                occ.getEntrySalaryAvg(),
                occ.getMidSalaryAvg(),
                toInstant(occ.getCreatedAt()),
                toInstant(occ.getUpdatedAt())
        );
    }

    private AdminSkillResponse toSkillResponse(Skill skill) {
        SkillCategory category = skill.getCategory();
        String code = skill.getCode();
        String standardSource = (code != null && code.startsWith("SOC-")) ? "O*NET" : "ESCO";
        return new AdminSkillResponse(
                skill.getId(),
                code,
                skill.getName(),
                category != null ? category.getId() : null,
                category != null ? category.getName() : null,
                skill.getSkillType(),
                skill.getDescription(),
                standardSource,
                toInstant(skill.getCreatedAt()),
                toInstant(skill.getUpdatedAt())
        );
    }

    private OccupationSkillMappingResponse toMappingResponse(OccupationSkill mapping) {
        Occupation occ = mapping.getOccupation();
        Skill skill = mapping.getSkill();
        return new OccupationSkillMappingResponse(
                mapping.getId(),
                occ.getId(),
                occ.getCode(),
                occ.getTitle(),
                skill.getId(),
                skill.getCode(),
                skill.getName(),
                mapping.getImportanceWeight(),
                mapping.getIsRequired(),
                toInstant(mapping.getCreatedAt()),
                toInstant(mapping.getUpdatedAt())
        );
    }

    private void requirePositiveId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER",
                    fieldName + " phải là số nguyên dương.");
        }
    }

    private Long parseRequiredPositiveLong(BigDecimal value, String fieldName, String errorCode) {
        Long parsed = parseOptionalPositiveLong(value, fieldName, errorCode);
        if (parsed == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, errorCode, fieldName + " không được để trống.");
        }
        return parsed;
    }

    private Long parseOptionalPositiveLong(BigDecimal value, String fieldName, String errorCode) {
        if (value == null) {
            return null;
        }
        if (value.stripTrailingZeros().scale() > 0
                || value.compareTo(BigDecimal.ONE) < 0
                || value.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, errorCode,
                    fieldName + " phải là số nguyên dương hợp lệ.");
        }
        return value.longValueExact();
    }

    private String requireText(String value, String fieldName, int maxLength, String errorCode) {
        if (value == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, errorCode, fieldName + " không được để trống.");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, errorCode, fieldName + " không được để trống.");
        }
        if (trimmed.length() > maxLength) {
            throw new ApiException(HttpStatus.BAD_REQUEST, errorCode,
                    fieldName + " tối đa " + maxLength + " ký tự.");
        }
        return trimmed;
    }

    private String normalizeOptionalText(String value, String fieldName, Integer maxLength, String errorCode) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (maxLength != null && trimmed.length() > maxLength) {
            throw new ApiException(HttpStatus.BAD_REQUEST, errorCode,
                    fieldName + " tối đa " + maxLength + " ký tự.");
        }
        return trimmed;
    }

    private BigDecimal normalizeSalary(BigDecimal value, String fieldName) {
        if (value == null) {
            return null;
        }
        if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(MAX_SALARY) > 0) {
            throw invalidOccupationRequest(fieldName + " phải nằm trong khoảng 0.00 đến " + MAX_SALARY + ".");
        }
        BigDecimal stripped = value.stripTrailingZeros();
        if (stripped.scale() > SALARY_MAX_SCALE) {
            throw invalidOccupationRequest(fieldName + " tối đa " + SALARY_MAX_SCALE + " chữ số thập phân.");
        }
        return stripped.setScale(SALARY_MAX_SCALE, RoundingMode.UNNECESSARY);
    }

    private BigDecimal validateWeight(BigDecimal weight) {
        if (weight == null) {
            return BigDecimal.ONE.setScale(WEIGHT_MAX_SCALE, RoundingMode.UNNECESSARY);
        }
        if (weight.compareTo(MIN_WEIGHT) < 0 || weight.compareTo(MAX_WEIGHT) > 0) {
            throw invalidOccSkillRequest("importanceWeight phải nằm trong khoảng 0.00 đến 1.00.");
        }
        BigDecimal stripped = weight.stripTrailingZeros();
        if (stripped.scale() > WEIGHT_MAX_SCALE) {
            throw invalidOccSkillRequest("importanceWeight tối đa " + WEIGHT_MAX_SCALE + " chữ số thập phân.");
        }
        return stripped.setScale(WEIGHT_MAX_SCALE, RoundingMode.UNNECESSARY);
    }

    private String normalizeSkillType(String raw) {
        if (raw == null) {
            return Skill.TYPE_TECHNICAL;
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() > MAX_SKILL_TYPE_LENGTH || !VALID_SKILL_TYPES.contains(normalized)) {
            throw invalidSkillRequest("skillType phải là một trong: TECHNICAL, SOFT, DOMAIN.");
        }
        return normalized;
    }

    private boolean isConstraintViolation(Throwable error, String expectedConstraint) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation
                    && violation.getConstraintName() != null
                    && expectedConstraint.equalsIgnoreCase(violation.getConstraintName())) {
                return true;
            }
        }
        return false;
    }

    private Instant toInstant(OffsetDateTime timestamp) {
        return timestamp != null ? timestamp.toInstant() : null;
    }

    private ApiException invalidOccupationRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OCCUPATION_REQUEST", message);
    }

    private ApiException invalidSkillRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_SKILL_REQUEST", message);
    }

    private ApiException invalidOccSkillRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OCCUPATION_SKILL_REQUEST", message);
    }

    private ApiException duplicateOccupationCode(String code) {
        return new ApiException(HttpStatus.CONFLICT, "OCCUPATION_CODE_ALREADY_EXISTS",
                "Mã nghề nghiệp '" + code + "' đã tồn tại.");
    }

    private ApiException duplicateSkillCode(String code) {
        return new ApiException(HttpStatus.CONFLICT, "SKILL_CODE_ALREADY_EXISTS",
                "Mã kỹ năng '" + code + "' đã tồn tại.");
    }

    public record OccupationSkillAssignment(OccupationSkillMappingResponse mapping, boolean created) {
    }

    private record ValidatedOccupation(
            String code,
            String title,
            Long groupId,
            String description,
            String industry,
            BigDecimal entrySalaryAvg,
            BigDecimal midSalaryAvg
    ) {
    }

    private record ValidatedSkill(
            String code,
            String name,
            Long categoryId,
            String skillType,
            String description
    ) {
    }
}
