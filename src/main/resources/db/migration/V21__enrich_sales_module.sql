-- Add financial and decision-support columns to sale_processes
ALTER TABLE sale_processes
    ADD COLUMN acquisition_price    DECIMAL(15, 2),
    ADD COLUMN total_works_amount   DECIMAL(15, 2),
    ADD COLUMN total_charges_amount DECIMAL(15, 2),
    ADD COLUMN target_price         DECIMAL(15, 2),
    ADD COLUMN estimated_net_gain   DECIMAL(15, 2),
    ADD COLUMN global_roi           DECIMAL(8, 4), -- e.g., 0.1523 for 15.23%
    ADD COLUMN abandon_reason       TEXT;
