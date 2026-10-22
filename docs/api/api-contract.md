# Đặc tả Hợp đồng REST API (REST API Contract Specification)

**Phiên bản:** 1.0.0
**Giao thức:** HTTPS / RESTful JSON
**Đường dẫn cơ sở (Base URL):** `/api/v1`
**Chuẩn định dạng:** JSON (`Content-Type: application/json; charset=utf-8`)
**Quy ước đặt tên khóa JSON:** `camelCase`
**Định dạng ngày giờ:** ISO-8601 UTC (`YYYY-MM-DDTHH:mm:ssZ`)
**Cơ chế xác thực:** HTTP Header `Authorization: Bearer <JWT_TOKEN>`

---

## 1. Chuẩn quy ước chung (Global Conventions)

### 1.1. Cấu trúc phản hồi thành công (Standard Success Response)
Đối với các thao tác truy vấn đơn hoặc cập nhật, trả về trực tiếp đối tượng dữ liệu JSON hoặc danh sách. Đối với các thao tác phân trang, sử dụng cấu trúc:

```json
{
  "content": [ ... ],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 150,
  "totalPages": 8,
  "last": false
}
```

### 1.2. Cấu trúc phản hồi lỗi chuẩn (Standard Error Response)
Mọi phản hồi lỗi (HTTP status $\ge 400$) trả về định dạng thống nhất:

```json
{
  "timestamp": "2026-10-22T15:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Dữ liệu đầu vào không hợp lệ",
  "path": "/api/v1/auth/register",
  "errors": [
    {
      "field": "email",
      "rejectedValue": "invalid-email",
      "reason": "Email không đúng định dạng hợp lệ"
    }
  ]
}
```

---

## 2. Danh mục chi tiết các Endpoint (Endpoint Specifications)

### 2.1. Phân hệ Xác thực & Tài khoản (`/api/v1/auth`)

#### `POST /api/v1/auth/register`
* **Mã thao tác (Operation ID):** `registerCandidate`
* **Mô tả:** Đăng ký tài khoản ứng viên mới.
* **Module chịu trách nhiệm:** `auth`
* **Quyền truy cập:** Công khai (`Public`)
* **Request Body:**
  ```json
  {
    "email": "candidate@example.com",     // String, email format, required, max 100
    "password": "SecurePassword123!",    // String, min 8, max 64, required
    "fullName": "Nguyễn Văn A",          // String, required, max 100
    "phone": "0901234567"                // String, optional, max 20
  }
  ```
* **Phản hồi thành công (`201 Created`):**
  ```json
  {
    "id": 1,
    "email": "candidate@example.com",
    "fullName": "Nguyễn Văn A",
    "role": "ROLE_CANDIDATE",
    "status": "ACTIVE",
    "createdAt": "2026-10-22T15:30:00Z"
  }
  ```
* **Mã lỗi dự kiến:**
  * `400 Bad Request`: Thiếu trường bắt buộc hoặc mật khẩu không đủ độ dài.
  * `409 Conflict`: Email đã tồn tại trong hệ thống.

---

#### `POST /api/v1/auth/login`
* **Mã thao tác (Operation ID):** `login`
* **Mô tả:** Xác thực danh tính và phát hành JWT Bearer Token.
* **Module chịu trách nhiệm:** `auth`
* **Quyền truy cập:** Công khai (`Public`)
* **Request Body:**
  ```json
  {
    "email": "candidate@example.com",     // String, required
    "password": "SecurePassword123!"     // String, required
  }
  ```
* **Phản hồi thành công (`200 OK`):**
  ```json
  {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": 1,
      "email": "candidate@example.com",
      "fullName": "Nguyễn Văn A",
      "role": "ROLE_CANDIDATE"
    }
  }
  ```
* **Mã lỗi dự kiến:**
  * `400 Bad Request`: Thiếu email hoặc mật khẩu.
  * `401 Unauthorized`: Sai thông tin đăng nhập hoặc tài khoản bị khóa.

---

#### `GET /api/v1/auth/me`
* **Mã thao tác (Operation ID):** `getCurrentUser`
* **Mô tả:** Lấy thông tin tài khoản hiện tại từ JWT token.
* **Module chịu trách nhiệm:** `auth`
* **Quyền truy cập:** Yêu cầu xác thực (`ROLE_CANDIDATE` hoặc `ROLE_ADMIN`)
* **Header:** `Authorization: Bearer <token>`
* **Phản hồi thành công (`200 OK`):**
  ```json
  {
    "id": 1,
    "email": "candidate@example.com",
    "fullName": "Nguyễn Văn A",
    "phone": "0901234567",
    "role": "ROLE_CANDIDATE",
    "status": "ACTIVE",
    "createdAt": "2026-10-22T15:30:00Z"
  }
  ```
* **Mã lỗi dự kiến:** `401 Unauthorized`.

---

### 2.2. Phân hệ Hồ sơ ứng viên & Kỹ năng cá nhân (`/api/v1/profile`)

> [!IMPORTANT]
> **Nguyên tắc bảo mật quyền sở hữu tài nguyên (Candidate Ownership Security Guarantee):**
> Toàn bộ các thao tác trong phân hệ `/api/v1/profile/**` và `/api/v1/recommendations/**` **tuyệt đối không** chấp nhận tham số `userId` trên URL đường dẫn (path variable) hoặc trong request body. Định danh ứng viên (`userId`) bắt buộc phải được trích xuất trực tiếp và duy nhất từ đối tượng `Authentication` (JWT Bearer subject) đã được Spring Security xác thực trong ngữ cảnh phiên làm việc (`SecurityContextHolder`).
> **Cam kết:** Bất kỳ ứng viên nào cũng không thể xem, sửa đổi, thêm hoặc xóa hồ sơ/kỹ năng của ứng viên khác. Mọi hành vi cố tình thao túng dữ liệu không thuộc quyền sở hữu đều bị từ chối với mã lỗi `403 Forbidden` hoặc `401 Unauthorized`.

#### `GET /api/v1/profile`
* **Mã thao tác (Operation ID):** `getCandidateProfile`
* **Mô tả:** Lấy toàn bộ thông tin hồ sơ cá nhân và nguyện vọng nghề nghiệp của ứng viên.
* **Module chịu trách nhiệm:** `user`
* **Quyền truy cập:** Yêu cầu xác thực (`ROLE_CANDIDATE`)
* **Header:** `Authorization: Bearer <token>`
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
    "updatedAt": "2026-10-22T15:35:00Z"
  }
  ```
* **Mã lỗi dự kiến:** `401 Unauthorized`, `404 Not Found`.

---

#### `PUT /api/v1/profile`
* **Mã thao tác (Operation ID):** `updateCandidateProfile`
* **Mô tả:** Cập nhật thông tin học vấn, kinh nghiệm và nguyện vọng nghề nghiệp.
* **Module chịu trách nhiệm:** `user`
* **Quyền truy cập:** Yêu cầu xác thực (`ROLE_CANDIDATE`)
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
* **Phản hồi thành công (`200 OK`):** Trả về đối tượng hồ sơ sau khi cập nhật.
* **Mã lỗi dự kiến:** `400 Bad Request`, `401 Unauthorized`.

---

#### `GET /api/v1/profile/skills`
* **Mã thao tác (Operation ID):** `getUserSkills`
* **Mô tả:** Lấy danh sách kỹ năng cá nhân đã khai báo kèm mức độ thành thạo.
* **Module chịu trách nhiệm:** `user`
* **Quyền truy cập:** Yêu cầu xác thực (`ROLE_CANDIDATE`)
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

---

#### `POST /api/v1/profile/skills`
* **Mã thao tác (Operation ID):** `addSkillToProfile`
* **Mô tả:** Thêm một kỹ năng vào hồ sơ năng lực của ứng viên.
* **Module chịu trách nhiệm:** `user`
* **Quyền truy cập:** Yêu cầu xác thực (`ROLE_CANDIDATE`)
* **Request Body:**
  ```json
  {
    "skillId": 12,                          // Long, required
    "proficiencyLevel": "INTERMEDIATE",     // Enum: BEGINNER | INTERMEDIATE | ADVANCED | EXPERT, required
    "yearsExperience": 1.5                  // Double, optional, >= 0
  }
  ```
* **Phản hồi thành công (`201 Created`):**
  ```json
  {
    "skillId": 12,
    "skillName": "Java",
    "proficiencyLevel": "INTERMEDIATE",
    "yearsExperience": 1.5,
    "createdAt": "2026-10-22T15:40:00Z"
  }
  ```
* **Mã lỗi dự kiến:**
  * `400 Bad Request`: `skillId` không hợp lệ hoặc `proficiencyLevel` sai enum.
  * `404 Not Found`: Không tìm thấy kỹ năng trong từ điển taxonomy.
  * `409 Conflict`: Kỹ năng đã có trong hồ sơ của người dùng.

---

#### `PUT /api/v1/profile/skills/{skillId}`
* **Mã thao tác (Operation ID):** `updateSkillInProfile`
* **Mô tả:** Cập nhật mức độ thành thạo hoặc số năm kinh nghiệm của một kỹ năng đã có trong hồ sơ cá nhân của ứng viên.
* **Module chịu trách nhiệm:** `user`
* **Quyền truy cập:** Yêu cầu xác thực (`ROLE_CANDIDATE`)
* **Tham số đường dẫn (Path Variable):** `skillId` (Long, ID kỹ năng trong hồ sơ ứng viên)
* **Request Body:**
  ```json
  {
    "proficiencyLevel": "ADVANCED",       // Enum: BEGINNER | INTERMEDIATE | ADVANCED | EXPERT, required
    "yearsExperience": 2.5                // Double, optional, >= 0
  }
  ```
* **Phản hồi thành công (`200 OK`):**
  ```json
  {
    "skillId": 12,
    "skillName": "Java",
    "proficiencyLevel": "ADVANCED",
    "yearsExperience": 2.5,
    "updatedAt": "2026-10-22T16:00:00Z"
  }
  ```
* **Mã lỗi dự kiến:**
  * `400 Bad Request`: Giá trị enum không hợp lệ hoặc số năm kinh nghiệm âm.
  * `401 Unauthorized`: Chưa đăng nhập hoặc token hết hạn.
  * `404 Not Found`: Kỹ năng không tồn tại trong hồ sơ của ứng viên hiện tại.

---

#### `DELETE /api/v1/profile/skills/{skillId}`
* **Mã thao tác (Operation ID):** `removeSkillFromProfile`
* **Mô tả:** Xóa một kỹ năng khỏi hồ sơ ứng viên.
* **Module chịu trách nhiệm:** `user`
* **Quyền truy cập:** Yêu cầu xác thực (`ROLE_CANDIDATE`)
* **Phản hồi thành công (`204 No Content`)**
* **Mã lỗi dự kiến:** `401 Unauthorized`, `404 Not Found`.

---

### 2.3. Phân hệ Khám phá nghề nghiệp & Danh mục Taxonomy (`/api/v1/occupations`)

#### `GET /api/v1/occupations`
* **Mã thao tác (Operation ID):** `searchOccupations`
* **Mô tả:** Tìm kiếm và lọc danh mục nghề nghiệp theo từ khóa, nhóm ngành, phân trang.
* **Module chịu trách nhiệm:** `taxonomy`
* **Quyền truy cập:** Công khai (`Public`)
* **Tham số truy vấn (Query Params):**
  * `query`: Từ khóa tìm kiếm tên/mô tả nghề (String, optional)
  * `groupId`: Lọc theo mã nhóm nghề (Long, optional)
  * `page`: Số trang, 0-indexed (Integer, default: 0)
  * `size`: Số phần tử trên mỗi trang (Integer, default: 20, max: 100)
  * `sort`: Sắp xếp theo trường (String, default: `title,asc`)
* **Phản hồi thành công (`200 OK`):**
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
    "totalElements": 45,
    "totalPages": 3,
    "last": false
  }
  ```

---

#### `GET /api/v1/occupations/{id}`
* **Mã thao tác (Operation ID):** `getOccupationDetail`
* **Mô tả:** Lấy thông tin chi tiết một nghề nghiệp, bao gồm bộ kỹ năng yêu cầu (cốt lõi vs bổ trợ) và chỉ số thị trường lao động.
* **Module chịu trách nhiệm:** `taxonomy` & `lmi`
* **Quyền truy cập:** Công khai (`Public`)
* **Phản hồi thành công (`200 OK`):**
  ```json
  {
    "id": 101,
    "code": "SOC-15-1252",
    "title": "Software Developer / Kỹ sư phần mềm",
    "groupName": "Công nghệ thông tin",
    "description": "Nghiên cứu, thiết kế, phát triển và thử nghiệm các phần mềm ứng dụng...",
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
* **Mã lỗi dự kiến:** `404 Not Found`.

---

#### `GET /api/v1/skills`
* **Mã thao tác (Operation ID):** `searchSkills`
* **Mô tả:** Tìm kiếm danh mục kỹ năng chuẩn hóa (theo O\*NET / ESCO) để người dùng chọn khi tạo hồ sơ.
* **Module chịu trách nhiệm:** `taxonomy`
* **Quyền truy cập:** Công khai (`Public`)
* **Tham số:** `query` (String, min 2 chars), `categoryId` (Long, optional)
* **Phản hồi thành công (`200 OK`):**
  ```json
  [
    {
      "id": 12,
      "code": "SK-JAVA-01",
      "name": "Java",
      "categoryName": "Lập trình",
      "standardSource": "ESCO"
    }
  ]
  ```

---

### 2.4. Phân hệ Gợi ý nghề nghiệp & Chẩn đoán khoảng cách kỹ năng (`/api/v1/recommendations`)

#### `GET /api/v1/recommendations`
* **Mã thao tác (Operation ID):** `getPersonalizedRecommendations`
* **Mô tả:** Tạo danh sách gợi ý nghề nghiệp cá nhân hóa dựa trên hồ sơ kỹ năng của ứng viên và chỉ báo nhu cầu thị trường.
* **Module chịu trách nhiệm:** `recommendation`
* **Quyền truy cập:** Yêu cầu xác thực (`ROLE_CANDIDATE`)
* **Tham số truy vấn (Query Params):**
  * `limit`: Số lượng nghề gợi ý trả về (Integer, default: 10, max: 50)
* **Phản hồi thành công (`200 OK`):**
  ```json
  [
    {
      "rank": 1,
      "occupationId": 101,
      "occupationCode": "SOC-15-1252",
      "occupationTitle": "Software Developer / Kỹ sư phần mềm",
      "groupName": "Công nghệ thông tin",
      "finalScore": 86.4,
      "scoreBreakdown": {
        "skillMatchPct": 85.0,
        "demandScore": 88.5,
        "preferenceMatchPct": 90.0,
        "formula": "0.6*SkillMatch + 0.3*Demand + 0.1*Pref"
      },
      "marketGrowth": "Tăng trưởng cao (+12.4%)",
      "matchedSkillsCount": 6,
      "totalRequiredSkillsCount": 8,
      "explanation": "Phù hợp với 6/8 kỹ năng yêu cầu (Java, SQL, Git...). Nghề có chỉ số nhu cầu tuyển dụng rất cao và mức lương kỳ vọng tương thích."
    }
  ]
  ```
* **Mã lỗi dự kiến:**
  * `400 Bad Request`: Ứng viên chưa khai báo bất kỳ kỹ năng nào trong hồ sơ.
  * `401 Unauthorized`.

---

#### `GET /api/v1/recommendations/{occupationId}/skill-gap`
* **Mã thao tác (Operation ID):** `getSkillGapDiagnostic`
* **Mô tả:** Chẩn đoán khoảng cách kỹ năng chi tiết giữa ứng viên và một nghề nghiệp mục tiêu.
* **Module chịu trách nhiệm:** `recommendation`
* **Quyền truy cập:** Yêu cầu xác thực (`ROLE_CANDIDATE`)
* **Phản hồi thành công (`200 OK`):**
  ```json
  {
    "occupationId": 101,
    "occupationTitle": "Software Developer / Kỹ sư phần mềm",
    "matchPercentage": 75.0,
    "matchedSkills": [
      {
        "skillId": 12,
        "name": "Java",
        "userProficiency": "INTERMEDIATE",
        "requiredImportance": "ESSENTIAL"
      },
      {
        "skillId": 15,
        "name": "SQL",
        "userProficiency": "ADVANCED",
        "requiredImportance": "ESSENTIAL"
      }
    ],
    "missingEssentialSkills": [
      {
        "skillId": 33,
        "name": "Spring Boot / Web Frameworks",
        "requiredImportance": "ESSENTIAL",
        "priority": "HIGH",
        "recommendationNote": "Kỹ năng cốt lõi bắt buộc, cần ưu tiên học tập trước"
      }
    ],
    "missingOptionalSkills": [
      {
        "skillId": 45,
        "name": "Docker & Kubernetes",
        "requiredImportance": "OPTIONAL",
        "priority": "MEDIUM",
        "recommendationNote": "Kỹ năng bổ trợ giúp tăng lợi thế cạnh tranh"
      }
    ]
  }
  ```
* **Mã lỗi dự kiến:** `401 Unauthorized`, `404 Not Found`.

---

### 2.5. Phân hệ Quản trị hệ thống & Dữ liệu (`/api/v1/admin`)

#### `GET /api/v1/admin/users`
* **Mã thao tác (Operation ID):** `adminListUsers`
* **Mô tả:** Quản lý danh sách tài khoản người dùng trong hệ thống.
* **Module chịu trách nhiệm:** `admin`
* **Quyền truy cập:** Yêu cầu vai trò Quản trị viên (`ROLE_ADMIN`)
* **Tham số:** `page`, `size`, `role`, `status`
* **Phản hồi thành công (`200 OK`):** Danh sách người dùng phân trang.
* **Mã lỗi dự kiến:** `401 Unauthorized`, `403 Forbidden`.

---

#### `POST /api/v1/admin/lmi/import`
* **Mã thao tác (Operation ID):** `importMarketData`
* **Mô tả:** Nhập chỉ số thị trường lao động hàng loạt từ bộ dữ liệu thống kê đã kiểm duyệt.
* **Module chịu trách nhiệm:** `admin` & `lmi`
* **Quyền truy cập:** Yêu cầu vai trò Quản trị viên (`ROLE_ADMIN`)
* **Request Body:**
  ```json
  [
    {
      "occupationCode": "SOC-15-1252",
      "demandScore": 88.5,
      "growthRatePct": 12.4,
      "shortageLevel": "HIGH",
      "openingsCount": 1250,
      "dataPeriod": "2025-2026",
      "sourceName": "Cục Thống kê & WEF 2025"
    }
  ]
  ```
* **Phản hồi thành công (`200 OK`):**
  ```json
  {
    "totalRecordsProcessed": 1,
    "recordsInserted": 0,
    "recordsUpdated": 1,
    "timestamp": "2026-10-22T15:45:00Z"
  }
  ```
* **Mã lỗi dự kiến:** `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`.
