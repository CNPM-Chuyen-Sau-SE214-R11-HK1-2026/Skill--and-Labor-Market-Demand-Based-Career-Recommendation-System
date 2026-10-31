# Quy chuẩn viết mã & Kiến trúc phần mềm (Coding Conventions & Standards)

**Mục đích:** Chuẩn hóa phong cách lập trình, kiến trúc mã nguồn và quy ước đặt tên cho toàn bộ dự án, áp dụng đồng nhất cho cả 06 thành viên và các AI agent tham gia phát triển.

---

## 1. Nguyên tắc thiết kế chung (General Clean Architecture Principles)

1. **Phân tách trách nhiệm (Separation of Concerns):**
   * Mỗi tầng kiến trúc (Controller, Service, Repository) chỉ đảm nhận đúng nhiệm vụ của mình. Controller không chứa business logic; Repository không chứa logic biến đổi dữ liệu.
2. **Không mã hóa giá trị cứng (No Magic Numbers / Strings):**
   * Các tham số cấu hình, chuỗi phân loại, hạn mức phải được khai báo dạng hằng số (`public static final` trong Java hoặc `const` trong TypeScript) hoặc nạp từ file cấu hình.
3. **Quản trị phụ thuộc (Dependency Injection):**
   * Ưu tiên tiêm phụ thuộc qua Constructor (Constructor Injection) thay vì dùng `@Autowired` trên thuộc tính trực tiếp.

---

## 2. Quy chuẩn Backend (Java 17 & Spring Boot 3.3.4)

### 2.1. Cấu trúc Package theo miền nghiệp vụ (Domain Package Structure)
Không gom tất cả controller hoặc service vào một package phẳng lớn, mà tổ chức theo module chức năng con dưới namespace `com.cnpm.careerrecommendation`:

```text
com.cnpm.careerrecommendation
├── common/                # Tiện ích chung, xử lý lỗi toàn cục, định dạng phản hồi chuẩn
│   ├── exception/         # Custom exceptions & GlobalExceptionHandler
│   ├── dto/               # ApiResponse, ErrorResponse, PageResponse
│   └── security/          # JWT utilities & filters
├── module/
│   ├── auth/              # Đăng ký, đăng nhập, JWT authentication
│   ├── profile/           # Hồ sơ ứng viên, kỹ năng cá nhân
│   ├── taxonomy/          # Danh mục nghề nghiệp, từ điển kỹ năng O*NET/ESCO
│   ├── lmi/               # Dữ liệu thị trường lao động (Labor Market Information)
│   └── recommendation/    # Thuật toán đối sánh lõi & phân tích khoảng cách kỹ năng
└── CareerRecommendationApplication.java
```

Mỗi module nghiệp vụ tự đóng gói các thành phần:
```text
module/profile/
├── controller/            # REST Controllers (@RestController)
├── service/               # Service interfaces & Service implementations
├── repository/            # Spring Data JPA Repositories
├── entity/                # JPA Entities (@Entity)
└── dto/                   # Request/Response Data Transfer Objects (ưu tiên Java 17 Record)
```

> [!NOTE]
> **Quy định chuyển dịch từ mã nguồn khởi tạo (Starter Skeleton) sang Domain Modules (GAP-03):**
> Trong bước khởi tạo dự án ban đầu (Week 1 Task 3), backend được sinh với cấu trúc package dạng phẳng cơ bản (`controller`, `entity`, `repository`, `service`). Kể từ Tuần 2 trở đi, **toàn bộ mã nguồn tính năng mới bắt buộc phải được tổ chức vào các package module theo đúng phân hệ nghiệp vụ** (`com.cnpm.careerrecommendation.module.<domain>.*` với `<domain>` gồm `auth`, `profile`, `taxonomy`, `lmi`, `recommendation`). Các lớp khởi tạo khung trước đó sẽ được refactor chuyển về domain module tương ứng khi từng thành viên tiếp quản hiện thực hóa task liên quan.

### 2.2. Quy ước đặt tên trong Java
* **Lớp (Class) & Giao diện (Interface):** Viết `PascalCase`.
  - Service: `CandidateProfileService`, `CandidateProfileServiceImpl`
  - Controller: `CandidateProfileController`
  - Repository: `CandidateProfileRepository`
  - Entity: `CandidateProfile`, `Skill`, `Occupation`
  - DTO: `CreateProfileRequest`, `ProfileDetailResponse`
* **Phương thức (Method) & Biến (Variable):** Viết `camelCase`.
  - Ví dụ: `calculateMatchScore()`, `yearsOfExperience`.
* **Hằng số (Constant):** Viết `UPPER_SNAKE_CASE`.
  - Ví dụ: `DEFAULT_PAGE_SIZE = 20;`, `JWT_TOKEN_PREFIX = "Bearer ";`.

### 2.3. Quy ước JPA Entity & Hibernate
1. **Khớp nối chính xác với Flyway Schema:**
   * Luôn chỉ định rõ ràng `@Table(name = "...")` và `@Column(name = "...")` bằng chữ thường `snake_case`.
2. **Chiến lược Lazy Loading:**
   * Mọi quan hệ `@ManyToOne` và `@ManyToMany` phải khai báo tường minh `fetch = FetchType.LAZY` để tránh truy vấn dữ liệu thừa thãi ngoài ý muốn.
3. **Tránh lỗi đệ quy vô hạn:**
   * Không sử dụng Lombok `@Data` trên các JPA Entity vì tự động sinh `equals()`, `hashCode()`, và `toString()` gây đệ quy khi có liên kết hai chiều. Chỉ dùng `@Getter`, `@Setter`, `@NoArgsConstructor`.

### 2.4. Xác thực dữ liệu đầu vào (Input Validation)
* Sử dụng bộ thư viện Jakarta Validation: `@NotNull`, `@NotBlank`, `@Email`, `@Size`, `@Min`, `@Max`.
* Bắt buộc đánh dấu `@Valid` trước các tham số `@RequestBody` trong Controller.

### 2.5. Xử lý ngoại lệ tập trung (Centralized Exception Handling)
* Kế thừa từ một lớp ngoại lệ nghiệp vụ nền tảng: `ApiException(HttpStatus status, String errorCode, String message)`.
* Xử lý tại [`GlobalExceptionHandler`](../api/api-contract.md#12-cấu-trúc-phản-hồi-lỗi-chuẩn-standard-error-response) bằng `@RestControllerAdvice` và luôn trả về cấu trúc `ErrorResponse` chuẩn.

### 2.6. Điểm kiểm tra trạng thái dịch vụ chuẩn (Backend Health Endpoint - GAP-02)
* **Đường dẫn chuẩn:** `GET /api/health`
* **Quyền truy cập:** Công khai (`Public`), cấu hình `permitAll()` trong Spring Security, không yêu cầu JWT Bearer Token.
* **Cấu trúc phản hồi:** Luôn trả về HTTP `200 OK` với thân JSON nhẹ:
  ```json
  {
    "status": "UP"
  }
  ```
* **Ràng buộc kiến trúc:**
  - Endpoint này phản hồi trạng thái sẵn sàng (liveness/readiness) nhanh chóng, không phụ thuộc vào kết nối cơ sở dữ liệu PostgreSQL.
  - Endpoint này hoàn toàn độc lập và tách biệt với các endpoint nội bộ của Spring Boot Actuator (`/actuator/health`).
  - Được sử dụng làm mục tiêu kiểm tra sức khỏe dịch vụ cho Docker Compose (`healthcheck`), Nginx Reverse Proxy và Frontend khởi động.

### 2.7. Chuẩn hóa JWT Claims & Nguyên tắc phân quyền (JWT Claims Standards - GAP-04)
* **Cấu trúc Payload Token:** Mọi token xác thực JWT phát hành từ hệ thống bắt buộc sử dụng chính xác các claim chuẩn hóa sau:
  * `sub` (`String`): Email định danh người dùng (theo RFC 7519), tương ứng với `users.email`.
  * `userId` (`Long`): Khóa chính bản ghi trong bảng `users` (`users.id`).
  * `role` (`String`): Vai trò bảo mật kèm tiền tố `ROLE_` (`ROLE_CANDIDATE` hoặc `ROLE_ADMIN`).
* **Thời gian hiệu lực (Expiration):** Mặc định 24 giờ (86.400.000 ms).
* **Khóa bí mật ký số (Secret Key):** Nạp qua biến môi trường `JWT_SECRET` (đảm bảo độ dài tối thiểu 256-bit cho thuật toán `HS256`, kèm fallback dự phòng phục vụ phát triển cục bộ).
* **Xác thực và phân quyền phía Server:**
  * Bộ lọc `JwtAuthenticationFilter` giải mã token, đọc claim `role` và ánh xạ trực tiếp sang `SimpleGrantedAuthority(role)`.
  * **Tuyệt đối không nối thêm hoặc nhân bản tiền tố `ROLE_`** khi tạo `GrantedAuthority` (vì giá trị claim `role` đã chứa sẵn tiền tố này).
  * Trích xuất `userId` và `sub` từ token để thiết lập đối tượng Principal xác thực trong `SecurityContextHolder`.

### 2.6. Phạm vi W1-T5 và phối hợp nền tảng

`common/exception/GlobalExceptionHandler` và `common/dto/ErrorResponse` đã được
thêm làm phụ thuộc tối thiểu của auth. W1-T3 cần tái sử dụng các lớp này,
tránh tạo handler hoặc SecurityFilterChain trùng. GET `/api/health` đã được
Security cho phép; controller health vẫn thuộc W1-T3.

### 2.7. Chuẩn JWT và định danh

Dùng JJWT 0.12.6 sẵn trong pom, HS256, `sub=email`, `userId` dạng số,
`role=ROLE_CANDIDATE|ROLE_ADMIN`, `iat`, `exp`; thời hạn mặc định 24 giờ.
Lấy userId từ `AuthenticatedUser` trong SecurityContext, không lấy từ request.
JWT_SECRET là chuỗi UTF-8 ngẫu nhiên tối thiểu 32 byte, không tự giải mã Base64;
chuỗi tạo bằng Base64 được dùng nguyên văn. Không có khóa mặc định trong ứng dụng.
BCrypt cost 12, không trim mật khẩu, giới hạn 72 byte UTF-8 và 8–64 ký tự.
Chỉ trả DTO, không serialize entity hoặc password hash; không log password/JWT.

---

## 3. Quy chuẩn Frontend (React 18, TypeScript & Vite)

### 3.1. Cấu trúc thư mục Frontend
```text
frontend/src/
├── assets/                # Hình ảnh, icons, file tĩnh
├── components/            # Các UI component dùng chung tái sử dụng (Button, Input, Modal, Badge)
│   ├── common/
│   └── layout/            # Header, Sidebar, Footer, Navigation
├── pages/                 # Màn hình giao diện tương ứng với URL Route
│   ├── auth/              # Login, Register
│   ├── candidate/         # Profile, SkillSelector, RecommendationDashboard
│   ├── explorer/          # OccupationCatalog, OccupationDetail
│   └── admin/             # AdminDashboard, DataIngestion
├── services/              # Lớp giao tiếp gọi API Backend (Axios Client)
│   ├── api.ts             # Cấu hình Axios Base Instance & Interceptors
│   ├── authService.ts
│   └── recommendationService.ts
├── types/                 # Định nghĩa TypeScript Interfaces & Types
│   ├── auth.ts
│   ├── profile.ts
│   └── recommendation.ts
├── hooks/                 # Custom React Hooks (useAuth, useDebounce)
├── context/               # React Context Providers (AuthContext, ThemeContext)
├── App.tsx                # Khai báo React Router v6
└── main.tsx               # Entry point
```

### 3.2. Quy ước TypeScript & React
1. **Kiểm tra kiểu dữ liệu nghiêm ngặt (Strict Types):**
   * **Nghiêm cấm sử dụng kiểu `any`.** Sử dụng `unknown` nếu kiểu dữ liệu chưa xác định và bắt buộc ép kiểu thông qua type guard.
   * Khai báo kiểu tường minh cho `Props` của mọi component:
     ```tsx
     interface SkillBadgeProps {
       name: string;
       proficiency: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';
       onRemove?: (skillId: number) => void;
     }
     export const SkillBadge: React.FC<SkillBadgeProps> = ({ name, proficiency, onRemove }) => { ... };
     ```
2. **Quy ước đặt tên file:**
   * React Components: Viết `PascalCase` (`SkillSelector.tsx`, `JobCard.tsx`).
   * Custom Hooks: Viết `camelCase` có tiền tố `use` (`useAuth.ts`, `useRecommendations.ts`).
   * Services & Utilities: Viết `camelCase` (`apiClient.ts`, `formatCurrency.ts`).
   * Types & Interfaces: Viết `camelCase` hoặc `PascalCase` (`user.ts`, `recommendation.types.ts`).

3. **Giao tiếp API tập trung:**
   * Không gọi trực tiếp `fetch()` hoặc `axios.get()` phân tán trong các component. Mọi lệnh gọi API phải thông qua các hàm đóng gói trong thư mục `services/`.
   * Luôn xử lý các trạng thái: `isLoading`, `isError`, và hiển thị thông báo lỗi thân thiện.

### 3.3. Quy chuẩn cấu hình API Client & Đường dẫn cơ sở (Frontend API Base URL - GAP-01)
1. **Biến môi trường Base URL:**
   * Frontend cấu hình URL cơ sở của backend thông qua biến môi trường Vite:
     ```env
     VITE_API_BASE_URL=http://localhost:8080/api/v1
     ```
   * Truy xuất trong mã nguồn TypeScript thông qua: `import.meta.env.VITE_API_BASE_URL`.
2. **Quy tắc gọi API tập trung qua Axios Client:**
   * Toàn bộ các service gọi API trong `frontend/src/services/` phải sử dụng Axios instance đã được gán sẵn `baseURL`:
     ```typescript
     // src/services/api.ts
     import axios from 'axios';

     export const apiClient = axios.create({
       baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
       headers: {
         'Content-Type': 'application/json',
       },
     });
     ```
   * **Nghiêm cấm lặp lại tiền tố `/api/v1` trong các hàm service:**
     * Đúng: `apiClient.get('/occupations')`, `apiClient.post('/auth/login', credentials)`.
     * Sai (bị lặp URL thành `/api/v1/api/v1/...`): `apiClient.get('/api/v1/occupations')`.
   * **Ngoại lệ Health Check:** Nếu frontend cần kiểm tra trạng thái sống của backend tại `GET /api/health` (không thuộc tiền tố `/api/v1`), hãy gọi qua Axios instance riêng biệt không tiền tố hoặc sử dụng URL tương đối `axios.get('/api/health')`.

### 3.4. Quy chuẩn định kiểu giao diện Frontend (Frontend Styling Convention - Decision 5)
1. **Công nghệ định kiểu áp dụng:**
   * Cho phép và khuyến nghị sử dụng **Plain CSS chuẩn** kết hợp với **CSS Modules** (`[ComponentName].module.css`).
   * **Không bắt buộc cài đặt hoặc sử dụng Tailwind CSS** trong phạm vi dự án 5 tuần nhằm giảm thiểu độ phức tạp cấu hình build, tránh xung đột phong cách và tối ưu thời gian phát triển song song của 02 thành viên Frontend.
2. **Tổ chức mã nguồn CSS & Themes:**
   * Mỗi UI component có thể đi kèm một file CSS Module cùng tên (ví dụ: `JobCard.tsx` đi kèm `JobCard.module.css`) để đóng gói kiểu dáng cục bộ (Scoped Styles), tránh việc trùng lặp tên class toàn cục.
   * **Quy chuẩn biến dùng chung & Theme:** Tổ chức toàn bộ CSS biến toàn cục, bảng màu, typography và kiểu dáng dùng chung trong thư mục `frontend/src/styles/`:
     * `frontend/src/styles/variables.css`: Khai báo CSS Custom Properties (`--color-primary`, `--color-background`, `--border-radius`, `--font-family`, `--spacing-unit`).
     * `frontend/src/styles/global.css`: Reset CSS, thiết lập font chữ nền tảng và kiểu dáng toàn trang.
   * Import `global.css` và `variables.css` một lần duy nhất tại `frontend/src/main.tsx`.
