# Hồ sơ Quyết định Kiến trúc Dự án (Architectural Decision Records - ADR)

Thư mục này lưu trữ toàn bộ các bản ghi quyết định kiến trúc (ADR) chính thức của dự án **Skill- and Labor Market Demand–Based Career Recommendation System**. Mỗi ADR ghi lại bối cảnh kỹ thuật, các giải pháp đã xem xét, quyết định được lựa chọn và các đánh đổi đi kèm.

---

## 1. Danh mục các Quyết định Kiến trúc (ADR Index)

| Mã ADR | Tiêu đề Quyết định Kiến trúc | Trạng thái | Ngày ban hành | Tác vụ liên quan |
| :---: | :--- | :---: | :---: | :---: |
| [**ADR-0001**](../decisions/ADR-0001-baseline-tech-stack.md) | Lựa chọn ngăn xếp công nghệ nền tảng (React + Spring Boot + PostgreSQL + Docker) | **Accepted** | 2026-10-07 | W1-T1 |
| [**ADR-0002**](../decisions/ADR-0002-database-migration-tool.md) | Áp dụng Flyway quản lý Database Migration & Chuyển `ddl-auto` sang `validate` | **Accepted** | 2026-10-22 | W1-T1, W1-T4 |
| [**ADR-0003**](../decisions/ADR-0003-auth-contract-alignment.md) | Đồng bộ auth API, schema và cấu hình JWT của W1-T5 | **Proposed** | 2026-10-29 | W1-T5 |
| [**ADR-0004**](./ADR-0004-error-response-and-exception-consolidation.md) | Thống nhất định dạng ErrorResponse và Hợp nhất xử lý ngoại lệ tập trung | **Proposed** | 2026-11-02 | W2-T1 |
| [**ADR-0005**](./ADR-0005-entity-user-consolidation-and-modular-boundaries.md) | Hợp nhất Thực thể User và Quy tắc giao tiếp định danh giữa các Phân hệ | **Proposed** | 2026-11-02 | W2-T1, W2-T2, W2-T3 |
| [**ADR-0006**](./ADR-0006-role-based-access-control-and-security-strategy.md) | Chiến lược Phân quyền RBAC bằng Method Security và Khắc phục Bảo vệ Endpoint | **Proposed** | 2026-11-02 | W2-T1, W2-T2, W2-T4 |
| [**ADR-0007**](./ADR-0007-flyway-migration-numbering-and-seed-data.md) | Quy ước Đánh số Migration Flyway và Chiến lược Nạp Dữ liệu Mẫu (Seed Data) | **Proposed** | 2026-11-02 | W2-T3, W2-T6 |
| [**ADR-0008**](./ADR-0008-frontend-architecture-and-state-management.md) | Thiết kế Khung Kiến trúc Frontend, Bộ Định tuyến Phân tán và Quản lý Trạng thái Phiên | **Proposed** | 2026-11-02 | W2-T4, W2-T5 |
| [**ADR-0009**](./ADR-0009-docker-and-environment-variable-contract.md) | Chuẩn hóa Hợp đồng Biến Môi trường, Docker Compose và Pipeline CI Tự động | **Proposed** | 2026-11-02 | W2-T6 |

---

## 2. Vòng đời Trạng thái ADR (ADR Status Lifecycle)

* **`Proposed` (Đề xuất):** Quyết định kỹ thuật được kiến trúc sư / thành viên đề xuất cho sprint tới, đang chờ đội ngũ thông qua khi bắt đầu sprint.
* **`Accepted` (Chấp thuận):** Quyết định đã được đội ngũ dự án thống nhất áp dụng chính thức vào mã nguồn.
* **`Superseded` (Bị thay thế):** Quyết định cũ không còn hiệu lực do có quyết định mới hơn thay thế.
* **`Rejected` (Từ chối):** Đề xuất bị từ chối sau khi phân tích đánh giá rủi ro.

---

## 3. Liên kết Tài liệu Kỹ thuật Liên quan

* [Báo cáo thẩm định kỹ thuật Tuần 1](../audit/week1-audit.md)
* [Kế hoạch chi tiết Tác vụ Tuần 2](../plan/week2-tasks.md)
* [Hợp đồng REST API](../api/api-contract.md)
* [Thiết kế Cơ sở Dữ liệu](../database/database-design.md)
* [Quy chuẩn Lập trình Dự án](../development/coding-conventions.md)
