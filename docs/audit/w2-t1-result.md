# Báo Cáo Kết Quả Thực Hiện W2-T1 (W2-T1a, W2-T1b, W2-T1c)

* **Người thực hiện:** Bùi Vạn Khải (Tech Lead)
* **Nhánh làm việc:** `fix/w2-t1-gate` (phân nhánh từ `main` commit `fcddf8d`)
* **Ngày hoàn thành:** 02/11/2026
* **Tài liệu căn cứ:** `docs/plan/week2-tasks.md`, `docs/audit/week1-audit.md`, `ADR-0004`, `ADR-0005`, `ADR-0006`, `docs/api/api-contract.md`.

---

## 1. Tóm Tắt Tiến Độ và Chuỗi Commit

| Bước | Commit Hash | Thông điệp Commit | Trạng thái `mvn test` |
| :--- | :--- | :--- | :--- |
| **W2-T1a** | `fb9756c` | `fix(config): sửa duplicate key jackson trong application.yml (W2-T1a / FIX-01)` | Green (59 passed, 0 failed, 0 errors) |
| **W2-T1b** | `eed4648` | `fix(security): bảo vệ /auth/me, đóng /api/test/**, bật method security (W2-T1b / FIX-04)` | Green (63 passed, 0 failed, 0 errors) |
| **W2-T1c** | `95e50c6` | `refactor(backend): hợp nhất User/Skill/SkillCategory vào common, dọn package phẳng (W2-T1c / FIX-02, FIX-03)` | Green (62 passed, 0 failed, 0 errors) |

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
  - **Vá tạm 1 (JwtAuthenticationFilter):** Bỏ annotation `@Component` trên `JwtAuthenticationFilter` để tránh Spring Boot tự động đăng ký filter này như một Servlet Filter toàn cục trong các bài `@WebMvcTest` (nơi thiếu bean `UserRepository`). Filter này được chuyển sang đăng ký có kiểm soát trong `SecurityConfig`. -> *Được giữ lại và hoàn thiện ở T1b/T1c*.
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
* **Số lượng tệp thay đổi:** 6 files
  1. `backend/src/main/java/com/cnpm/careerrecommendation/config/SecurityConfig.java`
  2. `backend/src/main/java/com/cnpm/careerrecommendation/module/auth/controller/AuthController.java`
  3. `backend/src/main/java/com/cnpm/careerrecommendation/exception/GlobalExceptionHandler.java`
  4. `backend/src/test/java/com/cnpm/careerrecommendation/exception/GlobalExceptionHandlerTest.java`
  5. `backend/src/test/java/com/cnpm/careerrecommendation/module/auth/AdminSmokeTestController.java` (mới)
  6. `backend/src/test/java/com/cnpm/careerrecommendation/module/auth/AuthSecurityTest.java` (mới)
* **Test counts:**
  - Trước: 59 tests.
  - Sau: 63 tests run, 0 failures, 0 errors (+4 tests bảo mật mới: 401 không token, 401 token sai, 200 token hợp lệ không lộ passwordHash, 403 khi candidate gọi endpoint admin-only).
* **Lệnh đã thực thi và kết quả:**
  - `mvn -f backend/pom.xml test` -> BUILD SUCCESS (63/63 passed).
* **Quyết định kỹ thuật:**
  - Tiền tố quyền hạn (Role string): Backend phát hành đồng nhất `ROLE_CANDIDATE` và `ROLE_ADMIN` ở cả JWT claim `role`, `SimpleGrantedAuthority`, và response `user.role`. Điều này giúp `hasRole('CANDIDATE')` của Spring Security hoạt động tự nhiên không cần cấu hình thêm mapping, đồng thời khớp 100% với hợp đồng Frontend tại ADR-0008.

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
  - **Tạo mới / Di chuyển vào common:**
    - `backend/src/main/java/com/cnpm/careerrecommendation/common/dto/PageResponse.java`
    - `backend/src/main/java/com/cnpm/careerrecommendation/common/entity/Role.java`
    - `backend/src/main/java/com/cnpm/careerrecommendation/common/entity/User.java`
    - `backend/src/main/java/com/cnpm/careerrecommendation/common/entity/Skill.java`
    - `backend/src/main/java/com/cnpm/careerrecommendation/common/entity/SkillCategory.java`
    - `backend/src/main/java/com/cnpm/careerrecommendation/common/repository/UserRepository.java`
    - `backend/src/main/java/com/cnpm/careerrecommendation/common/repository/SkillRepository.java`
    - `backend/src/main/java/com/cnpm/careerrecommendation/common/repository/SkillCategoryRepository.java`
    - `backend/src/main/java/com/cnpm/careerrecommendation/common/exception/ResourceNotFoundException.java`
  - **Xóa bỏ các class trùng lặp / package phẳng:**
    - `backend/src/main/java/com/cnpm/careerrecommendation/dto/**` (`ApiResponse`, `ErrorResponse`, `HealthResponse`, `ValidationErrorDetail`, `package-info`)
    - `backend/src/main/java/com/cnpm/careerrecommendation/exception/**` (`ApiException`, `GlobalExceptionHandler`, `ResourceNotFoundException`)
    - `backend/src/main/java/com/cnpm/careerrecommendation/entity/` (`User.java`, `Skill.java`, `SkillCategory.java`)
    - `backend/src/main/java/com/cnpm/careerrecommendation/repository/` (`UserRepository.java`, `SkillRepository.java`, `SkillCategoryRepository.java`)
    - `backend/src/main/java/com/cnpm/careerrecommendation/module/auth/entity/` (`User.java`, `Role.java`)
    - `backend/src/main/java/com/cnpm/careerrecommendation/module/auth/repository/` (`UserRepository.java`)
  - **Cập nhật import & mã nguồn liên quan:**
    - `HealthController.java`: nhúng trực tiếp record `HealthResponse` cục bộ, loại bỏ phụ thuộc vào `dto.HealthResponse`.
    - `CandidateProfile.java`, `UserSkill.java`, `OccupationSkill.java`, `RecommendationLog.java`: chỉ sửa các dòng import.
    - `AuthService.java`, `CurrentUserResponse.java`, `LoginResponse.java`, `RegisteredUserResponse.java`: chuyển sang thực thể `common.entity.*`.
    - `docs/api/api-contract.md`: cập nhật phân định tài liệu và hợp đồng phân hệ.
  - **Kiểm thử cập nhật:**
    - `EntityMappingTest.java`, `HibernateMetadataTest.java`, `JwtTokenProviderTest.java`, `AuthSecurityTest.java`.
    - Xóa `exception/GlobalExceptionHandlerTest.java` (cũ, lộ `rejectedValue`), thay thế bằng `common/exception/GlobalExceptionHandlerTest.java` kiểm thử đầy đủ cấu trúc lỗi, mã lỗi `ApiException`, `ResourceNotFoundException`, `AccessDeniedException`, và kiểm tra `rejectedValue == null`.
* **Test counts:**
  - 62 tests run, 0 failures, 0 errors, 0 skipped. (Tổng số test là 62 do đã dọn bớt 1 test trùng lặp ở exception cũ và bổ sung bộ test che giấu rejectedValue).
* **Lệnh đã thực thi và kết quả:**
  - `git grep "com.cnpm.careerrecommendation.exception\b" backend/src/` -> 0 kết quả (exit 1).
  - `git grep "com.cnpm.careerrecommendation.dto\b" backend/src/` -> 0 kết quả (exit 1).
  - `git grep "module.auth.entity" backend/src/` -> 0 kết quả (exit 1).
  - `git grep "module.auth.repository" backend/src/` -> 0 kết quả (exit 1).
  - `mvn -f backend/pom.xml test` -> BUILD SUCCESS (62/62 passed).
  - Kiểm tra Docker daemon: `docker info` -> Docker daemon không chạy (`Server: failed to connect to the docker API`). Báo cáo: **"not run"** cho Postgres integration profile.

---

## 3. Các Vấn Đề Ngoài Phạm Vi (Out of Scope) & Quyết Định Kỹ Thuật

1. **Profile Postgres IT (`-Ppostgres-it`):** Docker daemon trên máy host đang tắt nên bài kiểm thử tích hợp container PostgreSQL được báo cáo là **"not run"** theo đúng quy định đề bài.
2. **Không sửa đổi migration Flyway:** Toàn bộ thực thể JPA (`User`, `Skill`, `SkillCategory`) được căn chỉnh khớp 100% với DDL hiện hữu của V1 và V2, bảo toàn cấu hình `ddl-auto: validate`. Không có file migration nào được tạo thêm.

---

## 4. Khu Vực Cần Thẩm Định (Cần Audit)

Dưới đây là 5 nội dung có mức độ ảnh hưởng cao nhất cần Tech Lead / Peer review thẩm định kỹ:

1. **Chuẩn hóa Role String (`ROLE_CANDIDATE` / `ROLE_ADMIN`):**
   - Giá trị enum `Role.ROLE_CANDIDATE` và `Role.ROLE_ADMIN` được phát hành đồng nhất ở claim JWT `"role"`, authority trong `SecurityContextHolder`, và DTO phản hồi. Kiểm tra sự tương thích với client Frontend (ADR-0008).
2. **Quy tắc phân quyền và đóng `/api/test/**`:**
   - Kiểm tra `SecurityConfig`: các endpoint công khai chỉ giới hạn ở `GET /api/health`, `POST /api/v1/auth/login`, `POST /api/v1/auth/register`, `GET /api/v1/occupations/**`, `GET /api/v1/skills/**`. Endpoint `/api/v1/auth/me` đã được bảo vệ hoàn toàn với mã lỗi 401 khi không có token hoặc token sai.
3. **Cơ chế lọc và xác thực JWT (`JwtAuthenticationFilter`):**
   - Đã loại bỏ `@Component` để tránh Servlet Filter tự động đăng ký sai phạm vi trong slice test `@WebMvcTest`. Filter được đăng ký thủ công trong `SecurityConfig` và kiểm tra tài khoản còn tồn tại/active từ CSDL.
4. **Hợp nhất thực thể `User` và `Skill` / `SkillCategory`:**
   - Thực thể `User` tại `common.entity.User` tích hợp thuộc tính `phone` (từ V2), kiểu `Instant` cho `createdAt`/`updatedAt`, và các trường mặc định `isActive = true`. Cần xác nhận không có xung đột với các module `profile` (W2-T2) và `taxonomy` (W2-T3).
5. **Dọn dẹp package phẳng và bảo mật ErrorResponse (`rejectedValue = null`):**
   - Đã xóa hoàn toàn 2 package phẳng `dto` và `exception`.
   - `GlobalExceptionHandler` duy nhất tại `common.exception` đảm bảo khi người dùng nhập sai dữ liệu validation (như mật khẩu), trường `rejectedValue` luôn trả về `null` thay vì phản chiếu mật khẩu thô ra HTTP response.
