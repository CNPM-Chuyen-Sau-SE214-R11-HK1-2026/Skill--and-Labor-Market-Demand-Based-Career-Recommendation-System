# W4-T5 — Admin Portal và nghiệm thu

Phạm vi: giao diện quản trị và tích hợp danh mục nghề nghiệp. Không thay đổi backend, migration, router chung hay dependency. Các route được tự nạp theo [ADR-0008](../adr/ADR-0008-frontend-architecture-and-state-management.md); `ProtectedRoute` ở nhánh `/admin` và metadata `ROLE_ADMIN` bảo vệ UI. Backend vẫn là nguồn quyết định quyền (Spring Security `/api/v1/admin/**`).

## Chạy và chế độ dữ liệu

- Cấu hình backend/PostgreSQL theo [Development Guide](README.md). Dùng tài khoản admin đã được tạo bằng cơ chế dự án, không đăng ký công khai với quyền admin.
- Tại `frontend/`: `npm ci`, `node scripts/test-w4-t5.cjs`, `npm run build`, `npm run dev`.
- `VITE_API_BASE_URL` phải kết thúc bằng `/api/v1`. Mặc định `http://localhost:8080/api/v1`. `VITE_ENABLE_MOCK_API=false` cho đăng nhập thật.
- `/admin/lmi` và `/occupations[/id]` luôn dùng API thật. LMI nhận JSON dán hoặc tệp JSON; server là nơi kiểm tra nghề tồn tại, khóa tự nhiên và transaction. Mẫu chỉ để minh họa trên DB kiểm thử; không nạp dữ liệu giả vào DB thật.
- `VITE_ADMIN_DEMO=true` chỉ có hiệu lực trong Vite dev server, chỉ với Users/Stats và Taxonomy. Dữ liệu demo nằm trong bộ nhớ, mất khi chuyển trang hoặc tải lại. Không có fallback khi server lỗi. Để demo UI độc lập có thể dùng mock auth có sẵn (`VITE_ENABLE_MOCK_API=true`, email chứa `admin`), nhưng **không** dùng mock auth để nghiệm thu API LMI.

## Trạng thái hợp đồng API

| Endpoint | Trạng thái trong ZIP gốc | Adapter W4-T5 |
|---|---|---|
| `POST /admin/lmi/import` | Có, [LMI](../api/lmi.md) | `{items: [...]}`; `totalRecordsProcessed`, `recordsInserted`, `recordsUpdated`, `timestamp` |
| `GET /occupations`, `/occupations/{id}`, `/skills` | Có, [Taxonomy](../api/taxonomy.md) | Đọc thật, phân trang nghề và lọc kỹ năng |
| `GET /admin/users` | Chưa có controller | Dự kiến `query`, `role`, `active`, `page`, `size`; PageResponse có `id`, `email`, `fullName`, `role`, `active` |
| `PATCH /admin/users/{id}/status` | Chưa có controller | Dự kiến `{active: boolean}`; chặn tự vô hiệu hóa tại UI, backend phải chặn độc lập |
| `GET /admin/stats` | Chưa có controller | Dự kiến `totalUsers`, `totalProfiles`, `totalOccupations`, `totalSkills`, `totalLmiRecords`, `totalRecommendationLogs` |
| `POST /admin/occupations`, `PUT /admin/occupations/{id}` | Chưa có controller | Dự kiến `code`, `title`, `description`, `groupId`, `industry`, `entrySalaryAvg`, `midSalaryAvg` |
| `POST /admin/skills`, `PUT /admin/skills/{id}` | Chưa có controller | Dự kiến `code`, `name`, `description`, `categoryId`, `skillType` |
| `GET /admin/occupations/{id}`, `GET /admin/skills/{id}` | Chưa có; đề xuất bổ sung/đối chiếu W4-T3 | DTO chỉnh sửa đầy đủ; nếu không có thì khóa lưu chỉnh sửa để tránh ghi đè dữ liệu từ DTO công khai thiếu trường |

Các DTO chưa có là **đề xuất tích hợp**, không phải hợp đồng đã phê duyệt. Đối chiếu tên query/body, response, mã lỗi với code W4-T2/T3 sau khi merge; sửa tập trung tại `frontend/src/features/admin/adminApi.ts`, `model.ts` và form nếu cần. Không tuyên bố PASS nghiệp vụ ghi trước bước này. Không đưa xóa hay gán/gỡ skill vào W4-T5 vì bảng phân công chỉ yêu cầu modal thêm/sửa; backend W4-T3 vẫn phụ trách các chức năng đó.

## Kịch bản nghiệm thu (chạy trên DB kiểm thử)

| ID | Thao tác | Kết quả mong đợi |
|---|---|---|
| A01 | Mở `/admin`, `/admin/lmi`, `/admin/users`, `/admin/taxonomy` khi chưa đăng nhập | Chuyển `/login` |
| A02 | Đăng nhập candidate, thử cả bốn URL và `/admin/unknown` | 403 UI, không mount màn hình quản trị; gọi trực tiếp API admin với JWT candidate trả 403 |
| A03 | Đăng nhập admin thật, mở `/admin` và từng mục, F5 | Layout đúng, nav active, không mất phiên hợp lệ |
| A04 | LMI: JSON mẫu hợp lệ, xác nhận nạp | HTTP 200; tổng xử lý = thêm + cập nhật; xem Network payload `{items:[...]}` |
| A05 | Nạp lại cùng khóa nghề/vùng/ngày | Cập nhật, không thêm trùng; kiểm tra DB bằng dữ liệu test |
| A06 | Lô có một mã nghề không tồn tại và một mã hợp lệ | HTTP 400, không có bản ghi nào trong lô được ghi; đối chiếu DB trước/sau |
| A07 | LMI sai JSON, score 101, ngày 30/02, 501 item | UI báo lỗi, không gửi request |
| A08 | Users demo (dev): lọc email/role/active, bật/tắt candidate, hủy xác nhận | Kết quả đúng; hủy không thay đổi; cảnh báo demo, không gửi request |
| A09 | Users thật sau merge W4-T2: lọc/phân trang, stats, đổi trạng thái candidate, thử tự vô hiệu hóa | API 200, dữ liệu thay đổi sau reload; self-disable bị chặn; không có password hash |
| A10 | Taxonomy demo: thêm/sửa nghề và skill, mã trùng, hủy | Modal hoạt động, báo mã trùng, không ghi server |
| A11 | Taxonomy thật sau merge W4-T3 và chốt DTO: thêm/sửa rồi tra cứu public | HTTP thành công; dữ liệu xuất hiện trên API công khai; 409 mã trùng, lỗi hiển thị rõ |
| A12 | `/occupations` và chi tiết, tìm kiếm, phân trang, URL sai, dừng backend | Dữ liệu API thật; lỗi/404 đúng, không hiển thị mẫu; bật cờ mock auth/legacy không đổi service |
| A13 | Màn hình 375px, 768px, desktop; keyboard Tab, Escape modal | Không tràn trang; bảng cuộn nội bộ; focus rõ; đóng modal bằng Esc khi không lưu |
| A14 | `npm ci`, `node scripts/test-w4-t5.cjs`, `npm run build` | Exit 0 trên môi trường đầy đủ dependency |

Ghi bằng chứng: ảnh màn hình, HTTP status/response đã che JWT, log build/test và truy vấn DB test (nếu có). Không đưa token hoặc mật khẩu vào PR.
