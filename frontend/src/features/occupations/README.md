# Danh mục và chi tiết nghề nghiệp

`occupationsRoutes.tsx` đăng ký hai route công khai `/occupations` và
`/occupations/:id` qua cơ chế tự nạp feature của router.

## Chạy thử W3-T5

Tại thư mục `frontend/`, tạo hoặc bổ sung dòng sau vào `.env.local`:

```dotenv
VITE_USE_OCCUPATION_MOCKS=true
```

Chạy `npm ci`, sau đó chạy `npm run dev` và mở `/occupations/101`.
Có thể mở danh mục `/occupations` và chọn một nghề để kiểm tra điều hướng.
Chọn Hà Nội hoặc TP. Hồ Chí Minh để so sánh số liệu minh họa theo quý.
Mở `/occupations/99999` hoặc `/occupations/abc` để kiểm tra trạng thái 404.

Đặt `VITE_USE_OCCUPATION_MOCKS=false` và khởi động lại Vite để dùng API.
Trình duyệt gọi `GET /api/v1/occupations/{id}` thông qua Axios client chung.
Cấu hình `VITE_API_BASE_URL` khi backend dùng địa chỉ khác mặc định
`http://localhost:8080/api/v1`. Khi API gặp lỗi, nút **Thử lại** tải lại
chi tiết nghề hiện tại. Chạy `npm run build` để kiểm tra TypeScript và bundle.

## Dữ liệu hiển thị

- Kỹ năng `ESSENTIAL` (hoặc `CORE`) thuộc nhóm bắt buộc. `OPTIONAL`
  thuộc nhóm bổ trợ. Thanh đo dùng trọng số `weight` theo thang 0–1.
- Biểu đồ cột dùng SVG/CSS theo ADR-0008. Điểm nhu cầu có trục 0–100.
  Số lượng tuyển dụng dùng cùng thang cho các vùng để so sánh được chiều cao cột.
- API hiện cung cấp `marketIndicators` của kỳ gần nhất, nên chế độ API hiển thị
  kỳ đó với số liệu có sẵn. Chuỗi theo quý và vùng nằm trong `mocks/`
  và chỉ xuất hiện khi bật chế độ minh họa.

## Giao diện Material Design 3

Giao diện danh mục và chi tiết dùng bảng màu xanh dương, nền xanh nhạt và thẻ trắng,
với màu nền giới hạn trong feature occupations. Nút chính và vòng focus dùng
cùng biến màu thương hiệu với header CareerRec.
Mục LMI có hai chế độ **Biểu đồ** và **Bảng số liệu**. Cả hai dùng cùng
dữ liệu và bộ lọc khu vực. Bảng số liệu giữ nguyên các giá trị 0 và dùng
dấu — cho ô chưa có dữ liệu.
