-- ============================================
-- SCRIPT SỬA LỖI: Cập nhật user_id trỏ đến user hợp lệ
-- Thứ tự thực hiện từng bước
-- ============================================

-- ============================================
-- BƯỚC 1: KIỂM TRA TÌNH TRẠNG HIỆN TẠI
-- ============================================

-- 1.1. Kiểm tra owner bị lỗi (user_id không tồn tại trong users)
SELECT 
    ro.owner_id,
    ro.user_id,
    ro.owner_name,
    'ORPHANED - User không tồn tại' AS status
FROM restaurant_owner ro
LEFT JOIN users u ON ro.user_id = u.id
WHERE ro.user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c'
   OR (ro.user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c' AND u.id IS NULL);

-- 1.2. Kiểm tra xem có restaurant nào liên kết với owner bị lỗi không
SELECT 
    rp.restaurant_id,
    rp.restaurant_name,
    rp.owner_id,
    'Có restaurant liên kết' AS status
FROM restaurant_profile rp
INNER JOIN restaurant_owner ro ON rp.owner_id = ro.owner_id
WHERE ro.user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c';

-- ============================================
-- BƯỚC 2: TÌM USER_ID HỢP LỆ CHƯA ĐƯỢC SỬ DỤNG
-- ============================================

-- 2.1. Tìm user RESTAURANT_OWNER chưa có restaurant_owner (KHUYẾN NGHỊ)
SELECT 
    u.id AS user_id,
    u.username,
    u.email,
    u.full_name,
    'CHƯA CÓ RESTAURANT_OWNER - CÓ THỂ SỬ DỤNG' AS status
FROM users u
LEFT JOIN restaurant_owner ro ON u.id = ro.user_id
WHERE u.role = 'RESTAURANT_OWNER'
  AND ro.owner_id IS NULL
ORDER BY u.created_at DESC
LIMIT 5;

-- 2.2. Nếu không có user RESTAURANT_OWNER, tìm user bất kỳ chưa được sử dụng
-- (Chỉ dùng nếu bước 2.1 không có kết quả)
SELECT 
    u.id AS user_id,
    u.username,
    u.email,
    u.full_name,
    u.role,
    'CHƯA CÓ RESTAURANT_OWNER' AS status
FROM users u
LEFT JOIN restaurant_owner ro ON u.id = ro.user_id
WHERE ro.owner_id IS NULL
ORDER BY u.created_at DESC
LIMIT 5;

-- ============================================
-- BƯỚC 3: XÁC NHẬN USER_ID SẼ SỬ DỤNG
-- ============================================

-- 3.1. Kiểm tra user_id bạn chọn có thực sự chưa được sử dụng không
-- (Thay YOUR_NEW_USER_ID bằng user_id bạn chọn từ bước 2)
SELECT 
    CASE 
        WHEN ro.owner_id IS NULL THEN 'CHƯA ĐƯỢC SỬ DỤNG - AN TOÀN'
        ELSE 'ĐÃ ĐƯỢC SỬ DỤNG - KHÔNG THỂ DÙNG'
    END AS status,
    u.id,
    u.username,
    u.email,
    ro.owner_id,
    ro.owner_name
FROM users u
LEFT JOIN restaurant_owner ro ON u.id = ro.user_id
WHERE u.id = 'YOUR_NEW_USER_ID';  -- ⚠️ THAY BẰNG USER_ID BẠN CHỌN

-- ============================================
-- BƯỚC 4: THỰC HIỆN CẬP NHẬT
-- ============================================

-- 4.1. Cập nhật user_id (THAY YOUR_NEW_USER_ID BẰNG USER_ID TỪ BƯỚC 2)
-- ⚠️ QUAN TRỌNG: Đảm bảo user_id này CHƯA được sử dụng bởi restaurant_owner nào khác
UPDATE restaurant_owner 
SET user_id = 'YOUR_NEW_USER_ID'  -- ⚠️ THAY BẰNG USER_ID HỢP LỆ CHƯA ĐƯỢC SỬ DỤNG
WHERE user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c';

-- 4.2. Kiểm tra kết quả sau khi cập nhật
SELECT 
    ro.owner_id,
    ro.user_id,
    ro.owner_name,
    u.username,
    u.email,
    'ĐÃ CẬP NHẬT THÀNH CÔNG' AS status
FROM restaurant_owner ro
INNER JOIN users u ON ro.user_id = u.id
WHERE ro.user_id = 'YOUR_NEW_USER_ID';  -- ⚠️ THAY BẰNG USER_ID BẠN ĐÃ CẬP NHẬT

-- ============================================
-- BƯỚC 5: XÁC MINH SAU KHI SỬA
-- ============================================

-- 5.1. Kiểm tra xem còn owner nào có user_id không tồn tại không
SELECT 
    COUNT(*) as orphaned_count
FROM restaurant_owner ro
LEFT JOIN users u ON ro.user_id = u.id
WHERE u.id IS NULL;

-- Nếu orphaned_count = 0, không còn lỗi! ✅

-- 5.2. Kiểm tra duplicate user_id (không được có duplicate)
SELECT 
    user_id,
    COUNT(*) as count,
    STRING_AGG(owner_id::text, ', ') as owner_ids
FROM restaurant_owner
GROUP BY user_id
HAVING COUNT(*) > 1;

-- Nếu không có kết quả, không có duplicate! ✅

-- 5.3. Kiểm tra owner bị lỗi đã được sửa chưa
SELECT 
    ro.owner_id,
    ro.user_id,
    CASE 
        WHEN u.id IS NOT NULL THEN '✅ ĐÃ SỬA - User tồn tại'
        ELSE '❌ CHƯA SỬA - User không tồn tại'
    END AS status
FROM restaurant_owner ro
LEFT JOIN users u ON ro.user_id = u.id
WHERE ro.user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c';

-- Nếu status = '✅ ĐÃ SỬA', đã sửa thành công! ✅

-- ============================================
-- TÓM TẮT CÁC BƯỚC
-- ============================================
-- 1. Chạy BƯỚC 1 để kiểm tra tình trạng
-- 2. Chạy BƯỚC 2 để tìm user_id hợp lệ
-- 3. Chạy BƯỚC 3 để xác nhận user_id an toàn
-- 4. Chạy BƯỚC 4 để cập nhật (NHỚ THAY YOUR_NEW_USER_ID)
-- 5. Chạy BƯỚC 5 để xác minh đã sửa xong
-- ============================================

