package com.cnpm.careerrecommendation.repository;

import com.cnpm.careerrecommendation.entity.RecommendationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for {@link RecommendationLog} entity.
 */
@Repository
public interface RecommendationLogRepository extends JpaRepository<RecommendationLog, Long> {

    List<RecommendationLog> findByUserId(Long userId);

    List<RecommendationLog> findByUserIdOrderByRecommendationDateDesc(Long userId);

    @Query(
        value = """
            SELECT rl FROM RecommendationLog rl
            JOIN FETCH rl.occupation o
            LEFT JOIN FETCH o.group
            WHERE rl.user.id = :userId
        """,
        countQuery = """
            SELECT COUNT(rl) FROM RecommendationLog rl
            WHERE rl.user.id = :userId
        """
    )
    Page<RecommendationLog> findHistoryByUserId(@Param("userId") Long userId, Pageable pageable);
}

