# DevLog: Triển khai 10 JPA Entities & Spring Data Repositories Nền móng

* **Mã tác vụ / Task ID:** W1-T4
* **Người thực hiện / Agent:** Huỳnh Long Bảo Khanh (hỗ trợ bởi Antigravity AI Assistant)
* **Ngày thực hiện (Date):** 2026-10-29
* **Nhánh Git (Branch):** `main`

---

## 1. Mục tiêu (Objective)

Thực hiện task nền móng **W1-T4 (Mô hình dữ liệu PostgreSQL và Migration)**:
1. Ánh xạ chính xác toàn bộ 10 bảng cơ sở dữ liệu đã định nghĩa tại [`backend/src/main/resources/db/migration/V1__initial_schema.sql`](file:///d:/Khanh/Công%20nghệ%20phần%20mềm%20chuyên%20sâu/Project/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/backend/src/main/resources/db/migration/V1__initial_schema.sql) và [`docs/database/database-design.md`](file:///d:/Khanh/Công%20nghệ%20phần%20mềm%20chuyên%20sâu/Project/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/database/database-design.md) thành 10 JPA Entity hoàn chỉnh.
2. Thiết lập 10 Spring Data JPA Repositories tương ứng để phục vụ truy xuất dữ liệu cho các module chức năng kế tiếp.
3. Tuân thủ nghiêm ngặt quy chuẩn lập trình [`docs/development/coding-conventions.md`](file:///d:/Khanh/Công%20nghệ%20phần%20mềm%20chuyên%20sâu/Project/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/development/coding-conventions.md): chỉ định rõ tên bảng, tên cột `snake_case`, sử dụng `FetchType.LAZY` cho toàn bộ quan hệ `@ManyToOne` / `@OneToOne`, tránh đệ quy Lombok, và kiểm soát kiểu dữ liệu khớp chính xác schema PostgreSQL.

---

## 2. Thay đổi thực tế (Changes)

* **Mã nguồn (Source Code):**
  * Đã tạo 10 JPA Entities trong package `com.cnpm.careerrecommendation.entity`:
    * `User.java`: Bảng `users` (id, email, passwordHash, fullName, role, isActive, createdAt, updatedAt).
    * `CandidateProfile.java`: Bảng `candidate_profiles` (id, user_id, headline, bio, yearsOfExperience, targetIndustry, preferredLocation, preferredWorkMode, expectedSalaryMin, expectedSalaryMax, resumeUrl, createdAt, updatedAt).
    * `SkillCategory.java`: Bảng `skill_categories` (id, code, name, description, createdAt, updatedAt).
    * `Skill.java`: Bảng `skills` (id, category_id, code, name, skillType, description, createdAt, updatedAt).
    * `UserSkill.java`: Bảng `user_skills` (id, user_id, skill_id, proficiencyLevel, yearsOfExperience, verified, createdAt, updatedAt) cùng unique constraint `uq_user_skills_user_skill`.
    * `OccupationGroup.java`: Bảng `occupation_groups` (id, code, name, description, createdAt, updatedAt).
    * `Occupation.java`: Bảng `occupations` (id, group_id, code, title, description, industry, entrySalaryAvg, midSalaryAvg, createdAt, updatedAt).
    * `OccupationSkill.java`: Bảng `occupation_skills` (id, occupation_id, skill_id, importanceWeight, isRequired, createdAt, updatedAt) cùng unique constraint `uq_occupation_skills_occ_skill`.
    * `MarketDemandIndicator.java`: Bảng `market_demand_indicators` (id, occupation_id, region, demandScore, growthRatePct, openJobPostingsCount, competitiveIndex, snapshotDate, createdAt, updatedAt) cùng unique constraint `uq_market_demand_occ_region_date`.
    * `RecommendationLog.java`: Bảng `recommendation_logs` (id, user_id, occupation_id, matchScore, skillGapCount, inputSnapshotJson mapping Hibernate JSONB, recommendationDate, createdAt).
  * Đã tạo 10 Spring Data JPA Repositories trong package `com.cnpm.careerrecommendation.repository`:
    * `UserRepository.java`, `CandidateProfileRepository.java`, `SkillCategoryRepository.java`, `SkillRepository.java`, `UserSkillRepository.java`, `OccupationGroupRepository.java`, `OccupationRepository.java`, `OccupationSkillRepository.java`, `MarketDemandIndicatorRepository.java`, `RecommendationLogRepository.java`.
  * Đã tạo bộ kiểm thử phản xạ và ánh xạ thực thể `EntityMappingTest.java` xác thực tính tuân thủ 100% của cấu trúc JPA.
* **Tài liệu chuẩn (Documentation Updates):**
  * Cập nhật `docs/devlogs/README.md` thêm nhật ký của tác vụ W1-T4.

---

## 3. Khu vực bị ảnh hưởng (Affected Areas)

* `backend/src/main/java/com/cnpm/careerrecommendation/entity/*.java` (10 entities)
* `backend/src/main/java/com/cnpm/careerrecommendation/repository/*.java` (10 repositories)
* `backend/src/test/java/com/cnpm/careerrecommendation/entity/EntityMappingTest.java`
* `docs/devlogs/2026-10-29-w1-t4-jpa-entities-and-repositories.md`
* `docs/devlogs/README.md`

---

## 4. Quyết định kỹ thuật quan trọng (Decisions & Rationale)

* **FetchType.LAZY trên toàn bộ quan hệ đối tượng:** Mọi quan hệ `@ManyToOne` và `@OneToOne` đều cấu hình tường minh `fetch = FetchType.LAZY` để tránh truy vấn dữ liệu thừa ngoài ý muốn (N+1 query problem).
* **Kiểu dữ liệu tiền tệ và trọng số chính xác:** Sử dụng `java.math.BigDecimal` cho tất cả các cột `NUMERIC` (`years_of_experience`, `importance_weight`, `demand_score`, `match_score`, `expected_salary_...`, `..._salary_avg`) kèm định nghĩa `precision` và `scale` khớp 100% với DDL.
* **Thời gian có múi giờ:** Sử dụng `java.time.OffsetDateTime` tương ứng kiểu `TIMESTAMP WITH TIME ZONE` của PostgreSQL.
* **Lưu trữ JSONB trong Hibernate 6:** Trường `input_snapshot_json` trong `RecommendationLog` được ánh xạ bằng `@JdbcTypeCode(SqlTypes.JSON)` và `columnDefinition = "jsonb"`, tương thích trực tiếp với PostgreSQL JSONB.
* **Không dùng `@Data`:** Viết getters/setters/constructors tường minh, tránh phát sinh lỗi đệ quy vòng lặp quan hệ 2 chiều.

---

## 5. Kiểm tra & Xác thực (Validation Performed)

| Lệnh / Thao tác kiểm tra | Mục đích kiểm tra | Kết quả thực tế | Ghi chú |
|---|---|---|---|
| `mvn -f backend/pom.xml compile` | Biên dịch toàn bộ 27 tệp mã nguồn Java | **BUILD SUCCESS** (2.4s) | 10 Entities + 10 Repositories biên dịch không lỗi |
| `mvn -f backend/pom.xml test` | Chạy bộ kiểm thử tự động `EntityMappingTest` | **BUILD SUCCESS** (Tests run: 8, Failures: 0, Errors: 0) | Xác thực tên bảng, LAZY loading, Unique constraints, ID generation, Numeric scale |

---

## 6. Công việc tồn đọng & Bước tiếp theo (Outstanding Work)

* [ ] Kết nối với Docker Compose PostgreSQL để kiểm chứng xác thực schema khi khởi động với `spring.jpa.hibernate.ddl-auto: validate`.
* [ ] Phối hợp cùng Task W1-T5 (Đoàn Việt Khang) tích hợp `UserRepository` vào luồng Spring Security & JWT Filter.
* [ ] Triển khai các Use Case theo lộ trình sau khi các phân hệ backend core và auth hoàn thiện.

