-- Script to fix depositamount column in restaurant_table
-- This script renames the column from deposit_amount to depositamount

-- First, update any NULL values to 0
UPDATE restaurant_table 
SET deposit_amount = 0 
WHERE deposit_amount IS NULL;

-- Rename the column from deposit_amount to depositamount
ALTER TABLE restaurant_table 
RENAME COLUMN deposit_amount TO depositamount;

-- Add NOT NULL constraint if it doesn't exist
ALTER TABLE restaurant_table 
ALTER COLUMN depositamount SET NOT NULL;

-- Set default value if it doesn't exist
ALTER TABLE restaurant_table 
ALTER COLUMN depositamount SET DEFAULT 0;
