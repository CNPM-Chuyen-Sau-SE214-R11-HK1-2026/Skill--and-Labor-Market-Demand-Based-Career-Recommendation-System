# ADR-0007: Quy ước Đánh số Migration Flyway và Chiến lược Nạp Dữ liệu Mẫu (Seed Data)

* **Trạng thái (Status):** Proposed (Đề xuất áp dụng cho Tuần 2)
* **Tác giả (Author):** Huỳnh Long Bảo Khanh (Backend Dev) & Phan Nguyễn Thông (DevOps)
* **Ngày quyết định (Date):** 2026-11-02
* **Tài liệu liên quan (Related Documents):**
  - [Hồ sơ quyết định ADR-0002 (Flyway)](../decisions/ADR-0002-database-migration-tool.md)
  - [Thiết kế Cơ sở dữ liệu](../database/database-design.md)
  - [Báo cáo thẩm định kỹ thuật Tuần 1](../audit/week1-audit.md)
  - Mã yêu cầu: `FR-11`, `FR-12`, `FR-13`, `FR-21`, `UC-12`, `UC-13`, `UC-14`, `UC-26` | Task liên quan: `W2-T3`, `W2-T6`

---

## 1. Bối cảnh và Vấn đề (Context & Problem Statement)

1. **Thực trạng CSDL sau Tuần 1:** CSDL PostgreSQL đã có lược đồ 10 bảng sạch (`V1__initial_schema.sql`) và bản cập nhật tài khoản (`V2__auth_phone_and_email_uniqueness.sql`). Tuy nhiên, trong CSDL hoàn toàn **không có bất kỳ dữ liệu mẫu nào (0 bản ghi)**. Bảng kỹ năng, nhóm nghề nghiệp O*NET và chỉ số thị trường LMI đều rỗng, khiến việc phát triển giao diện tra cứu (Frontend Discovery) và thử nghiệm thuật toán đối sánh là bất khả thi.
2. **Rủi ro xung đột đánh số phiên bản Flyway:** Flyway quản lý lịch sử migration theo số thứ tự nghiêm ngặt (`V1`, `V2`, `V3`...). Khi nhiều thành viên cùng làm việc song song, nếu không phân định quyền tạo migration, nguy cơ hai người cùng tạo `V3__...` là rất lớn, dẫn đến lỗi xung đột checksum và phá vỡ cơ sở dữ liệu trên môi trường của nhau.

---

## 2. Các phương án đã xem xét (Options Considered)

### Phương án 1: Nạp dữ liệu mẫu bằng `CommandLineRunner` / `ApplicationRunner` trong Spring Boot
* **Mô tả:** Viết một Bean Java tự động kiểm tra nếu bảng rỗng thì chèn dữ liệu bằng Spring Data JPA.
* **Nhược điểm:** Phụ thuộc vào vòng đời khởi động của ứng dụng; làm chậm quá trình chạy test context; khó kiểm soát quan hệ ID và các ràng buộc khóa ngoại phức tạp giữa các bảng.

### Phương án 2: Quản lý Seed Data trực tiếp bằng SQL Migration qua Flyway (Được chọn)
* **Mô tả:** Sử dụng migration script SQL tuần tự `V3__seed_taxonomy_and_occupations.sql` chứa các lệnh `INSERT ... ON CONFLICT DO NOTHING`.
* **Ưu điểm:** Khởi tạo dữ liệu tức thì ngay khi CSDL boot lên; bảo đảm tính toàn vẹn khóa ngoại; hoạt động đồng nhất giữa môi trường Docker local, máy lập trình viên và pipeline CI.

---

## 3. Quyết định và Căn cứ lựa chọn (Decision & Rationale)

Nhóm quyết định lựa chọn **Phương án 2**:
1. **Phân bổ độc quyền phiên bản `V3` cho Task W2-T3:**
   - Chỉ duy nhất **Huỳnh Long Bảo Khanh (W2-T3)** được phép tạo file:
     `backend/src/main/resources/db/migration/V3__seed_taxonomy_and_occupations.sql`
   - Tuyệt đối cấm các thành viên khác tự ý tạo thêm file migration trong Tuần 2. Mọi thay đổi cấu trúc bảng phát sinh phải được thảo luận với Tech Lead để cấp số thứ tự tuần tự (`V4`, `V5`...) cho các sprint sau.
2. **Nội dung và Phạm vi của Seed Data `V3`:**
   - **Nhóm nghề nghiệp (`occupation_groups`):** Ít nhất 3 nhóm lớn ngành Công nghệ Thông tin (Software Development, Data & AI, Cloud & Infrastructure).
   - **Nghề nghiệp (`occupations`):** Tối thiểu 10 vị trí nghề nghiệp tiêu biểu (Backend Engineer, Frontend Engineer, Fullstack Developer, Data Scientist, Data Engineer, DevOps Engineer, Mobile App Developer, QA/Automation Tester, System Architect, UI/UX Designer).
   - **Từ điển Kỹ năng (`skill_categories` & `skills`):** Tối thiểu 3 nhóm kỹ năng (Programming Languages, Frameworks & Libraries, Cloud & DevOps) với hơn 30 kỹ năng chuẩn hóa (Java, Spring Boot, React, TypeScript, Python, PostgreSQL, Docker, Kubernetes, Git, REST API...).
   - **Ma trận Trọng số Nghề - Kỹ năng (`occupation_skills`):** Thiết lập trọng số `importance_weight` chuẩn từ 0.00 đến 1.00 cho từng nghề.
   - **Chỉ số Thị trường Mẫu (`market_demand_indicators`):** Điểm nhu cầu tuyển dụng (`demand_score` từ 70–95), tốc độ tăng trưởng và số lượng việc làm mở tham chiếu cho 10 nghề này.
3. **Nguyên tắc an toàn khi nạp dữ liệu:** Toàn bộ lệnh chèn dữ liệu phải có mệnh đề an toàn (idempotent), ví dụ: `INSERT INTO occupations (...) VALUES (...) ON CONFLICT (code) DO NOTHING;`.

---

## 4. Hệ quả và Đánh đổi (Consequences & Trade-offs)

### Tác động tích cực:
* Cung cấp ngay lập tức dữ liệu thực tế cho Frontend hiển thị mà không cần chờ Backend hoàn thành hết logic phức tạp.
* Không có xung đột phiên bản Flyway giữa 6 thành viên.
* Giữ nguyên nguyên tắc Hibernate `ddl-auto: validate` hoạt động trơn tru.

### Đánh đổi & Kế hoạch giảm thiểu:
* Dung lượng file SQL migration `V3` có thể dài hơn 100 dòng.
* **Biện pháp:** Tổ chức file SQL rõ ràng theo từng block bảng, có comment phân đoạn, sử dụng transaction duy nhất.
