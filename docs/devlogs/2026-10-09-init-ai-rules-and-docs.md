# DevLog: Thiết lập AI Guardrails và Hệ thống tài liệu sống (Living Documentation System)

* **Mã tác vụ / Task ID:** `TASK-02-INIT-AI-RULES-AND-DOCS`
* **Người thực hiện / Agent:** Antigravity AI Agent
* **Ngày thực hiện (Date):** 2026-10-09
* **Nhánh Git (Branch):** `main`

---

## 1. Mục tiêu (Objective)

Thiết lập một hệ thống quy tắc vận hành bắt buộc (AI Guardrails) và kiến trúc tài liệu sống (Living Documentation System) chuyên nghiệp, chuẩn mực cho dự án **Skill- and Labor Market Demand–Based Career Recommendation System**. Đảm bảo mọi AI agent và lập trình viên trong tương lai đều có hợp đồng làm việc rõ ràng, không bịa đặt thiết kế (anti-hallucination), không làm mất mát tài liệu nghiên cứu đã có, và duy trì tính nhất quán xuyên suốt quá trình phát triển hệ thống.

---

## 2. Thay đổi thực tế (Changes)

### 2.1. Thiết lập hợp đồng vận hành AI (`AI_RULES.md`)
* Đã tạo file [`AI_RULES.md`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/AI_RULES.md) tại thư mục gốc repository, định nghĩa 8 quy tắc bắt buộc:
  * **Quy tắc A:** Nạp ngữ cảnh bắt buộc (Mandatory Context Loading) trước khi thực hiện tác vụ.
  * **Quy tắc B:** Tài liệu là một phần không thể tách rời của tác vụ (Documentation Is Part of the Task).
  * **Quy tắc C:** Chính sách DevLog riêng biệt theo từng tác vụ (`docs/devlogs/YYYY-MM-DD-...md`), nghiêm cấm dùng một file append-only dùng chung.
  * **Quy tắc D:** Tài liệu chuẩn (Canonical Docs) phải luôn phản ánh trạng thái hiện tại; các phần chưa quyết định phải đánh dấu rõ `TBD` (To Be Decided).
  * **Quy tắc E:** Quy trình ghi nhận quyết định kiến trúc (ADR) với các trạng thái rõ ràng.
  * **Quy tắc F:** An toàn đa Agent và quy tắc an toàn Git.
  * **Quy tắc G:** Tính chính xác học thuật, bảo mật bí mật (không commit secret) và quy ước ngôn ngữ (tiếng Việt cho tài liệu học thuật/nghiệp vụ, tiếng Anh cho định danh kỹ thuật).
  * **Quy tắc H:** Quy trình 7 bước kết thúc tác vụ bắt buộc.

### 2.2. Khởi tạo và tích hợp cấu trúc thư mục tài liệu (`docs/`)
* **Điều hướng trung tâm:** Tạo [`docs/README.md`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/README.md) và [`docs/project-overview.md`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/project-overview.md).
* **Bảo lưu tài liệu nghiên cứu (`docs/research/`):** Tích hợp an toàn 3 báo cáo nghiên cứu học thuật nền tảng:
  * `docs/research/research-overview.md`
  * `docs/research/research-platforms-and-problems.md`
  * `docs/research/research-skill-gap-esco.md`
  * Tạo file mục lục [`docs/research/README.md`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/research/README.md).
* **Yêu cầu hệ thống (`docs/requirements/`):** Tích hợp an toàn tài liệu khảo sát và Actor/Use Case:
  * [`docs/requirements/current-state.md`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/requirements/current-state.md) (từ tài liệu khảo sát hiện trạng 01).
  * [`docs/requirements/actors-and-use-cases.md`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/requirements/actors-and-use-cases.md) (từ tài liệu Actor & Use Case 02).
  * Tạo file mục lục [`docs/requirements/README.md`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/requirements/README.md).
* **Kiến trúc hệ thống (`docs/architecture/`):**
  * Tạo [`docs/architecture/system-architecture.md`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/architecture/system-architecture.md) mô tả kiến trúc cơ sở (Frontend React+TS, Backend Spring Boot, PostgreSQL, Docker) và phân định rõ các thành phần đang chờ phân tích (`TBD`).
  * Tạo file hướng dẫn [`docs/architecture/README.md`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/architecture/README.md).
* **Cơ sở dữ liệu (`docs/database/`):**
  * Tạo [`docs/database/database-design.md`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/database/database-design.md) mô tả cấu hình kết nối PostgreSQL, 4 miền dữ liệu mục tiêu, quy ước đặt tên schema và đánh dấu trạng thái schema chi tiết là `TBD`.
  * Tạo file hướng dẫn [`docs/database/README.md`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/database/README.md).
* **Đặc tả API (`docs/api/`):**
  * Tạo [`docs/api/README.md`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/api/README.md) quy định chuẩn REST API, định dạng lỗi chuẩn JSON, xác thực Bearer JWT, và đánh dấu trạng thái endpoint là `TBD`.
* **Hồ sơ quyết định kiến trúc (`docs/decisions/`):**
  * Tạo mẫu chuẩn [`docs/decisions/ADR-TEMPLATE.md`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/decisions/ADR-TEMPLATE.md).
  * Tạo [`docs/decisions/ADR-0001-baseline-tech-stack.md`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/decisions/ADR-0001-baseline-tech-stack.md) ghi nhận quyết định lựa chọn ngăn xếp công nghệ nền tảng (React + Spring Boot + PostgreSQL + Docker).
  * Tạo file mục lục [`docs/decisions/README.md`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/decisions/README.md).
* **Hướng dẫn phát triển (`docs/development/`):**
  * Tạo [`docs/development/README.md`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/development/README.md) hướng dẫn thiết lập máy trạm, quy trình Docker Compose và các lệnh kiểm thử chất lượng.
* **Nhật ký phát triển (`docs/devlogs/`):**
  * Tạo hướng dẫn [`docs/devlogs/README.md`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/devlogs/README.md).
  * Tạo mẫu chuẩn [`docs/devlogs/DEVLOG-TEMPLATE.md`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/devlogs/DEVLOG-TEMPLATE.md).
  * Tạo bản ghi DevLog này ([`2026-10-09-init-ai-rules-and-docs.md`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/devlogs/2026-10-09-init-ai-rules-and-docs.md)).

---

## 3. Khu vực bị ảnh hưởng (Affected Areas)

* `AI_RULES.md`
* `docs/README.md`
* `docs/project-overview.md`
* `docs/research/` (`README.md`, `research-overview.md`, `research-platforms-and-problems.md`, `research-skill-gap-esco.md`)
* `docs/requirements/` (`README.md`, `current-state.md`, `actors-and-use-cases.md`)
* `docs/architecture/` (`README.md`, `system-architecture.md`)
* `docs/database/` (`README.md`, `database-design.md`)
* `docs/api/` (`README.md`)
* `docs/decisions/` (`README.md`, `ADR-TEMPLATE.md`, `ADR-0001-baseline-tech-stack.md`)
* `docs/development/` (`README.md`)
* `docs/devlogs/` (`README.md`, `DEVLOG-TEMPLATE.md`, `2026-10-09-init-ai-rules-and-docs.md`)

---

## 4. Quyết định kỹ thuật quan trọng (Decisions & Rationale)

1. **Bảo tồn toàn vẹn dữ liệu nghiên cứu sẵn có:** Không tạo lại các tài liệu nghiên cứu chung chung mà tích hợp trực tiếp các tài liệu nghiên cứu và yêu cầu học thuật sẵn có từ thư mục làm việc của nhóm.
2. **Triệt để áp dụng nguyên tắc chống bịa đặt (Anti-Hallucination):** Toàn bộ các bảng CSDL chi tiết, API endpoints và thuật toán chi tiết chưa được quyết định đều được gắn nhãn `TBD` thay vì tự tạo các lớp giả mạo.
3. **Mỗi tác vụ một file DevLog riêng biệt:** Loại bỏ hoàn toàn mô hình append-only trên một file duy nhất để loại bỏ xung đột merge khi nhiều agent/developer làm việc song song.
4. **Song ngữ có chủ đích:** Tiếng Việt cho toàn bộ tài liệu học thuật, nghiệp vụ, use case và tài liệu hướng dẫn; tiếng Anh cho các định danh kỹ thuật, tên biến, URL, và chuẩn cú pháp.

---

## 5. Kiểm tra & Xác thực (Validation Performed)

| Lệnh / Thao tác kiểm tra | Mục đích kiểm tra | Kết quả thực tế | Ghi chú |
|---|---|---|---|
| Kiểm tra cấu trúc file và liên kết nội bộ | Đảm bảo mọi đường dẫn liên kết giữa các tài liệu đều tồn tại và chính xác | **Thành công** | Mọi file trong cây thư mục `docs/` đều liên kết chuẩn xác |
| `git status` | Rà soát trạng thái cây làm việc và các file mới tạo | **Thành công** | Các file tài liệu mới nằm trong trạng thái untracked, không có file thừa hoặc lộ bí mật |
| Kiểm tra bảo mật | Rà soát các file tài liệu không chứa mật khẩu, key, token thực tế | **Thành công** | Chỉ sử dụng các placeholder mẫu |
| `docker compose config` | Kiểm tra tính hợp lệ của cấu hình Docker | **Exit code 0** | Cấu hình dịch vụ hợp lệ |

---

## 6. Công việc tồn đọng & Bước tiếp theo (Outstanding Work)

* [ ] Thiết kế chi tiết lược đồ CSDL PostgreSQL (chi tiết các bảng: users, skills, occupations, recommendations, lmi_indicators).
* [ ] Quyết định lựa chọn công cụ database migration (Flyway vs Liquibase) qua ADR-0002.
* [ ] Thiết kế chi tiết các hợp đồng RESTful API cho UC01 và UC02.
