# DevLog: Hoàn thiện quản lý kỹ năng ứng viên W3-T2

* **Mã tác vụ / Task ID:** W3-T2 / W3-BE-SKL-01
* **Người thực hiện / Agent:** Codex
* **Ngày thực hiện (Date):** 2026-11-16
* **Nhánh Git (Branch):** `ja-gnahk/CNPM-Chuyen-sau`

---

## 1. Mục tiêu (Objective)

Hoàn thiện bốn API `/api/v1/profile/skills`, bảo vệ ownership theo JWT principal, chuẩn hóa lỗi duplicate/not-found, bổ sung validation tại service và kiểm thử hành vi cho W3-T2.

---

## 2. Thay đổi thực tế (Changes)

* **Mã nguồn (Source Code):**
  * Bổ sung validation nghiệp vụ cho `skillId`, `proficiencyLevel` và `yearsExperience` trong `ProfileService` để các quy tắc vẫn được bảo vệ khi service được gọi ngoài Controller.
  * Chuẩn hóa lỗi `SKILL_ALREADY_EXISTS` (409) và `SKILL_NOT_FOUND` (404).
  * Dùng `saveAndFlush` và chuyển `DataIntegrityViolationException` thành `SKILL_ALREADY_EXISTS` để xử lý race tại unique constraint `(user_id, skill_id)`.
  * Giữ ownership query theo cặp `(userId, skillId)`; không nhận owner ID từ request.
* **Kiểm thử (Tests):**
  * Thêm `ProfileSkillServiceTest` với 13 test cho CRUD, validation, ownership/IDOR và duplicate race.
  * Đổi tên PostgreSQL integration suite từ `ProfilePostgresIT` thành `ProfileSkillControllerIT`, bổ sung assertion error code và trường hợp `yearsExperience` âm.
* **Tài liệu chuẩn (Documentation Updates):**
  * Cập nhật `docs/api/profile.md` với error code `SKILL_NOT_FOUND` và `SKILL_ALREADY_EXISTS`.

---

## 3. Khu vực bị ảnh hưởng (Affected Areas)

* `backend/src/main/java/com/cnpm/careerrecommendation/module/profile/service/ProfileService.java`
* `backend/src/test/java/com/cnpm/careerrecommendation/module/profile/ProfileSkillServiceTest.java`
* `backend/src/test/java/com/cnpm/careerrecommendation/module/profile/ProfileSkillControllerIT.java`
* `docs/api/profile.md`
* `docs/devlogs/2026-11-16-w3-t2-profile-skill-management.md`

---

## 4. Quyết định kỹ thuật quan trọng (Decisions & Rationale)

* **Giữ quan hệ UserSkill trực tiếp với User:** Schema V1 và `docs/api/profile.md` quy định `user_skills.user_id`; skill CRUD phải hoạt động trước khi có `CandidateProfile`. Không thay schema hoặc ép tạo hồ sơ rỗng.
* **Không tạo service interface/mapper/enum mới:** Module hiện tại dùng `ProfileService`, DTO record và hằng số level trong `UserSkill`; tái sử dụng cấu trúc này là thay đổi ít xâm lấn nhất.
* **Khóa ghi theo User và vẫn bắt unique violation:** Pessimistic lock tuần tự hóa các request qua service; unique constraint và exception mapping vẫn là lớp bảo vệ cuối nếu có writer khác hoặc race ngoài luồng kiểm tra.

---

## 5. Kiểm tra & Xác thực (Validation Performed)

| Lệnh / Thao tác kiểm tra | Mục đích kiểm tra | Kết quả thực tế | Ghi chú |
|---|---|---|---|
| `mvn -B -ntp -f backend/pom.xml -Dtest=ProfileSkillServiceTest test` | Unit test service W3-T2 | Thành công: 13/13 | 0 failure, 0 error |
| `mvn -B -ntp -f backend/pom.xml clean test` | Toàn bộ backend unit suite | Thành công: 91/91 | 0 failure, 0 error |
| `mvn -B -ntp -f backend/pom.xml -DskipTests package` | Compile và đóng gói backend | Thành công | Tạo Spring Boot JAR |
| `git diff --check` | Kiểm tra whitespace/diff | Thành công | Không có lỗi |
| PostgreSQL environment probe | Kiểm tra khả năng chạy IT | Không có môi trường | Không có biến `AUTH_TEST_DATABASE_*`, port 5432 đóng, không có `psql`, Docker daemon tắt |
| `mvn -B -ntp -f backend/pom.xml -Ppostgres-it "-Dit.test=ProfileSkillControllerIT" test-compile failsafe:integration-test failsafe:verify` | Chạy PostgreSQL IT mục tiêu | Không chạy được: 15/15 lỗi khởi tạo context | Root cause: thiếu `AUTH_TEST_DATABASE_URL`; chưa chạy assertion endpoint |

---

## 6. Công việc tồn đọng & Bước tiếp theo (Outstanding Work)

* [ ] Cấu hình một PostgreSQL test cô lập qua `AUTH_TEST_DATABASE_URL`, `AUTH_TEST_DATABASE_USER`, `AUTH_TEST_DATABASE_PASSWORD`, rồi chạy lại `ProfileSkillControllerIT`.
* [ ] Chạy toàn bộ profile `postgres-it` và kiểm tra HTTP runtime trên một backend kết nối PostgreSQL trước khi chuyển trạng thái sang DONE — VERIFIED.
