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
* Xử lý tại [`GlobalExceptionHandler`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/api/api-contract.md) bằng `@RestControllerAdvice` và luôn trả về cấu trúc `ErrorResponse` chuẩn.

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
