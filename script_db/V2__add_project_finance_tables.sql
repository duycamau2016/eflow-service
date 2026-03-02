-- ============================================================
-- V2 — Thêm bảng tài chính dự án
-- Mô tả: project_info, invoice_milestone, project_phase
-- Ngày tạo: 02/03/2026
-- ============================================================

-- Thông tin tài chính tổng thể của dự án
CREATE TABLE IF NOT EXISTS project_info (
    id               BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    project_name     VARCHAR(255) NOT NULL UNIQUE,  -- FK logic tới projects.name
    customer         VARCHAR(255),
    contract_number  VARCHAR(100),
    description      TEXT,
    start_date       DATE,
    end_date         DATE,
    contract_value   DECIMAL(18,0) DEFAULT 0,       -- Giá trị hợp đồng (VNĐ)
    planned_cost     DECIMAL(18,0) DEFAULT 0,       -- Kế hoạch chi phí (VNĐ)
    actual_cost      DECIMAL(18,0) DEFAULT 0,       -- Chi phí thực tế (VNĐ, admin nhập)
    created_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Mốc xuất hóa đơn của dự án (SIT, UAT, PAT, Nghiệm thu...)
CREATE TABLE IF NOT EXISTS invoice_milestone (
    id               BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    project_name     VARCHAR(255) NOT NULL,         -- FK logic tới project_info.project_name
    name             VARCHAR(255) NOT NULL,         -- Tên mốc: SIT / UAT / PAT / tùy admin
    amount           DECIMAL(18,0) DEFAULT 0,       -- Số tiền mốc này (VNĐ)
    planned_date     DATE,                          -- Ngày kế hoạch xuất HĐ
    actual_date      DATE,                          -- Ngày thực tế xuất HĐ
    status           VARCHAR(20)  NOT NULL DEFAULT 'pending'
                     CHECK (status IN ('pending', 'invoiced', 'paid')),
    note             TEXT,
    sort_order       INT          DEFAULT 0,        -- Thứ tự hiển thị
    created_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_invoice_project
        FOREIGN KEY (project_name) REFERENCES project_info(project_name)
        ON DELETE CASCADE
);

-- Giai đoạn / tiến độ dự án
CREATE TABLE IF NOT EXISTS project_phase (
    id               BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    project_name     VARCHAR(255) NOT NULL,         -- FK logic tới project_info.project_name
    name             VARCHAR(255) NOT NULL,         -- Tên phase: Phân tích / Dev / SIT...
    planned_start    DATE,
    planned_end      DATE,
    actual_start     DATE,
    actual_end       DATE,
    progress         INT          DEFAULT 0         -- % hoàn thành (0-100)
                     CHECK (progress BETWEEN 0 AND 100),
    status           VARCHAR(20)  NOT NULL DEFAULT 'on_track'
                     CHECK (status IN ('on_track', 'at_risk', 'delayed', 'completed')),
    note             TEXT,
    sort_order       INT          DEFAULT 0,
    created_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_phase_project
        FOREIGN KEY (project_name) REFERENCES project_info(project_name)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_invoice_project_name ON invoice_milestone(project_name);
CREATE INDEX IF NOT EXISTS idx_phase_project_name   ON project_phase(project_name);
