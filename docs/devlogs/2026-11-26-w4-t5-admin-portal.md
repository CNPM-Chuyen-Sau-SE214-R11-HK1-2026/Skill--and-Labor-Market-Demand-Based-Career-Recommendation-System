# DevLog: Admin Portal W4-T5

* **Mã tác vụ:** W4-T5
* **Người thực hiện / Agent:** AI assistant hỗ trợ Đoàn Việt Khang
* **Ngày:** 2026-11-26
* **Nhánh:** Đề xuất `feat/w4-t5-admin-portal`; thay đổi được đóng gói trên snapshot ZIP, không push.

## 1. Mục tiêu (Objective)
Triển khai các màn hình admin theo bảng phân công và bắt buộc danh mục nghề nghiệp dùng API thật.

## 2. Thay đổi thực tế (Changes)
- Thay placeholder bằng trang tổng quan và ba route LMI, Taxonomy, Users; sử dụng layout và guard chung đã có, bổ sung nav responsive.
- LMI dán/chọn JSON, kiểm tra lô, xác nhận ghi, gửi envelope đúng DTO; xử lý lỗi theo field và kiểm tra response counts.
- Users lọc/phân trang, stats, xác nhận đổi trạng thái, ngăn thao tác tài khoản hiện tại; Taxonomy list/modal, chống ghi đè từ DTO công khai thiếu trường.
- Adapter thật mặc định; demo Users/Taxonomy chỉ bật chủ động trong development. Bỏ nhánh mock trong occupationService và thông báo mock.
- Bổ sung test độc lập và cập nhật tài liệu API/feature, hướng dẫn nghiệm thu.

## 3. Khu vực bị ảnh hưởng (Affected Areas)
- `frontend/src/features/admin/`, `frontend/src/features/occupations/`, `frontend/src/layouts/AdminLayout*`, `frontend/.env.example`.
- `frontend/scripts/test-w4-t5.cjs`, `frontend/tests/w4-t5-model.test.cjs`.
- `docs/api/api-contract.md`, `docs/api/taxonomy.md`, [hướng dẫn W4-T5](../development/w4-t5-admin-portal.md).

## 4. Quyết định kỹ thuật (Decisions)
- Giữ [ADR-0008](../adr/ADR-0008-frontend-architecture-and-state-management.md), không thêm dependency hoặc chỉnh router trung tâm.
- Source chưa có W4-T2/W4-T3 controllers dù Excel đánh dấu W4-T2 hoàn thành. Không sửa code backend thuộc người khác. DTO dự kiến được ghi rõ, demo không tự fallback. Chỉnh sửa thật chỉ mở khi đọc được DTO quản trị đầy đủ.
- LMI theo DTO Java, không dùng ví dụ array lỗi thời; cho phép paste array nhưng chuẩn hóa request thành envelope.

## 5. Kiểm tra & xác thực (Validation)
| Lệnh | Kết quả thực tế |
|---|---|
| `node scripts/test-w4-t5.cjs` (Node 22, TypeScript toàn cục) | 30 test pass, 0 fail; gồm compile strict model |
| `npm ci --offline` | Không thành công: ENOTCACHED |
| `npm ci` | Không cài được dependency; DNS registry.npmjs.org không phân giải |
| `npm run build` | Exit 1: thiếu type/dependency do cài đặt chưa thành công; chưa xác minh production build |

## 6. Công việc tồn đọng (Outstanding Work)
- [ ] Chạy npm ci/build và các kịch bản A01–A14 tại môi trường thật; không ghi nhận browser/E2E pass khi chưa chạy.
- [ ] Merge W4-T2/T3 và đối chiếu DTO adapter, chạy thao tác Users/Taxonomy trên DB kiểm thử. API công khai hiện chỉ có snapshot LMI gần nhất; không giả lập lịch sử.
