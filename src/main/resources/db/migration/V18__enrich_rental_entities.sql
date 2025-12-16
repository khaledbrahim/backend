ALTER TABLE leases
    ADD COLUMN payment_method VARCHAR(50),
    ADD COLUMN irl_indexation BOOLEAN DEFAULT FALSE,
    ADD COLUMN duration       INTEGER,
    ADD COLUMN creation_mode  VARCHAR(50); -- To distinguish manual vs auto creation if needed later

ALTER TABLE rent_payments
    ADD COLUMN due_date DATE;

-- Update existing payments to have a due_date equal to payment_date (as a fallback)
UPDATE rent_payments
SET due_date = payment_date
WHERE due_date IS NULL;
