# ADR-0009: Chuẩn hóa Hợp đồng Biến Môi trường, Docker Compose và Pipeline CI Tự động

* **Trạng thái (Status):** Accepted (Đã phê duyệt cho Tuần 2)
* **Tác giả (Author):** Phan Nguyễn Thông (DevOps / QA Lead)
* **Ngày quyết định (Date):** 2026-11-02
* **Tài liệu liên quan (Related Documents):**
  - [Hướng dẫn Phát triển & Môi trường](../development/README.md)
  - [Hồ sơ quyết định ADR-0001 (Tech Stack)](../decisions/ADR-0001-baseline-tech-stack.md)
  - [Báo cáo thẩm định kỹ thuật Tuần 1](../audit/week1-audit.md)
  - [Kế hoạch Tác vụ Tuần 2](../plan/week2-tasks.md)
  - Mã yêu cầu: `NFR-08`, `NFR-09`, `NFR-10`, `NFR-13` | Task liên quan: `W2-T6`

---

## 1. Bối cảnh và Vấn đề (Context & Problem Statement)

Qua thẩm định kỹ thuật sau Tuần 1, môi trường khởi chạy container Docker Compose bộc lộ 4 điểm yếu nghiêm trọng cản trở quá trình làm việc của nhóm:
1. **Lỗi khởi chạy trên bản clone sạch:** Lệnh `docker compose config` hoặc `docker compose up` báo lỗi lập tức do biến môi trường `JWT_SECRET` bắt buộc nhưng file `.env` không nằm trong Git tracking và không có cơ chế tự động khởi tạo.
2. **Bẫy giá trị mẫu (Example Template Trap):** File `.env.example` cung cấp giá trị `JWT_SECRET=your_random_secret_key_at_least_32_utf8_bytes_here`. Chuỗi này kích hoạt cơ chế tự hủy (fail-fast exception) trong `JwtTokenProvider.java` do bắt đầu bằng tiền tố bị cấm `your_`.
3. **Thiếu Healthcheck cho Backend trong Docker Compose:** Dịch vụ `backend` chưa khai báo kiểm tra sức khỏe `GET /api/health`, khiến `frontend` khởi động đồng thời và có thể gặp lỗi kết nối trước khi backend Spring Boot nạp xong context.
4. **Thiếu Pipeline CI tự động:** Chưa có GitHub Actions workflow tự động kiểm tra xem các Pull Request có làm gãy build hoặc gãy test hay không, dẫn đến việc các lỗi nghiêm trọng (như lỗi duplicate key YAML của W1-T5) bị lọt vào nhánh `main`.

---

## 2. Các phương án đã xem xét (Options Considered)

### Phương án 1: Hardcode một giá trị secret mặc định trong `docker-compose.yml`
* **Mô tả:** Đặt `JWT_SECRET: ${JWT_SECRET:-default_super_secret_for_dev_only}`.
* **Nhược điểm:** Vi phạm nghiêm trọng nguyên tắc an ninh, có nguy cơ lọt key mặc định lên môi trường thật; bị `JwtTokenProvider` chặn vì bắt đầu bằng `default_`.

### Phương án 2: Script hóa việc khởi tạo `.env` + Cập nhật Healthcheck + Thiết lập GitHub Actions CI (Được chọn)
* **Mô tả:**
  - Viết script PowerShell (`scripts/init-env.ps1`) và Bash (`scripts/init-env.sh`) tự động sinh ngẫu nhiên chuỗi bảo mật an toàn 64 ký tự.
  - Sửa `docker-compose.yml` bổ sung healthcheck endpoint `/api/health`.
  - Tạo GitHub Actions workflow `.github/workflows/ci.yml` tự động chạy `mvn test` và `npm run build`.
* **Ưu điểm:** Khắc phục triệt để lỗi khởi chạy 1 chạm; giữ vững an ninh; có hàng rào bảo vệ mã nguồn tự động qua CI.

---

## 3. Quyết định và Căn cứ lựa chọn (Decision & Rationale)

Nhóm quyết định lựa chọn **Phương án 2**:
1. **Script khởi tạo môi trường tự động (`scripts/init-env.ps1` & `scripts/init-env.sh`):**
   - Lập trình viên mới clone dự án chỉ cần chạy:
     ```powershell
     powershell -ExecutionPolicy Bypass -File scripts/init-env.ps1
     ```
   - Script tự động sao chép `.env.example` thành `.env`, đồng thời sinh ngẫu nhiên một chuỗi bí mật Base64/Hex an toàn dài 64 ký tự thay thế vào `JWT_SECRET` (đảm bảo không chứa tiền tố `your_` hay `default_`).
2. **Chuẩn hóa `docker-compose.yml` & Bổ sung công cụ Healthcheck:**
   - Phan Nguyễn Thông (W2-T6) bổ sung `curl` hoặc `wget` vào `backend/Dockerfile`.
   - Bổ sung cấu hình healthcheck cho dịch vụ `backend`:
     ```yaml
     healthcheck:
       test: ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:8080/api/health || exit 1"]
       interval: 10s
       timeout: 5s
       retries: 5
       start_period: 30s
     ```
   - Chuyển ràng buộc của `frontend` thành:
     ```yaml
     depends_on:
       backend:
         condition: service_healthy
     ```
3. **Thiết lập GitHub Actions CI Workflow (`.github/workflows/ci.yml`):**
   - Kích hoạt khi có sự kiện `push` vào `main` hoặc `pull_request` mở vào nhánh `main`.
   - **Job 1: Backend Unit & Integration Tests:** Chạy trên môi trường `ubuntu-latest`, cài JDK 17, khởi động service container PostgreSQL 16, thực thi `mvn -f backend/pom.xml clean test`, sau đó chạy kiểm thử tích hợp chuyên biệt:
     ```bash
     mvn -f backend/pom.xml -Ppostgres-it test-compile failsafe:integration-test failsafe:verify
     ```
     Điều này đảm bảo toàn bộ 21 ca kiểm thử HTTP thật của `AuthPostgresIT.java` được tự động xác minh trước khi merge, trong khi lập trình viên phát triển cục bộ không bắt buộc phải bật Docker.
   - **Job 2: Frontend CI:** Cài Node.js 20, chạy `npm ci` và `npm run build` tại thư mục `frontend/`.
   - **Quy tắc Kiểm soát Cổng (CI Gate):** CI bắt đầu ở chế độ non-required và chính thức trở thành Required status check ngay sau khi PR T1a (sửa lỗi YAML) được merge vào `main` (hạn chót: 2026-11-03 09:00).
4. **Chính sách Fail-Fast cho Giá trị Mẫu `.env.example`:**
   - Để ngăn ngừa việc vô tình đưa khóa bí mật mẫu lên môi trường triển khai thực tế, `JwtTokenProvider.java` vẫn duy trì cơ chế fail-fast: nếu phát hiện `JWT_SECRET` bắt đầu bằng `your_` hoặc `default_`, ứng dụng ném lỗi `IllegalArgumentException` kèm thông báo hướng dẫn cụ thể thay vì âm thầm hoạt động. Lập trình viên chạy script `init-env.ps1` để tự sinh khóa 64 ký tự hợp lệ.

---

## 4. Hệ quả và Đánh đổi (Consequences & Trade-offs)

### Tác động tích cực:
* Đạt được mục tiêu khởi chạy 1 chạm đúng nghĩa từ bản clone sạch (`./scripts/init-env.ps1 && docker compose up -d`).
* Các dịch vụ trong Docker khởi động theo đúng thứ tự sẵn sàng: `postgres (healthy)` -> `backend (healthy)` -> `frontend`.
* Ngăn chặn hoàn toàn việc merge mã nguồn bị lỗi cú pháp hoặc gãy bài test nhờ CI bot.

### Đánh đổi & Kế hoạch giảm thiểu:
* Đòi hỏi máy dev chạy Windows PowerShell phải có quyền thực thi script.
* **Biện pháp:** Cung cấp sẵn cờ `-ExecutionPolicy Bypass` và tài liệu hướng dẫn cụ thể trong `docs/development/README.md`.
