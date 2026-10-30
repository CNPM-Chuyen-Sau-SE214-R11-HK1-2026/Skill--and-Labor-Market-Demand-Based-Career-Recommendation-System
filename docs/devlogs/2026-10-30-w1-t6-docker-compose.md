# DevLog: W1-T6 — Docker Compose và môi trường phát triển

- **Mã tác vụ / Task ID:** W1-T6
- **Người thực hiện / Agent:** Thành viên phụ trách W1-T6, có hỗ trợ từ ChatGPT
- **Ngày thực hiện (Date):** 2026-10-30
- **Nhánh Git (Branch):** `ops/w1-t6-docker-compose`

---

## 1. Mục tiêu (Objective)

Thiết lập và kiểm tra môi trường phát triển bằng Docker Compose cho hệ thống gợi ý nghề nghiệp.

Theo bảng phân công W1-T6, các yêu cầu bao gồm:

- Kiểm tra môi trường Docker Compose với ba dịch vụ Frontend, Backend và PostgreSQL.
- Kiểm tra trạng thái hoạt động và quá trình khởi động các dịch vụ.
- Kiểm tra mẫu biến môi trường và các lưu ý bảo mật.
- Xác minh URL API có thể truy cập từ trình duyệt.
- Kiểm tra khả năng Frontend gọi Backend.
- Viết hướng dẫn khởi chạy và khởi chạy lại môi trường.

---

## 2. Thay đổi thực tế (Changes)

### Mã nguồn (Source Code)

Không chỉnh sửa mã nguồn nghiệp vụ của Frontend hoặc Backend.

Không thay đổi `docker-compose.yml` vì cấu hình hiện tại đã build và khởi chạy thành công.

### Cấu hình và môi trường (Config & Environment)

Đã kiểm thử Docker Compose trên Windows 11 với:

- Docker Desktop 4.97.0.
- Docker Engine 29.9.2.
- Docker Compose v5.6.1.
- PostgreSQL 16 Alpine.
- Backend Spring Boot sử dụng Java 17.
- Frontend React, TypeScript và Nginx.

Đã thực hiện:

- Kiểm tra tính hợp lệ của cấu hình Docker Compose.
- Build Docker image cho Backend và Frontend.
- Khởi chạy ba container.
- Kiểm tra trạng thái PostgreSQL.
- Kiểm tra log khởi động Backend.
- Xác minh kết nối Backend–PostgreSQL và Flyway migration V1.
- Truy cập Frontend và Backend qua trình duyệt.
- Dừng và khởi động lại cả ba container bằng Docker Compose.

Đã rà soát `.env.example` và xác nhận file sử dụng các giá trị mẫu thay vì mật khẩu thật.

Tuy nhiên, `docker-compose.yml` vẫn có mật khẩu PostgreSQL và JWT secret mặc định dành cho phát triển. Cấu hình này không được xem là phù hợp để triển khai production.

### Tài liệu chuẩn (Documentation Updates)

Đã chỉnh sửa:

- `docs/development/README.md`: Hướng dẫn thiết lập và sử dụng môi trường phát triển.
- `docker/README.md`: Hướng dẫn Docker Compose, biến môi trường, kiểm tra dịch vụ và xử lý lỗi.

Đã tạo:

- `docs/devlogs/2026-10-30-w1-t6-docker-compose.md`: Ghi nhận quá trình thực hiện W1-T6.

---

## 3. Khu vực bị ảnh hưởng (Affected Areas)

Các file được thay đổi hoặc bổ sung:

- `docker/README.md`
- `docs/development/README.md`
- `docs/devlogs/2026-10-30-w1-t6-docker-compose.md`

Các file cấu hình được sử dụng để kiểm thử nhưng không chỉnh sửa:

- `docker-compose.yml`
- `backend/Dockerfile`
- `frontend/Dockerfile`
- `.env.example`

Các thành phần được kiểm tra:

- PostgreSQL.
- Backend Spring Boot.
- Frontend React.
- Docker Compose.
- Kết nối Backend–PostgreSQL.

---

## 4. Quyết định kỹ thuật quan trọng (Decisions & Rationale)

### Giữ nguyên Docker Compose hiện có

Cấu hình hiện tại đã build và chạy được ba dịch vụ nên không thay đổi khi chưa phát hiện lỗi cần sửa.

### Sử dụng Docker network để kết nối dịch vụ

Backend sử dụng hostname `postgres` để kết nối PostgreSQL trong Docker Compose.

### Bảo toàn dữ liệu PostgreSQL

Sử dụng volume `postgres_data` để lưu dữ liệu.

Trong quá trình kiểm thử khởi động lại, sử dụng `docker compose stop` và `docker compose start`, không dùng `docker compose down -v`.

### Không chỉnh sửa Spring Security ngoài phạm vi tác vụ

Backend hiện có thể chuyển hướng đến `/login` khi truy cập từ trình duyệt.

Không tự ý thay đổi cơ chế xác thực chỉ để kiểm thử Docker.

### Tách phần môi trường và phần tích hợp nghiệp vụ

W1-T6 chịu trách nhiệm kiểm tra môi trường Docker Compose.

Việc Frontend gọi Backend cần phối hợp với các thành viên phụ trách W1-T2 và W1-T3.

Không tự nhận đã hoàn thành kiểm thử tích hợp khi chưa có kết quả thực tế.

---

## 5. Kiểm tra & Xác thực (Validation Performed)

### Kết quả đã xác minh

| Lệnh / Thao tác kiểm tra | Mục đích | Kết quả thực tế | Ghi chú |
|---|---|---|---|
| `docker compose config --quiet` | Kiểm tra cấu hình Compose | Thành công | Không báo lỗi |
| `docker compose up -d --build` | Build và khởi chạy hệ thống | Thành công | Ba dịch vụ khởi chạy |
| `docker compose ps` | Kiểm tra container | Thành công | PostgreSQL `healthy`, Backend và Frontend `Up` |
| Truy cập `http://localhost:3000` | Kiểm tra Frontend | Thành công | Hiển thị giao diện khởi tạo |
| Truy cập `http://localhost:8080` | Kiểm tra phản hồi Backend | Có phản hồi | Chuyển hướng đến `/login` |
| `docker compose logs backend --tail 50` | Kiểm tra Backend | Thành công | Log ghi nhận Spring Boot khởi động |
| Kiểm tra log HikariCP | Kiểm tra kết nối cơ sở dữ liệu | Thành công | Kết nối PostgreSQL |
| Kiểm tra log Flyway | Kiểm tra migration | Thành công | Migration V1 được áp dụng |
| `docker compose stop` | Dừng môi trường | Thành công | Dừng cả ba container |
| `docker compose start` | Khởi động lại môi trường | Thành công | Ba container được khởi động lại |
| `docker compose ps` sau khi restart | Kiểm tra trạng thái | Thành công | PostgreSQL `healthy`, Backend và Frontend `Up` |
| `git diff --check` | Kiểm tra lỗi khoảng trắng | Thành công tại thời điểm kiểm tra | Cần kiểm tra lại trước commit |
| `git status -sb` | Kiểm tra nhánh và file thay đổi | Thành công | Đúng nhánh W1-T6 |

### Giới hạn của kết quả kiểm thử

- Đã xác nhận Docker Compose có thể dừng và khởi động lại ba container.
- Chưa kiểm thử quy trình dựng môi trường hoàn toàn mới từ đầu trên một máy khác.
- Log Backend sau lệnh `docker compose start` có chứa thông tin từ lần khởi động trước, nên chưa dùng log đó để xác nhận Backend đã sẵn sàng xử lý API sau lần restart.
- Chưa xác minh Frontend gọi Backend thành công.
- Chưa kiểm thử đầy đủ các API nghiệp vụ.
- Chưa chạy bộ unit test Backend trong phạm vi W1-T6.

---

## 6. Công việc tồn đọng & Bước tiếp theo (Outstanding Work)

### Phần cần phối hợp với các tác vụ khác

- [ ] Phối hợp W1-T2 và W1-T3 để kiểm tra Frontend gọi API Backend thành công.
- [ ] Xác minh endpoint phù hợp có thể truy cập từ trình duyệt.
- [ ] Kiểm tra tích hợp sau khi các thành viên hoàn thiện phần Frontend và Backend liên quan.

### Phần cần xác minh thêm

- [ ] Kiểm tra quy trình dựng môi trường từ đầu trên máy khác khi có điều kiện.
- [ ] Kiểm tra trạng thái sẵn sàng xử lý API của Backend sau khi restart.
- [ ] Đánh giá lại các giá trị mặc định trong Docker Compose trước khi sử dụng ngoài môi trường phát triển.

### Hoàn tất quy trình Git

- [ ] Kiểm tra toàn bộ thay đổi lần cuối.
- [ ] Commit trên nhánh `ops/w1-t6-docker-compose`.
- [ ] Push nhánh và tạo Pull Request.
- [ ] Chờ nhóm review và nghiệm thu.

### Kết luận

**Phần thiết lập và kiểm tra cơ bản môi trường Docker Compose của W1-T6 đã thực hiện thành công.**

Ba dịch vụ PostgreSQL, Backend và Frontend đã được build và khởi chạy. PostgreSQL đạt trạng thái `healthy`. Backend đã từng khởi động thành công và kết nối được với PostgreSQL. Frontend có thể truy cập từ trình duyệt.

Quá trình dừng và khởi động lại ba container cũng đã thực hiện thành công.

**W1-T6 chưa được xác nhận nghiệm thu toàn bộ**, vì chưa có kết quả kiểm thử Frontend gọi Backend thành công và còn một số bước xác minh môi trường cần phối hợp.

Các giới hạn này được ghi nhận để tiếp tục xử lý khi các thành phần liên quan sẵn sàng.