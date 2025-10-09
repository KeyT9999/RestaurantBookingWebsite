-- Kiểm tra cấu trúc các bảng liên quan
-- Chạy từng query để xem cấu trúc

-- 1. Kiểm tra cấu trúc withdrawal_request
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'withdrawal_request' 
ORDER BY ordinal_position;

-- 2. Kiểm tra cấu trúc restaurant_profile  
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'restaurant_profile' 
ORDER BY ordinal_position;

-- 3. Kiểm tra cấu trúc users
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'users' 
ORDER BY ordinal_position;

-- 4. Kiểm tra cấu trúc restaurant_balance
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'restaurant_balance' 
ORDER BY ordinal_position;
