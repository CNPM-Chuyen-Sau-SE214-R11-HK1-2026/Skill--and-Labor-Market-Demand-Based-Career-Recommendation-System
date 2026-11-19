# Đặc tả Hợp đồng REST API Phân hệ Dữ liệu Thị trường Lao động LMI (`/api/v1/admin/lmi`)

* **Phiên bản:** 1.0.0 (Tuần 3 — `W3-T3`)
* **Chủ sở hữu hợp đồng (Owner):** Huỳnh Long Bảo Khanh (`W3-T3`)
* **Giao thức:** HTTPS / RESTful JSON
* **Đường dẫn cơ sở (Base URL):** `/api/v1`
* **Chuẩn định dạng:** JSON (`Content-Type: application/json; charset=utf-8`), khóa `camelCase`
* **Xác thực & Phân quyền:** Yêu cầu JWT Bearer Token với vai trò **`ROLE_ADMIN`** (`@PreAuthorize("hasRole('ADMIN')")`)

---

## 1. Quy tắc Phân quyền & Toàn vẹn Dữ liệu

* Endpoint quản trị `/api/v1/admin/lmi/**` từ chối mọi yêu cầu chưa đăng nhập (`401 Unauthorized`) và mọi tài khoản không có vai trò `ROLE_ADMIN` (`403 Forbidden`).
* Cơ chế **Upsert theo khóa tự nhiên** `(occupation_id, region, snapshot_date)` trên bảng `market_demand_indicators` (`uq_market_demand_occ_region_date`). Nếu bản ghi theo bộ ba khóa này đã tồn tại thì cập nhật các chỉ số; nếu chưa có thì chèn mới.
* Toàn bộ lô nạp chạy trong **một transaction nguyên tử (`@Transactional`)**: nếu bất kỳ phần tử nào sai định dạng, ngoài miền giá trị hoặc trỏ tới nghề nghiệp không tồn tại, toàn bộ lô được rollback và trả về `400 Bad Request` (`ErrorResponse`).

---

## 2. Danh mục Endpoint Chi tiết

### 2.1. `POST /api/v1/admin/lmi/import`
* **Mã thao tác (Operation ID):** `importMarketData`
* **Mô tả:** Nạp theo lô (batch upsert) chỉ số nhu cầu thị trường lao động LMI theo nghề nghiệp, khu vực và kỳ thống kê.
* **Quyền truy cập:** `ROLE_ADMIN`
* **Header:** `Authorization: Bearer <JWT_ACCESS_TOKEN>`

#### Request Body (`LmiImportRequest`)
```json
{
  "items": [
    {
      "occupationId": 1,
      "occupationCode": "SOC-15-1252",
      "region": "HCMC",
      "demandScore": 88.50,
      "growthRatePct": 12.40,
      "openJobPostingsCount": 1250,
      "competitiveIndex": 2.80,
      "snapshotDate": "2026-11-01"
    }
  ]
}
```

#### Đặc tả các trường trong `items[]` (`LmiBatchItemDto`)
* Giới hạn kích thước lô: `1` đến `500` bản ghi cho mỗi yêu cầu.

| Trường | Kiểu JSON | Bắt buộc | Quy tắc kiểm tra & Mặc định |
| :--- | :--- | :---: | :--- |
| `occupationId` | `Integer` (Long) | Có điều kiện | Số nguyên dương (`> 0`, không nhận số thập phân). Bắt buộc cung cấp ít nhất `occupationId` hoặc `occupationCode`. Nếu truyền cả hai thì phải trỏ tới cùng một nghề nghiệp. |
| `occupationCode` | `String` | Có điều kiện | Tối đa `50` ký tự, khớp `occupations.code` (vd: `"SOC-15-1252"`). |
| `region` | `String` | Tùy chọn | Tối đa `50` ký tự (vd: `"NATIONAL"`, `"HCMC"`, `"HANOI"`, `"DANANG"`). Mặc định `"NATIONAL"` nếu bỏ trống; tự động trim và chuyển chữ hoa. |
| `demandScore` | `Number` | Bắt buộc | Thập phân `NUMERIC(5,2)` trong khoảng `[0.00, 100.00]`, tối đa 2 chữ số thập phân. |
| `growthRatePct` | `Number` | Tùy chọn | Thập phân `NUMERIC(5,2)` trong khoảng `[-999.99, 999.99]`. |
| `openJobPostingsCount` | `Integer` | Tùy chọn | Số nguyên không âm (`>= 0`, không nhận số thập phân). Mặc định `0` nếu bỏ trống. |
| `competitiveIndex` | `Number` | Tùy chọn | Thập phân `NUMERIC(3,2)` trong khoảng `[0.00, 9.99]`. |
| `snapshotDate` | `String` (Date) | Bắt buộc | Ngày thống kê theo định dạng ISO-8601 `YYYY-MM-DD`. |

*(Lưu ý: Hệ thống bật `spring.jackson.deserialization.fail-on-unknown-properties=true`, các trường ngoài hợp đồng sẽ bị từ chối với HTTP `400 Bad Request`.)*

#### Phản hồi thành công (`200 OK` — `LmiImportResponse`)
```json
{
  "totalRecordsProcessed": 1,
  "recordsInserted": 1,
  "recordsUpdated": 0,
  "timestamp": "2026-11-19T12:00:00Z"
}
```

#### Mã lỗi dự kiến (`ErrorResponse`)
* `400 Bad Request` (`Bad Request` / `INVALID_LMI_REQUEST`): Danh sách `items` rỗng, thiếu trường bắt buộc, `demandScore` ngoài `[0, 100]`, `openJobPostingsCount < 0`, hoặc có trường JSON không hợp lệ.
* `400 Bad Request` (`INVALID_OCCUPATION`): `occupationId` hoặc `occupationCode` không tồn tại trong bảng `occupations`, hoặc không khớp nhau khi truyền cả hai.
* `401 Unauthorized`: Thiếu hoặc sai JWT token.
* `403 Forbidden`: Tài khoản không có vai trò `ROLE_ADMIN`.
* `409 Conflict` (`LMI_CONCURRENT_CONFLICT`): Xung đột ghi đồng thời trên khóa duy nhất `uq_market_demand_occ_region_date`.
