# W2-T5 — Occupation Explorer UI

## Phạm vi
- Trang danh sách `/occupations` và chi tiết `/occupations/:id`.
- Components dùng chung: `Card`, `Badge`, `SearchBar`, `PaginationControl`, `Spinner`.
- Biểu đồ LMI dùng SVG/CSS thuần, không thêm thư viện chart.
- Route module export default tại `features/occupations/occupationsRoutes.tsx`, public theo ADR-0008 (`layout: 'candidate', roles: []`).
- API service đọc `VITE_API_BASE_URL`; mặc định `http://localhost:8080/api/v1`.
- Mock data chỉ bật khi chủ động đặt `VITE_USE_OCCUPATION_MOCKS=true`; mặc định gọi API thật.

## Tích hợp với W2-T4
Bản source đầu vào hiện tại vẫn là Vite skeleton W1, chưa có `src/routes/index.tsx`, `src/api/client.ts`, AuthContext hoặc React Router scaffold. Vì các file đó thuộc quyền sở hữu W2-T4, module này không tự tạo bản thay thế để tránh ghi đè/xung đột khi ghép PR.

Sau khi scaffold W2-T4 được merge:
1. Đảm bảo `react-router-dom` v6 đã có trong dependencies.
2. Router cha cần glob `../features/*/*Routes.tsx` và gom `default` exports như ADR-0008.
3. Đảm bảo router cha không áp dụng `ProtectedRoute` cho routes có `handle.roles: []`.
4. API client Axios chuẩn của W2-T4 nên được dùng thống nhất khi nhóm chốt interface client. Hiện tại module đóng gói toàn bộ GET transport trong `occupationService.ts` để không phụ thuộc vào file scaffold chưa có; không có `fetch` rải rác trong component.
5. Cấu hình `.env` của frontend:
   `VITE_API_BASE_URL=http://localhost:8080/api/v1`
   Tùy chọn kiểm thử UI bằng mock:
   `VITE_USE_OCCUPATION_MOCKS=true`

## Hợp đồng API sử dụng
- `GET /occupations?query=&page=0&size=9&sort=title,asc`
- `GET /occupations/{id}`
- DTO theo `docs/api/taxonomy.md`. Không tự sinh giá trị thay thế khi API trả trường LMI rỗng.

## Kiểm chứng
Không thay đổi `package.json` hoặc `package-lock.json`, không thêm dependency mới. Source ZIP đầu vào chưa chứa scaffold W2-T4 và môi trường thực thi hiện tại không có npm cache đầy đủ; do đó cần chạy `npm ci && npm run build` sau khi tích hợp scaffold trong môi trường dự án có registry/cache npm.
