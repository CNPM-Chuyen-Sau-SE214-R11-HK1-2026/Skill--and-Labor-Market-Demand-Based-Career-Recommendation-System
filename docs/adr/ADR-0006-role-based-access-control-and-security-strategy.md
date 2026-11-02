# ADR-0006: Chiến lược Phân quyền RBAC bằng Method Security và Khắc phục Bảo vệ Endpoint

* **Trạng thái (Status):** Proposed (Đề xuất áp dụng cho Tuần 2)
* **Tác giả (Author):** Bùi Vạn Khải (Tech Lead)
* **Ngày quyết định (Date):** 2026-11-02
* **Tài liệu liên quan (Related Documents):**
  - [Hợp đồng REST API (Mục 2.1)](../api/api-contract.md#21-phân-hệ-xác-thực--tài-khoản-apiv1auth)
  - [Báo cáo thẩm định kỹ thuật Tuần 1](../audit/week1-audit.md)
  - [GAP-04: JWT Claims Standards](../development/coding-conventions.md#27-chuẩn-hóa-jwt-claims--nguyên-tắc-phân-quyền-jwt-claims-standards---gap-04)
  - Mã yêu cầu: `FR-01`, `FR-03`, `FR-20`, `NFR-01`, `NFR-05`, `UC-01`, `UC-03`, `UC-25` | Task liên quan: `W2-T1`, `W2-T2`, `W2-T4`

---

## 1. Bối cảnh và Vấn đề (Context & Problem Statement)

Kiểm tra an ninh mã nguồn sau Tuần 1 phát hiện 3 lỗ hổng và bất cập lớn trong cơ chế bảo mật:
1. **Lỗ hổng hở endpoint `/api/v1/auth/me`:** Tại `SecurityConfig.java`, quy tắc `.requestMatchers("/api/v1/auth/**").permitAll()` vô tình áp dụng cho cả endpoint xem thông tin tài khoản `/me`. Khi một request không có Bearer token gọi vào, Spring Security cho phép đi qua, dẫn đến tham số `identity` bị null và làm sập ứng dụng với mã lỗi 500 `NullPointerException` tại `AuthService.java` thay vì trả về 401 Unauthorized chuẩn.
2. **Thiếu cơ chế phân quyền RBAC:** `SecurityConfig.java` chỉ có `.anyRequest().authenticated()`, hoàn toàn không phân biệt quyền giữa `ROLE_CANDIDATE` và `ROLE_ADMIN`. Bất kỳ ứng viên nào có token cũng có thể gọi vào các endpoint quản trị nếu chỉ dựa vào URL matching tập trung.
3. **Nguy cơ xung đột Git cao tại file `SecurityConfig.java`:** Nếu mỗi thành viên khi viết API mới (`/api/v1/profile/**`, `/api/v1/admin/**`) đều phải vào `SecurityConfig.java` để khai báo matcher thì file này sẽ trở thành điểm nghẽn xung đột merge liên tục.

---

## 2. Các phương án đã xem xét (Options Considered)

### Phương án 1: Khai báo toàn bộ đường dẫn và quyền tập trung trong `SecurityConfig.java`
* **Mô tả:** Sử dụng `.requestMatchers(HttpMethod.GET, "/api/v1/admin/**").hasRole("ADMIN")` trong filter chain.
* **Nhược điểm:** Dẫn đến xung đột Git trầm trọng giữa các thành viên backend; cấu hình URL dễ bị bỏ sót hoặc sai lệch khi endpoint thay đổi.

### Phương án 2: Bật Spring Method Security (`@EnableMethodSecurity`) và phân quyền phân tán bằng `@PreAuthorize` (Được chọn)
* **Mô tả:** Kích hoạt `@EnableMethodSecurity(prePostEnabled = true)` trong `SecurityConfig.java`. Chỉ giữ `SecurityConfig.java` để khai báo các endpoint hoàn toàn công khai (Public Permitted Endpoints). Toàn bộ việc kiểm soát quyền theo vai trò (`ROLE_CANDIDATE`, `ROLE_ADMIN`) sẽ do lập trình viên từng module khai báo trực tiếp trên Controller/Method bằng `@PreAuthorize("hasRole('CANDIDATE')")`.
* **Ưu điểm:** Khử hoàn toàn xung đột Git tại `SecurityConfig.java`; logic phân quyền gắn liền với Controller nghiệp vụ; linh hoạt và dễ kiểm thử bằng `@WithMockUser`.

---

## 3. Quyết định và Căn cứ lựa chọn (Decision & Rationale)

Nhóm quyết định lựa chọn **Phương án 2**:
1. **Khắc phục ngay lập tức quy tắc bảo vệ URL tại `SecurityConfig.java`:**
   - Chỉ mở `permitAll()` cho các endpoint thực sự công khai:
     * `GET /api/health`
     * `POST /api/v1/auth/login`
     * `POST /api/v1/auth/register`
     * `GET /api/v1/occupations/**`
     * `GET /api/v1/skills/**`
   - Đóng toàn bộ các đường dẫn còn lại, bao gồm `GET /api/v1/auth/me` (bắt buộc phải qua `authenticated()`).
   - Xóa bỏ matcher thử nghiệm `/api/test/**` khỏi cấu hình chính.
2. **Kích hoạt Method Security:**
   ```java
   @Configuration
   @EnableWebSecurity
   @EnableMethodSecurity(prePostEnabled = true)
   public class SecurityConfig { ... }
   ```
3. **Quy chuẩn phân quyền cho các thành viên Backend Tuần 2–5:**
   - Phân hệ Candidate Profile (W2-T2): Đặt `@PreAuthorize("hasRole('CANDIDATE')")` tại `ProfileController`.
   - Phân hệ Admin LMI (Tuần 4): Đặt `@PreAuthorize("hasRole('ADMIN')")` tại `AdminController`.
4. **Chiến lược Token Expiration & Refresh:**
   - Trong giai đoạn Tuần 2, giữ nguyên thời hạn JWT Access Token là 24 giờ (`86400000 ms`), không triển khai Refresh Token để tránh tăng độ phức tạp không cần thiết cho MVP hiện tại.
   - Khi token hết hạn hoặc không hợp lệ, `JwtAuthenticationFilter` tự động xóa context và trả về lỗi 401 Unauthorized thông qua `SecurityErrorHandler`.
5. **Đồng bộ Phân quyền Phía Frontend (`ProtectedRoute`):**
   - Thành phần bảo vệ route `ProtectedRoute` (do W2-T4 xây dựng) nhận prop `roles?: string[]`.
   - Các tuyến đường quản trị như `/admin` được bảo vệ bằng cấu hình `allowedRoles={['ROLE_ADMIN']}`. Nếu tài khoản mang quyền `ROLE_CANDIDATE` truy cập, hệ thống chặn hiển thị và điều hướng an toàn về `/dashboard` hoặc trang thông báo 403 Forbidden.

---

## 4. Hệ quả và Đánh đổi (Consequences & Trade-offs)

### Tác động tích cực:
* Sửa triệt để lỗ hổng an ninh tại `/api/v1/auth/me`, đảm bảo trả đúng mã HTTP 401 khi không đăng nhập.
* Giải phóng `SecurityConfig.java` khỏi việc bị chỉnh sửa bởi nhiều người.
* Cơ chế RBAC rõ ràng, trực quan, hỗ trợ unit test controller cô lập cực kỳ thuận tiện.

### Đánh đổi & Kế hoạch giảm thiểu:
* Lập trình viên phải nhớ gắn annotation `@PreAuthorize` trên các Controller cần bảo vệ.
* **Biện pháp:** Bộ test kiểm thử tự động của từng controller bắt buộc phải có test case xác minh chặn quyền khi người dùng mang role không phù hợp (trả về 403 Forbidden).
