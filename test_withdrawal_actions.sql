-- Test script để kiểm tra withdrawal actions
-- Chạy script này để xem dữ liệu hiện tại

-- 1. Kiểm tra tất cả withdrawal requests
SELECT 
    wr.id,
    wr.amount,
    wr.status,
    wr.created_at,
    wr.manual_transfer_ref,
    wr.manual_transferred_at,
    wr.manual_transferred_by,
    wr.manual_note,
    rp.restaurant_name,
    u.email as owner_email
FROM withdrawal_request wr
LEFT JOIN restaurant_profile rp ON wr.restaurant_id = rp.restaurant_id
LEFT JOIN users u ON wr.restaurant_id::text = u.id::text
ORDER BY wr.created_at DESC;

-- 2. Kiểm tra số lượng theo status
SELECT 
    status,
    COUNT(*) as count,
    SUM(amount) as total_amount
FROM withdrawal_request 
GROUP BY status
ORDER BY status;

-- 3. Kiểm tra restaurant balance
SELECT 
    rb.restaurant_id,
    rp.restaurant_name,
    rb.available_balance,
    rb.pending_withdrawal,
    rb.total_withdrawn
FROM restaurant_balance rb
LEFT JOIN restaurant_profile rp ON rb.restaurant_id = rp.restaurant_id
ORDER BY rb.restaurant_id;

-- 4. Test update một withdrawal request thành SUCCEEDED
-- (Chỉ chạy nếu muốn test)
/*
UPDATE withdrawal_request 
SET 
    status = 'SUCCEEDED',
    manual_transfer_ref = 'TEST001',
    manual_transferred_at = NOW(),
    manual_transferred_by = '00000000-0000-0000-0000-000000000001',
    manual_note = 'Test manual transfer',
    updated_at = NOW()
WHERE id = 5;
*/

-- 5. Test update một withdrawal request thành REJECTED
-- (Chỉ chạy nếu muốn test)
/*
UPDATE withdrawal_request 
SET 
    status = 'REJECTED',
    reviewed_at = NOW(),
    reviewed_by_user_id = '00000000-0000-0000-0000-000000000001',
    rejection_reason = 'Test rejection',
    updated_at = NOW()
WHERE id = 6;
*/
