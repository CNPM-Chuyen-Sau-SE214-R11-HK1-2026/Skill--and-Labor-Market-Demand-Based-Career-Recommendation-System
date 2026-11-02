# Đặc tả Hợp đồng REST API Phân hệ Danh mục Nghề nghiệp & Kỹ năng (`/api/v1/occupations`, `/api/v1/skills`)

* **Phiên bản:** 1.0.0 (Tuần 2)
* **Chủ sở hữu hợp đồng (Owner):** Huỳnh Long Bảo Khanh (W2-T3)
* **Giao thức:** HTTPS / RESTful JSON
* **Đường dẫn cơ sở (Base URL):** `/api/v1`
* **Chuẩn định dạng:** JSON (`Content-Type: application/json; charset=utf-8`)
* **Xác thực:** Công khai hoàn toàn (`permitAll`)

---

## 1. Nguyên tắc Tiếp cận Dữ liệu Công khai (Public Access Policy)

> [!NOTE]
> Toàn bộ các endpoint trong phân hệ tra cứu nghề nghiệp và từ điển kỹ năng (`/api/v1/occupations/**`, `/api/v1/skills/**`) được cấu hình công khai (`permitAll()`) trong Spring Security. Bất kỳ người dùng nào (khách vãng lai, ứng viên chưa đăng nhập, hoặc hệ thống tích hợp) đều có thể gọi các API này mà không cần đính kèm JWT Bearer Token.

Dữ liệu được nạp thông qua bản di chuyển Flyway `V3__seed_taxonomy_and_occupations.sql` bao gồm tối thiểu 10 nhóm nghề CNTT chuẩn hóa theo O*NET / ESCO, 30+ kỹ năng và dữ liệu chỉ số thị trường lao động (LMI).

---

## 2. Danh mục Endpoint Chi tiết

### 2.1. `GET /api/v1/occupations`
* **Mã thao tác (Operation ID):** `searchOccupations`
* **Mô tả:** Tìm kiếm và phân trang danh mục nghề nghiệp theo từ khóa, lọc theo nhóm ngành hoặc trạng thái.
* **Quyền truy cập:** Công khai (`Public`).
* **Tham số truy vấn (Query Params):**
  * `query`: Từ khóa tìm kiếm theo tên hoặc mô tả nghề (String, optional).
  * `groupId`: Lọc theo mã nhóm nghề nghiệp (Long, optional).
  * `page`: Số trang, 0-indexed (Integer, default: 0).
  * `size`: Số phần tử trên mỗi trang (Integer, default: 20, max: 100).
  * `sort`: Sắp xếp theo trường (String, default: `title,asc`).
* **Phản hồi thành công (`200 OK`):** Cấu trúc phân trang dùng chung `PageResponse`:
  ```json
  {
    "content": [
      {
        "id": 101,
        "code": "SOC-15-1252",
        "title": "Software Developer / Kỹ sư phần mềm",
        "groupName": "Công nghệ thông tin & Khoa học máy tính",
        "averageSalaryMin": 15000000,
        "averageSalaryMax": 35000000,
        "demandScore": 88.5,
        "growthRatePct": 12.4
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 15,
    "totalPages": 1,
    "last": true
  }
  ```
* **Mã lỗi dự kiến:** `400 Bad Request` (nếu tham số phân trang âm hoặc không hợp lệ).

---

### 2.2. `GET /api/v1/occupations/{id}`
* **Mã thao tác (Operation ID):** `getOccupationDetail`
* **Mô tả:** Lấy thông tin chi tiết một nghề nghiệp, bao gồm bộ kỹ năng yêu cầu (cốt lõi vs bổ trợ, trọng số) và chỉ số thị trường lao động (LMI) mới nhất.
* **Quyền truy cập:** Công khai (`Public`).
* **Tham số đường dẫn (Path Variable):** `id` (Long, ID nghề nghiệp).
* **Phản hồi thành công (`200 OK`):**
  ```json
  {
    "id": 101,
    "code": "SOC-15-1252",
    "title": "Software Developer / Kỹ sư phần mềm",
    "groupName": "Công nghệ thông tin & Khoa học máy tính",
    "description": "Nghiên cứu, thiết kế, phát triển và thử nghiệm các hệ thống phần mềm ứng dụng...",
    "salaryRange": {
      "min": 15000000,
      "max": 35000000,
      "currency": "VND"
    },
    "marketIndicators": {
      "demandScore": 88.5,
      "growthRatePct": 12.4,
      "shortageLevel": "HIGH",
      "openingsCount": 1250,
      "dataPeriod": "2025-2026",
      "sourceName": "Cục Thống kê & Báo cáo WEF 2025"
    },
    "skills": [
      {
        "skillId": 12,
        "skillCode": "SK-JAVA-01",
        "skillName": "Java",
        "importanceType": "ESSENTIAL",
        "weight": 1.0
      },
      {
        "skillId": 18,
        "skillCode": "SK-DOCKER-01",
        "skillName": "Docker & Containerization",
        "importanceType": "OPTIONAL",
        "weight": 0.5
      }
    ]
  }
  ```
* **Mã lỗi dự kiến:**
  * `404 Not Found`: Nghề nghiệp không tồn tại trong CSDL.

---

### 2.3. `GET /api/v1/skills`
* **Mã thao tác (Operation ID):** `searchSkills`
* **Mô tả:** Tìm kiếm danh mục kỹ năng chuẩn hóa (theo O*NET / ESCO) để người dùng chọn khi tạo hồ sơ hoặc tra cứu từ điển.
* **Quyền truy cập:** Công khai (`Public`).
* **Tham số truy vấn (Query Params):**
  * `query`: Từ khóa tìm kiếm (String, tối thiểu 2 ký tự, optional).
  * `categoryId`: Lọc theo mã danh mục kỹ năng (Long, optional).
* **Phản hồi thành công (`200 OK`):**
  ```json
  [
    {
      "id": 12,
      "code": "SK-JAVA-01",
      "name": "Java",
      "categoryName": "Lập trình & Phát triển phần mềm",
      "standardSource": "ESCO"
    },
    {
      "id": 15,
      "code": "SK-SQL-01",
      "name": "PostgreSQL / SQL",
      "categoryName": "Cơ sở dữ liệu",
      "standardSource": "ESCO"
    }
  ]
  ```
* **Mã lỗi dự kiến:** `400 Bad Request`.
