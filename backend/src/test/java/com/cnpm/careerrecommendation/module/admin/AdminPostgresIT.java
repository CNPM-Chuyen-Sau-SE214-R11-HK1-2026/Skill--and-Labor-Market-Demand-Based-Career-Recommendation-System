package com.cnpm.careerrecommendation.module.admin;

import com.cnpm.careerrecommendation.common.entity.Role;
import com.cnpm.careerrecommendation.common.entity.User;
import com.cnpm.careerrecommendation.common.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AdminPostgresIT {
    private static final String TEST_SECRET = "integration-test-only-private-key-never-deploy-123456789";

    @Autowired
    private UserRepository users;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> required("AUTH_TEST_DATABASE_URL"));
        registry.add("spring.datasource.username", () -> required("AUTH_TEST_DATABASE_USER"));
        registry.add("spring.datasource.password", () -> required("AUTH_TEST_DATABASE_PASSWORD"));
        registry.add("security.jwt.secret", () -> TEST_SECRET);
    }

    @Test
    void combinesEmailRoleAndActiveFilters() {
        String prefix = uniquePrefix();
        save(prefix + "-matching@test.invalid", Role.ROLE_ADMIN, true);
        save(prefix + "-wrong-role@test.invalid", Role.ROLE_CANDIDATE, true);
        save(prefix + "-inactive@test.invalid", Role.ROLE_ADMIN, false);
        save("unrelated-" + UUID.randomUUID() + "@test.invalid", Role.ROLE_ADMIN, true);

        var result = users.searchAdminUsers(
                prefix.toUpperCase(),
                Role.ROLE_ADMIN,
                true,
                PageRequest.of(0, 10, Sort.by("email")));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).extracting(User::getEmail)
                .containsExactly(prefix + "-matching@test.invalid");
    }

    @Test
    void paginatesFilteredUsersInDeterministicOrder() {
        String prefix = uniquePrefix();
        for (int index = 0; index < 5; index++) {
            save(prefix + "-" + index + "@test.invalid", Role.ROLE_CANDIDATE, true);
        }

        var result = users.searchAdminUsers(
                prefix,
                Role.ROLE_CANDIDATE,
                true,
                PageRequest.of(1, 2, Sort.by("email")));

        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getContent()).extracting(User::getEmail)
                .containsExactly(prefix + "-2@test.invalid", prefix + "-3@test.invalid");
    }

    @Test
    void returnsEmptyPageWhenFiltersDoNotMatch() {
        var result = users.searchAdminUsers(
                "missing-" + UUID.randomUUID(),
                Role.ROLE_ADMIN,
                false,
                PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    private void save(String email, Role role, boolean active) {
        User user = new User(email, "unused-test-hash", "Admin query fixture", role);
        user.setActive(active);
        users.save(user);
    }

    private static String uniquePrefix() {
        return "admin-query-" + UUID.randomUUID();
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Set " + name + " to an isolated test PostgreSQL database");
        }
        return value;
    }
}
