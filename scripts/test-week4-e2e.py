
#!/usr/bin/env python3
"""
W4-T6 - Local Docker E2E smoke test.
Uses Python standard library only.

Current scope: existing W3 APIs and W4 demo accounts.
W4-T2 / W4-T3 admin tests will be added after merge.
"""

import json
import os
import subprocess
import sys
import uuid
from pathlib import Path
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

BASE_URL = "http://127.0.0.1:8080"
REPO_ROOT = Path(__file__).resolve().parent.parent
CHECKS = 0


def check(name, valid):
    global CHECKS

    if not valid:
        raise AssertionError("FAIL - " + name)

    CHECKS += 1
    print(f"PASS {CHECKS:02d} - {name}", flush=True)


def api(method, path, token=None, payload=None):
    headers = {"Accept": "application/json"}
    data = None

    if token:
        headers["Authorization"] = "Bearer " + token

    if payload is not None:
        data = json.dumps(payload).encode("utf-8")
        headers["Content-Type"] = "application/json; charset=utf-8"

    request = Request(
        BASE_URL + path,
        data=data,
        headers=headers,
        method=method
    )

    try:
        with urlopen(request, timeout=20) as response:
            status = response.status
            raw = response.read()

    except HTTPError as error:
        status = error.code
        raw = error.read()

    except URLError as error:
        raise RuntimeError(
            "Backend localhost:8080 unavailable: " + str(error.reason)
        ) from error

    try:
        result = json.loads(raw.decode("utf-8")) if raw else None
    except (ValueError, UnicodeDecodeError):
        result = None

    return status, result


def docker(*args):
    process = subprocess.run(
        ["docker", "compose", *args],
        cwd=REPO_ROOT,
        capture_output=True,
        text=True,
        encoding="utf-8",
        errors="replace",
        check=False
    )

    if process.returncode != 0:
        message = process.stderr or process.stdout
        raise RuntimeError("Docker command failed: " + message[:450])

    return process.stdout.strip()


def database_sql(sql):
    user = docker(
        "exec", "-T", "postgres", "printenv", "POSTGRES_USER"
    )
    database = docker(
        "exec", "-T", "postgres", "printenv", "POSTGRES_DB"
    )

    if not user or not database:
        raise RuntimeError("Missing PostgreSQL environment")

    return docker(
        "exec", "-T", "postgres",
        "psql", "-X", "-qAt",
        "-v", "ON_ERROR_STOP=1",
        "-U", user,
        "-d", database,
        "-c", sql
    )


def main():
    candidate_password = os.environ.get(
        "W4_DEMO_CANDIDATE_PASSWORD", ""
    )
    admin_password = os.environ.get(
        "W4_DEMO_ADMIN_PASSWORD", ""
    )

    if not candidate_password or not admin_password:
        raise RuntimeError(
            "Set W4_DEMO_CANDIDATE_PASSWORD and "
            "W4_DEMO_ADMIN_PASSWORD first."
        )

    print("===== W4-T6: E2E BASELINE =====")

    # STAGE 1: Health

    print("===== STAGE 1: HEALTH =====")

    status, health = api("GET", "/api/health")

    check("Backend HTTP 200", status == 200)
    check(
        "Backend UP",
        isinstance(health, dict) and health.get("status") == "UP"
    )

    # STAGE 2: Authentication

    print("===== STAGE 2: AUTH =====")

    status, candidate = api(
        "POST",
        "/api/v1/auth/login",
        payload={
            "email": "w4-demo-candidate@demo.invalid",
            "password": candidate_password
        }
    )

    check("Candidate login HTTP 200", status == 200)

    candidate_token = (
        candidate.get("accessToken")
        if isinstance(candidate, dict) else None
    )

    check(
        "Candidate JWT and role",
        bool(candidate_token)
        and candidate.get("user", {}).get("role") == "ROLE_CANDIDATE"
    )

    status, admin = api(
        "POST",
        "/api/v1/auth/login",
        payload={
            "email": "w4-demo-admin@demo.invalid",
            "password": admin_password
        }
    )

    check("Admin login HTTP 200", status == 200)

    admin_token = (
        admin.get("accessToken")
        if isinstance(admin, dict) else None
    )

    check(
        "Admin JWT and role",
        bool(admin_token)
        and admin.get("user", {}).get("role") == "ROLE_ADMIN"
    )

    status, identity = api(
        "GET", "/api/v1/auth/me", candidate_token
    )

    check("Candidate identity HTTP 200", status == 200)

    check(
        "Candidate identity matches",
        isinstance(identity, dict)
        and identity.get("email") == "w4-demo-candidate@demo.invalid"
    )

    # STAGE 3: Profile and skills

    print("===== STAGE 3: PROFILE AND SKILLS =====")

    status, profile = api(
        "GET", "/api/v1/profile", candidate_token
    )

    check("Candidate profile HTTP 200", status == 200)

    check(
        "Candidate profile seeded",
        isinstance(profile, dict)
        and bool(profile.get("headline"))
    )

    status, skills = api(
        "GET", "/api/v1/profile/skills", candidate_token
    )

    check("Candidate skills HTTP 200", status == 200)

    skill_codes = (
        {
            skill.get("skillCode")
            for skill in skills
            if isinstance(skill, dict)
        }
        if isinstance(skills, list) else set()
    )

    check(
        "Candidate has 6 seeded skills",
        len(skill_codes) >= 6 and "SK-JAVA" in skill_codes
    )

    # STAGE 4: Occupations

    print("===== STAGE 4: OCCUPATIONS =====")

    status, occupations = api(
        "GET", "/api/v1/occupations?page=0&size=20"
    )

    check("Occupations HTTP 200", status == 200)

    items = (
        occupations.get("content", [])
        if isinstance(occupations, dict) else []
    )

    check(
        "At least 10 occupations",
        isinstance(items, list) and len(items) >= 10
    )

    # STAGE 5: Recommendations

    print("===== STAGE 5: RECOMMENDATIONS =====")

    status, recommendations = api(
        "GET",
        "/api/v1/recommendations?limit=10",
        candidate_token
    )

    check("Recommendations HTTP 200", status == 200)

    check(
        "Recommendations non-empty",
        isinstance(recommendations, list)
        and len(recommendations) > 0
    )

    top = recommendations[0]

    occupation_id = (
        top.get("occupationId")
        if isinstance(top, dict) else None
    )

    check(
        "Recommendation ID and score valid",
        isinstance(occupation_id, int)
        and occupation_id > 0
        and isinstance(top.get("finalScore"), (int, float))
        and 0 <= top["finalScore"] <= 100
    )

    # STAGE 6: Skill Gap

    print("===== STAGE 6: SKILL GAP =====")

    status, gap = api(
        "GET",
        f"/api/v1/recommendations/{occupation_id}/skill-gap",
        candidate_token
    )

    check("Skill Gap HTTP 200", status == 200)

    check(
        "Skill Gap occupation matches",
        isinstance(gap, dict)
        and gap.get("occupationId") == occupation_id
    )

    check(
        "Skill Gap categories present",
        isinstance(gap, dict)
        and all(
            isinstance(gap.get(key), list)
            for key in (
                "matchedSkills",
                "missingEssentialSkills",
                "missingOptionalSkills"
            )
        )
    )

    # STAGE 7: Admin LMI Import

    print("===== STAGE 7: ADMIN LMI IMPORT =====")

    region = "W4E2E" + uuid.uuid4().hex[:8].upper()
    snapshot = "2026-10-01"

    item = {
        "occupationCode": "SOC-15-1252",
        "region": region,
        "demandScore": 97.3,
        "growthRatePct": 17.5,
        "openJobPostingsCount": 123,
        "competitiveIndex": 2.3,
        "snapshotDate": snapshot
    }

    endpoint = "/api/v1/admin/lmi/import"
    payload = {"items": [item]}

    status, _ = api(
        "POST", endpoint, payload=payload
    )

    check("Anonymous LMI import HTTP 401", status == 401)

    status, _ = api(
        "POST", endpoint, candidate_token, payload
    )

    check("Candidate LMI import HTTP 403", status == 403)

    # Use a random region to avoid changing existing seed data.
    # Always clean temporary data after running the import.

    import_started = False

    try:
        import_started = True

        status, result = api(
            "POST", endpoint, admin_token, payload
        )

        check("Admin LMI import HTTP 200", status == 200)

        check(
            "LMI inserted",
            isinstance(result, dict)
            and result.get("totalRecordsProcessed") == 1
            and result.get("recordsInserted") == 1
        )

        stored = database_sql(
            "SELECT demand_score "
            "FROM market_demand_indicators "
            f"WHERE region = '{region}' "
            f"AND snapshot_date = DATE '{snapshot}';"
        )

        check(
            "LMI persisted in PostgreSQL",
            abs(float(stored) - 97.3) < 0.01
        )

        # Upsert the same record with a new score.

        item["demandScore"] = 92.2

        status, result = api(
            "POST", endpoint, admin_token, payload
        )

        check("Admin LMI upsert HTTP 200", status == 200)

        check(
            "LMI updated",
            isinstance(result, dict)
            and result.get("recordsUpdated") == 1
        )

        stored = database_sql(
            "SELECT demand_score "
            "FROM market_demand_indicators "
            f"WHERE region = '{region}' "
            f"AND snapshot_date = DATE '{snapshot}';"
        )

        check(
            "LMI update persisted",
            abs(float(stored) - 92.2) < 0.01
        )

    finally:
        if import_started:
            database_sql(
                "DELETE FROM market_demand_indicators "
                f"WHERE region = '{region}' "
                f"AND snapshot_date = DATE '{snapshot}';"
            )

            print("Temporary LMI cleaned.")

    # STAGE 8: Pending Week 4 backend modules

    print("===== WEEK 4 DEPENDENCIES =====")

    print("PENDING - Admin Users/Stats (W4-T2)")
    print("PENDING - Admin Taxonomy CRUD (W4-T3)")

    print(
        f"===== BASELINE PASSED: {CHECKS} CHECKS; "
        "W4 ADMIN TESTS PENDING ====="
    )


if __name__ == "__main__":
    try:
        main()

    except (
        AssertionError,
        RuntimeError,
        ValueError,
        OSError,
        TypeError
    ) as error:
        print("ERROR: " + str(error), file=sys.stderr)
        sys.exit(1)
