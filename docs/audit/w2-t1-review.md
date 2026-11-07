# Báo Cáo Thẩm Định Độc Lập Nhánh `fix/w2-t1-gate` (W2-T1a / T1b / T1c / T1d)

* **Vai trò:** Independent Reviewer (Thẩm định độc lập, hoài nghi kỹ thuật)
* **Đối tượng thẩm định:** Nhánh `fix/w2-t1-gate` (commits `fb9756c`, `eed4648`, `95e50c6`, `9fd6b21`, `d6c3075`)
* **Ngày thực hiện:** 02/11/2026
* **Trạng thái nhánh sau T1d:** **READY TO MERGE** (Tất cả 28 checks PASS, 64 unit tests pass, 21/21 integration tests pass, Docker runtime pass).

---

## 1. Bảng Tổng Hợp Kết Quả Kiểm Tra (Verdict Table)

| Check # | Phân vùng (Area) | Trạng thái (Status) | Bằng chứng tóm tắt (One-line Evidence) |
| :---: | :--- | :---: | :--- |
| **1** | A. Per-commit greenness | **PASS** | Cả 5 commit đều exit code 0: `fb9756c` (59 pass), `eed4648` (63 pass), `95e50c6` (62 pass), `9fd6b21` (62 pass), `d6c3075` (64 pass). T1a xanh độc lập. |
| **2** | A. Patch isolation | **PASS** | T1a chỉ sửa `application.yml` + 2 vá tạm (`JwtAuthenticationFilter` bỏ `@Component`, `GlobalExceptionHandler` gán Order/Name). Log khớp thực tế. |
| **3** | B. Scope checking | **PASS** | Không có tệp nào thuộc `frontend/`, `docker*`, `.github/`, `scripts/`, `pom.xml`, `.env*`. Các tệp ngoài là tài liệu ADR/kế hoạch ở commit 4. |
| **4** | B. Migration integrity | **PASS** | `git diff main...fix/w2-t1-gate -- backend/src/main/resources/db/migration` rỗng; V1/V2 nguyên vẹn, không có migration mới. |
| **5** | B. Leak prevention | **PASS** | `AdminSmokeTestController` nằm duy nhất trong `src/test/java/.../module/auth/`; không có code test rò rỉ vào `src/main`. |
| **6** | C. Matcher security | **PASS** | Đã thu hẹp permitAll trong T1d (`GET /api/health`, `POST /api/v1/auth/login`, `POST /api/v1/auth/register`, `GET /api/v1/occupations/**`, `GET /api/v1/skills`). Không wildcard thừa. |
| **7** | C. Endpoint lockdown | **PASS** | `/api/v1/auth/me` bắt buộc authenticated; `/api/test/**` đã đóng hoàn toàn; `@EnableMethodSecurity(prePostEnabled = true)` xuất hiện đúng 1 lần. |
| **8** | C. Filter registration | **PASS** | Đã chuyển sang constructor injection bắt buộc (fail-fast), loại bỏ hoàn toàn `@Autowired(required=false)`. Filter đăng ký 1 lần trong Security chain. |
| **9** | C. Standard error JSON | **PASS** | 401 và 403 đều trả `ErrorResponse` JSON. Token thiếu/sai/hết hạn đều xử lý chuẩn. `AuthController.me` và `AuthService` đã chặn NPE. |
| **10** | C. Role consistency | **PASS** | Enum `ROLE_CANDIDATE`/`ROLE_ADMIN`, JWT claim `"role"`, `GrantedAuthority` và DTO trả về đều có tiền tố `ROLE_`. Không có double prefix hay bare CANDIDATE. |
| **11** | C. Secret & Hash safety | **PASS** | DTO không chứa passwordHash; không có JWT secret thật hay password bị commit vào git; test dùng dummy key 32+ bytes. |
| **12** | C. Secret validation | **PASS** | `JwtTokenProvider.java:22-25` chặn chuỗi rỗng, bắt đầu bằng `your_`/`default_` hoặc dưới 32 bytes; có unit test kiểm chứng. |
| **13** | D. Advice consolidation | **PASS** | `git grep "@RestControllerAdvice"` cho đúng 1 kết quả duy nhất tại `common.exception.GlobalExceptionHandler`. |
| **14** | D. Data masking & tests | **PASS** | Khôi phục test `shortPassword_Returns400WithMaskedRejectedValue` kiểm tra mật khẩu ngắn với assertion `rejectedValue` là `null`. |
| **15** | D. Exception mapping | **PASS** | `ApiException` và `ResourceNotFoundException` trả đúng mã HTTP và `errorCode`, định dạng `ErrorResponse` chuẩn khớp API Contract 1.2. |
| **16** | E. Entity vs DDL | **PASS** | `common.entity.User` khớp 100% cột, kiểu dữ liệu, nullability, unique index, cột `phone` (V2) và kiểu `Instant` (`timestamptz`). |
| **17** | E. Entity relocation | **PASS** | `Skill`, `SkillCategory` và các repository đã chuyển vào `common`; `PageResponse` có đủ `pageNumber`, `pageSize`, `totalElements`, `totalPages`, `last`. |
| **18** | E. Ref-only changes | **PASS** | Diff của `CandidateProfile`, `UserSkill`, `OccupationSkill`, `RecommendationLog` 100% chỉ là các dòng import; zero logic change. |
| **19** | E. Residual cleanup | **PASS** | `git grep` 0 kết quả cho các package phẳng cũ (`exception`, `dto`, `module.auth.entity`, `module.auth.repository`, `entity.User`). |
| **20** | E. Monolith boundaries | **PASS** | `common` không import từ `module.*`; không có module nào import chéo module khác (chỉ có duy nhất `module.auth`). |
| **21** | F. Test suite coverage | **PASS** | 64 tests chạy qua Maven Surefire exit 0; đầy đủ các kịch bản validation và auth security. |
| **22** | F. Security test matrix | **PASS** | Đầy đủ kịch bản: no token (401), invalid token (401), expired token (401), valid token (200), candidate on admin (403), POST skills without auth (401). |
| **23** | F. Test count audit | **PASS** | T1a: 59 pass; T1b: 63 pass; T1c: 62 pass; T1d: 64 pass. |
| **24** | G. Docker compose up | **PASS** | Khởi chạy Docker Compose với PostgreSQL và backend container. Flyway xác thực 2 migrations thành công, Hibernate validate 10/10 JPA entities. Cổng 8080 mở sạch sẽ. |
| **25** | G. Postgres IT profile | **PASS** | Chạy `mvn -Ppostgres-it test-compile failsafe:integration-test failsafe:verify`: 21/21 tests pass, 0 failures, 0 errors against real PostgreSQL. |
| **26** | G. Smoke curl runtime | **PASS** | 5-request curl smoke test thành công: no-token `/auth/me` (401), register (201), login (200), `/auth/me` with Bearer (200, không lộ hash), `/api/test/x` (401 đóng), GET `/api/v1/occupations` (200/404 không yêu cầu auth). |
| **27** | H. Result log honesty | **PASS** | `w2-t1-result.md` phản ánh trung thực toàn bộ quá trình, các thay đổi tạm và các bước khắc phục ở T1d. |
| **28** | H. Contract alignment | **PASS** | `api-contract.md` khớp hoàn toàn với code về endpoint path, HTTP status, Role string và cấu trúc DTO. |

---

## 2. Báo Cáo Chi Tiết Các Phát Hiện & Kết Quả Khắc Phục (T1d)

### 🔴 MAJOR FINDINGS (ĐÃ KHẮC PHỤC Ở T1d)

#### FINDING-01: Bộ so khớp `permitAll` rộng hơn quy định nghiêm ngặt của prompt
* **Vị trí khắc phục:** `SecurityConfig.java:76-81`
* **Kết quả:**
  ```java
  // Health check endpoint (GET only)
  .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
  // Public auth endpoints
  .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/register").permitAll()
  // Public taxonomy GETs
  .requestMatchers(HttpMethod.GET, "/api/v1/occupations/**", "/api/v1/skills").permitAll()
  // Role-based authorization
  .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
  .requestMatchers("/api/v1/profile/**", "/api/v1/recommendations/**").hasRole("CANDIDATE")
  // All other endpoints require authentication
  .anyRequest().authenticated()
  ```
  - `GET /api/health` được giới hạn phương thức GET. POST `/api/health` không có token trả HTTP 401.
  - Bỏ wildcard `"/api/v1/skills/**"`, chỉ cho phép `GET /api/v1/skills`. Đã thêm test `postSkillsWithoutToken_Returns401Json()` trong `AuthSecurityTest`.

#### FINDING-02: Cơ chế `@Autowired(required = false)` trên các thành phần bảo mật cốt lõi
* **Vị trí khắc phục:** `SecurityConfig.java:46-63`
* **Kết quả:** Chuyển sang tiêm constructor bắt buộc cho `JwtTokenProvider`, `UserRepository`, `SecurityErrorHandler`. Ứng dụng fail-fast ngay khi khởi động nếu thiếu bất kỳ bean bảo mật nào. Đồng thời cấu hình `@Bean UserDetailsService` và `@Bean AuthenticationManager` trong `SecurityConfig` để cung cấp đầy đủ bean cho runtime.

---

### 🟡 MINOR FINDINGS (ĐÃ KHẮC PHỤC Ở T1d)

#### FINDING-03: Thất thoát kiểm thử validation trường mật khẩu
* **Vị trí khắc phục:** `GlobalExceptionHandlerTest.java`
* **Kết quả:** Khôi phục test `shortPassword_Returns400WithMaskedRejectedValue` kiểm tra chuỗi mật khẩu ngắn `"short"`, xác minh `rejectedValue` được mask thành `null` trong mảng `errors`.

#### FINDING-04: Cấu hình BCrypt cost 12
* **Vị trí khắc phục:** `SecurityConfig.java:131`
* **Kết quả:** Đã chuyển `new BCryptPasswordEncoder()` thành `new BCryptPasswordEncoder(12)` khớp với coding conventions, tài liệu W1-T5 và yêu cầu kiểm thử của `AuthPostgresIT`.

---

## 3. Bằng Chứng Thực Nghiệm Runtime (Checks 24, 25, 26)

### 3.1. Docker Compose Container Logs (Check 24)
```text
2026-11-02T15:36:53.117Z  INFO 1 --- [career-recommendation-system] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://postgres:5432/career_recommendation_db (PostgreSQL 16.15)
2026-11-02T15:36:53.176Z  INFO 1 --- [career-recommendation-system] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 2 migrations (execution time 00:00.037s)
2026-11-02T15:36:53.211Z  INFO 1 --- [career-recommendation-system] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": 2
2026-11-02T15:36:53.214Z  INFO 1 --- [career-recommendation-system] [           main] o.f.core.internal.command.DbMigrate      : Schema "public" is up to date. No migration necessary.
2026-11-02T15:36:54.561Z  INFO 1 --- [career-recommendation-system] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-11-02T15:36:55.468Z  INFO 1 --- [career-recommendation-system] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path '/'
2026-11-02T15:36:55.476Z  INFO 1 --- [career-recommendation-system] [           main] c.c.c.CareerRecommendationApplication    : Started CareerRecommendationApplication in 4.552 seconds
```

### 3.2. Failsafe Integration Tests Against PostgreSQL (Check 25)
Lệnh thực thi:
`$env:AUTH_TEST_DATABASE_URL="jdbc:postgresql://localhost:5432/career_recommendation_db"; mvn -f backend/pom.xml -Ppostgres-it test-compile failsafe:integration-test failsafe:verify`
Kết quả:
```text
[INFO] Running com.cnpm.careerrecommendation.module.auth.AuthPostgresIT
[INFO] Tests run: 21, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 9.970 s -- in com.cnpm.careerrecommendation.module.auth.AuthPostgresIT
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 21, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] --- failsafe:3.2.5:verify (default-cli) @ career-recommendation ---
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### 3.3. Curl Smoke Test 5 Requests (Check 26)
1. **GET `/api/health`:** Trả về HTTP 200 `{"status":"UP"}`.
2. **GET `/api/v1/auth/me` không có token:**
   ```text
   HTTP/1.1 401 
   WWW-Authenticate: Bearer
   {"timestamp":"2026-11-02T15:47:57.716626978Z","status":401,"error":"Unauthorized","message":"Yêu cầu token Bearer hợp lệ","path":"/api/v1/auth/me","errors":[]}
   ```
3. **POST `/api/v1/auth/register`:** Trả về HTTP 201:
   ```text
   HTTP/1.1 201 
   {"id":21,"email":"smokeuser1@example.com","fullName":"Smoke User","role":"ROLE_CANDIDATE","status":"ACTIVE","createdAt":"2026-11-02T15:48:45.826115583Z"}
   ```
4. **POST `/api/v1/auth/login`:** Trả về HTTP 200 kèm JWT accessToken có claim `role: "ROLE_CANDIDATE"`, `expiresIn: 86400`.
5. **GET `/api/v1/auth/me` với Bearer token:** Trả về HTTP 200:
   ```text
   HTTP/1.1 200 
   {"id":21,"email":"smokeuser1@example.com","fullName":"Smoke User","phone":"0901234567","role":"ROLE_CANDIDATE","status":"ACTIVE","createdAt":"2026-11-02T15:48:45.826116Z"}
   ```
   *(Không chứa trường `password` hay `passwordHash`)*.
6. **GET `/api/test/x`:** Trả về HTTP 401 Unauthorized (đã đóng hoàn toàn).
7. **GET `/api/v1/occupations` & `/api/v1/skills`:** Trả về HTTP 404 (do controller chưa code, nhưng đi qua Spring Security không bị 401).
8. **POST `/api/v1/skills` không token:** Trả về HTTP 401 Unauthorized.

---

## 4. Kết Luận Khả Năng Merge

Nhánh `fix/w2-t1-gate` sau commit `d6c3075` (W2-T1d) đạt trạng thái **SAFE TO MERGE 100%**:
- Không còn bất kỳ Major hay Minor finding nào chưa giải quyết.
- Xác thực thành công trên CSDL thật PostgreSQL và Docker Compose runtime.
- Không sửa đổi bất kỳ tệp nào ngoài Scope.
- Đủ điều kiện tạo PR, gửi Tech Lead / Reviewer Thông duyệt và tiến hành dỡ bỏ đóng băng mã nguồn (unfreeze notice).
