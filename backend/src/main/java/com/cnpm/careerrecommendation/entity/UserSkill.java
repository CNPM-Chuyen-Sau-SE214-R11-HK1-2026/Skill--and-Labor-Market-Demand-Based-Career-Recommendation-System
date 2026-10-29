package com.cnpm.careerrecommendation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Entity representing a skill possessed or claimed by an applicant/user.
 * Corresponds to the 'user_skills' table in PostgreSQL.
 */
@Entity
@Table(
    name = "user_skills",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_skills_user_skill", columnNames = {"user_id", "skill_id"})
    },
    indexes = {
        @Index(name = "idx_user_skills_user_id", columnList = "user_id"),
        @Index(name = "idx_user_skills_skill_id", columnList = "skill_id")
    }
)
public class UserSkill {

    public static final String LEVEL_BEGINNER = "BEGINNER";
    public static final String LEVEL_INTERMEDIATE = "INTERMEDIATE";
    public static final String LEVEL_ADVANCED = "ADVANCED";
    public static final String LEVEL_EXPERT = "EXPERT";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(name = "proficiency_level", nullable = false, length = 30)
    private String proficiencyLevel = LEVEL_INTERMEDIATE;

    @Column(name = "years_of_experience", nullable = false, precision = 4, scale = 1)
    private BigDecimal yearsOfExperience = BigDecimal.ZERO;

    @Column(name = "verified", nullable = false)
    private Boolean verified = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public UserSkill() {
    }

    public UserSkill(User user, Skill skill, String proficiencyLevel, BigDecimal yearsOfExperience) {
        this.user = user;
        this.skill = skill;
        this.proficiencyLevel = (proficiencyLevel != null) ? proficiencyLevel : LEVEL_INTERMEDIATE;
        this.yearsOfExperience = (yearsOfExperience != null) ? yearsOfExperience : BigDecimal.ZERO;
        this.verified = false;
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
        if (this.proficiencyLevel == null) {
            this.proficiencyLevel = LEVEL_INTERMEDIATE;
        }
        if (this.yearsOfExperience == null) {
            this.yearsOfExperience = BigDecimal.ZERO;
        }
        if (this.verified == null) {
            this.verified = false;
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

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public String getProficiencyLevel() {
        return proficiencyLevel;
    }

    public void setProficiencyLevel(String proficiencyLevel) {
        this.proficiencyLevel = proficiencyLevel;
    }

    public BigDecimal getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(BigDecimal yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
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

