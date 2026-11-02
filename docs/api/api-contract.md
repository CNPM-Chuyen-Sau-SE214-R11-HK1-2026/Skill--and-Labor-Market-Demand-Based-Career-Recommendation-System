# Đặc tả Hợp đồng REST API (REST API Contract Specification)

**Phiên bản:** 1.1.0 (W1-T5, 2026-10-29)
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

### 2.0. Điểm kiểm tra trạng thái dịch vụ (System Health Check - `/api/health`)

#### `GET /api/health`
* **Mã thao tác (Operation ID):** `healthCheck`
* **Mô tả:** Kiểm tra trạng thái hoạt động cơ bản của hệ thống backend. Endpoint này phản hồi tức thời mà không phụ thuộc vào kết nối cơ sở dữ liệu PostgreSQL và độc lập hoàn toàn với các endpoint nội bộ của Spring Boot Actuator.
* **Đường dẫn (Path):** `/api/health` *(Lưu ý: Endpoint này nằm trực tiếp tại root `/api/health`, không nằm dưới tiền tố phiên bản `/api/v1`)*.
* **Module chịu trách nhiệm:** `common`
* **Quyền truy cập:** Công khai (`Public`) - Không yêu cầu JWT Token.
* **Request Body:** Không có.
* **Phản hồi thành công (`200 OK`):**
  ```json
  {
    "status": "UP"
  }
  ```
* **Mã lỗi dự kiến:** Endpoint này luôn phản hồi HTTP 200 `{"status": "UP"}` khi tiến trình Spring Boot đang chạy và sẵn sàng tiếp nhận kết nối HTTP.

---

### 2.1. Phân hệ Xác thực & Tài khoản (`/api/v1/auth`)

**Trạng thái triển khai:** Ba endpoint auth dưới đây đã có mã nguồn. Các endpoint
profile/taxonomy/recommendation/admin phía dưới vẫn là hợp đồng cho các task tiếp theo;
W1-T5 chỉ đặt quy tắc Security cho các nhóm URL đó.

**JWT:** HS256, `sub=email`, `userId` là số nguyên dương, `role` là
`ROLE_CANDIDATE` hoặc `ROLE_ADMIN`; có `iat` và `exp`. Mặc định 24 giờ,
`expiresIn` tính bằng giây theo `JWT_EXPIRATION_MS`.
Mỗi request đọc lại `users` để từ chối tài khoản không tồn tại, bị khóa, đổi email
hoặc đổi vai trò kể từ lúc cấp token. Token sai/hết hạn trả 401 JSON.

**Dữ liệu đầu vào:** Email được trim và chuyển chữ thường; fullName/phone được trim.
Không trim mật khẩu. Mật khẩu dài 8–64 ký tự và tối đa 72 byte UTF-8, không chứa NUL.
`phone` tùy chọn được lưu bằng migration V2. Không có API đăng ký admin:
role luôn được đặt phía server là `ROLE_CANDIDATE`.
JSON có trường lạ (vd. `role`, `isActive`, `userId`) bị từ chối 400.
`errors[].rejectedValue` luôn là null để tránh trả lại mật khẩu/dữ liệu cá nhân.
Đăng ký/đăng nhập bỏ qua Bearer cũ để người dùng có thể đăng nhập lại.
Chi tiết triển khai và kiểm chứng: [W1-T5](../development/w1-t5-auth-guide.md).


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

* **Đặc tả cấu trúc JWT Payload Claims chuẩn hóa (GAP-04):**
  Khi xác thực thành công, access token trả về là một JSON Web Token (JWT) được ký bằng thuật toán HMAC-SHA256 (`HS256`) với khóa bí mật cấu hình qua biến môi trường `JWT_SECRET` (kèm fallback mặc định phục vụ phát triển cục bộ). Thời gian hiệu lực mặc định của token là 24 giờ (86.400.000 ms).
  Payload của JWT bắt buộc chứa chính xác 3 claim chuẩn hóa sau:

  | Tên Claim | Kiểu dữ liệu | Mô tả | Ví dụ giá trị | Ghi chú & Tiêu chuẩn sử dụng |
  | :--- | :--- | :--- | :--- | :--- |
  | `sub` | `String` | Email định danh của người dùng (Subject) | `"candidate@example.com"` | Khóa email chuẩn theo RFC 7519, dùng cho Spring Security `UserDetails.getUsername()` |
  | `userId` | `Long` (Numeric) | Khóa chính của người dùng trong CSDL (`users.id`) | `1` | Dùng để định danh thực thể hồ sơ (`candidate_profiles.user_id`), trích xuất an toàn phía backend |
  | `role` | `String` | Vai trò bảo mật kèm tiền tố `ROLE_` | `"ROLE_CANDIDATE"` hoặc `"ROLE_ADMIN"` | Đánh giá phân quyền trực tiếp qua `SimpleGrantedAuthority(role)`. Không nhân bản hay ghép thêm tiền tố `ROLE_` |

* **Nguyên tắc tạo và tiêu thụ Token (Token Generation & Consumption Rules):**
  - **Phía Backend khi phát hành token:** `JwtTokenProvider` bắt buộc đặt đúng 3 khóa claim `sub`, `userId`, `role`.
  - **Phía Backend khi xác thực request:** `JwtAuthenticationFilter` giải mã token, đọc `role` trực tiếp đưa vào `GrantedAuthority` mà không nối thêm tiền tố `ROLE_` (do `role` đã chứa sẵn tiền tố này), trích xuất `userId` và `sub` phục vụ gắn vào `UsernamePasswordAuthenticationToken`.
  - **Phía Frontend:** Có thể decode payload để lấy `userId` và `role` phục vụ hiển thị UI và điều hướng route phù hợp.

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

### 2.2. Phân hệ Hồ sơ ứng viên & Kỹ năng cá nhân (/api/v1/profile)

> [!NOTE]
> Toàn bộ hợp đồng chi tiết, quy tắc bảo mật quyền sở hữu tài nguyên và các trường hợp lỗi HTTP 404/400 của phân hệ Profile đã được tách riêng và do **Phạm Ngọc Gia Khang (W2-T2)** sở hữu độc quyền tại:
>
> 👉 [**Đặc tả Hợp đồng REST API Phân hệ Hồ sơ Ứng viên (docs/api/profile.md)**](./profile.md)
>
> **Tóm tắt danh mục endpoints:**
> - GET /api/v1/profile: Lấy thông tin hồ sơ (trả HTTP 404 PROFILE_NOT_FOUND nếu chưa tạo, không tự sinh ngầm).
> - PUT /api/v1/profile: Tạo mới hoặc cập nhật hồ sơ (validate salaryMin <= salaryMax).
> - GET /api/v1/profile/skills: Danh sách kỹ năng của ứng viên.
> - POST /api/v1/profile/skills: Thêm kỹ năng cá nhân (tham chiếu common.entity.Skill).
> - PUT /api/v1/profile/skills/{skillId}: Cập nhật mức độ thành thạo / số năm kinh nghiệm.
> - DELETE /api/v1/profile/skills/{skillId}: Xóa kỹ năng khỏi hồ sơ (204 No Content).

---

### 2.3. Phân hệ Khám phá nghề nghiệp & Danh mục Taxonomy (/api/v1/occupations, /api/v1/skills)

> [!NOTE]
> Toàn bộ hợp đồng chi tiết và định dạng dữ liệu Seed Data V3 của phân hệ Taxonomy đã được tách riêng và do **Huỳnh Long Bảo Khanh (W2-T3)** sở hữu độc quyền tại:
>
> 👉 [**Đặc tả Hợp đồng REST API Phân hệ Danh mục Nghề nghiệp & Kỹ năng (docs/api/taxonomy.md)**](./taxonomy.md)
>
> **Tóm tắt danh mục endpoints:**
> - GET /api/v1/occupations: Phân trang, tìm kiếm theo từ khóa, lọc theo nhóm ngành (công khai).
> - GET /api/v1/occupations/{id}: Xem chi tiết nghề, danh sách kỹ năng trọng số và chỉ số LMI (công khai).
> - GET /api/v1/skills: Danh mục từ điển kỹ năng chuẩn hóa O*NET/ESCO (công khai).

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
