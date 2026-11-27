# DevLog: Hiện thực hóa API Quản trị Danh mục Nghề nghiệp, Kỹ năng & Ánh xạ (`W4-T3`)

* **Mã tác vụ / Task ID:** W4-T3
* **Người thực hiện / Agent:** Huỳnh Long Bảo Khanh (hỗ trợ bởi Antigravity AI Assistant)
* **Ngày thực hiện (Date):** 2026-11-26
* **Nhánh Git (Branch):** `feat/w4-t3-admin-taxonomy-api`

---

## 1. Mục tiêu (Objective)

Triển khai đầy đủ các REST API quản trị CRUD cho nghề nghiệp (`/api/v1/admin/occupations`), từ điển kỹ năng (`/api/v1/admin/skills`), và ánh xạ kỹ năng–nghề nghiệp kèm trọng số/tính bắt buộc (`/api/v1/admin/occupations/{id}/skills`), được bảo vệ nghiêm ngặt bởi `ROLE_ADMIN`, kiểm tra ràng buộc dữ liệu (`400`, `404`, `409`) và phản ánh tức thì sang `GET /api/v1/occupations`, `GET /api/v1/skills` và `GET /api/v1/recommendations`.

---

## 2. Thay đổi thực tế (Changes)

* **Mã nguồn (Source Code):**
  * `backend/src/main/java/com/cnpm/careerrecommendation/module/taxonomy/dto/`: Tạo 9 DTO chuyên biệt (`AdminOccupationRequest`, `AdminOccupationResponse`, `AdminSkillRequest`, `AdminSkillResponse`, `OccupationSkillRequest`, `UpdateOccupationSkillRequest`, `OccupationSkillMappingResponse`, `OccupationGroupResponse`, `SkillCategoryResponse`). Các trường ID số nguyên (`groupId`, `categoryId`, `skillId`) dùng `BigDecimal` với `@Digits(fraction = 0)` để từ chối số thập phân thay vì để Jackson tự cắt.
  * `backend/src/main/java/com/cnpm/careerrecommendation/module/taxonomy/service/AdminTaxonomyService.java`: Triển khai CRUD nghề nghiệp (`createOccupation`, `updateOccupation`, `deleteOccupation`), CRUD kỹ năng (`createSkill`, `updateSkill`, `deleteSkill`), quản lý ánh xạ kỹ năng (`listOccupationSkills`, `assignOccupationSkill` upsert an toàn, `updateOccupationSkill`, `removeOccupationSkill`) và tra cứu danh mục nhóm/loại (`listOccupationGroups`, `listSkillCategories`). Kiểm tra trùng `code` -> `409` (`OCCUPATION_CODE_ALREADY_EXISTS`, `SKILL_CODE_ALREADY_EXISTS`, cả pre-check lẫn `ConstraintViolationException`), `entrySalaryAvg <= midSalaryAvg`, `importanceWeight` trong `[0.00, 1.00]`, `skillType` thuộc `{TECHNICAL, SOFT, DOMAIN}`, và thiếu ID -> `404` (`RESOURCE_NOT_FOUND`).
  * `backend/src/main/java/com/cnpm/careerrecommendation/module/taxonomy/controller/AdminTaxonomyController.java`: Triển khai controller `/api/v1/admin` gắn `@PreAuthorize("hasRole('ADMIN')")`, kiểm tra ID đường dẫn dương và bắt `MethodArgumentTypeMismatchException` / `MissingServletRequestParameterException` trả `400 INVALID_PARAMETER`.
* **Kiểm thử (Tests):**
  * `backend/src/test/java/com/cnpm/careerrecommendation/module/taxonomy/AdminTaxonomyServiceTest.java` (`15` unit tests): kiểm tra tạo/sửa/xóa occupation & skill, giữ nguyên `code` khi cập nhật, `409` trùng mã và xung đột đồng thời DB, `400` khoảng lương & `skillType` & trọng số ngoài `[0.00, 1.00]`, `404` thiếu ID/nhóm/danh mục, upsert & gỡ ánh xạ, sắp xếp nhóm/danh mục.
  * `backend/src/test/java/com/cnpm/careerrecommendation/module/taxonomy/AdminTaxonomyControllerTest.java` (`7` MockMvc slice tests): kiểm tra RBAC (`401` chưa đăng nhập, `403` `ROLE_CANDIDATE`, `200`/`201`/`204` `ROLE_ADMIN`), validation body (`400` khi weight `1.25`, `skillId` thập phân, unknown field), `404`, `409`, tham số đường dẫn/query sai (`abc`, `0`, thiếu `?skillId=`).
  * `backend/src/test/java/com/cnpm/careerrecommendation/module/taxonomy/AdminTaxonomyPostgresIT.java` (`2` PostgreSQL HTTP IT tests): kiểm tra RBAC với JWT thật và toàn bộ vòng đời CRUD + ánh xạ trên PostgreSQL 16 + Flyway V1–V4; xác minh thay đổi phản ánh ngay trên `GET /api/v1/skills`, `GET /api/v1/occupations`, `GET /api/v1/occupations/{id}`, `GET /api/v1/recommendations` và `/skill-gap`, cùng `ON DELETE CASCADE` sạch sẽ trên `occupation_skills`, `market_demand_indicators`, `recommendation_logs`, `user_skills`.
* **Tài liệu chuẩn (Documentation Updates):**
  * Cập nhật `docs/api/taxonomy.md` (bổ sung Mục 3 đặc tả toàn bộ API quản trị `W4-T3`), `docs/api/api-contract.md` (bổ sung tóm tắt API `W4-T3` trong Mục 2.5), và `docs/api/README.md`.
  * Tạo DevLog này và cập nhật `docs/devlogs/README.md`.

---

## 3. Khu vực bị ảnh hưởng (Affected Areas)

* `backend/src/main/java/com/cnpm/careerrecommendation/module/taxonomy/controller/AdminTaxonomyController.java`
* `backend/src/main/java/com/cnpm/careerrecommendation/module/taxonomy/service/AdminTaxonomyService.java`
* `backend/src/main/java/com/cnpm/careerrecommendation/module/taxonomy/dto/**` (9 DTO files mới)
* `backend/src/test/java/com/cnpm/careerrecommendation/module/taxonomy/AdminTaxonomyServiceTest.java`
* `backend/src/test/java/com/cnpm/careerrecommendation/module/taxonomy/AdminTaxonomyControllerTest.java`
* `backend/src/test/java/com/cnpm/careerrecommendation/module/taxonomy/AdminTaxonomyPostgresIT.java`
* `docs/api/taxonomy.md`
* `docs/api/api-contract.md`
* `docs/api/README.md`
* `docs/devlogs/2026-11-26-w4-t3-admin-taxonomy-crud.md`
* `docs/devlogs/README.md`

---

## 4. Quyết định kỹ thuật quan trọng (Decisions & Rationale)

* **Tách `AdminTaxonomyController` và `AdminTaxonomyService` trong `module.taxonomy`:** Tuân thủ quyền sở hữu `module.taxonomy/` trên Sheet `Task` và `ADR-0005`, hoàn toàn không sửa `common/**`, `entity/**`, `repository/**`, `SecurityConfig`, migration `V1`–`V4` hay `module/admin` (tránh xung đột với PR #17 `W4-T2`, PR #18 `W4-T6` và PR #19 `W4-T1`).
* **Bảo mật 2 lớp theo `ADR-0006`:** Kết hợp rule `/api/v1/admin/**` ở `SecurityConfig` với `@PreAuthorize("hasRole('ADMIN')")` trên `AdminTaxonomyController`.
* **Upsert trên `POST /api/v1/admin/occupations/{id}/skills` kèm `PUT` tường minh:** Vì đặc tả Sheet `W4-T3` chỉ nêu `POST/DELETE` cho ánh xạ kỹ năng, cho phép `POST` tự tạo mới (`201 Created`) hoặc cập nhật trọng số/`isRequired` khi đã tồn tại (`200 OK`), đồng thời vẫn cung cấp `PUT .../skills/{skillId}` và hỗ trợ `DELETE` theo cả path `{skillId}` lẫn query `?skillId=`.
* **Tận dụng `ON DELETE CASCADE` ở schema PostgreSQL V1:** Xác minh qua `AdminTaxonomyPostgresIT` rằng xóa nghề nghiệp tự động dọn sạch `occupation_skills`, `market_demand_indicators` và `recommendation_logs`, còn xóa kỹ năng dọn sạch `occupation_skills` và `user_skills`.

---

## 5. Kiểm tra & Xác thực (Validation Performed)

| Lệnh / Thao tác kiểm tra | Mục đích kiểm tra | Kết quả thực tế | Ghi chú |
|---|---|---|---|
| `mvn -B -ntp -f backend/pom.xml -Ppostgres-it test-compile failsafe:integration-test failsafe:verify` | Chạy integration tests trên PostgreSQL 16.15 (`career_w4t3_it`, Flyway V1–V4) | **BUILD SUCCESS** (Tests run: 43, Failures: 0, Errors: 0, Skipped: 0) | Gồm 21 `AuthPostgresIT` + 5 `LmiPostgresIT` + 15 `ProfilePostgresIT` + 2 `AdminTaxonomyPostgresIT`; đã drop DB tạm sau khi chạy |
| `mvn -B -ntp -f backend/pom.xml clean package` | Chạy toàn bộ unit/controller tests và đóng gói JAR (JDK 17.0.19) | **BUILD SUCCESS** (Tests run: 135, Failures: 0, Errors: 0, Skipped: 0) | Gồm 15 `AdminTaxonomyServiceTest` + 7 `AdminTaxonomyControllerTest` + 113 test hiện hữu |
| `git diff --check` | Kiểm tra whitespace/format | **Pass** (0 lỗi) | Chỉ thay đổi các file trong phạm vi `W4-T3` trên nền `main` (`fc037c3`) |

---

## 6. Công việc tồn đọng & Bước tiếp theo (Outstanding Work)

* [ ] Đồng bộ nhẹ cuối file `docs/api/api-contract.md` nếu PR #19 (`W4-T1`) được merge vào `main` trước PR `W4-T3`.
* [ ] Mở PR cho nhánh `feat/w4-t3-admin-taxonomy-api` sau khi bạn xác nhận commit/push, để `W4-T5` (Admin Portal `/admin/taxonomy`) và `W4-T6` (`scripts/test-week4-e2e.py`) tích hợp tiếp.
