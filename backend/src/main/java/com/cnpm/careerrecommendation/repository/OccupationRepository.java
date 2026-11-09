package com.cnpm.careerrecommendation.repository;

import com.cnpm.careerrecommendation.entity.Occupation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for {@link Occupation} entity.
 */
@Repository
public interface OccupationRepository extends JpaRepository<Occupation, Long> {

    Optional<Occupation> findByCode(String code);

    boolean existsByCode(String code);

    List<Occupation> findByGroupId(Long groupId);

    List<Occupation> findByIndustry(String industry);

    List<Occupation> findByTitleContainingIgnoreCase(String keyword);

    @Query(
        value = """
            SELECT o FROM Occupation o LEFT JOIN FETCH o.group
            WHERE (:groupId IS NULL OR o.group.id = :groupId)
              AND (CAST(:keyword AS String) IS NULL
                   OR LOWER(o.title) LIKE LOWER(CONCAT('%', CAST(:keyword AS String), '%'))
                   OR (o.description IS NOT NULL AND LOWER(o.description) LIKE LOWER(CONCAT('%', CAST(:keyword AS String), '%'))))
        """,
        countQuery = """
            SELECT COUNT(o) FROM Occupation o
            WHERE (:groupId IS NULL OR o.group.id = :groupId)
              AND (CAST(:keyword AS String) IS NULL
                   OR LOWER(o.title) LIKE LOWER(CONCAT('%', CAST(:keyword AS String), '%'))
                   OR (o.description IS NOT NULL AND LOWER(o.description) LIKE LOWER(CONCAT('%', CAST(:keyword AS String), '%'))))
        """
    )
    Page<Occupation> searchOccupations(@Param("groupId") Long groupId,
                                       @Param("keyword") String keyword,
                                       Pageable pageable);
}
