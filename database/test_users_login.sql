-- Script test để verify login sau khi tạo lại bảng users
-- Chạy script này sau khi đã chạy recreate_users_table.sql

-- 1. Kiểm tra constraint hoạt động đúng
-- Thử insert với role không hợp lệ (sẽ fail)
-- INSERT INTO users (username, email, password, role) VALUES ('test', 'test@test.com', 'password', 'INVALID_ROLE');

-- 2. Kiểm tra các users test có thể login
SELECT 
    username, 
    email, 
    role, 
    email_verified, 
    active,
    CASE 
        WHEN password LIKE '$%' THEN 'BCrypt Encoded'
        ELSE 'Plain Text'
    END as password_status
FROM users 
WHERE username IN ('admin1', 'res1', 'customer1')
ORDER BY role, username;

-- 3. Kiểm tra tất cả users theo role
SELECT 
    role,
    COUNT(*) as user_count,
    COUNT(CASE WHEN email_verified = TRUE THEN 1 END) as verified_count,
    COUNT(CASE WHEN active = TRUE THEN 1 END) as active_count
FROM users 
GROUP BY role
ORDER BY role;

-- 4. Kiểm tra password hash format
SELECT 
    username,
    role,
    LENGTH(password) as password_length,
    CASE 
        WHEN password LIKE '$%' THEN 'BCrypt Format'
        WHEN password LIKE '$%' THEN 'BCrypt Format'
        ELSE 'Unknown Format'
    END as password_format
FROM users 
WHERE username IN ('admin1', 'res1', 'customer1');
