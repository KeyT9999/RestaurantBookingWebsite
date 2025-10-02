-- Simple SQL script to fix the deposit_amount issue
-- Run this script directly on your PostgreSQL database

-- Step 1: Update all NULL values to 0
UPDATE restaurant_table 
SET deposit_amount = 0 
WHERE deposit_amount IS NULL;

-- Step 2: Verify the update
SELECT COUNT(*) as null_count 
FROM restaurant_table 
WHERE deposit_amount IS NULL;

-- Step 3: Check current data
SELECT table_id, table_name, deposit_amount 
FROM restaurant_table 
LIMIT 5;
