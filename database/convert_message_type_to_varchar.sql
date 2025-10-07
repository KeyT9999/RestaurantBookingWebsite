-- Convert message_type from enum to VARCHAR to work with Hibernate
-- This is the simplest solution

-- Step 1: Add temporary VARCHAR column
ALTER TABLE message ADD COLUMN message_type_temp VARCHAR(20);

-- Step 2: Copy data from enum to VARCHAR
UPDATE message SET message_type_temp = message_type::text;

-- Step 3: Drop the enum column
ALTER TABLE message DROP COLUMN message_type;

-- Step 4: Rename temp column to original name
ALTER TABLE message RENAME COLUMN message_type_temp TO message_type;

-- Step 5: Add NOT NULL constraint
ALTER TABLE message ALTER COLUMN message_type SET NOT NULL;

-- Step 6: Add check constraint for valid values
ALTER TABLE message ADD CONSTRAINT chk_message_type 
    CHECK (message_type IN ('TEXT', 'IMAGE', 'FILE', 'SYSTEM'));

-- Step 7: Add comment
COMMENT ON COLUMN message.message_type IS 'Message type: TEXT, IMAGE, FILE, SYSTEM';

-- Verify the change
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'message' AND column_name = 'message_type';
