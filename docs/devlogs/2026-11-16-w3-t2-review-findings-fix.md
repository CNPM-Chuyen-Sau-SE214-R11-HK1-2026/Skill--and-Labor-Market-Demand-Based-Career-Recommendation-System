# DevLog: Sửa review findings W3-T2

* **Mã tác vụ / Task ID:** W3-T2 review findings
* **Người thực hiện / Agent:** Codex
* **Ngày thực hiện (Date):** 2026-11-16
* **Nhánh Git (Branch):** `ja-gnahk/CNPM-Chuyen-sau`

---

## 1. Mục tiêu (Objective)

Sửa hai finding sau review: chỉ ánh xạ đúng unique constraint của `user_skills` thành lỗi duplicate và bổ sung unit test validation riêng cho `updateSkill`.

---

## 2. Thay đổi thực tế (Changes)

* **Mã nguồn (Source Code):**
  * Duyệt cause chain của `DataIntegrityViolationException` để nhận diện Hibernate `ConstraintViolationException`.
  * Chỉ trả `409 SKILL_ALREADY_EXISTS` khi constraint name là `uq_user_skills_user_skill`.
  * Ném lại nguyên bản các integrity violation không phải duplicate để global handler xử lý an toàn.
* **Kiểm thử (Tests):**
  * Dùng cause chain Hibernate thực trong unit test duplicate constraint.
  * Thêm regression test xác nhận constraint khác không bị đổi thành duplicate.
  * Thêm test `updateSkill` từ chối số năm âm và proficiency không hợp lệ trước khi truy cập database.
* **Tài liệu chuẩn (Documentation Updates):**
  * Không đổi API contract, schema hoặc kiến trúc; không cần sửa tài liệu chuẩn.

---

## 3. Khu vực bị ảnh hưởng (Affected Areas)

* `backend/src/main/java/com/cnpm/careerrecommendation/module/profile/service/ProfileService.java`
* `backend/src/test/java/com/cnpm/careerrecommendation/module/profile/ProfileSkillServiceTest.java`
* `docs/devlogs/2026-11-16-w3-t2-review-findings-fix.md`

---

## 4. Quyết định kỹ thuật quan trọng (Decisions & Rationale)

* **Dùng Hibernate constraint metadata:** `ConstraintViolationException#getConstraintName()` phân biệt constraint chính xác mà không phân tích chuỗi SQL và không cần dependency mới.
* **Giữ nguyên exception không liên quan:** Tránh báo sai 409 cho foreign-key hoặc integrity failure khác; `GlobalExceptionHandler` tiếp tục che thông tin nội bộ.
* **Không thay đổi transaction/locking:** Pessimistic lock theo User và unique constraint hiện tại vẫn là hai lớp bảo vệ duplicate.

---

## 5. Kiểm tra & Xác thực (Validation Performed)

| Lệnh / Thao tác kiểm tra | Mục đích kiểm tra | Kết quả thực tế | Ghi chú |
|---|---|---|---|
| `mvn -B -ntp -f backend/pom.xml -Dtest=ProfileSkillServiceTest test` | Unit test service | Thành công: 16/16 | 0 failure, 0 error |
| `mvn -B -ntp -f backend/pom.xml clean test` | Toàn bộ backend unit suite | Thành công: 94/94 | 0 failure, 0 error |
| `mvn -B -ntp -f backend/pom.xml -DskipTests package` | Đóng gói backend | Thành công | Spring Boot JAR được tạo |
| `mvn -B -ntp -f backend/pom.xml -Ppostgres-it "-Dit.test=ProfileSkillControllerIT" test-compile failsafe:integration-test failsafe:verify` | PostgreSQL HTTP integration test | Thành công: 15/15 | Chạy trên database test cô lập, sau đó đã drop database |

---

## 6. Công việc tồn đọng & Bước tiếp theo (Outstanding Work)

* Không còn finding nào trong phạm vi yêu cầu này.
