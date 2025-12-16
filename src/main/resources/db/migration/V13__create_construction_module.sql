-- Create construction_projects table
CREATE TABLE construction_projects
(
    id           BIGSERIAL PRIMARY KEY,
    property_id  BIGINT       NOT NULL,
    name         VARCHAR(255) NOT NULL,
    status       VARCHAR(50)  NOT NULL    DEFAULT 'NOT_STARTED', -- NOT_STARTED, IN_PROGRESS, PAUSED, DONE
    start_date   DATE,
    end_date     DATE,
    budget_total NUMERIC(19, 2)           DEFAULT 0,
    progress     NUMERIC(5, 2)            DEFAULT 0,
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at   TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT fk_project_property FOREIGN KEY (property_id) REFERENCES properties (id) ON DELETE CASCADE
);

CREATE INDEX idx_project_property ON construction_projects (property_id);

-- Create construction_lots table
CREATE TABLE construction_lots
(
    id              BIGSERIAL PRIMARY KEY,
    project_id      BIGINT       NOT NULL,
    name            VARCHAR(255) NOT NULL,
    lot_type        VARCHAR(50)  NOT NULL, -- DEMOLITION, GROS_OEUVRE, ELECTRICITE, etc.
    budget_expected NUMERIC(19, 2)           DEFAULT 0,
    budget_real     NUMERIC(19, 2)           DEFAULT 0,
    progress        NUMERIC(5, 2)            DEFAULT 0,
    notes           TEXT,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT fk_lot_project FOREIGN KEY (project_id) REFERENCES construction_projects (id) ON DELETE CASCADE
);

CREATE INDEX idx_lot_project ON construction_lots (project_id);

-- Create construction_progress_logs table
CREATE TABLE construction_progress_logs
(
    id          BIGSERIAL PRIMARY KEY,
    lot_id      BIGINT        NOT NULL,
    log_date    DATE          NOT NULL,
    progress    NUMERIC(5, 2) NOT NULL,
    description TEXT,
    photo_url   VARCHAR(500),
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT fk_log_lot FOREIGN KEY (lot_id) REFERENCES construction_lots (id) ON DELETE CASCADE
);

CREATE INDEX idx_log_lot ON construction_progress_logs (lot_id);
