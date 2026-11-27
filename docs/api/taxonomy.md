# Đặc tả Hợp đồng REST API Phân hệ Danh mục Nghề nghiệp & Kỹ năng (`/api/v1/occupations`, `/api/v1/skills`)

* **Phiên bản:** 1.1.0 (Tuần 2 – `W2-T3`, mở rộng Tuần 4 – `W4-T3`)
* **Chủ sở hữu hợp đồng (Owner):** Huỳnh Long Bảo Khanh (`W2-T3`, `W4-T3`)
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

* **Hiển thị tại frontend (W3-T5):** Trang `/occupations/:id` hiển thị
  `marketIndicators` của kỳ mới nhất nếu có. Hợp đồng hiện tại chưa cung cấp
  chuỗi theo thời gian hoặc phân vùng. Từ W4-T5, danh mục và chi tiết luôn
  gọi API thật; xem [feature occupations](../../frontend/src/features/occupations/README.md).

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

---

## 3. Phân hệ Quản trị Danh mục Nghề nghiệp, Kỹ năng & Ánh xạ (`/api/v1/admin` — `W4-T3`)

> [!IMPORTANT]
> Toàn bộ endpoint dưới đây yêu cầu JWT Bearer Token hợp lệ với vai trò **`ROLE_ADMIN`** (`@PreAuthorize("hasRole('ADMIN')")` và rule `/api/v1/admin/**` trong `SecurityConfig`). Yêu cầu chưa đăng nhập trả `401 Unauthorized`; vai trò `ROLE_CANDIDATE` trả `403 Forbidden`. Mọi thay đổi được ghi trực tiếp vào PostgreSQL và phản ánh ngay trên `GET /api/v1/occupations`, `GET /api/v1/skills` và `GET /api/v1/recommendations`.

### 3.1. `POST /api/v1/admin/occupations`
* **Mô tả:** Tạo mới nghề nghiệp chuẩn hóa.
* **Request Body (`AdminOccupationRequest`):**
  ```json
  {
    "code": "SOC-15-2051",
    "title": "Data Scientist / Khoa học dữ liệu",
    "groupId": 2,
    "description": "Xây dựng mô hình học máy và phân tích dữ liệu nâng cao.",
    "industry": "Technology",
    "entrySalaryAvg": 18000000,
    "midSalaryAvg": 40000000
  }
  ```
  * `code` (bắt buộc, `<= 50` ký tự, duy nhất), `title` (bắt buộc, `<= 200` ký tự), `groupId` (tùy chọn, số nguyên dương), `description` (tùy chọn), `industry` (tùy chọn, `<= 100` ký tự), `entrySalaryAvg` & `midSalaryAvg` (tùy chọn, `NUMERIC(12,2) >= 0`, nếu nhập cả hai thì `entrySalaryAvg <= midSalaryAvg`).
* **Phản hồi thành công (`201 Created` — `AdminOccupationResponse`):** Trả về đối tượng nghề nghiệp vừa tạo kèm `id`, `groupId`, `groupName`, `createdAt`, `updatedAt`.
* **Mã lỗi dự kiến:** `400 Bad Request` (`Bad Request` / `INVALID_OCCUPATION_REQUEST`), `401 Unauthorized`, `403 Forbidden`, `404 Not Found` (`RESOURCE_NOT_FOUND` nếu `groupId` không tồn tại), `409 Conflict` (`OCCUPATION_CODE_ALREADY_EXISTS` nếu trùng `code`).

### 3.2. `PUT /api/v1/admin/occupations/{id}`
* **Mô tả:** Cập nhật toàn bộ thông tin nghề nghiệp theo `id`. Cho phép giữ nguyên `code` của chính bản ghi đó; từ chối nếu trùng `code` của nghề nghiệp khác.
* **Phản hồi thành công (`200 OK`):** `AdminOccupationResponse`.
* **Mã lỗi dự kiến:** `400 Bad Request` (`INVALID_PARAMETER` / `INVALID_OCCUPATION_REQUEST`), `401 Unauthorized`, `403 Forbidden`, `404 Not Found`, `409 Conflict` (`OCCUPATION_CODE_ALREADY_EXISTS`).

### 3.3. `DELETE /api/v1/admin/occupations/{id}`
* **Mô tả:** Xóa nghề nghiệp theo `id` (`ON DELETE CASCADE` tự động dọn `occupation_skills`, `market_demand_indicators`, `recommendation_logs`).
* **Phản hồi thành công (`204 No Content`)**.
* **Mã lỗi dự kiến:** `400 Bad Request` (`INVALID_PARAMETER`), `401 Unauthorized`, `403 Forbidden`, `404 Not Found`.

### 3.4. `GET /api/v1/admin/occupation-groups`
* **Mô tả:** Lấy danh sách nhóm ngành (`occupation_groups`) sắp xếp theo `id` tăng dần để phục vụ form quản trị `/admin/taxonomy`.
* **Phản hồi thành công (`200 OK`):** `List<OccupationGroupResponse>` (`id`, `code`, `name`, `description`).

### 3.5. `POST /api/v1/admin/skills`
* **Mô tả:** Tạo mới kỹ năng chuẩn hóa trong từ điển `skills`.
* **Request Body (`AdminSkillRequest`):**
  ```json
  {
    "code": "SK-KOTLIN",
    "name": "Kotlin",
    "categoryId": 1,
    "skillType": "TECHNICAL",
    "description": "Ngôn ngữ lập trình hiện đại trên JVM và Android."
  }
  ```
  * `code` (bắt buộc, `<= 50` ký tự, duy nhất), `name` (bắt buộc, `<= 150` ký tự), `categoryId` (tùy chọn, số nguyên dương), `skillType` (tùy chọn, mặc định `"TECHNICAL"`, nhận `TECHNICAL | SOFT | DOMAIN`), `description` (tùy chọn).
* **Phản hồi thành công (`201 Created` — `AdminSkillResponse`):** Trả về `id`, `code`, `name`, `categoryId`, `categoryName`, `skillType`, `description`, `standardSource` (`O*NET` nếu `code` bắt đầu bằng `SOC-`, còn lại `ESCO`), `createdAt`, `updatedAt`.
* **Mã lỗi dự kiến:** `400 Bad Request` (`Bad Request` / `INVALID_SKILL_REQUEST`), `401 Unauthorized`, `403 Forbidden`, `404 Not Found` (`RESOURCE_NOT_FOUND` nếu `categoryId` không tồn tại), `409 Conflict` (`SKILL_CODE_ALREADY_EXISTS`).

### 3.6. `PUT /api/v1/admin/skills/{id}`
* **Mô tả:** Cập nhật kỹ năng theo `id`. Cho phép giữ nguyên `code` của chính kỹ năng đó.
* **Phản hồi thành công (`200 OK`):** `AdminSkillResponse`.
* **Mã lỗi dự kiến:** `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found`, `409 Conflict` (`SKILL_CODE_ALREADY_EXISTS`).

### 3.7. `DELETE /api/v1/admin/skills/{id}`
* **Mô tả:** Xóa kỹ năng theo `id` (`ON DELETE CASCADE` dọn `occupation_skills` và `user_skills`).
* **Phản hồi thành công (`204 No Content`)**.
* **Mã lỗi dự kiến:** `400 Bad Request` (`INVALID_PARAMETER`), `401 Unauthorized`, `403 Forbidden`, `404 Not Found`.

### 3.8. `GET /api/v1/admin/skill-categories`
* **Mô tả:** Lấy danh sách danh mục kỹ năng (`skill_categories`) sắp xếp theo `id` tăng dần.
* **Phản hồi thành công (`200 OK`):** `List<SkillCategoryResponse>` (`id`, `code`, `name`, `description`).

### 3.9. `GET /api/v1/admin/occupations/{id}/skills`
* **Mô tả:** Lấy danh sách kỹ năng đã gán cho nghề nghiệp `{id}` kèm trọng số `importanceWeight` và cờ `isRequired`.
* **Phản hồi thành công (`200 OK`):** `List<OccupationSkillMappingResponse>`.
* **Mã lỗi dự kiến:** `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found`.

### 3.10. `POST /api/v1/admin/occupations/{id}/skills`
* **Mô tả:** Gán kỹ năng vào nghề nghiệp `{id}` (hoặc cập nhật lại `importanceWeight` và `isRequired` nếu ánh xạ `(occupation_id, skill_id)` đã tồn tại).
* **Request Body (`OccupationSkillRequest`):**
  ```json
  {
    "skillId": 12,
    "importanceWeight": 0.90,
    "isRequired": true
  }
  ```
  * `skillId` (bắt buộc, số nguyên dương), `importanceWeight` (tùy chọn, mặc định `1.00`, nằm trong `[0.00, 1.00]`, tối đa `2` chữ số thập phân), `isRequired` (tùy chọn, mặc định `true`).
* **Phản hồi thành công:** `201 Created` (nếu tạo ánh xạ mới) hoặc `200 OK` (nếu cập nhật ánh xạ đã tồn tại), body `OccupationSkillMappingResponse`.
* **Mã lỗi dự kiến:** `400 Bad Request` (`INVALID_OCCUPATION_SKILL_REQUEST` khi `importanceWeight` ngoài `[0.00, 1.00]` hoặc sai định dạng), `401 Unauthorized`, `403 Forbidden`, `404 Not Found` (nếu `occupationId` hoặc `skillId` không tồn tại), `409 Conflict` (`OCCUPATION_SKILL_CONFLICT` khi xung đột ghi đồng thời).

### 3.11. `PUT /api/v1/admin/occupations/{id}/skills/{skillId}`
* **Mô tả:** Cập nhật `importanceWeight` và/hoặc `isRequired` cho ánh xạ kỹ năng hiện có. Phải cung cấp ít nhất một trường.
* **Phản hồi thành công (`200 OK`):** `OccupationSkillMappingResponse`.
* **Mã lỗi dự kiến:** `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found`.

### 3.12. `DELETE /api/v1/admin/occupations/{id}/skills/{skillId}` *(hỗ trợ alias `?skillId={skillId}` tại `/api/v1/admin/occupations/{id}/skills`)*
* **Mô tả:** Gỡ bỏ kỹ năng `{skillId}` khỏi nghề nghiệp `{id}`.
* **Phản hồi thành công (`204 No Content`)**.
* **Mã lỗi dự kiến:** `400 Bad Request` (`INVALID_PARAMETER`), `401 Unauthorized`, `403 Forbidden`, `404 Not Found` (nếu nghề nghiệp hoặc ánh xạ không tồn tại).
