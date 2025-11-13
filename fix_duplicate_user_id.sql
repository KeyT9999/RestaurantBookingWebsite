-- Script để sửa lỗi: duplicate key value violates unique constraint "restaurant_owner_user_id_key"
-- Lỗi: user_id 'ae9a0000-67a2-470e-a445-7d81b2bf75cf' đã được sử dụng bởi restaurant_owner khác

-- ============================================
-- BƯỚC 1: KIỂM TRA TÌNH TRẠNG HIỆN TẠI
-- ============================================

-- Kiểm tra user_id bị lỗi (không tồn tại trong users)
SELECT 
    ro.owner_id,
    ro.user_id,
    ro.owner_name,
    'ORPHANED - User không tồn tại' AS status
FROM restaurant_owner ro
LEFT JOIN users u ON ro.user_id = u.id
WHERE ro.user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c'
   OR u.id IS NULL;

-- Kiểm tra user_id đang được sử dụng bởi restaurant_owner nào
SELECT 
    ro.owner_id,
    ro.user_id,
    ro.owner_name,
    u.username,
    u.email,
    'ĐANG ĐƯỢC SỬ DỤNG' AS status
FROM restaurant_owner ro
INNER JOIN users u ON ro.user_id = u.id
WHERE ro.user_id = 'ae9a0000-67a2-470e-a445-7d81b2bf75cf';

-- Kiểm tra xem có restaurant nào liên kết với owner bị lỗi không
SELECT 
    rp.restaurant_id,
    rp.restaurant_name,
    rp.owner_id as orphaned_owner_id,
    ro.user_id as orphaned_user_id,
    'Có restaurant liên kết với owner bị lỗi' AS status
FROM restaurant_profile rp
INNER JOIN restaurant_owner ro ON rp.owner_id = ro.owner_id
WHERE ro.user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c';

-- ============================================
-- BƯỚC 2: TÌM USER_ID HỢP LỆ CHƯA ĐƯỢC SỬ DỤNG
-- ============================================

-- Tìm tất cả users có role RESTAURANT_OWNER nhưng chưa có restaurant_owner
SELECT 
    u.id,
    u.username,
    u.email,
    u.full_name,
    'CHƯA CÓ RESTAURANT_OWNER' AS status
FROM users u
LEFT JOIN restaurant_owner ro ON u.id = ro.user_id
WHERE u.role = 'RESTAURANT_OWNER'
  AND ro.owner_id IS NULL
ORDER BY u.created_at DESC;

-- Hoặc tìm user bất kỳ chưa được sử dụng (nếu cần)
SELECT 
    u.id,
    u.username,
    u.email,
    u.full_name,
    u.role,
    'CHƯA CÓ RESTAURANT_OWNER' AS status
FROM users u
LEFT JOIN restaurant_owner ro ON u.id = ro.user_id
WHERE ro.owner_id IS NULL
ORDER BY u.created_at DESC
LIMIT 10;

-- ============================================
-- BƯỚC 3: CÁC CÁCH SỬA LỖI
-- ============================================

-- CÁCH 1: TÌM USER_ID HỢP LỆ KHÁC VÀ CẬP NHẬT (KHUYẾN NGHỊ)
-- Bước 1: Tìm user hợp lệ chưa được sử dụng
SELECT 
    u.id,
    u.username,
    u.email
FROM users u
LEFT JOIN restaurant_owner ro ON u.id = ro.user_id
WHERE u.role = 'RESTAURANT_OWNER'
  AND ro.owner_id IS NULL
ORDER BY u.created_at DESC
LIMIT 1;

-- Bước 2: Cập nhật với user_id hợp lệ (thay YOUR_NEW_USER_ID bằng ID từ bước 1)
-- UPDATE restaurant_owner 
-- SET user_id = 'YOUR_NEW_USER_ID'  -- Thay bằng user_id hợp lệ CHƯA được sử dụng
-- WHERE user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c';

-- CÁCH 2: XÓA RESTAURANT_OWNER BỊ LỖI (CHỈ NẾU KHÔNG CÓ RESTAURANT NÀO)
-- ⚠️ CẢNH BÁO: Chỉ xóa nếu không có restaurant_profile nào liên kết

-- Bước 1: Kiểm tra xem có restaurant nào liên kết không
SELECT COUNT(*) as restaurant_count
FROM restaurant_profile rp
INNER JOIN restaurant_owner ro ON rp.owner_id = ro.owner_id
WHERE ro.user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c';

-- Bước 2: Nếu restaurant_count = 0, có thể xóa an toàn
-- DELETE FROM restaurant_owner 
-- WHERE user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c';

-- CÁCH 3: MERGE RESTAURANT_OWNER (NẾU CẦN GỘP NHIỀU RESTAURANT VÀO 1 OWNER)
-- Nếu owner bị lỗi có restaurant, và bạn muốn gộp vào owner đang dùng user_id hợp lệ

-- Bước 1: Kiểm tra restaurants của owner bị lỗi
SELECT 
    rp.restaurant_id,
    rp.restaurant_name,
    rp.owner_id as current_owner_id
FROM restaurant_profile rp
WHERE rp.owner_id IN (
    SELECT owner_id FROM restaurant_owner 
    WHERE user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c'
);

-- Bước 2: Tìm owner hợp lệ để merge vào
SELECT 
    ro.owner_id,
    ro.user_id,
    u.username,
    u.email
FROM restaurant_owner ro
INNER JOIN users u ON ro.user_id = u.id
WHERE ro.user_id = 'ae9a0000-67a2-470e-a445-7d81b2bf75cf';  -- Owner đang dùng user_id này

-- Bước 3: Cập nhật restaurant_profile để trỏ đến owner hợp lệ
-- (Thay YOUR_VALID_OWNER_ID bằng owner_id từ bước 2)
-- UPDATE restaurant_profile
-- SET owner_id = 'YOUR_VALID_OWNER_ID'
-- WHERE owner_id IN (
--     SELECT owner_id FROM restaurant_owner 
--     WHERE user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c'
-- );

-- Bước 4: Sau khi merge xong, xóa owner bị lỗi
-- DELETE FROM restaurant_owner 
-- WHERE user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c';

-- CÁCH 4: TẠO USER MỚI (CHỈ DÙNG NẾU BIẾT THÔNG TIN USER)
-- Nếu bạn biết thông tin user cần tạo và muốn giữ UUID cũ
-- INSERT INTO users (id, username, email, password, full_name, role, enabled, created_at, updated_at)
-- VALUES (
--     '7bc1aae0-f73e-43f5-954e-0986b8bc566c',
--     'owner_username',  -- Thay bằng username thực tế
--     'owner@example.com',  -- Thay bằng email thực tế
--     '$2a$10$...',  -- Thay bằng password đã hash (dùng BCrypt)
--     'Owner Name',  -- Thay bằng tên thực tế
--     'RESTAURANT_OWNER',
--     true,
--     NOW(),
--     NOW()
-- );

-- ============================================
-- BƯỚC 4: SỬA LỖI CỤ THỂ - GIẢI PHÁP TỰ ĐỘNG
-- ============================================

-- Tự động tìm user hợp lệ và cập nhật
-- Option A: Tìm user RESTAURANT_OWNER chưa được sử dụng
DO $$
DECLARE
    new_user_id UUID;
    orphaned_owner_id UUID;
BEGIN
    -- Tìm user hợp lệ chưa được sử dụng
    SELECT u.id INTO new_user_id
    FROM users u
    LEFT JOIN restaurant_owner ro ON u.id = ro.user_id
    WHERE u.role = 'RESTAURANT_OWNER'
      AND ro.owner_id IS NULL
    ORDER BY u.created_at DESC
    LIMIT 1;
    
    -- Lấy owner_id của owner bị lỗi
    SELECT owner_id INTO orphaned_owner_id
    FROM restaurant_owner
    WHERE user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c';
    
    IF new_user_id IS NOT NULL AND orphaned_owner_id IS NOT NULL THEN
        -- Cập nhật user_id
        UPDATE restaurant_owner 
        SET user_id = new_user_id
        WHERE owner_id = orphaned_owner_id;
        
        RAISE NOTICE 'Đã cập nhật owner_id % với user_id %', orphaned_owner_id, new_user_id;
    ELSE
        RAISE NOTICE 'Không tìm thấy user hợp lệ hoặc owner bị lỗi';
    END IF;
END $$;

-- ============================================
-- BƯỚC 5: XÁC MINH SAU KHI SỬA
-- ============================================

-- Kiểm tra lại xem còn lỗi không
SELECT 
    COUNT(*) as orphaned_count
FROM restaurant_owner ro
LEFT JOIN users u ON ro.user_id = u.id
WHERE u.id IS NULL;

-- Kiểm tra duplicate user_id
SELECT 
    user_id,
    COUNT(*) as count
FROM restaurant_owner
GROUP BY user_id
HAVING COUNT(*) > 1;

-- Nếu cả hai query trên đều trả về 0, đã sửa xong! ✅

