# script_db — Lịch sử thay đổi Schema

Thư mục chứa toàn bộ SQL script thay đổi cấu trúc database theo thứ tự phiên bản.

## 📁 Danh sách script

| File | Mô tả | Ngày tạo |
|------|-------|----------|
| `V1__init_schema.sql` | Khởi tạo schema ban đầu (employees, projects) | 02/03/2026 |
| `V2__add_project_finance_tables.sql` | Thêm bảng tài chính dự án (project_info, invoice_milestone, project_phase) | 02/03/2026 |

## 📏 Quy tắc đặt tên

```
V{version}__{mô_tả_ngắn}.sql
```

- `V` + số thứ tự tăng dần (V1, V2, V3...)
- Mô tả ngắn gọn, dùng dấu `_` thay khoảng trắng
- **Không sửa file cũ** — chỉ tạo file mới khi cần thay đổi schema

## 🚀 Cách chạy khi triển khai

### MySQL (Production)
```bash
mysql -u root -p eflowdb < script_db/V1__init_schema.sql
mysql -u root -p eflowdb < script_db/V2__add_project_finance_tables.sql
```

### H2 (Development)
H2 in-memory với `ddl-auto=create-drop` tự tạo schema từ entity — không cần chạy script.  
Script này dùng cho **MySQL production** và tham khảo cấu trúc bảng.

## ⚠️ Lưu ý

- Script viết theo chuẩn **MySQL 8**
- Luôn dùng `CREATE TABLE IF NOT EXISTS` để tránh lỗi khi chạy lại
- Mỗi khi thêm bảng hoặc cột mới, tạo file V(n+1) mới, **không sửa file cũ**
