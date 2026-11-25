# DevLog: W4-T2 Admin User Management & System Statistics

* **Mã tác vụ / Task ID:** W4-T2
* **Người thực hiện / Agent:** Codex
* **Ngày thực hiện (Date):** 2026-11-25
* **Nhánh Git (Branch):** `feature/w4-t2-admin-user-management`

---

## 1. Mục tiêu (Objective)

Triển khai API quản trị tài khoản và thống kê tổng quan hệ thống, bảo vệ bằng `ROLE_ADMIN`, không làm lộ dữ liệu xác thực và không thay đổi schema hoặc luồng JWT hiện có.

---

## 2. Thay đổi thực tế (Changes)

* **Mã nguồn:**
  * Thêm bốn endpoint quản trị: danh sách user có filter/phân trang, cập nhật trạng thái, cập nhật role và thống kê hệ thống.
  * Thêm DTO request/response riêng, service interface/implementation và JPQL query phân trang trong `UserRepository`.
  * Sử dụng `count()` của sáu repository hiện có; không tải toàn bộ entity để thống kê.
  * Chặn quản trị viên tự vô hiệu hóa và giữ thao tác cập nhật idempotent.
* **Kiểm thử:**
  * Thêm `AdminServiceTest` và `AdminUserControllerTest`, bao phủ filter, validation, idempotency, 401/403, token sai, self-deactivation và bảo vệ `passwordHash`.
  * Thêm `AdminPostgresIT` kiểm tra query thật với filter kết hợp, pagination và kết quả rỗng trên PostgreSQL.
* **Tài liệu:**
  * Cập nhật mục Admin API trong `docs/api/api-contract.md` với request, response và error code thực tế.

---

## 3. Khu vực bị ảnh hưởng (Affected Areas)

* `backend/src/main/java/com/cnpm/careerrecommendation/module/admin/**`
* `backend/src/main/java/com/cnpm/careerrecommendation/common/repository/UserRepository.java`
* `backend/src/test/java/com/cnpm/careerrecommendation/module/admin/**`
* `docs/api/api-contract.md`

Không chỉnh sửa hoặc stage các file local thuộc `module/profile/**` của W3.

---

## 4. Quyết định kỹ thuật quan trọng (Decisions & Rationale)

* Dùng một JPQL query với các filter tùy chọn và `Pageable` để tránh full-table loading và phân trang trong Java.
* Hỗ trợ cả `status=ACTIVE|INACTIVE` từ contract cũ và `active=true|false` từ đặc tả W4-T2; hai giá trị mâu thuẫn trả `400 INVALID_USER_STATUS`.
* Dùng `@PreAuthorize("hasRole('ADMIN')")` tại controller theo ADR-0006, đồng thời giữ nguyên `SecurityConfig`.
* Dùng DTO riêng nên `passwordHash` không thể xuất hiện trong response.
* Giữ sáu truy vấn `COUNT` đồng bộ, không thêm async/native SQL/index khi chưa có bằng chứng bottleneck. Email contains search dùng sequential scan nhưng chỉ đo khoảng `0.167 ms` với 5 user kiểm thử.

---

## 5. Kiểm tra & Xác thực (Validation Performed)

| Lệnh / Thao tác kiểm tra | Mục đích | Kết quả thực tế | Ghi chú |
|---|---|---|---|
| `mvn -B -ntp -f backend/pom.xml -DskipTests compile` | Compile production code | BUILD SUCCESS | 83 source files |
| `mvn -B -ntp -f backend/pom.xml "-Dtest=AdminServiceTest,AdminUserControllerTest" test` | Test W4-T2 | 26 passed, 0 failed | Service: 16; Controller: 10 |
| `mvn -B -ntp -f backend/pom.xml -Ppostgres-it "-Dit.test=AdminPostgresIT" test-compile failsafe:integration-test failsafe:verify` | Query Admin trên PostgreSQL thật | 3 passed, 0 failed | PostgreSQL 16.15; Flyway V1-V4 |
| PostgreSQL CI integration hiện hữu | Regression Auth và LMI | 26 passed, 0 failed | Auth: 21; LMI: 5 |
| Runtime smoke bốn Admin API trên database cô lập | Kiểm tra HTTP, RBAC và database state | Pass | Candidate 403; Admin thành công; self-deactivation 400; stats khớp SQL 6/6 |
| Chạy unit test được Git theo dõi trên `main` cộng test Admin | Regression không gồm file W3 local | 138 passed, 0 failed | Local CI-equivalent subset |
| `mvn -B -ntp -f backend/pom.xml clean test` trên dirty working tree | Full local regression | Fail do test W3 ngoài scope | `ProfileSkillServiceTest`: 12 failures, 1 error |
| `mvn -B -ntp -f backend/pom.xml -DskipTests package` | Đóng gói backend | BUILD SUCCESS | Tạo executable JAR |
| `docker build -t career-w4t2-review:java17 backend` | Xác minh toolchain Java 17 | BUILD SUCCESS | Image tạm đã xóa |
| `git diff --check` | Kiểm tra whitespace | Pass | Không có lỗi |

Database PostgreSQL và Docker image tạm dùng cho kiểm thử đã được xóa sau khi xác minh.

---

## 6. Công việc tồn đọng & Bước tiếp theo (Outstanding Work)

* [ ] Full `mvn clean test` trên dirty working tree vẫn bị chặn bởi test W3 local ngoài phạm vi W4-T2. Không đưa các file `module/profile/**` vào staged diff của W4.
* [ ] Chưa thực hiện commit, push hoặc merge.
