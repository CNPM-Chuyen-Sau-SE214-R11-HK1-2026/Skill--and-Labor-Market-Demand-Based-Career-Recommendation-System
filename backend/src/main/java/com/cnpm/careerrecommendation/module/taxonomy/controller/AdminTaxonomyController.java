package com.cnpm.careerrecommendation.module.taxonomy.controller;

import com.cnpm.careerrecommendation.common.dto.ErrorResponse;
import com.cnpm.careerrecommendation.common.exception.ApiException;
import com.cnpm.careerrecommendation.module.taxonomy.dto.AdminOccupationRequest;
import com.cnpm.careerrecommendation.module.taxonomy.dto.AdminOccupationResponse;
import com.cnpm.careerrecommendation.module.taxonomy.dto.AdminSkillRequest;
import com.cnpm.careerrecommendation.module.taxonomy.dto.AdminSkillResponse;
import com.cnpm.careerrecommendation.module.taxonomy.dto.OccupationGroupResponse;
import com.cnpm.careerrecommendation.module.taxonomy.dto.OccupationSkillMappingResponse;
import com.cnpm.careerrecommendation.module.taxonomy.dto.OccupationSkillRequest;
import com.cnpm.careerrecommendation.module.taxonomy.dto.SkillCategoryResponse;
import com.cnpm.careerrecommendation.module.taxonomy.dto.UpdateOccupationSkillRequest;
import com.cnpm.careerrecommendation.module.taxonomy.service.AdminTaxonomyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Administrative REST controller for CRUD management of occupations, skills,
 * and occupation-skill requirement mappings. Restricted to {@code ROLE_ADMIN}.
 */
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTaxonomyController {

    private final AdminTaxonomyService adminTaxonomyService;

    public AdminTaxonomyController(AdminTaxonomyService adminTaxonomyService) {
        this.adminTaxonomyService = adminTaxonomyService;
    }

    /**
     * Create a new occupation.
     */
    @PostMapping("/occupations")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminOccupationResponse createOccupation(@Valid @RequestBody AdminOccupationRequest request) {
        return adminTaxonomyService.createOccupation(request);
    }

    /**
     * Update an existing occupation by ID.
     */
    @PutMapping("/occupations/{id}")
    public AdminOccupationResponse updateOccupation(@PathVariable("id") Long id,
                                                    @Valid @RequestBody AdminOccupationRequest request) {
        return adminTaxonomyService.updateOccupation(requirePositiveId(id, "id"), request);
    }

    /**
     * Delete an occupation by ID.
     */
    @DeleteMapping("/occupations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOccupation(@PathVariable("id") Long id) {
        adminTaxonomyService.deleteOccupation(requirePositiveId(id, "id"));
    }

    /**
     * List occupation groups for administrative selection forms.
     */
    @GetMapping("/occupation-groups")
    public List<OccupationGroupResponse> listOccupationGroups() {
        return adminTaxonomyService.listOccupationGroups();
    }

    /**
     * Create a new standardized skill.
     */
    @PostMapping("/skills")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminSkillResponse createSkill(@Valid @RequestBody AdminSkillRequest request) {
        return adminTaxonomyService.createSkill(request);
    }

    /**
     * Update an existing standardized skill by ID.
     */
    @PutMapping("/skills/{id}")
    public AdminSkillResponse updateSkill(@PathVariable("id") Long id,
                                          @Valid @RequestBody AdminSkillRequest request) {
        return adminTaxonomyService.updateSkill(requirePositiveId(id, "id"), request);
    }

    /**
     * Delete a standardized skill by ID.
     */
    @DeleteMapping("/skills/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSkill(@PathVariable("id") Long id) {
        adminTaxonomyService.deleteSkill(requirePositiveId(id, "id"));
    }

    /**
     * List skill categories for administrative selection forms.
     */
    @GetMapping("/skill-categories")
    public List<SkillCategoryResponse> listSkillCategories() {
        return adminTaxonomyService.listSkillCategories();
    }

    /**
     * List skill mappings configured for an occupation.
     */
    @GetMapping("/occupations/{id}/skills")
    public List<OccupationSkillMappingResponse> listOccupationSkills(@PathVariable("id") Long id) {
        return adminTaxonomyService.listOccupationSkills(requirePositiveId(id, "id"));
    }

    /**
     * Assign a skill to an occupation (returns 201 Created) or update its weight/required flag (returns 200 OK).
     */
    @PostMapping("/occupations/{id}/skills")
    public ResponseEntity<OccupationSkillMappingResponse> assignOccupationSkill(
            @PathVariable("id") Long id,
            @Valid @RequestBody OccupationSkillRequest request) {
        AdminTaxonomyService.OccupationSkillAssignment assignment =
                adminTaxonomyService.assignOccupationSkill(requirePositiveId(id, "id"), request);
        HttpStatus status = assignment.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(assignment.mapping());
    }

    /**
     * Update an existing occupation-skill mapping.
     */
    @PutMapping("/occupations/{id}/skills/{skillId}")
    public OccupationSkillMappingResponse updateOccupationSkill(
            @PathVariable("id") Long id,
            @PathVariable("skillId") Long skillId,
            @Valid @RequestBody UpdateOccupationSkillRequest request) {
        return adminTaxonomyService.updateOccupationSkill(
                requirePositiveId(id, "id"),
                requirePositiveId(skillId, "skillId"),
                request);
    }

    /**
     * Remove a skill mapping from an occupation using path variable skillId.
     */
    @DeleteMapping("/occupations/{id}/skills/{skillId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeOccupationSkillByPath(@PathVariable("id") Long id,
                                            @PathVariable("skillId") Long skillId) {
        adminTaxonomyService.removeOccupationSkill(
                requirePositiveId(id, "id"),
                requirePositiveId(skillId, "skillId"));
    }

    /**
     * Remove a skill mapping from an occupation using query parameter skillId.
     */
    @DeleteMapping("/occupations/{id}/skills")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeOccupationSkillByQuery(@PathVariable("id") Long id,
                                             @RequestParam("skillId") Long skillId) {
        adminTaxonomyService.removeOccupationSkill(
                requirePositiveId(id, "id"),
                requirePositiveId(skillId, "skillId"));
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    ResponseEntity<ErrorResponse> handleInvalidParameter(Exception ex, HttpServletRequest request) {
        String message = ex instanceof MissingServletRequestParameterException missing
                ? "Thiếu tham số bắt buộc: " + missing.getParameterName()
                : "Tham số đường dẫn hoặc truy vấn phải là số nguyên hợp lệ.";
        return ResponseEntity.badRequest().body(new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_PARAMETER",
                message,
                request.getRequestURI(),
                List.of()
        ));
    }

    private Long requirePositiveId(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER",
                    fieldName + " phải là số nguyên dương.");
        }
        return value;
    }
}
