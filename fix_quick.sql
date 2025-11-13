-- ============================================
-- SCRIPT SỬA NHANH - Sử dụng user "owner_demo"
-- ============================================

-- BƯỚC 1: Kiểm tra user "owner_demo" có an toàn không
SELECT 
    CASE 
        WHEN ro.owner_id IS NULL THEN '✅ CHƯA ĐƯỢC SỬ DỤNG - AN TOÀN'
        ELSE '❌ ĐÃ ĐƯỢC SỬ DỤNG'
    END AS status,
    u.id,
    u.username,
    u.email,
    u.role
FROM users u
LEFT JOIN restaurant_owner ro ON u.id = ro.user_id
WHERE u.id = '36ba3e53-90d8-4c8d-a331-ed88218db9fa';  -- owner_demo

-- BƯỚC 2: Sửa owner "Kim Min Joon" (có restaurant "Seoul BBQ Premium")
UPDATE restaurant_owner 
SET user_id = '36ba3e53-90d8-4c8d-a331-ed88218db9fa'  -- owner_demo
WHERE owner_id = '83376775-b03a-4830-953b-e4c2cfa6cc2a';  -- Kim Min Joon

-- BƯỚC 3: Xác minh đã sửa
SELECT 
    rp.restaurant_id,
    rp.restaurant_name,
    ro.owner_name,
    ro.user_id,
    u.username,
    CASE 
        WHEN u.id IS NOT NULL THEN '✅ ĐÃ SỬA'
        ELSE '❌ CHƯA SỬA'
    END AS status
FROM restaurant_profile rp
INNER JOIN restaurant_owner ro ON rp.owner_id = ro.owner_id
LEFT JOIN users u ON ro.user_id = u.id
WHERE rp.restaurant_id = 31;  -- Seoul BBQ Premium

-- BƯỚC 4: Kiểm tra còn owner nào bị lỗi không
SELECT 
    COUNT(*) as orphaned_count
FROM restaurant_owner ro
LEFT JOIN users u ON ro.user_id = u.id
WHERE u.id IS NULL;

