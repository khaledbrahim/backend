CREATE TABLE sale_processes
(
    id               BIGSERIAL PRIMARY KEY,
    property_id      BIGINT      NOT NULL,
    unit_id          BIGINT,
    status           VARCHAR(50) NOT NULL, -- DRAFT, EN_VENTE, VISITES, OFFRE_REÇUE, NÉGOCIATION, OFFRE_ACCEPTÉE, COMPROMIS_SIGNÉ, ACTE_SIGNÉ, VENTE_ABANDONNÉE
    asking_price     DECIMAL(15, 2),
    net_price        DECIMAL(15, 2),
    agency_fee       DECIMAL(15, 2),
    estimated_margin DECIMAL(15, 2),
    listing_date     DATE,
    closing_date     DATE,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_sale_property FOREIGN KEY (property_id) REFERENCES properties (id),
    CONSTRAINT fk_sale_unit FOREIGN KEY (unit_id) REFERENCES property_units (id)
);

-- Index for finding active sales processes quickly
CREATE INDEX idx_sale_process_property ON sale_processes (property_id);
CREATE INDEX idx_sale_process_status ON sale_processes (status);

CREATE TABLE sale_prospects
(
    id         BIGSERIAL PRIMARY KEY,
    process_id BIGINT       NOT NULL,
    first_name VARCHAR(100),
    last_name  VARCHAR(100) NOT NULL,
    email      VARCHAR(150),
    phone      VARCHAR(50),
    source     VARCHAR(50), -- AGENCE, ANNONCE, DIRECT, AUTRE
    notes      TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_prospect_process FOREIGN KEY (process_id) REFERENCES sale_processes (id) ON DELETE CASCADE
);

CREATE TABLE sale_visits
(
    id             BIGSERIAL PRIMARY KEY,
    process_id     BIGINT    NOT NULL,
    prospect_id    BIGINT    NOT NULL,
    visit_date     TIMESTAMP NOT NULL,
    visit_type     VARCHAR(50), -- SIMPLE, CONTRE_VISITE
    feedback       TEXT,
    interest_level VARCHAR(20), -- LOW, MEDIUM, HIGH
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_visit_process FOREIGN KEY (process_id) REFERENCES sale_processes (id) ON DELETE CASCADE,
    CONSTRAINT fk_visit_prospect FOREIGN KEY (prospect_id) REFERENCES sale_prospects (id) ON DELETE CASCADE
);

CREATE TABLE sale_offers
(
    id            BIGSERIAL PRIMARY KEY,
    process_id    BIGINT         NOT NULL,
    prospect_id   BIGINT         NOT NULL,
    offer_date    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    offer_amount  DECIMAL(15, 2) NOT NULL,
    conditions    TEXT,                    -- Suspensive conditions
    status        VARCHAR(50)    NOT NULL, -- PENDING, ACCEPTED, REJECTED, COUNTER_OFFER
    validity_date DATE,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_offer_process FOREIGN KEY (process_id) REFERENCES sale_processes (id) ON DELETE CASCADE,
    CONSTRAINT fk_offer_prospect FOREIGN KEY (prospect_id) REFERENCES sale_prospects (id) ON DELETE CASCADE
);

-- Ensure only one active sale process per property/unit (Partial unique indexes if supported, or handled in app logic)
-- We will handle strict validation in the application layer to avoid complex partial index logic across different DB types if unnecessary,
-- but for consistency with Leases, we can stick to app logic first.
