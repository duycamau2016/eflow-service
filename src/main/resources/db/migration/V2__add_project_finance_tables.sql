-- ============================================================
-- V2 — Thêm bảng tài chính dự án
-- Mô tả: project_info, invoice_milestone, project_phase
-- Ngày tạo: 02/03/2026
-- PostgreSQL compatible (BIGSERIAL thay AUTO_INCREMENT, không có ON UPDATE)
-- ============================================================

-- Thông tin tài chính tổng thể của dự án
CREATE TABLE IF NOT EXISTS project_info (
    id               BIGSERIAL    NOT NULL PRIMARY KEY,       -- ID tự tăng
    project_name     VARCHAR(255) NOT NULL UNIQUE,            -- FK logic tới projects.name (tên dự án duy nhất)
    customer         VARCHAR(255),                            -- Tên khách hàng / đối tác
    contract_number  VARCHAR(100),                            -- Số hợp đồng
    description      TEXT,                                    -- Mô tả nội dung dự án
    start_date       DATE,                                    -- Ngày bắt đầu dự án (theo hợp đồng)
    end_date         DATE,                                    -- Ngày kết thúc dự án (theo hợp đồng)
    contract_value   DECIMAL(18,0) DEFAULT 0,                 -- Giá trị hợp đồng (VNĐ)
    planned_cost     DECIMAL(18,0) DEFAULT 0,                 -- Kế hoạch chi phí (VNĐ)
    actual_cost      DECIMAL(18,0) DEFAULT 0,                 -- Chi phí thực tế (VNĐ, admin nhập)
    created_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,  -- Thời điểm tạo bản ghi
    updated_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP   -- Thời điểm cập nhật gần nhất
);

-- Mốc xuất hóa đơn của dự án (SIT, UAT, PAT, Nghiệm thu...)
CREATE TABLE IF NOT EXISTS invoice_milestone (
    id               BIGSERIAL    NOT NULL PRIMARY KEY,       -- ID tự tăng
    project_name     VARCHAR(255) NOT NULL,                   -- FK logic tới project_info.project_name
    name             VARCHAR(255) NOT NULL,                   -- Tên mốc: SIT / UAT / PAT / tùy admin
    amount           DECIMAL(18,0) DEFAULT 0,                 -- Số tiền mốc này (VNĐ)
    planned_date     DATE,                                    -- Ngày kế hoạch xuất hóa đơn
    actual_date      DATE,                                    -- Ngày thực tế xuất hóa đơn
    status           VARCHAR(20)  NOT NULL DEFAULT 'pending'  -- Trạng thái: pending / invoiced / paid
                     CHECK (status IN ('pending', 'invoiced', 'paid')),
    note             TEXT,                                    -- Ghi chú thêm
    sort_order       INT          DEFAULT 0,                  -- Thứ tự hiển thị trong danh sách
    created_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,  -- Thời điểm tạo bản ghi
    updated_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,  -- Thời điểm cập nhật gần nhất

    CONSTRAINT fk_invoice_project
        FOREIGN KEY (project_name) REFERENCES project_info(project_name)
        ON DELETE CASCADE
);

-- Giai đoạn / tiến độ dự án
CREATE TABLE IF NOT EXISTS project_phase (
    id               BIGSERIAL    NOT NULL PRIMARY KEY,       -- ID tự tăng
    project_name     VARCHAR(255) NOT NULL,                   -- FK logic tới project_info.project_name
    name             VARCHAR(255) NOT NULL,                   -- Tên phase: Phân tích / Dev / SIT / UAT...
    planned_start    DATE,                                    -- Ngày bắt đầu kế hoạch
    planned_end      DATE,                                    -- Ngày kết thúc kế hoạch
    actual_start     DATE,                                    -- Ngày bắt đầu thực tế
    actual_end       DATE,                                    -- Ngày kết thúc thực tế
    progress         INT          DEFAULT 0                   -- % hoàn thành (0-100)
                     CHECK (progress BETWEEN 0 AND 100),
    status           VARCHAR(20)  NOT NULL DEFAULT 'on_track' -- Trạng thái: on_track / at_risk / delayed / completed
                     CHECK (status IN ('on_track', 'at_risk', 'delayed', 'completed')),
    note             TEXT,                                    -- Ghi chú thêm
    sort_order       INT          DEFAULT 0,                  -- Thứ tự hiển thị trong danh sách
    created_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,  -- Thời điểm tạo bản ghi
    updated_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,  -- Thời điểm cập nhật gần nhất

    CONSTRAINT fk_phase_project
        FOREIGN KEY (project_name) REFERENCES project_info(project_name)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_invoice_project_name ON invoice_milestone(project_name);
CREATE INDEX IF NOT EXISTS idx_phase_project_name   ON project_phase(project_name);
