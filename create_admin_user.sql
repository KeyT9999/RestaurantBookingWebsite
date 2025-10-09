-- Tạo admin user để fix foreign key constraint
-- Chạy script này trước khi test

-- 1. Kiểm tra xem có admin user nào không
SELECT id, email, role FROM users WHERE role = 'ADMIN';

-- 2. Tạo admin user nếu chưa có
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

-- 3. Kiểm tra lại
SELECT id, email, role FROM users WHERE role = 'ADMIN';
