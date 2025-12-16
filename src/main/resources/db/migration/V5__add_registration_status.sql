ALTER TABLE users
    ADD COLUMN registration_status VARCHAR(255) DEFAULT 'PENDING_VERIFICATION';
ALTER TABLE users
    ADD COLUMN activation_token VARCHAR(255);

UPDATE users
SET registration_status = 'VERIFIED'
WHERE is_verified = true;
