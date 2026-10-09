# DevLog: [Tiêu đề tác vụ ngắn gọn]

* **Mã tác vụ / Task ID:** [Ví dụ: TASK-01, Issue #12, hoặc slug ngắn gọn]
* **Người thực hiện / Agent:** [Tên người thực hiện hoặc AI Agent]
* **Ngày thực hiện (Date):** [YYYY-MM-DD]
* **Nhánh Git (Branch):** [main hoặc tên feature-branch]

---

## 1. Mục tiêu (Objective)

[Mô tả ngắn gọn lý do tại sao tác vụ này được thực hiện, bài toán hoặc yêu cầu cần giải quyết.]

---

## 2. Thay đổi thực tế (Changes)

[Liệt kê chi tiết và chính xác những gì đã thực sự được thực hiện trong phiên làm việc này. Không ghi nhận các kế hoạch tương lai tại mục này.]
* **Mã nguồn (Source Code):**
  * Đã thêm / sửa ...
* **Cấu hình & Môi trường (Config & Environment):**
  * Đã cập nhật ...
* **Tài liệu chuẩn (Documentation Updates):**
  * Đã cập nhật file `docs/...` để phản ánh trạng thái mới của hệ thống.

---

## 3. Khu vực bị ảnh hưởng (Affected Areas)

[Danh sách các đường dẫn file và thư mục cụ thể đã bị thay đổi hoặc bổ sung.]
* `path/to/source/file.ext`
* `docs/path/to/doc.md`

---

## 4. Quyết định kỹ thuật quan trọng (Decisions & Rationale)

[Mô tả các lựa chọn kỹ thuật đã đưa ra và lý do chọn. Nếu có quyết định kiến trúc lớn, hãy trích dẫn file ADR tương ứng trong `docs/decisions/`.]
* **Quyết định 1:** [Chi tiết lựa chọn và căn cứ]
* **Quyết định 2:** [Chi tiết lựa chọn và căn cứ]

---

## 5. Kiểm tra & Xác thực (Validation Performed)

> [!IMPORTANT]
> **Chỉ ghi nhận những kiểm thử hoặc lệnh kiểm tra ĐÃ THỰC SỰ ĐƯỢC CHẠY thành công.**  
> Tuyệt đối không khai báo vượt hoặc giả mạo kết quả kiểm thử.

| Lệnh / Thao tác kiểm tra | Mục đích kiểm tra | Kết quả thực tế | Ghi chú |
|---|---|---|---|
| `[Ví dụ: npm run build]` | [Kiểm tra compile frontend] | [Thành công, 0 lỗi] | [Ghi chú nếu có] |
| `[Ví dụ: mvn test]` | [Chạy unit test backend] | [Tests run: X, Failures: 0] | [Ghi chú nếu có] |

---

## 6. Công việc tồn đọng & Bước tiếp theo (Outstanding Work)

[Ghi nhận trung thực các giới hạn kỹ thuật hiện tại, câu hỏi còn bỏ ngỏ hoặc các bước cần làm tiếp theo.]
* [ ] [Việc tồn đọng 1]
* [ ] [Việc tồn đọng 2]
