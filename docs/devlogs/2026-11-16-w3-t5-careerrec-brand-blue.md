# W3-T5 — Đồng bộ màu thương hiệu CareerRec

## 1. Mục tiêu
Đồng bộ bảng màu danh mục và chi tiết nghề nghiệp với xanh dương của header.

## 2. Thay đổi thực tế
Chuyển nền, thẻ, trạng thái tương tác, thanh trọng số và biểu đồ LMI sang
bảng màu xanh dương. Bảng màu lỗi tiếp tục dùng sắc đỏ nhạt.

## 3. Khu vực bị ảnh hưởng
[Feature occupations](../../frontend/src/features/occupations/README.md).

## 4. Quyết định kỹ thuật
Dùng token `--color-primary` và `--color-primary-hover` của CareerRec
trong CSS của feature để nút chính đồng bộ với header.

## 5. Kiểm tra và xác thực
Patch được áp dụng kiểm chứng trên baseline Material Design 3 màu olive.
`npm run build` thành công.

## 6. Công việc tồn đọng
Cần duyệt màu trên trình duyệt của người dùng.
