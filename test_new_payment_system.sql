-- Script test hệ thống chia tiền và hoàn tiền mới
-- File: test_new_payment_system.sql

-- 1. Kiểm tra tỷ lệ hoa hồng hiện tại
SELECT 
    restaurant_id,
    commission_rate,
    total_revenue,
    total_commission,
    available_balance,
    'Current commission rate' as status
FROM restaurant_balance 
ORDER BY restaurant_id;

-- 2. Test scenario: Booking hoàn thành với deposit 100,000 VNĐ
-- Giả sử có booking với deposit_amount = 100,000
-- Admin nhận: 30,000 VNĐ (30%)
-- Restaurant nhận: 70,000 VNĐ (70%)

-- Simulate completed booking
INSERT INTO booking (
    customer_id, 
    restaurant_id, 
    deposit_amount, 
    status, 
    booking_time, 
    created_at
) VALUES (
    (SELECT customer_id FROM customer LIMIT 1),
    (SELECT restaurant_id FROM restaurant_profile LIMIT 1),
    100000,
    'COMPLETED',
    now() + interval '1 day',
    now()
);

-- 3. Kiểm tra balance sau khi booking completed
SELECT 
    rb.restaurant_id,
    rb.total_revenue,
    rb.total_commission,
    rb.available_balance,
    'After booking completed' as status
FROM restaurant_balance rb
WHERE rb.restaurant_id = (SELECT restaurant_id FROM restaurant_profile LIMIT 1);

-- 4. Test scenario: Hoàn tiền 100,000 VNĐ
-- Restaurant sẽ bị trừ: 30,000 VNĐ (30% hoa hồng)
-- Customer nhận: 100,000 VNĐ từ admin

-- Simulate refund process
-- Step 1: Deduct commission from restaurant balance
UPDATE restaurant_balance 
SET 
    available_balance = available_balance - 30000,
    updated_at = now()
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile LIMIT 1);

-- Step 2: Log refund transaction
INSERT INTO payout_audit_log (
    restaurant_id,
    action_type,
    amount,
    description,
    metadata
) VALUES (
    (SELECT restaurant_id FROM restaurant_profile LIMIT 1),
    'REFUND_COMMISSION_DEDUCTION',
    -30000,
    'Commission deducted for refund of 100,000 VNĐ',
    '{"refund_amount": 100000, "commission_deducted": 30000, "customer_refund": 100000}'
);

-- 5. Kiểm tra balance sau khi hoàn tiền
SELECT 
    rb.restaurant_id,
    rb.total_revenue,
    rb.total_commission,
    rb.available_balance,
    'After refund (commission deducted)' as status
FROM restaurant_balance rb
WHERE rb.restaurant_id = (SELECT restaurant_id FROM restaurant_profile LIMIT 1);

-- 6. Test scenario: Restaurant có số dư âm
-- Nếu restaurant chỉ có 20,000 VNĐ và cần hoàn tiền 100,000 VNĐ
-- Restaurant sẽ có số dư: 20,000 - 30,000 = -10,000 VNĐ

-- Simulate negative balance scenario
UPDATE restaurant_balance 
SET 
    available_balance = 20000,
    updated_at = now()
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile LIMIT 1);

-- Deduct commission for refund (allowing negative balance)
UPDATE restaurant_balance 
SET 
    available_balance = available_balance - 30000,
    updated_at = now()
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile LIMIT 1);

-- 7. Kiểm tra số dư âm
SELECT 
    rb.restaurant_id,
    rb.total_revenue,
    rb.total_commission,
    rb.available_balance,
    CASE 
        WHEN rb.available_balance < 0 THEN 'NEGATIVE BALANCE - OK'
        ELSE 'POSITIVE BALANCE'
    END as balance_status
FROM restaurant_balance rb
WHERE rb.restaurant_id = (SELECT restaurant_id FROM restaurant_profile LIMIT 1);

-- 8. Test scenario: Restaurant rút tiền với số dư âm
-- Restaurant không thể rút tiền khi có số dư âm
-- Cần có booking mới để cộng tiền vào

-- Simulate new booking to recover from negative balance
INSERT INTO booking (
    customer_id, 
    restaurant_id, 
    deposit_amount, 
    status, 
    booking_time, 
    created_at
) VALUES (
    (SELECT customer_id FROM customer LIMIT 1),
    (SELECT restaurant_id FROM restaurant_profile LIMIT 1),
    200000,
    'COMPLETED',
    now() + interval '2 days',
    now()
);

-- 9. Kiểm tra balance sau booking mới
SELECT 
    rb.restaurant_id,
    rb.total_revenue,
    rb.total_commission,
    rb.available_balance,
    'After new booking (recovered from negative)' as status
FROM restaurant_balance rb
WHERE rb.restaurant_id = (SELECT restaurant_id FROM restaurant_profile LIMIT 1);

-- 10. Summary report
SELECT 
    'SUMMARY REPORT' as report_type,
    COUNT(*) as total_restaurants,
    SUM(total_revenue) as total_revenue_all_restaurants,
    SUM(total_commission) as total_commission_all_restaurants,
    SUM(available_balance) as total_available_balance,
    AVG(commission_rate) as average_commission_rate,
    COUNT(CASE WHEN available_balance < 0 THEN 1 END) as restaurants_with_negative_balance
FROM restaurant_balance;

-- 11. Cleanup test data
DELETE FROM booking WHERE deposit_amount IN (100000, 200000);
DELETE FROM payout_audit_log WHERE action_type = 'REFUND_COMMISSION_DEDUCTION';
