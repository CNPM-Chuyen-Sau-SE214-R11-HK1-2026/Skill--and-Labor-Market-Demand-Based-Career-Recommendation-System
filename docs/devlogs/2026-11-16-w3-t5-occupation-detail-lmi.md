# DevLog W3-T5 — Chi tiết nghề nghiệp và biểu đồ LMI

- **Mã tác vụ:** W3-T5
- **Phụ trách theo sheet Task:** Đoàn Việt Khang
- **Ngày thực hiện:** 2026-11-16
- **Nguồn làm việc:** Bản ZIP snapshot của nhánh `main`

## 1. Mục tiêu

Hoàn thiện trang công khai `/occupations/:id` để xem thông tin nghề,
kỹ năng yêu cầu và dữ liệu thị trường lao động.

## 2. Thay đổi thực tế

- Dùng route params và điều hướng React Router giữa danh mục và trang chi tiết.
- Hiển thị nhóm nghề, mô tả, khoảng lương và hai nhóm kỹ năng với thanh trọng số.
- Dùng SVG/CSS cho điểm nhu cầu và số lượng tuyển dụng. Bộ lọc vùng dùng dữ liệu
  minh họa theo quý trong chế độ mock.
- Dùng Axios client chung cho API chi tiết. Trang có skeleton, lỗi kèm nút
  thử lại và màn hình 404. Huỷ yêu cầu cũ khi rời trang hoặc đổi ID.
- Thay biểu đồ gauge cũ bằng biểu đồ cột. Ghi chú giới hạn dữ liệu ở tài liệu
  API và thêm hướng dẫn chạy thử trong feature.

## 3. Khu vực bị ảnh hưởng

- [Feature occupations](../../frontend/src/features/occupations/README.md)
- [Hợp đồng Taxonomy](../api/taxonomy.md#22-get-apiv1occupationsid)

## 4. Quyết định kỹ thuật

- Giữ điểm nhu cầu trên thang 0–100 và dùng cùng thang số lượng tuyển dụng
  khi đổi vùng. Giá trị thiếu hiển thị trạng thái chưa cập nhật.
- Route tự nạp từ feature và biểu đồ dùng SVG/CSS theo
  [ADR-0008](../adr/ADR-0008-frontend-architecture-and-state-management.md).

## 5. Kiểm tra và xác thực

| Thao tác | Kết quả |
|---|---|
| `npm ci` và `npm run build` tại `frontend/` | Thành công. TypeScript và Vite build pass. |
| Kiểm thử DOM qua route feature với dữ liệu minh họa | Pass điều hướng danh mục → chi tiết, nhóm kỹ năng và thanh trọng số, chiều cao cột SVG, đổi vùng, ID sai và ID không tồn tại. |
| Kiểm thử DOM với phản hồi API mô phỏng qua Axios adapter | Pass skeleton, HTTP 500, thử lại, HTTP 404, kỳ LMI đơn, giá trị 0 và dữ liệu LMI rỗng. |

## 6. Công việc tồn đọng

- API Taxonomy hiện chỉ trả kỳ gần nhất. Biểu đồ nhiều kỳ theo vùng với dữ
  liệu thật cần backend cung cấp lịch sử theo vùng.
- Chưa chạy với backend thật. Môi trường không khởi động được Chromium,
  nên bố cục responsive cần kiểm tra trực quan trên trình duyệt.
