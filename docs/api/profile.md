# Đặc tả Hợp đồng REST API Phân hệ Hồ sơ Ứng viên & Kỹ năng cá nhân (`/api/v1/profile`)

* **Phiên bản:** 1.0.0 (Tuần 2)
* **Chủ sở hữu hợp đồng (Owner):** Phạm Ngọc Gia Khang (W2-T2)
* **Giao thức:** HTTPS / RESTful JSON
* **Đường dẫn cơ sở (Base URL):** `/api/v1/profile`
* **Chuẩn định dạng:** JSON (`Content-Type: application/json; charset=utf-8`)
* **Xác thực:** Yêu cầu JWT Bearer Token (`ROLE_CANDIDATE`)

---

## 1. Nguyên tắc Bảo mật Quyền sở hữu Tài nguyên (Candidate Ownership Guarantee)

> [!IMPORTANT]
> Toàn bộ các thao tác trong phân hệ `/api/v1/profile/**` **tuyệt đối không** chấp nhận tham số `userId` trên URL đường dẫn (path variable) hoặc trong request body. Định danh ứng viên (`userId`) bắt buộc phải được trích xuất trực tiếp và duy nhất từ đối tượng `Authentication` (chứa `AuthenticatedUser`) đã được Spring Security xác thực trong ngữ cảnh phiên làm việc (`SecurityContextHolder`).
>
> Ứng viên chỉ được phép xem, sửa đổi hồ sơ và kỹ năng thuộc sở hữu của chính mình. Controller được bảo vệ bằng `@PreAuthorize("hasRole('CANDIDATE')")`.

---

## 2. Danh mục Endpoint Chi tiết

### 2.1. `GET /api/v1/profile`
* **Mã thao tác (Operation ID):** `getCandidateProfile`
* **Mô tả:** Lấy toàn bộ thông tin hồ sơ cá nhân và nguyện vọng nghề nghiệp của ứng viên hiện tại.
* **Quyền truy cập:** Yêu cầu vai trò `ROLE_CANDIDATE`.
* **Header:** `Authorization: Bearer <token>`
* **Request Body:** Không có.
* **Xử lý đặc biệt khi chưa có hồ sơ:**
  * Backend **KHÔNG TỰ ĐỘNG TẠO HỒ SƠ RỖNG** trong CSDL (tuân thủ nguyên tắc GET phi tác dụng phụ).
  * Nếu chưa có bản ghi trong bảng `candidate_profiles`, backend trả về mã **HTTP 404 Not Found** với mã lỗi `PROFILE_NOT_FOUND`:
    ```json
    {
      "timestamp": "2026-11-03T10:00:00Z",
      "status": 404,
      "error": "Not Found",
      "message": "Hồ sơ ứng viên chưa được khởi tạo. Vui lòng cập nhật thông tin hồ sơ lần đầu qua PUT /api/v1/profile",
      "path": "/api/v1/profile",
      "errors": []
    }
    ```
* **Phản hồi thành công (`200 OK`):**
  ```json
  {
    "id": 1,
    "userId": 1,
    "headline": "Junior Software Engineer",
    "educationLevel": "BACHELOR",
    "yearsOfExperience": 1,
    "expectedSalaryMin": 15000000,
    "expectedSalaryMax": 25000000,
    "preferredLocation": "Hồ Chí Minh",
    "workMode": "HYBRID",
    "targetIndustry": "Công nghệ thông tin",
    "skillsCount": 5,
    "updatedAt": "2026-11-03T10:15:00Z"
  }
  ```
* **Mã lỗi dự kiến:** `401 Unauthorized`, `404 Not Found`.

---

### 2.2. `PUT /api/v1/profile`
* **Mã thao tác (Operation ID):** `updateCandidateProfile`
* **Mô tả:** Tạo mới (nếu chưa có) hoặc cập nhật thông tin học vấn, kinh nghiệm và nguyện vọng nghề nghiệp.
* **Quyền truy cập:** Yêu cầu vai trò `ROLE_CANDIDATE`.
* **Validation:** Bắt buộc `expectedSalaryMin <= expectedSalaryMax` (nếu cả hai được cung cấp); `yearsOfExperience >= 0`.
* **Request Body:**
  ```json
  {
    "headline": "Fullstack Java & React Developer",
    "educationLevel": "BACHELOR",
    "yearsOfExperience": 2,
    "expectedSalaryMin": 18000000,
    "expectedSalaryMax": 30000000,
    "preferredLocation": "Hồ Chí Minh",
    "workMode": "HYBRID",
    "targetIndustry": "Công nghệ thông tin"
  }
  ```
* **Phản hồi thành công (`200 OK`):** Trả về đối tượng hồ sơ sau khi tạo mới / cập nhật.
* **Mã lỗi dự kiến:** `400 Bad Request`, `401 Unauthorized`.

---

### 2.3. `GET /api/v1/profile/skills`
* **Mã thao tác (Operation ID):** `getCandidateSkills`
* **Mô tả:** Lấy danh sách kỹ năng cá nhân đã khai báo kèm mức độ thành thạo của ứng viên hiện tại.
* **Quyền truy cập:** Yêu cầu vai trò `ROLE_CANDIDATE`.
* **Phản hồi thành công (`200 OK`):**
  ```json
  [
    {
      "skillId": 12,
      "skillCode": "SK-JAVA-01",
      "skillName": "Java",
      "categoryName": "Lập trình & Phát triển phần mềm",
      "proficiencyLevel": "INTERMEDIATE",
      "yearsExperience": 1.5,
      "verified": false
    },
    {
      "skillId": 15,
      "skillCode": "SK-SQL-01",
      "skillName": "PostgreSQL / SQL",
      "categoryName": "Cơ sở dữ liệu",
      "proficiencyLevel": "ADVANCED",
      "yearsExperience": 2.0,
      "verified": false
    }
  ]
  ```
* **Mã lỗi dự kiến:** `401 Unauthorized`.

---

### 2.4. `POST /api/v1/profile/skills`
* **Mã thao tác (Operation ID):** `addSkillToProfile`
* **Mô tả:** Thêm một kỹ năng từ danh mục chuẩn hóa vào hồ sơ của ứng viên. Kỹ năng phải tồn tại trong bảng `skills` (`common.entity.Skill`).
* **Quyền truy cập:** Yêu cầu vai trò `ROLE_CANDIDATE`.
* **Request Body:**
  ```json
  {
    "skillId": 12,
    "proficiencyLevel": "INTERMEDIATE",
    "yearsExperience": 1.5
  }
  ```
* **Validation:**
  * `skillId`: Long, bắt buộc, > 0.
  * `proficiencyLevel`: Enum `BEGINNER | INTERMEDIATE | ADVANCED | EXPERT`, bắt buộc.
  * `yearsExperience`: Double, tùy chọn, $\ge 0$.
* **Phản hồi thành công (`201 Created`):**
  ```json
  {
    "skillId": 12,
    "skillName": "Java",
    "proficiencyLevel": "INTERMEDIATE",
    "yearsExperience": 1.5,
    "createdAt": "2026-11-03T10:20:00Z"
  }
  ```
* **Mã lỗi dự kiến:**
  * `400 Bad Request`: Thiếu trường bắt buộc hoặc giá trị enum sai.
  * `401 Unauthorized`: Chưa đăng nhập hoặc token hết hạn.
  * `404 Not Found`: Không tìm thấy kỹ năng có ID tương ứng trong bảng `skills`.
  * `409 Conflict`: Kỹ năng này đã tồn tại trong hồ sơ của ứng viên.

---

### 2.5. `PUT /api/v1/profile/skills/{skillId}`
* **Mã thao tác (Operation ID):** `updateCandidateSkill`
* **Mô tả:** Cập nhật mức độ thành thạo hoặc số năm kinh nghiệm của một kỹ năng đã có trong hồ sơ ứng viên.
* **Quyền truy cập:** Yêu cầu vai trò `ROLE_CANDIDATE`.
* **Tham số đường dẫn (Path Variable):** `skillId` (Long, ID kỹ năng trong hồ sơ).
* **Request Body:**
  ```json
  {
    "proficiencyLevel": "ADVANCED",
    "yearsExperience": 2.5
  }
  ```
* **Phản hồi thành công (`200 OK`):** Đối tượng kỹ năng sau khi cập nhật.
* **Mã lỗi dự kiến:** `400 Bad Request`, `401 Unauthorized`, `404 Not Found`.

---

### 2.6. `DELETE /api/v1/profile/skills/{skillId}`
* **Mã thao tác (Operation ID):** `deleteCandidateSkill`
* **Mô tả:** Xóa một kỹ năng khỏi hồ sơ ứng viên.
* **Quyền truy cập:** Yêu cầu vai trò `ROLE_CANDIDATE`.
* **Tham số đường dẫn (Path Variable):** `skillId` (Long).
* **Phản hồi thành công (`204 No Content`)**
* **Mã lỗi dự kiến:** `401 Unauthorized`, `404 Not Found`.
