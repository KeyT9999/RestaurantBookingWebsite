-- Kiểm tra cấu trúc bảng withdrawal_request
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'withdrawal_request' 
ORDER BY ordinal_position;
