# DevLog: W1-T5 — Xác thực và phân quyền JWT

* **Mã tác vụ / Task ID:** W1-T5
* **Người thực hiện / Agent:** Đoàn Việt Khang / Codex hỗ trợ
* **Ngày thực hiện (Date):** 2026-10-29
* **Nhánh Git (Branch):** Bản ZIP được cung cấp không có .git; chưa commit/push.

## 1. Mục tiêu (Objective)

Hoàn thành task giao cho Việt Khang ở A6:J6 sheet Task trong Excel nhóm:
đăng ký, đăng nhập, tài khoản hiện tại, BCrypt, JWT và RBAC. Đối chiếu hợp đồng API,
kiến trúc/domain package, V1 DDL và tài liệu nguồn của task. Excel là căn cứ phân công W1;
không coi kế hoạch frontend W2–W5 trong repo là task W1 hiện tại.

## 2. Thay đổi thực tế (Changes)

- Thêm auth Controller/Service/DTO, User/Role/Repository, BCrypt cost 12.
- Thêm JWT HS256 với sub=email, userId số, role và iat/exp mặc định 24h.
- Filter kiểm tra chữ ký và trạng thái/role/email trong DB, principal không chứa hash.
- Security stateless, CORS allowlist và lỗi CORS JSON; chặn theo role và mặc định deny.
- Validation, lỗi chuẩn hóa/redaction, xử lý trùng email kể cả đăng ký đồng thời.
- V2 bổ sung phone và unique lower(email); V1 không đổi byte.
- Bỏ secret mặc định, thêm validation starter và profile kiểm thử PostgreSQL thật.
- Thay test skeleton rỗng bằng 32 unit/validation và 21 HTTP/PostgreSQL integration cases.
- Thêm script PowerShell 15 ca HTTP, hướng dẫn Windows, đồng bộ tài liệu và ADR-0003
  ở trạng thái Proposed để nhóm review (không tự nhận nhóm đã phê duyệt).

## 3. Khu vực bị ảnh hưởng (Affected Areas)

- `.env.example`
- `README.md`
- `backend/pom.xml`
- `backend/src/main/java/com/cnpm/careerrecommendation/common/dto/ErrorResponse.java`
- `backend/src/main/java/com/cnpm/careerrecommendation/common/exception/ApiException.java`
- `backend/src/main/java/com/cnpm/careerrecommendation/common/exception/GlobalExceptionHandler.java`
- `backend/src/main/java/com/cnpm/careerrecommendation/common/security/AuthenticatedUser.java`
- `backend/src/main/java/com/cnpm/careerrecommendation/common/security/JwtAuthenticationFilter.java`
- `backend/src/main/java/com/cnpm/careerrecommendation/common/security/JwtTokenProvider.java`
- `backend/src/main/java/com/cnpm/careerrecommendation/common/security/SecurityErrorHandler.java`
- `backend/src/main/java/com/cnpm/careerrecommendation/common/validation/Utf8Password.java`
- `backend/src/main/java/com/cnpm/careerrecommendation/common/validation/Utf8PasswordValidator.java`
- `backend/src/main/java/com/cnpm/careerrecommendation/config/SecurityConfig.java`
- `backend/src/main/java/com/cnpm/careerrecommendation/module/auth/controller/AuthController.java`
- `backend/src/main/java/com/cnpm/careerrecommendation/module/auth/dto/CurrentUserResponse.java`
- `backend/src/main/java/com/cnpm/careerrecommendation/module/auth/dto/LoginRequest.java`
- `backend/src/main/java/com/cnpm/careerrecommendation/module/auth/dto/LoginResponse.java`
- `backend/src/main/java/com/cnpm/careerrecommendation/module/auth/dto/RegisterRequest.java`
- `backend/src/main/java/com/cnpm/careerrecommendation/module/auth/dto/RegisteredUserResponse.java`
- `backend/src/main/java/com/cnpm/careerrecommendation/module/auth/entity/Role.java`
- `backend/src/main/java/com/cnpm/careerrecommendation/module/auth/entity/User.java`
- `backend/src/main/java/com/cnpm/careerrecommendation/module/auth/repository/UserRepository.java`
- `backend/src/main/java/com/cnpm/careerrecommendation/module/auth/service/AuthService.java`
- `backend/src/main/resources/application.yml`
- `backend/src/main/resources/db/migration/V2__auth_phone_and_email_uniqueness.sql`
- `backend/src/test/java/com/cnpm/careerrecommendation/CareerRecommendationApplicationTests.java`
- `backend/src/test/java/com/cnpm/careerrecommendation/common/security/JwtTokenProviderTest.java`
- `backend/src/test/java/com/cnpm/careerrecommendation/module/auth/AuthPostgresIT.java`
- `backend/src/test/java/com/cnpm/careerrecommendation/module/auth/AuthRequestValidationTest.java`
- `docker-compose.yml`
- `docs/api/api-contract.md`
- `docs/architecture/system-architecture.md`
- `docs/database/database-design.md`
- `docs/decisions/ADR-0003-auth-contract-alignment.md`
- `docs/decisions/README.md`
- `docs/development/README.md`
- `docs/development/coding-conventions.md`
- `docs/development/w1-t5-auth-guide.md`
- `docs/planning/week-2-5-backlog.md`
- `scripts/test-auth.ps1`

- `docs/devlogs/2026-10-29-w1-t5-jwt-auth-viet-khang.md` và mục lục DevLog.

## 4. Quyết định kỹ thuật quan trọng (Decisions & Rationale)

Giữ Java 17/Spring Boot 3.3.4/JJWT 0.12.6/Flyway đã chọn. Dùng V2 thay cho sửa
checksum V1; tái sử dụng một User entity trong module auth. Lấy ErrorResponse theo
API §1.2; giá trị bị từ chối luôn null. Khóa JWT do môi trường cung cấp, tối thiểu
32 byte UTF-8, không tự decode. BCrypt giới hạn 72 byte UTF-8, không trim password.
DB là nguồn quyền hiện tại; mỗi request xác thực thêm một truy vấn User.
Xem [ADR-0003](../decisions/ADR-0003-auth-contract-alignment.md).

## 5. Kiểm tra & Xác thực (Validation Performed)

| Lệnh / Thao tác | Kết quả thực tế |
|---|---|
| `mvn -Ppostgres-it clean verify` (Maven 3.9.9, Java 17) | BUILD SUCCESS; unit 32/32, integration 21/21; failures=0, errors=0, skipped=0 |
| Flyway trên PostgreSQL 16.4 mới | Áp dụng V1 và V2 thành công, đủ 10 bảng nghiệp vụ |
| Hibernate `ddl-auto=validate` | EntityManagerFactory khởi tạo thành công; User mapping hợp lệ |
| HTTP đăng ký/đăng nhập/me | Server thật cổng ngẫu nhiên; BCrypt/JWT/PostgreSQL thật |
| HTTP bảo mật và concurrency | Token sai/hết hạn/giả role bị từ chối; candidate/admin boundary; 201+409 cho đăng ký đồng thời |
| `npm ci --no-audit --no-fund` và `npm run build` | Exit 0; tsc và Vite build thành công, 32 modules; source frontend giữ nguyên |
| Kiểm tra V1 với ZIP đầu vào | Nội dung byte không thay đổi |
| `git status`/`git diff` | ZIP không có .git; kiểm tra thay đổi qua baseline ZIP, patch và đối chiếu file |

Bằng chứng: thư mục evidence trong gói bàn giao chứa log verify cuối và báo cáo XML/TXT.
Môi trường Linux dùng PostgreSQL native 16.4 với shim định danh UID do host chỉ ánh xạ
UID 0, kết nối TCP loopback; không dùng Docker/H2 trong lần chạy này. Hướng dẫn Windows
chạy PostgreSQL 16 qua Docker bình thường, không cần shim. Lần verify cuối trên cluster mới.
Trong ca đăng ký đồng thời, Hibernate có log unique violation SQLSTATE 23505;
service trả đúng 409 và bộ test báo Errors: 0, đây là trường hợp lỗi dữ liệu được kiểm soát.

## 6. Công việc tồn đọng & Bước tiếp theo (Outstanding Work)

- Xác nhận script PowerShell và Docker Compose trên Windows/Docker Desktop; môi trường
  này không có pwsh hoặc Docker daemon. Không tuyên bố đã chạy hai phần này.
- W1-T3 ghép health/controller nền tảng và tái sử dụng handler/chain; W1-T4 ghép các
  entity còn lại, giữ một mapping User và thứ tự migration; W1-T2 tích hợp frontend.
- Các controller profile/taxonomy/admin nghiệp vụ và UI vẫn thuộc task khác; probe chỉ ở test.
- Chưa đo code coverage phần trăm; không tự nhận đạt 80% theo backlog W2.
- Nếu DB cũ có email chỉ khác hoa thường, xử lý trùng trước khi chạy V2.
- Excel quản lý nhóm được dùng làm căn cứ và giữ nguyên file đã gửi.
