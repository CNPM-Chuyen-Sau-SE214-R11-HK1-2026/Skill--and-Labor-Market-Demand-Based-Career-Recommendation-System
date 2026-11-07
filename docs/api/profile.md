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
> Ứng viên chỉ được phép xem, sửa đổi hồ sơ và kỹ năng thuộc sở hữu của chính mình. Controller được bảo vệ bằng `@PreAuthorize("hasRole('CANDIDATE')")`. Mọi endpoint trả 401 khi thiếu token hợp lệ và 403 khi vai trò không phải Candidate. Request body chứa trường ngoài hợp đồng bị từ chối với 400.

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
      "error": "PROFILE_NOT_FOUND",
      "message": "Hồ sơ ứng viên chưa được khởi tạo. Vui lòng cập nhật qua PUT /api/v1/profile.",
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
* **Mô tả:** Tạo mới (nếu chưa có) hoặc cập nhật thông tin kinh nghiệm và nguyện vọng nghề nghiệp.
* **Quyền truy cập:** Yêu cầu vai trò `ROLE_CANDIDATE`.

> [!NOTE]
> Ví dụ ban đầu có `educationLevel`, nhưng V1, thiết kế CSDL và entity hiện hành chưa có nơi lưu. Trường này đang chờ chốt schema, tập giá trị và quy tắc null. Bản triển khai hiện hành chỉ chấp nhận các trường trong ví dụ dưới đây.

* **Kiểu dữ liệu và null:** Lương là số thập phân theo `NUMERIC(12,2)`. Kinh nghiệm là số thập phân theo `NUMERIC(4,1)`. `workMode` nhận `REMOTE | HYBRID | ONSITE`. PUT thay thế các trường công khai của hồ sơ. Trường bỏ qua hoặc null dùng mặc định V1 là `0` cho kinh nghiệm và `HYBRID` cho chế độ làm việc. Headline, địa điểm, ngành và hai cận lương có thể để null.
* **Validation:** Bắt buộc `expectedSalaryMin <= expectedSalaryMax` (nếu cả hai được cung cấp); `yearsOfExperience >= 0`.
* **Request Body:**
  ```json
  {
    "headline": "Fullstack Java & React Developer",
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
* **Mô tả:** Lấy danh sách kỹ năng cá nhân đã khai báo kèm mức độ thành thạo của ứng viên hiện tại. Kỹ năng được lưu theo tài khoản, nên toàn bộ các thao tác kỹ năng hoạt động trước khi tạo hồ sơ. Khi chưa khai báo kỹ năng, GET trả `[]`.
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
  * `yearsExperience`: Số thập phân theo `NUMERIC(4,1)`, tùy chọn, không âm. Bỏ qua hoặc null dùng mặc định `0` của V1.
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
* **Tham số đường dẫn (Path Variable):** `skillId` (Long dương, khóa chính `skills.id` trong danh mục. Bản ghi được xác định theo cặp `(userId, skillId)`).
* **Request Body:**
  ```json
  {
    "proficiencyLevel": "ADVANCED",
    "yearsExperience": 2.5
  }
  ```
* **Phản hồi thành công (`200 OK`):** Đối tượng kỹ năng theo cấu trúc ở mục 2.3. Chỉ cập nhật trường khác null được gửi trong body. Thiếu hoặc null giữ nguyên giá trị cũ. Thiếu liên kết thuộc ứng viên hiện tại trả 404.
* **Mã lỗi dự kiến:** `400 Bad Request`, `401 Unauthorized`, `404 Not Found`.

---

### 2.6. `DELETE /api/v1/profile/skills/{skillId}`
* **Mã thao tác (Operation ID):** `deleteCandidateSkill`
* **Mô tả:** Xóa một kỹ năng khỏi hồ sơ ứng viên.
* **Quyền truy cập:** Yêu cầu vai trò `ROLE_CANDIDATE`.
* **Tham số đường dẫn (Path Variable):** `skillId` (Long dương, `skills.id` trong danh mục).
* **Phản hồi thành công (`204 No Content`)**
* **Mã lỗi dự kiến:** `400 Bad Request` nếu ID sai định dạng hoặc không dương, `401 Unauthorized`, `404 Not Found` nếu ứng viên không có kỹ năng này.
