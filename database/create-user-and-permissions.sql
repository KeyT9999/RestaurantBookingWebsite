-- =========================
-- TẠO USER VÀ GRANT QUYỀN CHO RESTAURANT BOOKING DATABASE
-- Chạy script này với user postgres
-- =========================

-- Tạo database nếu chưa có
CREATE DATABASE restaurant_db;

-- Tạo user restaurant_user
CREATE USER restaurant_user WITH PASSWORD '123456';

-- Grant quyền trên database
GRANT ALL PRIVILEGES ON DATABASE restaurant_db TO restaurant_user;

-- Kết nối với database restaurant_db
\c restaurant_db;

-- Grant quyền trên schema public
GRANT ALL ON SCHEMA public TO restaurant_user;

-- Grant quyền trên tất cả tables hiện tại và tương lai
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO restaurant_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO restaurant_user;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO restaurant_user;

-- Grant quyền cho các tables/sequences sẽ được tạo trong tương lai
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO restaurant_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO restaurant_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO restaurant_user;

-- Hiển thị thông tin user đã tạo
SELECT usename, usesuper, usecreatedb, usecanlogin 
FROM pg_user 
WHERE usename = 'restaurant_user';

ECHO 'User restaurant_user đã được tạo thành công với password: 123456';
ECHO 'Bây giờ bạn có thể chạy script restaurant_booking_fixed.sql'; 