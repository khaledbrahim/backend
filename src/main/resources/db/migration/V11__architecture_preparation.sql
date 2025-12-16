-- 1. Update properties table
ALTER TABLE properties
    RENAME COLUMN type TO property_type;
ALTER TABLE properties
    ADD COLUMN rental_type VARCHAR(20);
ALTER TABLE properties
    ADD COLUMN ownership_type VARCHAR(20);
ALTER TABLE properties
    ADD COLUMN market_value DECIMAL(15, 2);

-- 2. Update financial_operations table
ALTER TABLE financial_operations
    ADD COLUMN currency VARCHAR(3) DEFAULT 'EUR';
ALTER TABLE financial_operations
    ADD COLUMN status VARCHAR(20) DEFAULT 'CONFIRMED';
ALTER TABLE financial_operations
    ADD COLUMN source_reference VARCHAR(100);

-- 3. Create pivot table for financial categories property types
CREATE TABLE financial_category_property_types
(
    category_id   BIGINT      NOT NULL,
    property_type VARCHAR(20) NOT NULL,
    PRIMARY KEY (category_id, property_type),
    CONSTRAINT fk_category_property_type
        FOREIGN KEY (category_id)
            REFERENCES financial_categories (id)
            ON DELETE CASCADE
);

-- 4. Migrate existing applicable_property_type data to pivot table
INSERT INTO financial_category_property_types (category_id, property_type)
SELECT id, applicable_property_type
FROM financial_categories
WHERE applicable_property_type IS NOT NULL;

-- 5. Drop old column from financial_categories
ALTER TABLE financial_categories
    DROP COLUMN applicable_property_type;
