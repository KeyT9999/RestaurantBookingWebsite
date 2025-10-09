-- Tạo thêm dữ liệu test để có đủ 5 withdrawal requests

-- 1. Tạo thêm restaurant profiles
INSERT INTO restaurant_profile (restaurant_id, restaurant_name, owner_id) VALUES
(19, 'Burger King', '550e8400-e29b-41d4-a716-446655440001'),
(20, 'KFC', '550e8400-e29b-41d4-a716-446655440002'),
(21, 'McDonald''s', '550e8400-e29b-41d4-a716-446655440003'),
(22, 'Subway', '550e8400-e29b-41d4-a716-446655440004')
ON CONFLICT (restaurant_id) DO NOTHING;

-- 2. Tạo thêm users (owners)
INSERT INTO users (id, username, email, full_name, phone_number, role) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'burger_owner', 'burger@example.com', 'Nguyễn Văn Burger', '0123456789', 'RESTAURANT_OWNER'),
('550e8400-e29b-41d4-a716-446655440002', 'kfc_owner', 'kfc@example.com', 'Trần Văn KFC', '0123456790', 'RESTAURANT_OWNER'),
('550e8400-e29b-41d4-a716-446655440003', 'mcd_owner', 'mcd@example.com', 'Lê Văn McDonald', '0123456791', 'RESTAURANT_OWNER'),
('550e8400-e29b-41d4-a716-446655440004', 'subway_owner', 'subway@example.com', 'Phạm Văn Subway', '0123456792', 'RESTAURANT_OWNER')
ON CONFLICT (id) DO NOTHING;

-- 3. Tạo thêm restaurant balances
INSERT INTO restaurant_balance (restaurant_id, total_revenue, available_balance) VALUES
(19, 3000000, 2700000),
(20, 4000000, 3600000),
(21, 5000000, 4500000),
(22, 2500000, 2250000)
ON CONFLICT (restaurant_id) DO NOTHING;

-- 4. Tạo thêm bank accounts
INSERT INTO restaurant_bank_account (restaurant_id, bank_code, account_number, account_holder_name) VALUES
(19, '970436', '1111111111', 'NGUYEN VAN BURGER'),
(20, '970422', '2222222222', 'TRAN VAN KFC'),
(21, '970407', '3333333333', 'LE VAN MCDONALD'),
(22, '970418', '4444444444', 'PHAM VAN SUBWAY')
ON CONFLICT (restaurant_id, account_number) DO NOTHING;

-- 5. Tạo thêm withdrawal requests
INSERT INTO withdrawal_request (restaurant_id, bank_account_id, amount, description, status) VALUES
(19, (SELECT account_id FROM restaurant_bank_account WHERE restaurant_id = 19 LIMIT 1), 500000, 'Rút tiền tháng 12 - Burger King', 'PENDING'),
(20, (SELECT account_id FROM restaurant_bank_account WHERE restaurant_id = 20 LIMIT 1), 800000, 'Rút tiền tháng 12 - KFC', 'PENDING'),
(21, (SELECT account_id FROM restaurant_bank_account WHERE restaurant_id = 21 LIMIT 1), 1200000, 'Rút tiền tháng 12 - McDonald''s', 'PENDING'),
(22, (SELECT account_id FROM restaurant_bank_account WHERE restaurant_id = 22 LIMIT 1), 300000, 'Rút tiền tháng 12 - Subway', 'PENDING');

-- 6. Kiểm tra kết quả
SELECT 'Created withdrawal requests:' as info, COUNT(*) as count FROM withdrawal_request WHERE status = 'PENDING';

SELECT 'All withdrawal requests:' as info;
SELECT 
    wr.request_id,
    rp.restaurant_name,
    wr.amount,
    wr.status,
    wr.description,
    wr.created_at
FROM withdrawal_request wr
JOIN restaurant_profile rp ON wr.restaurant_id = rp.restaurant_id
ORDER BY wr.created_at DESC;
