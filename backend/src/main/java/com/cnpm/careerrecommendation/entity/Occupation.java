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

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Entity representing a standardized occupation / job role.
 * Corresponds to the 'occupations' table in PostgreSQL.
 */
@Entity
@Table(
    name = "occupations",
    indexes = {
        @Index(name = "idx_occupations_title", columnList = "title"),
        @Index(name = "idx_occupations_code", columnList = "code"),
        @Index(name = "idx_occupations_group_id", columnList = "group_id")
    }
)
public class Occupation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private OccupationGroup group;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "industry", length = 100)
    private String industry;

    @Column(name = "entry_salary_avg", precision = 12, scale = 2)
    private BigDecimal entrySalaryAvg;

    @Column(name = "mid_salary_avg", precision = 12, scale = 2)
    private BigDecimal midSalaryAvg;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Occupation() {
    }

    public Occupation(String code, String title, String industry, OccupationGroup group) {
        this.code = code;
        this.title = title;
        this.industry = industry;
        this.group = group;
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

    public OccupationGroup getGroup() {
        return group;
    }

    public void setGroup(OccupationGroup group) {
        this.group = group;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public BigDecimal getEntrySalaryAvg() {
        return entrySalaryAvg;
    }

    public void setEntrySalaryAvg(BigDecimal entrySalaryAvg) {
        this.entrySalaryAvg = entrySalaryAvg;
    }

    public BigDecimal getMidSalaryAvg() {
        return midSalaryAvg;
    }

    public void setMidSalaryAvg(BigDecimal midSalaryAvg) {
        this.midSalaryAvg = midSalaryAvg;
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

