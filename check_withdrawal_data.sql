-- Kiểm tra dữ liệu withdrawal trong database
-- Chạy script này để xem có bao nhiêu withdrawal requests

-- 1. Đếm tổng số withdrawal requests
SELECT 'Total withdrawal requests:' as info, COUNT(*) as count FROM withdrawal_request;

-- 2. Đếm theo status
SELECT 'By status:' as info, status, COUNT(*) as count 
FROM withdrawal_request 
GROUP BY status 
ORDER BY status;

-- 3. Hiển thị tất cả withdrawal requests
SELECT 'All withdrawal requests:' as info;
SELECT 
    request_id,
    restaurant_id,
    amount,
    status,
    description,
    created_at
FROM withdrawal_request 
ORDER BY created_at DESC;

-- 4. Kiểm tra restaurant_bank_account
SELECT 'Bank accounts:' as info;
SELECT 
    account_id,
    restaurant_id,
    bank_code,
    account_number,
    account_holder_name
FROM restaurant_bank_account 
ORDER BY account_id;

-- 5. Kiểm tra restaurant_profile
SELECT 'Restaurant profiles:' as info;
SELECT 
    restaurant_id,
    restaurant_name,
    owner_id
FROM restaurant_profile 
WHERE restaurant_id IN (SELECT DISTINCT restaurant_id FROM withdrawal_request)
ORDER BY restaurant_id;

-- 6. Kiểm tra users (owners)
SELECT 'Owner users:' as info;
SELECT 
    u.id,
    u.full_name,
    u.email,
    rp.restaurant_id
FROM users u
JOIN restaurant_profile rp ON u.id = rp.owner_id
WHERE rp.restaurant_id IN (SELECT DISTINCT restaurant_id FROM withdrawal_request)
ORDER BY rp.restaurant_id;
