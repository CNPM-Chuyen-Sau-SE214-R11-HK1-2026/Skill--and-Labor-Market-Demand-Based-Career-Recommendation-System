package com.cnpm.careerrecommendation.module.admin.dto;

public record AdminStatsResponse(
        long totalUsers,
        long totalProfiles,
        long totalOccupations,
        long totalSkills,
        long totalLmiRecords,
        long totalRecommendationLogs
) {
}
