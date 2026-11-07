package com.cnpm.careerrecommendation.module.profile.dto;

import java.math.BigDecimal;

public record SkillResponse(
        Long skillId, String skillCode, String skillName, String categoryName,
        String proficiencyLevel, BigDecimal yearsExperience, boolean verified) {
}
