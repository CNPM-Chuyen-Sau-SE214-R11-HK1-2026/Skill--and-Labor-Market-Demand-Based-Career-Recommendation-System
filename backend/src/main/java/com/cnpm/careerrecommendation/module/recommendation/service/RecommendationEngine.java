package com.cnpm.careerrecommendation.module.recommendation.service;

import com.cnpm.careerrecommendation.entity.CandidateProfile;
import com.cnpm.careerrecommendation.entity.MarketDemandIndicator;
import com.cnpm.careerrecommendation.entity.Occupation;
import com.cnpm.careerrecommendation.entity.OccupationSkill;
import com.cnpm.careerrecommendation.entity.UserSkill;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class RecommendationEngine {
  public record Score(
      double skillMatchPct,
      double demandScore,
      double preferenceMatchPct,
      double finalScore,
      MarketDemandIndicator indicator) {}

  public Score score(
      Occupation occupation,
      List<UserSkill> userSkills,
      List<OccupationSkill> occupationSkills,
      CandidateProfile profile,
      List<MarketDemandIndicator> indicators) {
    double skill = skillMatch(userSkills, occupationSkills);
    MarketDemandIndicator indicator = selectIndicator(profile, indicators);
    double demand =
        clamp(
            indicator == null || indicator.getDemandScore() == null
                ? 50
                : indicator.getDemandScore().doubleValue());
    double preference = preferenceMatch(occupation, profile);
    return new Score(
        skill, demand, preference, clamp(.6 * skill + .3 * demand + .1 * preference), indicator);
  }

  public double skillMatch(List<UserSkill> userSkills, List<OccupationSkill> occupationSkills) {
    if (userSkills == null
        || userSkills.isEmpty()
        || occupationSkills == null
        || occupationSkills.isEmpty()) return 0;
    Map<Long, UserSkill> skills =
        userSkills.stream()
            .collect(Collectors.toMap(s -> s.getSkill().getId(), Function.identity(), (a, b) -> a));
    double total = 0, matched = 0;
    for (OccupationSkill os : occupationSkills) {
      double weight = os.getImportanceWeight() == null ? 0 : os.getImportanceWeight().doubleValue();
      total += weight;
      UserSkill user = skills.get(os.getSkill().getId());
      if (user != null) matched += weight * proficiency(user.getProficiencyLevel());
    }
    return total <= 0 ? 0 : clamp(matched / total * 100);
  }

  public double preferenceMatch(Occupation occupation, CandidateProfile profile) {
    if (profile == null) return 50;
    double total = 0;
    int criteria = 0;
    if (profile.getTargetIndustry() != null && !profile.getTargetIndustry().isBlank()) {
      total +=
          profile
                  .getTargetIndustry()
                  .trim()
                  .equalsIgnoreCase(
                      occupation.getIndustry() == null ? "" : occupation.getIndustry().trim())
              ? 100
              : 0;
      criteria++;
    }
    BigDecimal min = profile.getExpectedSalaryMin(), max = profile.getExpectedSalaryMax();
    if (min != null || max != null) {
      BigDecimal a = occupation.getEntrySalaryAvg(), b = occupation.getMidSalaryAvg();
      double salary = 50;
      if (a != null || b != null) {
        BigDecimal lo = a == null ? b : b == null ? a : a.min(b),
            hi = a == null ? b : b == null ? a : a.max(b);
        boolean overlaps =
            (max == null || lo.compareTo(max) <= 0) && (min == null || hi.compareTo(min) >= 0);
        if (overlaps) salary = 100;
        else if (min != null && hi.compareTo(min.multiply(new BigDecimal("0.8"))) >= 0
            || max != null && lo.compareTo(max.multiply(new BigDecimal("1.2"))) <= 0) salary = 50;
        else salary = 0;
      }
      total += salary;
      criteria++;
    }
    return criteria == 0 ? 50 : total / criteria;
  }

  public MarketDemandIndicator selectIndicator(
      CandidateProfile profile, List<MarketDemandIndicator> indicators) {
    if (indicators == null || indicators.isEmpty()) return null;
    String location = profile == null ? null : profile.getPreferredLocation();
    if (location != null && !location.isBlank()) {
      for (MarketDemandIndicator i : indicators)
        if (i.getRegion() != null && location.trim().equalsIgnoreCase(i.getRegion().trim()))
          return i;
    }
    for (MarketDemandIndicator i : indicators)
      if ("NATIONAL".equalsIgnoreCase(i.getRegion())) return i;
    return indicators.get(0);
  }

  public static double round1(double value) {
    return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
  }

  public static double clamp(double value) {
    return Math.max(0, Math.min(100, value));
  }

  public static double proficiency(String level) {
    if (level == null) return .5;
    return switch (level.trim().toUpperCase()) {
      case "INTERMEDIATE" -> .8;
      case "ADVANCED", "EXPERT" -> 1;
      default -> .5;
    };
  }

  public static String importance(BigDecimal weight) {
    double w = weight == null ? 0 : weight.doubleValue();
    return w >= .70 ? "HIGH" : w >= .40 ? "MEDIUM" : "LOW";
  }

  public static String priority(boolean required, BigDecimal weight) {
    double w = weight == null ? 0 : weight.doubleValue();
    return required && w >= .70 ? "CRITICAL" : required ? "HIGH" : w >= .40 ? "MEDIUM" : "LOW";
  }

  public static Comparator<Ranked> rankingOrder() {
    return Comparator.comparingDouble((Ranked r) -> r.score.finalScore())
        .reversed()
        .thenComparing(Comparator.comparingDouble((Ranked r) -> r.score.skillMatchPct()).reversed())
        .thenComparing(Comparator.comparingDouble((Ranked r) -> r.score.demandScore()).reversed())
        .thenComparing(r -> r.occupation.getId());
  }

  public record Ranked(Occupation occupation, Score score, List<OccupationSkill> skills) {}
}
