# ADR-0005: Hợp nhất Thực thể User và Quy tắc Giao tiếp giữa các Phân hệ

* **Trạng thái (Status):** Proposed (Đề xuất áp dụng cho Tuần 2)
* **Tác giả (Author):** Bùi Vạn Khải (Tech Lead)
* **Ngày quyết định (Date):** 2026-11-02
* **Tài liệu liên quan (Related Documents):**
  - [Thiết kế Cơ sở dữ liệu (Bảng users)](../database/database-design.md#31-bảng-users-tài-khoản-và-xác-thực)
  - [Báo cáo thẩm định kỹ thuật Tuần 1](../audit/week1-audit.md)
  - [Hồ sơ quyết định ADR-0003](../decisions/ADR-0003-auth-contract-alignment.md)
  - Mã yêu cầu: `FR-01`, `FR-05`, `FR-07`, `UC-01`, `UC-06`, `UC-08` | Task liên quan: `W2-T1`, `W2-T2`, `W2-T3`

---

## 1. Bối cảnh và Vấn đề (Context & Problem Statement)

Sau Tuần 1, trong kho mã nguồn tồn tại song song hai lớp thực thể cùng ánh xạ vào bảng CSDL `users`:
1. `com.cnpm.careerrecommendation.entity.User` (W1-T4): Được các thực thể `CandidateProfile`, `UserSkill`, `RecommendationLog` tham chiếu khóa ngoại. Tuy nhiên, thực thể này thiếu cột `phone` được thêm từ migration `V2`, sử dụng kiểu dữ liệu `OffsetDateTime` và lưu `role` dưới dạng chuỗi `String`.
2. `com.cnpm.careerrecommendation.module.auth.entity.User` (W1-T5): Được `AuthService` và `UserRepository` của phân hệ xác thực sử dụng. Thực thể này có cột `phone`, sử dụng kiểu `Instant` và lưu `role` bằng Enum `Role`.

Hiện tượng này gây ra các vấn đề nghiêm trọng:
* Hibernate phát hiện 2 thực thể cùng map một bảng, có thể dẫn đến lỗi khởi tạo `EntityManagerFactory` hoặc ghi đè metadata ngẫu nhiên khi bật `ddl-auto: validate`.
* Phân hệ Profile (Task W2-T2 của Phạm Ngọc Gia Khang) không biết phải liên kết khóa ngoại với thực thể `User` nào.

---

## 2. Các phương án đã xem xét (Options Considered)

### Phương án 1: Giữ `User` độc quyền bên trong `module/auth/` và buộc các module khác gọi API/Service nội bộ
* **Mô tả:** Phân hệ `profile` không ánh xạ `@OneToOne` hay `@ManyToOne` với `User` mà chỉ lưu trường thô `Long userId`.
* **Ưu điểm:** Tách biệt module rất cao (Decoupling).
* **Nhược điểm:** Phải viết lại cấu trúc quan hệ JPA của toàn bộ 10 thực thể đã tạo ở W1-T4, Hibernate không thể thực hiện các phép join tự động khi cần lấy thông tin cơ bản; tăng độ trễ và khối lượng công việc cho Tuần 2.

### Phương án 2: Đưa thực thể `User` thành Shared Entity chuẩn tại `common/entity/User.java` (Được chọn)
* **Mô tả:** Hợp nhất `User` thành một lớp thực thể chia sẻ duy nhất trong `com.cnpm.careerrecommendation.common.entity.User`, chứa đầy đủ các trường của `V1` và `V2` (gồm `phone`, `role` enum, `Instant` / `OffsetDateTime` chuẩn), xóa bỏ file `User.java` cũ trong package phẳng `entity/`.
* **Ưu điểm:** Khắc phục ngay lập tức lỗi ánh xạ bảng CSDL; tương thích hoàn toàn với cả phân hệ Auth và các quan hệ khóa ngoại của `CandidateProfile`, `UserSkill`, `RecommendationLog`.

---

## 3. Quyết định và Căn cứ lựa chọn (Decision & Rationale)

Nhóm quyết định lựa chọn **Phương án 2**:
1. **Vị trí và Cấu trúc Thực thể Chuẩn:**
   - Thực thể `User` duy nhất của toàn hệ thống được đặt tại:
     `com.cnpm.careerrecommendation.common.entity.User`
   - Chứa đầy đủ các trường: `id`, `email` (unique lower index), `passwordHash` (có `@JsonIgnore`), `fullName`, `phone`, `role` (Enum `Role`), `isActive`, `createdAt`, `updatedAt`.
2. **Quy tắc giao tiếp định danh giữa các Module (Authentication Principal):**
   - Các Controller nghiệp vụ (như `ProfileController`, `RecommendationController`) **tuyệt đối không** nhận `userId` từ Request Body hoặc Path Variable để xác định chủ sở hữu dữ liệu (Candidate Ownership Guarantee).
   - Mọi Controller phải trích xuất danh tính trực tiếp từ Spring Security Context thông qua annotation:
     ```java
     @GetMapping("/api/v1/profile")
     public ProfileResponse getProfile(@AuthenticationPrincipal AuthenticatedUser user) {
         return profileService.getProfileByUserId(user.userId());
     }
     ```
   - Lớp `AuthenticatedUser` nằm trong `common.security` đóng vai trò là DTO định danh an toàn (không chứa mật khẩu/hash).
3. **Đưa Thực thể `Skill` và `SkillCategory` vào `common.entity.*`:**
   - Cả phân hệ Profile (T2 - bảng `user_skills`) và phân hệ Taxonomy (T3 - bảng `occupation_skills`) đều có khóa ngoại trỏ tới `skills(id)`.
   - Để triệt tiêu phụ thuộc chéo và tránh việc T2 phải import từ `module.taxonomy`, thực thể `Skill` và `SkillCategory` cùng các Spring Data Repositories tương ứng được chuyển vào `com.cnpm.careerrecommendation.common.entity.*` và `com.cnpm.careerrecommendation.common.repository.*` (do PR T1c thiết lập).
   - T2 và T3 hoàn toàn không import chéo package của nhau; các bài test của T2 tự chèn dữ liệu mock fixture riêng, không phụ thuộc vào Seed Data V3 của T3.

---

## 4. Hệ quả và Đánh đổi (Consequences & Trade-offs)

### Tác động tích cực:
* Giải quyết dứt điểm xung đột thực thể Hibernate khi khởi chạy với `ddl-auto: validate`.
* Đảm bảo tính nhất quán dữ liệu giữa đăng ký tài khoản (Auth) và quản lý hồ sơ (Profile).
* Ngăn chặn hoàn toàn lỗ hổng IDOR (Insecure Direct Object References) nhờ cơ chế trích xuất `userId` từ token chính chủ.

### Đánh đổi & Kế hoạch giảm thiểu:
* Cần cập nhật lại import trong `CandidateProfile.java`, `UserSkill.java`, `RecommendationLog.java`.
* Việc này được giao cho Bùi Vạn Khải xử lý trong đợt dọn dẹp W2-T1.
