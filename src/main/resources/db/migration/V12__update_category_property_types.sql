-- Update Financial Categories Pivot Table to use French PropertyType enum values
-- (Note: applicable_property_type was moved to this pivot table in V11)
UPDATE financial_category_property_types
SET property_type = 'APPARTEMENT'
WHERE property_type = 'APARTMENT';
UPDATE financial_category_property_types
SET property_type = 'MAISON'
WHERE property_type = 'HOUSE';
UPDATE financial_category_property_types
SET property_type = 'IMMEUBLE'
WHERE property_type = 'BUILDING';
UPDATE financial_category_property_types
SET property_type = 'TERRAIN'
WHERE property_type = 'LAND';
UPDATE financial_category_property_types
SET property_type = 'LOCAL_COMMERCIAL'
WHERE property_type = 'COMMERCIAL';

-- Also update Properties table to ensure consistency
UPDATE properties
SET property_type = 'APPARTEMENT'
WHERE property_type = 'APARTMENT';
UPDATE properties
SET property_type = 'MAISON'
WHERE property_type = 'HOUSE';
UPDATE properties
SET property_type = 'IMMEUBLE'
WHERE property_type = 'BUILDING';
UPDATE properties
SET property_type = 'TERRAIN'
WHERE property_type = 'LAND';
UPDATE properties
SET property_type = 'LOCAL_COMMERCIAL'
WHERE property_type = 'COMMERCIAL';
