-- Quick test script for withdrawal data
-- Run in pgAdmin

-- 1. Create RestaurantOwner
INSERT INTO restaurant_owner (owner_id, username, email, phone, full_name, is_active, created_at, updated_at) 
VALUES (18, 'restaurant_owner', 'owner@test.com', '0123456789', 'Restaurant Owner', true, NOW(), NOW()) 
ON CONFLICT (owner_id) DO NOTHING;

-- 2. Create RestaurantProfile
INSERT INTO restaurant_profile (restaurant_id, owner_id, restaurant_name, address, phone, description, created_at, updated_at) 
VALUES (18, 18, 'Pizza Italia', '123 Test Street', '0123456789', 'Test restaurant for withdrawal', NOW(), NOW()) 
ON CONFLICT (restaurant_id) DO NOTHING;

-- 3. Create RestaurantBalance
INSERT INTO restaurant_balance (balance_id, restaurant_id, total_revenue, total_bookings_completed, commission_rate, commission_type, total_commission, total_withdrawn, pending_withdrawal, available_balance, last_calculated_at, created_at, updated_at) 
VALUES (18, 18, 20000000.00, 200, 7.50, 'PERCENTAGE', 1500000.00, 0.00, 0.00, 18500000.00, NOW(), NOW(), NOW()) 
ON CONFLICT (balance_id) DO NOTHING;

-- 4. Create RestaurantBankAccount
INSERT INTO restaurant_bank_account (account_id, restaurant_id, bank_code, bank_name, account_number, account_holder_name, is_default, is_verified, created_at, updated_at) 
VALUES (18, 18, '970422', 'MB Bank', '676868868679', 'TRAN KIM THANG', true, true, NOW(), NOW()) 
ON CONFLICT (account_id) DO NOTHING;

-- 5. Create withdrawal requests
INSERT INTO withdrawal_request (request_id, restaurant_id, bank_account_id, amount, description, status, created_at, updated_at) 
VALUES 
    ((SELECT COALESCE(MAX(request_id), 0) + 1 FROM withdrawal_request), 18, 18, 1000000.00, 'Test PENDING 1', 'PENDING', NOW(), NOW()),
    ((SELECT COALESCE(MAX(request_id), 0) + 1 FROM withdrawal_request), 18, 18, 1500000.00, 'Test PENDING 2', 'PENDING', NOW(), NOW()),
    ((SELECT COALESCE(MAX(request_id), 0) + 1 FROM withdrawal_request), 18, 18, 2000000.00, 'Test PROCESSING', 'PROCESSING', NOW(), NOW()),
    ((SELECT COALESCE(MAX(request_id), 0) + 1 FROM withdrawal_request), 18, 18, 500000.00, 'Test SUCCEEDED', 'SUCCEEDED', NOW(), NOW());

-- 6. Check results
SELECT 'WITHDRAWAL REQUESTS' as info;
SELECT request_id, restaurant_id, amount, description, status, created_at 
FROM withdrawal_request 
WHERE restaurant_id = 18 
ORDER BY created_at DESC LIMIT 10;

-- 7. Check stats
SELECT 'STATS BY STATUS' as info;
SELECT status, COUNT(*) as count, SUM(amount) as total_amount
FROM withdrawal_request 
WHERE restaurant_id = 18
GROUP BY status
ORDER BY status;
