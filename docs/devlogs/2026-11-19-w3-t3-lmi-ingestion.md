# DevLog: Hiện thực hóa Dịch vụ Nạp & Quản trị Chỉ số Thị trường Lao động LMI (`W3-T3`)

* **Mã tác vụ / Task ID:** W3-T3 / W3-BE-LMI-01
* **Người thực hiện / Agent:** Huỳnh Long Bảo Khanh (hỗ trợ bởi Antigravity AI Assistant)
* **Ngày thực hiện (Date):** 2026-11-19
* **Nhánh Git (Branch):** `feat/w3-t3-lmi-ingestion`

---

## 1. Mục tiêu (Objective)

Hiện thực hóa endpoint quản trị `POST /api/v1/admin/lmi/import` cho phép tài khoản `ROLE_ADMIN` nạp theo lô (batch upsert) dữ liệu chỉ số thị trường lao động (`market_demand_indicators`) an toàn theo khóa tự nhiên `(occupation_id, region, snapshot_date)`, kiểm tra dữ liệu chặt chẽ và bảo vệ phân quyền RBAC.

---

## 2. Thay đổi thực tế (Changes)

* **Mã nguồn (Source Code):**
  * `backend/src/main/java/com/cnpm/careerrecommendation/module/lmi/dto/`: Tạo `LmiImportRequest` (giới hạn `1..500` bản ghi/lần), `LmiBatchItemDto` (`occupationId` và `openJobPostingsCount` dùng `BigDecimal` với `@Digits(fraction = 0)` để chặn số thập phân bị Jackson tự cắt), và `LmiImportResponse` theo chuẩn `camelCase`.
  * `backend/src/main/java/com/cnpm/careerrecommendation/module/lmi/service/LmiIngestionService.java`: Triển khai validation miền giá trị (`demandScore` trong `[0.00, 100.00]`, `growthRatePct` trong `[-999.99, 999.99]`, `openJobPostingsCount >= 0`, `competitiveIndex` trong `[0.00, 9.99]`, `snapshotDate` bắt buộc), chuẩn hóa `region` mặc định `NATIONAL` (uppercase cả khi insert lẫn upsert), tra cứu nghề nghiệp theo lô (`occupationId` hoặc `occupationCode`), upsert theo khóa tự nhiên `(occupation_id, region, snapshot_date)` (kể cả gộp trùng khóa nội bộ trong cùng lô) và ánh xạ xung đột đồng thời `uq_market_demand_occ_region_date` sang `409 Conflict`.
  * `backend/src/main/java/com/cnpm/careerrecommendation/module/lmi/controller/AdminLmiController.java`: Triển khai `POST /api/v1/admin/lmi/import` gắn `@PreAuthorize("hasRole('ADMIN')")`.
  * `backend/src/main/java/com/cnpm/careerrecommendation/repository/OccupationRepository.java`: Bổ sung `findByCodeIn`.
  * `backend/src/main/java/com/cnpm/careerrecommendation/repository/MarketDemandIndicatorRepository.java`: Bổ sung `findByOccupationIdInAndSnapshotDateIn`.
* **Kiểm thử (Tests):**
  * `backend/src/test/java/com/cnpm/careerrecommendation/module/lmi/LmiServiceTest.java` (8 unit tests): insert, upsert (gồm `growthRatePct` âm và chuẩn hóa `region`), gộp trùng trong cùng lô, từ chối lô rỗng/vượt 500 bản ghi, validation lỗi (bao gồm số thập phân và tràn `Long`), nghề không tồn tại/không khớp, xung đột đồng thời 409 và rethrow lỗi integrity khác.
  * `backend/src/test/java/com/cnpm/careerrecommendation/module/lmi/LmiControllerTest.java` (7 MockMvc slice tests): chặn 401 chưa đăng nhập, chặn 403 `ROLE_CANDIDATE`, 200 `ROLE_ADMIN`, validation 400 (`demandScore` ngoài miền, số thập phân ở `occupationId`/`openJobPostingsCount`, `items` rỗng / vượt 500 bản ghi, trường JSON lạ, `INVALID_OCCUPATION`).
  * `backend/src/test/java/com/cnpm/careerrecommendation/module/lmi/LmiPostgresIT.java` (5 PostgreSQL HTTP IT tests): kiểm tra RBAC 401/403 với JWT thật, insert & upsert idempotent trên PostgreSQL 16 (kể cả `growthRatePct` âm), upsert bản ghi seed từ Flyway V3 (`SOC-15-1252`), validation 400 qua HTTP thật (`demandScore` ngoài miền, số thập phân ở ID/postings, thiếu cả ID & code), và rollback nguyên tử cả lô khi có item lỗi.
* **Tài liệu chuẩn (Documentation Updates):**
  * Tạo `docs/api/lmi.md` đặc tả đầy đủ hợp đồng `POST /api/v1/admin/lmi/import`.
  * Cập nhật `docs/api/README.md` và `docs/devlogs/README.md`.

---

## 3. Khu vực bị ảnh hưởng (Affected Areas)

* `backend/src/main/java/com/cnpm/careerrecommendation/module/lmi/**`
* `backend/src/main/java/com/cnpm/careerrecommendation/repository/OccupationRepository.java`
* `backend/src/main/java/com/cnpm/careerrecommendation/repository/MarketDemandIndicatorRepository.java`
* `backend/src/test/java/com/cnpm/careerrecommendation/module/lmi/**`
* `docs/api/lmi.md`
* `docs/api/README.md`
* `docs/devlogs/2026-11-19-w3-t3-lmi-ingestion.md`
* `docs/devlogs/README.md`

---

## 4. Quyết định kỹ thuật quan trọng (Decisions & Rationale)

* **Giữ nguyên `MarketDemandIndicator` ở `entity/` và `MarketDemandIndicatorRepository` ở `repository/`:** Tránh phá vỡ phụ thuộc của `module/taxonomy` (`TaxonomyService`) và tạo thuận lợi cho `W3-T1` (Recommendation Engine).
* **Thống nhất hợp đồng `camelCase`, chặn số thập phân ở trường nguyên và hỗ trợ `occupationId` / `occupationCode`:** Giải quyết mâu thuẫn giữa Sheet `Task` W3-T3 và `docs/api/api-contract.md` §2.5; giữ wrapper `LmiImportRequest { items }` giới hạn tối đa `500` bản ghi để `@Valid` xác thực an toàn, bám sát các cột thực tế của bảng `market_demand_indicators` trong V1.
* **Giao dịch nguyên tử (`@Transactional`) + Fail-fast `400 Bad Request`:** Bảo đảm khi một bản ghi có `demandScore` sai hoặc nghề không tồn tại thì cả lô rollback sạch sẽ và trả HTTP 400 đúng tiêu chí nghiệm thu của Sheet `Task` W3-T3.
* **Batch Lookup tránh N+1:** Nạp trước `Occupation` và `MarketDemandIndicator` hiện có bằng truy vấn `IN` theo lô trước khi lưu qua `saveAllAndFlush`.

---

## 5. Kiểm tra & Xác thực (Validation Performed)

| Lệnh / Thao tác kiểm tra | Mục đích kiểm tra | Kết quả thực tế | Ghi chú |
|---|---|---|---|
| `mvn -B -ntp -f backend/pom.xml clean test` | Chạy toàn bộ unit & MockMvc slice tests backend (JDK 17.0.19) | **BUILD SUCCESS** (Tests run: 93, Failures: 0, Errors: 0, Skipped: 0) | Gồm 8 test `LmiServiceTest` + 7 test `LmiControllerTest` + 78 test hiện hữu |
| `mvn -B -ntp -f backend/pom.xml -Ppostgres-it test-compile failsafe:integration-test failsafe:verify` | Chạy integration tests với PostgreSQL 16 (`postgres:16-alpine` Docker container tạm thời) & Flyway V1–V3 | **BUILD SUCCESS** (Tests run: 41, Failures: 0, Errors: 0, Skipped: 0) | Gồm 21 `AuthPostgresIT` + 5 `LmiPostgresIT` + 15 `ProfilePostgresIT` |
| `mvn -B -ntp -f backend/pom.xml -DskipTests package` | Đóng gói Spring Boot JAR | **BUILD SUCCESS** | Chạy kiểm chứng sau cùng trước khi bàn giao |
| `git diff --check` | Kiểm tra whitespace/format | **Pass** (0 lỗi) | Working tree chỉ gồm các file phạm vi `W3-T3` trên nền `main` (`49c8b53`) |

---

## 6. Công việc tồn đọng & Bước tiếp theo (Outstanding Work)

* [ ] Đồng bộ mục `§2.5` của `docs/api/api-contract.md` (liên kết sang `docs/api/lmi.md`) với Tech Lead Bùi Vạn Khải khi merge PR.
* [ ] Gửi payload mẫu `POST /api/v1/admin/lmi/import` cho Phan Nguyễn Thông (`W3-T6`) để hoàn thiện Stage 2 E2E trong `scripts/test-week3-integration.ps1`.
* [ ] Phối hợp rà soát yêu cầu mở rộng `GET /api/v1/occupations/{id}` trả lịch sử LMI đa kỳ cho biểu đồ `W3-T5` ở giai đoạn tiếp theo.
