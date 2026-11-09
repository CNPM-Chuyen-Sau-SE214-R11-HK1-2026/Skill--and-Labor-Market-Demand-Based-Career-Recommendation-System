# Hướng dẫn Docker Compose, biến môi trường và CI — W2-T6

## 1. Mục đích

Tài liệu hướng dẫn thiết lập môi trường Docker Compose và kiểm thử tự động cho dự án Hệ thống gợi ý nghề nghiệp dựa trên kỹ năng và nhu cầu thị trường lao động.

Nội dung thuộc tác vụ **W2-T6 — Chuẩn hóa môi trường Docker Compose, biến môi trường và thiết lập CI Pipeline**, tuân theo [ADR-0009](../adr/ADR-0009-docker-and-environment-variable-contract.md).

## 2. Yêu cầu

Để chạy hệ thống bằng Docker Compose, máy cần có:

- Git.
- Docker Desktop (Windows) hoặc Docker Engine (Linux).
- Docker Compose phiên bản 2 trở lên.
- PowerShell trên Windows; Bash và OpenSSL khi sử dụng script trên Linux/macOS.

Các lệnh dưới đây được thực hiện tại thư mục gốc repository.

## 3. Khởi tạo biến môi trường

### 3.1. Windows

Chạy:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\init-env.ps1
```

### 3.2. Linux/macOS

Chạy:

```bash
bash scripts/init-env.sh
```

Script có chức năng:

- Tạo `.env` từ `.env.example` nếu chưa tồn tại.
- Sinh `JWT_SECRET` ngẫu nhiên gồm 64 ký tự hexadecimal.
- Sinh mật khẩu PostgreSQL ngẫu nhiên khi tạo môi trường mới.
- Không ghi đè `.env` nếu khóa JWT hiện tại hợp lệ.
- Thay khóa JWT không hợp lệ mà vẫn giữ các biến môi trường khác.

**Bảo mật:** Không commit `.env`, mật khẩu PostgreSQL hoặc JWT secret lên GitHub.

**Lưu ý về cơ sở dữ liệu đã tồn tại:** Khi PostgreSQL đã được khởi tạo trong Docker volume, mật khẩu lưu trong cơ sở dữ liệu không tự động thay đổi theo `.env`. Cần bảo đảm cấu hình `.env` khớp với tài khoản PostgreSQL hiện có trước khi tạo lại container.

## 4. Khởi chạy Docker Compose

Kiểm tra cấu hình:

```bash
docker compose config --quiet
```

Build và khởi chạy:

```bash
docker compose up -d --build
```

Kiểm tra trạng thái:

```bash
docker compose ps
```

Kết quả mong đợi:

| Dịch vụ | Trạng thái |
|---|---|
| PostgreSQL | `healthy` |
| Backend | `healthy` |
| Frontend | `running` |

Docker Compose sử dụng thứ tự khởi chạy:

1. PostgreSQL đạt trạng thái `healthy`.
2. Backend khởi động và đạt trạng thái `healthy`.
3. Frontend bắt đầu khởi chạy.

Healthcheck Backend sử dụng endpoint `GET /api/health` trên cổng 8080.

## 5. Kiểm tra API Health

Trên Windows PowerShell:

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/health"
```

Hoặc truy cập:

`http://localhost:8080/api/health`

Kết quả mong đợi:

```json
{
  "status": "UP"
}
```

Các địa chỉ mặc định:

| Thành phần | Địa chỉ |
|---|---|
| Frontend | `http://localhost:3000` |
| Backend | `http://localhost:8080` |
| PostgreSQL | `localhost:5432` |

## 6. Quản lý Docker

Xem log:

```bash
docker compose logs backend --tail 50
```

Dừng container:

```bash
docker compose stop
```

Khởi động lại:

```bash
docker compose start
```

Dừng và xóa container, nhưng giữ named volume:

```bash
docker compose down
```

**Không chạy `docker compose down -v` nếu chưa chủ động quyết định xóa dữ liệu PostgreSQL.**

## 7. Kiểm thử tự động bằng GitHub Actions

Workflow CI được định nghĩa tại:

`.github/workflows/ci.yml`

Workflow được kích hoạt khi có Pull Request vào `main` hoặc push lên `main`.

### 7.1. Backend Unit Test

CI sử dụng Java 17 và Maven để chạy:

```bash
mvn -B -ntp -f backend/pom.xml clean test
```

### 7.2. Backend Integration Test

CI khởi tạo một PostgreSQL 16 riêng cho việc kiểm thử.

Các biến môi trường phục vụ `AuthPostgresIT`:

- `AUTH_TEST_DATABASE_URL`
- `AUTH_TEST_DATABASE_USER`
- `AUTH_TEST_DATABASE_PASSWORD`

Chạy kiểm thử tích hợp:

```bash
mvn -B -ntp -f backend/pom.xml -Ppostgres-it test-compile failsafe:integration-test failsafe:verify
```

Cơ sở dữ liệu kiểm thử CI tách biệt với PostgreSQL trên máy của thành viên.

### 7.3. Frontend Build

CI sử dụng Node.js 20, thực hiện trong thư mục `frontend`:

```bash
npm ci
npm run build
```

Lệnh build bao gồm kiểm tra TypeScript và tạo bản build Vite.

## 8. Kiểm tra CI trên GitHub

Sau khi push nhánh W2-T6 và tạo Pull Request vào `main`:

1. Mở Pull Request trên GitHub.
2. Chọn tab **Checks** hoặc phần kiểm tra tự động.
3. Xem kết quả hai job `Backend - Unit Tests and PostgreSQL IT` và `Frontend - TypeScript and Vite Build`.
4. Nếu job thất bại, xem log của bước báo lỗi và sửa trên nhánh W2-T6.

CI chỉ được ghi nhận thành công khi các job thực sự hoàn thành với trạng thái thành công trên GitHub.

Việc chuyển CI thành **Required status check** phải được người có quyền quản trị repository cấu hình theo quy định của nhóm.

## 9. Kết quả kiểm thử tại máy phát triển

**Ngày kiểm thử:** 06/11/2026.

Đã thực hiện và ghi nhận:

| Nội dung | Kết quả |
|---|---|
| Script Windows tạo `.env` | Thành công |
| Sinh JWT secret 64 ký tự | Thành công |
| Đồng bộ cấu hình với PostgreSQL cũ | Thành công |
| Kiểm tra cấu hình Docker Compose | Thành công |
| Build Backend và Frontend | Thành công |
| PostgreSQL healthcheck | `healthy` |
| Backend healthcheck | `healthy` |
| Frontend container | `running` |
| `GET /api/health` | `UP` |

**Chưa xác minh:** Kết quả GitHub Actions CI và khả năng chạy script Bash trên Linux/macOS.

## 10. Công việc tiếp theo

- Chạy CI khi Pull Request W2-T6 được tạo.
- Xử lý lỗi kiểm thử nếu xuất hiện trên GitHub Actions.
- Phối hợp với người quản trị repository để thiết lập Required status check khi CI ổn định.
- Hoàn thiện DevLog và quy trình review trước khi merge.