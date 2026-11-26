package com.cnpm.careerrecommendation.module.profile.dto;

import java.math.BigDecimal;

public record AddSkillRequest(
        BigDecimal skillId,
        String proficiencyLevel,
        BigDecimal yearsExperience) { }
