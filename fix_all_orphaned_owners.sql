-- ============================================
-- SCRIPT SỬA TẤT CẢ RESTAURANT_OWNER BỊ LỖI
-- Xử lý cả trường hợp role không phân biệt hoa thường
-- ============================================

-- ============================================
-- BƯỚC 1: TÌM USER HỢP LỆ (KHÔNG PHÂN BIỆT HOA THƯỜNG)
-- ============================================

-- 1.1. Tìm user có role restaurant_owner (không phân biệt hoa thường) chưa được sử dụng
SELECT 
    u.id AS user_id,
    u.username,
    u.email,
    u.full_name,
    u.role,
    'CHƯA CÓ RESTAURANT_OWNER - CÓ THỂ SỬ DỤNG' AS status
FROM users u
LEFT JOIN restaurant_owner ro ON u.id = ro.user_id
WHERE LOWER(u.role) = 'restaurant_owner'  -- Không phân biệt hoa thường
  AND ro.owner_id IS NULL
ORDER BY u.created_at DESC
LIMIT 10;

-- 1.2. Nếu vẫn không có, tìm user bất kỳ chưa được sử dụng (ưu tiên admin hoặc owner)
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
  AND (LOWER(u.role) IN ('restaurant_owner', 'admin', 'owner'))
ORDER BY 
    CASE LOWER(u.role)
        WHEN 'restaurant_owner' THEN 1
        WHEN 'owner' THEN 2
        WHEN 'admin' THEN 3
        ELSE 4
    END,
    u.created_at DESC
LIMIT 10;

-- ============================================
-- BƯỚC 2: KIỂM TRA TẤT CẢ OWNER BỊ LỖI
-- ============================================

-- 2.1. Liệt kê tất cả owner bị lỗi
SELECT 
    ro.owner_id,
    ro.user_id,
    ro.owner_name,
    'ORPHANED - User không tồn tại' AS status
FROM restaurant_owner ro
LEFT JOIN users u ON ro.user_id = u.id
WHERE u.id IS NULL
ORDER BY ro.owner_name;

-- 2.2. Kiểm tra owner nào có restaurant liên kết
SELECT 
    ro.owner_id,
    ro.user_id,
    ro.owner_name,
    COUNT(rp.restaurant_id) as restaurant_count,
    STRING_AGG(rp.restaurant_name, ', ') as restaurant_names
FROM restaurant_owner ro
LEFT JOIN users u ON ro.user_id = u.id
LEFT JOIN restaurant_profile rp ON ro.owner_id = rp.owner_id
WHERE u.id IS NULL
GROUP BY ro.owner_id, ro.user_id, ro.owner_name
ORDER BY restaurant_count DESC, ro.owner_name;

-- ============================================
-- BƯỚC 3: SỬA TỪNG OWNER BỊ LỖI
-- ============================================

-- 3.1. Sửa owner có restaurant (ưu tiên) - "Kim Min Joon" với restaurant "Seoul BBQ Premium"
-- Sử dụng user "owner_demo" (36ba3e53-90d8-4c8d-a331-ed88218db9fa) hoặc user khác từ bước 1

-- Kiểm tra user "owner_demo" có an toàn không
SELECT 
    CASE 
        WHEN ro.owner_id IS NULL THEN 'CHƯA ĐƯỢC SỬ DỤNG - AN TOÀN'
        ELSE 'ĐÃ ĐƯỢC SỬ DỤNG - KHÔNG THỂ DÙNG'
    END AS status,
    u.id,
    u.username,
    u.email,
    u.role
FROM users u
LEFT JOIN restaurant_owner ro ON u.id = ro.user_id
WHERE u.id = '36ba3e53-90d8-4c8d-a331-ed88218db9fa';  -- owner_demo

-- Cập nhật owner "Kim Min Joon" (có restaurant)
UPDATE restaurant_owner 
SET user_id = '36ba3e53-90d8-4c8d-a331-ed88218db9fa'  -- owner_demo
WHERE owner_id = '83376775-b03a-4830-953b-e4c2cfa6cc2a';  -- Kim Min Joon

-- 3.2. Sửa các owner khác (không có restaurant hoặc ít quan trọng)
-- Có thể dùng cùng user hoặc tạo user mới cho mỗi owner

-- Option A: Dùng cùng user cho tất cả (nếu không quan trọng)
-- UPDATE restaurant_owner 
-- SET user_id = '36ba3e53-90d8-4c8d-a331-ed88218db9fa'  -- owner_demo
-- WHERE owner_id IN (
--     SELECT owner_id FROM restaurant_owner ro
--     LEFT JOIN users u ON ro.user_id = u.id
--     WHERE u.id IS NULL
--     AND owner_id != '83376775-b03a-4830-953b-e4c2cfa6cc2a'  -- Đã sửa ở trên
-- );

-- Option B: Xóa các owner không có restaurant (nếu không cần thiết)
-- DELETE FROM restaurant_owner 
-- WHERE owner_id IN (
--     SELECT ro.owner_id
--     FROM restaurant_owner ro
--     LEFT JOIN users u ON ro.user_id = u.id
--     LEFT JOIN restaurant_profile rp ON ro.owner_id = rp.owner_id
--     WHERE u.id IS NULL
--       AND rp.restaurant_id IS NULL  -- Không có restaurant
-- );

-- ============================================
-- BƯỚC 4: XÁC MINH SAU KHI SỬA
-- ============================================

-- 4.1. Kiểm tra còn owner nào bị lỗi không
SELECT 
    COUNT(*) as orphaned_count,
    'Còn owner bị lỗi' AS status
FROM restaurant_owner ro
LEFT JOIN users u ON ro.user_id = u.id
WHERE u.id IS NULL;

-- Phải = 0 nếu đã sửa hết

-- 4.2. Kiểm tra duplicate user_id
SELECT 
    user_id,
    COUNT(*) as count,
    STRING_AGG(owner_id::text, ', ') as owner_ids
FROM restaurant_owner
GROUP BY user_id
HAVING COUNT(*) > 1;

-- Không được có duplicate

-- 4.3. Kiểm tra restaurant "Seoul BBQ Premium" đã được sửa chưa
SELECT 
    rp.restaurant_id,
    rp.restaurant_name,
    ro.owner_id,
    ro.owner_name,
    ro.user_id,
    u.username,
    CASE 
        WHEN u.id IS NOT NULL THEN '✅ ĐÃ SỬA - User tồn tại'
        ELSE '❌ CHƯA SỬA - User không tồn tại'
    END AS status
FROM restaurant_profile rp
INNER JOIN restaurant_owner ro ON rp.owner_id = ro.owner_id
LEFT JOIN users u ON ro.user_id = u.id
WHERE rp.restaurant_id = 31;  -- Seoul BBQ Premium

-- ============================================
-- GIẢI PHÁP TỰ ĐỘNG: SỬA TẤT CẢ OWNER BỊ LỖI
-- ============================================

-- Script tự động tìm user hợp lệ và cập nhật cho tất cả owner bị lỗi
DO $$
DECLARE
    valid_user_id UUID;
    orphaned_owner RECORD;
BEGIN
    -- Tìm user hợp lệ (không phân biệt hoa thường)
    SELECT u.id INTO valid_user_id
    FROM users u
    LEFT JOIN restaurant_owner ro ON u.id = ro.user_id
    WHERE LOWER(u.role) = 'restaurant_owner'
      AND ro.owner_id IS NULL
    ORDER BY u.created_at DESC
    LIMIT 1;
    
    -- Nếu không có, tìm user bất kỳ
    IF valid_user_id IS NULL THEN
        SELECT u.id INTO valid_user_id
        FROM users u
        LEFT JOIN restaurant_owner ro ON u.id = ro.user_id
        WHERE ro.owner_id IS NULL
          AND LOWER(u.role) IN ('restaurant_owner', 'admin', 'owner')
        ORDER BY u.created_at DESC
        LIMIT 1;
    END IF;
    
    IF valid_user_id IS NULL THEN
        RAISE NOTICE 'Không tìm thấy user hợp lệ để sử dụng';
        RETURN;
    END IF;
    
    RAISE NOTICE 'Sử dụng user_id: %', valid_user_id;
    
    -- Cập nhật tất cả owner bị lỗi
    FOR orphaned_owner IN 
        SELECT ro.owner_id, ro.owner_name
        FROM restaurant_owner ro
        LEFT JOIN users u ON ro.user_id = u.id
        WHERE u.id IS NULL
    LOOP
        UPDATE restaurant_owner 
        SET user_id = valid_user_id
        WHERE owner_id = orphaned_owner.owner_id;
        
        RAISE NOTICE 'Đã cập nhật owner: % (owner_id: %)', 
            orphaned_owner.owner_name, orphaned_owner.owner_id;
    END LOOP;
    
    RAISE NOTICE 'Hoàn thành!';
END $$;

-- ============================================
-- TÓM TẮT CÁC BƯỚC
-- ============================================
-- 1. Chạy BƯỚC 1 để tìm user hợp lệ (không phân biệt hoa thường)
-- 2. Chạy BƯỚC 2 để xem tất cả owner bị lỗi
-- 3. Chạy BƯỚC 3 để sửa từng owner (hoặc dùng script tự động ở cuối)
-- 4. Chạy BƯỚC 4 để xác minh
-- ============================================

