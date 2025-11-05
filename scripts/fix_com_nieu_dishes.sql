-- =====================================================
-- SQL Script: Fix món ăn không hiển thị
-- =====================================================

-- 1. Kiểm tra approval status và update nếu cần
UPDATE restaurant_profile
SET approval_status = 'APPROVED',
    approved_at = NOW(),
    approved_by = (SELECT id FROM users WHERE username = 'admin' LIMIT 1)
WHERE restaurant_name LIKE '%Cơm niêu 3 Cá Bống%'
  AND approval_status = 'PENDING';

-- 2. Đảm bảo tất cả món ăn có status = 'AVAILABLE'
UPDATE dish
SET status = 'AVAILABLE'
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Cơm niêu 3 Cá Bống%' LIMIT 1)
  AND status != 'AVAILABLE';

-- 3. Kiểm tra lại nhà hàng
SELECT 
    'Nhà hàng' as loai,
    restaurant_id,
    restaurant_name,
    approval_status,
    CASE 
        WHEN approval_status = 'APPROVED' THEN '✅ Đã duyệt'
        ELSE '❌ Chưa duyệt'
    END as status_desc
FROM restaurant_profile
WHERE restaurant_name LIKE '%Cơm niêu 3 Cá Bống%';

-- 3b. Kiểm tra lại món ăn
SELECT 
    'Món ăn' as loai,
    dish_id,
    name,
    status,
    price,
    CASE 
        WHEN status = 'AVAILABLE' THEN '✅ Có sẵn'
        ELSE '❌ ' || status
    END as status_desc
FROM dish
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Cơm niêu 3 Cá Bống%' LIMIT 1)
ORDER BY dish_id;

-- 4. Tổng kết
SELECT 
    'Tổng món ăn (AVAILABLE)' as thong_tin,
    COUNT(*) as so_luong
FROM dish
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Cơm niêu 3 Cá Bống%' LIMIT 1)
  AND status = 'AVAILABLE';

