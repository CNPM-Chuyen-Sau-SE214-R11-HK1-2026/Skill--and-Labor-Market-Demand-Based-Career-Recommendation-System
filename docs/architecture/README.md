# Kiến trúc hệ thống (System Architecture Hub)

Thư mục này lưu trữ tài liệu chuẩn mô tả kiến trúc tổng thể, mô hình phân tầng Modular Monolith, ranh giới các module, sơ đồ luồng tuần tự và thuật toán gợi ý của dự án **Skill- and Labor Market Demand–Based Career Recommendation System**.

---

## 1. Tài liệu kiến trúc chuẩn (Canonical Document)

* [**`system-architecture.md`**](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/architecture/system-architecture.md): Đặc tả kiến trúc hệ thống chính thức của dự án.
  * **Phạm vi MVP:** Các phân hệ trong phạm vi Tuần 2–5.
  * **Phong cách kiến trúc:** Modular Monolith (Spring Boot 3.3.4 + React 18 SPA + PostgreSQL 16).
  * **Phân định ranh giới & module:** `auth`, `user`, `taxonomy`, `lmi`, `recommendation`, `admin`.
  * **Sơ đồ luồng tuần tự:**
    * Luồng 1: Đăng ký, Đăng nhập & Xác thực Bearer JWT.
    * Luồng 2: Khai báo hồ sơ & Quản lý kỹ năng ứng viên.
    * Luồng 3: Gợi ý nghề nghiệp & Chẩn đoán khoảng cách kỹ năng ($\Delta$).
    * Luồng 4: Quản trị danh mục & Nhập dữ liệu nhu cầu thị trường.
  * **Công thức toán học thuật toán gợi ý:** $\text{FinalScore} = 0.6 \cdot \text{SkillMatch} + 0.3 \cdot \text{DemandScore} + 0.1 \cdot \text{PrefMatch}$.
  * **Chuẩn xử lý lỗi và HTTP status codes.**

---

## 2. Quy tắc cập nhật kiến trúc

Mọi thành viên và AI agent khi thay đổi ranh giới module hoặc bổ sung dịch vụ bắt buộc phải:
1. Thảo luận và thống nhất quyết định kiến trúc thông qua bản ghi ADR tại [`docs/decisions/`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/decisions/).
2. Cập nhật trực tiếp vào file [`system-architecture.md`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/architecture/system-architecture.md).
3. Ghi nhận nhật ký DevLog tương ứng theo chính sách [`AI_RULES.md`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/AI_RULES.md).
