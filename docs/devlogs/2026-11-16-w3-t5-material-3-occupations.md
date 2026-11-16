# DevLog W3-T5 — Giao diện nghề nghiệp theo Material Design 3

- **Mã tác vụ:** W3-T5
- **Phụ trách theo sheet Task:** Đoàn Việt Khang
- **Ngày thực hiện:** 2026-11-16
- **Baseline:** W3-T5 Occupation Detail & LMI

## 1. Mục tiêu

Đồng bộ giao diện danh mục và chi tiết nghề nghiệp với bộ UI Material Design 3
tham chiếu, đồng thời giữ các luồng tra cứu và hiển thị dữ liệu W3-T5.

## 2. Thay đổi thực tế

- Dùng bảng màu kem, sage và olive cho thẻ nghề, vùng thông tin lương,
  thanh tìm kiếm, bộ lọc khu vực và trạng thái tải hoặc lỗi.
- Thêm chuyển đổi giữa biểu đồ SVG và bảng số liệu LMI trên cùng tập dữ liệu.
- Chặn cập nhật kết quả hoặc lỗi khi yêu cầu danh mục đã bị huỷ.
- Trả focus về ô tìm kiếm sau khi xóa truy vấn.

## 3. Khu vực bị ảnh hưởng

- [Feature occupations và hướng dẫn chạy thử](../../frontend/src/features/occupations/README.md)

## 4. Quyết định kỹ thuật

- Dùng CSS thuần và CSS Modules theo ADR-0008. Các biến màu và trạng thái
  tương tác của giao diện mới nằm trong lớp bọc `.occ-theme`.
- Bảng số liệu có tiêu đề cột và hàng để dùng với trình đọc màn hình. Chuyển vùng
  cập nhật cùng giá trị trong cả hai cách hiển thị.

## 5. Kiểm tra và xác thực

| Thao tác | Kết quả |
|---|---|
| `npm ci`, `npm run build` | Cài thành công và build pass với TypeScript và Vite. |
| Render DOM với dữ liệu minh họa | Pass tìm kiếm, xóa và trả focus, mở chi tiết, nhóm và meter kỹ năng, đổi vùng ở bảng, chuyển biểu đồ và đối chiếu chiều cao cột, quay lại và 404. |
| Render DOM với Axios adapter | Pass loading, HTTP 500, thử lại, HTTP 404, LMI rỗng, giá trị 0 và ô thiếu dữ liệu. |

## 6. Công việc tồn đọng

- Cần duyệt giao diện trên trình duyệt thật ở kích thước máy tính và điện thoại.
  Chromium không khởi động được trong môi trường thực thi.
- API hiện trả LMI của kỳ mới nhất. Chuỗi nhiều kỳ theo vùng dùng dữ liệu
  minh họa cho đến khi backend cung cấp lịch sử.
