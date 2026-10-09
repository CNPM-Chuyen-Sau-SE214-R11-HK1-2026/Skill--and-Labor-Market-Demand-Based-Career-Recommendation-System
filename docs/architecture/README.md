# Kiến trúc hệ thống (System Architecture)

Thư mục này lưu trữ tài liệu chuẩn mô tả kiến trúc tổng thể, ranh giới các module, tương tác giữa các tầng công nghệ và quyết định phân rã dịch vụ của hệ thống.

---

## Danh mục tài liệu chuẩn

* [**`system-architecture.md`**](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/architecture/system-architecture.md): Tài liệu đặc tả kiến trúc hệ thống hiện tại, mô tả các thành phần đã xác lập và danh sách các phần đang trong quá trình phân tích (TBD).

---

## Hướng dẫn cập nhật tài liệu kiến trúc

Mọi thay đổi liên quan đến:
1. Thêm mới hoặc tái cấu trúc ranh giới module trong Frontend hoặc Backend.
2. Bổ sung dịch vụ hỗ trợ (ví dụ: Cache Redis, Message Queue, AI Ingestion Service).
3. Thay đổi luồng dữ liệu hoặc mô hình bảo mật (Spring Security / JWT filter chain).

$\rightarrow$ Đều bắt buộc phải cập nhật [`system-architecture.md`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/architecture/system-architecture.md) và ghi nhận lý do vào một bản ghi quyết định kiến trúc tại [`docs/decisions/`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/decisions/).
