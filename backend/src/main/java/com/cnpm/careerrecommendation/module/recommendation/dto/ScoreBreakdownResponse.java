package com.cnpm.careerrecommendation.module.recommendation.dto;

public record ScoreBreakdownResponse(
    double skillMatchPct, double demandScore, double preferenceMatchPct, String formula) {}
