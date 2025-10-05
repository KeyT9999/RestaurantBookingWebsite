-- Complete fix for all database errors
-- This script resolves all Hibernate schema validation issues

-- 1. Drop problematic views that block schema changes
DROP VIEW IF EXISTS customer_voucher_details CASCADE;
DROP VIEW IF EXISTS voucher_usage_stats CASCADE;

-- 2. Fix NULL values in customer.full_name
DO $$ 
BEGIN
    -- Add full_name column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'customer' AND column_name = 'full_name') THEN
        ALTER TABLE customer ADD COLUMN full_name VARCHAR(255);
    END IF;
    
    -- Fix NULL values
    UPDATE customer SET full_name = 'Unknown Customer' WHERE full_name IS NULL OR full_name = '';
    
    -- Make it NOT NULL
    ALTER TABLE customer ALTER COLUMN full_name SET NOT NULL;
END $$;

-- 3. Fix NULL values in restaurant_owner.owner_name
DO $$ 
BEGIN
    -- Add owner_name column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'restaurant_owner' AND column_name = 'owner_name') THEN
        ALTER TABLE restaurant_owner ADD COLUMN owner_name VARCHAR(255);
    END IF;
    
    -- Fix NULL values
    UPDATE restaurant_owner SET owner_name = 'Unknown Owner' WHERE owner_name IS NULL OR owner_name = '';
    
    -- Make it NOT NULL
    ALTER TABLE restaurant_owner ALTER COLUMN owner_name SET NOT NULL;
END $$;

-- 4. Add missing voucher columns
ALTER TABLE voucher ADD COLUMN IF NOT EXISTS global_usage_limit INTEGER;
ALTER TABLE voucher ADD COLUMN IF NOT EXISTS per_customer_limit INTEGER NOT NULL DEFAULT 1;
ALTER TABLE voucher ADD COLUMN IF NOT EXISTS min_order_amount NUMERIC(18,2);
ALTER TABLE voucher ADD COLUMN IF NOT EXISTS max_discount_amount NUMERIC(18,2);

-- 5. Update existing vouchers with default per_customer_limit
UPDATE voucher SET per_customer_limit = COALESCE(usage_limit, 1) WHERE per_customer_limit IS NULL;

-- 6. Create voucher_redemption table if not exists
CREATE TABLE IF NOT EXISTS voucher_redemption (
    redemption_id SERIAL PRIMARY KEY,
    voucher_id INTEGER NOT NULL REFERENCES voucher(voucher_id),
    customer_id UUID NOT NULL REFERENCES customer(customer_id),
    booking_id INTEGER REFERENCES booking(booking_id),
    payment_id INTEGER REFERENCES payment(payment_id),
    discount_applied NUMERIC(18,2) NOT NULL,
    amount_before NUMERIC(18,2) NOT NULL,
    amount_after NUMERIC(18,2) NOT NULL,
    used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    meta JSONB
);

-- 7. Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_voucher_redemption_voucher_id ON voucher_redemption(voucher_id);
CREATE INDEX IF NOT EXISTS idx_voucher_redemption_customer_id ON voucher_redemption(customer_id);
CREATE INDEX IF NOT EXISTS idx_voucher_redemption_used_at ON voucher_redemption(used_at);

-- 8. Create unique constraints
CREATE UNIQUE INDEX IF NOT EXISTS ux_voucher_code_global ON voucher (LOWER(code)) WHERE restaurant_id IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS ux_voucher_rest_code ON voucher (restaurant_id, LOWER(code)) WHERE restaurant_id IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS ux_customer_voucher ON customer_voucher (customer_id, voucher_id);

-- 9. Recreate views
CREATE VIEW customer_voucher_details AS
SELECT 
    cv.customer_voucher_id,
    cv.customer_id,
    'Customer ' || cv.customer_id::text AS customer_name,
    cv.voucher_id,
    v.code AS voucher_code,
    v.description,
    v.discount_type,
    v.discount_value,
    v.start_date,
    v.end_date,
    v.status,
    COALESCE(v.per_customer_limit, 1) AS per_customer_limit,
    cv.times_used,
    cv.assigned_at,
    cv.last_used_at,
    CASE 
        WHEN v.status = 'ACTIVE' AND (v.end_date IS NULL OR v.end_date >= CURRENT_DATE) 
        THEN COALESCE(v.per_customer_limit, 1) - cv.times_used
        ELSE 0
    END AS remaining_uses
FROM customer_voucher cv
JOIN voucher v ON cv.voucher_id = v.voucher_id;

CREATE VIEW voucher_usage_stats AS
SELECT 
    v.voucher_id,
    v.code,
    v.description,
    COUNT(vr.redemption_id) AS total_redemptions,
    COUNT(DISTINCT vr.customer_id) AS unique_customers,
    COALESCE(SUM(vr.discount_applied), 0) AS total_discount_given,
    MAX(vr.used_at) AS last_used
FROM voucher v
LEFT JOIN voucher_redemption vr ON v.voucher_id = vr.voucher_id
GROUP BY v.voucher_id, v.code, v.description;
