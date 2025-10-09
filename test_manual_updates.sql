-- Test manual update withdrawal requests
-- Chạy từng phần để test

-- 1. Test update ID 5 thành SUCCEEDED
UPDATE withdrawal_request 
SET 
    status = 'SUCCEEDED',
    manual_transfer_ref = 'TEST001',
    manual_transferred_at = NOW(),
    manual_transferred_by = '00000000-0000-0000-0000-000000000001',
    manual_note = 'Test manual transfer - ID 5',
    updated_at = NOW()
WHERE request_id = 5;

-- 2. Test update ID 6 thành REJECTED
UPDATE withdrawal_request 
SET 
    status = 'REJECTED',
    reviewed_at = NOW(),
    reviewed_by_user_id = '00000000-0000-0000-0000-000000000001',
    rejection_reason = 'Test rejection - ID 6',
    updated_at = NOW()
WHERE request_id = 6;

-- 3. Kiểm tra kết quả
SELECT 
    request_id,
    status,
    amount,
    manual_transfer_ref,
    rejection_reason,
    updated_at
FROM withdrawal_request 
WHERE request_id IN (5, 6, 7, 8, 9)
ORDER BY request_id;

-- 4. Kiểm tra restaurant balance sau khi update
SELECT 
    restaurant_id,
    available_balance,
    pending_withdrawal,
    total_withdrawn
FROM restaurant_balance
WHERE restaurant_id = 18;
