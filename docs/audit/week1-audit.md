# Báo cáo thẩm định kỹ thuật sau Tuần 1 (Week 1 Audit Report)

**Dự án:** Hệ thống gợi ý nghề nghiệp dựa trên kỹ năng và nhu cầu thị trường lao động (Skill- and Labor Market Demand–Based Career Recommendation System)  
**Vai trò:** Senior Tech Lead / Kiến trúc sư phần mềm độc lập  
**Ngày thực hiện:** 2026-11-02  
**Phạm vi thẩm định:** Tác vụ Tuần 1 (`W1-T1` đến `W1-T6`), toàn bộ mã nguồn `backend`, `frontend`, cấu hình hạ tầng `docker`, thiết kế CSDL và hệ thống tài liệu.  
**Nguyên tắc thẩm định:** Dựa trên chứng cứ thực tế từ mã nguồn và thực thi lệnh kiểm tra (evidence-based), không dựa vào lời tự khai báo trong task sheet hay devlog.

---

## Tóm tắt trạng thái các hạng mục thẩm định (Audit Summary Table)

| STT | Phân vùng thẩm định | Trạng thái xác minh | Rủi ro kỹ thuật hàng đầu (Top Risk) |
| :---: | :--- | :---: | :--- |
| **1** | **Đối chiếu Task W1-T1 – W1-T6** | **Partially** | Khai báo hoàn thành 100% nhưng Frontend (W1-T2) chỉ là starter skeleton rỗng; Backend bị lỗi cú pháp YAML gãy runtime; Docker Compose không thể chạy out-of-the-box. |
| **2** | **Cấu trúc Backend & Modular Monolith** | **Partially** | Ranh giới package bị vỡ hoàn toàn do xung đột giữa cấu trúc phẳng (`entity/`, `dto/`, `exception/`) và module auth (`module/auth/`, `common/`). Xuất hiện 2 lớp thực thể `User`, 2 lớp `ErrorResponse`, 2 bộ `GlobalExceptionHandler` cùng tồn tại. |
| **3** | **Cơ sở dữ liệu, Flyway & Thực thể JPA** | **Partially** | Thực thể `entity.User` không khớp migration `V2` (thiếu cột `phone`); 8/10 bảng CSDL mới chỉ có Entity/Repository, hoàn toàn chưa có Service/Controller; chưa có bất kỳ dữ liệu mẫu (Seed Data) nào. |
| **4** | **Hiện trạng Frontend** | **Not done** | Chưa cài đặt `react-router-dom`, `axios`, không có state management, không có API client, không có màn hình Auth hay Layout; chỉ có màn hình mẫu mặc định của Vite. |
| **5** | **Bảo mật & Phân quyền (Auth/Security)** | **Partially** | Lỗ hổng bảo mật nghiêm trọng: `GET /api/v1/auth/me` bị cấu hình `permitAll()` nhầm dẫn đến crash NPE 500 khi chưa đăng nhập; không có cấu hình phân quyền theo role (`hasRole`) trên URL/Method; thiếu cơ chế Refresh Token. |
| **6** | **Hạ tầng DevOps & Docker Compose** | **Partially** | Clone sạch về không thể chạy: `docker-compose.yml` bắt buộc `JWT_SECRET` nhưng không có `.env`; file `.env.example` chứa chuỗi mẫu `your_...` sẽ kích hoạt exception tự hủy của backend; thiếu healthcheck cho backend. |
| **7** | **Tài liệu & Kế hoạch Tuần 2–5** | **Partially** | Kế hoạch Tuần 2 trong `docs/planning/week-2-5-backlog.md` bị lệch hoàn toàn so với thực tế (yêu cầu viết lại những thứ đã làm ở W1-T4, W1-T5, đồng thời bỏ sót các việc cốt lõi của Frontend và dọn dẹp xung đột code). |
| **8** | **Điểm nóng xung đột mã nguồn (Hot Spots)** | **Partially** | `application.yml`, `SecurityConfig.java`, `GlobalExceptionHandler.java`, thực thể `User`, bộ định tuyến Frontend và thư mục Migration `V3+` đang là các điểm nghẽn xung đột merge cực kỳ nguy hiểm nếu phát triển song song. |

---

## Chi tiết kết quả kiểm tra và bằng chứng thực nghiệm (Detailed Audit Findings)

### 1. Đối chiếu cam kết công việc và thực tế (Task Claims vs. Reality: W1-T1 đến W1-T6)

#### W1-T1: Kiến trúc hệ thống, Hợp đồng API, Thiết kế CSDL & Quy chuẩn (Bùi Vạn Khải)
* **Trạng thái:** **Partially** (Tài liệu chi tiết, nhưng mã nguồn phân rã sai quy chuẩn).
* **Cam kết mong đợi:** Xây dựng hệ thống tài liệu kiến trúc Modular Monolith, hợp đồng API, thiết kế CSDL 10 bảng và tài liệu quy chuẩn làm việc song song.
* **Chứng cứ thực tế:**
  - Tài liệu lý thuyết đã được viết rất kỹ tại `docs/architecture/system-architecture.md`, `docs/api/api-contract.md`, `docs/database/database-design.md`.
  - **Sai lệch (Mismatch):** Tài liệu quy định chuyển sang cấu trúc domain module `com.cnpm.careerrecommendation.module.<domain>.*` (GAP-03), nhưng trong mã nguồn thực tế lại bị phân tách làm đôi: W1-T3 và W1-T4 dựng cấu trúc phẳng (`controller`, `dto`, `entity`, `exception`, `repository`), trong khi W1-T5 tự ý dựng `module.auth.*` và `common.*`.
  - **Lệch hợp đồng API:** Mục 2.1 API contract không mô tả trường `phone` trong đăng ký, nhưng W1-T5 đã đưa vào DTO và CSDL migration `V2`.

#### W1-T2: React Frontend Bootstrap (Đoàn Việt Khang & Huỳnh Long Bảo Khanh)
* **Trạng thái:** **Not done** (Chỉ là starter template Vite nguyên bản).
* **Cam kết mong đợi:** Thiết lập khung dự án React 18, TypeScript, Axios client đồng bộ `VITE_API_BASE_URL` (GAP-01), quy chuẩn CSS Modules, cấu trúc điều hướng.
* **Chứng cứ thực tế:**
  - Kiểm tra `frontend/package.json`: Chỉ chứa 2 dependency cốt lõi: `"react": "^18.3.1"` và `"react-dom": "^18.3.1"`. Không hề có `react-router-dom`, không có `axios`, không có thư viện quản lý state hay định kiểu nào.
  - Kiểm tra `frontend/src`: Chỉ có 5 file mặc định từ template (`App.tsx`, `App.css`, `index.css`, `main.tsx`, `vite-env.d.ts`).
  - Trong [App.tsx](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/frontend/src/App.tsx#L8-L9), nội dung chỉ là text tĩnh: `"Frontend skeleton initialized successfully. System Status: Skeleton Initialized"`.
  - Không có router, không có layout, không có API service, không có form đăng nhập/đăng ký. Khai báo "Hoàn thành" là hoàn toàn không đúng thực tế.

#### W1-T3: Spring Boot Foundation (Phạm Ngọc Gia Khang / Bùi Vạn Khải)
* **Trạng thái:** **Partially** (Đã tạo nền móng nhưng bị lỗi nghiêm trọng sau khi tích hợp W1-T5).
* **Cam kết mong đợi:** Ứng dụng build thành công, `GET /api/health` trả HTTP 200, xử lý lỗi chuẩn API 1.2, validation dữ liệu, CORS hoạt động.
* **Chứng cứ thực tế:**
  - Đã có `HealthController.java`, `CorsProperties.java`, `WebMvcConfig.java`, `SecurityConfig.java`.
  - **Lỗi nghiêm trọng (Critical Defect):** Chạy lệnh `mvn -f backend/pom.xml test` gặp lỗi:
    ```text
    org.yaml.snakeyaml.constructor.DuplicateKeyException: while constructing a mapping
     in 'reader', line 5, column 3: application:
     found duplicate key jackson in 'reader', line 33, column 3: jackson:
    ```
    Tại [backend/src/main/resources/application.yml](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/backend/src/main/resources/application.yml#L8-L38), key `jackson` bị khai báo lặp lại 2 lần (dòng 8 và dòng 33). Hậu quả: Toàn bộ 18 lượt test Spring context / WebMvc test bị nổ lỗi (`Errors: 18, Failures: 0`).
  - Lớp `GlobalExceptionHandler` và `ErrorResponse` trong W1-T3 bị xung đột trực tiếp với lớp cùng tên của W1-T5.

#### W1-T4: PostgreSQL & JPA Entities/Repositories (Huỳnh Long Bảo Khanh)
* **Trạng thái:** **Partially** (10 Entity được tạo nhưng bị cô lập và xung đột với Auth).
* **Cam kết mong đợi:** Ánh xạ 10 bảng CSDL thành 10 JPA Entities và 10 Repositories, tuân thủ `FetchType.LAZY` và kiểm soát kiểu dữ liệu.
* **Chứng cứ thực tế:**
  - Đã tạo 10 Entities và 10 Repositories tại `backend/src/main/java/com/cnpm/careerrecommendation/{entity,repository}`. Unit test phản xạ `EntityMappingTest.java` chạy độc lập pass (8 tests).
  - **Xung đột lớn (Major Conflict):** Lớp `com.cnpm.careerrecommendation.entity.User` xung đột hoàn toàn với `com.cnpm.careerrecommendation.module.auth.entity.User`. Cả 2 cùng map vào bảng `users`.
  - `entity.User` thiếu cột `phone` của migration `V2`, sử dụng `OffsetDateTime` trong khi `module.auth.entity.User` dùng `Instant`.
  - Toàn bộ 8 bảng còn lại (`candidate_profiles`, `skills`, `occupations`...) chưa hề có bất kỳ Service hay Controller nào.
  - Chưa hề có Seed Data trong repository.

#### W1-T5: JWT Auth & Security (Đoàn Việt Khang)
* **Trạng thái:** **Partially** (Có logic auth nhưng để hổng endpoint bảo mật và gãy tích hợp).
* **Cam kết mong đợi:** Đăng ký, đăng nhập, JWT HS256 với claim chuẩn (`sub`, `userId`, `role`), BCrypt cost 12, kiểm soát phân quyền RBAC.
* **Chứng cứ thực tế:**
  - Logic mã hóa mật khẩu và phát sinh JWT hoạt động tốt trong các unit test (`JwtTokenProviderTest` pass 23 tests).
  - Đã có `AuthController.java` và `AuthService.java`.
  - **Lỗ hổng bảo mật (Security Gap):** Tại [SecurityConfig.java](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/backend/src/main/java/com/cnpm/careerrecommendation/config/SecurityConfig.java#L56), cấu hình `.requestMatchers("/api/v1/auth/**").permitAll()` vô tình mở toang endpoint `GET /api/v1/auth/me`. Khi người dùng chưa đăng nhập gọi endpoint này, `identity` bị null, dẫn đến crash `NullPointerException` tại [AuthService.java](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/backend/src/main/java/com/cnpm/careerrecommendation/module/auth/service/AuthService.java#L62) thay vì trả về lỗi 401 Unauthorized.
  - File cấu hình `SecurityConfig.java` không hề đăng ký `JwtAuthenticationFilter` vào chuỗi filter chain; filter này đang chạy như một Servlet Filter rời thông qua annotation `@Component`.
  - Không có cơ chế Refresh Token hay Logout/Blacklist.

#### W1-T6: Docker Compose & E2E Environment (Phan Nguyễn Thông)
* **Trạng thái:** **Partially** (Chưa chạy được end-to-end trên một bản clone sạch).
* **Cam kết mong đợi:** Khởi động 3 dịch vụ Frontend, Backend, PostgreSQL qua 1 lệnh `docker compose up -d`, kiểm tra kết nối liên thông.
* **Chứng cứ thực tế:**
  - Chạy thử lệnh kiểm tra cấu hình trên bản clone: `docker compose config`
  - **Kết quả lệnh:**
    ```text
    error while interpolating services.backend.environment.JWT_SECRET: required variable JWT_SECRET is missing a value: Set JWT_SECRET to a private random UTF-8 secret of at least 32 bytes
    ```
  - Trong kho không có file `.env`. Nếu người dùng copy `.env.example` sang `.env`, biến `JWT_SECRET` mẫu mang giá trị `your_random_secret_key_at_least_32_utf8_bytes_here`. Chuỗi này bắt đầu bằng `your_`, khiến [JwtTokenProvider.java](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/backend/src/main/java/com/cnpm/careerrecommendation/common/security/JwtTokenProvider.java#L22-L26) ném lỗi `IllegalArgumentException` ngay khi boot container!
  - `docker-compose.yml` thiếu healthcheck cho backend. Bản thân container backend cũng không thể khởi động do lỗi `application.yml` (DuplicateKeyException).
  - Devlog W1-T6 chính thức thừa nhận: *"W1-T6 chưa được xác nhận nghiệm thu toàn bộ, vì chưa có kết quả kiểm thử Frontend gọi Backend thành công"*.

---

### 2. Thẩm định Backend: Ranh giới kiến trúc, API, DTO, Service, Exception & Test

#### 2.1. Ranh giới Package & Modular Monolith
Kiến trúc bị rạn nứt nghiêm trọng giữa hai trường phái thiết kế package:
1. **Trường phái phẳng (Flat Skeleton từ W1-T3, W1-T4):**
   - `com.cnpm.careerrecommendation.dto.*`
   - `com.cnpm.careerrecommendation.exception.*`
   - `com.cnpm.careerrecommendation.entity.*` (chứa 10 entities gồm `User.java`)
   - `com.cnpm.careerrecommendation.repository.*` (chứa 10 repos gồm `UserRepository.java`)
2. **Trường phái Domain Module (từ W1-T5):**
   - `com.cnpm.careerrecommendation.module.auth.controller.*`
   - `com.cnpm.careerrecommendation.module.auth.service.*`
   - `com.cnpm.careerrecommendation.module.auth.dto.*`
   - `com.cnpm.careerrecommendation.module.auth.entity.User`
   - `com.cnpm.careerrecommendation.module.auth.repository.UserRepository`
   - `com.cnpm.careerrecommendation.common.dto.ErrorResponse`
   - `com.cnpm.careerrecommendation.common.exception.GlobalExceptionHandler`

Hậu quả: Hibernate và Spring IoC phát hiện 2 entity `User` cùng map vào bảng `users`. Hai `@RestControllerAdvice` cùng can thiệp vào pipeline xử lý ngoại lệ.

#### 2.2. Hiện trạng Endpoints, Services và DTOs
* **Endpoints hiện có trong toàn hệ thống:** Chỉ có duy nhất 4 endpoints hoạt động:
  1. `GET /api/health`
  2. `POST /api/v1/auth/register`
  3. `POST /api/v1/auth/login`
  4. `GET /api/v1/auth/me`
* **Services hiện có:** Chỉ có duy nhất `AuthService.java`. Toàn bộ các phân hệ nghiệp vụ `CandidateProfile`, `Taxonomy`, `Occupations`, `LMI`, `Recommendations` đều chưa có bất kỳ service nào.
* **Xử lý ngoại lệ (Exception Handling):**
  - [com.cnpm.careerrecommendation.exception.GlobalExceptionHandler](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/backend/src/main/java/com/cnpm/careerrecommendation/exception/GlobalExceptionHandler.java#L39-L43): Trả về `fieldError.getRejectedValue()`. Điều này gây rủi ro rò rỉ dữ liệu nhạy cảm (như mật khẩu thô người dùng nhập sai).
  - [com.cnpm.careerrecommendation.common.exception.GlobalExceptionHandler](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/backend/src/main/java/com/cnpm/careerrecommendation/common/exception/GlobalExceptionHandler.java#L33-L35): Đã chủ động che giấu `rejectedValue = null`.
  - Hai handler này có kiểu trả về khác nhau (`com.cnpm.careerrecommendation.dto.ErrorResponse` với `timestamp` kiểu String vs `com.cnpm.careerrecommendation.common.dto.ErrorResponse` với `timestamp` kiểu `Instant`).

#### 2.3. Quy tắc phân quyền theo Role (RBAC) & Kiểm thử
* **Cấu hình Security:** Trong [SecurityConfig.java](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/backend/src/main/java/com/cnpm/careerrecommendation/config/SecurityConfig.java#L52-L63), hệ thống CHƯA CÓ bất kỳ quy tắc phân quyền nào dựa trên role (không có `.hasRole("CANDIDATE")` hay `.hasRole("ADMIN")`). Mọi người dùng sau khi xác thực (dù là CANDIDATE) đều có thể gọi các API quản trị tương lai nếu không có `@PreAuthorize`.
* **Chất lượng kiểm thử:**
  - Chạy `mvn -f backend/pom.xml test`: **BUILD FAILURE**.
  - Tổng số test: 59. Số test lỗi: 18 (100% các test sử dụng `@WebMvcTest` đều lỗi do `DuplicateKeyException` trong `application.yml`).
  - Bộ tích hợp `AuthPostgresIT.java` (21 tests) được ẩn sau profile `-Ppostgres-it`, bắt buộc phải có database PostgreSQL thật cấu hình thủ công qua biến môi trường `AUTH_TEST_DATABASE_URL`, không thể chạy trong quy trình CI chuẩn tự động.

---

### 3. Thẩm định Cơ sở dữ liệu: DDL V1/V2, JPA Entities và Flyway State

#### 3.1. Đối chiếu 10 bảng DDL với JPA Entities

| Tên bảng CSDL | DDL Migration | Thực thể JPA | Đánh giá khớp nối | Tình trạng Service / Controller |
| :--- | :---: | :---: | :--- | :--- |
| `users` | `V1` + `V2` | `entity.User` & `module.auth.entity.User` | **Xung đột nghiêm trọng:** 2 class cùng map 1 bảng; `entity.User` thiếu cột `phone` của V2; kiểu ngày giờ không đồng nhất (`OffsetDateTime` vs `Instant`). | Đã có `AuthService` & `AuthController` |
| `candidate_profiles` | `V1` | `entity.CandidateProfile` | Khớp DDL, nhưng đang trỏ quan hệ `@OneToOne` vào `entity.User` cũ. | **Chưa có Service, Chưa có Controller** |
| `skill_categories` | `V1` | `entity.SkillCategory` | Khớp DDL (có index, unique code). | **Chưa có Service, Chưa có Controller** |
| `skills` | `V1` | `entity.Skill` | Khớp DDL (quan hệ Lazy ManyToOne với Category). | **Chưa có Service, Chưa có Controller** |
| `user_skills` | `V1` | `entity.UserSkill` | Khớp DDL (Unique constraint user_id + skill_id). Trỏ vào `entity.User` cũ. | **Chưa có Service, Chưa có Controller** |
| `occupation_groups` | `V1` | `entity.OccupationGroup` | Khớp DDL (unique code). | **Chưa có Service, Chưa có Controller** |
| `occupations` | `V1` | `entity.Occupation` | Khớp DDL (quan hệ Lazy ManyToOne với Group). | **Chưa có Service, Chưa có Controller** |
| `occupation_skills` | `V1` | `entity.OccupationSkill` | Khớp DDL (trọng số Numeric 3,2, constraint kiểm tra). | **Chưa có Service, Chưa có Controller** |
| `market_demand_indicators`| `V1` | `entity.MarketDemandIndicator`| Khớp DDL (Unique occupation + region + snapshot_date). | **Chưa có Service, Chưa có Controller** |
| `recommendation_logs` | `V1` | `entity.RecommendationLog` | Khớp DDL (ánh xạ JSONB bằng Hibernate 6 `@JdbcTypeCode(SqlTypes.JSON)`). | **Chưa có Service, Chưa có Controller** |

#### 3.2. Trạng thái Flyway Migration & Seed Data
* Script `V1__initial_schema.sql`: Khởi tạo 10 bảng sạch sẽ, đầy đủ khóa ngoại và chỉ mục (155 dòng).
* Script `V2__auth_phone_and_email_uniqueness.sql`: Bổ sung cột `phone` và chỉ mục chức năng `lower(email)`.
* **Seed Data:** Hoàn toàn **chưa có dữ liệu mẫu (0 bản ghi)**. Bảng danh mục kỹ năng, nghề nghiệp O*NET và chỉ số thị trường LMI đều rỗng, khiến việc chạy thử UI hay test recommendation là bất khả thi.

---

### 4. Thẩm định Frontend: Định tuyến, API Client, Auth State và Build

* **Routes & Layouts:** Không có bất kỳ route hay layout nào. `react-router-dom` chưa được cài đặt trong `package.json`.
* **API Client:** Không có thư viện `axios`, không có file cấu hình instance hay interceptor nào để bắt lỗi 401 hoặc đính kèm Bearer token. Biến môi trường `VITE_API_BASE_URL` (GAP-01) chưa được đưa vào sử dụng trong mã nguồn.
* **Auth / Token Handling & State Management:** Chưa có cơ chế lưu trữ token (localStorage/sessionStorage), không có React Context hay hook `useAuth()`.
* **Mock vs Wired:** 100% giao diện chưa được kết nối với Backend, và cũng không có mock data.
* **Kết quả thực thi lệnh kiểm tra Frontend:**
  - Lệnh `npm run build` (thực thi `tsc -b && vite build`): **Thành công (Exit 0)** với 32 module nhỏ giọt của Vite skeleton (tổng bundle JS chỉ 142 kB).
  - Không có script `npm run lint` hoặc `npm test` trong `package.json`. Không có ESLint được thiết lập.

---

### 5. Thẩm định Xác thực & An ninh hệ thống (Auth & Security)

1. **Cấu hình JWT:**
   - Sử dụng thuật toán HS256 (`io.jsonwebtoken:jjwt-api:0.12.6`).
   - Thời hạn token: Mặc định 24h (`86400000 ms`).
   - Token Payload: Có đủ `sub`, `userId`, `role`.
   - **Điểm khuyết:** Không có Refresh Token. Khi token 24h hết hạn, người dùng bắt buộc phải đăng nhập lại. Không có cơ chế thu hồi token (Revocation / Blacklist) khi người dùng đổi mật khẩu hoặc đăng xuất.
2. **Lỗ hổng bảo vệ nhầm URL:**
   - Tại `SecurityConfig.java`: Cấu hình `.requestMatchers("/api/v1/auth/**").permitAll()` vô hiệu hóa việc kiểm tra token trên toàn bộ cụm auth, bao gồm cả `/api/v1/auth/me`. Khi không có token, `identity` null gây 500 thay vì 401.
   - Endpoint `/api/test/**` vẫn đang mở `permitAll()` trong cấu hình chính của ứng dụng.
3. **Quy tắc mật khẩu:**
   - `@Utf8Password` kiểm soát tốt: độ dài 8–64 ký tự, tối đa 72 byte UTF-8 (ngăn chặn lỗi BCrypt truncation silent), cấm byte NUL `\0`.
4. **Cơ chế kiểm tra User mỗi request:**
   - [JwtAuthenticationFilter.java](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/backend/src/main/java/com/cnpm/careerrecommendation/common/security/JwtAuthenticationFilter.java#L55): Với MỖI request kèm Bearer token, filter lại thực hiện một truy vấn `users.findById(identity.userId())` vào CSDL. Mặc dù giúp cập nhật trạng thái user tức thì, điều này làm mất đi ưu thế không trạng thái (stateless) của JWT và sẽ là nút thắt cổ chai hiệu năng khi có nhiều người dùng.

---

### 6. Thẩm định DevOps & Môi trường chạy Docker Compose

* **Khởi chạy từ Clean Clone:** **THẤT BẠI**.
  - Không thể thực hiện `docker compose up` nếu chưa copy `.env.example` thành `.env`.
  - File `.env.example` thiết lập `JWT_SECRET=your_random_secret_key_at_least_32_utf8_bytes_here`. Chuỗi này sẽ bị backend từ chối ngay lập tức khi khởi động vì bắt đầu bằng tiền tố bị cấm `your_`.
* **Lỗi cấu hình Backend trong Docker:**
  - Do `application.yml` bị lỗi duplicate key `jackson`, backend image khi khởi động trong container sẽ tự thoát (crash loop) ngay lập tức.
* **Cấu hình Healthcheck & Phụ thuộc:**
  - `postgres` có healthcheck `pg_isready`.
  - `backend` KHÔNG có healthcheck trong `docker-compose.yml`.
  - `frontend` phụ thuộc vào `backend` theo điều kiện mặc định `service_started` thay vì `service_healthy`.
* **Độ chính xác của README:**
  - Hướng dẫn tại `README.md` và `docs/development/README.md` khẳng định dự án có thể chạy ngay với `docker compose up -d` và `mvn clean test` đều không đúng với trạng thái thực tế của mã nguồn hiện tại.

---

### 7. Đánh giá Backlog Tuần 2–5 và Các quyết định kỹ thuật GAP-01..GAP-04

#### 7.1. Hiện trạng các quyết định GAP
* **GAP-01 (Frontend Base URL `/api/v1`):** Chưa được áp dụng do Frontend chưa có API client.
* **GAP-02 (Backend Health Check `/api/health`):** Đã hiện thực tại `HealthController.java` (nhưng hiện không test được do lỗi YAML).
* **GAP-03 (Modular Monolith Packages):** Bị vi phạm nghiêm trọng (tồn tại song song 2 cấu trúc thư mục trái ngược nhau).
* **GAP-04 (JWT Claims Standards):** Đã tuân thủ trong code tạo token, nhưng chưa được ánh xạ vào phân quyền endpoint.

#### 7.2. Tình trạng Backlog Tuần 2–5 (`docs/planning/week-2-5-backlog.md`)
Kế hoạch Tuần 2 hiện tại trong tài liệu bị **Lệch pha (Mis-scoped)** và **Bị chặn (Blocked)** nghiêm trọng:
1. `W2-BE-SEC-01` (Bùi Vạn Khải): Kế hoạch yêu cầu viết Spring Security, BCrypt, JWT -> **Lỗi thời / Trùng lặp (Obsolete):** W1-T5 (Đoàn Việt Khang) đã viết phần lớn logic này. Bùi Vạn Khải cần tập trung sửa lỗi YAML, hợp nhất cấu trúc package và cấu hình RBAC chuẩn.
2. `W2-BE-PRF-01` (Phạm Ngọc Gia Khang): Kế hoạch yêu cầu tạo JPA Entities cho `candidate_profiles` và `user_skills` -> **Trùng lặp (Redundant):** W1-T4 (Huỳnh Long Bảo Khanh) đã tạo Entity và Repository rồi. Gia Khang chỉ cần viết DTO, Service và Controller.
3. `W2-BE-TAX-01` (Lương Phúc Khang): Kế hoạch yêu cầu tạo JPA Entities cho `skills` và `occupations` -> **Trùng lặp (Redundant):** Đã được tạo ở W1-T4. Lương Phúc Khang cần viết Service, Controller và nạp Seed Data.
4. `W2-FE-CND-01` & `W2-FE-ADM-01`: Cả hai tác vụ Frontend đều **Bị chặn (Blocked)** vì khung nền tảng Frontend (Router, Axios, Interceptor, Layout chung) chưa tồn tại từ Tuần 1.

---

### 8. Điểm nóng xung đột mã nguồn (Merge Conflict Hot Spots)

Nếu 6 thành viên bắt đầu Tuần 2 mà không có quy ước phân chia biên giới nghiêm ngặt, các file sau chắc chắn sẽ xảy ra xung đột Git (Merge Conflicts):
1. **`backend/src/main/resources/application.yml`:** Nơi mọi thành viên backend muốn thêm cấu hình cho module của mình.
2. **`backend/src/main/java/com/cnpm/careerrecommendation/config/SecurityConfig.java`:** Nơi các thành viên backend cùng muốn can thiệp để mở hoặc chặn URL endpoint.
3. **`GlobalExceptionHandler.java`:** Nơi các thành viên muốn thêm xử lý cho Exception nghiệp vụ riêng (`ProfileNotFoundException`, `OccupationNotFoundException`...).
4. **`entity/User.java` và `UserRepository.java`:** Đang bị nhân bản ở 2 nơi, cần một người duy nhất dọn dẹp và chuẩn hóa.
5. **Thư mục `db/migration/`:** Nguy cơ trùng lặp số phiên bản migration `V3__...` giữa người làm Seed Data và người làm thay đổi lược đồ CSDL.
6. **Frontend Router & API Client (`App.tsx`, `apiClient.ts`):** Cả 2 thành viên Frontend cùng cần đăng ký trang mới và khai báo endpoint.

---

## Báo cáo các hạng mục không thể chạy kiểm thử trực tiếp (Unrunnable Verifications)

1. **Khởi chạy Docker Compose (`docker compose up`):**
   - *Lệnh thực hiện:* `docker ps`
   - *Kết quả:* `failed to connect to the docker API at npipe:////./pipe/dockerDesktopLinuxEngine... The system cannot find the file specified.`
   - *Nguyên nhân:* Docker Desktop daemon hiện không bật trên môi trường máy chủ audit.
2. **Kiểm tra kết nối Live PostgreSQL & Xác thực Hibernate Validate:**
   - *Nguyên nhân:* Do Docker daemon chưa bật và `application.yml` bị lỗi cú pháp DuplicateKeyException, Spring Boot context không thể khởi tạo để kết nối CSDL và chạy xác thực Hibernate ddl validate.
3. **Gọi thử HTTP thực tế tới `GET /api/health`:**
   - *Nguyên nhân:* Ứng dụng Backend không thể khởi động thành công do lỗi phân tích cú pháp cấu hình YAML.
