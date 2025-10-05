-- Debug script to check vouchers in database
SELECT 
    v.voucher_id,
    v.code,
    v.description,
    v.discount_type,
    v.discount_value,
    v.status,
    v.global_usage_limit,
    v.per_customer_limit,
    v.start_date,
    v.end_date,
    v.created_at,
    v.restaurant_id
FROM voucher v 
WHERE v.restaurant_id = 16
ORDER BY v.created_at DESC;

-- Check total count
SELECT COUNT(*) as total_vouchers FROM voucher WHERE restaurant_id = 16;

-- Check all restaurants
SELECT DISTINCT restaurant_id FROM voucher WHERE restaurant_id IS NOT NULL;
