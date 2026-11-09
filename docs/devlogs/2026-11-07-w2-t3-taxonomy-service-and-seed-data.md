# DevLog: Hiện thực hóa Dịch vụ Danh mục Nghề nghiệp, Kỹ năng & Nạp Seed Data V3

* **Mã tác vụ / Task ID:** W2-T3
* **Người thực hiện / Agent:** Huỳnh Long Bảo Khanh (hỗ trợ bởi Antigravity AI Assistant)
* **Ngày thực hiện (Date):** 2026-11-07
* **Nhánh Git (Branch):** `feat/w2-t3-taxonomy-service-seed-data`

---

## 1. Mục tiêu (Objective)

Thực hiện toàn diện **Tác vụ Tuần 2: W2-T3 (Hiện thực hóa Dịch vụ Danh mục Nghề nghiệp, Kỹ năng & Nạp Seed Data V3)**:
1. Soạn thảo kịch bản migration Flyway độc quyền `V3__seed_taxonomy_and_occupations.sql` tuân thủ [ADR-0007](file:///d:/Khanh/Công%20nghệ%20phần%20mềm%20chuyên%20sâu/Project/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/adr/ADR-0007-flyway-migration-numbering-and-seed-data.md) nhằm nạp dữ liệu mẫu ban đầu: 6 danh mục kỹ năng, 35 kỹ năng chuẩn hóa, 3 nhóm nghề SOC chính, 12 chức danh nghề nghiệp CNTT tiêu biểu, ma trận trọng số nghề–kỹ năng (`occupation_skills`) và chỉ số thị trường lao động (`market_demand_indicators`).
2. Hiện thực hóa tầng nghiệp vụ `TaxonomyService` và `TaxonomyController` tuân thủ 100% hợp đồng REST API tại [`docs/api/taxonomy.md`](file:///d:/Khanh/Công%20nghệ%20phần%20mềm%20chuyên%20sâu/Project/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/api/taxonomy.md) với quyền truy cập công khai (`permitAll()`):
   - `GET /api/v1/occupations`: Tìm kiếm từ khóa, lọc theo nhóm nghề và phân trang qua `PageResponse`.
   - `GET /api/v1/occupations/{id}`: Trả về chi tiết nghề, dải lương tham khảo, chỉ số thị trường LMI và danh sách kỹ năng cốt lõi/bổ trợ kèm trọng số.
   - `GET /api/v1/skills`: Tra cứu từ điển kỹ năng chuẩn hóa theo từ khóa (tối thiểu 2 ký tự) và lọc theo category.
3. Thiết lập bộ kiểm thử tự động toàn diện gồm 13 ca test (8 ca unit test dịch vụ và 5 ca slice test controller) bảo đảm chất lượng code đạt 100% pass.

---

## 2. Thay đổi thực tế (Changes)

* **Mã nguồn (Source Code):**
  * **Flyway Migration Seed Data:**
    * `backend/src/main/resources/db/migration/V3__seed_taxonomy_and_occupations.sql`: Chèn dữ liệu mẫu an toàn với cú pháp `ON CONFLICT DO NOTHING`, liên kết khóa ngoại bằng truy vấn `JOIN` trên bảng mã định danh.
  * **DTOs Phân hệ Taxonomy:**
    * `backend/src/main/java/com/cnpm/careerrecommendation/module/taxonomy/dto/OccupationSummaryResponse.java`: Record tóm tắt thông tin nghề kèm lương và điểm nhu cầu cho kết quả phân trang.
    * `backend/src/main/java/com/cnpm/careerrecommendation/module/taxonomy/dto/OccupationDetailResponse.java`: Record chi tiết nghề nghiệp chứa cấu trúc lồng `SalaryRangeDto`, `MarketIndicatorDto`, và `OccupationSkillRequirementDto`.
    * `backend/src/main/java/com/cnpm/careerrecommendation/module/taxonomy/dto/SkillDictionaryResponse.java`: Record từ điển kỹ năng chuẩn hóa (ESCO / O*NET).
  * **Tầng Truy xuất Dữ liệu (Repositories):**
    * `backend/src/main/java/com/cnpm/careerrecommendation/repository/OccupationRepository.java`: Bổ sung `@Query` phân trang `searchOccupations` hỗ trợ tìm kiếm linh hoạt theo từ khóa và mã nhóm nghề `groupId`.
    * `backend/src/main/java/com/cnpm/careerrecommendation/repository/OccupationSkillRepository.java`: Bổ sung `findByOccupationIdWithSkills` thực hiện eager fetch kỹ năng để tối ưu truy vấn (tránh N+1).
    * `backend/src/main/java/com/cnpm/careerrecommendation/repository/MarketDemandIndicatorRepository.java`: Bổ sung phương thức truy xuất bản ghi chỉ số thị trường mới nhất `findFirstByOccupationIdOrderBySnapshotDateDesc`.
    * `backend/src/main/java/com/cnpm/careerrecommendation/common/repository/SkillRepository.java`: Bổ sung `findByCategoryIdAndNameContainingIgnoreCase` phục vụ tìm kiếm kết hợp.
  * **Tầng Dịch vụ & Điều phối REST API:**
    * `backend/src/main/java/com/cnpm/careerrecommendation/module/taxonomy/service/TaxonomyService.java`: Logic nghiệp vụ tìm kiếm, phân trang, tính toán mức độ khan hiếm lao động (`shortageLevel`), xử lý ngoại lệ nghiệp vụ và định dạng dữ liệu đầu ra.
    * `backend/src/main/java/com/cnpm/careerrecommendation/module/taxonomy/controller/TaxonomyController.java`: REST controller công khai ánh xạ các đường dẫn `/api/v1/occupations` và `/api/v1/skills`.
  * **Kiểm thử Tự động (Tests):**
    * `backend/src/test/java/com/cnpm/careerrecommendation/module/taxonomy/TaxonomyServiceTest.java`: 8 ca kiểm thử đơn vị độc lập bao quát các kịch bản phân trang, lọc từ khóa, tìm chi tiết, ném lỗi 404, xử lý thiếu LMI mặc định và validate độ dài từ khóa.
    * `backend/src/test/java/com/cnpm/careerrecommendation/module/taxonomy/TaxonomyControllerTest.java`: 5 ca kiểm thử MockMvc xác thực mã trạng thái HTTP, tính công khai không cần Bearer token, cấu trúc JSON phản hồi và định dạng lỗi `ErrorResponse`.
* **Tài liệu chuẩn (Documentation Updates):**
  * Cập nhật `docs/devlogs/README.md` ghi nhận tác vụ W2-T3.

---

## 3. Khu vực bị ảnh hưởng (Affected Areas)

* `backend/src/main/resources/db/migration/V3__seed_taxonomy_and_occupations.sql`
* `backend/src/main/java/com/cnpm/careerrecommendation/module/taxonomy/`
* `backend/src/main/java/com/cnpm/careerrecommendation/repository/OccupationRepository.java`
* `backend/src/main/java/com/cnpm/careerrecommendation/repository/OccupationSkillRepository.java`
* `backend/src/main/java/com/cnpm/careerrecommendation/repository/MarketDemandIndicatorRepository.java`
* `backend/src/main/java/com/cnpm/careerrecommendation/common/repository/SkillRepository.java`
* `backend/src/test/java/com/cnpm/careerrecommendation/module/taxonomy/`
* `docs/devlogs/2026-11-07-w2-t3-taxonomy-service-and-seed-data.md`
* `docs/devlogs/README.md`

---

## 4. Quyết định kỹ thuật quan trọng (Decisions & Rationale)

1. **Tuân thủ Cơ chế Migration Idempotent (ADR-0007):** Toàn bộ câu lệnh chèn dữ liệu mẫu trong `V3` đều sử dụng mệnh đề `ON CONFLICT DO NOTHING` và giải quyết khóa ngoại thông qua mã định danh chuẩn (`code`) thay vì hardcode số ID tự tăng, đảm bảo script có thể chạy an toàn trên bất kỳ cơ sở dữ liệu rỗng hoặc đã có dữ liệu.
2. **Khắc phục N+1 Query trong Chi tiết Nghề nghiệp:** Sử dụng câu truy vấn `JOIN FETCH os.skill s LEFT JOIN FETCH s.category` trong `OccupationSkillRepository` để gom toàn bộ dữ liệu kỹ năng trong một câu lệnh SELECT duy nhất khi trả về chi tiết nghề.
3. **Phân loại Mức độ Khan hiếm Lao động (Shortage Level Calculation):** Thuật toán tự động chuyển đổi điểm nhu cầu thị trường `demandScore` thành các mức `HIGH` ($\ge 85.0$), `MEDIUM` ($\ge 70.0$) và `LOW` (< 70.0) phục vụ việc trực quan hóa trên giao diện Frontend của Task W2-T5.
4. **Validation Độ dài Từ khóa Tìm kiếm:** Bắt buộc từ khóa tìm kiếm kỹ năng có độ dài tối thiểu 2 ký tự (nếu người dùng có truyền tham số `query`) để tránh quét toàn bộ bảng và giảm tải cho cơ sở dữ liệu, ném lỗi chuẩn `ApiException(HttpStatus.BAD_REQUEST, "INVALID_QUERY")`.

---

## 5. Kiểm tra & Xác thực (Validation Performed)

| Lệnh / Thao tác kiểm tra | Mục đích kiểm tra | Kết quả thực tế | Ghi chú |
|---|---|---|---|
| `mvn -f backend/pom.xml compile` | Kiểm tra biên dịch Java 17 | **BUILD SUCCESS** (62 source files compiled) | Không có cảnh báo hoặc lỗi biên dịch |
| `mvn -f backend/pom.xml test -Dtest="TaxonomyServiceTest,TaxonomyControllerTest"` | Chạy kiểm thử tự động phân hệ Taxonomy | **BUILD SUCCESS** (Tests run: 13, Failures: 0, Errors: 0) | Vượt chỉ tiêu nghiệm thu (>= 8 ca test) |
| `mvn -f backend/pom.xml test -Dtest="!*PostgresIT"` | Chạy toàn bộ test suite backend (Unit & Slice tests) | **BUILD SUCCESS** (Tests run: 78, Failures: 0, Errors: 0) | Đảm bảo không phá vỡ bất kỳ module nào hiện có |

---

## 6. Công việc tồn đọng & Bước tiếp theo (Outstanding Work)

* [ ] Hỗ trợ Đoàn Việt Khang (W2-T5) tích hợp API thực tế vào màn hình `/occupations` và `/occupations/:id` thay thế dữ liệu mock.
* [ ] Kiểm thử tích hợp runtime `TaxonomyPostgresIT` khi môi trường Docker Compose PostgreSQL sẵn sàng trong buổi kiểm thử hệ thống Thứ Bảy 2026-11-07.

