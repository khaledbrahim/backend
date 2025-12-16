-- Add constraints to ensure progress is between 0 and 100
ALTER TABLE construction_projects
    ADD CONSTRAINT chk_project_progress CHECK (progress >= 0 AND progress <= 100);
ALTER TABLE construction_lots
    ADD CONSTRAINT chk_lot_progress CHECK (progress >= 0 AND progress <= 100);
ALTER TABLE construction_progress_logs
    ADD CONSTRAINT chk_log_progress CHECK (progress >= 0 AND progress <= 100);

-- Add is_archived to construction_projects
ALTER TABLE construction_projects
    ADD COLUMN is_archived BOOLEAN NOT NULL DEFAULT FALSE;

-- Add sort_order and assigned_to to construction_lots
ALTER TABLE construction_lots
    ADD COLUMN sort_order INT DEFAULT 0;
ALTER TABLE construction_lots
    ADD COLUMN assigned_to BIGINT;

-- Index for assigned_to for future use
CREATE INDEX idx_lot_assigned_to ON construction_lots (assigned_to);
