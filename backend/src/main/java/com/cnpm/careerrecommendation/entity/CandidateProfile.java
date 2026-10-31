package com.cnpm.careerrecommendation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Entity representing candidate profile, preferences, and career goals.
 * Corresponds to the 'candidate_profiles' table in PostgreSQL.
 */
@Entity
@Table(
    name = "candidate_profiles",
    indexes = {
        @Index(name = "idx_candidate_profiles_user_id", columnList = "user_id")
    }
)
public class CandidateProfile {

    public static final String WORK_MODE_REMOTE = "REMOTE";
    public static final String WORK_MODE_HYBRID = "HYBRID";
    public static final String WORK_MODE_ONSITE = "ONSITE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "headline", length = 255)
    private String headline;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "years_of_experience", nullable = false, precision = 4, scale = 1)
    private BigDecimal yearsOfExperience = BigDecimal.ZERO;

    @Column(name = "target_industry", length = 100)
    private String targetIndustry;

    @Column(name = "preferred_location", length = 100)
    private String preferredLocation;

    @Column(name = "preferred_work_mode", nullable = false, length = 30)
    private String preferredWorkMode = WORK_MODE_HYBRID;

    @Column(name = "expected_salary_min", precision = 12, scale = 2)
    private BigDecimal expectedSalaryMin;

    @Column(name = "expected_salary_max", precision = 12, scale = 2)
    private BigDecimal expectedSalaryMax;

    @Column(name = "resume_url", length = 500)
    private String resumeUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public CandidateProfile() {
    }

    public CandidateProfile(User user) {
        this.user = user;
        this.yearsOfExperience = BigDecimal.ZERO;
        this.preferredWorkMode = WORK_MODE_HYBRID;
    }

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        if (this.yearsOfExperience == null) {
            this.yearsOfExperience = BigDecimal.ZERO;
        }
        if (this.preferredWorkMode == null) {
            this.preferredWorkMode = WORK_MODE_HYBRID;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public BigDecimal getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(BigDecimal yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public String getTargetIndustry() {
        return targetIndustry;
    }

    public void setTargetIndustry(String targetIndustry) {
        this.targetIndustry = targetIndustry;
    }

    public String getPreferredLocation() {
        return preferredLocation;
    }

    public void setPreferredLocation(String preferredLocation) {
        this.preferredLocation = preferredLocation;
    }

    public String getPreferredWorkMode() {
        return preferredWorkMode;
    }

    public void setPreferredWorkMode(String preferredWorkMode) {
        this.preferredWorkMode = preferredWorkMode;
    }

    public BigDecimal getExpectedSalaryMin() {
        return expectedSalaryMin;
    }

    public void setExpectedSalaryMin(BigDecimal expectedSalaryMin) {
        this.expectedSalaryMin = expectedSalaryMin;
    }

    public BigDecimal getExpectedSalaryMax() {
        return expectedSalaryMax;
    }

    public void setExpectedSalaryMax(BigDecimal expectedSalaryMax) {
        this.expectedSalaryMax = expectedSalaryMax;
    }

    public String getResumeUrl() {
        return resumeUrl;
    }

    public void setResumeUrl(String resumeUrl) {
        this.resumeUrl = resumeUrl;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

