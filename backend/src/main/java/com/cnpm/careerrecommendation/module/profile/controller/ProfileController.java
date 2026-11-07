package com.cnpm.careerrecommendation.module.profile.controller;

import com.cnpm.careerrecommendation.common.dto.ErrorResponse;
import com.cnpm.careerrecommendation.common.exception.ApiException;
import com.cnpm.careerrecommendation.common.security.AuthenticatedUser;
import com.cnpm.careerrecommendation.module.profile.dto.AddSkillRequest;
import com.cnpm.careerrecommendation.module.profile.dto.CreatedSkillResponse;
import com.cnpm.careerrecommendation.module.profile.dto.ProfileRequest;
import com.cnpm.careerrecommendation.module.profile.dto.ProfileResponse;
import com.cnpm.careerrecommendation.module.profile.dto.SkillResponse;
import com.cnpm.careerrecommendation.module.profile.dto.UpdateSkillRequest;
import com.cnpm.careerrecommendation.module.profile.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestController
@RequestMapping("/api/v1/profile")
@PreAuthorize("hasRole('CANDIDATE')")
public class ProfileController {

    private final ProfileService service;

    public ProfileController(ProfileService service) {
        this.service = service;
    }

    @GetMapping
    public ProfileResponse getProfile(@AuthenticationPrincipal AuthenticatedUser user) {
        return service.getProfile(user.userId());
    }

    @PutMapping
    public ProfileResponse upsertProfile(@AuthenticationPrincipal AuthenticatedUser user,
                                         @Valid @RequestBody ProfileRequest request) {
        return service.upsertProfile(user.userId(), request);
    }

    @GetMapping("/skills")
    public List<SkillResponse> getSkills(@AuthenticationPrincipal AuthenticatedUser user) {
        return service.getSkills(user.userId());
    }

    @PostMapping("/skills")
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedSkillResponse addSkill(@AuthenticationPrincipal AuthenticatedUser user,
                                         @Valid @RequestBody AddSkillRequest request) {
        return service.addSkill(user.userId(), request);
    }

    @PutMapping("/skills/{skillId}")
    public SkillResponse updateSkill(@AuthenticationPrincipal AuthenticatedUser user,
                                     @PathVariable Long skillId,
                                     @Valid @RequestBody UpdateSkillRequest request) {
        return service.updateSkill(user.userId(), requirePositiveSkillId(skillId), request);
    }

    @DeleteMapping("/skills/{skillId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSkill(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long skillId) {
        service.deleteSkill(user.userId(), requirePositiveSkillId(skillId));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ErrorResponse> invalidPathId(HttpServletRequest request) {
        return ResponseEntity.badRequest().body(ErrorResponse.of(HttpStatus.BAD_REQUEST,
                "skillId phải là số nguyên dương.", request.getRequestURI()));
    }

    private Long requirePositiveSkillId(Long skillId) {
        if (skillId <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, null, "skillId phải là số nguyên dương.");
        }
        return skillId;
    }
}
