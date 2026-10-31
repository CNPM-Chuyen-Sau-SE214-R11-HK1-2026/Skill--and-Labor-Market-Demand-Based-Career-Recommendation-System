# Hướng dẫn phát triển & Môi trường (Development Guide)

Tài liệu này hướng dẫn các thành viên dự án **Skill- and Labor Market Demand–Based Career Recommendation System** thiết lập môi trường phát triển, chạy ứng dụng cục bộ, kiểm thử và khởi chạy toàn bộ hệ thống bằng Docker Compose.

---

## 1. Yêu cầu công cụ cài đặt sẵn (Prerequisites)

### 1.1. Phát triển bằng Docker Compose (khuyến nghị)

Cần cài đặt:

- **Docker Desktop** trên Windows hoặc Docker Engine trên Linux.
- **Docker Compose v2 trở lên**.
- **Git** để quản lý mã nguồn.

Trên Windows, khuyến nghị sử dụng Docker Desktop với **WSL 2 backend**.

Kiểm tra cài đặt bằng Terminal:

```bash
docker --version
docker compose version
docker info
```

Docker Engine phải đang hoạt động trước khi khởi chạy dự án.

> **Lưu ý:** Khi chạy toàn bộ ứng dụng bằng Docker Compose, không bắt buộc cài riêng Java, Maven hoặc Node.js trên máy, vì các công cụ build đã được khai báo trong Dockerfile.

### 1.2. Phát triển trực tiếp trên máy (Local Development)

Nếu muốn chạy backend hoặc frontend ngoài Docker, cần cài đặt:

- **Node.js:** Phiên bản 18+ (khuyến nghị Node.js 20).
- **npm:** Phiên bản 9+.
- **JDK:** Java 17.
- **Apache Maven:** Phiên bản 3.8+ hoặc Maven Wrapper nếu dự án hỗ trợ.
- **Docker:** Để chạy PostgreSQL cục bộ.

---

## 2. Thiết lập biến môi trường (Environment Setup)

Hệ thống sử dụng file `.env` tại thư mục gốc dự án.

### 2.1. Tạo file `.env`

Mở Terminal tại thư mục gốc dự án.

**Windows PowerShell:**

```powershell
Copy-Item .env.example .env
```

**Linux/macOS/Git Bash:**

```bash
cp .env.example .env
```

Chỉ thực hiện thao tác sao chép nếu file `.env` chưa tồn tại.

### 2.2. Cấu hình các biến môi trường

File `.env.example` cung cấp các biến:

| Biến | Mục đích | Giá trị mẫu |
|---|---|---|
| `POSTGRES_DB` | Tên cơ sở dữ liệu | `career_recommendation_db` |
| `POSTGRES_USER` | Tài khoản PostgreSQL | `postgres` |
| `POSTGRES_PASSWORD` | Mật khẩu PostgreSQL | Cần thiết lập |
| `POSTGRES_HOST` | Host kết nối từ máy cục bộ | `localhost` |
| `POSTGRES_PORT` | Cổng PostgreSQL trên máy | `5432` |
| `BACKEND_PORT` | Cổng backend trên máy | `8080` |
| `FRONTEND_PORT` | Cổng frontend trên máy | `3000` |
| `JWT_SECRET` | Khóa bí mật phục vụ xác thực JWT | Cần thiết lập |
| `JWT_EXPIRATION_MS` | Thời hạn JWT tính bằng mili giây | `86400000` |

Thay các giá trị mẫu của `POSTGRES_PASSWORD` và `JWT_SECRET` bằng giá trị riêng trước khi sử dụng.

**Lưu ý về kết nối PostgreSQL:**

- Khi kết nối từ Windows hoặc máy host, sử dụng `localhost:5432` theo cấu hình cổng mặc định.
- Khi backend chạy bên trong Docker Compose, hostname của PostgreSQL là `postgres`, không phải `localhost`.
- File `docker-compose.yml` đã cấu hình `POSTGRES_HOST=postgres` cho container backend.
Docker Compose đọc `.env` ở thư mục gốc. Khi chạy Maven trực tiếp, cần nạp biến vào terminal; Spring Boot không tự đọc file `.env`. Xem [hướng dẫn W1-T5](w1-t5-auth-guide.md).

1. Sao chép file cấu hình mẫu:
   ```bash
   cp .env.example .env
   ```
2. Chỉnh sửa các tham số trong file `.env` nếu cần thay đổi cổng hoặc mật khẩu CSDL:
   ```env
   POSTGRES_DB=career_recommendation_db
   POSTGRES_USER=postgres
   POSTGRES_PASSWORD=your_secure_password
   POSTGRES_HOST=localhost
   POSTGRES_PORT=5432
   BACKEND_PORT=8080
   FRONTEND_PORT=3000
   ```

> [!CAUTION]
> **Không commit file `.env` hoặc thông tin bí mật lên Git.**
>
> File `.gitignore` đã loại trừ `.env` và cho phép theo dõi `.env.example`.
>
> Nếu PostgreSQL đã được khởi tạo và lưu dữ liệu trong Docker volume, việc đổi `POSTGRES_PASSWORD` trong `.env` không tự động thay đổi mật khẩu của tài khoản PostgreSQL hiện có. Cần cập nhật mật khẩu trong database tương ứng trước khi khởi động lại backend.
>
> Không sử dụng `docker compose down -v` nếu muốn giữ dữ liệu PostgreSQL.

### 2.3. Trường hợp chưa tạo `.env`

File `docker-compose.yml` có khai báo giá trị mặc định cho một số biến môi trường. Vì vậy, hệ thống có thể khởi chạy mà chưa có file `.env`.

Tuy nhiên, các giá trị mật khẩu và khóa bí mật mặc định chỉ phục vụ môi trường phát triển cục bộ, không phù hợp để triển khai production.

Khuyến nghị tạo và cấu hình `.env` ngay từ lần thiết lập môi trường đầu tiên.

---

## 3. Khởi chạy toàn diện với Docker Compose (Khuyến nghị)

Docker Compose quản lý ba dịch vụ:

| Service | Công nghệ | Chức năng |
|---|---|---|
| `postgres` | PostgreSQL 16 Alpine | Lưu trữ dữ liệu |
| `backend` | Spring Boot, Java 17 | Cung cấp REST API |
| `frontend` | React, TypeScript, Nginx | Cung cấp giao diện web |

### 3.1. Kiểm tra cấu hình

Tại thư mục gốc dự án, chạy:

```bash
docker compose config --quiet
```

Nếu lệnh kết thúc mà không báo lỗi, cấu hình Docker Compose hợp lệ.

### 3.2. Build và khởi chạy

```bash
docker compose up -d --build
```

Lệnh trên thực hiện:

1. Tải image PostgreSQL nếu chưa có.
2. Build Docker image cho backend.
3. Build Docker image cho frontend.
4. Tạo Docker network và volume cần thiết.
5. Khởi chạy các container.

Ở lần chạy đầu tiên, quá trình tải image và build có thể mất vài phút.

### 3.3. Kiểm tra trạng thái container

```bash
docker compose ps
```

Kết quả mong đợi:

| Container | Trạng thái |
|---|---|
| `career-recommendation-postgres` | `Up (healthy)` |
| `career-recommendation-backend` | `Up` |
| `career-recommendation-frontend` | `Up` |

Trạng thái `Up` xác nhận container đang chạy nhưng chưa đủ để khẳng định toàn bộ chức năng ứng dụng hoạt động chính xác.

### 3.4. Truy cập các dịch vụ

Với cấu hình cổng mặc định:

| Dịch vụ | Địa chỉ |
|---|---|
| Frontend Web | `http://localhost:3000` |
| Backend REST API | `http://localhost:8080` |
| PostgreSQL | `localhost:5432` |

**Frontend:**

Mở `http://localhost:3000` trên trình duyệt.

Ở giai đoạn khởi tạo dự án, frontend có thể hiển thị:

```text
Frontend skeleton initialized successfully.
```

Đây là giao diện khởi tạo, chưa phải toàn bộ chức năng của hệ thống.

**Backend:**

Backend chạy trên cổng `8080`.

Trong cấu hình Spring Security hiện tại, truy cập trực tiếp backend có thể chuyển hướng đến `/login`.

Đây là hành vi xác thực mặc định, không đồng nghĩa với lỗi Docker.

Không mặc định coi `/actuator/health` là endpoint kiểm tra sức khỏe hợp lệ khi chưa xác nhận dự án đã cấu hình Spring Boot Actuator và quyền truy cập tương ứng.

### 3.5. Kiểm tra log backend

```bash
docker compose logs backend --tail 50
```

Các thông báo cần kiểm tra:

**Spring Boot khởi động thành công:**

```text
Started CareerRecommendationApplication
```

**Kết nối PostgreSQL thành công:**

```text
HikariPool-1 - Start completed.
```

**Flyway migration thành công:**

```text
Successfully applied 1 migration
```

Các thông báo này xác nhận backend khởi động, thiết lập kết nối cơ sở dữ liệu và áp dụng migration thành công.

### 3.6. Xem log các dịch vụ

Xem log toàn bộ hệ thống:

```bash
docker compose logs -f
```

Xem log từng dịch vụ:

```bash
docker compose logs postgres --tail 50
docker compose logs backend --tail 50
docker compose logs frontend --tail 50
```

Nhấn `Ctrl + C` để thoát chế độ theo dõi log. Thao tác này không dừng các container đang chạy ở chế độ nền.

### 3.7. Dừng và khởi động lại hệ thống

Dừng các container:

```bash
docker compose stop
```

Khởi động lại:

```bash
docker compose start
```

Dừng và xóa các container cùng network do Compose quản lý:

```bash
docker compose down
```

Lệnh `docker compose down` mặc định không xóa named volume PostgreSQL.

Để chạy lại:

```bash
docker compose up -d
```

> [!WARNING]
> Không tùy tiện chạy `docker compose down -v` vì tùy chọn `-v` sẽ xóa các volume do Compose quản lý, bao gồm volume lưu dữ liệu PostgreSQL của dự án.

---

## 4. Chạy từng dịch vụ độc lập cho quá trình phát triển (Local Development)

### 4.1. Khởi động riêng PostgreSQL

Tại thư mục gốc:

```bash
docker compose up -d postgres
```

Kiểm tra trạng thái:

```bash
docker compose ps
```

Đợi PostgreSQL chuyển sang trạng thái `healthy` trước khi chạy backend.

### 4.2. Khởi chạy Backend (Spring Boot)

Yêu cầu JDK 17 và Maven.

Từ thư mục gốc dự án:

```bash
cd backend
mvn clean spring-boot:run
```

Hoặc chạy lớp `CareerRecommendationApplication.java` từ IDE như IntelliJ IDEA, VS Code hoặc Eclipse.

Khi chạy backend trực tiếp trên máy, cần bảo đảm cấu hình kết nối database trỏ đến PostgreSQL đang chạy trên host và các biến môi trường cần thiết được cung cấp cho ứng dụng.

Không chạy đồng thời backend cục bộ và backend Docker trên cùng cổng `8080`.

### 4.3. Khởi chạy Frontend (React + TypeScript)

Yêu cầu Node.js và npm.

Từ thư mục gốc dự án:

```bash
cd frontend
npm install
npm run dev
```

Địa chỉ truy cập frontend trong chế độ phát triển phụ thuộc vào cấu hình Vite và cổng được hiển thị trong Terminal.

Không mặc định sử dụng cổng `3000` cho Vite nếu chưa kiểm tra cấu hình. Cổng `3000` là cổng mặc định của frontend khi chạy bằng Docker Compose trong dự án này.

Không chạy đồng thời hai ứng dụng trên cùng một cổng.

---

## 5. Quy trình kiểm tra & Xác thực chất lượng mã nguồn (Verification Commands)

Trước khi gửi Pull Request hoặc hoàn tất tác vụ, cần thực hiện các bước kiểm tra phù hợp với phạm vi thay đổi.

| Hạng mục | Thư mục | Lệnh | Tiêu chí đạt |
|---|---|---|---|
| Kiểm tra TypeScript và build frontend | `frontend/` | `npm run build` | Build thành công |
| Biên dịch backend | `backend/` | `mvn clean compile` | `BUILD SUCCESS` |
| Kiểm thử backend | `backend/` | `mvn test` | Không có test thất bại |
| Kiểm tra cấu hình Compose | Thư mục gốc | `docker compose config --quiet` | Không báo lỗi |
| Build và chạy Docker | Thư mục gốc | `docker compose up -d --build` | Build và khởi chạy thành công |
| Kiểm tra container | Thư mục gốc | `docker compose ps` | Các service ở trạng thái mong đợi |
| Kiểm tra Git | Thư mục gốc | `git status -sb` | Chỉ có các thay đổi dự kiến |

### 5.1. Kết quả kiểm thử môi trường W1-T6

**Ngày kiểm thử:** 30/10/2026.

**Môi trường kiểm thử:**

- Hệ điều hành: Windows 11.
- Docker Desktop: 4.97.0.
- Docker Engine: 29.9.2.
- Docker Compose: v5.6.1.
- Backend: Spring Boot 3.3.4, Java 17.
- Database: PostgreSQL 16.
- Frontend: React + TypeScript, phục vụ bằng Nginx.

**Kết quả thực tế:**

| Hạng mục | Kết quả |
|---|---|
| `docker compose config --quiet` | PASS |
| `docker compose up -d --build` | PASS |
| Build backend image | PASS |
| Build frontend image | PASS |
| PostgreSQL container | PASS — Healthy |
| Backend container | PASS — Up |
| Frontend container | PASS — Up |
| Truy cập `http://localhost:3000` | PASS — Hiển thị frontend skeleton |
| Spring Boot khởi động | PASS |
| Kết nối backend đến PostgreSQL | PASS |
| Flyway migration V1 | PASS |
| Kiểm thử đầy đủ các chức năng API | Chưa thực hiện |

**Kết luận:** Môi trường Docker Compose đã được xác nhận có thể build và khởi chạy thành công cả ba dịch vụ trên máy kiểm thử. Việc kiểm thử chức năng nghiệp vụ và API sẽ được thực hiện khi các module tương ứng được phát triển.

---

## 6. Xử lý sự cố thường gặp (Troubleshooting)

### 6.1. Docker Engine chưa chạy

**Biểu hiện:** Lệnh Docker không kết nối được với Docker daemon.

**Cách xử lý:**

1. Mở Docker Desktop.
2. Đợi Docker Engine khởi động hoàn tất.
3. Chạy lại:

```bash
docker info
```

### 6.2. Cổng 3000, 8080 hoặc 5432 đã được sử dụng

**Biểu hiện:** Docker báo lỗi không thể bind cổng.

**Cách xử lý:**

- Kiểm tra ứng dụng hoặc container khác đang sử dụng cổng.
- Dừng dịch vụ gây xung đột nếu phù hợp.
- Hoặc thay đổi `FRONTEND_PORT`, `BACKEND_PORT`, `POSTGRES_PORT` trong `.env` khi cần.

**Lưu ý:** Các biến cổng trong Compose đang ánh xạ cổng trên máy host. Không tự ý thay đổi cổng bên trong container nếu chưa cập nhật cấu hình ứng dụng tương ứng.

### 6.3. Backend không kết nối được PostgreSQL

Kiểm tra trạng thái PostgreSQL:

```bash
docker compose ps
```

Kiểm tra log:

```bash
docker compose logs postgres --tail 50
docker compose logs backend --tail 50
```

Xác nhận:

- PostgreSQL đã ở trạng thái `healthy`.
- Backend sử dụng hostname `postgres` khi chạy trong Docker.
- Tên database, username và password phù hợp với database đã khởi tạo.

Nếu database đã tồn tại trong volume, việc sửa `POSTGRES_PASSWORD` trong `.env` không tự động thay đổi mật khẩu PostgreSQL hiện có.

### 6.4. Backend chuyển hướng đến trang đăng nhập

Nếu truy cập `http://localhost:8080` và thấy trang đăng nhập Spring Security, điều đó cho thấy backend đang yêu cầu xác thực.

Để kiểm tra khởi động ứng dụng, sử dụng:

```bash
docker compose logs backend --tail 50
```

Không tự ý vô hiệu hóa Spring Security chỉ để loại bỏ trang đăng nhập.

### 6.5. Frontend hiển thị giao diện khởi tạo

Nếu trang web hiển thị `Frontend skeleton initialized successfully`, frontend đã được phục vụ thành công.

Đây là trạng thái phù hợp với giai đoạn skeleton của dự án, không phải lỗi Docker.

### 6.6. Container tự dừng hoặc khởi động lại liên tục

Kiểm tra:

```bash
docker compose ps
docker compose logs --tail 100
```

Đọc thông báo lỗi của service liên quan trước khi thay đổi cấu hình hoặc xóa dữ liệu.

### 6.7. Docker build chậm hoặc thiếu tài nguyên

Lần build đầu tiên có thể chậm vì Docker phải tải image, Maven dependencies và npm packages.

Trên máy có RAM hạn chế:

- Đóng bớt ứng dụng không cần thiết.
- Kiểm tra dung lượng ổ đĩa còn trống.
- Đợi quá trình build hoàn tất trước khi kết luận có lỗi.
- Không xóa Docker volume hoặc image hàng loạt khi chưa xác định nguyên nhân.

---

## 7. Quy chuẩn phối hợp và phong cách lập trình

Để bảo đảm chất lượng kỹ thuật giữa các thành viên và AI agent, tham khảo:

- [Quy trình làm việc với Git & Kiểm soát mã nguồn](git-workflow.md): Quy trình phân nhánh, Conventional Commits, Pull Request và code review.
- [Quy chuẩn viết mã & Kiến trúc phần mềm](coding-conventions.md): Quy ước lập trình Java/Spring Boot, React/TypeScript và tổ chức mã nguồn.
- [Hướng dẫn cấu hình Docker](../../docker/README.md): Tổng quan các dịch vụ và lệnh Docker.
- [Quy tắc làm việc với AI](../../AI_RULES.md): Các quy định áp dụng khi sử dụng AI trong dự án.

Mọi thay đổi cần tuân thủ quy trình Git và yêu cầu tài liệu của nhóm. Không đưa mật khẩu, khóa bí mật hoặc dữ liệu nhạy cảm vào commit.

---

## 8. Ghi chú về phạm vi W1-T6

Tác vụ **W1-T6 — Docker Compose & Development Environment** tập trung vào:

- Xác minh cấu hình Docker Compose.
- Build và khởi chạy PostgreSQL, backend, frontend.
- Kiểm tra kết nối giữa các dịch vụ.
- Chuẩn hóa hướng dẫn thiết lập môi trường phát triển.
- Ghi nhận kết quả kiểm thử môi trường.
- Chuẩn bị tài liệu và DevLog theo quy định nhóm.

Các chức năng nghiệp vụ như đăng nhập, quản lý người dùng, phân tích kỹ năng và đề xuất nghề nghiệp nằm ngoài phạm vi kiểm thử khởi động Docker của tác vụ này.
Để đảm bảo chất lượng kỹ thuật đồng đều giữa 06 thành viên và các AI agent:
* [**Quy trình làm việc với Git & Kiểm soát mã nguồn (`git-workflow.md`)**](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/development/git-workflow.md): Mô hình phân nhánh Trunk-based + Feature branch, quy chuẩn Conventional Commits, mẫu PR checklist và chính sách review.
* [**Quy chuẩn viết mã & Kiến trúc phần mềm (`coding-conventions.md`)**](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/development/coding-conventions.md): Quy chuẩn phân chia package Java/Spring Boot theo Domain, quy ước JPA Entity, kiến trúc thư mục Frontend React/TypeScript và nguyên tắc Clean Code.


## 7. Xác thực W1-T5

Đăng ký, đăng nhập, JWT/RBAC và hướng dẫn kiểm thử Windows: [w1-t5-auth-guide.md](w1-t5-auth-guide.md).
