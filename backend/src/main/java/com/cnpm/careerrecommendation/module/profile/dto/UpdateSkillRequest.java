package com.cnpm.careerrecommendation.module.profile.dto;

import java.math.BigDecimal;

public record UpdateSkillRequest(
        String proficiencyLevel,
        BigDecimal yearsExperience) {
}
