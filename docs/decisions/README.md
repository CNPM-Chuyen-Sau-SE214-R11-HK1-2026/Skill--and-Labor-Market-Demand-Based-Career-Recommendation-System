# Hồ sơ quyết định kiến trúc (Architectural Decision Records - ADR)

Thư mục này lưu trữ các bản ghi quyết định kiến trúc (ADR) ghi lại các quyết định kỹ thuật quan trọng của dự án, bối cảnh ra quyết định, các phương án đã xem xét và những đánh đổi kỹ thuật đi kèm.

---

## 1. Mẫu chuẩn bị quyết định kiến trúc (ADR Template)

Mọi quyết định kiến trúc mới bắt buộc phải tuân theo mẫu chuẩn tại:
* [**`ADR-TEMPLATE.md`**](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/decisions/ADR-TEMPLATE.md)

---

## 2. Quy tắc đặt tên và quản lý vòng đời ADR

### Quy ước đặt tên file:
```text
ADR-<SỐ_THỨ_TỰ>-<slug-ngan-gon>.md
```
*Ví dụ:* `ADR-0001-baseline-tech-stack.md`, `ADR-0002-database-migration-tool.md`.

### Vòng đời trạng thái (Status Lifecycle):
* **`Proposed` (Đề xuất):** Ý tưởng kỹ thuật hoặc khuyến nghị từ lập trình viên / AI agent, đang chờ đội ngũ dự án thảo luận và phê duyệt.
* **`Accepted` (Chấp thuận):** Quyết định đã được đội ngũ dự án thông qua và áp dụng chính thức vào mã nguồn.
* **`Superseded` (Bị thay thế):** Quyết định cũ không còn hiệu lực do một quyết định mới hơn (ghi rõ trích dẫn ADR mới thay thế).
* **`Rejected` (Từ chối):** Đề xuất bị từ chối sau khi phân tích đánh giá rủi ro.

> [!WARNING]
> AI agent không được tự ý chuyển một đề xuất (`Proposed`) thành quyết định đã được duyệt (`Accepted`) nếu người dùng chưa xác nhận.

---

## 3. Danh mục các quyết định kiến trúc đã ghi nhận

| Mã ADR | Tiêu đề quyết định | Trạng thái | Ngày quyết định |
|---|---|---|---|
| [**ADR-0001**](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/decisions/ADR-0001-baseline-tech-stack.md) | Lựa chọn ngăn xếp công nghệ nền tảng (React + Spring Boot + PostgreSQL + Docker) | **Accepted** | 2026-10-07 |
| [**ADR-0002**](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/decisions/ADR-0002-database-migration-tool.md) | Áp dụng Flyway quản lý Database Migration & Chuyển `ddl-auto` sang `validate` | **Accepted** | 2026-10-22 |

| [**ADR-0003**](ADR-0003-auth-contract-alignment.md) | Đồng bộ auth API/schema/JWT W1-T5 | **Proposed (đã triển khai để review)** | 2026-10-29 |
| [**ADR-0004**](../adr/ADR-0004-error-response-and-exception-consolidation.md) | Thống nhất định dạng ErrorResponse và Hợp nhất xử lý ngoại lệ tập trung | **Proposed** | 2026-11-02 |
| [**ADR-0005**](../adr/ADR-0005-entity-user-consolidation-and-modular-boundaries.md) | Hợp nhất Thực thể User và Quy tắc giao tiếp định danh giữa các Phân hệ | **Proposed** | 2026-11-02 |
| [**ADR-0006**](../adr/ADR-0006-role-based-access-control-and-security-strategy.md) | Chiến lược Phân quyền RBAC bằng Method Security và Khắc phục Bảo vệ Endpoint | **Proposed** | 2026-11-02 |
| [**ADR-0007**](../adr/ADR-0007-flyway-migration-numbering-and-seed-data.md) | Quy ước Đánh số Migration Flyway và Chiến lược Nạp Dữ liệu Mẫu (Seed Data) | **Proposed** | 2026-11-02 |
| [**ADR-0008**](../adr/ADR-0008-frontend-architecture-and-state-management.md) | Thiết kế Khung Kiến trúc Frontend, Bộ Định tuyến Phân tán và Quản lý Trạng thái Phiên | **Proposed** | 2026-11-02 |
| [**ADR-0009**](../adr/ADR-0009-docker-and-environment-variable-contract.md) | Chuẩn hóa Hợp đồng Biến Môi trường, Docker Compose và Pipeline CI Tự động | **Proposed** | 2026-11-02 |

> [!NOTE]
> Các quyết định kiến trúc từ **ADR-0004** trở đi được lưu trữ và quản lý tập trung tại thư mục [**docs/adr/**](../adr/README.md).
