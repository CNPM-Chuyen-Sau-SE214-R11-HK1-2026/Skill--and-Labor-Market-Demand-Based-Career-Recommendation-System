# ADR-0008: Thiết kế Khung Kiến trúc Frontend, Bộ Định tuyến Phân tán và Quản lý Trạng thái Phiên

* **Trạng thái (Status):** Accepted (Đã phê duyệt cho Tuần 2)
* **Tác giả (Author):** Lương Phúc Khang & Đoàn Việt Khang (Frontend Developers)
* **Ngày quyết định (Date):** 2026-11-02
* **Tài liệu liên quan (Related Documents):**
  - [GAP-01: Cấu hình Frontend API Base URL](../development/coding-conventions.md#33-quy-chuẩn-cấu-hình-api-client--đường-dẫn-cơ-sở-frontend-api-base-url---gap-01)
  - [Decision 5: Quy chuẩn Styling Frontend](../development/coding-conventions.md#34-quy-chuẩn-định-kiểu-giao-diện-frontend-frontend-styling-convention---decision-5)
  - [Báo cáo thẩm định kỹ thuật Tuần 1](../audit/week1-audit.md)
  - [Kế hoạch Tác vụ Tuần 2](../plan/week2-tasks.md)
  - Mã yêu cầu: `FR-01`, `FR-02`, `FR-03`, `FR-11`, `FR-12`, `FR-13`, `NFR-01`, `UC-01` đến `UC-04`, `UC-12` đến `UC-14` | Task liên quan: `W2-T4`, `W2-T5`

---

## 1. Bối cảnh và Vấn đề (Context & Problem Statement)

Qua thẩm định kỹ thuật Tuần 1, phân hệ Frontend đang ở tình trạng hoàn toàn chưa được triển khai (chỉ có bộ khung rỗng mặc định từ Vite). Để hai thành viên Frontend (**Lương Phúc Khang - W2-T4** và **Đoàn Việt Khang - W2-T5**) có thể triển khai song song nhiều màn hình phức tạp trong Tuần 2 mà không xảy ra xung đột merge mã nguồn, dự án cần thống nhất các quyết định kỹ thuật cốt lõi:
1. **Thư viện định tuyến và cấu trúc URL:** Cần hỗ trợ các phân hệ công khai (`/login`, `/register`, `/occupations`) và phân hệ ứng viên/quản trị cần bảo vệ (`/dashboard`, `/profile`, `/admin`).
2. **Cấu hình API Client:** Tuân thủ GAP-01 (`VITE_API_BASE_URL`), tự động đính kèm JWT Bearer token và tự động xử lý khi token hết hạn.
3. **Quản lý trạng thái xác thực (Auth State):** Tránh cài đặt các thư viện quá cồng kềnh (Redux Toolkit) khi hệ thống chưa có nhu cầu.
4. **Phân rã file để phát triển song song:** Làm sao để Đoàn Việt Khang có thể thêm màn hình Khám phá nghề nghiệp mà không phải sửa chung file định tuyến chính với Lương Phúc Khang.
5. **Trực quan hóa dữ liệu thị trường (LMI Chart):** Quyết định việc có cài đặt thư viện biểu đồ hay không.

---

## 2. Các phương án đã xem xét (Options Considered)

### Phương án 1: Sử dụng Redux Toolkit + Một file `App.tsx` định tuyến tập trung + Cài đặt Chart.js
* **Mô tả:** Cài đặt Redux để lưu Auth token, mọi route đều khai báo trong `<Routes>` của `App.tsx`, cài thư viện Chart.js/Recharts để vẽ biểu đồ LMI.
* **Nhược điểm:** Tăng chi phí boilerplate, hai thành viên sẽ thường xuyên gặp xung đột Git khi cùng sửa `App.tsx` và `store.ts`; tăng dung lượng bundle JS hơn 300 kB và tiềm ẩn lỗi tương thích React 18.

### Phương án 2: React Context + Axios Interceptors + Định tuyến động `import.meta.glob` + Plain SVG/CSS Chart (Được chọn)
* **Mô tả:**
  - Dùng **React Context API** thuần cho trạng thái xác thực (`AuthContext`, `useAuth()`). Lưu token trong `localStorage` (đánh đổi MVP).
  - Dùng **Axios Instance** tập trung tại `frontend/src/api/client.ts`.
  - Dùng **React Router v6** (`createBrowserRouter`) kết hợp cơ chế quét tự động `import.meta.glob('../features/*/*Routes.tsx', { eager: true })` với metadata qua thuộc tính `handle`.
  - Biểu đồ nhu cầu LMI được vẽ bằng **Plain SVG/CSS thuần** (thẻ `<svg>`, `<rect>`, CSS bar).
* **Ưu điểm:** Nhẹ nhàng, không phát sinh dependency phức tạp; hỗ trợ phân tách trách nhiệm hoàn hảo giữa 2 lập trình viên.

---

## 3. Quyết định và Căn cứ lựa chọn (Decision & Rationale)

Nhóm quyết định lựa chọn **Phương án 2** với các đặc tả kỹ thuật chi tiết sau:

### 3.1. Cấu hình Thư viện Cốt lõi (do Lương Phúc Khang - W2-T4 thiết lập trong Ngày 1):
* Bổ sung vào `frontend/package.json`:
  `"react-router-dom": "^6.22.0"`, `"axios": "^1.6.8"`.
* Bắt buộc sử dụng cú pháp **`import type`** đối với toàn bộ các kiểu dữ liệu, interface và enum từ TypeScript để đảm bảo quá trình transpile của Vite (esbuild) sạch sẽ và tối ưu tree-shaking.

### 3.2. Cột mốc Ngày 1 (Day-1 Milestone: 2026-11-03) & Phối hợp Mock:
* Lương Phúc Khang cam kết merge **Scaffold PR** trong Ngày 1 (2026-11-03), gồm `package.json`, `client.ts`, router core, `CandidateLayout`, `AdminLayout`, `AuthContext`.
* Trong ngày đầu tiên, Đoàn Việt Khang (W2-T5) phát triển độc lập trên nhánh riêng với mock data đặt tại `frontend/src/features/occupations/mocks/`. Sau khi Scaffold PR merge, Khang ghép module vào hệ thống chính.

### 3.3. Chuẩn hóa API Client theo GAP-01:
* File duy nhất: `frontend/src/api/client.ts`.
* `baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'`.
* **Request Interceptor:** Tự động lấy token từ `localStorage.getItem('token')` và gắn header `Authorization: Bearer <token>`.
* **Response Interceptor:** Bắt mã lỗi HTTP 401, tự động xóa token và chuyển hướng trình duyệt về `/login`.

### 3.4. Cơ chế Định tuyến Tự động Quét Module (`import.meta.glob`) & Quy chuẩn `handle`:
* Mỗi route module định nghĩa đối tượng `RouteObject` của React Router v6. Cấu hình layout và phân quyền được truyền qua thuộc tính `handle`:
  ```typescript
  import type { RouteObject } from 'react-router-dom';

  export interface AppRouteHandle {
    layout: 'candidate' | 'admin' | 'public';
    roles?: ('ROLE_CANDIDATE' | 'ROLE_ADMIN')[];
  }
  ```
* **Quy chuẩn trang Khám phá Nghề nghiệp (`/occupations`):**
  * Trang `/occupations` và `/occupations/:id` là **HOÀN TOÀN CÔNG KHAI (Public)**:
    `handle: { layout: 'candidate', roles: [] }` (hoặc `roles` undefined).
  * Khách vãng lai, ứng viên chưa đăng nhập hoặc đã đăng nhập đều có thể truy cập tự do mà không bị chặn.
* Lương Phúc Khang thiết lập router cha tại `frontend/src/routes/index.tsx`:
  ```typescript
  import { createBrowserRouter } from 'react-router-dom';
  import type { RouteObject } from 'react-router-dom';

  const featureModules = import.meta.glob<{ default: RouteObject[] }>(
    '../features/*/*Routes.tsx',
    { eager: true }
  );
  const featureRoutes: RouteObject[] = Object.values(featureModules).flatMap(
    (m) => m.default || []
  );
  ```
* Đoàn Việt Khang chỉ việc tạo file `frontend/src/features/occupations/occupationsRoutes.tsx` xuất `export default [...]`. Hệ thống tự động nhận diện route mà Phúc Khang không cần sửa router chính.

### 3.5. Trực quan hóa LMI bằng Plain SVG/CSS (do Đoàn Việt Khang hiện thực):
* Không cài thêm bất kỳ thư viện chart ngoài nào (tránh xung đột bundle và giữ dung lượng nhẹ). Thành phần biểu đồ LMI dùng SVG và CSS thuần để hiển thị điểm nhu cầu thị trường (`demand_score`) và chỉ số cạnh tranh.

### 3.6. Bảo vệ Đường dẫn riêng tư (`ProtectedRoute`) & Chuẩn hóa Prop `roles`:
* Thống nhất **duy nhất một tên prop là `roles`** cho việc kiểm tra quyền truy cập:
  ```tsx
  import type { ReactNode } from 'react';

  interface ProtectedRouteProps {
    roles?: ('ROLE_CANDIDATE' | 'ROLE_ADMIN')[];
    children: ReactNode;
  }
  ```
* **Chuỗi vai trò chuẩn từ `/auth/me`:**
  * Endpoint `GET /api/v1/auth/me` trả về trường `role` mang giá trị chính xác là:
    `"ROLE_CANDIDATE"` hoặc `"ROLE_ADMIN"`.
  * Frontend so khớp trực tiếp chuỗi này với mảng `roles`.
* Nếu chưa đăng nhập: `<Navigate to="/login" replace />`.
* Nếu đã đăng nhập nhưng vai trò không nằm trong danh sách `roles` được phép: hiển thị trang 403 Forbidden hoặc điều hướng về `/dashboard`.

---

## 4. Hệ quả và Đánh đổi (Consequences & Trade-offs)

### Tác động tích cực:
* Thiết lập nền tảng Frontend chuẩn mực, sạch sẽ, sẵn sàng kết nối Backend.
* Khử hoàn toàn xung đột Git giữa 2 thành viên Frontend.
* Không làm phình to bundle JS; tốc độ tải trang cực nhanh nhờ SVG thuần.

### Đánh đổi & Ghi nhận Rủi ro An ninh (Known Security Trade-offs):
* **Lưu trữ Token trong `localStorage`:** Đây là một **đánh đổi chấp nhận được cho giai đoạn MVP (Known MVP Trade-off)** để đơn giản hóa giao tiếp API không trạng thái. Đội ngũ ghi nhận rõ ràng: `localStorage` **không an toàn tuyệt đối** trước các cuộc tấn công XSS nếu ứng dụng có lỗ hổng chèn mã độc.
* **Biện pháp giảm thiểu:** Không lưu trữ mật mã hay thông tin nhạy cảm trong token; tuân thủ cơ chế escape HTML mặc định của React; xem xét chuyển sang cơ chế Cookie `HttpOnly SameSite` khi nâng cấp hạ tầng Production.
