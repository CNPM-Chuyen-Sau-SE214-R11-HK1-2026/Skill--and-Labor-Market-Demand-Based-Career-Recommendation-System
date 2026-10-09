# Yêu cầu hệ thống (System Requirements)

Thư mục này lưu trữ tài liệu chuẩn về hiện trạng, bài toán, yêu cầu chức năng (FR), yêu cầu phi chức năng (NFR), cùng với danh mục các Actor và Use Case của dự án.

---

## Danh mục tài liệu chuẩn

1. [**`current-state.md`**](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/requirements/current-state.md)
   * **Tên gốc:** *01-Khảo sát hiện trạng & Thu thập yêu cầu*.
   * **Nội dung:**
     * Bối cảnh và các vấn đề cần giải quyết (rào cản người học, rào cản thuật toán, thách thức dữ liệu thị trường).
     * Yêu cầu chức năng sơ bộ: **FR1** (Quản lý & Trích xuất hồ sơ kỹ năng), **FR2** (Tích hợp dữ liệu nhu cầu thị trường LMI), **FR3** (Gợi ý nghề nghiệp có trọng số nhu cầu), **FR4** (Chẩn đoán khoảng cách kỹ năng), **FR5** (Gợi ý lộ trình đào tạo bổ trợ), **FR6** (Giải thích kết quả gợi ý).
     * Yêu cầu phi chức năng sơ bộ: **NFR1** (Chuẩn hóa ngữ nghĩa), **NFR2** (Độ tươi dữ liệu), **NFR3** (Công bằng & Chống thiên lệch), **NFR4** (Hiệu năng phản hồi < 3s), **NFR5** (Bảo vệ quyền riêng tư & CV).
     * Mô hình 4 tầng dữ liệu cần thiết (User Profile, Taxonomy chuẩn hóa O\*NET/ESCO, Dữ liệu LMI, Dữ liệu khóa học).
     * So sánh các hệ thống tương tự (O\*NET OnLine, Lightcast, LinkedIn, eDoer/SkillRec) và danh mục tài liệu tham khảo khoa học.

2. [**`actors-and-use-cases.md`**](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/requirements/actors-and-use-cases.md)
   * **Tên gốc:** *02-Actor & Use Case*.
   * **Nội dung:**
     * Danh sách Actor chính: Người dùng / Người học (Job Seeker / Learner), Chuyên viên dữ liệu & Hướng nghiệp (Labor Data & Career Specialist), Quản trị viên hệ thống (System Administrator).
     * Danh sách Actor hỗ trợ/bên ngoài: Hệ thống dữ liệu thị trường & Danh mục chuẩn (External LMI & Taxonomy Service), Hệ thống tài nguyên đào tạo (External Learning Platform).
     * Danh sách Use Case:
       * **UC01**: Quản lý hồ sơ & Bóc tách kỹ năng cá nhân
       * **UC02**: Nhận gợi ý nghề nghiệp theo kỹ năng và nhu cầu thị trường
       * **UC03**: Xem chẩn đoán khoảng cách kỹ năng (Skill Gap Analysis)
       * **UC04**: Xem gợi ý lộ trình đào tạo bổ trợ (Upskilling Pathway)
       * **UC05**: Thu nạp & Chuẩn hóa dữ liệu thị trường lao động
       * **UC06**: Cấu hình trọng số gợi ý & Quản lý Taxonomy
       * **UC07**: Tra cứu thông tin & Xu hướng nghề nghiệp
       * **UC08**: Quản trị tài khoản & Giám sát hệ thống
     * Sơ đồ quan hệ Use Case tổng quát (Mermaid diagram).
