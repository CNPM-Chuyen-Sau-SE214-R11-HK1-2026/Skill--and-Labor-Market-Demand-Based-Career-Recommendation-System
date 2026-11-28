# Báo Cáo Điều Phối Tích Hợp, Kiểm Soát Hợp Đồng API & Review PR Tuần 4 (`W4-T1`)

* **Người thực hiện:** Bùi Vạn Khải (Tech Lead / Gatekeeper Tuần 4)
* **Nhánh thực hiện:** `feat/w4-t1-recommendation-history` (tạo từ `origin/main` commit `fc037c3`)
* **Ngày thực hiện:** 2026-11-26
* **Phạm vi GitHub PRs:** Chỉ thẩm định và lập báo cáo cục bộ (chưa thực hiện merge, comment, hoặc push lên remote theo xác nhận workflow).

---

## 1. Hiện Thực Hóa API Lịch Sử Gợi Ý (`GET /api/v1/recommendations/history`)

### 1.1. Thành phần đã triển khai
* **Repository:** `backend/src/main/java/com/cnpm/careerrecommendation/repository/RecommendationLogRepository.java`
  * Bổ sung `findHistoryByUserId(Long userId, Pageable pageable)` dùng JPQL `JOIN FETCH rl.occupation o LEFT JOIN FETCH o.group` kèm `countQuery` tách biệt để tương thích hoàn toàn với `spring.jpa.open-in-view: false` và tránh N+1 query.
* **DTO:** `backend/src/main/java/com/cnpm/careerrecommendation/module/recommendation/dto/RecommendationHistoryItemResponse.java`
  * Trả về đầy đủ thông tin tóm tắt (`id`, `occupationId`, `occupationCode`, `occupationTitle`, `groupName`, `matchScore`, `skillGapCount`, `recommendationDate`, `createdAt`) và `inputSnapshot` (`JsonNode` parse an toàn từ cột JSONB `input_snapshot_json`).
  * Quy đổi `OffsetDateTime` -> `Instant` để xuất chuẩn ISO-8601 UTC (`YYYY-MM-DDTHH:mm:ssZ`). Không trả `User` entity hay thông tin nhạy cảm.
* **Service:** `RecommendationService` & `RecommendationServiceImpl`
  * Sắp xếp mặc định mới nhất trước: `Sort.by(Sort.Order.desc("recommendationDate"), Sort.Order.desc("id"))`.
  * Validate phân trang (`page >= 0`, `1 <= size <= 100`) -> trả `400 INVALID_PAGINATION` khi vi phạm.
  * Guard `userId == null` -> trả `401 UNAUTHORIZED`.
  * Parse `inputSnapshotJson` an toàn: trả `null` nếu `null`, rỗng hoặc JSON lỗi cấu trúc (không làm sập API).
* **Controller:** `RecommendationController`
  * Endpoint `GET /api/v1/recommendations/history` (`page` mặc định `0`, `size` mặc định `20`), kế thừa `@PreAuthorize("hasRole('CANDIDATE')")` cấp lớp và rule `/api/v1/recommendations/**` -> `hasRole("CANDIDATE")` tại `SecurityConfig`.
  * **Chống IDOR:** Chỉ trích xuất `userId` từ `@AuthenticationPrincipal AuthenticatedUser` qua `requireUserId(currentUser)`; không khai báo hoặc sử dụng `userId` từ query/path/body.
  * Bổ sung `@ExceptionHandler(MethodArgumentTypeMismatchException.class)` trả `400 INVALID_PARAMETER` chuẩn `ErrorResponse` khi truyền `page`, `size`, `limit`, hoặc `occupationId` sai kiểu số.

---

## 2. Kiểm Soát Hợp Đồng API Backend ↔ Frontend ↔ Docs

### 2.1. Tổng hợp đối chiếu theo phân hệ

| Phân hệ / Endpoint | Backend thực tế | Frontend / Docs hiện hành | Sai lệch phát hiện & Xử lý trong W4-T1 |
| :--- | :--- | :--- | :--- |
| **Global Conventions** (`PageResponse`, `ErrorResponse`, `GAP-01`) | `PageResponse(content, pageNumber, pageSize, totalElements, totalPages, last)`; `ErrorResponse(timestamp, status, error, message, path, errors)`; `fail-on-unknown-properties: true` | `frontend/src/types/api.ts`, `frontend/src/api/client.ts` (đặt sẵn `/api/v1` baseURL, gắn Bearer JWT, xử lý 401 an toàn) | **Khớp 100%.** Không có lỗi lặp `/api/v1`. |
| **Auth** (`/api/v1/auth/*`) | `AuthController`, `LoginResponse`, `RegisteredUserResponse`, `CurrentUserResponse` | `realAuthService.ts`, `types/auth.ts`, `docs/api/api-contract.md#21` | **Khớp 100%.** JWT claims (`sub`, `userId`, `role`) và status codes (`201`, `200`, `400`, `401`, `409`) đồng nhất. |
| **Profile & Skills** (`/api/v1/profile/*`) | `ProfileController`, `ProfileResponse`, `SkillResponse`, `CreatedSkillResponse` | `profileService.ts`, `profile.types.ts`, `docs/api/profile.md` | **Khớp cấu trúc payload & status code.** Ghi nhận: `main` chưa có `SKILL_NOT_FOUND`/`SKILL_ALREADY_EXISTS` do PR #11 (W3-T2) chưa merge; `profileService.ts` hiện chỉ kiểm tra `PROFILE_NOT_FOUND` (404) và hiển thị `data.message` nên UI không bị ảnh hưởng. |
| **Taxonomy** (`/api/v1/occupations/*`, `/api/v1/skills`) | `TaxonomyController`, `OccupationSummaryResponse`, `OccupationDetailResponse`, `SkillDictionaryResponse` | `occupationService.ts`, `occupation.types.ts`, `profileService.ts`, `docs/api/taxonomy.md` | **Khớp 100%.** Lưu ý trang `/occupations/:id` sử dụng `marketIndicators` đơn kỳ mới nhất (không trả chuỗi thời gian; `marketTrend` chỉ dùng trong mock). |
| **Recommendations** (`/api/v1/recommendations/*`) | `RecommendationController`, `RecommendationResponse`, `SkillGapResponse`, `RecommendationHistoryItemResponse` | `docs/api/api-contract.md#24` (bản Tuần 1 cũ chưa cập nhật) | **Đã cập nhật `docs/api/api-contract.md#24`:** Chuẩn hóa `scoreBreakdown.formula` (`"0.6 * SkillMatch + 0.3 * DemandScore + 0.1 * PrefMatch"`), `marketGrowth` (`"+12.4%"` / `"N/A"`), `requiredImportance` (`HIGH` / `MEDIUM` / `LOW`), `importanceWeight`, `priority` (`CRITICAL` / `HIGH` / `MEDIUM` / `LOW`), các mã lỗi (`NO_SKILLS_DECLARED`, `INVALID_LIMIT`, `INVALID_PAGINATION`, `INVALID_PARAMETER`) và bổ sung đặc tả đầy đủ cho `GET /api/v1/recommendations/history`. |
| **Admin LMI** (`/api/v1/admin/lmi/import`) | `AdminLmiController`, `LmiImportRequest` (`{"items": [LmiBatchItemDto]}`), `LmiImportResponse` | `docs/api/lmi.md` (chuẩn) vs `docs/api/api-contract.md#25` (vẫn ghi mảng JSON thô và tên trường cũ từ Tuần 1) | **Đã cập nhật `docs/api/api-contract.md#25`:** Đồng bộ request body thành `{ "items": [...] }` theo `LmiImportRequest` và liên kết tới `docs/api/lmi.md` để tránh lỗi `400 Bad Request` do `fail-on-unknown-properties: true` khi W4-T5 tích hợp. |

### 2.2. Lưu ý phối hợp tích hợp cho các task Frontend & QA Tuần 4
* **Phối hợp W4-T4 (Recommendation & Skill Gap UI):**
  * Gọi `GET /recommendations?limit=10` (mảng trực tiếp) -> xử lý riêng `400 NO_SKILLS_DECLARED` để điều hướng/nhắc ứng viên bổ sung kỹ năng tại `/profile`.
  * Gọi `GET /recommendations/{occupationId}/skill-gap` -> dùng đúng giá trị enum `requiredImportance` (`HIGH` | `MEDIUM` | `LOW`) và `priority` (`CRITICAL` | `HIGH` | `MEDIUM` | `LOW`), kết hợp `importanceWeight`.
  * Gọi `GET /recommendations/history?page=0&size=20` -> nhận `PageResponse<RecommendationHistoryItemResponse>`; xử lý nullable cho `groupName` và `inputSnapshot`.
* **Phối hợp W4-T5 (Admin Portal UI):**
  * Endpoint `POST /admin/lmi/import` bắt buộc gửi payload dạng `{ "items": [ ... ] }` với đúng các khóa của `LmiBatchItemDto` (`occupationId` hoặc `occupationCode`, `region`, `demandScore`, `growthRatePct`, `openJobPostingsCount`, `competitiveIndex`, `snapshotDate`).
  * Các endpoint `GET /admin/users`, `PATCH /admin/users/{id}/status`, `PATCH /admin/users/{id}/role`, `GET /admin/stats` phụ thuộc PR #17 (`feature/w4-t2-admin-user-management`) được chỉnh sửa finding PostgreSQL và merge vào `main`.
* **Phối hợp W4-T6 (E2E & CI):**
  * Sau khi merge W4-T1 và PR #18 (`ops/w4-t6-demo-e2e-ci`), cần bổ sung bước kiểm thử E2E cho `GET /api/v1/recommendations/history` vào `scripts/test-week4-e2e.py` (kiểm tra HTTP 200, phần tử đầu tiên khớp `occupationId` vừa sinh gợi ý, thứ tự mới nhất trước, và cô lập IDOR khi truyền `?userId=...`).

---

## 3. Gatekeeper Review Các PR Mở (Tính đến 2026-11-26)

### 3.1. PR #17 — `feature/w4-t2-admin-user-management` (W4-T2, tác giả: `ja-gnahk`)
* **Trạng thái khuyến nghị:** **Yêu cầu chỉnh sửa trước khi merge (`Changes Requested`)**
* **Điểm đạt chuẩn:**
  * Bảo vệ phân quyền chặt chẽ với `@PreAuthorize("hasRole('ADMIN')")` tại `AdminUserController`, giữ nguyên `SecurityConfig`, không thay đổi Flyway migrations hay JWT.
  * DTO `AdminUserResponse` cô lập hoàn toàn, không trả `passwordHash`.
  * Chặn quản trị viên tự vô hiệu hóa tài khoản (`400 SELF_DEACTIVATION_NOT_ALLOWED`), thao tác cập nhật trạng thái/vai trò idempotent, thống kê `GET /api/v1/admin/stats` dùng truy vấn `COUNT` trực tiếp từ 6 repository.
  * Đã có 26 unit/controller tests (`AdminServiceTest`: 16, `AdminUserControllerTest`: 10) và `AdminPostgresIT` (3 tests); 3/3 CI checks xanh.
* **Vấn đề chặn merge (Blocking):**
  1. **Nguy cơ lỗi `lower(bytea)` trên PostgreSQL khi `email = null` trong `UserRepository.searchAdminUsers`:**
     * Truy vấn hiện viết:
       `WHERE (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))`
     * Trên PostgreSQL + Hibernate 6, tham số `:email` khi truyền `null` (trường hợp phổ biến khi Admin mở danh sách user mà không nhập ô tìm kiếm email) không có ép kiểu tường minh dễ phát sinh lỗi `ERROR: function lower(bytea) does not exist` — lỗi tương tự đã từng xảy ra và được vá tại `OccupationRepository.searchOccupations` (commit `30fa3eb` dùng `CAST(:keyword AS String)`).
     * Đối chiếu `AdminPostgresIT`: cả 3 test case hiện tại đều truyền `email != null` (`prefix.toUpperCase()`, `prefix`, `"missing-..."`), chưa kiểm chứng kịch bản `email = null` trên PostgreSQL thật.
     * **Khắc phục yêu cầu:** Đổi `:email` sang `CAST(:email AS String)` trong `UserRepository.searchAdminUsers` và bổ sung test case `email = null` vào `AdminPostgresIT`.
* **Góp ý không chặn merge (Non-blocking):**
  * Bổ sung `@ExceptionHandler(MethodArgumentTypeMismatchException.class)` tại `AdminUserController` (tương tự `ProfileController` và `RecommendationController`) để tham số sai kiểu như `?page=abc` hoặc `?active=invalid` trả `400` thay vì rơi xuống `GlobalExceptionHandler` trả `500`.
* **Ghi chú xung đột hợp đồng:** Trong `W4-T1`, tệp `docs/api/api-contract.md` cố ý giữ nguyên vùng dòng `GET /api/v1/admin/users` để khi PR #17 được chỉnh sửa và merge sẽ không phát sinh merge conflict.

### 3.2. PR #18 — `ops/w4-t6-demo-e2e-ci` (W4-T6, tác giả: `Thongphan76`)
* **Trạng thái khuyến nghị:** **Sẵn sàng merge (`Ready to Merge`)**
* **Đánh giá kỹ thuật:**
  * `V5__seed_demo_accounts.sql` gắn điều kiện an toàn `current_setting('w4.demo_accounts_enabled', TRUE) = 'on'` (mặc định `off` trong `docker-compose.yml`), dùng domain `.invalid`, hash mật khẩu bằng BCrypt cost 12, resolve ID động và idempotent (`ON CONFLICT ... DO NOTHING`).
  * `scripts/test-week4-e2e.py` kiểm tra 28 checks qua HTTP thật và dọn dẹp dữ liệu LMI tạm trong khối `finally`.
  * Cả 3 job CI trên PR #18 đều `SUCCESS` (`Backend - Unit Tests and PostgreSQL IT`, `Frontend - TypeScript and Vite Build`, `Week 3 + Week 4 - Docker API Integration`).
* **Việc cần làm sau khi merge:** Cập nhật `scripts/test-week4-e2e.py` để bổ sung kiểm tra `GET /api/v1/recommendations/history` (W4-T1) và các API Admin sau khi PR #17 / W4-T3 merge.

### 3.3. PR #11 — `feature/w3-t2-profile-skill-management` (W3-T2 tồn đọng, tác giả: `ja-gnahk`)
* **Trạng thái khuyến nghị:** **Tiếp tục giữ (`Changes Requested` từ 2026-11-16)**
* **Lý do:** Chưa có commit mới nào kể từ review lần trước; commit `10b6542` vẫn thiếu các file test đã nêu trong mô tả PR (`ProfileSkillServiceTest.java` và `ProfileSkillControllerIT.java`).

---

## 4. Kết Quả Kiểm Chứng Cục Bộ (`feat/w4-t1-recommendation-history`)

| Hạng mục kiểm tra | Lệnh thực thi | Kết quả quan sát |
| :--- | :--- | :--- |
| **Backend Unit & Slice Tests** | `mvn -B -ntp -f backend/pom.xml clean test` | **125 passed, 0 failed, 0 errors, 0 skipped** (`BUILD SUCCESS`) — gồm 11 `RecommendationControllerTest`, 9 `RecommendationEngineTest`, 12 `RecommendationServiceImplTest` |
| **PostgreSQL Integration Tests (DB cô lập `career_w4t1_it`)** | `mvn -B -ntp -f backend/pom.xml -Ppostgres-it test-compile failsafe:integration-test failsafe:verify` | **44 passed, 0 failed, 0 errors, 0 skipped** (`BUILD SUCCESS`) — `AuthPostgresIT`: 21, `LmiPostgresIT`: 5, `ProfilePostgresIT`: 15, `RecommendationHistoryPostgresIT`: 3 (đã drop DB tạm sau khi chạy) |
| **Frontend Build** | `npm --prefix frontend ci && npm --prefix frontend run build` | **`tsc -b` + `vite build` thành công** (`176 modules transformed`, `built in 6.54s`) |
| **Git Whitespace Check** | `git diff --check` | **Pass** (0 lỗi whitespace) |
