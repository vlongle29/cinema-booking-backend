-- Add ticket_code and QR code support to tickets table

ALTER TABLE tickets ADD COLUMN IF NOT EXISTS ticket_code VARCHAR(20) UNIQUE;
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS qr_code_url VARCHAR(500);
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS is_checked_in BOOLEAN DEFAULT FALSE;

-- Create index for ticket_code lookup
CREATE INDEX IF NOT EXISTS idx_tickets_code ON tickets(ticket_code);
