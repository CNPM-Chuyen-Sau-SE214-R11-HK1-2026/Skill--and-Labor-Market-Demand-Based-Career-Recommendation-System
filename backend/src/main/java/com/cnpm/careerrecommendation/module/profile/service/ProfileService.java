package com.cnpm.careerrecommendation.module.profile.service;

import com.cnpm.careerrecommendation.common.entity.Skill;
import com.cnpm.careerrecommendation.common.entity.User;
import com.cnpm.careerrecommendation.common.exception.ApiException;
import com.cnpm.careerrecommendation.common.repository.SkillRepository;
import com.cnpm.careerrecommendation.entity.CandidateProfile;
import com.cnpm.careerrecommendation.entity.UserSkill;
import com.cnpm.careerrecommendation.module.profile.dto.AddSkillRequest;
import com.cnpm.careerrecommendation.module.profile.dto.CreatedSkillResponse;
import com.cnpm.careerrecommendation.module.profile.dto.ProfileRequest;
import com.cnpm.careerrecommendation.module.profile.dto.ProfileResponse;
import com.cnpm.careerrecommendation.module.profile.dto.SkillResponse;
import com.cnpm.careerrecommendation.module.profile.dto.UpdateSkillRequest;
import com.cnpm.careerrecommendation.repository.CandidateProfileRepository;
import com.cnpm.careerrecommendation.repository.UserSkillRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProfileService {

    private final CandidateProfileRepository profiles;
    private final UserSkillRepository userSkills;
    private final SkillRepository skills;
    private final EntityManager entityManager;

    public ProfileService(CandidateProfileRepository profiles, UserSkillRepository userSkills,
                          SkillRepository skills, EntityManager entityManager) {
        this.profiles = profiles;
        this.userSkills = userSkills;
        this.skills = skills;
        this.entityManager = entityManager;
    }

    public ProfileResponse getProfile(Long userId) {
        CandidateProfile profile = profiles.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PROFILE_NOT_FOUND",
                        "Hồ sơ ứng viên chưa được khởi tạo. Vui lòng cập nhật qua PUT /api/v1/profile."));
        return toProfileResponse(profile);
    }

    @Transactional
    public ProfileResponse upsertProfile(Long userId, ProfileRequest request) {
        BigDecimal min = request.expectedSalaryMin();
        BigDecimal max = request.expectedSalaryMax();
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, null,
                    "expectedSalaryMin phải nhỏ hơn hoặc bằng expectedSalaryMax.");
        }

        User owner = lockOwner(userId);
        CandidateProfile profile = profiles.findByUserId(userId)
                .orElseGet(() -> new CandidateProfile(owner));
        profile.setHeadline(request.headline());
        profile.setYearsOfExperience(Objects.requireNonNullElse(request.yearsOfExperience(), BigDecimal.ZERO));
        profile.setExpectedSalaryMin(min);
        profile.setExpectedSalaryMax(max);
        profile.setPreferredLocation(request.preferredLocation());
        profile.setPreferredWorkMode(Objects.requireNonNullElse(request.workMode(), CandidateProfile.WORK_MODE_HYBRID));
        profile.setTargetIndustry(request.targetIndustry());
        return toProfileResponse(profiles.saveAndFlush(profile));
    }

    public List<SkillResponse> getSkills(Long userId) {
        return userSkills.findByUserId(userId).stream().map(this::toSkillResponse).toList();
    }

    @Transactional
    public CreatedSkillResponse addSkill(Long userId, AddSkillRequest request) {
        User owner = lockOwner(userId);
        Skill skill = skills.findById(request.skillId().longValueExact())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, null,
                        "Không tìm thấy kỹ năng trong danh mục."));
        if (userSkills.existsByUserIdAndSkillId(userId, skill.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, null, "Ứng viên đã khai báo kỹ năng này.");
        }

        UserSkill saved = userSkills.save(new UserSkill(owner, skill,
                request.proficiencyLevel(), request.yearsExperience()));
        return new CreatedSkillResponse(skill.getId(), skill.getName(), saved.getProficiencyLevel(),
                saved.getYearsOfExperience(), saved.getCreatedAt().toInstant());
    }

    @Transactional
    public SkillResponse updateSkill(Long userId, Long skillId, UpdateSkillRequest request) {
        lockOwner(userId);
        UserSkill skill = findOwnedSkill(userId, skillId);
        if (request.proficiencyLevel() != null) {
            skill.setProficiencyLevel(request.proficiencyLevel());
        }
        if (request.yearsExperience() != null) {
            skill.setYearsOfExperience(request.yearsExperience());
        }
        return toSkillResponse(skill);
    }

    @Transactional
    public void deleteSkill(Long userId, Long skillId) {
        lockOwner(userId);
        userSkills.delete(findOwnedSkill(userId, skillId));
    }

    private User lockOwner(Long userId) {
        // Serialize writes for this owner even before a profile or skill row exists.
        User user = entityManager.find(User.class, userId, LockModeType.PESSIMISTIC_WRITE);
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, null, "Tài khoản không còn tồn tại.");
        }
        return user;
    }

    private UserSkill findOwnedSkill(Long userId, Long skillId) {
        return userSkills.findByUserIdAndSkillId(userId, skillId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, null,
                        "Không tìm thấy kỹ năng cá nhân."));
    }

    private ProfileResponse toProfileResponse(CandidateProfile profile) {
        Long userId = profile.getUser().getId();
        return new ProfileResponse(profile.getId(), userId, profile.getHeadline(), profile.getYearsOfExperience(),
                profile.getExpectedSalaryMin(), profile.getExpectedSalaryMax(), profile.getPreferredLocation(),
                profile.getPreferredWorkMode(), profile.getTargetIndustry(), userSkills.countByUserId(userId),
                profile.getUpdatedAt().toInstant());
    }

    private SkillResponse toSkillResponse(UserSkill userSkill) {
        Skill skill = userSkill.getSkill();
        String categoryName = skill.getCategory() == null ? null : skill.getCategory().getName();
        return new SkillResponse(skill.getId(), skill.getCode(), skill.getName(), categoryName,
                userSkill.getProficiencyLevel(), userSkill.getYearsOfExperience(), userSkill.getVerified());
    }
}
