# Nhật ký phát triển (Task DevLogs)

Thư mục này lưu trữ các bản ghi nhật ký phát triển (DevLogs) độc lập cho từng tác vụ được thực hiện trong dự án.

---

## 1. Chính sách DevLog bắt buộc (Mandatory DevLog Policy)

> [!WARNING]
> **Tuyệt đối KHÔNG ghi nối tiếp (append) vào một file `devlog.md` dùng chung.**  
> Dự án hỗ trợ nhiều người và nhiều AI agent cộng tác song song. Việc ghi chung một file duy nhất sẽ gây xung đột Git (merge conflicts). **Mỗi tác vụ bắt buộc phải tạo một file DevLog mới.**

---

## 2. Quy ước đặt tên file DevLog

Mỗi file DevLog phải tuân thủ nghiêm ngặt quy tắc đặt tên sau:

```text
docs/devlogs/YYYY-MM-DD-<task-id-or-branch-slug>-<short-description>.md
```

*Ví dụ hợp lệ:*
* `2026-10-09-init-ai-rules-and-docs.md`
* `2026-10-14-issue-23-implement-user-repository.md`
* `2026-10-18-feature-skill-extraction-endpoint.md`

### Kiểm tra trùng tên trước khi tạo:
Trước khi tạo file mới, lập trình viên / AI agent phải kiểm tra xem file có cùng tên đã tồn tại hay chưa để tránh ghi đè lên nhật ký của người khác.

---

## 3. Cấu trúc bắt buộc của một DevLog

Mỗi DevLog phải được khởi tạo từ mẫu [**`DEVLOG-TEMPLATE.md`**](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/devlogs/DEVLOG-TEMPLATE.md) và bao gồm đầy đủ 6 phần:
1. **Mục tiêu (Objective):** Lý do thực hiện tác vụ.
2. **Thay đổi thực tế (Changes):** Những gì đã thực sự được thay đổi trong mã nguồn, cấu hình, và tài liệu chuẩn.
3. **Khu vực bị ảnh hưởng (Affected Areas):** Danh sách đường dẫn file liên quan.
4. **Quyết định kỹ thuật (Decisions & Rationale):** Các lựa chọn kỹ thuật và lý do chọn.
5. **Kiểm tra & Xác thực (Validation Performed):** Chỉ ghi nhận các lệnh kiểm thử và kiểm tra đã THỰC SỰ ĐƯỢC CHẠY.
6. **Công việc tồn đọng (Outstanding Work):** Những hạn chế, câu hỏi chưa có câu trả lời, và bước đi tiếp theo.

---

## 4. Danh mục các DevLog đã ghi nhận

| Ngày | File DevLog | Tác vụ | Người thực hiện |
|---|---|---|---|
| 2026-10-09 | [**`2026-10-09-init-ai-rules-and-docs.md`**](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/devlogs/2026-10-09-init-ai-rules-and-docs.md) | Thiết lập AI Guardrails và Hệ thống tài liệu sống (Living Documentation System) | Antigravity AI Agent |
| 2026-10-22 | [**`2026-10-22-resolve-week-1-task-1.md`**](file:///d:/Khai%20Van/KhaiVan%20Data/Dai%20Hoc/Skill--and-Labor-Market-Demand-Based-Career-Recommendation-System/docs/devlogs/2026-10-22-resolve-week-1-task-1.md) | Giải quyết toàn diện các hạng mục tồn đọng Tuần 1 Tác vụ 1 (Resolve Week 1 Task 1) | Antigravity AI Assistant |

| 2026-10-29 | [2026-10-29-w1-t5-jwt-auth-viet-khang.md](2026-10-29-w1-t5-jwt-auth-viet-khang.md) | W1-T5 — JWT/Auth/RBAC | Đoàn Việt Khang / Codex |
