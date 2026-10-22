# DevLog: Giải quyết toàn diện các hạng mục tồn đọng Tuần 1 Tác vụ 1 (Resolve Week 1 Task 1)

* **Mã tác vụ / Task ID:** W1-TASK-01-RESOLUTION
* **Người thực hiện / Agent:** Antigravity AI Assistant
* **Ngày thực hiện (Date):** 2026-10-22
* **Nhánh Git (Branch):** `main` (không commit/push theo quy định)

---

## 1. Mục tiêu (Objective)

Tiếp nhận tài liệu đặc tả đồ án chính thức mới được bổ sung tại `ADDITIONAL_DOC/Báo cáo đồ án.docx` để hoàn thiện triệt để toàn bộ các hạng mục cốt lõi của **Tuần 1 — Tác vụ 1: Kiến trúc, Hợp đồng API & Điều phối dự án**:
1. Bảo vệ tài liệu `ADDITIONAL_DOC` không bị đưa vào Git tracking.
2. Hoàn thiện tài liệu kiến trúc hệ thống (`docs/architecture/system-architecture.md`) theo mô hình Modular Monolith và bổ sung 4 sơ đồ tuần tự Mermaid chi tiết.
3. Thiết lập hợp đồng REST API đầy đủ (`docs/api/api-contract.md`) cho các phân hệ người dùng, hồ sơ, kỹ năng, tra cứu, gợi ý và quản trị.
4. Hoàn thiện thiết kế cơ sở dữ liệu (`docs/database/database-design.md`) với 10 bảng thực thể, thiết lập công cụ Flyway Migration (`ADR-0002`), chuyển Hibernate sang chế độ `validate` và tạo baseline script `V1__initial_schema.sql`.
5. Xây dựng kế hoạch phát triển và Backlog Tuần 2–5 (`docs/planning/week-2-5-backlog.md`) phân công chi tiết cho 06 thành viên với tiêu chí nghiệm thu rõ ràng.
6. Thiết lập quy chuẩn phối hợp nhóm gồm quy trình Git (`docs/development/git-workflow.md`) và phong cách viết mã (`docs/development/coding-conventions.md`).

---

## 2. Thay đổi thực tế (Changes)

### Mã nguồn & Cấu hình (Source Code & Config):
* **`.gitignore`:** Bổ sung quy tắc root-anchored `/ADDITIONAL_DOC/` (dòng 116–119) để bảo vệ tài liệu nghiệp vụ gốc khỏi Git tracking.
* **`backend/pom.xml`:** Bổ sung 2 dependencies quản trị migration: `org.flywaydb:flyway-core` và `org.flywaydb:flyway-database-postgresql`.
* **`backend/src/main/resources/application.yml`:** Chuyển `spring.jpa.hibernate.ddl-auto` từ `update` sang `validate`; cấu hình `spring.flyway` (`enabled: true`, `baseline-on-migrate: true`, `locations: classpath:db/migration`).
* **`backend/src/main/resources/db/migration/V1__initial_schema.sql`:** Khởi tạo tập lệnh DDL hoàn chỉnh cho 10 bảng: `users`, `candidate_profiles`, `skill_categories`, `skills`, `user_skills`, `occupation_groups`, `occupations`, `occupation_skills`, `market_demand_indicators`, `recommendation_logs` cùng toàn bộ khóa chính, khóa ngoại, ràng buộc duy nhất và chỉ mục hiệu năng.

### Tài liệu chuẩn (Living Documentation Updates):
* **`docs/architecture/system-architecture.md`:** Viết lại toàn diện kiến trúc Modular Monolith, xác định ranh giới 5 module nội bộ, bổ sung 4 sơ đồ Mermaid tuần tự (`Authentication & Token Refresh`, `Candidate Profile & Skills Submission`, `Career Recommendation & Skill Gap Diagnosis`, `Admin Market Demand & Taxonomy Ingestion`), công thức toán học tính điểm đề xuất và ma trận phân tích giao tiếp.
* **`docs/api/api-contract.md`:** Khởi tạo đặc tả hợp đồng REST API chi tiết với định dạng JSON request/response đầy đủ, mã HTTP status, phân quyền bảo mật, kiểm tra hợp lệ đầu vào cho 10+ endpoints.
* **`docs/database/database-design.md`:** Cập nhật từ trạng thái TBD sang thiết kế chi tiết: Sơ đồ Mermaid ERD, Từ điển dữ liệu 10 bảng (Data Dictionary) và Chiến lược đánh chỉ mục.
* **`docs/decisions/ADR-0002-database-migration-tool.md`:** Khởi tạo hồ sơ quyết định kiến trúc số 0002 lựa chọn Flyway làm công cụ quản lý lược đồ CSDL và chuyển `ddl-auto` sang `validate`.
* **`docs/planning/week-2-5-backlog.md`:** Khởi tạo Backlog Tuần 2–5 chi tiết cho 06 thành viên (Bùi Vạn Khải, Phạm Ngọc Gia Khang, Lương Phúc Khang, Đoàn Việt Khang, Huỳnh Long Bảo Khanh, Phan Nguyễn Thông) kèm mã Task, tiêu chí nghiệm thu (DoD) và ma trận rủi ro.
* **`docs/development/git-workflow.md`:** Khởi tạo quy chuẩn phân nhánh Trunk-based, quy ước đặt tên nhánh, chuẩn Conventional Commits, mẫu PR checklist và quy định xét duyệt mã nguồn.
* **`docs/development/coding-conventions.md`:** Khởi tạo quy chuẩn lập trình Java 17/Spring Boot và TypeScript/React, cấu trúc thư mục, quy tắc kiểm tra kiểu strict, quản lý lỗi tập trung.
* **Cập nhật các Index:** `docs/README.md`, `docs/architecture/README.md`, `docs/api/README.md`, `docs/decisions/README.md`, `docs/planning/README.md`, `docs/development/README.md` và `README.md` ở thư mục gốc.

---

## 3. Khu vực bị ảnh hưởng (Affected Areas)

* `.gitignore`
* `README.md`
* `backend/pom.xml`
* `backend/src/main/resources/application.yml`
* `backend/src/main/resources/db/migration/V1__initial_schema.sql`
* `docs/README.md`
* `docs/architecture/README.md`
* `docs/architecture/system-architecture.md`
* `docs/api/README.md`
* `docs/api/api-contract.md`
* `docs/database/database-design.md`
* `docs/decisions/README.md`
* `docs/decisions/ADR-0002-database-migration-tool.md`
* `docs/planning/README.md`
* `docs/planning/week-2-5-backlog.md`
* `docs/development/README.md`
* `docs/development/git-workflow.md`
* `docs/development/coding-conventions.md`
* `docs/devlogs/2026-10-22-resolve-week-1-task-1.md`

---

## 4. Quyết định kỹ thuật quan trọng (Decisions & Rationale)

1. **Bảo vệ tài liệu tham khảo gốc (`/ADDITIONAL_DOC/`):** Tài liệu này chứa thông tin học phần nội bộ và tệp `.docx` nhị phân lớn; được bổ sung vào `.gitignore` bằng quy tắc root-anchored để tránh ô nhiễm Git commit history.
2. **Kiến trúc Modular Monolith:** Thay vì microservices phân tán quá phức tạp cho đồ án 5 tuần, hệ thống giữ nguyên mô hình một mã nguồn Spring Boot duy nhất nhưng chia rõ package theo domain độc lập để 6 thành viên không xung đột logic.
3. **Áp dụng Flyway & Khóa `ddl-auto: validate` ([`ADR-0002`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/decisions/ADR-0002-database-migration-tool.md)):** Ngăn ngừa việc Hibernate tự ý thay đổi cấu trúc bảng trên môi trường production/testing, đảm bảo tính tái lập 100% của database schema.
4. **Đánh giá và Quyết định về pgvector:** `ADDITIONAL_DOC` nêu `pgvector` phục vụ tìm kiếm việc làm ngữ nghĩa; tuy nhiên công thức cốt lõi MVP là đại số quan hệ kết hợp ($0.6 \cdot \text{SkillMatch} + 0.3 \cdot \text{DemandScore} + 0.1 \cdot \text{PrefMatch}$) và Dockerfile cơ sở là `postgres:16-alpine`. Do đó, `V1__initial_schema.sql` tập trung vào 10 bảng quan hệ chuẩn, hoãn việc thêm extension vector sang bản migration `V2` khi triển khai tính năng embedding nâng cao để tránh lỗi thiếu thư viện trên máy của các thành viên.
5. **Bảo mật quyền sở hữu tài nguyên (Candidate Ownership Guarantee):** Các thao tác hồ sơ (`/api/v1/profile/**`) và gợi ý (`/api/v1/recommendations/**`) xác định danh tính ứng viên tuyệt đối qua JWT `SecurityContextHolder`, không nhận `userId` ngoài luồng để ngăn chặn triệt để lỗ hổng truy cập chéo.
6. **Lọc bỏ nội dung rác từ template đồ án cũ:** Các đoạn văn bản sao chép sót lại trong template sinh viên (liên quan đến sàn thương mại điện tử, cổng thanh toán VNPAY, giải quyết tranh chấp giao dịch) đã được lọc bỏ hoàn toàn khỏi kiến trúc và thiết kế hệ thống gợi ý nghề nghiệp.

---

## 5. Kiểm tra & Xác thực (Validation Performed)

| Lệnh / Thao tác kiểm tra | Mục đích kiểm tra | Kết quả thực tế | Ghi chú |
| :--- | :--- | :--- | :--- |
| `cd frontend; npm.cmd run build` | Biên dịch TypeScript & Vite bundle | **Thành công** (`built in 4.97s`, 0 error) | Đạt chuẩn type-check và bundle |
| `mvn -f backend/pom.xml clean test` | Biên dịch Java và chạy unit test backend | **BUILD SUCCESS** (Tests run: 1, Failures: 0, Errors: 0) | Flyway dependency & schema SQL nạp hợp lệ |
| `docker compose config` | Kiểm tra tính hợp lệ cú pháp docker-compose | **Thành công** (exit code 0) | Cả 3 services `postgres`, `backend`, `frontend` khớp cấu hình |
| `git diff --check` | Kiểm tra lỗi khoảng trắng và định dạng | **Thành công** (0 trailing whitespace / format errors) | Mã nguồn và tài liệu sạch |
| `Kiểm tra toàn bộ liên kết Markdown` | Quét toàn bộ link `file:///` trong `docs/` & `README.md` | **Thành công** (100% link trỏ tới tệp tồn tại thực tế) | Không có broken links |
| `git check-ignore -v ADDITIONAL_DOC/` | Kiểm tra quy tắc loại trừ Git | **Chính xác** (Khớp dòng 119 `.gitignore:119:/ADDITIONAL_DOC/`) | Tệp `.docx` không bị track |
| `git ls-files ADDITIONAL_DOC` | Xác thực `ADDITIONAL_DOC` chưa từng bị track | **Chính xác** (0 file tracked) | Hoàn toàn an toàn |
| `docker compose up -d postgres && mvn spring-boot:run` | Kiểm thử thực thi Flyway migration trên PostgreSQL 16 thực tế | **Thành công** (Flyway migrated schema to version 1 trong 272ms; xác thực đủ 10 bảng, 10 FKs, 3 CHECKs, 36 indexes qua `psql`) | Runtime DB Migration Test |
| `git status --short` | Xác thực trạng thái Git working tree | **Chính xác** (Chỉ chứa file tài liệu & config, sẵn sàng commit) | Tuân thủ tuyệt đối quy định kiểm soát mã nguồn |

---

## 6. Công việc tồn đọng & Bước tiếp theo (Outstanding Work)

Các nhiệm vụ tiếp theo đã được lập kế hoạch chi tiết trong [`docs/planning/week-2-5-backlog.md`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/planning/week-2-5-backlog.md) để bắt đầu thực hiện trong **Tuần 2 (Sprint 2)**:
* [ ] Triển khai thực thể JPA (`@Entity`) tương ứng với 10 bảng trong `V1__initial_schema.sql` (Phạm Ngọc Gia Khang & Lương Phúc Khang).
* [ ] Hiện thực hóa `JwtTokenProvider`, `JwtAuthenticationFilter` và Spring Security 6 `SecurityFilterChain` (Bùi Vạn Khải).
* [ ] Thiết lập React Router và khung layout ứng viên / quản trị (Đoàn Việt Khang & Huỳnh Long Bảo Khanh).
* [ ] Kiểm thử nạp dữ liệu mẫu ban đầu qua Docker PostgreSQL (Phan Nguyễn Thông).
