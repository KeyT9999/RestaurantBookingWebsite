-- =====================================================
-- SQL Script: Cập nhật giá thành 50.000 VNĐ
-- cho nhà hàng "Cơm niêu 3 Cá Bống"
-- =====================================================

-- 1. Update giá món ăn → 50.000
UPDATE dish
SET price = 50000
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Cơm niêu 3 Cá Bống%' LIMIT 1);

-- 2. Update giá dịch vụ (có phí) → 50.000
UPDATE restaurant_service
SET price = 50000
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Cơm niêu 3 Cá Bống%' LIMIT 1)
  AND price > 0;

-- 3. Update deposit amount của bàn → 50.000
UPDATE restaurant_table
SET depositamount = 50000
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Cơm niêu 3 Cá Bống%' LIMIT 1);

-- 4. Update average_price của nhà hàng → 50.000
UPDATE restaurant_profile
SET average_price = 50000
WHERE restaurant_name LIKE '%Cơm niêu 3 Cá Bống%';

-- =====================================================
-- VERIFICATION - Kiểm tra giá đã cập nhật
-- =====================================================

SELECT 
    'Giá món ăn' as loai,
    COUNT(*) as so_luong,
    MIN(price) as gia_thap_nhat,
    MAX(price) as gia_cao_nhat,
    AVG(price)::INTEGER as gia_trung_binh
FROM dish
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Cơm niêu 3 Cá Bống%' LIMIT 1)

UNION ALL

SELECT 
    'Giá dịch vụ (có phí)' as loai,
    COUNT(*) as so_luong,
    MIN(price)::INTEGER as gia_thap_nhat,
    MAX(price)::INTEGER as gia_cao_nhat,
    AVG(price)::INTEGER as gia_trung_binh
FROM restaurant_service
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Cơm niêu 3 Cá Bống%' LIMIT 1)
  AND price > 0

UNION ALL

SELECT 
    'Deposit amount' as loai,
    COUNT(*) as so_luong,
    MIN(depositamount)::INTEGER as gia_thap_nhat,
    MAX(depositamount)::INTEGER as gia_cao_nhat,
    AVG(depositamount)::INTEGER as gia_trung_binh
FROM restaurant_table
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Cơm niêu 3 Cá Bống%' LIMIT 1)

UNION ALL

SELECT 
    'Average price (restaurant)' as loai,
    1 as so_luong,
    average_price::INTEGER as gia_thap_nhat,
    average_price::INTEGER as gia_cao_nhat,
    average_price::INTEGER as gia_trung_binh
FROM restaurant_profile
WHERE restaurant_name LIKE '%Cơm niêu 3 Cá Bống%';

