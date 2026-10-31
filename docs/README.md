# Living Documentation System

**Project:** Skill- and Labor Market Demand–Based Career Recommendation System  
*(Hệ thống gợi ý nghề nghiệp dựa trên hồ sơ kỹ năng và nhu cầu thị trường lao động)*

Chào mừng bạn đến với hệ thống tài liệu sống (Living Documentation System) của dự án. Thư mục này lưu trữ toàn bộ tri thức của dự án từ nghiên cứu học thuật, yêu cầu nghiệp vụ, kiến trúc hệ thống, thiết kế cơ sở dữ liệu, đặc tả API, quyết định kỹ thuật cho đến nhật ký phát triển theo từng tác vụ.

> [!IMPORTANT]
> **Quy định bắt buộc:** Mọi cộng tác viên và AI agent khi làm việc trong repository này đều phải đọc và tuân thủ tuyệt đối quy định tại [**`AI_RULES.md`**](../AI_RULES.md).

---

## 1. Cấu trúc cây tài liệu (Documentation Tree)

```text
docs/
├── README.md                      # Tài liệu điều hướng trung tâm (file này)
├── project-overview.md            # Tổng quan dự án, bài toán và phạm vi giải pháp
│
├── research/                      # Các tài liệu nghiên cứu chuyên sâu & cơ sở khoa học
│   ├── README.md
│   ├── research-overview.md
│   ├── research-platforms-and-problems.md
│   └── research-skill-gap-esco.md
│
├── requirements/                  # Yêu cầu nghiệp vụ, tác nhân và ca sử dụng
│   ├── README.md
│   ├── current-state.md           # Khảo sát hiện trạng & thu thập yêu cầu (FR/NFR)
│   └── actors-and-use-cases.md    # Danh mục Actor & Use Case chi tiết
│
├── architecture/                  # Kiến trúc hệ thống và thiết kế kỹ thuật
│   ├── README.md
│   └── system-architecture.md     # Ranh giới thành phần, luồng xử lý và trạng thái hiện tại
│
├── database/                      # Thiết kế cơ sở dữ liệu và lưu trữ
│   ├── README.md
│   └── database-design.md         # Mô hình dữ liệu quan hệ, quy ước và trạng thái hiện tại
│
├── api/                           # Đặc tả giao tiếp RESTful API
│   ├── README.md                  # Quy ước API, chuẩn phản hồi & danh mục endpoint
│   └── api-contract.md            # Chi tiết hợp đồng các REST endpoints (MVP)
│
├── decisions/                     # Hồ sơ quyết định kiến trúc (ADR)
│   ├── README.md
│   ├── ADR-0001-baseline-tech-stack.md
│   ├── ADR-0002-database-migration-tool.md
│   └── ADR-TEMPLATE.md            # Mẫu chuẩn bị và ghi nhận quyết định kiến trúc
│
├── planning/                      # Kế hoạch phát triển và phân công công việc
│   ├── README.md
│   └── week-2-5-backlog.md        # Backlog chi tiết Tuần 2–5 cho 6 thành viên
│
├── development/                   # Hướng dẫn phát triển, môi trường và Docker
│   ├── README.md                  # Thiết lập máy trạm, quy trình build, test & run
│   ├── git-workflow.md            # Quy trình làm việc với Git, branch naming, PR checklist
│   └── coding-conventions.md      # Quy chuẩn viết mã Java/Spring Boot & TypeScript/React
│
└── devlogs/                       # Nhật ký phát triển theo từng tác vụ (Task Logs)
    ├── README.md                  # Chính sách ghi nhận DevLog độc lập
    ├── DEVLOG-TEMPLATE.md         # Mẫu chuẩn ghi nhật ký tác vụ
    └── 2026-10-09-init-ai-rules-and-docs.md # Nhật ký thiết lập hệ thống tài liệu
```

---

## 2. Phân loại tài liệu: Tài liệu chuẩn vs. Quyết định vs. DevLog

Để duy trì tính nhất quán và dễ bảo trì, hệ thống tài liệu phân định rõ 3 tầng thông tin:

| Tầng tài liệu | Vị trí thư mục | Mục đích & Bản chất | Nguyên tắc cập nhật |
|---|---|---|---|
| **Tài liệu chuẩn (Current-State Docs)** | `docs/requirements/`<br/>`docs/architecture/`<br/>`docs/database/`<br/>`docs/api/`<br/>`docs/development/` | **Mô tả trạng thái hiện tại, chuẩn xác của hệ thống.** Bất kỳ ai muốn biết hệ thống *hiện tại có gì* đều đọc tại đây. | Phải luôn được cập nhật đồng bộ ngay khi có thay đổi thực tế trong mã nguồn/cấu hình. |
| **Quyết định kiến trúc (ADR)** | `docs/decisions/` | **Lưu giữ bối cảnh, lý do và sự đánh đổi của các quyết định lớn.** | Là hồ sơ bất biến (immutable record). Khi quyết định bị thay thế, chỉ cập nhật trạng thái (`Superseded`) và trích dẫn ADR mới. |
| **Nhật ký phát triển (DevLogs)** | `docs/devlogs/` | **Hồ sơ lịch sử ghi nhận chi tiết từng phiên làm việc / tác vụ độc lập.** | Mỗi tác vụ tạo một file riêng biệt (`YYYY-MM-DD-...md`). Tuyệt đối không ghi đè hoặc chỉnh sửa nhật ký cũ. |

---

## 3. Quy ước trạng thái TBD (To Be Decided / Chưa quyết định)

Hệ thống tuân thủ nghiêm ngặt **nguyên tắc chống bịa đặt (Anti-Hallucination Policy)**:
* Những gì **đã được thiết lập** (ví dụ: khung ứng dụng React+TS, Spring Boot 3.3.4 + Java 17, Dockerfile multi-stage, PostgreSQL qua biến môi trường) được ghi nhận chính xác theo mã nguồn.
* Những gì **chưa được phân tích hoặc chưa được thống nhất** (ví dụ: các bảng cụ thể trong CSDL, danh sách endpoint chi tiết, thuật toán bóc tách kỹ năng, mô hình gợi ý) đều được gắn nhãn rõ ràng: **`TBD` (To Be Decided)** hoặc **`Chưa xác định`**.
* Không tự ý sáng tác các bảng CSDL hoặc API giả mạo khi chưa có quyết định kỹ thuật từ đội ngũ phát triển.

---

## 4. Danh mục tài liệu tham chiếu nhanh (Quick Links)

* **Quy tắc làm việc cho AI:** [`AI_RULES.md`](../AI_RULES.md)
* **Tổng quan dự án:** [`docs/project-overview.md`](./project-overview.md)
* **Khảo sát & Yêu cầu:** [`docs/requirements/current-state.md`](./requirements/current-state.md)
* **Actor & Use Case:** [`docs/requirements/actors-and-use-cases.md`](./requirements/actors-and-use-cases.md)
* **Kiến trúc hệ thống:** [`docs/architecture/system-architecture.md`](./architecture/system-architecture.md)
* **Cơ sở dữ liệu:** [`docs/database/database-design.md`](./database/database-design.md)
* **Hợp đồng REST API (MVP):** [`docs/api/api-contract.md`](./api/api-contract.md)
* **Kế hoạch phát triển Tuần 2–5:** [`docs/planning/week-2-5-backlog.md`](./planning/week-2-5-backlog.md)
* **Quy trình Git & PR Checklist:** [`docs/development/git-workflow.md`](./development/git-workflow.md)
* **Quy chuẩn lập trình:** [`docs/development/coding-conventions.md`](./development/coding-conventions.md)
* **Hướng dẫn phát triển & Docker:** [`docs/development/README.md`](./development/README.md)
* **Mẫu DevLog:** [`docs/devlogs/DEVLOG-TEMPLATE.md`](./devlogs/DEVLOG-TEMPLATE.md)
* **Mẫu ADR:** [`docs/decisions/ADR-TEMPLATE.md`](./decisions/ADR-TEMPLATE.md)
