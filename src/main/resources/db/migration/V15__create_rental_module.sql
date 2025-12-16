CREATE TABLE tenants
(
    id          SERIAL PRIMARY KEY,
    property_id BIGINT       NOT NULL,
    first_name  VARCHAR(255) NOT NULL,
    last_name   VARCHAR(255) NOT NULL,
    email       VARCHAR(255),
    phone       VARCHAR(50),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tenant_property FOREIGN KEY (property_id) REFERENCES properties (id)
);

CREATE TABLE leases
(
    id             SERIAL PRIMARY KEY,
    property_id    BIGINT         NOT NULL,
    tenant_id      BIGINT         NOT NULL,
    start_date     DATE           NOT NULL,
    end_date       DATE,
    rent_amount    NUMERIC(19, 2) NOT NULL,
    charges_amount NUMERIC(19, 2) DEFAULT 0,
    deposit_amount NUMERIC(19, 2) DEFAULT 0,
    frequency      VARCHAR(50)    NOT NULL,
    status         VARCHAR(50)    NOT NULL,
    created_at     TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lease_property FOREIGN KEY (property_id) REFERENCES properties (id),
    CONSTRAINT fk_lease_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

CREATE TABLE rent_payments
(
    id           SERIAL PRIMARY KEY,
    lease_id     BIGINT         NOT NULL,
    payment_date DATE           NOT NULL,
    amount       NUMERIC(19, 2) NOT NULL,
    payment_type VARCHAR(50),
    status       VARCHAR(50)    NOT NULL,
    receipt_url  VARCHAR(255),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_lease FOREIGN KEY (lease_id) REFERENCES leases (id)
);
