-- Create join table for leases and tenants
CREATE TABLE lease_tenants
(
    lease_id  BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    PRIMARY KEY (lease_id, tenant_id),
    CONSTRAINT fk_lt_lease FOREIGN KEY (lease_id) REFERENCES leases (id),
    CONSTRAINT fk_lt_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

-- Migrate existing data (assuming 1 tenant per lease originally)
INSERT INTO lease_tenants (lease_id, tenant_id)
SELECT id, tenant_id
FROM leases
WHERE tenant_id IS NOT NULL;

-- Remove the old foreign key column from leases
ALTER TABLE leases
    DROP CONSTRAINT fk_lease_tenant;
ALTER TABLE leases
    DROP COLUMN tenant_id;
