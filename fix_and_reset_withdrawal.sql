-- Script để fix và test withdrawal system
-- Chạy từng phần một

-- 1. Tạo admin user nếu chưa có
INSERT INTO users (
    id, 
    username,
    email, 
    password, 
    full_name, 
    role, 
    active, 
    email_verified,
    created_at,
    updated_at
) VALUES (
    '00000000-0000-0000-0000-000000000001',
    'admin',
    'admin@test.com',
    '$2a$10$dummy.password.hash',
    'Test Admin',
    'ADMIN',
    true,
    true,
    NOW(),
    NOW()
) ON CONFLICT (id) DO NOTHING;

-- 2. Kiểm tra withdrawal requests hiện tại
SELECT 
    request_id,
    restaurant_id,
    amount,
    status,
    created_at
FROM withdrawal_request 
ORDER BY created_at DESC;

-- 3. Kiểm tra restaurant balance
SELECT 
    restaurant_id,
    available_balance,
    pending_withdrawal,
    total_withdrawn
FROM restaurant_balance
ORDER BY restaurant_id;

-- 4. Reset tất cả withdrawal về PENDING để test lại
UPDATE withdrawal_request 
SET 
    status = 'PENDING',
    reviewed_at = NULL,
    reviewed_by_user_id = NULL,
    rejection_reason = NULL,
    manual_transfer_ref = NULL,
    manual_transferred_at = NULL,
    manual_transferred_by = NULL,
    manual_note = NULL,
    updated_at = NOW()
WHERE request_id IN (5, 6, 7, 8, 9);

-- 5. Kiểm tra lại sau khi reset
SELECT 
    request_id,
    status,
    amount
FROM withdrawal_request 
WHERE request_id IN (5, 6, 7, 8, 9)
ORDER BY request_id;
