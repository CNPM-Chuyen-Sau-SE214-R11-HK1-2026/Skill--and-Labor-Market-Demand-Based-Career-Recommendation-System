# Đặc tả Giao tiếp REST API (REST API Hub)

Thư mục này lưu trữ hợp đồng giao tiếp REST API chính thức và các quy ước thiết kế API của dự án **Skill- and Labor Market Demand–Based Career Recommendation System**.

---

## 1. Danh mục Tài liệu Hợp đồng API chuẩn theo Phân hệ

Nhằm đảm bảo nguyên tắc **Single Ownership** và tránh xung đột Git giữa các thành viên, hợp đồng API được phân tách thành 3 tài liệu chuyên biệt:

* [**`api-contract.md`**](./api-contract.md) *(Chủ sở hữu: **Bùi Vạn Khải - W2-T1**)*:
  * Quy ước toàn cục: Cấu trúc phân trang `PageResponse`, cấu trúc lỗi chuẩn `ErrorResponse`.
  * **Kiểm tra trạng thái hệ thống (`/api/health`):** `GET /api/health` (Công khai, phản hồi liveness tức thời không phụ thuộc CSDL).
  * **Xác thực & Tài khoản (`/api/v1/auth`):** `POST /register`, `POST /login`, `GET /me`.
  * Chỉ mục tổng thể và hợp đồng định hướng cho Gợi ý (`/recommendations` - W3+) và Quản trị (`/admin` - W4+).

* [**`profile.md`**](./profile.md) *(Chủ sở hữu: **Phạm Ngọc Gia Khang - W2-T2**)*:
  * **Hồ sơ ứng viên & Kỹ năng (`/api/v1/profile`):**
    * `GET /profile` (trả về HTTP 404 `PROFILE_NOT_FOUND` nếu chưa tạo, không tự tạo ngầm).
    * `PUT /profile` (tạo mới/cập nhật nguyện vọng và học vấn).
    * `GET /profile/skills`, `POST /profile/skills`, `PUT /profile/skills/{id}`, `DELETE /profile/skills/{id}`.
    * Cơ chế trích xuất `userId` độc quyền từ `AuthenticatedUser`, bảo mật qua `@PreAuthorize("hasRole('CANDIDATE')")`.

* [**`taxonomy.md`**](./taxonomy.md) *(Chủ sở hữu: **Huỳnh Long Bảo Khanh - W2-T3**)*:
  * **Khám phá nghề nghiệp & Danh mục Taxonomy (`/api/v1/occupations`, `/api/v1/skills`):**
    * `GET /occupations` (tìm kiếm, lọc nhóm ngành, phân trang `PageResponse`).
    * `GET /occupations/{id}` (chi tiết nghề nghiệp, kỹ năng trọng số, chỉ số LMI).
    * `GET /skills` (từ điển kỹ năng chuẩn hóa O*NET / ESCO).
    * Cơ chế truy cập công khai hoàn toàn (`permitAll()`).

---

## 2. Tiêu chuẩn thiết kế RESTful thống nhất

### 2.1. Định dạng URL & Động từ HTTP
* **Đường dẫn cơ sở (Base URL):**
  * Tất cả các endpoint nghiệp vụ đều mang tiền tố chuẩn hóa `/api/v1` (cấu hình frontend qua `VITE_API_BASE_URL=http://localhost:8080/api/v1`).
  * Riêng endpoint kiểm tra trạng thái dịch vụ (Health Check) nằm trực tiếp tại `/api/health`.
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
* Các endpoint công khai (`/api/v1/auth/register`, `/api/v1/auth/login`, `/api/v1/occupations/**`, `/api/v1/skills/**`) không yêu cầu token.
* Phân quyền theo vai trò (Role-Based Access Control):
  * `ROLE_CANDIDATE`: Quyền truy cập các phân hệ cá nhân và gợi ý.
  * `ROLE_ADMIN`: Quyền quản trị tài khoản, danh mục và nhập dữ liệu LMI.
