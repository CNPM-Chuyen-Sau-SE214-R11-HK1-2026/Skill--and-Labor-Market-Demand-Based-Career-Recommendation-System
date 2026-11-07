# ADR-0004: Thống nhất Định dạng ErrorResponse và Hợp nhất Xử lý Ngoại lệ Tập trung

* **Trạng thái (Status):** Proposed (Đề xuất áp dụng cho Tuần 2)
* **Tác giả (Author):** Bùi Vạn Khải (Tech Lead)
* **Ngày quyết định (Date):** 2026-11-02
* **Tài liệu liên quan (Related Documents):**
  - [Hợp đồng REST API (Mục 1.2)](../api/api-contract.md#12-cấu-trúc-phản-hồi-lỗi-chuẩn-errorresponse)
  - [Báo cáo thẩm định kỹ thuật Tuần 1](../audit/week1-audit.md)
  - [Quy chuẩn lập trình Backend](../development/coding-conventions.md#25-quản-lý-lỗi-tập-trung-centralized-exception-handling)
  - Mã yêu cầu: `FR-01`, `FR-03`, `NFR-05`, `UC-01`, `UC-03` | Task liên quan: `W2-T1`

---

## 1. Bối cảnh và Vấn đề (Context & Problem Statement)

Trong quá trình thẩm định kỹ thuật Tuần 1, hệ thống Backend xuất hiện hai bộ xử lý ngoại lệ và hai cấu trúc phản hồi lỗi chạy song song:
1. `com.cnpm.careerrecommendation.exception.GlobalExceptionHandler` và `dto.ErrorResponse` (do Task W1-T3 tạo).
2. `com.cnpm.careerrecommendation.common.exception.GlobalExceptionHandler` và `common.dto.ErrorResponse` (do Task W1-T5 tạo).

Sự phân mảnh này dẫn đến các rủi ro kỹ thuật nghiêm trọng:
* Hai `@RestControllerAdvice` cùng xử lý các ngoại lệ `MethodArgumentNotValidException`, `HttpMessageNotReadableException` gây xung đột bean và thứ tự bắt lỗi không thể đoán trước trong Spring MVC.
* Lớp `GlobalExceptionHandler` của W1-T3 phản chiếu trực tiếp giá trị bị từ chối `fieldError.getRejectedValue()` ra JSON response. Nếu người dùng nhập sai mật khẩu ở màn hình đăng ký, chuỗi mật khẩu thô sẽ bị lộ nguyên văn trong HTTP response, vi phạm nghiêm trọng chuẩn an ninh thông tin.
* Kiểu dữ liệu trường `timestamp` không đồng nhất (`String` định dạng thủ công vs `java.time.Instant` chuẩn ISO-8601).

---

## 2. Các phương án đã xem xét (Options Considered)

### Phương án 1: Giữ nguyên lớp tại `exception/` và cập nhật lại logic của W1-T5
* **Mô tả:** Giữ cấu trúc phẳng `com.cnpm.careerrecommendation.exception.GlobalExceptionHandler` và sửa code che giấu mật khẩu.
* **Nhược điểm:** Phá vỡ định hướng chuyển đổi sang cấu trúc module và thư mục chia sẻ `common/`; không tương thích với DTO lỗi đang được phân hệ Auth sử dụng.

### Phương án 2: Chuẩn hóa duy nhất bộ xử lý tại `common/exception/` và xóa bỏ package phẳng (Được chọn)
* **Mô tả:** Lấy `com.cnpm.careerrecommendation.common.exception.GlobalExceptionHandler` và `com.cnpm.careerrecommendation.common.dto.ErrorResponse` làm chuẩn duy nhất cho toàn hệ thống. Xóa bỏ hoàn toàn lớp trùng lặp tại package `exception/`.
* **Ưu điểm:** Khớp 100% với định dạng của W1-T5, che giấu an toàn dữ liệu nhạy cảm (`rejectedValue = null`), chuẩn hóa `timestamp` kiểu `Instant`, dọn sạch xung đột bean trong Spring IoC.

---

## 3. Quyết định và Căn cứ lựa chọn (Decision & Rationale)

Nhóm quyết định lựa chọn **Phương án 2**:
1. **Một nguồn sự thật duy nhất (Single Source of Truth):** Toàn bộ hệ thống Backend sử dụng bản ghi `com.cnpm.careerrecommendation.common.dto.ErrorResponse`:
   ```java
   public record ErrorResponse(
       Instant timestamp,
       int status,
       String error,
       String message,
       String path,
       List<FieldError> errors
   ) {
       public record FieldError(String field, Object rejectedValue, String reason) {}
   }
   ```
2. **Quy tắc an ninh dữ liệu:** Mọi lỗi validation trường dữ liệu liên quan đến mật khẩu hoặc thông tin định danh cá nhân bắt buộc phải thiết lập `rejectedValue = null` trước khi trả về client.
3. **Chuẩn hóa DTO phân trang dùng chung (`PageResponse`):** Tạo lớp generic `com.cnpm.careerrecommendation.common.dto.PageResponse<T>` chứa `content`, `pageNumber`, `pageSize`, `totalElements`, `totalPages`, `isLast` trong `common.dto` (do PR T1c tạo). Cả phân hệ Profile (T2) và Taxonomy (T3) đều sử dụng DTO này, tránh việc tạo DTO phân trang riêng rẽ trong từng module.
4. **Điểm mở rộng cho các lập trình viên nghiệp vụ:** Mọi ngoại lệ nghiệp vụ mới trong Tuần 2–5 (`ProfileNotFoundException`, `OccupationNotFoundException`...) bắt buộc phải kế thừa lớp cơ sở `ApiException(HttpStatus status, String errorCode, String message)`. `GlobalExceptionHandler` tập trung sẽ tự động ánh xạ thành HTTP Status tương ứng mà không yêu cầu lập trình viên phải sửa file handler dùng chung.

---

## 4. Hệ quả và Đánh đổi (Consequences & Trade-offs)

### Tác động tích cực:
* Loại bỏ triệt để hiện tượng xung đột bean `@RestControllerAdvice` trong Spring Boot.
* Khắc phục hoàn toàn rủi ro rò rỉ thông tin mật khẩu qua HTTP response.
* Các module `profile`, `taxonomy` chỉ cần kế thừa `ApiException` là tự động có phản hồi lỗi chuẩn REST.

### Đánh đổi & Kế hoạch giảm thiểu:
* **Tác động:** Phải xóa các file cũ trong `exception/` và refactor lại các import trong các bài test cũ của W1-T3 (`GlobalExceptionHandlerTest.java`).
* **Giảm thiểu:** Bùi Vạn Khải phụ trách thực hiện trọn gói việc xóa và refactor trong task `W2-T1` vào ngày đầu tiên của sprint.
