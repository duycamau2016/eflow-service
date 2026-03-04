-- ============================================================
-- V4 — Audit Log: ghi lại lịch sử thay đổi dữ liệu
-- ============================================================

CREATE TABLE IF NOT EXISTS audit_logs (
    id          BIGSERIAL     NOT NULL PRIMARY KEY,
    action      VARCHAR(20)   NOT NULL,           -- CREATE | UPDATE | DELETE | IMPORT
    entity_type VARCHAR(30)   NOT NULL,           -- EMPLOYEE | PROJECT | DEPARTMENT
    entity_id   VARCHAR(100),
    entity_name VARCHAR(255),
    actor       VARCHAR(100)  NOT NULL DEFAULT 'system',
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    detail      TEXT
);

CREATE INDEX IF NOT EXISTS idx_audit_created     ON audit_logs(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_actor       ON audit_logs(actor);
CREATE INDEX IF NOT EXISTS idx_audit_entity_type ON audit_logs(entity_type);
CREATE INDEX IF NOT EXISTS idx_audit_action      ON audit_logs(action);
