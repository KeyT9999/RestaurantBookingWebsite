-- =====================================================
-- KIỂM TRA NHÀ HÀNG CÒN TỒN TẠI KHÔNG
-- =====================================================
-- Thay đổi tên nhà hàng ở dưới để kiểm tra
-- =====================================================

-- Kiểm tra các nhà hàng đã xóa
SELECT 
    'AVVVV' AS tên_nhà_hàng,
    CASE 
        WHEN EXISTS (SELECT 1 FROM restaurant_profile WHERE restaurant_name = 'AVVVV') 
        THEN '✅ CÒN TỒN TẠI'
        ELSE '❌ ĐÃ BỊ XÓA'
    END AS trạng_thái,
    (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name = 'AVVVV' LIMIT 1) AS restaurant_id

UNION ALL

SELECT 
    'Sushi',
    CASE 
        WHEN EXISTS (SELECT 1 FROM restaurant_profile WHERE restaurant_name ILIKE '%Sushi%') 
        THEN '✅ CÒN TỒN TẠI'
        ELSE '❌ ĐÃ BỊ XÓA'
    END,
    (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name ILIKE '%Sushi%' LIMIT 1)

UNION ALL

SELECT 
    'AI',
    CASE 
        WHEN EXISTS (SELECT 1 FROM restaurant_profile WHERE restaurant_name ILIKE '%AI%') 
        THEN '✅ CÒN TỒN TẠI'
        ELSE '❌ ĐÃ BỊ XÓA'
    END,
    (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name ILIKE '%AI%' LIMIT 1);

-- =====================================================
-- Kiểm tra chi tiết (nếu còn tồn tại)
-- =====================================================

-- Kiểm tra "AVVVV"
SELECT 
    'AVVVV' AS nhà_hàng,
    restaurant_id,
    restaurant_name,
    address,
    phone,
    approval_status,
    created_at
FROM restaurant_profile 
WHERE restaurant_name = 'AVVVV'
LIMIT 1;

-- Kiểm tra "Sushi"
SELECT 
    'Sushi' AS nhà_hàng,
    restaurant_id,
    restaurant_name,
    address,
    phone,
    approval_status,
    created_at
FROM restaurant_profile 
WHERE restaurant_name ILIKE '%Sushi%'
LIMIT 1;

-- Kiểm tra "AI"
SELECT 
    'AI' AS nhà_hàng,
    restaurant_id,
    restaurant_name,
    address,
    phone,
    approval_status,
    created_at
FROM restaurant_profile 
WHERE restaurant_name ILIKE '%AI%'
LIMIT 1;




