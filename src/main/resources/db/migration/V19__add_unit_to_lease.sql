-- Add unit_id column to leases table
ALTER TABLE leases
    ADD COLUMN unit_id BIGINT;
ALTER TABLE leases
    ADD CONSTRAINT fk_lease_unit FOREIGN KEY (unit_id) REFERENCES property_units (id);

-- Clean up existing duplicates by keeping only the most recent active lease per property
-- This prevents the Unique Index from failing on existing data
UPDATE leases l
SET status = 'EXPIRED'
WHERE unit_id IS NULL
  AND status IN ('ACTIVE', 'UPCOMING', 'RENEWING', 'IN_GRACE_PERIOD')
  AND EXISTS (SELECT 1
              FROM leases l2
              WHERE l2.property_id = l.property_id
                AND l2.unit_id IS NULL
                AND l2.status IN ('ACTIVE', 'UPCOMING', 'RENEWING', 'IN_GRACE_PERIOD')
                AND l2.id > l.id -- Keep the one with higher ID (newer)
);

-- Create Partial Unique Index for Single-Unit Properties (or Global Lease on Immeuble)
-- Ensures only one active lease per property where unit_id is NULL
CREATE UNIQUE INDEX idx_lease_active_property_global ON leases (property_id)
    WHERE unit_id IS NULL AND status IN ('ACTIVE', 'UPCOMING', 'RENEWING', 'IN_GRACE_PERIOD');

-- Create Partial Unique Index for Multi-Unit Properties
-- Ensures only one active lease per unit
CREATE UNIQUE INDEX idx_lease_active_unit ON leases (unit_id)
    WHERE unit_id IS NOT NULL AND status IN ('ACTIVE', 'UPCOMING', 'RENEWING', 'IN_GRACE_PERIOD');
