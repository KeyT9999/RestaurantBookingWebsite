-- Kiểm tra dữ liệu balance và withdrawal
SELECT 
    '=== RESTAURANT BALANCE ===' as section,
    restaurant_id,
    total_revenue,
    total_commission,
    available_balance,
    total_withdrawn,
    pending_withdrawal,
    total_withdrawal_requests,
    last_withdrawal_at,
    updated_at
FROM restaurant_balance 
WHERE restaurant_id = 18;

SELECT 
    '=== WITHDRAWAL REQUESTS ===' as section,
    request_id,
    restaurant_id,
    amount,
    status,
    created_at,
    reviewed_at,
    manual_transferred_at
FROM withdrawal_request 
WHERE restaurant_id = 18
ORDER BY request_id;

-- Tính toán thủ công để kiểm tra
SELECT 
    '=== CALCULATION CHECK ===' as section,
    (SELECT total_withdrawn FROM restaurant_balance WHERE restaurant_id = 18) as db_total_withdrawn,
    (SELECT SUM(amount) FROM withdrawal_request WHERE restaurant_id = 18 AND status = 'SUCCEEDED') as calc_total_withdrawn,
    (SELECT pending_withdrawal FROM restaurant_balance WHERE restaurant_id = 18) as db_pending_withdrawal,
    (SELECT SUM(amount) FROM withdrawal_request WHERE restaurant_id = 18 AND status = 'PENDING') as calc_pending_withdrawal;
