-- ============================================================
-- V1 — Khởi tạo schema ban đầu
-- Mô tả: Tạo 2 bảng employees và projects (schema gốc)
-- Ngày tạo: 02/03/2026
-- ============================================================

-- Bảng nhân viên
CREATE TABLE IF NOT EXISTS employees (
    id          VARCHAR(50)  NOT NULL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    position    VARCHAR(255) NOT NULL,
    department  VARCHAR(255) NOT NULL,
    email       VARCHAR(255) UNIQUE,
    phone       VARCHAR(20),
    manager_id  VARCHAR(50),
    avatar      VARCHAR(255),
    level       INT          DEFAULT 0,
    join_date   DATE
);

-- Bảng assignment nhân viên - dự án
-- (Mỗi dòng = 1 nhân viên tham gia 1 dự án)
CREATE TABLE IF NOT EXISTS projects (
    id          VARCHAR(50)  NOT NULL PRIMARY KEY,
    employee_id VARCHAR(50)  NOT NULL,
    name        VARCHAR(255) NOT NULL,
    role        VARCHAR(255) NOT NULL,
    start_date  DATE,
    end_date    DATE,
    status      VARCHAR(20)  NOT NULL DEFAULT 'pending'
                CHECK (status IN ('active', 'completed', 'pending')),

    CONSTRAINT fk_projects_employee
        FOREIGN KEY (employee_id) REFERENCES employees(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_projects_employee_id ON projects(employee_id);
CREATE INDEX IF NOT EXISTS idx_projects_name        ON projects(name);
CREATE INDEX IF NOT EXISTS idx_projects_status      ON projects(status);
CREATE INDEX IF NOT EXISTS idx_employees_manager_id ON employees(manager_id);
CREATE INDEX IF NOT EXISTS idx_employees_department ON employees(department);
