# DevLog: Giải quyết Request Changes PR #17 — W4-T2

* **Mã tác vụ / Task ID:** PR #17 / W4-T2 review follow-up
* **Người thực hiện / Agent:** Codex
* **Ngày thực hiện (Date):** 2026-11-27
* **Nhánh Git (Branch):** `feature/w4-t2-admin-user-management`

---

## 1. Mục tiêu (Objective)

Khắc phục lỗi PostgreSQL khi Admin mở danh sách user không truyền email và hoàn thiện các góp ý review về lỗi sai kiểu tham số, pagination overflow, tự hạ quyền và thứ tự phân trang.

---

## 2. Thay đổi thực tế (Changes)

* **Mã nguồn:**
  * Ép kiểu `email` thành `String` trong JPQL để Hibernate 6 bind giá trị null đúng kiểu trên PostgreSQL.
  * Trả `400 INVALID_ADMIN_REQUEST` cho query/path parameter sai kiểu trong phạm vi Admin controller.
  * Chặn offset vượt `Integer.MAX_VALUE` ở controller và service.
  * Chặn Admin tự đổi role xuống `ROLE_CANDIDATE`; danh tính lấy từ `AuthenticatedUser`.
  * Thêm tie-breaker `id DESC` sau `createdAt DESC`.
* **Kiểm thử:**
  * Mở rộng unit/controller test cho validation, overflow, principal và stable sort.
  * Mở rộng `AdminPostgresIT` cho nullable filters, count query, stable pagination và HTTP request mặc định.
* **Tài liệu chuẩn:**
  * Cập nhật mục Admin trong `docs/api/api-contract.md`.

---

## 3. Khu vực bị ảnh hưởng (Affected Areas)

* `backend/src/main/java/com/cnpm/careerrecommendation/common/repository/UserRepository.java`
* `backend/src/main/java/com/cnpm/careerrecommendation/module/admin/**`
* `backend/src/test/java/com/cnpm/careerrecommendation/module/admin/**`
* `docs/api/api-contract.md`

---

## 4. Quyết định kỹ thuật quan trọng (Decisions & Rationale)

* Tái sử dụng cách `CAST(:parameter AS String)` đã được kiểm chứng tại commit `30fa3eb`; không đổi sang native SQL hoặc lọc trong Java.
* Giữ offset pagination hiện tại và thêm khóa sắp xếp duy nhất; không tuyên bố bảo vệ khỏi dữ liệu thay đổi đồng thời giữa hai request.
* Giữ nguyên JWT, `SecurityConfig`, schema và migration.

---

## 5. Kiểm tra & Xác thực (Validation Performed)

| Lệnh / Thao tác kiểm tra | Mục đích | Kết quả thực tế | Ghi chú |
|---|---|---|---|
| Baseline `mvn ... "-Dtest=AdminServiceTest,AdminUserControllerTest" test` | Test W4-T2 trước thay đổi | 26/26 pass | Service 16; Controller 10 |
| Baseline `mvn ... clean test` | Unit regression trước thay đổi | 139/139 pass | Clean worktree |
| PostgreSQL regression trên code cũ | Tái hiện nullable email bug | 2 error | SQLState 42883, `lower(bytea)` |
| `mvn ... "-Dtest=AdminServiceTest,AdminUserControllerTest" test` | Targeted test sau sửa | 33/33 pass | Service 19; Controller 14 |
| `mvn ... -Ppostgres-it "-Dit.test=AdminPostgresIT" ...` | PostgreSQL/HTTP regression | 10/10 pass | PostgreSQL 16.15 |
| `mvn -B -ntp -f backend/pom.xml -Ppostgres-it verify` | Full unit và PostgreSQL regression | 197/197 pass | 146 unit; 51 integration; 0 skipped |
| `mvn -B -ntp -f backend/pom.xml -DskipTests package` | Đóng gói backend | BUILD SUCCESS | Spring Boot JAR |
| `docker build -t career-w4t2-pr17-review:local backend` | Kiểm tra Docker build | BUILD SUCCESS | Eclipse Temurin Java 17 |

---

## 6. Công việc tồn đọng & Bước tiếp theo (Outstanding Work)

* [x] Chạy full unit và PostgreSQL integration suite.
* [x] Chạy package và Docker build.
* [ ] Xác minh Git diff và CI trên commit được push.
