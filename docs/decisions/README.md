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
