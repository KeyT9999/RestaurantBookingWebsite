-- Script test withdrawal actions với cấu trúc bảng đúng
-- Chạy từng phần một

-- 1. Xem tất cả withdrawal requests hiện tại
SELECT 
    request_id,
    restaurant_id,
    amount,
    status,
    created_at,
    manual_transfer_ref,
    manual_transferred_at,
    manual_note,
    rejection_reason
FROM withdrawal_request 
ORDER BY created_at DESC;

-- 2. Đếm theo status
SELECT 
    status,
    COUNT(*) as count,
    SUM(amount) as total_amount
FROM withdrawal_request 
GROUP BY status
ORDER BY status;

-- 3. Test update một record thành SUCCEEDED (chạy nếu muốn test)
-- UPDATE withdrawal_request 
-- SET 
--     status = 'SUCCEEDED',
--     manual_transfer_ref = 'TEST001',
--     manual_transferred_at = NOW(),
--     manual_transferred_by = '00000000-0000-0000-0000-000000000001',
--     manual_note = 'Test manual transfer',
--     updated_at = NOW()
-- WHERE request_id = 5;

-- 4. Test update một record thành REJECTED (chạy nếu muốn test)
-- UPDATE withdrawal_request 
-- SET 
--     status = 'REJECTED',
--     reviewed_at = NOW(),
--     reviewed_by_user_id = '00000000-0000-0000-0000-000000000001',
--     rejection_reason = 'Test rejection',
--     updated_at = NOW()
-- WHERE request_id = 6;

-- 5. Kiểm tra restaurant balance
SELECT 
    restaurant_id,
    available_balance,
    pending_withdrawal,
    total_withdrawn
FROM restaurant_balance
ORDER BY restaurant_id;
