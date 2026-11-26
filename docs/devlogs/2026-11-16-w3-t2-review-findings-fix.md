# DevLog: Giải quyết Request Changes PR #11

- **Task ID:** W3-T2 review findings
- **Ngày cập nhật:** 2026-11-26
- **Nhánh:** `feature/w3-t2-profile-skill-management`

## Finding đã xử lý

- Bổ sung `ProfileSkillServiceTest` với 25 ca cho CRUD, ownership, validation, null authentication và mapping constraint.
- Mở rộng `ProfilePostgresIT` lên 17 ca HTTP/PostgreSQL; xác minh 400/404/409, error code và database state.
- Chỉ constraint `uq_user_skills_user_skill` được ánh xạ thành `SKILL_ALREADY_EXISTS`; constraint khác hoặc không có tên được ném lại.
- Đồng bộ quy tắc `yearsExperience` giữa controller/service/database và tài liệu; `1.50` được chấp nhận như `1.5`, `1000` và `1.55` bị từ chối.
- Chuẩn hóa lỗi nghiệp vụ skill thành `INVALID_SKILL_REQUEST` và bổ sung mô tả `ErrorResponse` trong `docs/api/profile.md`.
- Chặn `EntityManager.find(User.class, null)` bằng guard xác thực trong `lockOwner`.

## Quyết định kỹ thuật

- Dùng `ConstraintViolationException#getConstraintName()` thay vì phân tích chuỗi SQL.
- Giữ nguyên transaction, pessimistic lock, schema và truy vấn ownership `(userId, skillId)`.
- Không thêm dependency, abstraction hoặc suite integration mới; mở rộng đúng test hiện có.

## Bằng chứng kiểm thử

| Suite | Tổng | Pass | Fail | Skip |
|---|---:|---:|---:|---:|
| `ProfileSkillServiceTest` | 25 | 25 | 0 | 0 |
| Backend unit regression | 138 | 138 | 0 | 0 |
| `ProfilePostgresIT` | 17 | 17 | 0 | 0 |
| PostgreSQL integration regression | 43 | 43 | 0 | 0 |

Không còn finding blocking nào trong phạm vi Request Changes sau khi các bước package, diff và CI hoàn tất.
