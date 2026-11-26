package com.cnpm.careerrecommendation.common.repository;

import com.cnpm.careerrecommendation.common.entity.User;
import com.cnpm.careerrecommendation.common.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);

    @Query("""
            SELECT u FROM User u
            WHERE (CAST(:email AS String) IS NULL
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:email AS String), '%')))
              AND (:role IS NULL OR u.role = :role)
              AND (:active IS NULL OR u.active = :active)
            """)
    Page<User> searchAdminUsers(@Param("email") String email,
                                @Param("role") Role role,
                                @Param("active") Boolean active,
                                Pageable pageable);
}
