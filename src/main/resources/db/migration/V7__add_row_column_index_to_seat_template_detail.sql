-- Add row_index and column_index to seat_templates_detail table
ALTER TABLE seat_templates_detail 
ADD COLUMN row_index INTEGER NOT NULL DEFAULT 0,
ADD COLUMN column_index INTEGER NOT NULL DEFAULT 0;

-- Remove default after adding columns
ALTER TABLE seat_templates_detail 
ALTER COLUMN row_index DROP DEFAULT,
ALTER COLUMN column_index DROP DEFAULT;
