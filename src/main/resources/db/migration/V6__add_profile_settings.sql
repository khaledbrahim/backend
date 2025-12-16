-- Add new columns to users table
ALTER TABLE users
    ADD COLUMN country             VARCHAR(255),
    ADD COLUMN language            VARCHAR(10) DEFAULT 'en'  NOT NULL,
    ADD COLUMN timezone            VARCHAR(50) DEFAULT 'UTC' NOT NULL,
    ADD COLUMN email_notifications BOOLEAN     DEFAULT TRUE  NOT NULL,
    ADD COLUMN deleted             BOOLEAN     DEFAULT FALSE NOT NULL,
    ADD COLUMN deleted_at          TIMESTAMP;

-- Create audit_logs table
CREATE TABLE audit_logs
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    action     VARCHAR(255) NOT NULL,
    details    TEXT,
    ip_address VARCHAR(45),
    timestamp  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_user_id ON audit_logs (user_id);
