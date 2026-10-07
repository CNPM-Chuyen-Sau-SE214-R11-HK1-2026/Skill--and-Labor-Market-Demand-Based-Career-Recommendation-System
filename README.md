# Skill- and Labor Market Demand–Based Career Recommendation System

## 1. Giới thiệu dự án (Project Description)

> Hệ thống gợi ý nghề nghiệp dựa trên kỹ năng cá nhân và nhu cầu thị trường lao động, giúp người dùng xác định nghề nghiệp phù hợp, đánh giá mức độ tương thích và nhận diện các kỹ năng cần phát triển.

Dự án cung cấp nền tảng gợi ý nghề nghiệp thông minh, kết nối trực tiếp khoảng cách giữa năng lực hiện tại của người lao động / sinh viên với xu hướng tuyển dụng thực tế của thị trường lao động.

---

## 2. Công nghệ sử dụng (Tech Stack)

* **Frontend:** React, TypeScript, Vite
* **Backend:** Spring Boot (Java 17), Maven
* **Database:** PostgreSQL
* **API:** RESTful API
* **Security & Auth:** Spring Security, JSON Web Token (JWT)
* **DevOps & Deployment:** Docker, Docker Compose

---

## 3. Cấu trúc thư mục (Repository Structure)

```text
.
├── frontend/                   # Ứng dụng Frontend (React + TypeScript)
│   ├── public/                 # Tài nguyên tĩnh
│   ├── src/                    # Mã nguồn chính (App, Components, v.v.)
│   ├── Dockerfile              # Cấu hình đóng gói Docker Frontend (Nginx)
│   ├── nginx.conf              # Cấu hình máy chủ web Nginx cho SPA
│   ├── package.json            # Định nghĩa dependencies và npm scripts
│   ├── tsconfig.json           # Cấu hình TypeScript
│   └── vite.config.ts          # Cấu hình Vite bundler
│
├── backend/                    # Ứng dụng Backend (Spring Boot + Java)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/cnpm/careerrecommendation/
│   │   │   │   ├── config/     # Các cấu hình hệ thống (Security, Web, v.v.)
│   │   │   │   ├── controller/ # REST API Controllers
│   │   │   │   ├── dto/        # Data Transfer Objects
│   │   │   │   ├── entity/     # JPA Domain Entities
│   │   │   │   ├── repository/ # Spring Data JPA Repositories
│   │   │   │   ├── service/    # Business Services & Interfaces
│   │   │   │   └── CareerRecommendationApplication.java # Entry point
│   │   │   └── resources/
│   │   │       └── application.yml # Cấu hình môi trường Spring Boot
│   │   └── test/               # Unit & Integration Tests
│   ├── Dockerfile              # Multi-stage Dockerfile cho Backend
│   └── pom.xml                 # Cấu hình Maven dependencies
│
├── docker/                     # Tài liệu và tài nguyên hỗ trợ Docker
│   └── README.md
│
├── .env.example                # Mẫu khai báo biến môi trường
├── .gitignore                  # Cấu hình loại trừ file của Git
├── docker-compose.yml          # Điều phối các dịch vụ Docker (PostgreSQL, Backend, Frontend)
├── LICENSE                     # Giấy phép mã nguồn mở (MIT)
└── README.md                   # Tài liệu hướng dẫn dự án
```

---

## 4. Yêu cầu môi trường (Prerequisites)

Để chạy dự án ở môi trường cục bộ, máy tính cần cài đặt các công cụ sau:

* **Node.js**: Phiên bản 18+ (khuyến nghị Node 20 LTS) & **npm**
* **Java Development Kit (JDK)**: Java 17 trở lên
* **Apache Maven**: Phiên bản 3.8+ (hoặc dùng Maven wrapper)
* **PostgreSQL**: Phiên bản 15+ (nếu không chạy qua Docker)
* **Docker & Docker Compose**: Khuyến nghị để thiết lập môi trường đồng nhất và nhanh chóng

---

## 5. Hướng dẫn phát triển cục bộ (Local Development)

### Bước 1: Khởi tạo biến môi trường

Sao chép file mẫu `.env.example` thành `.env` tại thư mục gốc và chỉnh sửa thông tin phù hợp:

```bash
cp .env.example .env
```

### Bước 2: Chạy Backend (Spring Boot)

1. Đảm bảo dịch vụ PostgreSQL đang hoạt động và cơ sở dữ liệu đã được tạo.
2. Di chuyển vào thư mục backend và chạy ứng dụng:

```bash
cd backend
mvn clean spring-boot:run
```

Backend sẽ khởi chạy mặc định tại cổng `http://localhost:8080`.

### Bước 3: Chạy Frontend (React + TypeScript)

1. Mở một terminal mới và di chuyển vào thư mục frontend:

```bash
cd frontend
npm install
npm run dev
```

Frontend sẽ khởi chạy tại `http://localhost:3000`.

---

## 6. Khởi chạy bằng Docker (Docker Deployment)

Hệ thống hỗ trợ chạy toàn bộ dịch vụ (PostgreSQL, Spring Boot Backend, React Frontend) thông qua Docker Compose:

1. **Chuẩn bị cấu hình:**

   ```bash
   cp .env.example .env
   ```

2. **Khởi chạy toàn bộ hệ thống:**

   ```bash
   docker compose up --build -d
   ```

3. **Kiểm tra trạng thái các container:**

   ```bash
   docker compose ps
   ```

4. **Xem nhật ký hoạt động (logs):**

   ```bash
   docker compose logs -f
   ```

5. **Truy cập ứng dụng:**
   * **Frontend:** `http://localhost:3000`
   * **Backend REST API:** `http://localhost:8080`
   * **PostgreSQL Database:** `localhost:5432`

6. **Dừng toàn bộ hệ thống:**

   ```bash
   docker compose down
   ```
