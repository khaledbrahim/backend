ALTER TABLE visitor_requests
    ADD COLUMN proposed_price NUMERIC(19, 2),
    ADD COLUMN preferred_date TIMESTAMP;
