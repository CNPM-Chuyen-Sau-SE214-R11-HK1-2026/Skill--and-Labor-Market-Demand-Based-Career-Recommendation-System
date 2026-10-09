# Đặc tả giao tiếp REST API (REST API Specification)

Thư mục này lưu trữ tài liệu chuẩn về các quy ước thiết kế, cấu trúc URL, định dạng dữ liệu truyền tải (Request/Response payload), mã lỗi chuẩn và danh mục các endpoint của hệ thống.

---

## 1. Trạng thái hiện tại của API (Current API Status)

> [!IMPORTANT]
> **Trạng thái: `TBD` (To Be Decided — Chưa triển khai endpoint nghiệp vụ).**  
> Tuân thủ nguyên tắc ban đầu: Hệ thống hiện tại chỉ là khung dự án ban đầu (initial skeleton), chưa tạo controller giả định hoặc endpoint nghiệp vụ giả mạo nào.  
> Các endpoint cụ thể sẽ được bổ sung vào tài liệu này khi các Use Case (UC01 - UC08) được hiện thực hóa.

---

## 2. Quy ước chuẩn thiết kế REST API (Design Conventions)

Khi bắt đầu hiện thực các endpoint, các lập trình viên và AI agent phải tuân thủ nghiêm ngặt các tiêu chuẩn sau:

### 2.1. Định dạng URL & Động từ HTTP
* Sử dụng danh từ số nhiều cho tài nguyên (`resources`), ví dụ: `/api/v1/users`, `/api/v1/skills`, `/api/v1/occupations`.
* Sử dụng chữ thường và phân cách bằng dấu gạch ngang (`kebab-case`) cho các cụm từ trong đường dẫn, ví dụ: `/api/v1/career-recommendations`.
* Sử dụng đúng ngữ nghĩa động từ HTTP:
  * `GET`: Lấy dữ liệu (idempotent, không thay đổi trạng thái hệ thống).
  * `POST`: Tạo mới tài nguyên hoặc kích hoạt tác vụ tính toán phức tạp.
  * `PUT`: Cập nhật toàn bộ tài nguyên.
  * `PATCH`: Cập nhật một phần tài nguyên.
  * `DELETE`: Xóa tài nguyên.

### 2.2. Tiêu chuẩn xác thực (Authentication Convention)
* Các endpoint yêu cầu xác thực sẽ sử dụng chuẩn JSON Web Token (JWT) trong HTTP Header:
  ```http
  Authorization: Bearer <jwt_token_string>
  ```
* Các endpoint công khai (ví dụ: đăng ký, đăng nhập, tra cứu xu hướng nghề nghiệp chung UC07) không yêu cầu token.

### 2.3. Cấu trúc phản hồi lỗi chuẩn hóa (Standard Error Response)
Mọi phản hồi lỗi từ Spring Boot Backend phải tuân theo định dạng JSON thống nhất (thông qua `@RestControllerAdvice`):

```json
{
  "timestamp": "2026-10-09T14:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Chi tiết thông điệp lỗi gửi cho client",
  "path": "/api/v1/recommendations"
}
```

### 2.4. Phân trang và sắp xếp (Pagination & Sorting)
Đối với các endpoint trả về danh sách (danh mục nghề nghiệp, tin tuyển dụng, khóa học), sử dụng tham số truy vấn:
* `page`: Số trang (0-indexed hoặc 1-indexed theo quy ước dự án).
* `size`: Số phần tử trên mỗi trang (mặc định: 10 hoặc 20).
* `sort`: Tiêu chí sắp xếp, ví dụ: `sort=demandScore,desc`.

---

## 3. Quy trình cập nhật tài liệu API

Mỗi khi tạo mới hoặc sửa đổi một endpoint trong tầng `controller/`:
1. Cập nhật phương thức, URL, headers, query params, request body mẫu vào tài liệu này hoặc file đặc tả tương ứng trong `docs/api/`.
2. Ghi rõ mã HTTP status trả về khi thành công (`200 OK`, `201 Created`, `204 No Content`) và các lỗi có thể xảy ra (`400`, `401`, `403`, `404`, `500`).
3. Ghi nhận thay đổi trong file DevLog của tác vụ đó.
