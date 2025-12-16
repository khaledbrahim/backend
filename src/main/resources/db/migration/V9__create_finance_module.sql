CREATE TABLE financial_categories
(
    id                BIGSERIAL PRIMARY KEY,
    name              VARCHAR(100) NOT NULL,
    type              VARCHAR(20)  NOT NULL, -- REVENUE vs EXPENSE
    user_id           BIGINT,                -- Nullable for system default categories
    is_system_default BOOLEAN                  DEFAULT FALSE,
    created_at        TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT fk_category_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE TABLE financial_operations
(
    id                     BIGSERIAL PRIMARY KEY,
    property_id            BIGINT         NOT NULL,
    category_id            BIGINT,
    operation_type         VARCHAR(20)    NOT NULL, -- REVENUE vs EXPENSE
    amount                 NUMERIC(19, 2) NOT NULL,
    operation_date         DATE           NOT NULL,
    description            TEXT,
    attachment_url         VARCHAR(500),
    attachment_storage_key VARCHAR(500),
    created_at             TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at             TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT fk_finance_property FOREIGN KEY (property_id) REFERENCES properties (id) ON DELETE CASCADE,
    CONSTRAINT fk_finance_category FOREIGN KEY (category_id) REFERENCES financial_categories (id) ON DELETE SET NULL
);

CREATE INDEX idx_finance_property ON financial_operations (property_id);
CREATE INDEX idx_finance_date ON financial_operations (operation_date);
CREATE INDEX idx_finance_category ON financial_operations (category_id);

-- Insert default categories
INSERT INTO financial_categories (name, type, is_system_default)
VALUES ('Loyer perçu', 'REVENUE', TRUE),
       ('Remboursement', 'REVENUE', TRUE),
       ('Autre revenu', 'REVENUE', TRUE),
       ('Travaux', 'EXPENSE', TRUE),
       ('Taxe', 'EXPENSE', TRUE),
       ('Assurance', 'EXPENSE', TRUE),
       ('Entretien', 'EXPENSE', TRUE),
       ('Frais de gestion', 'EXPENSE', TRUE),
       ('Remboursement prêt', 'EXPENSE', TRUE),
       ('Autre dépense', 'EXPENSE', TRUE);
