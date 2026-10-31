# DevLog: Ghi nhận quyết định kỹ thuật Tuần 1 và Ánh xạ tài liệu nguồn cho Task 2–6 và Backlog Tuần 2–5

* **Mã tác vụ / Task ID:** `TASK-W1-DECISION-MAPPING`
* **Người thực hiện / Agent:** Antigravity AI Assistant
* **Ngày thực hiện (Date):** 2026-10-23
* **Nhánh Git (Branch):** `docs/week-1-decisions-and-task-mapping`

---

## 1. Mục tiêu (Objective)

Thực hiện cập nhật tài liệu chuẩn (documentation-only update) nhằm:
1. Chuẩn hóa và ghi nhận chính thức 05 quyết định kỹ thuật quan trọng phục vụ phát triển song song (GAP-01 đến GAP-04 cùng Quy chuẩn định kiểu giao diện Frontend).
2. Xây dựng ma trận tham chiếu tài liệu nguồn bắt buộc cho các nhiệm vụ còn lại của Tuần 1 (Tasks 2–6).
3. Bổ sung mục "Tài liệu nguồn & Căn cứ kỹ thuật" (Source Documents & Technical References) cho toàn bộ các task của 06 thành viên trong Backlog Tuần 2–5 (`docs/planning/week-2-5-backlog.md`).
4. Khắc phục và chuẩn hóa các liên kết markdown sang đường dẫn tương đối trong repository, đảm bảo tính khả chuyển và nhất quán 100% giữa tài liệu kiến trúc, hợp đồng API, thiết kế CSDL và quy chuẩn lập trình.

---

## 2. Thay đổi thực tế (Changes)

* **Mã nguồn (Source Code):**
  * Không thay đổi mã nguồn ứng dụng Java, TypeScript hay cấu hình build (`pom.xml`, `package.json`). Tuân thủ nghiêm ngặt phạm vi tác vụ chỉ tác động vào tài liệu kỹ thuật.
* **Cấu hình & Môi trường (Config & Environment):**
  * Không thay đổi cấu hình runtime, file `.env` hay `docker-compose.yml`.
* **Tài liệu chuẩn (Documentation Updates):**
  * `docs/api/api-contract.md`:
    - Thêm mục **2.0. Điểm kiểm tra trạng thái dịch vụ (System Health Check - `/api/health`)** với phản hồi HTTP 200 `{"status": "UP"}` công khai không cần xác thực token.
    - Tại mục 2.1 (`POST /api/v1/auth/login`), bổ sung bảng đặc tả cấu trúc JWT Payload Claims chuẩn hóa (GAP-04: `sub`, `userId`, `role`) cùng nguyên tắc tạo và tiêu thụ token.
  * `docs/api/README.md`:
    - Bổ sung endpoint `GET /api/health` vào danh mục API Hub.
    - Ghi nhận quy ước tiền tố Base URL `/api/v1` cho các API nghiệp vụ và phân biệt với endpoint sức khỏe hệ thống `/api/health`.
  * `docs/development/coding-conventions.md`:
    - Mục 2.1: Bổ sung chỉ dẫn chuyển dịch kiến trúc từ starter skeleton flat packages (`controller`, `entity`, `repository`, `service`) sang domain module packages `com.cnpm.careerrecommendation.module.<domain>.*` cho Tuần 2–5 (GAP-03).
    - Mục 2.6: Chuẩn hóa quy chuẩn điểm kiểm tra trạng thái dịch vụ `GET /api/health` (GAP-02).
    - Mục 2.7: Chuẩn hóa cấu trúc JWT Claims (`sub`, `userId`, `role`), thời hạn 24h, secret key và nguyên tắc đánh giá quyền phía server không nhân bản tiền tố `ROLE_` (GAP-04).
    - Mục 3.3: Chuẩn hóa cấu hình API Client với biến môi trường `VITE_API_BASE_URL=http://localhost:8080/api/v1` và quy tắc nghiêm cấm lặp tiền tố `/api/v1` trong services (GAP-01).
    - Mục 3.4: Chuẩn hóa quy chuẩn styling giao diện Frontend: Plain CSS + CSS Modules, cấu trúc CSS Variables tại `frontend/src/styles/`, không bắt buộc cài đặt Tailwind CSS (Decision 5).
  * `docs/architecture/system-architecture.md`:
    - Bổ sung ghi chú kiểm tra sức khỏe dịch vụ `GET /api/health` tại tầng Common Module.
    - Cập nhật sơ đồ sequence diagram xác thực token để hiển thị rõ các claim chuẩn hóa (`sub`, `userId`, `role`).
  * `docs/database/database-design.md`:
    - Chuẩn hóa các đường dẫn liên kết sang relative path trong repository.
  * `docs/planning/week-2-5-backlog.md`:
    - Bổ sung **Mục 1.1: Ma trận phụ thuộc tài liệu nguồn Tuần 1** chi tiết cho Tasks 2, 3, 4, 5, 6.
    - Bổ sung cấu trúc mục **Tài liệu nguồn & Căn cứ kỹ thuật** cho 100% các task từ Tuần 2 đến Tuần 5 của cả 06 thành viên (liệt kê rõ: Tài liệu tham chiếu, Endpoint API liên quan, Bảng CSDL liên quan, Quy chuẩn kiến trúc & Mã nguồn).
  * `README.md` & `docs/README.md`:
    - Chuyển đổi toàn bộ các liên kết tham chiếu sang đường dẫn tương đối chuẩn.
  * `docs/devlogs/README.md`:
    - Đăng ký bản ghi DevLog mới vào mục lục nhật ký phát triển.

---

## 3. Khu vực bị ảnh hưởng (Affected Areas)

* `docs/api/api-contract.md`
* `docs/api/README.md`
* `docs/development/coding-conventions.md`
* `docs/architecture/system-architecture.md`
* `docs/database/database-design.md`
* `docs/planning/week-2-5-backlog.md`
* `README.md`
* `docs/README.md`
* `docs/devlogs/README.md`
* `docs/devlogs/2026-10-23-week-1-decisions-and-task-mapping.md`

---

## 4. Quyết định kỹ thuật quan trọng (Decisions & Rationale)

* **GAP-01 (Frontend API Base URL):** Sử dụng `VITE_API_BASE_URL=http://localhost:8080/api/v1`. Toàn bộ service của client gọi theo đường dẫn tương đối từ gốc `/api/v1` (ví dụ `/occupations`) để tránh lỗi duplicate path `/api/v1/api/v1/...`.
* **GAP-02 (Backend Health Check):** Cung cấp `GET /api/health` trả về HTTP 200 `{"status": "UP"}` không yêu cầu xác thực, hoạt động độc lập không phụ thuộc vào trạng thái kết nối CSDL PostgreSQL, phục vụ trực tiếp Docker healthcheck, Nginx và Frontend ping.
* **GAP-03 (Java Domain Packages):** Áp dụng kiến trúc Domain-oriented Modular Monolith: `com.cnpm.careerrecommendation.module.<domain>.*` (`auth`, `profile`, `taxonomy`, `lmi`, `recommendation`). Toàn bộ tính năng Tuần 2–5 viết mới trong module packages; các class skeleton ban đầu được refactor dần khi thực hiện từng task.
* **GAP-04 (JWT Claims Standards):** Thống nhất 3 claim tiêu chuẩn: `sub` (email), `userId` (Long), `role` (`ROLE_CANDIDATE` hoặc `ROLE_ADMIN`). Spring Security nạp trực tiếp `role` vào `GrantedAuthority` mà không ghép thêm `ROLE_` để tránh lỗi phân quyền (RBAC mismatch).
* **Decision 5 (Frontend Styling Convention):** Thống nhất cho phép và khuyến nghị Plain CSS + CSS Modules (`.module.css`), biến dùng chung tại `frontend/src/styles/`. Không bắt buộc Tailwind CSS nhằm tối ưu thời gian phát triển và tránh xung đột phụ thuộc cho 2 thành viên Frontend.

---

## 5. Kiểm tra & Xác thực (Validation Performed)

| Lệnh / Thao tác kiểm tra | Mục đích kiểm tra | Kết quả thực tế | Ghi chú |
|---|---|---|---|
| `git diff --check` | Kiểm tra lỗi cú pháp whitespace, khoảng trắng thừa hoặc thụt lề sai | Thành công, 0 cảnh báo | Không có lỗi whitespace |
| `python check_links.py` | Kiểm tra tính hợp lệ của toàn bộ liên kết relative markdown | Thành công, 0 liên kết hỏng | 100% liên kết trỏ đúng file tồn tại |
| `git status --short` | Xác thực phạm vi thay đổi file | 100% các file thay đổi đều là tài liệu Markdown (`.md`) | Không có file code/config nào bị thay đổi |
| `npm run build` (tại `frontend/`) | Kiểm tra tính tương thích và build sạch của frontend | Thành công, 0 lỗi (`dist/` sinh hoàn chỉnh) | Đảm bảo frontend nguyên vẹn |
| `mvn clean test` (tại `backend/`) | Kiểm tra build và unit tests của backend | Thành công, `BUILD SUCCESS` (Tests run: 1, Failures: 0) | Đảm bảo backend nguyên vẹn |

---

## 6. Công việc tồn đọng & Bước tiếp theo (Outstanding Work)

* [x] Đã hoàn thành ghi nhận đầy đủ GAP-01 đến GAP-04 và Quyết định Styling.
* [x] Đã ánh xạ toàn diện tài liệu nguồn cho Week 1 Tasks 2–6 và Backlog Weeks 2–5.
* [ ] Triển khai các nhiệm vụ Tuần 1 còn lại trên các nhánh tính năng độc lập:
  - Task 2: React Frontend Bootstrap (Đoàn Việt Khang, Huỳnh Long Bảo Khanh)
  - Task 3: Spring Boot Foundation (Bùi Vạn Khải, Phạm Ngọc Gia Khang, Lương Phúc Khang)
  - Task 4: PostgreSQL & Migrations (Phan Nguyễn Thông)
  - Task 5: JWT Auth & Security (Bùi Vạn Khải)
  - Task 6: Docker Compose & E2E Env (Phan Nguyễn Thông)
