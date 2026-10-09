# ADR-0001: Lựa chọn ngăn xếp công nghệ nền tảng (Baseline Tech Stack Selection)

* **Trạng thái (Status):** Accepted
* **Người đề xuất / Tác giả:** Project Team
* **Ngày quyết định (Date):** 2026-10-07
* **Tài liệu liên quan (Related Documents):** [`README.md`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/README.md), [`docs/project-overview.md`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/project-overview.md)

---

## 1. Bối cảnh và Vấn đề (Context & Problem Statement)

Dự án cần một nền tảng công nghệ đáng tin cậy, mạnh mẽ, có tính sẵn sàng sản xuất cao (production-ready) để xây dựng hệ thống gợi ý nghề nghiệp dựa trên kỹ năng và nhu cầu thị trường. Hệ thống đòi hỏi:
* Giao diện người dùng có tính tương tác cao, an toàn kiểu dữ liệu (type-safe).
* Tầng máy chủ xử lý dữ liệu và REST API ổn định, hiệu năng cao, dễ mở rộng.
* Cơ sở dữ liệu quan hệ mạnh mẽ, hỗ trợ tốt cho dữ liệu có cấu trúc và mở rộng bán cấu trúc (JSONB/vector).
* Môi trường phát triển và triển khai đồng nhất, dễ dàng tự động hóa.

---

## 2. Các phương án đã xem xét (Options Considered)

### Phương án 1: React + TypeScript + Spring Boot (Java 17) + PostgreSQL + Docker
* **Frontend:** React + TypeScript (build bằng Vite) cho giao diện nhanh, linh hoạt, cộng đồng lớn.
* **Backend:** Spring Boot 3.3.4 (Java 17, Maven) đảm bảo tiêu chuẩn doanh nghiệp, kiến trúc phân tầng rõ ràng, bảo mật vững chắc với Spring Security.
* **Database:** PostgreSQL 16 hỗ trợ quan hệ chuẩn ACID, hiệu năng cao, hỗ trợ mở rộng dữ liệu ngữ nghĩa.
* **DevOps:** Docker và Docker Compose cho phép đóng gói đa tầng (multi-stage) và chạy nhất quán trên mọi môi trường.

### Phương án 2: Next.js Fullstack (TypeScript + Node.js) + MongoDB + Docker
* **Ưu điểm:** Một ngôn ngữ duy nhất xuyên suốt client và server.
* **Nhược điểm:** Xử lý các bài toán nghiệp vụ phức tạp, tính toán trọng số dữ liệu thị trường và tích hợp bảo mật doanh nghiệp phân quyền sâu khó quản lý bằng Spring Boot; MongoDB thiếu các ràng buộc toàn vẹn quan hệ chặt chẽ cần thiết cho mô hình phân loại nghề nghiệp (Taxonomy).

---

## 3. Quyết định và Căn cứ lựa chọn (Decision & Rationale)

Quyết định lựa chọn **Phương án 1 (React + TypeScript + Spring Boot + PostgreSQL + Docker)**:
1. Phù hợp hoàn hảo với yêu cầu kỹ thuật đã xác lập của dự án môn học và tiêu chuẩn công nghiệp.
2. Tách biệt rõ ràng ranh giới giữa Frontend (trải nghiệm người dùng) và Backend (xử lý nghiệp vụ, bảo mật, dữ liệu lớn).
3. PostgreSQL cung cấp nền tảng vững chắc cho cả dữ liệu quan hệ (người dùng, quyền) và khả năng mở rộng vector embeddings trong tương lai.
4. Docker Compose đảm bảo môi trường đồng nhất cho toàn bộ nhóm phát triển mà không phụ thuộc vào cấu hình máy cục bộ.

---

## 4. Hệ quả và Đánh đổi (Consequences & Trade-offs)

### Tác động tích cực:
* Độ tin cậy và tính ổn định cao.
* Khả năng mở rộng tốt khi tích hợp các thuật toán Machine Learning hoặc xử lý ngầm (batch jobs).
* Quản lý biến môi trường an toàn và nhất quán qua `.env.example`.

### Đánh đổi:
* Đòi hỏi thành viên phát triển nắm vững cả hệ sinh thái Java/Spring Boot lẫn React/Node.js.
* Tài nguyên tiêu thụ ban đầu lớn hơn so với một monolithic NodeJS app siêu nhẹ.

---

## 5. Xác nhận & Phê duyệt (Approval)

* **Trạng thái:** Đã được phê duyệt và thiết lập thành công trong khung mã nguồn ban đầu của dự án.
