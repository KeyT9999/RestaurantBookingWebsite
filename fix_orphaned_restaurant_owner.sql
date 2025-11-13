-- Script để kiểm tra và sửa lỗi: restaurant_owner có user_id không tồn tại trong bảng users
-- Lỗi: Unable to find com.example.booking.domain.User with id 7bc1aae0-f73e-43f5-954e-0986b8bc566c

-- ============================================
-- BƯỚC 1: KIỂM TRA DỮ LIỆU LỖI
-- ============================================

-- Tìm tất cả restaurant_owner có user_id không tồn tại trong users
SELECT 
    ro.owner_id,
    ro.user_id,
    ro.owner_name,
    ro.created_at,
    'ORPHANED - User không tồn tại' AS status
FROM restaurant_owner ro
LEFT JOIN users u ON ro.user_id = u.id
WHERE u.id IS NULL;

-- Kiểm tra user_id cụ thể gây lỗi
SELECT * FROM restaurant_owner WHERE user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c';
SELECT * FROM users WHERE id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c';

-- Kiểm tra xem có restaurant_profile nào liên kết với owner bị lỗi không
SELECT 
    rp.restaurant_id,
    rp.restaurant_name,
    rp.owner_id,
    ro.user_id,
    'Có restaurant liên kết với owner bị lỗi' AS status
FROM restaurant_profile rp
INNER JOIN restaurant_owner ro ON rp.owner_id = ro.owner_id
LEFT JOIN users u ON ro.user_id = u.id
WHERE u.id IS NULL;

-- ============================================
-- BƯỚC 2: CÁC CÁCH SỬA LỖI
-- ============================================

-- CÁCH 1: XÓA restaurant_owner bị lỗi (CHỈ DÙNG NẾU KHÔNG CÓ RESTAURANT NÀO LIÊN KẾT)
-- ⚠️ CẢNH BÁO: Chỉ xóa nếu không có restaurant_profile nào liên kết với owner này
-- Kiểm tra trước:
SELECT COUNT(*) as restaurant_count 
FROM restaurant_profile 
WHERE owner_id IN (
    SELECT owner_id FROM restaurant_owner ro
    LEFT JOIN users u ON ro.user_id = u.id
    WHERE u.id IS NULL
);

-- Nếu restaurant_count = 0, có thể xóa an toàn:
-- DELETE FROM restaurant_owner 
-- WHERE owner_id IN (
--     SELECT owner_id FROM restaurant_owner ro
--     LEFT JOIN users u ON ro.user_id = u.id
--     WHERE u.id IS NULL
-- );

-- CÁCH 2: CẬP NHẬT user_id trỏ đến user hợp lệ (KHUYẾN NGHỊ)
-- Tìm một user hợp lệ (ví dụ: user có role RESTAURANT_OWNER)
SELECT id, username, email, full_name 
FROM users 
WHERE id IN (
    SELECT DISTINCT ro.user_id 
    FROM restaurant_owner ro
    WHERE ro.user_id IN (
        SELECT user_id FROM users WHERE id IS NOT NULL
    )
)
LIMIT 1;

-- Cập nhật user_id trỏ đến user hợp lệ (thay YOUR_VALID_USER_ID bằng ID thực tế)
-- UPDATE restaurant_owner 
-- SET user_id = 'YOUR_VALID_USER_ID'  -- Thay bằng user_id hợp lệ
-- WHERE user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c';

-- CÁCH 3: TẠO USER MỚI NẾU CẦN (CHỈ DÙNG NẾU BIẾT THÔNG TIN USER)
-- INSERT INTO users (id, username, email, password, full_name, role, enabled, created_at, updated_at)
-- VALUES (
--     '7bc1aae0-f73e-43f5-954e-0986b8bc566c',
--     'owner_username',  -- Thay bằng username thực tế
--     'owner@example.com',  -- Thay bằng email thực tế
--     '$2a$10$...',  -- Thay bằng password đã hash
--     'Owner Name',  -- Thay bằng tên thực tế
--     'RESTAURANT_OWNER',
--     true,
--     NOW(),
--     NOW()
-- );

-- ============================================
-- BƯỚC 3: SỬA LỖI CỤ THỂ (UUID: 7bc1aae0-f73e-43f5-954e-0986b8bc566c)
-- ============================================

-- Option A: Tìm user hợp lệ gần nhất và cập nhật
-- Bước 1: Tìm user hợp lệ
SELECT id, username, email 
FROM users 
WHERE role = 'RESTAURANT_OWNER' 
ORDER BY created_at DESC 
LIMIT 1;

-- Bước 2: Cập nhật (thay YOUR_VALID_USER_ID bằng ID từ bước 1)
-- UPDATE restaurant_owner 
-- SET user_id = 'YOUR_VALID_USER_ID'
-- WHERE user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c';

-- Option B: Xóa owner bị lỗi (CHỈ NẾU KHÔNG CÓ RESTAURANT NÀO)
-- Kiểm tra trước:
SELECT COUNT(*) as restaurant_count
FROM restaurant_profile rp
INNER JOIN restaurant_owner ro ON rp.owner_id = ro.owner_id
WHERE ro.user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c';

-- Nếu restaurant_count = 0, xóa:
-- DELETE FROM restaurant_owner 
-- WHERE user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c';

-- ============================================
-- BƯỚC 4: XÁC MINH SAU KHI SỬA
-- ============================================

-- Kiểm tra lại xem còn lỗi không
SELECT 
    COUNT(*) as orphaned_count
FROM restaurant_owner ro
LEFT JOIN users u ON ro.user_id = u.id
WHERE u.id IS NULL;

-- Nếu orphaned_count = 0, đã sửa xong!

