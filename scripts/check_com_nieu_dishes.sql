-- =====================================================
-- SQL Script: Kiểm tra món ăn và status của nhà hàng
-- =====================================================

-- 1. Kiểm tra approval status của nhà hàng
SELECT 
    restaurant_id,
    restaurant_name,
    approval_status,
    CASE 
        WHEN approval_status = 'APPROVED' THEN '✅ Đã duyệt'
        WHEN approval_status = 'PENDING' THEN '⚠️  Chờ duyệt'
        WHEN approval_status = 'REJECTED' THEN '❌ Bị từ chối'
        ELSE '❓ Khác'
    END as status_description
FROM restaurant_profile
WHERE restaurant_name LIKE '%Cơm niêu 3 Cá Bống%';

-- 2. Kiểm tra món ăn và status
SELECT 
    dish_id,
    name,
    status,
    price,
    category,
    CASE 
        WHEN status = 'AVAILABLE' THEN '✅ Có sẵn'
        WHEN status = 'OUT_OF_STOCK' THEN '⚠️  Hết hàng'
        WHEN status = 'DISCONTINUED' THEN '❌ Ngừng phục vụ'
        ELSE '❓ Khác: ' || status
    END as status_description
FROM dish
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Cơm niêu 3 Cá Bống%' LIMIT 1)
ORDER BY dish_id;

-- 3. Kiểm tra ảnh của món ăn
SELECT 
    rm.media_id,
    rm.type,
    rm.url,
    d.name as dish_name
FROM restaurant_media rm
LEFT JOIN dish d ON rm.url LIKE '%/dish_' || d.dish_id::text || '_%'
WHERE rm.restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Cơm niêu 3 Cá Bống%' LIMIT 1)
  AND rm.type = 'dish'
ORDER BY rm.media_id;

-- 4. Tổng kết
SELECT 
    'Nhà hàng' as loai,
    COUNT(*) as so_luong,
    STRING_AGG(approval_status, ', ') as status
FROM restaurant_profile
WHERE restaurant_name LIKE '%Cơm niêu 3 Cá Bống%'

UNION ALL

SELECT 
    'Món ăn (tất cả)' as loai,
    COUNT(*) as so_luong,
    STRING_AGG(DISTINCT status, ', ') as status
FROM dish
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Cơm niêu 3 Cá Bống%' LIMIT 1)

UNION ALL

SELECT 
    'Món ăn (AVAILABLE)' as loai,
    COUNT(*) as so_luong,
    'AVAILABLE' as status
FROM dish
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Cơm niêu 3 Cá Bống%' LIMIT 1)
  AND status = 'AVAILABLE'

UNION ALL

SELECT 
    'Ảnh dish' as loai,
    COUNT(*) as so_luong,
    'dish' as status
FROM restaurant_media
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Cơm niêu 3 Cá Bống%' LIMIT 1)
  AND type = 'dish';

