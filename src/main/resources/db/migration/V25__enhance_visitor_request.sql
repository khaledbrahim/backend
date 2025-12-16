ALTER TABLE visitor_requests
    ADD COLUMN unit_id      BIGINT,
    ADD COLUMN contacted_at TIMESTAMP,
    ADD COLUMN handled_by   BIGINT;

ALTER TABLE visitor_requests
    ADD CONSTRAINT fk_visitor_requests_unit
        FOREIGN KEY (unit_id) REFERENCES property_units (id);

ALTER TABLE visitor_requests
    ADD CONSTRAINT fk_visitor_requests_handler
        FOREIGN KEY (handled_by) REFERENCES users (id);

CREATE INDEX idx_visitor_requests_unit ON visitor_requests (unit_id);
