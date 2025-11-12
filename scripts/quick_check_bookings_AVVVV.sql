-- =====================================================
-- QUERY ĐƠN GIẢN: Kiểm tra nhanh bookings của "AVVVV"
-- =====================================================

-- Kiểm tra xem nhà hàng có tồn tại không
SELECT 
    CASE 
        WHEN EXISTS (SELECT 1 FROM restaurant_profile WHERE restaurant_name IN ('AVVVV', 'AVVVVV')) 
        THEN '✅ Nhà hàng tồn tại'
        ELSE '❌ Nhà hàng KHÔNG tồn tại (đã bị xóa?)'
    END AS trạng_thái_nhà_hàng;

-- Đếm số bookings
SELECT 
    CASE 
        WHEN COUNT(*) = 0 THEN '✅ KHÔNG CÓ BOOKING - Có thể xóa an toàn!'
        ELSE '⚠️  CÓ ' || COUNT(*) || ' BOOKING(S) - Cần xóa trước!'
    END AS kết_quả,
    COUNT(*) AS số_booking
FROM booking 
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name IN ('AVVVV', 'AVVVVV') LIMIT 1);

-- Nếu có bookings, hiển thị danh sách
SELECT 
    booking_id,
    booking_time,
    number_of_guests,
    status,
    deposit_amount,
    created_at
FROM booking 
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name IN ('AVVVV', 'AVVVVV') LIMIT 1)
ORDER BY booking_time DESC
LIMIT 10;




