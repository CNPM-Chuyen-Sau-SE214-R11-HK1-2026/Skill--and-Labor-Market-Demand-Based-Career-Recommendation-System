package com.cnpm.careerrecommendation.repository;

import com.cnpm.careerrecommendation.entity.RecommendationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for {@link RecommendationLog} entity.
 */
@Repository
public interface RecommendationLogRepository extends JpaRepository<RecommendationLog, Long> {

    List<RecommendationLog> findByUserId(Long userId);

    List<RecommendationLog> findByUserIdOrderByRecommendationDateDesc(Long userId);
}

