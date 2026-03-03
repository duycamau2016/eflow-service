-- ============================================================
-- V1 — Khởi tạo schema ban đầu
-- Mô tả: Tạo 2 bảng employees và projects (schema gốc)
-- Ngày tạo: 02/03/2026
-- ============================================================

-- Bảng nhân viên
CREATE TABLE IF NOT EXISTS employees (
    id          VARCHAR(50)  NOT NULL PRIMARY KEY,           -- Mã nhân viên (VD: EMP001)
    name        VARCHAR(255) NOT NULL,                       -- Họ và tên đầy đủ
    position    VARCHAR(255) NOT NULL,                       -- Chức danh (VD: Backend Developer)
    department  VARCHAR(255) NOT NULL,                       -- Phòng ban (VD: Engineering)
    email       VARCHAR(255) UNIQUE,                         -- Email công ty (duy nhất)
    phone       VARCHAR(20),                                 -- Số điện thoại liên hệ
    manager_id  VARCHAR(50),                                 -- Mã quản lý trực tiếp (tự tham chiếu employees.id)
    avatar      VARCHAR(255),                                -- URL ảnh đại diện
    level       INT          DEFAULT 0,                      -- Cấp bậc nhân viên (0 = junior, tăng dần)
    join_date   DATE                                         -- Ngày gia nhập công ty
);

-- Bảng assignment nhân viên - dự án
-- (Mỗi dòng = 1 nhân viên tham gia 1 dự án)
CREATE TABLE IF NOT EXISTS projects (
    id          VARCHAR(50)  NOT NULL PRIMARY KEY,           -- Mã assignment (UUID)
    employee_id VARCHAR(50)  NOT NULL,                       -- FK → employees.id
    name        VARCHAR(255) NOT NULL,                       -- Tên dự án
    role        VARCHAR(255) NOT NULL,                       -- Vai trò của nhân viên trong dự án (VD: Tech Lead)
    start_date  DATE,                                        -- Ngày bắt đầu tham gia dự án
    end_date    DATE,                                        -- Ngày kết thúc tham gia dự án
    status      VARCHAR(20)  NOT NULL DEFAULT 'pending'      -- Trạng thái: pending / active / completed
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
