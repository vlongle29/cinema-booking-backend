-- Fix seat_template column lengths
ALTER TABLE seat_template 
    ALTER COLUMN name TYPE VARCHAR(255),
    ALTER COLUMN description TYPE VARCHAR(500);
