# Hướng dẫn phát triển & Môi trường (Development Guide)

Tài liệu này cung cấp hướng dẫn chi tiết dành cho lập trình viên và AI agent để thiết lập môi trường máy trạm, chạy ứng dụng cục bộ, build kiểm thử, và triển khai toàn bộ hệ thống bằng Docker.

---

## 1. Yêu cầu công cụ cài đặt sẵn (Prerequisites)

Để phát triển trực tiếp trên máy cục bộ, cần cài đặt:
* **Node.js**: Phiên bản 18+ (khuyến nghị Node 20 LTS) & **npm** (9+).
* **Java Development Kit (JDK)**: Java 17 (OpenJDK hoặc Eclipse Temurin).
* **Apache Maven**: Phiên bản 3.8+ (hoặc sử dụng Maven wrapper).
* **Docker & Docker Compose**: Docker Engine 24+ và Compose v2+.

---

## 2. Thiết lập biến môi trường (Environment Setup)

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
> **Tuyệt đối không commit file `.env` lên Git.** File [`.gitignore`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/.gitignore) đã cấu hình loại trừ file `.env` và bảo lưu file [`.env.example`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/.env.example).

---

## 3. Khởi chạy toàn diện với Docker Compose (Khuyến nghị)

Phương pháp nhanh nhất và đồng nhất nhất giữa các môi trường:

```bash
# 1. Khởi chạy và build lại toàn bộ các dịch vụ (PostgreSQL, Backend, Frontend)
docker compose up --build -d

# 2. Kiểm tra trạng thái các container và healthcheck
docker compose ps

# 3. Xem nhật ký hoạt động (logs) thời gian thực
docker compose logs -f

# 4. Dừng hệ thống
docker compose down
```

### Các cổng dịch vụ mặc định:
* **Frontend Web:** `http://localhost:3000`
* **Backend REST API:** `http://localhost:8080`
* **PostgreSQL Engine:** `localhost:5432`

---

## 4. Chạy từng dịch vụ độc lập cho quá trình phát triển (Local Development)

### 4.1. Khởi động riêng dịch vụ CSDL PostgreSQL:
```bash
docker compose up -d postgres
```

### 4.2. Khởi chạy Backend (Spring Boot):
Từ thư mục gốc dự án:
```bash
cd backend
mvn clean spring-boot:run
```
Hoặc chạy trực tiếp lớp [`CareerRecommendationApplication.java`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/backend/src/main/java/com/cnpm/careerrecommendation/CareerRecommendationApplication.java) từ IDE (IntelliJ IDEA / VS Code / Eclipse).

### 4.3. Khởi chạy Frontend (React + TypeScript):
Từ thư mục gốc dự án:
```bash
cd frontend
npm install
npm run dev
```
Frontend sẽ chạy ở chế độ Hot Module Replacement (HMR) tại `http://localhost:3000`.

---

## 5. Quy trình kiểm tra & Xác thực chất lượng mã nguồn (Verification Commands)

Trước khi gửi Pull Request hoặc hoàn tất một tác vụ, lập trình viên và AI agent phải chạy các lệnh kiểm tra sau:

| Hạng mục kiểm tra | Thư mục thực hiện | Lệnh thực thi | Tiêu chí đạt |
|---|---|---|---|
| **Kiểm tra kiểu dữ liệu TypeScript** | `frontend/` | `npm run build` hoặc `tsc -b` | 0 errors |
| **Đóng gói thử nghiệm Frontend** | `frontend/` | `npm run build` | Thư mục `dist/` được tạo thành công |
| **Biên dịch Backend Java** | `backend/` | `mvn clean compile` | `BUILD SUCCESS` |
| **Chạy kiểm thử Backend** | `backend/` | `mvn test` | `BUILD SUCCESS` (0 failures, 0 errors) |
| **Cú pháp Docker Compose** | Thư mục gốc | `docker compose config` | File cấu hình hợp lệ, exit code 0 |
| **Kiểm tra trạng thái Git** | Thư mục gốc | `git status` | Không có file rác, file `.env` không bị track |

---

## 6. Quy chuẩn phối hợp và Phong cách lập trình

Để đảm bảo chất lượng kỹ thuật đồng đều giữa 06 thành viên và các AI agent:
* [**Quy trình làm việc với Git & Kiểm soát mã nguồn (`git-workflow.md`)**](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/development/git-workflow.md): Mô hình phân nhánh Trunk-based + Feature branch, quy chuẩn Conventional Commits, mẫu PR checklist và chính sách review.
* [**Quy chuẩn viết mã & Kiến trúc phần mềm (`coding-conventions.md`)**](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/development/coding-conventions.md): Quy chuẩn phân chia package Java/Spring Boot theo Domain, quy ước JPA Entity, kiến trúc thư mục Frontend React/TypeScript và nguyên tắc Clean Code.


## 7. Xác thực W1-T5

Đăng ký, đăng nhập, JWT/RBAC và hướng dẫn kiểm thử Windows: [w1-t5-auth-guide.md](w1-t5-auth-guide.md).
