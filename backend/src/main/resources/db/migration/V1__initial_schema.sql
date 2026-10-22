-- =============================================================================
-- Migration: V1__initial_schema.sql
-- Description: Baseline database schema for Career Recommendation System
-- Target DBMS: PostgreSQL 16
-- =============================================================================

-- 1. Users table (Authentication & User Accounts)
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'ROLE_CANDIDATE',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);

-- 2. Candidate Profiles table (Profile, Preferences & Career Goals)
CREATE TABLE IF NOT EXISTS candidate_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    headline VARCHAR(255),
    bio TEXT,
    years_of_experience NUMERIC(4,1) NOT NULL DEFAULT 0.0,
    target_industry VARCHAR(100),
    preferred_location VARCHAR(100),
    preferred_work_mode VARCHAR(30) NOT NULL DEFAULT 'HYBRID',
    expected_salary_min NUMERIC(12,2),
    expected_salary_max NUMERIC(12,2),
    resume_url VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_candidate_profiles_user_id ON candidate_profiles(user_id);

-- 3. Skill Categories table (Taxonomy Grouping)
CREATE TABLE IF NOT EXISTS skill_categories (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. Skills table (Standardized Skill Dictionary)
CREATE TABLE IF NOT EXISTS skills (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT REFERENCES skill_categories(id) ON DELETE SET NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    skill_type VARCHAR(30) NOT NULL DEFAULT 'TECHNICAL',
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_skills_name ON skills(name);
CREATE INDEX idx_skills_category_id ON skills(category_id);

-- 5. User Skills table (Skills Claimed or Extracted per Candidate)
CREATE TABLE IF NOT EXISTS user_skills (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    proficiency_level VARCHAR(30) NOT NULL DEFAULT 'INTERMEDIATE',
    years_of_experience NUMERIC(4,1) NOT NULL DEFAULT 0.0,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_skills_user_skill UNIQUE (user_id, skill_id)
);

CREATE INDEX idx_user_skills_user_id ON user_skills(user_id);
CREATE INDEX idx_user_skills_skill_id ON user_skills(skill_id);

-- 6. Occupation Groups table (SOC Major/Minor Groups)
CREATE TABLE IF NOT EXISTS occupation_groups (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 7. Occupations table (Standardized Job Roles)
CREATE TABLE IF NOT EXISTS occupations (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT REFERENCES occupation_groups(id) ON DELETE SET NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    industry VARCHAR(100),
    entry_salary_avg NUMERIC(12,2),
    mid_salary_avg NUMERIC(12,2),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_occupations_title ON occupations(title);
CREATE INDEX idx_occupations_code ON occupations(code);
CREATE INDEX idx_occupations_group_id ON occupations(group_id);

-- 8. Occupation Skills table (Required Skill Weights for Occupations)
CREATE TABLE IF NOT EXISTS occupation_skills (
    id BIGSERIAL PRIMARY KEY,
    occupation_id BIGINT NOT NULL REFERENCES occupations(id) ON DELETE CASCADE,
    skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    importance_weight NUMERIC(3,2) NOT NULL DEFAULT 1.00 CHECK (importance_weight >= 0.00 AND importance_weight <= 1.00),
    is_required BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_occupation_skills_occ_skill UNIQUE (occupation_id, skill_id)
);

CREATE INDEX idx_occupation_skills_occupation_id ON occupation_skills(occupation_id);
CREATE INDEX idx_occupation_skills_skill_id ON occupation_skills(skill_id);

-- 9. Market Demand Indicators table (LMI Labor Market Trends)
CREATE TABLE IF NOT EXISTS market_demand_indicators (
    id BIGSERIAL PRIMARY KEY,
    occupation_id BIGINT NOT NULL REFERENCES occupations(id) ON DELETE CASCADE,
    region VARCHAR(50) NOT NULL DEFAULT 'NATIONAL',
    demand_score NUMERIC(5,2) NOT NULL CHECK (demand_score >= 0.00 AND demand_score <= 100.00),
    growth_rate_pct NUMERIC(5,2),
    open_job_postings_count INTEGER NOT NULL DEFAULT 0,
    competitive_index NUMERIC(3,2),
    snapshot_date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_market_demand_occ_region_date UNIQUE (occupation_id, region, snapshot_date)
);

CREATE INDEX idx_market_demand_occupation_id ON market_demand_indicators(occupation_id);
CREATE INDEX idx_market_demand_snapshot_date ON market_demand_indicators(snapshot_date);

-- 10. Recommendation Logs table (Audit & Recommendation History)
CREATE TABLE IF NOT EXISTS recommendation_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    occupation_id BIGINT NOT NULL REFERENCES occupations(id) ON DELETE CASCADE,
    match_score NUMERIC(5,2) NOT NULL CHECK (match_score >= 0.00 AND match_score <= 100.00),
    skill_gap_count INTEGER NOT NULL DEFAULT 0,
    input_snapshot_json JSONB,
    recommendation_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_recommendation_logs_user_id ON recommendation_logs(user_id);
CREATE INDEX idx_recommendation_logs_occupation_id ON recommendation_logs(occupation_id);
