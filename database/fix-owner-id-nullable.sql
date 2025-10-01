-- Fix owner_id to be nullable in restaurant_profile table
-- This allows creating restaurants without owner first (for seeding data)

ALTER TABLE restaurant_profile 
ALTER COLUMN owner_id DROP NOT NULL;

-- Verify the change
SELECT column_name, is_nullable, data_type 
FROM information_schema.columns 
WHERE table_name = 'restaurant_profile' 
AND column_name = 'owner_id';
