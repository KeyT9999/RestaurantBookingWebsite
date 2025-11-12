-- =====================================================
-- QUERY ĐƠN GIẢN: Kiểm tra owner_id của "Taiphan"
-- =====================================================

-- Query nhanh nhất
SELECT 
    u.username,
    ro.owner_id,
    ro.owner_name
FROM users u
LEFT JOIN restaurant_owner ro ON u.id = ro.user_id
WHERE u.username = 'Taiphan';

-- Nếu muốn tất cả thông tin trong 1 dòng
SELECT 
    u.id AS user_id,
    u.username,
    u.full_name,
    u.email,
    ro.owner_id,
    ro.owner_name,
    (SELECT COUNT(*) FROM restaurant_profile WHERE owner_id = ro.owner_id) AS số_nhà_hàng
FROM users u
LEFT JOIN restaurant_owner ro ON u.id = ro.user_id
WHERE u.username = 'Taiphan';




