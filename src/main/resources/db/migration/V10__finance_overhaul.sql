-- Create property_units table
CREATE TABLE property_units
(
    id          BIGSERIAL PRIMARY KEY,
    property_id BIGINT       NOT NULL,
    name        VARCHAR(255) NOT NULL,
    type        VARCHAR(50)  NOT NULL,
    area        DOUBLE PRECISION,
    shares      DOUBLE PRECISION         DEFAULT 0,
    description TEXT,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT fk_unit_property FOREIGN KEY (property_id) REFERENCES properties (id) ON DELETE CASCADE
);

CREATE INDEX idx_unit_property ON property_units (property_id);

-- Alter financial_operations table
ALTER TABLE financial_operations
    ADD COLUMN unit_id BIGINT,
    ADD COLUMN source  VARCHAR(50); -- MANUAL, OCR

ALTER TABLE financial_operations
    ADD CONSTRAINT fk_finance_unit FOREIGN KEY (unit_id) REFERENCES property_units (id) ON DELETE SET NULL;

CREATE INDEX idx_finance_unit ON financial_operations (unit_id);

-- Alter financial_categories table
ALTER TABLE financial_categories
    ADD COLUMN applicable_property_type VARCHAR(50);
-- Null = All, or specific PropertyType

-- Insert new default categories
INSERT INTO financial_categories (name, type, is_system_default, applicable_property_type)
VALUES
-- Terrain
('Géomètre', 'EXPENSE', TRUE, 'LAND'),
('Viabilisation', 'EXPENSE', TRUE, 'LAND'),
('Étude de sol', 'EXPENSE', TRUE, 'LAND'),
('Permis de construire', 'EXPENSE', TRUE, 'LAND'),
('Clôture', 'EXPENSE', TRUE, 'LAND'),

-- Appartement / Maison (Loyer exists, adding specifics)
('Taxe Foncière', 'EXPENSE', TRUE, 'APARTMENT'),
('Taxe Foncière', 'EXPENSE', TRUE, 'HOUSE'),
('Copropriété', 'EXPENSE', TRUE, 'APARTMENT'),

-- Immeuble
('Charges Communes', 'EXPENSE', TRUE, 'BUILDING'),
('Façade', 'EXPENSE', TRUE, 'BUILDING'),
('Toiture', 'EXPENSE', TRUE, 'BUILDING'),
('Syndic', 'EXPENSE', TRUE, 'BUILDING'),
('Ascenseur', 'EXPENSE', TRUE, 'BUILDING'),

-- Local Commercial
('Mise aux normes', 'EXPENSE', TRUE, 'COMMERCIAL'),
('Ventilation / Clim', 'EXPENSE', TRUE, 'COMMERCIAL'),
('Aménagement', 'EXPENSE', TRUE, 'COMMERCIAL'),
('Frais Juridiques', 'EXPENSE', TRUE, 'COMMERCIAL'),

-- Parking
('Impôts', 'EXPENSE', TRUE, 'PARKING');
