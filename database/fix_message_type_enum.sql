-- Fix message_type enum to accept string values
-- This allows Hibernate to send string values directly

-- Drop existing constraint if exists
ALTER TABLE message DROP CONSTRAINT IF EXISTS chk_message_type;

-- Add check constraint to accept string values
ALTER TABLE message ADD CONSTRAINT chk_message_type 
    CHECK (message_type IN ('TEXT', 'IMAGE', 'FILE', 'SYSTEM'));

-- Update existing records to use string values
UPDATE message SET message_type = 'TEXT' WHERE message_type::text = 'TEXT';
UPDATE message SET message_type = 'IMAGE' WHERE message_type::text = 'IMAGE';
UPDATE message SET message_type = 'FILE' WHERE message_type::text = 'FILE';
UPDATE message SET message_type = 'SYSTEM' WHERE message_type::text = 'SYSTEM';

-- Add comment for documentation
COMMENT ON COLUMN message.message_type IS 'Message type: TEXT, IMAGE, FILE, SYSTEM';
