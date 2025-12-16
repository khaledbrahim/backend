ALTER TABLE sale_prospects
    ADD COLUMN engagement VARCHAR(20);
UPDATE sale_prospects
SET engagement = 'COLD'
WHERE engagement IS NULL;
