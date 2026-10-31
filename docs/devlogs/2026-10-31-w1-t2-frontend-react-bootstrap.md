# DevLog: Khởi tạo Khung Frontend React + TypeScript + Vite + Router + Axios (Task W1-T2)

* **Mã tác vụ / Task ID:** `W1-T2` / `TASK-W1-FE-BOOTSTRAP`
* **Người thực hiện / Agent:** Lương Phúc Khang (hỗ trợ bởi Claude Code AI Assistant)
* **Ngày thực hiện (Date):** 2026-10-31
* **Nhánh Git (Branch):** `main`

---

## 1. Mục tiêu (Objective)

Thực hiện nhiệm vụ Tuần 1 Tác vụ 2 (W1-T2: Khởi tạo Frontend React) nhằm:
1. Thiết lập hoàn chỉnh bộ khung Frontend SPA sử dụng React 18, TypeScript Strict Mode và Vite.
2. Cấu hình hệ thống Styling theo Quyết định 5 (Plain CSS + CSS Modules, CSS Variables tập trung tại `src/styles/variables.css`, không phụ thuộc Tailwind CSS).
3. Triển khai kiến trúc giao tiếp API tập trung với Axios Client tuân thủ GAP-01 (không lặp lại tiền tố `/api/v1`), tự động đính kèm JWT Bearer token và cơ chế phân tách Real API / Mock API độc lập (API-01).
4. Thiết lập hệ thống Authentication Client-side: `tokenStorage` an toàn (safe JSON parsing), giải mã và kiểm tra thời hạn JWT `isTokenExpired()` khi khôi phục session (chống flash redirect khi F5), mutex lock chống xử lý 401 đồng thời (AUTH-02), và Route Guards (`ProtectedRoute`, `GuestRoute`).
5. Xây dựng khung các màn hình cơ bản: Trang chủ (`HomePage`), Đăng nhập (`LoginPage`), Đăng ký (`RegisterPage`), Bảng điều khiển ứng viên (`DashboardPage`), và Trang 404 (`NotFoundPage`).

---

## 2. Thay đổi thực tế (Changes)

* **Mã nguồn (Source Code):**
  * Đã cấu hình và cập nhật `frontend/package.json` bổ sung `axios` (`^1.7.7`) và `react-router-dom` (`^6.27.0`).
  * `src/config/env.ts`: Quản lý tập trung biến môi trường (`VITE_API_BASE_URL`, `VITE_ENABLE_MOCK_API`) kèm giá trị fallback an toàn chống WSOD.
  * `src/types/`: Khởi tạo `api.ts` (chuẩn `ApiErrorResponse`, `ApiFieldError`, `PageResponse`) và `auth.ts` (chuẩn `User`, `LoginRequest`, `RegisterRequest`, `AuthResponse`).
  * `src/utils/`:
    * `tokenStorage.ts`: Quản lý `localStorage` an toàn với try-catch bắt lỗi JSON hỏng và security disclaimer.
    * `jwt.ts`: Tiện ích kiểm tra `isTokenExpired()` qua client-side Base64Url decode.
  * `src/styles/`:
    * `variables.css`: Khai báo bảng màu chuẩn, typography, spacing, border-radius và shadows.
    * `global.css`: Reset CSS, container utility và typography toàn trang.
  * `src/services/`:
    * `api.ts`: Cấu hình Axios instance tập trung, Request Interceptor đính kèm Bearer token, Response Interceptor xử lý 401 có cờ `isHandlingUnauthorized` mutex lock và hàm `resetUnauthorizedHandling()`.
    * `authService.ts`: Interface `AuthService` và factory phân giải Real/Mock.
    * `realAuthService.ts`: Gọi REST API backend thật (`/auth/login`, `/auth/register`, `/auth/me`).
    * `mock/mockAuthService.ts`: Mock adapter độc lập tạo token riêng (`mock-token-*`), không gửi lên backend thật.
  * `src/context/AuthContext.tsx` & `src/hooks/useAuth.ts`: Quản lý state phiên đăng nhập (`user`, `token`, `isLoading`, `isAuthenticated`), lắng nghe sự kiện `auth:unauthorized` để dọn dẹp state và điều hướng bằng React Router.
  * `src/components/`:
    * `common/`: `Button` (kèm variants, loading state), `Input` (kèm validation error & helper text), `Alert` (error/success/warning/info), `LoadingSpinner`.
    * `layout/`: `Header` (hiển thị trạng thái đăng nhập, profile name, navigation), `Footer` (bản quyền dự án Nhóm 4), `MainLayout`.
  * `src/routes/`: `ProtectedRoute.tsx` (kiểm tra `isLoading` và `isAuthenticated`), `GuestRoute.tsx`, `AppRoutes.tsx`.
  * `src/pages/`: `HomePage`, `LoginPage`, `RegisterPage`, `DashboardPage`, `NotFoundPage`.
  * `src/App.tsx`: Bọc theo thứ tự chuẩn `<BrowserRouter><AuthProvider><AppRoutes /></AuthProvider></BrowserRouter>`.
  * `src/main.tsx`: Nạp `variables.css` và `global.css`, render React 18 root.
* **Cấu hình & Môi trường (Config & Environment):**
  * Tạo `frontend/.env.example` và `frontend/.env` với `VITE_API_BASE_URL=http://localhost:8080/api/v1` và `VITE_ENABLE_MOCK_API=false`.
* **Tài liệu chuẩn (Documentation Updates):**
  * Cập nhật `docs/devlogs/README.md` bổ sung bản ghi DevLog cho W1-T2.

---

## 3. Khu vực bị ảnh hưởng (Affected Areas)

* `frontend/package.json`
* `frontend/.env.example`
* `frontend/.env`
* `frontend/src/config/env.ts`
* `frontend/src/types/api.ts`
* `frontend/src/types/auth.ts`
* `frontend/src/utils/tokenStorage.ts`
* `frontend/src/utils/jwt.ts`
* `frontend/src/styles/variables.css`
* `frontend/src/styles/global.css`
* `frontend/src/services/api.ts`
* `frontend/src/services/authService.ts`
* `frontend/src/services/realAuthService.ts`
* `frontend/src/services/mock/mockAuthService.ts`
* `frontend/src/context/AuthContext.tsx`
* `frontend/src/hooks/useAuth.ts`
* `frontend/src/components/common/Button.tsx` & `.module.css`
* `frontend/src/components/common/Input.tsx` & `.module.css`
* `frontend/src/components/common/Alert.tsx` & `.module.css`
* `frontend/src/components/common/LoadingSpinner.tsx` & `.module.css`
* `frontend/src/components/layout/Header.tsx` & `.module.css`
* `frontend/src/components/layout/Footer.tsx` & `.module.css`
* `frontend/src/components/layout/MainLayout.tsx` & `.module.css`
* `frontend/src/routes/ProtectedRoute.tsx`
* `frontend/src/routes/GuestRoute.tsx`
* `frontend/src/routes/AppRoutes.tsx`
* `frontend/src/pages/home/HomePage.tsx` & `.module.css`
* `frontend/src/pages/auth/LoginPage.tsx` & `.module.css`
* `frontend/src/pages/auth/RegisterPage.tsx` & `.module.css`
* `frontend/src/pages/candidate/DashboardPage.tsx` & `.module.css`
* `frontend/src/pages/NotFoundPage.tsx`
* `frontend/src/App.tsx`
* `frontend/src/main.tsx`
* `docs/devlogs/README.md`
* `docs/devlogs/2026-10-31-w1-t2-frontend-react-bootstrap.md`

---

## 4. Quyết định kỹ thuật quan trọng (Decisions & Rationale)

* **GAP-01 (API Base URL & Path Alignment):** Cấu hình Axios instance với `baseURL = env.API_BASE_URL` (`http://localhost:8080/api/v1`). Mọi hàm gọi service truyền đường dẫn tương đối (ví dụ `apiClient.post('/auth/login', payload)`), ngăn chặn tuyệt đối lỗi lặp prefix `/api/v1/api/v1/...`.
* **API-01 (Phân tách Real và Mock API):** Tách `mockAuthService.ts` thành adapter độc lập; chỉ kích hoạt khi `VITE_ENABLE_MOCK_API=true`. Khi gọi Backend thật gặp lỗi mạng hoặc HTTP error, hệ thống giữ nguyên lỗi thật trả về UI thay vì tự động nhảy sang mock data làm sai lệch dữ liệu kiểm thử.
* **AUTH-02 (Mutex Lock, Token Identity & Safe 401 Interceptor):** Sử dụng cờ `isHandlingUnauthorized` kết hợp xác minh danh tính token (`requestToken === Bearer ${currentToken}`) tương thích với `AxiosHeaders` của Axios 1.x. Đảm bảo nếu một request cũ từ phiên làm việc trước nhận mã 401 sau khi người dùng đã đăng nhập lại (nhận token mới), session mới tuyệt đối không bị xóa nhầm (ngăn chặn triệt để Race Condition). Bỏ qua mã 401 tại `/auth/login` và `/auth/register` để hiển thị lỗi trên form. Cung cấp hàm `resetUnauthorizedHandling()` reset cờ khi đăng nhập thành công.
* **AUTH-05 (Hardening & Cô lập Mock Token 2 lớp):** Tại Request Interceptor của `apiClient`, kiểm tra cả 2 lớp:
  1. *Lớp Storage:* Nếu `tokenStorage` lưu `mock-token-*`, chủ động xóa bỏ header `Authorization` khỏi request.
  2. *Lớp Caller:* Nếu caller tự ý truyền header `Authorization` chứa chuỗi `mock-token-`, interceptor chủ động gỡ bỏ header này trước khi gửi ra ngoài.
  Đảm bảo không một mock token nào có thể lọt ra backend thật.
* **AUTH-04 (Kiểm tra thời hạn JWT & Chống Flash Redirect):** Tại `AuthContext`, khi nạp lại trang (F5/Reload), kiểm tra `isTokenExpired()` trước khi khôi phục session. Cả `ProtectedRoute` và `GuestRoute` đều kiểm tra `if (isLoading) return <LoadingSpinner />` trước khi đưa ra quyết định chuyển hướng, loại bỏ triệt để hiện tượng nháy trang hoặc redirect ngoài ý muốn khi ứng dụng đang khởi tạo.
* **Decision 5 (CSS Modules & CSS Variables):** Đóng gói style của component qua `[Component].module.css` và dùng biến toàn cục tại `src/styles/variables.css`, không cài đặt Tailwind CSS nhằm giảm độ phức tạp dependency và tối ưu thời gian phát triển song song cho đội ngũ Frontend.
* **TS-01 (Strict TypeScript Discipline):** Nghiêm cấm hoàn toàn `any`, `as any`, `@ts-ignore`. Toàn bộ Props và dữ liệu API đều được định kiểu qua interface rõ ràng.

---

## 5. Kiểm tra & Xác thực (Validation Performed)

### 5.1. Phân định rõ 3 cấp độ kiểm thử (Testing Levels Distinction):
1. **Cấp độ 1 — Kiểm thử thuật toán mô phỏng (Simulation Test):**
   - Đã thực hiện kiểm tra logic giải mã Base64Url `isTokenExpired()`, xử lý an toàn lỗi cú pháp JSON của `tokenStorage.getUser()`.
   - Kết quả: PASS về mặt thuật toán.
2. **Cấp độ 2 — Kiểm thử trực tiếp Module Production (`apiClient` trong `src/services/api.ts`):**
   - Không dùng code mô phỏng viết lại. Sử dụng Vite SSR (`scripts/run-verify.cjs`) biên dịch `scripts/verify-production-api.ts` để import và gọi trực tiếp instance `apiClient` thật.
   - Sử dụng **Axios Custom Adapter** để chặn tại tầng transport mạng, cho phép 100% chuỗi Interceptors thật của `api.ts` thực thi và tương tác với `tokenStorage` và `window.dispatchEvent`.
   - Kết quả: **PASS 6/6 Scenarios (Chi tiết bảng dưới)**.
3. **Cấp độ 3 — Kiểm thử tích hợp Backend thực tế (Live Backend Integration):**
   - **Chưa thực hiện (Pending):** Do backend Spring Boot (W1-T5/W1-T4) đang được hoàn thiện song song trên nhánh khác và chưa khởi chạy server live ở cổng 8080.
   - Hợp đồng REST API đã được khớp 100% với `docs/api/api-contract.md §2.1`. Sẽ thực hiện kiểm thử E2E trực tiếp khi bước vào Tuần 2.

### 5.2. Kết quả kiểm tra công cụ & Build:

| Lệnh / Thao tác kiểm tra | Mục đích kiểm tra | Kết quả thực tế | Ghi chú |
|---|---|---|---|
| `npm install` | Cài đặt các thư viện phụ thuộc (`axios@^1.7.7`, `react-router-dom@^6.27.0`) | **PASS** (173 packages installed, 0 vulnerabilities) | Không cài thêm dependency ngoài phạm vi |
| `npx tsc -b` | Kiểm tra toàn bộ mã nguồn theo TypeScript Strict Mode | **PASS** (0 warnings, 0 errors) | Không có vi phạm type hoặc lỗi cú pháp |
| `npm run build` | Đóng gói production build bằng Vite + Rollup | **PASS** (128 modules transformed, sinh `dist/` thành công trong 5.79s) | Bundle JS: 241 kB, CSS: 12.8 kB |
| Quét vi phạm `any`, `@ts-ignore` | Quét regex `any`, `as any`, `@ts-ignore` trong `src/` | **PASS** (0 kết quả vi phạm) | Tuân thủ 100% `TS-01` |
| `node scripts/run-verify.cjs` | Kiểm thử trực tiếp module production `apiClient` qua Custom Adapter | **PASS (6/6 Scenarios đạt)** | Chạy trực tiếp mã nguồn `api.ts` |

### 5.3. Bằng chứng kiểm thử 6 kịch bản trên Module Production:
* **Scenario A (Race condition guard - HIGH):** Khởi tạo request $R_1$ mang $T_1$ qua `apiClient.get('/slow-request')`. Trong lúc $R_1$ đang bay trên mạng, người dùng đăng nhập lại nhận $T_2$ (`saveSession({ accessToken: 'T2' })`). Sau đó $R_1$ nhận mã lỗi 401 từ server cũ.
  - *Kết quả:* `currentTokenInStorage === 'T2'`, `dispatchedEventsCount === 0` $\rightarrow$ **PASS**.
* **Scenario B (Xử lý 401 phiên hiện tại):** Request mang token hiện hành $T_1$ nhận mã 401 từ adapter.
  - *Kết quả:* `currentTokenInStorage === null`, phát đúng 1 sự kiện `auth:unauthorized` $\rightarrow$ **PASS**.
* **Scenario C (Xử lý 5 request 401 đồng thời):** 5 request đồng thời nhận 401.
  - *Kết quả:* `currentTokenInStorage === null`, `dispatchedEventsCount === 1` $\rightarrow$ **PASS**.
* **Scenario D (Hardening Mock Token 2 lớp):**
  - D1 (Storage chứa mock token): `apiClient.get('/test-d1')` gửi đi $\rightarrow$ Header `Authorization` bị xóa (`undefined`).
  - D2 (Caller cố tình truyền header mock token): `apiClient.get('/test-d2', { headers: { Authorization: 'Bearer mock-token-...' } })` $\rightarrow$ Header `Authorization` bị phát hiện và chủ động xóa sạch (`undefined`).
  - *Kết quả:* Cả D1 và D2 đều không có Authorization header $\rightarrow$ **PASS**.
* **Scenario E (Gắn Token thật hợp lệ):** Storage lưu JWT thật, request gửi qua `apiClient.get('/occupations')`.
  - *Kết quả:* Header nhận đúng `Bearer real.jwt.token.payload.signature` $\rightarrow$ **PASS**.
* **Scenario F (Xử lý 401 tại `/auth/login`):** Gọi `apiClient.post('/auth/login')` nhận 401 từ adapter.
  - *Kết quả:* Không kích hoạt sự kiện unauthorized toàn cục (`dispatchedEventsCount === 0`), ném lỗi về caller xử lý $\rightarrow$ **PASS**.

---

## 6. Công việc tồn đọng & Bước tiếp theo (Outstanding Work)

* [x] Chạy `npm install`, `npx tsc -b` và `npm run build` kiểm tra toàn diện build production (Đã hoàn thành 100%).
* [x] Sửa lỗi Race Condition và Hardening Mock Token tại `src/services/api.ts` (Đã hoàn thành).
* [x] Kiểm thử trực tiếp module production `apiClient` qua Custom Adapter (Đã hoàn thành và xác thực 6/6 Scenarios).
* [ ] Bàn giao cho Tuần 2 (W2-FE-CND-01 & W2-FE-ADM-01) để tiếp tục hiện thực hóa:
  * Màn hình quản lý hồ sơ ứng viên chi tiết (`CandidateProfile`).
  * Bảng chọn danh mục kỹ năng cá nhân (`SkillSelector`).
  * Màn hình khám phá nghề nghiệp và dữ liệu thị trường LMI (`OccupationCatalog`).
  * Kiểm thử tích hợp E2E khi Backend Spring Boot sẵn sàng ở cổng 8080.
