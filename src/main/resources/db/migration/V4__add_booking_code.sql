-- Add booking_code column to bookings table
ALTER TABLE bookings ADD COLUMN booking_code VARCHAR(50) UNIQUE;

-- Create index for faster lookup
CREATE INDEX idx_bookings_booking_code ON bookings(booking_code);
