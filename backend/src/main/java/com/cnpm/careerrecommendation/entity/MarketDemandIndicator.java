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
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Entity representing labor market demand indicators and trends for an occupation in a given region and snapshot date.
 * Corresponds to the 'market_demand_indicators' table in PostgreSQL.
 */
@Entity
@Table(
    name = "market_demand_indicators",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_market_demand_occ_region_date", columnNames = {"occupation_id", "region", "snapshot_date"})
    },
    indexes = {
        @Index(name = "idx_market_demand_occupation_id", columnList = "occupation_id"),
        @Index(name = "idx_market_demand_snapshot_date", columnList = "snapshot_date")
    }
)
public class MarketDemandIndicator {

    public static final String REGION_NATIONAL = "NATIONAL";
    public static final String REGION_HANOI = "HANOI";
    public static final String REGION_HCMC = "HCMC";
    public static final String REGION_DANANG = "DANANG";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "occupation_id", nullable = false)
    private Occupation occupation;

    @Column(name = "region", nullable = false, length = 50)
    private String region = REGION_NATIONAL;

    @Column(name = "demand_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal demandScore;

    @Column(name = "growth_rate_pct", precision = 5, scale = 2)
    private BigDecimal growthRatePct;

    @Column(name = "open_job_postings_count", nullable = false)
    private Integer openJobPostingsCount = 0;

    @Column(name = "competitive_index", precision = 3, scale = 2)
    private BigDecimal competitiveIndex;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public MarketDemandIndicator() {
    }

    public MarketDemandIndicator(Occupation occupation, String region, BigDecimal demandScore, LocalDate snapshotDate) {
        this.occupation = occupation;
        this.region = (region != null) ? region : REGION_NATIONAL;
        this.demandScore = demandScore;
        this.snapshotDate = snapshotDate;
        this.openJobPostingsCount = 0;
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
        if (this.region == null) {
            this.region = REGION_NATIONAL;
        }
        if (this.openJobPostingsCount == null) {
            this.openJobPostingsCount = 0;
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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public BigDecimal getDemandScore() {
        return demandScore;
    }

    public void setDemandScore(BigDecimal demandScore) {
        this.demandScore = demandScore;
    }

    public BigDecimal getGrowthRatePct() {
        return growthRatePct;
    }

    public void setGrowthRatePct(BigDecimal growthRatePct) {
        this.growthRatePct = growthRatePct;
    }

    public Integer getOpenJobPostingsCount() {
        return openJobPostingsCount;
    }

    public void setOpenJobPostingsCount(Integer openJobPostingsCount) {
        this.openJobPostingsCount = openJobPostingsCount;
    }

    public BigDecimal getCompetitiveIndex() {
        return competitiveIndex;
    }

    public void setCompetitiveIndex(BigDecimal competitiveIndex) {
        this.competitiveIndex = competitiveIndex;
    }

    public LocalDate getSnapshotDate() {
        return snapshotDate;
    }

    public void setSnapshotDate(LocalDate snapshotDate) {
        this.snapshotDate = snapshotDate;
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

