-- Migration: Add preferred_booking_time column to waitlist table
-- Date: 2025-01-18
-- Description: Add preferred_booking_time field to store customer's preferred booking time

-- Add the new column
ALTER TABLE waitlist 
ADD COLUMN preferred_booking_time TIMESTAMPTZ;

-- Add comment to document the column
COMMENT ON COLUMN waitlist.preferred_booking_time IS 'Customer preferred booking time when joining waitlist';

-- Optional: Update existing records to have a default preferred booking time
-- This sets preferred_booking_time to join_time + 30 minutes for existing records
UPDATE waitlist 
SET preferred_booking_time = join_time + INTERVAL '30 minutes'
WHERE preferred_booking_time IS NULL;

-- Verify the migration
SELECT 
    waitlist_id,
    customer_id,
    restaurant_id,
    party_size,
    join_time,
    preferred_booking_time,
    status
FROM waitlist 
LIMIT 5;
