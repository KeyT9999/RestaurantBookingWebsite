-- ============================================================
-- 🔧 FIX CITEXT VIEW ISSUE
-- Drop view that depends on voucher.code before app restart
-- ============================================================

BEGIN;

-- Drop the view that depends on voucher.code column
DROP VIEW IF EXISTS customer_voucher_details CASCADE;

-- Verify voucher.code is citext
SELECT column_name, data_type, udt_name 
FROM information_schema.columns 
WHERE table_name = 'voucher' AND column_name = 'code';

COMMIT;
