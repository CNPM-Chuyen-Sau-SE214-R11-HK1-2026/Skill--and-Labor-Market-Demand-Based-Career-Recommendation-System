# Thiết kế cơ sở dữ liệu (Database Design)

Thư mục này lưu trữ tài liệu chuẩn về kiến trúc cơ sở dữ liệu, sơ đồ thực thể liên kết (ERD), quy ước đặt tên, định nghĩa bảng và chiến lược migration của hệ thống.

---

## Danh mục tài liệu chuẩn

* [**`database-design.md`**](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/database/database-design.md): Tài liệu đặc tả thiết kế CSDL, các miền dữ liệu chính và trạng thái schema hiện tại.

---

## Quy ước bắt buộc khi thay đổi CSDL

Mọi thay đổi liên quan đến cấu trúc dữ liệu:
1. Tạo bảng mới, thêm cột, chỉnh sửa khóa ngoại, chỉ mục (Index).
2. Viết file migration (Flyway/Liquibase).
3. Định nghĩa hoặc thay đổi các thực thể JPA (`@Entity`).

$\rightarrow$ Đều bắt buộc phải cập nhật [`database-design.md`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/database/database-design.md) trước khi kết thúc tác vụ, kèm theo DevLog tương ứng.
