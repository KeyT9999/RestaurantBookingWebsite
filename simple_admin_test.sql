-- Script đơn giản để test withdrawal actions
-- Chạy từng phần một

-- 1. Tạo admin user với đầy đủ thông tin
INSERT INTO users (
    id, 
    username,
    email, 
    password, 
    full_name, 
    role, 
    active, 
    email_verified,
    phone_number,
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
    '0123456789',
    NOW(),
    NOW()
) ON CONFLICT (id) DO UPDATE SET
    username = EXCLUDED.username,
    email = EXCLUDED.email,
    full_name = EXCLUDED.full_name,
    role = EXCLUDED.role,
    active = EXCLUDED.active,
    email_verified = EXCLUDED.email_verified,
    phone_number = EXCLUDED.phone_number,
    updated_at = NOW();

-- 2. Kiểm tra admin user đã tạo
SELECT id, username, email, role FROM users WHERE role = 'ADMIN';

-- 3. Kiểm tra withdrawal requests hiện tại
SELECT 
    request_id,
    restaurant_id,
    amount,
    status,
    created_at
FROM withdrawal_request 
ORDER BY created_at DESC;

-- 4. Test update một record thành SUCCEEDED
UPDATE withdrawal_request 
SET 
    status = 'SUCCEEDED',
    manual_transfer_ref = 'TEST001',
    manual_transferred_at = NOW(),
    manual_transferred_by = '00000000-0000-0000-0000-000000000001',
    manual_note = 'Test manual transfer',
    updated_at = NOW()
WHERE request_id = 5;

-- 5. Test update một record thành REJECTED
UPDATE withdrawal_request 
SET 
    status = 'REJECTED',
    reviewed_at = NOW(),
    reviewed_by_user_id = '00000000-0000-0000-0000-000000000001',
    rejection_reason = 'Test rejection',
    updated_at = NOW()
WHERE request_id = 6;

-- 6. Kiểm tra kết quả
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
