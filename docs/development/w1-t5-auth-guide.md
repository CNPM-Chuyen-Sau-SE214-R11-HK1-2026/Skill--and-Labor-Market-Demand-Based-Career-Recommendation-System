# W1-T5 — Xác thực và phân quyền JWT — Đoàn Việt Khang

## 1. Phạm vi được giao và sản phẩm

Căn cứ: `CNPM Chuyên Sâu - Nhóm 4.xlsx`, sheet `Task`, dòng 6 (A6:J6),
đối chiếu sheet `Tài liệu Repo`, dòng 5. W1-T5 giao Đoàn Việt Khang:
đăng ký/đăng nhập/tài khoản hiện tại, BCrypt, JWT, SecurityFilterChain và RBAC.
Bảng phân công Tuần 2–5 trong repo cũ khác Excel; không dùng nó để đổi task W1.

- `POST /api/v1/auth/register`: 201, tài khoản CANDIDATE, BCrypt cost 12.
- `POST /api/v1/auth/login`: 200, token HS256 và user DTO.
- `GET /api/v1/auth/me`: 200 khi có Bearer hợp lệ, lấy userId từ principal.
- Sai/hết hạn/giả chữ ký/tài khoản bị khóa hoặc xóa: 401.
- Candidate gọi admin: 403. Admin không được dùng API dành riêng candidate.
- Trường lạ trong JSON, gồm `role`, `userId`, `isActive`: 400.
- Email được chuẩn hóa; đăng ký trùng hoặc đồng thời: 409.
- Không trả password/hash; validation không phản chiếu giá trị bị từ chối.
- V2 lưu phone và unique lower(email), không thay đổi V1.

Các controller profile, skills, occupations, recommendations, admin, health và
UI auth chưa được làm trong W1-T5. Chỉ có quy tắc bảo vệ các nhóm URL này.
Security probe trong test không được đóng vào ứng dụng production.
Không có refresh token/logout server/cấp admin public trong task này.

## 2. Cấu trúc và phối hợp ghép code

| Thành phần | Vị trí | Phối hợp |
|---|---|---|
| Controller/Service/DTO | `backend/src/main/java/com/cnpm/careerrecommendation/module/auth/` | W1-T2 gọi đúng API contract |
| User/Role/Repository | `module/auth/entity`, `module/auth/repository` | W1-T4 tái sử dụng, không tạo User mapping thứ hai |
| JWT/filter/principal | `common/security/` | Module khác nhận `@AuthenticationPrincipal AuthenticatedUser` |
| Security/CORS | `config/SecurityConfig.java` | W1-T3 không tạo chain trùng |
| ErrorResponse/handler | `common/dto`, `common/exception` | W1-T3 tái sử dụng định dạng API §1.2 |
| Validation mật khẩu | `common/validation/` | 8–64 ký tự, tối đa 72 byte UTF-8, không NUL |
| Migration | `resources/db/migration/V2__auth_phone_and_email_uniqueness.sql` | Ghép migration theo thứ tự, không sửa V1 đã chạy |

Ví dụ cho module profile sau này:

```java
@GetMapping("/api/v1/profile")
public ProfileResponse profile(@AuthenticationPrincipal AuthenticatedUser current) {
    return profileService.getByUserId(current.userId());
}
```

Đây là ví dụ tích hợp, không phải controller đã tồn tại.
`AuthenticatedUser` không chứa hash. Không dùng userId từ body/query để chọn chủ hồ sơ.

## 3. Khởi chạy trên Windows PowerShell

Yêu cầu JDK 17, Maven 3.8+, PostgreSQL 16 hoặc Docker Desktop.
Chạy tại thư mục gốc source sau khi giải nén. Maven không tự nạp `.env`.

### 3.1. Tạo cấu hình local một lần

```powershell
Copy-Item .env.example .env
$rng = [Security.Cryptography.RandomNumberGenerator]::Create()
$keyBytes = New-Object byte[] 32
$rng.GetBytes($keyBytes)
$rng.Dispose()
$jwtSecret = [Convert]::ToBase64String($keyBytes)
(Get-Content .env) -replace '^JWT_SECRET=.*$', "JWT_SECRET=$jwtSecret" | Set-Content .env -Encoding utf8
```

Đặt `POSTGRES_PASSWORD` trong `.env` phù hợp DB local; giữ `JWT_EXPIRATION_MS=86400000`
khi chạy script nghiệm thu mặc định. Secret trong ví dụ mẫu `your_...` bị ứng dụng
chủ động từ chối. Mỗi môi trường dùng khóa riêng; không commit `.env`.
JWT_SECRET được dùng nguyên văn UTF-8, không tự decode Base64.

### 3.2. Nạp môi trường và chạy

```powershell
Get-Content .env | ForEach-Object {
    if ($_ -match '^\s*([A-Za-z_][A-Za-z0-9_]*)=(.*)$') {
        [Environment]::SetEnvironmentVariable($matches[1], $matches[2], 'Process')
    }
}
docker compose up -d postgres
cd backend
mvn clean spring-boot:run
```

Nếu dùng PostgreSQL cài sẵn, bỏ lệnh Docker; tạo DB trùng POSTGRES_DB và đúng user/password.
Backend mặc định `http://localhost:8080`. Khi startup thành công, Flyway ở V2 và
Hibernate `ddl-auto=validate`. Thiếu JWT_SECRET hoặc secret placeholder là lỗi cấu hình,
không phải lỗi đăng nhập.

Lựa chọn chạy toàn bộ Compose: sau khi chuẩn bị `.env`, dùng
`docker compose up --build -d`; Compose tự nạp `.env`. Frontend hiện vẫn là skeleton.
CORS mặc định cho `http://localhost:3000` và `http://localhost:5173`;
đổi `CORS_ALLOWED_ORIGINS` nếu chạy origin khác, ví dụ địa chỉ 127.0.0.1.

### 3.3. Kiểm thử HTTP có PASS/FAIL trên màn hình

Mở PowerShell thứ hai tại gốc source:

```powershell
powershell -NoProfile -File .\scripts\test-auth.ps1
```

Nếu backend khác cổng:

```powershell
powershell -NoProfile -File .\scripts\test-auth.ps1 -BaseUrl http://localhost:8081
```

Script thực hiện 15 ca HTTP, in từng dòng PASS và dừng với lỗi khi kết quả khác kỳ vọng.
Nó tạo email ngẫu nhiên và giữ tài khoản test trong DB development; không có API xóa user.
Script không in token/password và không dùng admin endpoint giả để đạt PASS.
Cần chạy trên DB development. Script PowerShell được cung cấp để bạn chạy trên Windows;
bộ kiểm thử Java HTTP tương ứng là kiểm chứng tự động trong bản bàn giao.

## 4. Kiểm thử tự động

### 4.1. Unit test — không cần PostgreSQL

```powershell
cd backend
mvn clean test
```

Kiểm tra claim, expiry, chữ ký, algorithm, secret cấu hình, payload thiếu/sai định dạng,
role lạ, validation và mật khẩu Unicode. Test key là dữ liệu cố định chỉ dùng trong test.

### 4.2. Kiểm thử tích hợp thật — PostgreSQL riêng + HTTP thật

Tạo container test mới, không tái sử dụng DB development:

```powershell
docker run --name career-auth-test -e POSTGRES_DB=career_auth_test -e POSTGRES_USER=auth_test -e POSTGRES_PASSWORD=local-test-password-only -p 127.0.0.1:55439:5432 -d postgres:16-alpine
docker exec career-auth-test pg_isready -U auth_test -d career_auth_test
$env:AUTH_TEST_DATABASE_URL='jdbc:postgresql://127.0.0.1:55439/career_auth_test'
$env:AUTH_TEST_DATABASE_USER='auth_test'
$env:AUTH_TEST_DATABASE_PASSWORD='local-test-password-only'
cd backend
mvn -Ppostgres-it clean verify
```

Chạy lại pg_isready nếu container chưa sẵn sàng. Profile không có biến test DB sẽ thất bại
với hướng dẫn rõ ràng; không tự chọn DB thật của ứng dụng.
Test chạy Flyway V1+V2, Hibernate validate, server HTTP trên cổng ngẫu nhiên,
BCrypt/JWT thật và database thật. Không có UserRepository mock hoặc H2.
Có probe chỉ trong test để chứng minh boundary admin/profile/taxonomy trước khi
module khác có controller; probe không chứng minh module đó đã hoàn thành nghiệp vụ.

Kết quả XML/TXT: `backend/target/surefire-reports/` và `backend/target/failsafe-reports/`.
Dọn container test sau khi xong: `docker rm -f career-auth-test` (chỉ container test vừa tạo).

## 5. Ma trận quyền

| Nhóm URL/phương thức | Anonymous | CANDIDATE | ADMIN |
|---|---|---|---|
| POST auth/register, auth/login | Cho phép | Cho phép | Cho phép |
| GET auth/me | 401 | Cho phép | Cho phép |
| GET /api/health, /api/v1/skills, /api/v1/occupations và /{id} | Cho phép qua security | Cho phép | Cho phép |
| /api/v1/profile và /** | 401 | Cho phép | 403 |
| /api/v1/recommendations và /** | 401 | Cho phép | 403 |
| /api/v1/admin/** | 401 | 403 | Cho phép |
| URL/phương thức khác | Bị chặn mặc định | 403 | 403 |

“Cho phép” là qua security; controller chưa có có thể trả 404.
CORS preflight được xử lý theo allowlist trước bước xác thực. Origin bị chặn trả 403
với ErrorResponse JSON chuẩn như lỗi auth/validation. Token sai trên route đọc public cũng bị từ chối 401;
POST login/register bỏ qua Bearer cũ để đăng nhập lại được.

## 6. Lưu ý ghép với công việc của nhóm

- Không copy `target`, `.env`, thư mục tools hoặc database từ môi trường kiểm thử.
- Có `CHANGES.md` và patch trong gói bàn giao; chỉ áp dụng vào đúng baseline đã gửi.
- Nếu đồng đội đã sửa User/Security/handler/pom/application.yml, merge nội dung,
  không ghi đè toàn bộ file. Chỉ có một User entity ánh xạ users và một chain chính.
- V1 được giữ nguyên byte; nếu DB đang có email chỉ khác hoa thường, rà soát và xử lý
  trùng trước V2. Không tự dùng flyway repair, không xóa DB nhóm.
- Không tạo endpoint seed admin. Ca admin trong integration test dùng SQL trên DB test.
- Docker Compose toàn hệ thống và script PowerShell cần được xác nhận thêm trên máy Windows;
  không xem việc unit test PASS là bằng chứng cả frontend/Compose đã hoàn tất.
