# Danh mục và chi tiết nghề nghiệp

`occupationsRoutes.tsx` đăng ký hai route công khai `/occupations` và
`/occupations/:id` qua cơ chế tự nạp feature của router.

## Chạy W4-T5 với API thật

Chạy backend và PostgreSQL theo hướng dẫn repository. Tại `frontend/`, chạy
`npm ci`, `npm run dev`, mở `/occupations` và chọn một nghề để kiểm tra chi tiết.
Cấu hình `VITE_API_BASE_URL` khi backend dùng địa chỉ khác mặc định
`http://localhost:8080/api/v1`. Hai route luôn gọi API thật, kể cả khi mock auth
được bật. Fixture cũ không được import vào service; không fallback khi API lỗi.
Mở `/occupations/99999` hoặc `/occupations/abc` để kiểm tra 404; dừng backend
để kiểm tra error/retry. Chạy `npm run build` để kiểm tra TypeScript và bundle.

## Dữ liệu hiển thị

- Kỹ năng `ESSENTIAL` (hoặc `CORE`) thuộc nhóm bắt buộc. `OPTIONAL`
  thuộc nhóm bổ trợ. Thanh đo dùng trọng số `weight` theo thang 0–1.
- Biểu đồ cột dùng SVG/CSS theo ADR-0008. Điểm nhu cầu có trục 0–100.
  Số lượng tuyển dụng dùng cùng thang cho các vùng để so sánh được chiều cao cột.
- API hiện cung cấp `marketIndicators` của kỳ gần nhất, nên chế độ API hiển thị
  kỳ đó với số liệu có sẵn. Fixture lịch sử trong `mocks/` không được dùng lúc chạy.

## Giao diện Material Design 3

Giao diện danh mục và chi tiết dùng bảng màu xanh dương, nền xanh nhạt và thẻ trắng,
với màu nền giới hạn trong feature occupations. Nút chính và vòng focus dùng
cùng biến màu thương hiệu với header CareerRec.
Mục LMI có hai chế độ **Biểu đồ** và **Bảng số liệu**. Cả hai dùng cùng
dữ liệu và bộ lọc khu vực. Bảng số liệu giữ nguyên các giá trị 0 và dùng
dấu — cho ô chưa có dữ liệu.
