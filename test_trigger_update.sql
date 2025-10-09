-- Test trigger bằng cách update withdrawal status
-- Trước khi update
SELECT 
    'BEFORE UPDATE' as status,
    request_id,
    amount,
    status,
    created_at
FROM withdrawal_request 
WHERE request_id = 5;

SELECT 
    'BEFORE UPDATE BALANCE' as status,
    total_withdrawn,
    pending_withdrawal,
    available_balance
FROM restaurant_balance 
WHERE restaurant_id = 18;

-- Force update để test trigger
UPDATE withdrawal_request 
SET status = 'SUCCEEDED', 
    reviewed_at = now(),
    manual_transferred_at = now(),
    manual_transferred_by = '00000000-0000-0000-0000-000000000001'::uuid
WHERE request_id = 5 AND status = 'SUCCEEDED';

-- Sau khi update
SELECT 
    'AFTER UPDATE' as status,
    request_id,
    amount,
    status,
    reviewed_at,
    manual_transferred_at
FROM withdrawal_request 
WHERE request_id = 5;

SELECT 
    'AFTER UPDATE BALANCE' as status,
    total_withdrawn,
    pending_withdrawal,
    available_balance,
    updated_at
FROM restaurant_balance 
WHERE restaurant_id = 18;
