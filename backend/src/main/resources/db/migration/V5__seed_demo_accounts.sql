
-- =============================================================================
-- W4-T6 - Flyway V5: Demo Candidate and Admin Accounts
-- Owner: Phan Nguyen Thong
-- PostgreSQL 16 | ADR-0007 | Demo environments only
--
-- Passwords are stored as BCrypt hashes (cost 12).
-- Demo accounts are seeded only when the PostgreSQL connection has:
--     w4.demo_accounts_enabled=on
--
-- Do NOT enable this flag in production.
-- =============================================================================

-- 1. Seed login-enabled demo users.
-- Fixed demo credentials are intended for local/isolated testing only.

INSERT INTO users (
    email,
    password_hash,
    full_name,
    role,
    is_active
)
SELECT
    d.email,
    d.password_hash,
    d.full_name,
    d.role,
    TRUE
FROM (
    VALUES
    (
        'w4-demo-candidate@demo.invalid',
        '$2y$12$EoWa0sDIg3TPRwoJ3uf9vOmcyYkkasjmwLbHSl0l9LVX.6zqLuW4O',
        'W4 Demo Candidate',
        'ROLE_CANDIDATE'
    ),
    (
        'w4-demo-admin@demo.invalid',
        '$2y$12$UvVVdnMOo7whrAV3LvXsaelNhBfQqehp.gWoiSALCL7o7zFABJk42',
        'W4 Demo Administrator',
        'ROLE_ADMIN'
    )
) AS d(email, password_hash, full_name, role)
WHERE current_setting('w4.demo_accounts_enabled', TRUE) = 'on'
ON CONFLICT (email) DO NOTHING;


-- 2. Create the Candidate demo profile.
-- Reference the user by email, never by hard-coded ID.

INSERT INTO candidate_profiles (
    user_id,
    headline,
    bio,
    years_of_experience,
    target_industry,
    preferred_location,
    preferred_work_mode,
    expected_salary_min,
    expected_salary_max
)
SELECT
    u.id,
    'Junior Backend Developer',
    'W4 demo candidate for career recommendation and skill-gap testing.',
    1.0,
    'Công nghệ Thông tin',
    'HCMC',
    'HYBRID',
    12000000.00,
    20000000.00
FROM users u
WHERE u.email = 'w4-demo-candidate@demo.invalid'
  AND u.role = 'ROLE_CANDIDATE'
  AND u.is_active = TRUE
  AND current_setting('w4.demo_accounts_enabled', TRUE) = 'on'
ON CONFLICT (user_id) DO NOTHING;


-- 3. Seed skills for the Candidate demo profile.
-- Skill codes come from Flyway V3.
-- All IDs are resolved dynamically.

INSERT INTO user_skills (
    user_id,
    skill_id,
    proficiency_level,
    years_of_experience
)
SELECT
    u.id,
    s.id,
    fixture.proficiency_level,
    fixture.years_of_experience
FROM (
    VALUES
        ('SK-JAVA',     'ADVANCED',     1.0::numeric),
        ('SK-SPRING',   'INTERMEDIATE', 1.0::numeric),
        ('SK-SQL',      'INTERMEDIATE', 1.0::numeric),
        ('SK-POSTGRES', 'BEGINNER',     0.5::numeric),
        ('SK-GIT',      'INTERMEDIATE', 1.0::numeric),
        ('SK-DOCKER',   'BEGINNER',     0.5::numeric)
) AS fixture(skill_code, proficiency_level, years_of_experience)
JOIN skills s ON s.code = fixture.skill_code
JOIN users u ON u.email = 'w4-demo-candidate@demo.invalid'
WHERE u.role = 'ROLE_CANDIDATE'
  AND u.is_active = TRUE
  AND current_setting('w4.demo_accounts_enabled', TRUE) = 'on'
ON CONFLICT (user_id, skill_id) DO NOTHING;
