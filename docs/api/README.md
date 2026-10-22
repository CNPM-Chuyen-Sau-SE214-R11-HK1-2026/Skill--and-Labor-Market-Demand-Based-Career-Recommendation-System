# Đặc tả Giao tiếp REST API (REST API Hub)

Thư mục này lưu trữ hợp đồng giao tiếp REST API chính thức và các quy ước thiết kế API của dự án **Skill- and Labor Market Demand–Based Career Recommendation System**.

---

## 1. Tài liệu Hợp đồng API chuẩn (Canonical Contract)

* [**`api-contract.md`**](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/api/api-contract.md): Đặc tả chi tiết 100% các endpoint thuộc phạm vi MVP Tuần 2–5.
  * **Xác thực (`/api/v1/auth`):** `POST /register`, `POST /login`, `GET /me`.
  * **Hồ sơ ứng viên & Kỹ năng (`/api/v1/profile`):** `GET /profile`, `PUT /profile`, `GET /skills`, `POST /skills`, `PUT /skills/{id}`, `DELETE /skills/{id}`.
  * **Khám phá nghề nghiệp (`/api/v1/occupations`):** `GET /occupations` (tìm kiếm/lọc/phân trang), `GET /occupations/{id}`, `GET /skills` (từ điển taxonomy).
  * **Gợi ý nghề nghiệp & Khoảng cách kỹ năng (`/api/v1/recommendations`):** `GET /recommendations`, `GET /recommendations/{id}/skill-gap`.
  * **Quản trị hệ thống (`/api/v1/admin`):** `GET /users`, `POST /lmi/import`.

---

## 2. Tiêu chuẩn thiết kế RESTful thống nhất

### 2.1. Định dạng URL & Động từ HTTP
* **Danh từ số nhiều (`kebab-case`):** `/api/v1/occupations`, `/api/v1/recommendations`.
* **Quy ước động từ:**
  * `GET`: Truy vấn dữ liệu an toàn, không thay đổi trạng thái hệ thống.
  * `POST`: Tạo mới tài nguyên hoặc yêu cầu tính toán gợi ý phức tạp.
  * `PUT`: Cập nhật toàn bộ tài nguyên.
  * `DELETE`: Xóa tài nguyên (`204 No Content`).

### 2.2. Định dạng dữ liệu & Đặt tên
* **Request / Response Body:** Chuẩn JSON UTF-8.
* **Tên trường:** Chuẩn `camelCase` (ví dụ: `expectedSalaryMin`, `growthRatePct`).
* **Thời gian:** ISO-8601 UTC (`YYYY-MM-DDTHH:mm:ssZ`).
* **Tiền tệ:** Số nguyên / thập phân đơn vị `VND`.

### 2.3. Cơ chế xác thực
* Sử dụng chuẩn Bearer Token trong HTTP Header:
  ```http
  Authorization: Bearer <JWT_ACCESS_TOKEN>
  ```
* Các endpoint công khai (`/api/v1/auth/register`, `/api/v1/auth/login`, `/api/v1/occupations/**`) không yêu cầu token.
* Phân quyền theo vai trò (Role-Based Access Control):
  * `ROLE_CANDIDATE`: Quyền truy cập các phân hệ cá nhân và gợi ý.
  * `ROLE_ADMIN`: Quyền quản trị tài khoản, danh mục và nhập dữ liệu LMI.
