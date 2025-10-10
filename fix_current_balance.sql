image.png-- Fix balance hiện tại dựa trên withdrawal requests thực tế
UPDATE restaurant_balance 
SET 
    total_withdrawn = (
        SELECT COALESCE(SUM(amount), 0) 
        FROM withdrawal_request 
        WHERE restaurant_id = 18 AND status = 'SUCCEEDED'
    ),
    pending_withdrawal = (
        SELECT COALESCE(SUM(amount), 0) 
        FROM withdrawal_request 
        WHERE restaurant_id = 18 AND status = 'PENDING'
    ),
    total_withdrawal_requests = (
        SELECT COUNT(*) 
        FROM withdrawal_request 
        WHERE restaurant_id = 18
    ),
    last_withdrawal_at = (
        SELECT MAX(reviewed_at) 
        FROM withdrawal_request 
        WHERE restaurant_id = 18 AND status = 'SUCCEEDED'
    ),
    updated_at = now()
WHERE restaurant_id = 18;

-- Recalculate available balance
SELECT calculate_available_balance(18);

-- Kiểm tra kết quả
SELECT 
    'FIXED BALANCE' as status,
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

-- Verify calculation
SELECT 
    'VERIFICATION' as status,
    (SELECT total_withdrawn FROM restaurant_balance WHERE restaurant_id = 18) as db_total_withdrawn,
    (SELECT SUM(amount) FROM withdrawal_request WHERE restaurant_id = 18 AND status = 'SUCCEEDED') as calc_total_withdrawn,
    (SELECT pending_withdrawal FROM restaurant_balance WHERE restaurant_id = 18) as db_pending_withdrawal,
    (SELECT SUM(amount) FROM withdrawal_request WHERE restaurant_id = 18 AND status = 'PENDING') as calc_pending_withdrawal;
