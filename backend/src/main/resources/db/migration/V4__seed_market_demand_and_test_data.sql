-- =============================================================================
-- W3-T6 - Flyway V4: synthetic LMI history and safe candidate test fixtures
-- Owner: Phan Nguyen Thong | PostgreSQL 16 | ADR-0007
-- IMPORTANT: These figures are synthetic demonstration data, not official LMI.
-- =============================================================================

-- 1. Labor market demand: 12 occupations x 4 regions x 3 months = 144 rows.
-- Reference occupations come from V3 by stable occupation code, never hard-coded IDs.
-- Regions: NATIONAL (Toan quoc), HCMC (TP.HCM), HANOI (Ha Noi), DANANG (Da Nang).
-- Periods: August, September, October 2026. January 2026 records from V3 remain unchanged.
WITH occupation_fixture (occupation_code, base_demand, base_growth, base_openings) AS (
    VALUES
        ('SOC-15-1252', 88.50::numeric, 12.40::numeric, 1250),
        ('SOC-15-1253', 84.00::numeric, 10.20::numeric,  980),
        ('SOC-15-1254', 91.00::numeric, 14.50::numeric, 1420),
        ('SOC-15-1255', 89.00::numeric, 13.00::numeric, 1100),
        ('SOC-15-1256', 79.50::numeric,  8.50::numeric,  620),
        ('SOC-15-1251', 81.00::numeric,  9.10::numeric,  710),
        ('SOC-15-1257', 93.50::numeric, 18.20::numeric,  850),
        ('SOC-15-1258', 87.00::numeric, 11.80::numeric,  420),
        ('SOC-15-1259', 86.50::numeric, 15.00::numeric,  560),
        ('SOC-15-2051', 85.00::numeric, 13.50::numeric,  680),
        ('SOC-15-2052', 90.50::numeric, 16.80::numeric,  940),
        ('SOC-15-1260', 95.00::numeric, 22.50::numeric,  890)
),
region_fixture (region, demand_offset, openings_factor) AS (
    VALUES
        ('NATIONAL',  0.00::numeric, 1.00::numeric),
        ('HCMC',      2.50::numeric, 0.45::numeric),
        ('HANOI',     1.00::numeric, 0.35::numeric),
        ('DANANG',   -3.50::numeric, 0.12::numeric)
),
month_fixture (snapshot_date, demand_offset, openings_factor) AS (
    VALUES
        (DATE '2026-08-01', -1.50::numeric, 0.93::numeric),
        (DATE '2026-09-01',  0.00::numeric, 1.00::numeric),
        (DATE '2026-10-01',  1.50::numeric, 1.07::numeric)
)
INSERT INTO market_demand_indicators (
    occupation_id, region, demand_score, growth_rate_pct,
    open_job_postings_count, competitive_index, snapshot_date
)
SELECT
    o.id,
    r.region,
    ROUND(LEAST(100.00, GREATEST(0.00,
        f.base_demand + r.demand_offset + m.demand_offset)), 2),
    ROUND(f.base_growth + m.demand_offset * 0.20, 2),
    GREATEST(0, ROUND(f.base_openings * r.openings_factor * m.openings_factor)::integer),
    ROUND(LEAST(9.99, GREATEST(0.00,
        f.base_demand / 35.00 + r.demand_offset / 20.00)), 2),
    m.snapshot_date
FROM occupation_fixture f
JOIN occupations o ON o.code = f.occupation_code
CROSS JOIN region_fixture r
CROSS JOIN month_fixture m
WHERE TRUE
ON CONFLICT (occupation_id, region, snapshot_date) DO NOTHING;

-- 2. Non-login candidate fixtures. Reserved .invalid domains and disabled accounts
-- prevent a shared demo password or administrative access from entering the repository.
-- The end-to-end API suite will register its own temporary, active candidate.
INSERT INTO users (email, password_hash, full_name, role, is_active)
VALUES
    ('w3-junior-frontend@seed.invalid', '!disabled-w3-fixture!', 'W3 Junior Frontend', 'ROLE_CANDIDATE', FALSE),
    ('w3-fresher-data@seed.invalid', '!disabled-w3-fixture!', 'W3 Fresher Data', 'ROLE_CANDIDATE', FALSE),
    ('w3-mid-backend@seed.invalid', '!disabled-w3-fixture!', 'W3 Mid Backend', 'ROLE_CANDIDATE', FALSE)
ON CONFLICT (email) DO NOTHING;

-- 3. Candidate profiles: one profile for each of the three test personas.
INSERT INTO candidate_profiles (
    user_id, headline, bio, years_of_experience, target_industry,
    preferred_location, preferred_work_mode, expected_salary_min, expected_salary_max
)
SELECT u.id, p.headline, p.bio, p.years_of_experience,
       'Công nghệ Thông tin', p.preferred_location, p.work_mode,
       p.salary_min, p.salary_max
FROM (VALUES
    ('w3-junior-frontend@seed.invalid', 'Junior Frontend Developer',
     'Ứng viên mẫu: giao diện web React và TypeScript.',
     1.0::numeric, 'HCMC', 'HYBRID', 12000000::numeric, 18000000::numeric),
    ('w3-fresher-data@seed.invalid', 'Fresher Data Analyst / Engineer',
     'Ứng viên mẫu: Python, SQL và phân tích dữ liệu.',
     0.0::numeric, 'HANOI', 'ONSITE', 9000000::numeric, 15000000::numeric),
    ('w3-mid-backend@seed.invalid', 'Mid-level Backend Engineer',
     'Ứng viên mẫu: Java, Spring Boot, PostgreSQL và Docker.',
     3.5::numeric, 'DANANG', 'REMOTE', 25000000::numeric, 38000000::numeric)
) AS p(email, headline, bio, years_of_experience, preferred_location,
       work_mode, salary_min, salary_max)
JOIN users u ON u.email = p.email
WHERE TRUE
ON CONFLICT (user_id) DO NOTHING;

-- 4. Candidate skills: stable skill codes from V3 with realistic proficiencies.
INSERT INTO user_skills (user_id, skill_id, proficiency_level, years_of_experience)
SELECT u.id, s.id, p.proficiency, p.years_experience
FROM (VALUES
    ('w3-junior-frontend@seed.invalid', 'SK-JS',      'INTERMEDIATE', 1.0::numeric),
    ('w3-junior-frontend@seed.invalid', 'SK-TS',      'BEGINNER',     0.5::numeric),
    ('w3-junior-frontend@seed.invalid', 'SK-REACT',   'INTERMEDIATE', 1.0::numeric),
    ('w3-junior-frontend@seed.invalid', 'SK-GIT',     'INTERMEDIATE', 1.0::numeric),
    ('w3-fresher-data@seed.invalid',    'SK-PYTHON',  'INTERMEDIATE', 0.5::numeric),
    ('w3-fresher-data@seed.invalid',    'SK-SQL',     'BEGINNER',     0.5::numeric),
    ('w3-fresher-data@seed.invalid',    'SK-ML',      'BEGINNER',     0.0::numeric),
    ('w3-fresher-data@seed.invalid',    'SK-GIT',     'BEGINNER',     0.5::numeric),
    ('w3-mid-backend@seed.invalid',    'SK-JAVA',    'ADVANCED',     3.5::numeric),
    ('w3-mid-backend@seed.invalid',    'SK-SPRING',  'ADVANCED',     3.0::numeric),
    ('w3-mid-backend@seed.invalid',    'SK-SQL',     'ADVANCED',     3.0::numeric),
    ('w3-mid-backend@seed.invalid',    'SK-POSTGRES','INTERMEDIATE', 2.5::numeric),
    ('w3-mid-backend@seed.invalid',    'SK-DOCKER',  'INTERMEDIATE', 2.0::numeric)
) AS p(email, skill_code, proficiency, years_experience)
JOIN users u ON u.email = p.email
JOIN skills s ON s.code = p.skill_code
WHERE TRUE
ON CONFLICT (user_id, skill_id) DO NOTHING;
