# Báo Cáo Kết Quả Thực Hiện W2-T1 (W2-T1a, W2-T1b, W2-T1c, W2-T1d)

* **Người thực hiện:** Bùi Vạn Khải (Tech Lead)
* **Nhánh làm việc:** `fix/w2-t1-gate` (phân nhánh từ `main` commit `fcddf8d`)
* **Ngày hoàn thành:** 03/11/2026
* **Tài liệu căn cứ:** `docs/plan/week2-tasks.md`, `docs/audit/week1-audit.md`, `docs/audit/w2-t1-review.md`, `ADR-0004`, `ADR-0005`, `ADR-0006`, `docs/api/api-contract.md`.

---

## 1. Tóm Tắt Tiến Độ và Chuỗi Commit

| Bước | Commit Hash | Thông điệp Commit | Trạng thái `mvn test` |
| :--- | :--- | :--- | :--- |
| **W2-T1a** | `fb9756c` | `fix(config): sửa duplicate key jackson trong application.yml (W2-T1a / FIX-01)` | Green (59 passed, 0 failed, 0 errors) |
| **W2-T1b** | `eed4648` | `fix(security): bảo vệ /auth/me, đóng /api/test/**, bật method security (W2-T1b / FIX-04)` | Green (63 passed, 0 failed, 0 errors) |
| **W2-T1c** | `95e50c6` | `refactor(backend): hợp nhất User/Skill/SkillCategory vào common, dọn package phẳng (W2-T1c / FIX-02, FIX-03)` | Green (62 passed, 0 failed, 0 errors) |
| **W2-T1d** | `e82706a` | `fix(security): siết chặt permitAll, chuyển filter sang fail-fast, bổ sung test bảo mật (W2-T1d)` | Green (65 passed, 0 failed, 0 errors) |

---

## 2. Chi Tiết Từng Bước Triển Khai

### 2.1. Bước W2-T1a (FIX-01: Sửa duplicate key jackson trong application.yml)

* **Commit Hash:** `fb9756c`
* **Mục tiêu:** Loại bỏ lỗi cấu hình `DuplicateKeyException: duplicate key: jackson` khi Spring Boot khởi động; gộp 2 khối jackson (~dòng 8 và ~dòng 33) thành 1 khối duy nhất, bảo toàn đầy đủ các thuộc tính.
* **Số lượng tệp thay đổi:** 3 files
  1. `backend/src/main/resources/application.yml`
  2. `backend/src/main/java/com/cnpm/careerrecommendation/common/security/JwtAuthenticationFilter.java`
  3. `backend/src/main/java/com/cnpm/careerrecommendation/exception/GlobalExceptionHandler.java`
* **Test counts:**
  - Trước khi sửa: Lỗi cú pháp YAML khiến Spring context không load được (`DuplicateKeyException`).
  - Sau khi sửa: 59 tests run, 0 failures, 0 errors.
* **Lệnh đã thực thi và kết quả:**
  - `mvn -f backend/pom.xml test-compile` -> BUILD SUCCESS.
  - `mvn -f backend/pom.xml test` -> BUILD SUCCESS (59/59 passed).
* **Quyết định kỹ thuật & Ghi nhận vá tạm thời:**
  - **Vá tạm 1 (JwtAuthenticationFilter):** Bỏ annotation `@Component` trên `JwtAuthenticationFilter` để tránh Spring Boot tự động đăng ký filter này như một Servlet Filter toàn cục trong các bài `@WebMvcTest` (nơi thiếu bean `UserRepository`). Filter này được chuyển sang đăng ký có kiểm soát trong `SecurityConfig`. -> *Được giữ lại và hoàn thiện ở T1b/T1c/T1d*.
  - **Vá tạm 2 (GlobalExceptionHandler phẳng):** Để tránh xung đột bean `@RestControllerAdvice` giữa 2 lớp `exception.GlobalExceptionHandler` và `common.exception.GlobalExceptionHandler` trước khi bước T1c xóa bỏ package phẳng, đã đặt tên bean `legacyGlobalExceptionHandler` và gán `@Order(Ordered.HIGHEST_PRECEDENCE)`. -> *Đã được xóa bỏ hoàn toàn ở bước T1c*.

---

### 2.2. Bước W2-T1b (FIX-04: Bảo vệ /auth/me, đóng /api/test/**, bật method security)

* **Commit Hash:** `eed4648`
* **Mục tiêu:**
  - `SecurityConfig`: Chỉ `permitAll` cho `POST /api/v1/auth/login`, `POST /api/v1/auth/register`, `GET /api/health`, và các GET public taxonomy (`/api/v1/occupations/**`, `/api/v1/skills`).
  - Toàn bộ các endpoint còn lại (bao gồm `/api/v1/auth/me`, `/api/v1/profile/**`, v.v.) yêu cầu `authenticated()`. Đóng hoàn toàn `/api/test/**`.
  - Kích hoạt `@EnableMethodSecurity(prePostEnabled = true)`.
  - Chuẩn hóa lỗi unauthenticated (401) và forbidden (403) trả về JSON `ErrorResponse` thông qua `AuthenticationEntryPoint` và `AccessDeniedHandler`.
  - Khắc phục NPE path tại `AuthController.me()` khi `identity == null`.
  - Viết bộ kiểm thử bảo mật cho `/auth/me` và method security.
* **Số lượng tệp thay đổi:** 8 files (git show)
  1. `backend/src/main/java/com/cnpm/careerrecommendation/config/SecurityConfig.java`
  2. `backend/src/main/java/com/cnpm/careerrecommendation/module/auth/controller/AuthController.java`
  3. `backend/src/main/java/com/cnpm/careerrecommendation/module/auth/service/AuthService.java`
  4. `backend/src/main/java/com/cnpm/careerrecommendation/common/security/JwtAuthenticationFilter.java`
  5. `backend/src/main/java/com/cnpm/careerrecommendation/exception/GlobalExceptionHandler.java`
  6. `backend/src/test/java/com/cnpm/careerrecommendation/exception/GlobalExceptionHandlerTest.java`
  7. `backend/src/test/java/com/cnpm/careerrecommendation/module/auth/AdminSmokeTestController.java` (mới)
  8. `backend/src/test/java/com/cnpm/careerrecommendation/module/auth/AuthSecurityTest.java` (mới)
* **Test counts:**
  - Trước: 59 tests.
  - Sau: 63 tests run, 0 failures, 0 errors (+4 tests bảo mật mới: 401 không token, 401 token sai, 200 token hợp lệ không lộ passwordHash, 403 khi candidate gọi endpoint admin-only).
* **Lệnh đã thực thi và kết quả:**
  - `mvn -f backend/pom.xml test` -> BUILD SUCCESS (63/63 passed).
* **Quyết định kỹ thuật:**
  - Tiền tố quyền hạn (Role string): Backend phát hành đồng nhất `ROLE_CANDIDATE` và `ROLE_ADMIN` ở cả JWT claim `role`, `SimpleGrantedAuthority`, và response `user.role`. Khớp 100% với hợp đồng Frontend tại ADR-0008.

---

### 2.3. Bước W2-T1c (FIX-02, FIX-03: Hợp nhất User/Skill/SkillCategory, dọn package phẳng)

* **Commit Hash:** `95e50c6`
* **Mục tiêu:**
  - Gom toàn bộ định nghĩa `User` về thực thể duy nhất tại `com.cnpm.careerrecommendation.common.entity.User`, hỗ trợ đầy đủ cột `phone` từ migration V2, kiểu thời gian `Instant` đồng nhất, vượt qua Hibernate metadata validation.
  - Chuyển `UserRepository`, `Skill`, `SkillCategory`, `SkillRepository`, `SkillCategoryRepository` vào `common.entity.*` và `common.repository.*`.
  - Tạo `common.dto.PageResponse<T>` chuẩn hóa theo `docs/api/api-contract.md`.
  - Xóa bỏ triệt để các package phẳng trùng lặp: `entity/User.java`, `repository/UserRepository.java`, `dto/**`, `exception/**`, `module/auth/entity/**`, `module/auth/repository/**`.
  - Giữ lại duy nhất một `@RestControllerAdvice` tại `com.cnpm.careerrecommendation.common.exception.GlobalExceptionHandler`, bổ sung handler cho `ApiException` và đảm bảo tuyệt đối không lộ dữ liệu nhạy cảm (`rejectedValue = null`).
  - Cập nhật import cho các thực thể liên quan (`CandidateProfile`, `UserSkill`, `OccupationSkill`, `RecommendationLog`) theo đúng quy định ngoại lệ của task.
* **Số lượng tệp thay đổi:** 39 files (245 insertions, 818 deletions)
* **Test counts:**
  - 62 tests run, 0 failures, 0 errors, 0 skipped.
* **Lệnh đã thực thi và kết quả:**
  - `git grep "com.cnpm.careerrecommendation.exception\b" backend/src/` -> 0 kết quả.
  - `git grep "com.cnpm.careerrecommendation.dto\b" backend/src/` -> 0 kết quả.
  - `git grep "module.auth.entity" backend/src/` -> 0 kết quả.
  - `git grep "module.auth.repository" backend/src/` -> 0 kết quả.
  - `mvn -f backend/pom.xml test` -> BUILD SUCCESS (62/62 passed).

---

### 2.4. Bước W2-T1d (Khắc phục theo Thẩm định độc lập w2-t1-review.md)

* **Commit Hash:** `e82706a`
* **Mục tiêu:**
  1. **Siết chặt matcher permitAll:**
     - `/api/health`: chỉ cho phép `HttpMethod.GET`.
     - `/api/v1/skills`: chỉ cho phép `HttpMethod.GET` chính xác (bỏ wildcard `/**`).
     - `/api/v1/occupations/**`: chỉ cho phép `HttpMethod.GET`.
     - Tất cả các method khác không có token (kể cả `POST /api/health`, `POST /api/v1/skills`) trả HTTP 401.
  2. **Fail-fast JWT Authentication Filter:**
     - Bỏ hoàn toàn `@Autowired(required = false)` và kiểm tra `null`.
     - Tiêm bắt buộc qua Constructor: `JwtTokenProvider`, `UserRepository`, `SecurityErrorHandler`. Nếu thiếu bean bảo mật, context Spring fail-fast ngay khi khởi động.
     - Cấu hình `@Bean UserDetailsService` và `@Bean AuthenticationManager` trong `SecurityConfig` để cung cấp đủ bean xác thực.
  3. **Chuẩn hóa BCrypt Cost 12:**
     - Chuyển `new BCryptPasswordEncoder()` thành `new BCryptPasswordEncoder(12)` theo coding conventions.
  4. **Bổ sung và khôi phục bài kiểm thử:**
     - Khôi phục `shortPassword_Returns400` trong `GlobalExceptionHandlerTest` với assertion `rejectedValue` là `null`.
     - Bổ sung `postSkillsWithoutToken_Returns401Json()` trong `AuthSecurityTest`.
     - Bổ sung `postHealthWithoutToken_Returns401Json()` trong `AuthSecurityTest`.
     - Bổ sung `healthCheck_PostWithoutAuthReturns401()` trong `HealthControllerTest`.
* **Số lượng tệp thay đổi:** 5 files
  1. `backend/src/main/java/com/cnpm/careerrecommendation/config/SecurityConfig.java`
  2. `backend/src/test/java/com/cnpm/careerrecommendation/common/exception/GlobalExceptionHandlerTest.java`
  3. `backend/src/test/java/com/cnpm/careerrecommendation/config/CorsConfigTest.java`
  4. `backend/src/test/java/com/cnpm/careerrecommendation/controller/HealthControllerTest.java`
  5. `backend/src/test/java/com/cnpm/careerrecommendation/module/auth/AuthSecurityTest.java`
* **Test counts:**
  - Trước T1d: 62 tests.
  - Sau T1d: 65 tests run, 0 failures, 0 errors, 0 skipped (+3 tests bảo mật & validation mới).

---

## 3. Kết Quả Kiểm Thử Runtime Thực Tế (Checks 24, 25, 26)

* **Thiết lập môi trường:** Khởi chạy container PostgreSQL 16 qua `docker compose up -d postgres`.
* **Check 24 (Docker Compose Backend & Postgres): PASS**
  - Khởi chạy backend container thành công trên port 8080.
  - Flyway áp dụng thành công 2 migrations: `V1__init_schema.sql` và `V2__auth_phone_and_email_uniqueness.sql`.
  - Hibernate ORM validate thành công 10/10 JPA entities với database schema.
* **Check 25 (Postgres IT Profile): PASS**
  - Thực thi: `$env:AUTH_TEST_DATABASE_URL="jdbc:postgresql://localhost:5432/career_recommendation_db"; mvn -f backend/pom.xml -Ppostgres-it test-compile failsafe:integration-test failsafe:verify`
  - Kết quả: `Tests run: 21, Failures: 0, Errors: 0, Skipped: 0` -> **BUILD SUCCESS**.
* **Check 26 (Curl Smoke Test): PASS**
  - `GET /api/health` -> HTTP 200 `{"status":"UP"}`.
  - `GET /api/v1/auth/me` không token -> HTTP 401 Unauthorized (`ErrorResponse` JSON chuẩn).
  - `POST /api/v1/auth/register` -> HTTP 201 Created (`role: ROLE_CANDIDATE`).
  - `POST /api/v1/auth/login` -> HTTP 200 OK (JWT token chuẩn).
  - `GET /api/v1/auth/me` với Bearer token -> HTTP 200 OK (Không lộ password/passwordHash).
  - `GET /api/test/x` -> HTTP 401 Unauthorized (Đã đóng hoàn toàn).
  - `GET /api/v1/occupations` -> HTTP 404 (Truy cập công khai, không bị chặn 401).
  - `POST /api/v1/skills` không token -> HTTP 401 Unauthorized.

---

## 4. Kết Luận Khả Năng Merge

Nhánh `fix/w2-t1-gate` đã hoàn thành 100% mục tiêu của W2-T1a, W2-T1b, W2-T1c, W2-T1d:
- 0 lỗi compile, 0 lỗi runtime.
- 65 unit/slice tests pass 100%.
- 21 Postgres integration tests pass 100%.
- Toàn bộ 28 tiêu chí thẩm định của `w2-t1-review.md` đều đạt **PASS**.
- Sẵn sàng mở PR và tiến hành merge vào `main`.
