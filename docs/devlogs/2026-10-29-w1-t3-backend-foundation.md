# DevLog: Triển khai nền tảng Backend Spring Boot — Task W1-T3

* **Mã tác vụ / Task ID:** W1-T3
* **Người thực hiện / Agent:** Phạm Ngọc Gia Khang (24520765) — hỗ trợ bởi Antigravity AI
* **Ngày thực hiện (Date):** 2026-10-29
* **Nhánh Git (Branch):** `main`

---

## 1. Mục tiêu (Objective)

Xây dựng nền tảng Backend Spring Boot hoàn chỉnh cho hệ thống Career Recommendation, đáp ứng đầy đủ 7 tiêu chí nghiệm thu của Task W1-T3:
- Ứng dụng build thành công và khởi chạy được.
- Endpoint `GET /api/health` trả về HTTP 200.
- Xử lý lỗi và response tuân theo định dạng đã thống nhất tại [`docs/api/api-contract.md`](file:///d:/UIT-2026_2027_HK1/CNPM%20Chuyen%20Sau/DoAn/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/api/api-contract.md) mục 1.2.
- Validation hoạt động đúng với dữ liệu hợp lệ và không hợp lệ.
- CORS được cấu hình phù hợp với frontend (`http://localhost:3000`, `http://localhost:5173`).
- Cấu hình môi trường local hoạt động.
- Các module có thể tích hợp theo hợp đồng API chung.

---

## 2. Thay đổi thực tế (Changes)

### Mã nguồn (Source Code):

**DTOs — Chuẩn hóa response theo API Contract:**
* [`backend/src/main/java/com/cnpm/careerrecommendation/dto/ErrorResponse.java`](file:///d:/UIT-2026_2027_HK1/CNPM%20Chuyen%20Sau/DoAn/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/backend/src/main/java/com/cnpm/careerrecommendation/dto/ErrorResponse.java): Java Record biểu diễn cấu trúc lỗi chuẩn khớp 100% với API Contract 1.2 (`timestamp`, `status`, `error`, `message`, `path`, `errors`).
* [`backend/src/main/java/com/cnpm/careerrecommendation/dto/ValidationErrorDetail.java`](file:///d:/UIT-2026_2027_HK1/CNPM%20Chuyen%20Sau/DoAn/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/backend/src/main/java/com/cnpm/careerrecommendation/dto/ValidationErrorDetail.java): Java Record chứa chi tiết lỗi từng trường (`field`, `rejectedValue`, `reason`).
* [`backend/src/main/java/com/cnpm/careerrecommendation/dto/ApiResponse.java`](file:///d:/UIT-2026_2027_HK1/CNPM%20Chuyen%20Sau/DoAn/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/backend/src/main/java/com/cnpm/careerrecommendation/dto/ApiResponse.java): Generic response wrapper cho response thành công.
* [`backend/src/main/java/com/cnpm/careerrecommendation/dto/HealthResponse.java`](file:///d:/UIT-2026_2027_HK1/CNPM%20Chuyen%20Sau/DoAn/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/backend/src/main/java/com/cnpm/careerrecommendation/dto/HealthResponse.java): Java Record cho endpoint health check (`{"status":"UP"}`).

**Exception Handling — Xử lý lỗi tập trung:**
* [`backend/src/main/java/com/cnpm/careerrecommendation/exception/ApiException.java`](file:///d:/UIT-2026_2027_HK1/CNPM%20Chuyen%20Sau/DoAn/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/backend/src/main/java/com/cnpm/careerrecommendation/exception/ApiException.java): Lớp ngoại lệ nghiệp vụ cơ sở với `HttpStatus`, `errorCode`, `message` theo quy chuẩn [`coding-conventions.md`](file:///d:/UIT-2026_2027_HK1/CNPM%20Chuyen%20Sau/DoAn/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/development/coding-conventions.md) mục 2.5.
* [`backend/src/main/java/com/cnpm/careerrecommendation/exception/ResourceNotFoundException.java`](file:///d:/UIT-2026_2027_HK1/CNPM%20Chuyen%20Sau/DoAn/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/backend/src/main/java/com/cnpm/careerrecommendation/exception/ResourceNotFoundException.java): Ngoại lệ HTTP 404 cho tài nguyên không tồn tại.
* [`backend/src/main/java/com/cnpm/careerrecommendation/exception/GlobalExceptionHandler.java`](file:///d:/UIT-2026_2027_HK1/CNPM%20Chuyen%20Sau/DoAn/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/backend/src/main/java/com/cnpm/careerrecommendation/exception/GlobalExceptionHandler.java): `@RestControllerAdvice` xử lý tập trung 8 loại ngoại lệ (`MethodArgumentNotValidException`, `ConstraintViolationException`, `ApiException`, `NoResourceFoundException`, `HttpRequestMethodNotSupportedException`, `HttpMediaTypeNotSupportedException`, `HttpMessageNotReadableException`, `MissingServletRequestParameterException`, và `Exception` fallback 500).

**Controller — Health Check:**
* [`backend/src/main/java/com/cnpm/careerrecommendation/controller/HealthController.java`](file:///d:/UIT-2026_2027_HK1/CNPM%20Chuyen%20Sau/DoAn/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/backend/src/main/java/com/cnpm/careerrecommendation/controller/HealthController.java): `GET /api/health` trả HTTP 200 `{"status":"UP"}`. Không lộ thông tin nhạy cảm.

**Configuration — CORS, Security, WebMvc:**
* [`backend/src/main/java/com/cnpm/careerrecommendation/config/CorsProperties.java`](file:///d:/UIT-2026_2027_HK1/CNPM%20Chuyen%20Sau/DoAn/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/backend/src/main/java/com/cnpm/careerrecommendation/config/CorsProperties.java): `@ConfigurationProperties(prefix = "app.cors")` đọc danh sách allowed origins từ cấu hình.
* [`backend/src/main/java/com/cnpm/careerrecommendation/config/WebMvcConfig.java`](file:///d:/UIT-2026_2027_HK1/CNPM%20Chuyen%20Sau/DoAn/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/backend/src/main/java/com/cnpm/careerrecommendation/config/WebMvcConfig.java): `WebMvcConfigurer` đăng ký CORS mappings từ `CorsProperties`.
* [`backend/src/main/java/com/cnpm/careerrecommendation/config/SecurityConfig.java`](file:///d:/UIT-2026_2027_HK1/CNPM%20Chuyen%20Sau/DoAn/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/backend/src/main/java/com/cnpm/careerrecommendation/config/SecurityConfig.java): `SecurityFilterChain` cơ sở: CORS tích hợp, CSRF disable, stateless session, permitAll cho `/api/health`, `/api/v1/auth/**`, `/api/v1/occupations/**`, `/api/v1/skills/**`, và `AuthenticationEntryPoint`/`AccessDeniedHandler` trả ErrorResponse chuẩn JSON.

### Cấu hình & Môi trường (Config & Environment):
* [`backend/pom.xml`](file:///d:/UIT-2026_2027_HK1/CNPM%20Chuyen%20Sau/DoAn/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/backend/pom.xml): Bổ sung dependency `spring-boot-starter-validation`.
* [`backend/src/main/resources/application.yml`](file:///d:/UIT-2026_2027_HK1/CNPM%20Chuyen%20Sau/DoAn/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/backend/src/main/resources/application.yml): Bổ sung cấu hình Jackson ISO-8601 UTC, cấu hình CORS (`app.cors.allowed-origins`, `allowed-methods`, `allowed-headers`, `allow-credentials`, `max-age`) đọc từ biến môi trường `CORS_ALLOWED_ORIGINS`.

### Kiểm thử (Tests):
* [`backend/src/test/java/com/cnpm/careerrecommendation/controller/HealthControllerTest.java`](file:///d:/UIT-2026_2027_HK1/CNPM%20Chuyen%20Sau/DoAn/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/backend/src/test/java/com/cnpm/careerrecommendation/controller/HealthControllerTest.java): 3 tests — health 200, public access without auth, POST method not allowed 405.
* [`backend/src/test/java/com/cnpm/careerrecommendation/exception/GlobalExceptionHandlerTest.java`](file:///d:/UIT-2026_2027_HK1/CNPM%20Chuyen%20Sau/DoAn/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/backend/src/test/java/com/cnpm/careerrecommendation/exception/GlobalExceptionHandlerTest.java): 10 tests — validation hợp lệ (200), thiếu trường (400), email sai format (400), password ngắn (400), JSON malformed (400), ResourceNotFoundException (404), ApiException (409), RuntimeException (500), cấu trúc ErrorResponse đầy đủ fields, mảng `errors[]` có `field/rejectedValue/reason`.
* [`backend/src/test/java/com/cnpm/careerrecommendation/config/CorsConfigTest.java`](file:///d:/UIT-2026_2027_HK1/CNPM%20Chuyen%20Sau/DoAn/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/backend/src/test/java/com/cnpm/careerrecommendation/config/CorsConfigTest.java): 5 tests — allowed origin header, preflight OPTIONS, alternative origin 5173, disallowed origin rejected, preflight disallowed origin rejected.

---

## 3. Khu vực bị ảnh hưởng (Affected Areas)

* `backend/pom.xml`
* `backend/src/main/resources/application.yml`
* `backend/src/main/java/com/cnpm/careerrecommendation/dto/ErrorResponse.java`
* `backend/src/main/java/com/cnpm/careerrecommendation/dto/ValidationErrorDetail.java`
* `backend/src/main/java/com/cnpm/careerrecommendation/dto/ApiResponse.java`
* `backend/src/main/java/com/cnpm/careerrecommendation/dto/HealthResponse.java`
* `backend/src/main/java/com/cnpm/careerrecommendation/exception/ApiException.java`
* `backend/src/main/java/com/cnpm/careerrecommendation/exception/ResourceNotFoundException.java`
* `backend/src/main/java/com/cnpm/careerrecommendation/exception/GlobalExceptionHandler.java`
* `backend/src/main/java/com/cnpm/careerrecommendation/controller/HealthController.java`
* `backend/src/main/java/com/cnpm/careerrecommendation/config/CorsProperties.java`
* `backend/src/main/java/com/cnpm/careerrecommendation/config/WebMvcConfig.java`
* `backend/src/main/java/com/cnpm/careerrecommendation/config/SecurityConfig.java`
* `backend/src/test/java/com/cnpm/careerrecommendation/controller/HealthControllerTest.java`
* `backend/src/test/java/com/cnpm/careerrecommendation/exception/GlobalExceptionHandlerTest.java`
* `backend/src/test/java/com/cnpm/careerrecommendation/config/CorsConfigTest.java`
* `docs/devlogs/2026-10-29-w1-t3-backend-foundation.md`

---

## 4. Quyết định kỹ thuật quan trọng (Decisions & Rationale)

1. **Tận dụng package hiện có thay vì tái cấu trúc phá hủy:** Giữ nguyên các package phẳng `config/`, `controller/`, `dto/` đã có sẵn `package-info.java`, tạo thêm package `exception/` mới. Không xóa bất kỳ file nào để tránh xung đột Git với thành viên khác. Các package `entity/`, `repository/`, `service/` được giữ nguyên cho W1-T4 và W1-T5 sử dụng.

2. **ErrorResponse khớp 100% với API Contract 1.2:** Sử dụng Java 17 Record với đúng các trường `timestamp` (ISO-8601 UTC), `status` (int), `error` (String), `message` (String), `path` (String), `errors` (List<ValidationErrorDetail>) — không sáng tạo thêm trường ngoài hợp đồng.

3. **SecurityConfig cơ sở sẵn sàng cho W1-T5:** Cấu hình `SecurityFilterChain` chỉ thiết lập khung nền (CORS, CSRF off, stateless, permitAll public endpoints). Task W1-T5 (Đoàn Việt Khang) chỉ việc inject `JwtAuthenticationFilter` vào filter chain bằng `.addFilterBefore()` mà không cần sửa lại cấu trúc Security hiện có. Đồng thời `AuthenticationEntryPoint` và `AccessDeniedHandler` đã trả JSON `ErrorResponse` chuẩn, giúp lỗi 401/403 cũng nhất quán với toàn hệ thống.

4. **CORS tách biệt qua ConfigurationProperties:** Sử dụng `CorsProperties` bean đọc giá trị từ `application.yml` (`app.cors.*`) thay vì hardcode, cho phép thay đổi linh hoạt qua biến môi trường `CORS_ALLOWED_ORIGINS` khi triển khai production mà không cần sửa code.

5. **Test bằng @WebMvcTest thay vì @SpringBootTest:** Dùng `@WebMvcTest` tải context cực nhẹ (chỉ controller, filter, exception handler) và không cần PostgreSQL chạy. Cho phép kiểm thử triệt để Controller, Validation, CORS và Security filter ngay trên máy phát triển mà không phụ thuộc Docker.

6. **Không tạo thêm Spring Boot Actuator:** Dù API Contract không yêu cầu, endpoint health check được triển khai thủ công tại `GET /api/health` để kiểm soát chính xác URL và nội dung phản hồi theo yêu cầu đề bài, tránh lộ thông tin hệ thống qua các Actuator endpoint mặc định.

---

## 5. Kiểm tra & Xác thực (Validation Performed)

> [!IMPORTANT]
> **Chỉ ghi nhận những kiểm thử hoặc lệnh kiểm tra ĐÃ THỰC SỰ ĐƯỢC CHẠY thành công.**
> Tuyệt đối không khai báo vượt hoặc giả mạo kết quả kiểm thử.

| Lệnh / Thao tác kiểm tra | Mục đích kiểm tra | Kết quả thực tế | Ghi chú |
|---|---|---|---|
| `mvn clean test` | Biên dịch 18 source files + 4 test files và chạy toàn bộ test suite | **BUILD SUCCESS** — Tests run: **19**, Failures: 0, Errors: 0, Skipped: 0 (51.891s) | Exit code 0. Toàn bộ 19 tests pass. |
| `HealthControllerTest` (3 tests) | Kiểm tra `GET /api/health` trả 200, truy cập không cần auth, POST trả 405 | **3/3 pass** (0.536s) | `{"status":"UP"}` đúng format. |
| `GlobalExceptionHandlerTest` (10 tests) | Kiểm tra validation valid/invalid, ApiException, ResourceNotFoundException, RuntimeException, cấu trúc ErrorResponse | **10/10 pass** (7.233s) | ErrorResponse khớp API Contract 1.2. |
| `CorsConfigTest` (5 tests) | Kiểm tra CORS headers cho origin được phép, preflight OPTIONS, origin bị từ chối | **5/5 pass** (22.61s) | `http://localhost:3000` và `http://localhost:5173` được phép; `http://evil-site.com` bị chặn. |
| `CareerRecommendationApplicationTests` (1 test) | Kiểm tra skeleton test cơ sở | **1/1 pass** (0.136s) | Test cũ vẫn giữ nguyên, không bị ảnh hưởng. |

**Lưu ý:**
- Chưa thể kiểm thử khởi chạy đầy đủ ứng dụng (`mvn spring-boot:run`) do Docker Desktop chưa bật trên máy, PostgreSQL chưa sẵn sàng dẫn đến Flyway/JPA không kết nối được. Tuy nhiên, toàn bộ tầng Web (Controller, Security, CORS, Validation, Exception Handling) đã được xác nhận hoạt động chính xác qua MockMvc.

---

## 6. Công việc tồn đọng & Bước tiếp theo (Outstanding Work)

* [ ] **Kiểm thử khởi chạy thực tế (`spring-boot:run`):** Cần bật Docker Desktop → `docker compose up -d postgres` → chạy `mvn spring-boot:run` → gọi `curl http://localhost:8080/api/health` để xác nhận HTTP 200 trên môi trường hoàn chỉnh.
* [ ] **Phối hợp W1-T5 (Đoàn Việt Khang):** `SecurityConfig.java` đã sẵn sàng cho tích hợp `JwtAuthenticationFilter`. Cần xác nhận cách thêm filter vào chain và endpoint nào cần quyền xác thực cụ thể.
* [ ] **Phối hợp W1-T4 (Huỳnh Long Bảo Khanh):** Khi entity JPA được tạo, cần xác nhận Hibernate `validate` khớp chính xác schema V1.
* [ ] **Cân nhắc bổ sung DuplicateResourceException (409):** Khi module `auth` register endpoint kiểm tra email trùng, cần thêm ngoại lệ tương ứng kế thừa từ `ApiException`.
* [ ] **Cấu hình profile `application-local.yml` riêng biệt:** Nếu nhóm có nhu cầu tách biệt cấu hình môi trường dev/staging/production, cần tạo thêm file profile-based configuration.
