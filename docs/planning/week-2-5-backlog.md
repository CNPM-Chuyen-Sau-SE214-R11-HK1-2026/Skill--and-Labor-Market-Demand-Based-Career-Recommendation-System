# Kế hoạch phát triển và Backlog Tuần 2 – Tuần 5 (Development Backlog & Sprint Plan)

**Dự án:** Hệ thống gợi ý nghề nghiệp dựa trên kỹ năng và nhu cầu thị trường lao động (Skill- and Labor Market Demand–Based Career Recommendation System)
**Quy mô đội ngũ:** 06 thành viên
**Tổng thời gian triển khai:** 05 tuần
* **Tuần 1:** Thiết lập kiến trúc, hợp đồng API, thiết kế CSDL, quy chuẩn phát triển (Đã hoàn thành).
* **Tuần 2:** Xây dựng thực thể JPA, Xác thực bảo mật JWT, Khởi tạo khung UI và Dữ liệu danh mục nền tảng.
* **Tuần 3:** Xây dựng dịch vụ nghiệp vụ (Hồ sơ, Kỹ năng, LMI), Thuật toán đối sánh lõi & Màn hình ứng viên.
* **Tuần 4:** Tích hợp toàn diện End-to-End, Chẩn đoán khoảng cách kỹ năng (Skill Gap), Cổng quản trị Admin.
* **Tuần 5:** Kiểm thử tích hợp tự động, Tối ưu hóa hiệu năng, Đóng gói Docker Compose & Nghiệm thu đồ án.

---

## 1. Ma trận phân công trách nhiệm đội ngũ (RACI / Ownership Matrix)

**Lưu ý phân công hiện hành W1 (2026-10-29):** Excel `CNPM Chuyên Sâu - Nhóm 4.xlsx`,
sheet `Task`, giao **W1-T5 — Xác thực và phân quyền JWT cho Đoàn Việt Khang**.
Bản bàn giao này hoàn thành phạm vi W1-T5, không coi các task frontend W2–W5
của Việt Khang bên dưới là đã hoàn tất. Bảng dưới vẫn lưu kế hoạch Tuần 2–5.
Tham khảo [hướng dẫn W1-T5](../development/w1-t5-auth-guide.md).


Căn cứ theo Bảng 1 trong tài liệu đồ án chính thức:

| STT | Thành viên | Mã số SV | Vai trò chính | Phạm vi phụ trách kỹ thuật (Domain Ownership) |
| :---: | :--- | :---: | :--- | :--- |
| 1 | **Bùi Vạn Khải** | 24520719 | **Nhóm trưởng / Tech Lead** | Kiến trúc hệ thống, Dịch vụ gợi ý nghề nghiệp lõi (Recommendation Engine), Bảo mật & Xác thực (Spring Security + JWT) |
| 2 | **Phạm Ngọc Gia Khang** | 24520765 | **Backend Developer** | Quản lý hồ sơ ứng viên (Candidate Profile), Dịch vụ kỹ năng cá nhân (User Skills), Nguyện vọng nghề nghiệp |
| 3 | **Lương Phúc Khang** | 24520747 | **Backend Developer** | Từ điển kỹ năng chuẩn hóa (Taxonomy), Danh mục nghề nghiệp (Occupations), Nạp chỉ số thị trường lao động (LMI Services) |
| 4 | **Đoàn Việt Khang** | 24520730 | **Frontend Developer** | Giao diện ứng viên (Candidate UI): Quản lý hồ sơ cá nhân, Bảng chọn kỹ năng, Bảng điều khiển gợi ý việc làm |
| 5 | **Huỳnh Long Bảo Khanh** | 24520781 | **Frontend Developer** | Giao diện khám phá thị trường & Quản trị (Admin UI): Tra cứu danh mục nghề nghiệp, Trực quan hóa LMI, Cổng quản trị |
| 6 | **Phan Nguyễn Thông** | 24521722 | **Database & DevOps / QA** | Quản trị CSDL (Flyway Migrations), Hạ tầng container (Docker/Compose), Pipeline CI/CD, Kiểm thử tích hợp (QA & Testing) |

---

## 2. Kế hoạch chi tiết Tuần 2 (Sprint 2): Nền tảng Dữ liệu, Bảo mật & Bộ khung giao diện

### Mục tiêu Sprint:
Toàn bộ 6 thành viên thiết lập xong các module cơ bản, cơ sở dữ liệu có thể chạy migration sạch qua Flyway, luồng đăng nhập JWT hoạt động hoàn chỉnh và Frontend có bộ khung điều hướng và quản lý trạng thái.

### Phân rã công việc chi tiết:

#### 1. Bùi Vạn Khải (Leader / Security & Engine Foundation)
* **Mã task:** `W2-BE-SEC-01`
* **Nhiệm vụ:** Hiện thực hóa Spring Security 6 với JWT Authentication Filter, Custom UserDetailsService, PasswordEncoder (BCrypt) và cấu hình CORS/CSRF.
* **Đầu vào phụ thuộc:** `backend/src/main/resources/db/migration/V1__initial_schema.sql` (bảng `users`), [`docs/api/api-contract.md`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/api/api-contract.md) mục 2.
* **Tiêu chí nghiệm thu (DoD):**
  - Endpoint `POST /api/v1/auth/register` lưu thành công người dùng mới với mật khẩu BCrypt.
  - Endpoint `POST /api/v1/auth/login` kiểm tra mật khẩu chính xác và trả về JWT Bearer token hợp lệ (thời hạn 24h).
  - Endpoint `GET /api/v1/auth/me` trả về thông tin người dùng đăng nhập hiện tại khi kèm Header `Authorization: Bearer <token>`.
  - Có unit test và integration test đạt tỷ lệ bao phủ > 80% cho lớp xác thực.

#### 2. Phạm Ngọc Gia Khang (Backend: Candidate Profile & Skills)
* **Mã task:** `W2-BE-PRF-01`
* **Nhiệm vụ:** Xây dựng JPA Entities, Repositories, DTOs và Services cho bảng `candidate_profiles` và `user_skills`.
* **Đầu vào phụ thuộc:** Bảng `candidate_profiles` và `user_skills` trong V1 schema, hợp đồng API mục 3.
* **Tiêu chí nghiệm thu (DoD):**
  - Endpoint `GET /api/v1/profile` trả về hồ sơ ứng viên gắn với User ID hiện hành.
  - Endpoint `PUT /api/v1/profile` cập nhật hợp lệ thông tin headline, bio, số năm kinh nghiệm, nguyện vọng và validation mức lương (`expected_salary_min <= expected_salary_max`).
  - Xử lý ném lỗi 404 `PROFILE_NOT_FOUND` và 400 `VALIDATION_FAILED` theo đúng chuẩn `ErrorResponse`.

#### 3. Lương Phúc Khang (Backend: Taxonomy & Occupations)
* **Mã task:** `W2-BE-TAX-01`
* **Nhiệm vụ:** Xây dựng JPA Entities và Repositories cho `skill_categories`, `skills`, `occupation_groups`, `occupations`, `occupation_skills`. Xây dựng dữ liệu mẫu (Seed Data) chuẩn O\*NET ban đầu.
* **Đầu vào phụ thuộc:** Bảng phân loại chuẩn trong V1 schema, hợp đồng API mục 4.
* **Tiêu chí nghiệm thu (DoD):**
  - Endpoint `GET /api/v1/occupations` hỗ trợ phân trang (`page`, `size`), tìm kiếm theo từ khóa `search` và lọc theo `industry`.
  - Endpoint `GET /api/v1/occupations/{id}` trả về thông tin chi tiết nghề nghiệp kèm danh sách kỹ năng yêu cầu (`skills`) và chỉ số LMI gần nhất.
  - Tích hợp migration data hoặc script seed ít nhất 10 nhóm nghề nghiệp mẫu của ngành CNTT.

#### 4. Đoàn Việt Khang (Frontend: Auth & Candidate Layout)
* **Mã task:** `W2-FE-CND-01`
* **Nhiệm vụ:** Xây dựng cấu trúc điều hướng React Router (v6+), cài đặt Axios Interceptor tự động đính kèm JWT token vào header, xây dựng màn hình Đăng nhập (`/login`), Đăng ký (`/register`) và Khung layout ứng viên.
* **Đầu vào phụ thuộc:** API Contract mục 2, giao diện React nền tảng hiện có.
* **Tiêu chí nghiệm thu (DoD):**
  - Người dùng có thể đăng nhập, lưu JWT vào LocalStorage / In-memory, chuyển hướng vào Dashboard.
  - Khi token hết hạn hoặc lỗi 401, tự động xóa phiên và điều hướng về trang đăng nhập.
  - Thiết kế Form đăng nhập/đăng ký có validate phía client (email format, độ dài password).

#### 5. Huỳnh Long Bảo Khanh (Frontend: Discovery UI & Layout Quản trị)
* **Mã task:** `W2-FE-ADM-01`
* **Nhiệm vụ:** Xây dựng trang Khám phá danh mục nghề nghiệp công khai (`/occupations`), hiển thị danh sách thẻ nghề nghiệp dạng lưới (Grid), phân trang và thanh tìm kiếm từ khóa. Xây dựng khung giao diện Admin (`/admin`).
* **Đầu vào phụ thuộc:** API Contract mục 4 và mục 6.
* **Tiêu chí nghiệm thu (DoD):**
  - Màn hình `/occupations` tải và hiển thị danh sách chức danh nghề nghiệp, mức lương tham chiếu và tag kỹ năng.
  - Giao diện Admin có điều hướng Sidebar cho phép truy cập mục nạp dữ liệu LMI.
  - Sử dụng giao diện chuẩn Responsive (Desktop & Mobile).

#### 6. Phan Nguyễn Thông (Database, DevOps & CI Pipeline)
* **Mã task:** `W2-OPS-ENV-01`
* **Nhiệm vụ:** Kiểm thử cấu hình Flyway trên Docker Postgres container, kiểm tra script `V1__initial_schema.sql`, xây dựng GitHub Actions CI workflow (hoặc script kiểm tra cục bộ) tự động chạy `mvn test` và `npm run build` khi có Pull Request.
* **Đầu vào phụ thuộc:** [`docker-compose.yml`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docker-compose.yml), `backend/pom.xml`, `frontend/package.json`.
* **Tiêu chí nghiệm thu (DoD):**
  - Chạy `docker compose up postgres` và khởi động Backend thì Flyway tự động tạo đủ 10 bảng và bảng `flyway_schema_history` thành công không có lỗi.
  - Tài liệu hướng dẫn khởi chạy môi trường dev và quy trình chạy test nội bộ được xác nhận chạy thông suốt trên máy mọi thành viên.

---

## 3. Kế hoạch chi tiết Tuần 3 (Sprint 3): Dịch vụ nghiệp vụ, Thuật toán đối sánh lõi & Tương tác ứng viên

### Mục tiêu Sprint:
Hoàn thành toàn bộ logic nạp kỹ năng ứng viên, nạp dữ liệu LMI, và triển khai lõi thuật toán đối sánh kỹ năng kết hợp nhu cầu thị trường.

### Phân rã công việc chi tiết:

#### 1. Bùi Vạn Khải (Core Recommendation Engine & Matching Logic)
* **Mã task:** `W3-BE-REC-01`
* **Nhiệm vụ:** Hiện thực hóa `RecommendationEngineService` tính toán điểm số phù hợp theo công thức toán học chuẩn:
  $$\text{FinalScore} = 0.6 \cdot \text{SkillMatch} + 0.3 \cdot \text{DemandScore} + 0.1 \cdot \text{PrefMatch}$$
  Xây dựng thuật toán phân tích khoảng cách kỹ năng (Skill Gap Analysis).
* **Đầu vào phụ thuộc:** Hợp đồng API mục 5, dữ liệu từ `user_skills`, `occupation_skills`, `market_demand_indicators`.
* **Tiêu chí nghiệm thu (DoD):**
  - Đạt chỉ tiêu độ trễ: thời gian tính toán trả về Top 10 đề xuất $< 500\text{ms}$ cho tập 1,000 nghề nghiệp.
  - Xác định chính xác danh sách kỹ năng đã đạt (`matched_skills`) và kỹ năng còn thiếu (`missing_skills`).
  - Ghi nhật ký vào bảng `recommendation_logs` cho mỗi lượt đề xuất thành công.
  - Bộ kiểm thử thuật toán bao phủ đầy đủ các ca biên (0% kỹ năng, 100% kỹ năng, không khớp nguyện vọng).

#### 2. Phạm Ngọc Gia Khang (Backend: Skill Management & Assessment)
* **Mã task:** `W3-BE-SKL-01`
* **Nhiệm vụ:** Hiện thực hóa các API quản lý bộ kỹ năng của ứng viên: thêm kỹ năng, cập nhật mức độ thành thạo, số năm kinh nghiệm, xóa kỹ năng.
* **Đầu vào phụ thuộc:** API Contract mục 3.2 (`/api/v1/profile/skills`).
* **Tiêu chí nghiệm thu (DoD):**
  - Endpoint `POST /api/v1/profile/skills` cho phép thêm mới kỹ năng, kiểm tra trùng lặp (`409 CONFLICT`).
  - Endpoint `PUT /api/v1/profile/skills/{id}` cập nhật mức độ thành thạo (`BEGINNER`, `INTERMEDIATE`, `ADVANCED`, `EXPERT`).
  - Endpoint `DELETE /api/v1/profile/skills/{id}` gỡ bỏ kỹ năng khỏi hồ sơ ứng viên.

#### 3. Lương Phúc Khang (Backend: LMI Ingestion & Market Indicators)
* **Mã task:** `W3-BE-LMI-01`
* **Nhiệm vụ:** Hiện thực hóa service nạp và chuẩn hóa dữ liệu thị trường lao động LMI (`market_demand_indicators`), xây dựng endpoint cho Quản trị viên nạp chỉ số nhu cầu tuyển dụng theo từng khu vực.
* **Đầu vào phụ thuộc:** Bảng `market_demand_indicators`, API Contract mục 2.5 (`POST /api/v1/admin/lmi/import`).
* **Tiêu chí nghiệm thu (DoD):**
  - Xử lý nạp lô dữ liệu chỉ số thị trường (Batch ingestion), hỗ trợ ghi đè (`UPSERT`) theo bộ khóa `(occupation_id, region, snapshot_date)`.
  - Validate dữ liệu nạp: `demand_score` bắt buộc từ 0 đến 100.
  - Phân quyền nghiêm ngặt: chỉ tài khoản có vai trò `ROLE_ADMIN` mới có thể gọi endpoint nạp chỉ số.

#### 4. Đoàn Việt Khang (Frontend: Skill Selector & Profile Editor)
* **Mã task:** `W3-FE-SKL-01`
* **Nhiệm vụ:** Xây dựng màn hình Chỉnh sửa hồ sơ ứng viên (`/profile`), tích hợp giao diện tìm kiếm và chọn kỹ năng với Autocomplete, đánh giá mức độ thành thạo và số năm kinh nghiệm.
* **Đầu vào phụ thuộc:** API endpoints `/api/v1/profile`, `/api/v1/profile/skills`, `/api/v1/skills`.
* **Tiêu chí nghiệm thu (DoD):**
  - Người dùng có thể gõ tìm kiếm kỹ năng, chọn kỹ năng từ gợi ý, chọn cấp độ và lưu vào hồ sơ tức thời.
  - Hiển thị danh sách kỹ năng đã chọn dưới dạng Badge / Tag trực quan kèm nút gỡ bỏ.
  - Tích hợp thông báo thành công / cảnh báo lỗi Toast Notification mượt mà.

#### 5. Huỳnh Long Bảo Khanh (Frontend: Occupation Detail & LMI Charting)
* **Mã task:** `W3-FE-DET-01`
* **Nhiệm vụ:** Xây dựng trang chi tiết nghề nghiệp (`/occupations/:id`) hiển thị mô tả công việc, mức lương, danh sách kỹ năng yêu cầu phân loại theo trọng số quan trọng, và biểu đồ trực quan hóa chỉ số nhu cầu tuyển dụng (sử dụng thư viện Recharts / Chart.js).
* **Đầu vào phụ thuộc:** API endpoint `/api/v1/occupations/{id}`.
* **Tiêu chí nghiệm thu (DoD):**
  - Hiển thị đầy đủ thông tin chi tiết và đồ thị cột/đường xu hướng nhu cầu thị trường theo vùng.
  - Tách bạch rõ nhóm kỹ năng cốt lõi (Bắt buộc) và kỹ năng bổ trợ (Tùy chọn).

#### 6. Phan Nguyễn Thông (DevOps, Test Data Generation & Flyway Baseline)
* **Mã task:** `W3-OPS-DAT-01`
* **Nhiệm vụ:** Xây dựng script nạp dữ liệu giả lập chất lượng cao (`V2__seed_demo_data.sql`) gồm 30 kỹ năng thực tế, 15 nghề nghiệp CNTT phổ biến, 100 chỉ số thị trường lao động mẫu để phục vụ chạy thử nghiệm thuật toán và demo.
* **Đầu vào phụ thuộc:** `V1__initial_schema.sql`.
* **Tiêu chí nghiệm thu (DoD):**
  - Dữ liệu seed nạp trơn tru không vi phạm khóa ngoại.
  - Viết bộ kịch bản kiểm thử Postman / Bruno tập trung kiểm tra toàn bộ luồng nghiệp vụ Tuần 3.

---

## 4. Kế hoạch chi tiết Tuần 4 (Sprint 4): Tích hợp luồng Gợi ý hoàn chỉnh, Bảng điều khiển & Phân tích khoảng cách kỹ năng

### Mục tiêu Sprint:
Tích hợp toàn diện giao diện gợi ý nghề nghiệp với Recommendation Engine, hiển thị phân tích khoảng cách kỹ năng (Skill Gap Analysis), hoàn thiện cổng quản trị nạp dữ liệu.

### Phân rã công việc chi tiết:

#### 1. Bùi Vạn Khải (Engine Optimization & Gap Analysis Recommendation)
* **Mã task:** `W4-BE-ENG-01`
* **Nhiệm vụ:** Tinh chỉnh công thức đối sánh khoảng cách kỹ năng, tính toán phần trăm kỹ năng đáp ứng và sinh đề xuất lộ trình bổ sung các kỹ năng còn thiếu theo mức độ ưu tiên trọng số.
* **Đầu vào phụ thuộc:** `RecommendationEngineService`.
* **Tiêu chí nghiệm thu (DoD):**
  - Trả về danh sách kỹ năng còn thiếu sắp xếp theo thứ tự giảm dần của `importance_weight`.
  - Tối ưu hóa truy vấn CSDL để không gặp lỗi $N+1$ query khi lấy dữ liệu liên kết kỹ năng và chỉ số LMI.

#### 2. Phạm Ngọc Gia Khang (Backend: Recommendation History & Feedback)
* **Mã task:** `W4-BE-HST-01`
* **Nhiệm vụ:** Xây dựng endpoint tra cứu lịch sử các lần gợi ý của ứng viên và chức năng lưu vết phản hồi người dùng.
* **Đầu vào phụ thuộc:** Bảng `recommendation_logs`.
* **Tiêu chí nghiệm thu (DoD):**
  - Endpoint `GET /api/v1/recommendations/history` trả về danh sách lịch sử có phân trang và sắp xếp giảm dần theo ngày tạo.

#### 3. Lương Phúc Khang (Backend: Admin Batch Processing & Data Sync)
* **Mã task:** `W4-BE-ADM-01`
* **Nhiệm vụ:** Xây dựng endpoint quản trị nạp hàng loạt chức danh nghề nghiệp và kỹ năng liên kết qua file định dạng JSON/CSV.
* **Đầu vào phụ thuộc:** API Contract mục 6 (`POST /api/v1/admin/occupations/batch`).
* **Tiêu chí nghiệm thu (DoD):**
  - Cho phép quản trị viên tải lên cấu hình danh mục nghề nghiệp mới kèm bộ kỹ năng và tự động lưu đồng bộ trong transaction an toàn.

#### 4. Đoàn Việt Khang (Frontend: Recommendation Result Dashboard & Gap View)
* **Mã task:** `W4-FE-REC-01`
* **Nhiệm vụ:** Xây dựng Bảng điều khiển gợi ý nghề nghiệp (`/recommendations`), hiển thị thẻ Top nghề nghiệp phù hợp nhất với thanh tiến độ % tương thích, danh sách kỹ năng đã có (màu xanh lá) và kỹ năng cần cải thiện (màu hổ phách/đỏ).
* **Đầu vào phụ thuộc:** API Contract mục 5 (`GET /api/v1/recommendations`).
* **Tiêu chí nghiệm thu (DoD):**
  - Hiển thị trực quan điểm tổng hợp, điểm kỹ năng, điểm thị trường, điểm nguyện vọng.
  - Cho phép lọc nhanh kết quả theo mức độ lương hoặc khoảng cách kỹ năng ít nhất.

#### 5. Huỳnh Long Bảo Khanh (Frontend: Admin Ingestion Portal & Data Management)
* **Mã task:** `W4-FE-ADM-02`
* **Nhiệm vụ:** Xây dựng giao diện Cổng Quản trị viên (`/admin/market-data`): nạp số liệu chỉ số thị trường lao động bằng Form thủ công hoặc tải file dữ liệu, hiển thị bảng danh sách các đợt nạp dữ liệu gần nhất.
* **Đầu vào phụ thuộc:** API Contract mục 6.
* **Tiêu chí nghiệm thu (DoD):**
  - Màn hình nạp dữ liệu phản hồi trạng thái rõ ràng (thành công số bản ghi, cảnh báo bản ghi lỗi).
  - Phân quyền giao diện: nếu người dùng không có vai trò `ROLE_ADMIN`, tự động hiển thị thông báo 403 Forbidden hoặc điều hướng về trang chủ.

#### 6. Phan Nguyễn Thông (DevOps, E2E Testing & Performance Profiling)
* **Mã task:** `W4-OPS-PRF-01`
* **Nhiệm vụ:** Thiết lập môi trường chạy thử nghiệm Docker Compose tổng thể (Frontend + Backend + PostgreSQL), đo lường hiệu năng truy vấn API dưới tải 50 concurrent requests.
* **Đầu vào phụ thuộc:** Dockerfile của các module và Docker Compose.
* **Tiêu chí nghiệm thu (DoD):**
  - Hệ thống vận hành trơn tru trong container Docker cục bộ mà không gặp xung đột mạng hay biến môi trường.
  - Toàn bộ thời gian phản hồi của các API đọc dữ liệu $< 300\text{ms}$.

---

## 5. Kế hoạch chi tiết Tuần 5 (Sprint 5): Kiểm thử toàn diện, Tinh chỉnh, Đóng gói Docker & Nghiệm thu

### Mục tiêu Sprint:
Đóng băng tính năng mới (Feature Freeze), tập trung tối đa vào kiểm thử hồi quy, sửa lỗi, hoàn thiện tài liệu kỹ thuật, chuẩn bị kịch bản nghiệm thu và đóng gói sản phẩm hoàn chỉnh sẵn sàng trình bày.

### Phân rã công việc chi tiết:

#### 1. Toàn bộ đội ngũ (All Members)
* **Đóng băng mã nguồn (Feature Freeze):** Không thêm tính năng mới ngoài phạm vi MVP đã cam kết.
* **Kiểm thử hồi quy (Regression Testing):** Chạy lại toàn bộ test suite frontend và backend.
* **Đóng gói bàn giao:** Kiểm tra file `docker-compose.yml` khởi chạy bằng một lệnh duy nhất:
  ```bash
  docker compose up -d --build
  ```

#### 2. Phân công nghiệm thu cụ thể:
* **Bùi Vạn Khải:**
  - Tổng duyệt mã nguồn, tối ưu hóa thuật toán và bảo mật.
  - Chuẩn bị báo cáo tóm tắt kiến trúc và bài trình bày báo cáo đồ án.
* **Phạm Ngọc Gia Khang & Lương Phúc Khang:**
  - Hoàn thiện tài liệu API nội bộ (Swagger/OpenAPI documentation nếu cần).
  - Viết bộ unit test bổ sung cho các kịch bản lỗi ngoại lệ (Negative Test Cases).
* **Đoàn Việt Khang & Huỳnh Long Bảo Khanh:**
  - Tinh chỉnh trải nghiệm người dùng UI/UX, kiểm tra tính tương thích trên các kích thước màn hình khác nhau.
  - Xử lý trạng thái tải (Loading Skeleton) và thông báo lỗi thân thiện.
* **Phan Nguyễn Thông:**
  - Kiểm tra script dọn dẹp và reset database sạch sẽ.
  - Xác thực việc không để lộ khóa bí mật hay dữ liệu nhạy cảm trong repository.
  - Đóng gói tài liệu bàn giao sản phẩm.

---

## 6. Kế hoạch quản trị rủi ro & Biện pháp khắc phục (Risk Management Matrix)

| Mã rủi ro | Mô tả rủi ro kỹ thuật | Mức độ ảnh hưởng | Khả năng xảy ra | Biện pháp phòng ngừa & Giảm thiểu |
| :---: | :--- | :---: | :---: | :--- |
| **RSK-01** | Thiếu dữ liệu thị trường việc làm thực tế thời gian thực | Trung bình | Cao | Sử dụng bộ chỉ số thị trường lao động mẫu đã chuẩn hóa từ O\*NET và dữ liệu khảo sát việc làm CNTT Việt Nam được nạp sẵn qua migration seed (`V2__seed_demo_data.sql`). |
| **RSK-02** | Xung đột mã nguồn khi 6 thành viên làm việc song song | Cao | Trung bình | Tuân thủ tuyệt đối quy trình Git Feature Branching (`docs/development/git-workflow.md`), mỗi thành viên sở hữu package/module độc lập, bắt buộc PR review trước khi merge vào `main`. |
| **RSK-03** | Lệch cấu hình môi trường giữa máy các thành viên | Cao | Trung bình | Chuẩn hóa môi trường thông qua Docker Compose và file `.env.example`, khóa phiên bản Node.js (v18+) và Java OpenJDK 17. |
| **RSK-04** | Hiệu năng thuật toán gợi ý bị chậm khi tập dữ liệu lớn | Trung bình | Thấp | Tạo đầy đủ các B-tree index trên các cột khóa ngoại và tiêu chí tra cứu (`docs/database/database-design.md`), giới hạn phân trang kết quả Top 10 đề xuất. |
