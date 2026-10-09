# Tổng quan dự án (Project Overview)

**Tên dự án:** Skill- and Labor Market Demand–Based Career Recommendation System  
*(Hệ thống gợi ý nghề nghiệp dựa trên kỹ năng cá nhân và nhu cầu thị trường lao động)*

---

## 1. Giới thiệu dự án

> **Mô tả cốt lõi:**  
> Hệ thống gợi ý nghề nghiệp dựa trên kỹ năng cá nhân và nhu cầu thị trường lao động, giúp người dùng xác định nghề nghiệp phù hợp, đánh giá mức độ tương thích và nhận diện các kỹ năng cần phát triển.

Trong bối cảnh nền kinh tế số và tự động hóa chuyển dịch mạnh mẽ từ tuyển dụng theo bằng cấp sang tuyển dụng dựa trên kỹ năng thực tế (*skills-based economy*), dự án hướng tới xây dựng một nền tảng thông minh giúp:
1. Người tìm việc và sinh viên định vị chính xác năng lực bản thân so với nhu cầu thực tế của thị trường.
2. Tránh bẫy định hướng vào các ngành nghề đang suy thoái (*historical echo chamber*) bằng cách kết hợp dự báo nhu cầu thị trường lao động (*Labor Market Information - LMI*).
3. Đưa ra chẩn đoán khoảng cách kỹ năng định lượng (*skill gap diagnosis*) và gợi ý lộ trình bồi dưỡng năng lực (*upskilling pathway*) có thể hành động được.

---

## 2. Các vấn đề cốt lõi cần giải quyết

Dựa trên nghiên cứu hiện trạng, hệ thống giải quyết 4 rào cản chính:

* **Mất phương hướng & Bội thực thông tin (Cognitive Fatigue):** Người tìm việc đối mặt với hàng triệu tin tuyển dụng phi cấu trúc, không biết bắt đầu từ đâu.
* **Bẫy khởi đầu lạnh bất đối xứng (Asymmetric Cold-Start):** Sinh viên hoặc người chuyển ngành thiếu dữ liệu kinh nghiệm làm việc trong quá khứ khiến các thuật toán lọc cộng tác truyền thống thất bại.
* **Bẫy gợi ý ngành nghề suy thoái:** Các thuật toán gợi ý dựa trên lịch sử thường tiếp tục khớp nối người dùng vào những công việc đang bị thu hẹp hoặc thay thế bởi AI.
* **Thiếu khả năng hành động cụ thể:** Phần lớn hệ thống chỉ trả về danh sách việc làm hoặc % điểm chung chung, không chỉ rõ thiếu kỹ năng gì và học ở đâu.

---

## 3. Ngăn xếp công nghệ đã xác lập (Known Tech Stack)

Hệ thống được xây dựng trên nền tảng công nghệ hiện đại, ổn định và sẵn sàng cho môi trường doanh nghiệp:

| Thành phần | Công nghệ đã chọn | Chi tiết cấu hình hiện tại |
|---|---|---|
| **Frontend** | React 18 + TypeScript | Khung ứng dụng xây dựng bằng Vite, hỗ trợ Single Page Application (SPA), Nginx phục vụ tĩnh trong Docker. |
| **Backend** | Spring Boot 3.3.4 (Java 17) | Quản lý bằng Maven, kiến trúc phân tầng sạch (`controller`, `service`, `repository`, `entity`, `dto`, `config`). |
| **Cơ sở dữ liệu** | PostgreSQL 16 | Cấu hình kết nối động hoàn toàn qua biến môi trường, không lưu trữ thông tin nhạy cảm cứng. |
| **Giao thức API** | RESTful API | Định dạng JSON tiêu chuẩn. |
| **Bảo mật & Xác thực** | Spring Security + JWT | Tích hợp sẵn bộ thư viện `jjwt` (0.12.6) và starter security (chưa triển khai logic phân quyền). |
| **Đóng gói & Triển khai** | Docker & Docker Compose | Multi-stage Dockerfile cho Frontend và Backend; file `docker-compose.yml` điều phối toàn bộ dịch vụ kèm kiểm tra sức khỏe (*healthcheck*). |

---

## 4. Ranh giới phạm vi (Scope Boundaries)

### Trong phạm vi (In Scope):
* Thu nạp và bóc tách hồ sơ kỹ năng người dùng từ văn bản/CV.
* Chuẩn hóa kỹ năng và nghề nghiệp theo các danh mục chuẩn quốc tế (O\*NET / ESCO).
* Tính toán độ tương hợp ($Sim$) kết hợp trọng số nhu cầu thị trường ($Demand$).
* Chẩn đoán khoảng cách kỹ năng ($\Delta$) và đề xuất lộ trình bồi dưỡng kỹ năng.
* Cung cấp giao diện web thân thiện, bảo mật và trực quan.

### Ngoài phạm vi / Chưa triển khai ở giai đoạn này (Out of Scope / TBD):
* Hệ thống tuyển dụng trực tiếp hoặc sàn giao dịch việc làm trung gian.
* Tự động nộp hồ sơ xin việc thay cho ứng viên.
* Triển khai chi tiết các thuật toán Machine Learning / LLM chuyên sâu (sẽ thực hiện ở các sprint tiếp theo sau khi chốt thiết kế kiến trúc và CSDL).

---

## 5. Trạng thái hiện tại của dự án

* **Khung mã nguồn cơ sở (Baseline Skeleton):** Đã thiết lập hoàn chỉnh và kiểm thử thành công (Frontend Vite build sạch, Backend Maven compile và test thành công, Docker compose hợp lệ).
* **Hệ thống quy chuẩn tài liệu & AI Rules:** Đã thiết lập đầy đủ tại [`AI_RULES.md`](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/AI_RULES.md) và thư mục `docs/`.
* **Nghiệp vụ chi tiết:** Chưa triển khai (No business logic implemented yet).
