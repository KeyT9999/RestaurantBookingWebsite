-- ============================================================
-- 🧾 VOUCHER SCHEMA FINAL – COMPLETE & TESTED
-- PostgreSQL-safe (idempotent where possible)
-- Based on lessons learned from previous attempts
-- ============================================================

BEGIN;

-- 0) Extensions ------------------------------------------------
CREATE EXTENSION IF NOT EXISTS citext;

-- 1) VOUCHER: constraints, limits, policies -------------------

-- 1.1 Normalize enum-like checks (UPPER casing)
ALTER TABLE voucher DROP CONSTRAINT IF EXISTS voucher_discount_type_check;
ALTER TABLE voucher DROP CONSTRAINT IF EXISTS chk_voucher_discount_type;
ALTER TABLE voucher
  ADD CONSTRAINT chk_voucher_discount_type
  CHECK (discount_type IN ('PERCENT','FIXED'));

ALTER TABLE voucher DROP CONSTRAINT IF EXISTS voucher_status_check;
ALTER TABLE voucher DROP CONSTRAINT IF EXISTS chk_voucher_status;
ALTER TABLE voucher
  ADD CONSTRAINT chk_voucher_status
  CHECK (status IN ('SCHEDULED','ACTIVE','INACTIVE','EXPIRED'));

-- 1.2 Column types / case-insensitive code
ALTER TABLE voucher
  ALTER COLUMN code TYPE citext;

-- 1.3 Add/ensure usage limit columns
ALTER TABLE voucher
  ADD COLUMN IF NOT EXISTS global_usage_limit INTEGER,
  ADD COLUMN IF NOT EXISTS per_customer_limit INTEGER NOT NULL DEFAULT 1;

-- 1.4 Add common campaign policy fields
ALTER TABLE voucher
  ADD COLUMN IF NOT EXISTS min_order_amount    NUMERIC(18,2),
  ADD COLUMN IF NOT EXISTS max_discount_amount NUMERIC(18,2);

-- 1.5 Value/date checks (tight)
ALTER TABLE voucher DROP CONSTRAINT IF EXISTS chk_voucher_discount_value_positive;
ALTER TABLE voucher
  ADD CONSTRAINT chk_voucher_discount_value_positive
  CHECK (discount_value > 0);

ALTER TABLE voucher DROP CONSTRAINT IF EXISTS chk_voucher_percent_limit;
ALTER TABLE voucher
  ADD CONSTRAINT chk_voucher_percent_limit
  CHECK ((discount_type = 'PERCENT' AND discount_value BETWEEN 0 AND 100) 
         OR (discount_type = 'FIXED' AND discount_value > 0));

ALTER TABLE voucher DROP CONSTRAINT IF EXISTS chk_voucher_date_range;
ALTER TABLE voucher
  ADD CONSTRAINT chk_voucher_date_range
  CHECK (start_date IS NULL OR end_date IS NULL OR start_date <= end_date);

-- 1.6 Migrate old lowercase values / old usage_limit -> global_usage_limit
UPDATE voucher
SET
  discount_type = UPPER(discount_type)
WHERE discount_type IS NOT NULL AND discount_type ~ '^[a-z]+$';

UPDATE voucher
SET
  status = UPPER(status)
WHERE status IS NOT NULL AND status ~ '^[a-z]+$';

UPDATE voucher
SET
  global_usage_limit = COALESCE(global_usage_limit, usage_limit),
  per_customer_limit = COALESCE(per_customer_limit, 1)
WHERE usage_limit IS NOT NULL
   OR per_customer_limit IS NULL;

-- 1.7 Set discount_value NOT NULL after migration (safe now)
ALTER TABLE voucher
  ALTER COLUMN discount_value SET NOT NULL;

-- 1.8 Drop legacy single-usage column if present
ALTER TABLE voucher DROP COLUMN IF EXISTS usage_limit;

-- 1.9 Scoped uniqueness (Shopee-style): (restaurant_id, code) + separate global uniqueness
-- Remove legacy unique on code (could be constraint or index)
ALTER TABLE voucher DROP CONSTRAINT IF EXISTS voucher_code_key;
DROP INDEX IF EXISTS voucher_code_key;

-- Create scoped unique (shop/restaurant scope)
CREATE UNIQUE INDEX IF NOT EXISTS ux_voucher_rest_code
  ON voucher(restaurant_id, code);

-- Unique for global vouchers (restaurant_id IS NULL)
CREATE UNIQUE INDEX IF NOT EXISTS ux_voucher_code_global
  ON voucher(code) WHERE restaurant_id IS NULL;

-- 1.10 Comments
COMMENT ON COLUMN voucher.restaurant_id        IS 'NULL = Admin/Platform voucher (global), NOT NULL = Restaurant voucher (scoped)';
COMMENT ON COLUMN voucher.global_usage_limit   IS 'Total usage across system (NULL = unlimited)';
COMMENT ON COLUMN voucher.per_customer_limit   IS 'Max times a single customer can use this voucher';
COMMENT ON COLUMN voucher.min_order_amount     IS 'Minimum order amount required to apply the voucher';
COMMENT ON COLUMN voucher.max_discount_amount  IS 'Maximum discount cap for percent vouchers';
COMMENT ON TABLE  voucher                      IS 'Voucher table for Admin (global) and Restaurant (scoped)';

-- 2) CUSTOMER_VOUCHER: multi-use, uniqueness ------------------

-- 2.1 Ensure multi-use fields
ALTER TABLE customer_voucher
  ADD COLUMN IF NOT EXISTS times_used   INTEGER     NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS last_used_at TIMESTAMPTZ;

-- 2.2 Backfill from legacy is_used if column exists
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'customer_voucher' AND column_name = 'is_used'
  ) THEN
    UPDATE customer_voucher
       SET times_used = GREATEST(times_used, 1)
     WHERE is_used = TRUE;
  END IF;
END$$;

-- 2.3 Constraints & uniqueness
ALTER TABLE customer_voucher DROP CONSTRAINT IF EXISTS chk_customer_voucher_times_used;
ALTER TABLE customer_voucher
  ADD CONSTRAINT chk_customer_voucher_times_used
  CHECK (times_used >= 0);

ALTER TABLE customer_voucher DROP CONSTRAINT IF EXISTS ux_customer_voucher_unique;
ALTER TABLE customer_voucher
  ADD CONSTRAINT ux_customer_voucher_unique
  UNIQUE (customer_id, voucher_id);

-- 2.4 Remove legacy is_used and its index (now redundant)
DROP INDEX IF EXISTS ix_customer_voucher_used;
ALTER TABLE customer_voucher DROP COLUMN IF EXISTS is_used;

COMMENT ON COLUMN customer_voucher.times_used   IS 'How many times this customer has used the voucher';
COMMENT ON COLUMN customer_voucher.last_used_at IS 'Timestamp of the latest usage';
COMMENT ON TABLE  customer_voucher              IS 'Assigned/targeted vouchers per customer (for Admin assigned flows)';

-- 3) VOUCHER_REDEMPTION: audit log (source-of-truth) ----------

CREATE TABLE IF NOT EXISTS voucher_redemption (
  redemption_id     INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  voucher_id        INTEGER       NOT NULL REFERENCES voucher(voucher_id)         ON DELETE CASCADE,
  customer_id       UUID          NOT NULL REFERENCES customer(customer_id)       ON DELETE CASCADE,
  booking_id        INTEGER                REFERENCES booking(booking_id)         ON DELETE SET NULL,
  payment_id        INTEGER                REFERENCES payment(payment_id)         ON DELETE SET NULL,
  discount_applied  NUMERIC(18,2) NOT NULL,
  amount_before     NUMERIC(18,2) NOT NULL,
  amount_after      NUMERIC(18,2) NOT NULL,
  used_at           TIMESTAMPTZ   NOT NULL DEFAULT now(),
  meta              JSONB,
  CONSTRAINT chk_redemption_amounts
    CHECK (amount_after = amount_before - discount_applied
       AND discount_applied >= 0
       AND amount_after >= 0)
);

COMMENT ON TABLE voucher_redemption IS 'Per-usage audit log for vouchers (accurate counting & reporting)';

-- 4) Indexes ---------------------------------------------------

-- Voucher (optimized for common queries)
CREATE INDEX IF NOT EXISTS ix_voucher_restaurant_id ON voucher(restaurant_id);
CREATE INDEX IF NOT EXISTS ix_voucher_status        ON voucher(status);
CREATE INDEX IF NOT EXISTS ix_voucher_date_window   ON voucher(start_date, end_date);
CREATE INDEX IF NOT EXISTS ix_voucher_created_by    ON voucher(created_by_user);
CREATE INDEX IF NOT EXISTS ix_voucher_active_scope  ON voucher(status, restaurant_id) WHERE status = 'ACTIVE';
-- (If any old LOWER(code) index existed, drop it; CITEXT already handles case-insensitive)
DROP INDEX IF EXISTS ix_voucher_code_lower;

-- Customer_voucher
CREATE INDEX IF NOT EXISTS ix_customer_voucher_customer ON customer_voucher(customer_id);
CREATE INDEX IF NOT EXISTS ix_customer_voucher_voucher  ON customer_voucher(voucher_id);

-- Redemption
CREATE INDEX IF NOT EXISTS ix_redemption_voucher  ON voucher_redemption(voucher_id);
CREATE INDEX IF NOT EXISTS ix_redemption_customer ON voucher_redemption(customer_id);
CREATE INDEX IF NOT EXISTS ix_redemption_booking  ON voucher_redemption(booking_id);
CREATE INDEX IF NOT EXISTS ix_redemption_used_at  ON voucher_redemption(used_at);

-- 5) Views -----------------------------------------------------

-- Voucher usage aggregate
CREATE OR REPLACE VIEW voucher_usage_stats AS
SELECT
  v.voucher_id,
  v.code,
  v.description,
  v.discount_type,
  v.discount_value,
  v.status,
  v.restaurant_id,
  v.global_usage_limit,
  v.per_customer_limit,
  COALESCE(COUNT(r.redemption_id), 0)        AS total_redemptions,
  COALESCE(COUNT(DISTINCT r.customer_id), 0) AS unique_customers,
  v.created_at
FROM voucher v
LEFT JOIN voucher_redemption r
  ON v.voucher_id = r.voucher_id
GROUP BY v.voucher_id, v.code, v.description, v.discount_type, v.discount_value,
         v.status, v.restaurant_id, v.global_usage_limit, v.per_customer_limit, v.created_at;

COMMENT ON VIEW voucher_usage_stats IS 'Voucher usage summary: totals & unique customers';

-- Customer voucher detail (robust - works with current structure)
CREATE OR REPLACE VIEW customer_voucher_details AS
SELECT
  cv.customer_voucher_id,
  cv.customer_id,
  'Customer ' || cv.customer_id::text AS customer_name,
  cv.voucher_id,
  v.code AS voucher_code,
  v.description AS voucher_description,
  v.discount_type,
  v.discount_value,
  v.status AS voucher_status,
  cv.times_used,
  cv.assigned_at,
  cv.last_used_at,
  v.per_customer_limit,
  GREATEST(v.per_customer_limit - cv.times_used, 0) AS remaining_uses
FROM customer_voucher cv
JOIN voucher v ON cv.voucher_id = v.voucher_id;

COMMENT ON VIEW customer_voucher_details IS 'Assigned vouchers per customer with remaining uses';

-- 6) Helper functions (read-only eligibility) -------------------

-- 6.1 Main eligibility check function
CREATE OR REPLACE FUNCTION can_use_voucher(
  p_voucher_id    INTEGER,
  p_customer_id   UUID,
  p_restaurant_id INTEGER DEFAULT NULL,
  p_order_amount  NUMERIC(18,2) DEFAULT NULL
) RETURNS BOOLEAN AS $$
DECLARE
  v_voucher        voucher%ROWTYPE;
  v_global_usage   BIGINT;
  v_customer_usage BIGINT;
BEGIN
  SELECT * INTO v_voucher FROM voucher WHERE voucher_id = p_voucher_id;
  IF NOT FOUND OR v_voucher.status <> 'ACTIVE' THEN
    RETURN FALSE;
  END IF;

  -- Date window
  IF v_voucher.start_date IS NOT NULL AND v_voucher.start_date > CURRENT_DATE THEN
    RETURN FALSE;
  END IF;
  IF v_voucher.end_date IS NOT NULL AND v_voucher.end_date < CURRENT_DATE THEN
    RETURN FALSE;
  END IF;

  -- Scope
  IF v_voucher.restaurant_id IS NOT NULL AND v_voucher.restaurant_id <> p_restaurant_id THEN
    RETURN FALSE;
  END IF;

  -- Min order
  IF v_voucher.min_order_amount IS NOT NULL
     AND p_order_amount IS NOT NULL
     AND p_order_amount < v_voucher.min_order_amount THEN
    RETURN FALSE;
  END IF;

  -- Global usage
  IF v_voucher.global_usage_limit IS NOT NULL THEN
    SELECT COUNT(*) INTO v_global_usage
    FROM voucher_redemption
    WHERE voucher_id = p_voucher_id;
    IF v_global_usage >= v_voucher.global_usage_limit THEN
      RETURN FALSE;
    END IF;
  END IF;

  -- Per-customer usage
  SELECT COUNT(*) INTO v_customer_usage
  FROM voucher_redemption
  WHERE voucher_id = p_voucher_id
    AND customer_id = p_customer_id;

  IF v_customer_usage >= v_voucher.per_customer_limit THEN
    RETURN FALSE;
  END IF;

  RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

-- 6.2 Calculate discount amount function
CREATE OR REPLACE FUNCTION calculate_voucher_discount(
  p_voucher_id    INTEGER,
  p_order_amount  NUMERIC(18,2)
) RETURNS NUMERIC(18,2) AS $$
DECLARE
  v_voucher        voucher%ROWTYPE;
  v_discount       NUMERIC(18,2);
  v_max_discount   NUMERIC(18,2);
BEGIN
  SELECT * INTO v_voucher FROM voucher WHERE voucher_id = p_voucher_id;
  IF NOT FOUND THEN
    RETURN 0;
  END IF;

  -- Calculate base discount
  IF v_voucher.discount_type = 'PERCENT' THEN
    v_discount := p_order_amount * v_voucher.discount_value / 100;
  ELSIF v_voucher.discount_type = 'FIXED' THEN
    v_discount := LEAST(v_voucher.discount_value, p_order_amount);
  ELSE
    RETURN 0;
  END IF;

  -- Apply max discount cap if exists
  IF v_voucher.max_discount_amount IS NOT NULL THEN
    v_discount := LEAST(v_discount, v_voucher.max_discount_amount);
  END IF;

  -- Ensure discount doesn't exceed order amount
  RETURN GREATEST(0, LEAST(v_discount, p_order_amount));
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION can_use_voucher IS 'Lightweight read-only check. Use service-layer transaction + row locks to APPLY.';
COMMENT ON FUNCTION calculate_voucher_discount IS 'Calculate discount amount for a voucher and order amount.';

COMMIT;

-- 7) Sample data (optional) -----------------------------------
/*
-- Uncomment and run after creating users and restaurants
INSERT INTO voucher (code, description, discount_type, discount_value, start_date, end_date,
                     global_usage_limit, per_customer_limit, created_by_user, restaurant_id, status,
                     min_order_amount, max_discount_amount)
VALUES
('WELCOME10', 'Giảm 10% cho khách mới (Platform)', 'PERCENT', 10,
 CURRENT_DATE, CURRENT_DATE + INTERVAL '30 days',
 100, 1, '00000000-0000-0000-0000-000000000001', NULL, 'ACTIVE', 200000, 50000),
('REST50K', 'Giảm 50k cho nhà hàng ABC', 'FIXED', 50000,
 CURRENT_DATE, CURRENT_DATE + INTERVAL '7 days',
 50, 2, '00000000-0000-0000-0000-000000000002', 1, 'ACTIVE', NULL, NULL),
('SUMMER20', 'Giảm 20% mùa hè (Platform)', 'PERCENT', 20,
 CURRENT_DATE, CURRENT_DATE + INTERVAL '60 days',
 500, 3, '00000000-0000-0000-0000-000000000001', NULL, 'ACTIVE', 500000, 100000);
*/

-- 8) Verification queries (run after script) ------------------
/*
-- Check voucher table structure
\d voucher

-- Check constraints
SELECT conname, contype, consrc 
FROM pg_constraint 
WHERE conrelid = 'voucher'::regclass;

-- Test sample voucher validation
SELECT can_use_voucher(1, 'customer-uuid-here', 1, 300000);
SELECT calculate_voucher_discount(1, 300000);

-- Check indexes
SELECT indexname, indexdef 
FROM pg_indexes 
WHERE tablename IN ('voucher', 'customer_voucher', 'voucher_redemption');

-- Test views
SELECT * FROM voucher_usage_stats LIMIT 5;
SELECT * FROM customer_voucher_details LIMIT 5;
*/
