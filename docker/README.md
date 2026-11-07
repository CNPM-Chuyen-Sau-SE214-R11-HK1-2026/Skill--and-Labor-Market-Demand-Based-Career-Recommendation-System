# Hướng dẫn cấu hình Docker (Docker Configuration Guide)

Tài liệu này hướng dẫn cách thiết lập, khởi chạy, kiểm tra và quản lý môi trường Docker cho dự án **Skill- and Labor Market Demand–Based Career Recommendation System**.

Hệ thống sử dụng **Docker Compose** để chạy đồng thời ba dịch vụ: PostgreSQL, Backend Spring Boot và Frontend React.

Để xem hướng dẫn phát triển đầy đủ, tham khảo [Hướng dẫn phát triển và môi trường](../docs/development/README.md).

---

## 1. Yêu cầu cài đặt

Trước khi chạy dự án, máy tính cần có:

- **Git:** Quản lý mã nguồn.
- **Docker Desktop:** Dành cho Windows.
- **Docker Engine:** Dành cho Linux.
- **Docker Compose:** Phiên bản v2 trở lên.

Trên Windows, khuyến nghị sử dụng Docker Desktop với WSL 2.

Kiểm tra các công cụ bằng Terminal:

```bash
docker --version
docker compose version
docker info
```

Nếu Docker Engine đang hoạt động, lệnh `docker info` sẽ hiển thị thông tin Docker Client và Server.

**Lưu ý:** Khi chạy toàn bộ hệ thống bằng Docker Compose, không bắt buộc cài đặt riêng Java, Maven hoặc Node.js trên máy tính. Các công cụ cần thiết đã được khai báo trong Dockerfile.

---

## 2. Tổng quan các dịch vụ

File `docker-compose.yml` tại thư mục gốc dự án cấu hình ba dịch vụ:

| Dịch vụ | Công nghệ | Tên container | Cổng mặc định |
|---|---|---|---|
| PostgreSQL | PostgreSQL 16 Alpine | `career-recommendation-postgres` | 5432 |
| Backend | Spring Boot, Java 17 | `career-recommendation-backend` | 8080 |
| Frontend | React, TypeScript, Nginx | `career-recommendation-frontend` | 3000 |

### 2.1. PostgreSQL

PostgreSQL đảm nhiệm việc lưu trữ dữ liệu của hệ thống.

Cấu hình bao gồm:

- Sử dụng image `postgres:16-alpine`.
- Nhận tên cơ sở dữ liệu, tài khoản và mật khẩu từ biến môi trường.
- Sử dụng volume `postgres_data` để lưu trữ dữ liệu lâu dài.
- Kiểm tra trạng thái hoạt động bằng `pg_isready`.
- Kết nối với các dịch vụ khác thông qua Docker network.

Dữ liệu PostgreSQL được lưu trong volume nên không bị mất khi chỉ dừng hoặc tạo lại container theo cách thông thường.

### 2.2. Backend

Backend được xây dựng từ file `backend/Dockerfile`.

Cấu hình bao gồm:

- Sử dụng Java 17.
- Xây dựng ứng dụng Spring Boot bằng Maven.
- Kết nối PostgreSQL thông qua hostname `postgres`.
- Chờ PostgreSQL đạt trạng thái `healthy` trước khi khởi chạy.
- Ánh xạ cổng `8080` của máy tính vào cổng `8080` của container.

### 2.3. Frontend

Frontend được xây dựng từ file `frontend/Dockerfile`.

Cấu hình bao gồm:

- Sử dụng Node.js để build ứng dụng React và TypeScript.
- Sử dụng Nginx để phục vụ giao diện sau khi build.
- Ánh xạ cổng `3000` của máy tính vào cổng `80` của container.
- Khởi chạy sau khi container backend được bắt đầu.

**Lưu ý:** Việc frontend phụ thuộc vào backend không đồng nghĩa với việc Docker Compose đã kiểm tra backend sẵn sàng xử lý API.

---

## 3. Thiết lập biến môi trường

Dự án sử dụng file `.env` tại thư mục gốc để quản lý các biến môi trường.

### 3.1. Tạo file `.env`

Mở Terminal tại thư mục gốc dự án.

**Trên Windows PowerShell:**

```powershell
Copy-Item .env.example .env
```

**Trên Linux, macOS hoặc Git Bash:**

```bash
cp .env.example .env
```

Chỉ sao chép nếu file `.env` chưa tồn tại để tránh ghi đè cấu hình đang sử dụng.

### 3.2. Các biến môi trường

| Biến | Chức năng | Giá trị mẫu |
|---|---|---|
| `POSTGRES_DB` | Tên cơ sở dữ liệu | `career_recommendation_db` |
| `POSTGRES_USER` | Tài khoản PostgreSQL | `postgres` |
| `POSTGRES_PASSWORD` | Mật khẩu PostgreSQL | Cần thiết lập |
| `POSTGRES_HOST` | Địa chỉ PostgreSQL khi kết nối từ máy tính | `localhost` |
| `POSTGRES_PORT` | Cổng PostgreSQL trên máy tính | `5432` |
| `BACKEND_PORT` | Cổng Backend trên máy tính | `8080` |
| `FRONTEND_PORT` | Cổng Frontend trên máy tính | `3000` |
| `JWT_SECRET` | Khóa bí mật phục vụ xác thực JWT | Cần thiết lập |
| `JWT_EXPIRATION_MS` | Thời gian hết hạn JWT, tính bằng mili giây | `86400000` |

Thay các giá trị mẫu của `POSTGRES_PASSWORD` và `JWT_SECRET` bằng giá trị riêng khi thiết lập môi trường.

### 3.3. Lưu ý về kết nối PostgreSQL

Khi kết nối từ Windows hoặc máy tính cá nhân:

```text
localhost:5432
```

Khi backend chạy bên trong Docker Compose:

```text
postgres:5432
```

Nguyên nhân là Docker Compose cho phép các container giao tiếp thông qua tên dịch vụ.

File `docker-compose.yml` đã cấu hình hostname `postgres` cho backend.

### 3.4. Bảo mật biến môi trường

**Tuyệt đối không commit file `.env` lên Git.**

File `.gitignore` của dự án đã loại trừ `.env` và cho phép commit `.env.example`.

Không đưa mật khẩu thật hoặc khóa bí mật vào `.env.example`.

**Lưu ý quan trọng:** Nếu PostgreSQL đã được khởi tạo trong volume, việc thay đổi `POSTGRES_PASSWORD` trong `.env` không tự động cập nhật mật khẩu của tài khoản PostgreSQL hiện có.

Không xóa volume để xử lý lỗi mật khẩu khi chưa xác định rõ hậu quả đối với dữ liệu.

---

## 4. Khởi chạy hệ thống bằng Docker Compose

Tất cả lệnh trong phần này được thực hiện tại **thư mục gốc dự án**, nơi chứa file `docker-compose.yml`.

### 4.1. Kiểm tra cấu hình Docker Compose

Chạy lệnh:

```bash
docker compose config --quiet
```

Nếu lệnh kết thúc mà không báo lỗi, cấu hình Docker Compose hợp lệ.

### 4.2. Build và khởi chạy toàn bộ hệ thống

Chạy lệnh:

```bash
docker compose up -d --build
```

Lệnh này thực hiện:

1. Tải các Docker image cần thiết.
2. Build image cho Backend.
3. Build image cho Frontend.
4. Tạo Docker network.
5. Tạo volume lưu trữ PostgreSQL nếu chưa tồn tại.
6. Khởi chạy ba dịch vụ.

Ở lần chạy đầu tiên, quá trình tải image và build có thể mất vài phút.

### 4.3. Kiểm tra trạng thái container

Chạy lệnh:

```bash
docker compose ps
```

Kết quả mong đợi:

| Container | Trạng thái |
|---|---|
| `career-recommendation-postgres` | `Up (healthy)` |
| `career-recommendation-backend` | `Up` |
| `career-recommendation-frontend` | `Up` |

Nếu cả ba container đều hoạt động, quá trình khởi chạy môi trường đã thành công.

Tuy nhiên, trạng thái `Up` chỉ xác nhận container đang chạy, chưa đủ để khẳng định toàn bộ chức năng của ứng dụng hoạt động chính xác.

---

## 5. Kiểm tra hoạt động của hệ thống

### 5.1. Kiểm tra Frontend

Mở trình duyệt và truy cập:

```text
http://localhost:3000
```

Ở giai đoạn khởi tạo dự án, giao diện có thể hiển thị thông báo:

```text
Frontend skeleton initialized successfully.
```

Thông báo này cho thấy Frontend đã được build và phục vụ thành công thông qua Nginx.

Đây là giao diện khởi tạo, chưa phải toàn bộ chức năng của hệ thống.

### 5.2. Kiểm tra Backend

Backend sử dụng cổng mặc định:

```text
http://localhost:8080
```

Kiểm tra log Backend:

```bash
docker compose logs backend --tail 50
```

Nếu ứng dụng khởi động thành công, log sẽ xuất hiện thông báo:

```text
Started CareerRecommendationApplication
```

**Lưu ý về Spring Security:**

Trong cấu hình hiện tại, khi truy cập Backend bằng trình duyệt, Spring Security có thể chuyển hướng đến trang:

```text
http://localhost:8080/login
```

Đây là hành vi xác thực mặc định của Spring Security, không đồng nghĩa với lỗi Docker.

Không tự ý vô hiệu hóa Spring Security chỉ để bỏ qua trang đăng nhập.

Đường dẫn `/actuator/health` chỉ có thể sử dụng để kiểm tra sức khỏe ứng dụng nếu dự án đã cấu hình Spring Boot Actuator và quyền truy cập phù hợp.

### 5.3. Kiểm tra kết nối PostgreSQL

Kiểm tra trạng thái PostgreSQL:

```bash
docker compose ps
```

Container PostgreSQL cần đạt trạng thái:

```text
Up (healthy)
```

Ngoài ra, có thể kiểm tra log Backend:

```bash
docker compose logs backend --tail 50
```

Thông báo kết nối cơ sở dữ liệu thành công:

```text
HikariPool-1 - Start completed.
```

Thông báo Flyway áp dụng migration thành công trong lần khởi tạo đầu tiên:

```text
Successfully applied 1 migration
```

Các thông báo này cho thấy Backend đã kết nối được PostgreSQL và quá trình khởi tạo cấu trúc cơ sở dữ liệu đã thực hiện thành công.

Ở những lần khởi động tiếp theo, Flyway có thể không chạy lại migration đã được áp dụng.

---

## 6. Xem nhật ký hoạt động (Logs)

### 6.1. Xem log toàn bộ hệ thống

```bash
docker compose logs -f
```

Nhấn `Ctrl + C` để thoát chế độ theo dõi log.

Nếu các container được khởi chạy bằng tùy chọn `-d`, thao tác này không dừng các container.

### 6.2. Xem log PostgreSQL

```bash
docker compose logs postgres --tail 50
```

### 6.3. Xem log Backend

```bash
docker compose logs backend --tail 50
```

### 6.4. Xem log Frontend

```bash
docker compose logs frontend --tail 50
```

---

## 7. Dừng và khởi động lại hệ thống

### 7.1. Dừng các container

```bash
docker compose stop
```

Lệnh này dừng container nhưng không xóa container hoặc volume.

### 7.2. Khởi động lại các container đã dừng

```bash
docker compose start
```

### 7.3. Dừng và xóa container

```bash
docker compose down
```

Lệnh này xóa các container và network do Docker Compose quản lý.

Theo mặc định, named volume PostgreSQL không bị xóa.

### 7.4. Khởi chạy lại hệ thống

```bash
docker compose up -d
```

### 7.5. Build lại sau khi thay đổi mã nguồn

```bash
docker compose up -d --build
```

**Cảnh báo:**

Không tùy tiện chạy:

```bash
docker compose down -v
```

Tùy chọn `-v` sẽ xóa các volume do Docker Compose quản lý, bao gồm volume chứa dữ liệu PostgreSQL.

Chỉ sử dụng khi thực sự muốn xóa dữ liệu và đã xác nhận hậu quả.

---

## 8. Xử lý lỗi thường gặp (Troubleshooting)

### 8.1. Docker Engine chưa hoạt động

**Biểu hiện:**

Terminal báo lỗi không thể kết nối đến Docker daemon.

**Cách xử lý:**

1. Mở Docker Desktop.
2. Chờ Docker Engine khởi động hoàn tất.
3. Chạy lại:

```bash
docker info
```

### 8.2. Cổng đã được sử dụng

**Biểu hiện:**

Docker không thể sử dụng cổng `3000`, `8080` hoặc `5432`.

**Nguyên nhân:**

Một ứng dụng hoặc container khác đang sử dụng cổng tương ứng.

**Cách xử lý:**

- Kiểm tra dịch vụ đang chiếm cổng.
- Dừng dịch vụ gây xung đột nếu phù hợp.
- Hoặc thay đổi cổng trên máy host thông qua các biến trong `.env`.

Không tự ý thay đổi cổng bên trong container nếu chưa cập nhật cấu hình ứng dụng tương ứng.

### 8.3. Backend không kết nối được PostgreSQL

**Biểu hiện:**

Log Backend xuất hiện lỗi kết nối hoặc xác thực PostgreSQL.

**Cách xử lý:**

Kiểm tra trạng thái container:

```bash
docker compose ps
```

Kiểm tra log:

```bash
docker compose logs postgres --tail 50
docker compose logs backend --tail 50
```

Xác nhận:

- PostgreSQL đã đạt trạng thái `healthy`.
- Backend sử dụng hostname `postgres`.
- Tên cơ sở dữ liệu, tài khoản và mật khẩu phù hợp với PostgreSQL đã khởi tạo.

Không xóa volume PostgreSQL như bước xử lý đầu tiên.

### 8.4. Backend hiển thị trang đăng nhập

**Biểu hiện:**

Truy cập Backend và trình duyệt chuyển đến `/login`.

**Giải thích:**

Spring Security đang yêu cầu xác thực.

Đây không phải dấu hiệu cho thấy Docker bị lỗi.

Kiểm tra log Backend để xác nhận ứng dụng đã khởi động thành công.

### 8.5. Frontend chỉ hiển thị giao diện khởi tạo

**Biểu hiện:**

Trình duyệt hiển thị:

```text
Frontend skeleton initialized successfully.
```

**Giải thích:**

Frontend đang ở giai đoạn khởi tạo.

Đây là trạng thái bình thường khi các chức năng nghiệp vụ chưa được phát triển đầy đủ.

### 8.6. Container tự dừng hoặc khởi động lại liên tục

Kiểm tra:

```bash
docker compose ps
docker compose logs --tail 100
```

Đọc thông báo lỗi của dịch vụ liên quan trước khi thay đổi cấu hình.

### 8.7. Docker build chậm

Lần build đầu tiên có thể mất nhiều thời gian vì phải tải:

- Docker base image.
- Maven dependencies.
- npm dependencies.

Trên máy có RAM hạn chế, nên đóng các ứng dụng không cần thiết và kiểm tra dung lượng ổ đĩa còn trống.

Không xóa hàng loạt image hoặc volume khi chưa xác định nguyên nhân.

---

## 9. Kết quả kiểm thử môi trường W1-T6

**Mã công việc:** W1-T6

**Tên công việc:** Docker Compose & Development Environment

**Ngày kiểm thử:** 30/10/2026

### 9.1. Môi trường kiểm thử

| Thành phần | Phiên bản |
|---|---|
| Hệ điều hành | Windows 11 |
| Docker Desktop | 4.97.0 |
| Docker Engine | 29.9.2 |
| Docker Compose | v5.6.1 |
| Java | 17 |
| Spring Boot | 3.3.4 |
| PostgreSQL | 16 |
| Frontend | React + TypeScript |
| Web Server Frontend | Nginx |

### 9.2. Kết quả kiểm thử

| Hạng mục | Kết quả |
|---|---|
| Kiểm tra cấu hình Docker Compose | ĐẠT |
| Tải image PostgreSQL | ĐẠT |
| Build Docker image Backend | ĐẠT |
| Build Docker image Frontend | ĐẠT |
| Tạo Docker network | ĐẠT |
| Tạo PostgreSQL volume | ĐẠT |
| PostgreSQL healthcheck | ĐẠT |
| Khởi động Backend container | ĐẠT |
| Khởi động Frontend container | ĐẠT |
| Truy cập Frontend trên trình duyệt | ĐẠT |
| Spring Boot khởi động | ĐẠT |
| Backend kết nối PostgreSQL | ĐẠT |
| Flyway migration V1 | ĐẠT |
| Kiểm thử toàn bộ chức năng API | Chưa thực hiện |

### 9.3. Kết luận

Môi trường Docker Compose đã được kiểm thử thành công trên Windows 11.

Cả ba dịch vụ PostgreSQL, Backend và Frontend đều được build và khởi chạy thành công.

Kết quả kiểm tra thực tế:

- PostgreSQL hoạt động ở trạng thái `healthy`.
- Backend Spring Boot khởi động thành công trên cổng `8080`.
- Backend kết nối thành công đến PostgreSQL.
- Flyway áp dụng thành công migration V1.
- Frontend hiển thị giao diện khởi tạo tại `http://localhost:3000`.

**Kết luận:** Môi trường Docker Compose đáp ứng yêu cầu khởi chạy ban đầu của tác vụ W1-T6.

Việc kiểm thử API và các chức năng nghiệp vụ sẽ được thực hiện khi các module tương ứng được hoàn thiện.

---

## 10. Tài liệu liên quan

- [Hướng dẫn phát triển và môi trường](../docs/development/README.md)
- [README chính của dự án](../README.md)
- [Cấu hình Docker Compose](../docker-compose.yml)
- [File biến môi trường mẫu](../.env.example)
- [Quy tắc làm việc với AI](../AI_RULES.md)

Mọi thay đổi liên quan đến Docker cần tuân thủ quy trình Git, quy định tài liệu và yêu cầu kiểm tra mã nguồn của nhóm.