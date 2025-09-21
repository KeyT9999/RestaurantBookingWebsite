-- =========================
-- FIX PERMISSIONS FOR RESTAURANT_USER
-- Chạy script này với user postgres
-- =========================

-- Kết nối với database
\c restaurant_db;

-- Grant quyền cho user restaurant_user trên tất cả tables hiện tại
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO restaurant_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO restaurant_user;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO restaurant_user;

-- Grant quyền cho tables/sequences sẽ được tạo trong tương lai
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO restaurant_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO restaurant_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO restaurant_user;

-- Kiểm tra permissions
SELECT 
    schemaname,
    tablename,
    tableowner,
    hasinsert,
    hasselect,
    hasupdate,
    hasdelete
FROM pg_tables t
LEFT JOIN information_schema.table_privileges p 
    ON t.tablename = p.table_name 
    AND p.grantee = 'restaurant_user'
WHERE schemaname = 'public';

-- Hiển thị kết quả
SELECT 'Permissions granted successfully for restaurant_user!' as status; 