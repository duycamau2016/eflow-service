-- ============================================================
-- V3 — Thêm bảng danh mục phòng ban
-- Mô tả: departments — danh mục phòng ban toàn công ty
-- Ngày tạo: 03/03/2026
-- ============================================================

CREATE TABLE IF NOT EXISTS departments (
    id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    sort_order INT          DEFAULT 0,
    CONSTRAINT uq_departments_name UNIQUE (name)
);

CREATE INDEX IF NOT EXISTS idx_departments_sort ON departments(sort_order);

-- Seed dữ liệu mặc định
INSERT INTO departments (name, sort_order) VALUES
    ('Ban Giám đốc',                   0),
    ('Kỹ thuật / Engineering',         1),
    ('Kinh doanh / Sales',             2),
    ('Kế toán / Finance',              3),
    ('Nhân sự / HR',                   4),
    ('Marketing',                      5),
    ('Vận hành / Operations',          6),
    ('Pháp lý / Legal',                7),
    ('Hạ tầng / IT Infrastructure',    8),
    ('Kiểm thử / QA',                  9),
    ('Thiết kế / Design',             10),
    ('Chăm sóc khách hàng / CS',      11);
