# ADR-0002: Lựa chọn Flyway làm công cụ quản lý di cư cơ sở dữ liệu (Database Migration Tool)

* **Trạng thái (Status):** Accepted
* **Người đề xuất / Tác giả:** Project Architecture Team
* **Ngày quyết định (Date):** 2026-10-22
* **Tài liệu liên quan (Related Documents):** [`ADDITIONAL_DOC/Báo cáo đồ án.docx`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/requirements/current-state.md), [`docs/database/database-design.md`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/database/database-design.md), [`backend/pom.xml`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/backend/pom.xml)

---

## 1. Bối cảnh và Vấn đề (Context & Problem Statement)

Trong dự án phát triển phần mềm gồm 6 thành viên làm việc song song trên 5 tuần:
* Việc sử dụng chế độ tự động của Hibernate (`spring.jpa.hibernate.ddl-auto: update`) tiềm ẩn rủi ro rất lớn: gây xung đột cấu trúc cột giữa các máy trạm cục bộ, không thể kiểm soát lịch sử phiên bản CSDL, và dễ phát sinh lỗi không nhất quán dữ liệu khi chạy trên Docker.
* Nhóm cần một công cụ quản lý phiên bản mã nguồn của cơ sở dữ liệu (Database Migration Tool) đáng tin cậy, dễ cấu hình và tương thích hoàn hảo với Spring Boot 3.3.4 và PostgreSQL 16.

---

## 2. Các phương án đã xem xét (Options Considered)

### Phương án 1: Flyway (Khuyến nghị & Được xác định trong đặc tả đồ án)
* **Mô tả:** Flyway quản lý migration bằng các file SQL thuần túy (`V1__initial_schema.sql`, `V2__...sql`) đặt tại `src/main/resources/db/migration/`.
* **Ưu điểm:**
  * Được chỉ định chính thức trong tài liệu đồ án của nhóm ([`ADDITIONAL_DOC`, Mục 2.3, Hình 2.11]).
  * Sử dụng cú pháp SQL PostgreSQL tự nhiên, dễ đọc, dễ viết, không cần học cú pháp XML/YAML phức tạp.
  * Tích hợp mượt mà và tự động chạy khi Spring Boot khởi động.
  * Tương thích hoàn hảo với các kiểu dữ liệu nâng cao của PostgreSQL (như `pgvector`, `JSONB`, `NUMERIC`).
* **Nhược điểm:** Rollback ở phiên bản Community đòi hỏi viết script hoàn tác thủ công.

### Phương án 2: Liquibase
* **Mô tả:** Quản lý thay đổi qua changelog XML, YAML hoặc JSON.
* **Ưu điểm:** Hỗ trợ rollback tự động tốt hơn và độc lập hệ quản trị CSDL.
* **Nhược điểm:** Độ dốc học tập cao hơn, cú pháp XML/YAML dài dòng và không tận dụng trực quan các câu lệnh SQL tối ưu của PostgreSQL.

---

## 3. Quyết định và Căn cứ lựa chọn (Decision & Rationale)

Quyết định lựa chọn **Phương án 1 (Flyway)**:
1. **Tuân thủ đặc tả dự án:** Báo cáo đồ án chính thức xác định rõ Flyway là công cụ quản lý phiên bản schema theo chuỗi thời gian tuần tự (V1, V2, ...).
2. **Đơn giản & Trực quan:** Đối với thời hạn 5 tuần của 6 thành viên, việc viết file SQL thuần túy giúp toàn bộ nhóm dễ dàng theo dõi và debug trực tiếp trên PostgreSQL.
3. **Chuyển đổi chế độ Hibernate an toàn:** Cấu hình `spring.jpa.hibernate.ddl-auto: validate` để Hibernate chỉ kiểm tra tính tương thích giữa Entity Java và schema CSDL, nhường toàn quyền tạo và sửa bảng cho Flyway.

---

## 4. Hệ quả & Kế hoạch triển khai (Consequences & Implementation)

* **Cấu hình Maven:** Thêm `flyway-core` và `flyway-database-postgresql` vào `backend/pom.xml`.
* **Cấu hình Spring Boot:** Cập nhật `backend/src/main/resources/application.yml` với `ddl-auto: validate` và kích hoạt `spring.flyway`.
* **Script khởi tạo:** Tạo file `V1__initial_schema.sql` chứa toàn bộ bảng của phạm vi MVP.
