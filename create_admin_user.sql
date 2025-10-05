-- Tạo user admin để test
INSERT INTO users (user_id, username, email, password, role, is_active, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'admin',
    'admin@bookeat.vn',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyVhUx0J8KqF0vVjqKjKjKjKjKjK', -- password: admin123
    'ADMIN',
    true,
    NOW(),
    NOW()
) ON CONFLICT (email) DO NOTHING;
