# DevLog: Hoàn thiện quản lý kỹ năng ứng viên W3-T2

- **Task ID:** W3-T2 / W3-BE-SKL-01
- **Ngày cập nhật:** 2026-11-26
- **Nhánh:** `feature/w3-t2-profile-skill-management`

## Mục tiêu

Hoàn thiện bốn API `/api/v1/profile/skills`, bảo vệ ownership theo JWT principal, chuẩn hóa lỗi duplicate/not-found/validation và bổ sung kiểm thử thực thi trên PostgreSQL.

## Thay đổi

- `ProfileService` xác thực `skillId`, `proficiencyLevel`, `yearsExperience` và `userId` trước khi truy cập dữ liệu.
- Duplicate chỉ được ánh xạ thành `409 SKILL_ALREADY_EXISTS` khi Hibernate báo constraint `uq_user_skills_user_skill` (không phân biệt hoa thường); integrity error khác được ném lại.
- Ownership vẫn được truy vấn theo `(userId, skillId)`; client không thể truyền owner ID.
- `yearsExperience` chấp nhận từ `0` đến `999.9`, tối đa một chữ số thập phân sau chuẩn hóa số (`1.50` được xử lý như `1.5`).
- HTTP 400 của skill business validation dùng `INVALID_SKILL_REQUEST`; JSON sai cú pháp hoặc field không hỗ trợ vẫn dùng lỗi `Bad Request` chung.
- `ProfilePostgresIT` được mở rộng tại chỗ, không đổi tên suite.

## File ảnh hưởng

- `backend/src/main/java/com/cnpm/careerrecommendation/module/profile/controller/ProfileController.java`
- `backend/src/main/java/com/cnpm/careerrecommendation/module/profile/dto/AddSkillRequest.java`
- `backend/src/main/java/com/cnpm/careerrecommendation/module/profile/dto/UpdateSkillRequest.java`
- `backend/src/main/java/com/cnpm/careerrecommendation/module/profile/service/ProfileService.java`
- `backend/src/test/java/com/cnpm/careerrecommendation/module/profile/ProfileSkillServiceTest.java`
- `backend/src/test/java/com/cnpm/careerrecommendation/module/profile/ProfilePostgresIT.java`
- `docs/api/profile.md`

## Kết quả xác minh

| Lệnh | Kết quả |
|---|---|
| `mvn -B -ntp -f backend/pom.xml -Dtest=ProfileSkillServiceTest test` | 25/25 pass |
| `mvn -B -ntp -f backend/pom.xml clean test` | 138/138 pass |
| `mvn -B -ntp -f backend/pom.xml -Ppostgres-it "-Dit.test=ProfilePostgresIT" test-compile failsafe:integration-test failsafe:verify` | 17/17 pass trên PostgreSQL 16 |
| `mvn -B -ntp -f backend/pom.xml -Ppostgres-it test-compile failsafe:integration-test failsafe:verify` | 43/43 pass (Auth 21, LMI 5, Profile 17) |
| `mvn -B -ntp -f backend/pom.xml -DskipTests package` | Build Spring Boot JAR thành công |
| `git diff --check` | Không có lỗi whitespace |

CI được ghi nhận trực tiếp trên PR sau khi push.
