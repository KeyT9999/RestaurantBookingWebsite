-- =====================================================
-- QUERY ĐƠN GIẢN: Kiểm tra nhanh nhà hàng còn tồn tại không
-- =====================================================
-- Thay đổi tên nhà hàng ở dòng cuối
-- =====================================================

-- Kiểm tra "AVVVV"
SELECT 
    CASE 
        WHEN EXISTS (SELECT 1 FROM restaurant_profile WHERE restaurant_name = 'AVVVV') 
        THEN '✅ Nhà hàng "AVVVV" CÒN TỒN TẠI'
        ELSE '❌ Nhà hàng "AVVVV" ĐÃ BỊ XÓA'
    END AS kết_quả;

-- Kiểm tra "Sushi"
SELECT 
    CASE 
        WHEN EXISTS (SELECT 1 FROM restaurant_profile WHERE restaurant_name ILIKE '%Sushi%') 
        THEN '✅ Nhà hàng "Sushi" CÒN TỒN TẠI'
        ELSE '❌ Nhà hàng "Sushi" ĐÃ BỊ XÓA'
    END AS kết_quả;

-- Kiểm tra "AI"
SELECT 
    CASE 
        WHEN EXISTS (SELECT 1 FROM restaurant_profile WHERE restaurant_name ILIKE '%AI%') 
        THEN '✅ Nhà hàng "AI" CÒN TỒN TẠI'
        ELSE '❌ Nhà hàng "AI" ĐÃ BỊ XÓA'
    END AS kết_quả;

-- =====================================================
-- Kiểm tra nhà hàng bất kỳ (thay đổi tên ở đây)
-- =====================================================

-- Thay đổi 'TÊN NHÀ HÀNG' thành tên nhà hàng bạn muốn kiểm tra
SELECT 
    CASE 
        WHEN EXISTS (SELECT 1 FROM restaurant_profile WHERE restaurant_name ILIKE '%TÊN NHÀ HÀNG%') 
        THEN '✅ Nhà hàng CÒN TỒN TẠI'
        ELSE '❌ Nhà hàng ĐÃ BỊ XÓA hoặc KHÔNG TỒN TẠI'
    END AS kết_quả,
    (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name ILIKE '%TÊN NHÀ HÀNG%' LIMIT 1) AS restaurant_id;




