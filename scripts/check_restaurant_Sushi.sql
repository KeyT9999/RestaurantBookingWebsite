-- =====================================================
-- KIỂM TRA NHÀ HÀNG "Sushi" TRƯỚC KHI XÓA
-- =====================================================

-- Tìm nhà hàng có tên chứa "Sushi"
SELECT 
    restaurant_id,
    restaurant_name,
    address,
    phone,
    owner_id,
    approval_status,
    created_at
FROM restaurant_profile 
WHERE restaurant_name ILIKE '%Sushi%'
ORDER BY restaurant_name;

-- Đếm số lượng bookings
SELECT 
    r.restaurant_name,
    COUNT(b.booking_id) AS số_booking,
    COUNT(p.payment_id) AS số_payment,
    COUNT(d.dish_id) AS số_dish,
    COUNT(rt.table_id) AS số_table
FROM restaurant_profile r
LEFT JOIN booking b ON r.restaurant_id = b.restaurant_id
LEFT JOIN payment p ON p.booking_id = b.booking_id
LEFT JOIN dish d ON d.restaurant_id = r.restaurant_id
LEFT JOIN restaurant_table rt ON rt.restaurant_id = r.restaurant_id
WHERE r.restaurant_name ILIKE '%Sushi%'
GROUP BY r.restaurant_id, r.restaurant_name;




