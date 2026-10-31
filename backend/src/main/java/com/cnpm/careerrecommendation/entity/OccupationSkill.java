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
 * Entity mapping the required or preferred skills for an occupation, along with their importance weight.
 * Corresponds to the 'occupation_skills' table in PostgreSQL.
 */
@Entity
@Table(
    name = "occupation_skills",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_occupation_skills_occ_skill", columnNames = {"occupation_id", "skill_id"})
    },
    indexes = {
        @Index(name = "idx_occupation_skills_occupation_id", columnList = "occupation_id"),
        @Index(name = "idx_occupation_skills_skill_id", columnList = "skill_id")
    }
)
public class OccupationSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "occupation_id", nullable = false)
    private Occupation occupation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(name = "importance_weight", nullable = false, precision = 3, scale = 2)
    private BigDecimal importanceWeight = BigDecimal.ONE;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public OccupationSkill() {
    }

    public OccupationSkill(Occupation occupation, Skill skill, BigDecimal importanceWeight, Boolean isRequired) {
        this.occupation = occupation;
        this.skill = skill;
        this.importanceWeight = (importanceWeight != null) ? importanceWeight : BigDecimal.ONE;
        this.isRequired = (isRequired != null) ? isRequired : true;
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
        if (this.importanceWeight == null) {
            this.importanceWeight = BigDecimal.ONE;
        }
        if (this.isRequired == null) {
            this.isRequired = true;
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

    public Occupation getOccupation() {
        return occupation;
    }

    public void setOccupation(Occupation occupation) {
        this.occupation = occupation;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public BigDecimal getImportanceWeight() {
        return importanceWeight;
    }

    public void setImportanceWeight(BigDecimal importanceWeight) {
        this.importanceWeight = importanceWeight;
    }

    public Boolean getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Boolean isRequired) {
        this.isRequired = isRequired;
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

