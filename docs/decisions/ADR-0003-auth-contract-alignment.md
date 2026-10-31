# ADR-0003: Đồng bộ auth API, schema và cấu hình JWT của W1-T5

* **Trạng thái (Status):** Proposed — đã hiện thực trong bản bàn giao W1-T5 để review; chưa ghi nhận phê duyệt chung của nhóm.
* **Người đề xuất / Tác giả:** Đoàn Việt Khang / Codex hỗ trợ
* **Ngày (Date):** 2026-10-29
* **Tài liệu liên quan:** [API](../api/api-contract.md), [database](../database/database-design.md), [hướng dẫn](../development/w1-t5-auth-guide.md)

## 1. Bối cảnh và Vấn đề

Excel giao W1-T5 cho Việt Khang; repo mới có skeleton. API yêu cầu phone nhưng V1
chưa có cột; tài liệu kiến trúc và API dùng hai cấu trúc lỗi khác nhau. Secret mẫu
đang có giá trị mặc định dùng chung.

## 2. Các phương án đã xem xét

- Bỏ phone hoặc nhận nhưng không lưu: thiếu hợp đồng API.
- Sửa V1: gây thay đổi checksum database đã chạy.
- Thêm V2 và giữ V1: đồng bộ API, giữ lịch sử migration; cần phối hợp người làm DB.
- Dùng secret mặc định: khởi động tiện nhưng token có thể bị giả mạo khi khóa công khai.

## 3. Quyết định và Căn cứ lựa chọn

Bản triển khai dùng V2 thêm phone và unique lower(email); giữ nguyên V1.
Giữ Java 17, Spring Boot 3.3.4, JJWT 0.12.6, Flyway đã chọn. Chỉ thêm starter-validation.
Dùng ErrorResponse theo API §1.2 (`errors`, `reason`); rejectedValue luôn null.
JWT dùng HS256 với khóa UTF-8 riêng, không có fallback, mặc định 24h. Kiểm tra DB
mỗi request để trạng thái active/role mới có hiệu lực. JSON trường lạ bị từ chối,
ngăn mass assignment. Dùng origin allowlist; mặc định có localhost:3000 và :5173.

## 4. Hệ quả và Đánh đổi

- Một truy vấn User mỗi request đã xác thực; đổi lại khóa/đổi quyền có hiệu lực ngay.
- V2 cần rà soát case-only duplicate email trước khi nâng DB đã có dữ liệu.
- Frontend phải gửi đúng DTO, không gửi field role/userId không thuộc hợp đồng.
- Chưa thêm refresh token/logout server, throttling hoặc quy trình cấp admin vì ngoài W1-T5.

## 5. Xác nhận & Theo dõi

Các lựa chọn trên nằm trong bản triển khai được yêu cầu; không tự nhận nhóm đã duyệt ADR.
W1-T3 tái sử dụng shared handler/security; W1-T4 tái sử dụng một User mapping này
và giữ thứ tự migration; W1-T2 tích hợp ba API auth theo hợp đồng.
