-- Script đơn giản để test withdrawal actions
-- Chạy từng phần một

-- 1. Xem tất cả withdrawal requests
SELECT 
    request_id,
    restaurant_id,
    amount,
    status,
    created_at,
    manual_transfer_ref,
    manual_transferred_at,
    manual_note
FROM withdrawal_request 
ORDER BY created_at DESC;

-- 2. Đếm theo status
SELECT 
    status,
    COUNT(*) as count
FROM withdrawal_request 
GROUP BY status;

-- 3. Test manual update một record thành SUCCEEDED
-- (Chỉ chạy nếu muốn test)
UPDATE withdrawal_request 
SET 
    status = 'SUCCEEDED',
    manual_transfer_ref = 'TEST001',
    manual_transferred_at = NOW(),
    manual_transferred_by = '00000000-0000-0000-0000-000000000001',
    manual_note = 'Test manual transfer',
    updated_at = NOW()
WHERE request_id = 5;

-- 4. Test manual update một record thành REJECTED  
-- (Chỉ chạy nếu muốn test)
UPDATE withdrawal_request 
SET 
    status = 'REJECTED',
    reviewed_at = NOW(),
    reviewed_by_user_id = '00000000-0000-0000-0000-000000000001',
    rejection_reason = 'Test rejection',
    updated_at = NOW()
WHERE request_id = 6;

-- 5. Kiểm tra lại sau khi update
SELECT 
    request_id,
    status,
    manual_transfer_ref,
    rejection_reason,
    updated_at
FROM withdrawal_request 
WHERE request_id IN (5, 6);
