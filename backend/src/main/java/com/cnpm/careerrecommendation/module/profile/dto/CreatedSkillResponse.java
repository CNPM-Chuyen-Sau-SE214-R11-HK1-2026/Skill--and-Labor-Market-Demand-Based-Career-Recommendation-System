package com.cnpm.careerrecommendation.module.profile.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CreatedSkillResponse(
        Long skillId, String skillName, String proficiencyLevel,
        BigDecimal yearsExperience, Instant createdAt) {
}
