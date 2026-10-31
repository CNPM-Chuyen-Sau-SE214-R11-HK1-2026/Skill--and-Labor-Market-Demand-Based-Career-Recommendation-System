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
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Entity representing an audit / history log entry for career recommendations generated for a user.
 * Corresponds to the 'recommendation_logs' table in PostgreSQL.
 */
@Entity
@Table(
    name = "recommendation_logs",
    indexes = {
        @Index(name = "idx_recommendation_logs_user_id", columnList = "user_id"),
        @Index(name = "idx_recommendation_logs_occupation_id", columnList = "occupation_id")
    }
)
public class RecommendationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "occupation_id", nullable = false)
    private Occupation occupation;

    @Column(name = "match_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal matchScore;

    @Column(name = "skill_gap_count", nullable = false)
    private Integer skillGapCount = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input_snapshot_json", columnDefinition = "jsonb")
    private String inputSnapshotJson;

    @Column(name = "recommendation_date", nullable = false)
    private OffsetDateTime recommendationDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public RecommendationLog() {
    }

    public RecommendationLog(User user, Occupation occupation, BigDecimal matchScore, Integer skillGapCount, String inputSnapshotJson) {
        this.user = user;
        this.occupation = occupation;
        this.matchScore = matchScore;
        this.skillGapCount = (skillGapCount != null) ? skillGapCount : 0;
        this.inputSnapshotJson = inputSnapshotJson;
    }

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.recommendationDate == null) {
            this.recommendationDate = now;
        }
        if (this.skillGapCount == null) {
            this.skillGapCount = 0;
        }
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

    public Occupation getOccupation() {
        return occupation;
    }

    public void setOccupation(Occupation occupation) {
        this.occupation = occupation;
    }

    public BigDecimal getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(BigDecimal matchScore) {
        this.matchScore = matchScore;
    }

    public Integer getSkillGapCount() {
        return skillGapCount;
    }

    public void setSkillGapCount(Integer skillGapCount) {
        this.skillGapCount = skillGapCount;
    }

    public String getInputSnapshotJson() {
        return inputSnapshotJson;
    }

    public void setInputSnapshotJson(String inputSnapshotJson) {
        this.inputSnapshotJson = inputSnapshotJson;
    }

    public OffsetDateTime getRecommendationDate() {
        return recommendationDate;
    }

    public void setRecommendationDate(OffsetDateTime recommendationDate) {
        this.recommendationDate = recommendationDate;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

