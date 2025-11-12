-- =====================================================
-- KIỂM TRA CÁC BOOKING LIÊN QUAN ĐẾN NHÀ HÀNG "AVVVV"
-- =====================================================

-- Tìm restaurant_id của nhà hàng "AVVVV"
DO $$
DECLARE
    v_restaurant_id INTEGER;
    v_restaurant_name VARCHAR(255);
    v_booking_count INTEGER;
    v_payment_count INTEGER;
    v_booking_dish_count INTEGER;
    v_booking_service_count INTEGER;
    v_booking_table_count INTEGER;
    rec RECORD;  -- Biến cho FOR loop
BEGIN
    -- Tìm nhà hàng
    SELECT restaurant_id, restaurant_name INTO v_restaurant_id, v_restaurant_name
    FROM restaurant_profile 
    WHERE restaurant_name = 'AVVVV' OR restaurant_name = 'AVVVVV';
    
    IF v_restaurant_id IS NULL THEN
        RAISE NOTICE '';
        RAISE NOTICE '====================================================';
        RAISE NOTICE '❌ KHÔNG TÌM THẤY NHÀ HÀNG "AVVVV" HOẶC "AVVVVV"';
        RAISE NOTICE '====================================================';
        RAISE NOTICE '';
        RAISE NOTICE 'Nhà hàng có thể đã bị xóa hoặc không tồn tại.';
        RETURN;
    END IF;
    
    RAISE NOTICE '';
    RAISE NOTICE '====================================================';
    RAISE NOTICE '📊 KIỂM TRA BOOKINGS CHO NHÀ HÀNG';
    RAISE NOTICE '====================================================';
    RAISE NOTICE 'Nhà hàng: % (ID: %)', v_restaurant_name, v_restaurant_id;
    RAISE NOTICE '';
    
    -- Đếm bookings
    SELECT COUNT(*) INTO v_booking_count 
    FROM booking 
    WHERE restaurant_id = v_restaurant_id;
    
    IF v_booking_count = 0 THEN
        RAISE NOTICE '✅ KHÔNG CÓ BOOKING NÀO!';
        RAISE NOTICE '   Nhà hàng này không có booking nào.';
        RAISE NOTICE '';
        RAISE NOTICE '✅ CÓ THỂ XÓA NHÀ HÀNG AN TOÀN!';
    ELSE
        RAISE NOTICE '⚠️  TÌM THẤY % BOOKING(S)!', v_booking_count;
        RAISE NOTICE '';
        
        -- Hiển thị chi tiết bookings
        RAISE NOTICE '📋 DANH SÁCH BOOKINGS:';
        RAISE NOTICE '----------------------------------------------------';
        
        FOR rec IN 
            SELECT 
                booking_id,
                booking_time,
                number_of_guests,
                status,
                deposit_amount,
                created_at
            FROM booking 
            WHERE restaurant_id = v_restaurant_id
            ORDER BY booking_time DESC
            LIMIT 20
        LOOP
            RAISE NOTICE '   Booking ID: % | Thời gian: % | Số khách: % | Status: % | Đặt cọc: % VNĐ | Tạo: %',
                rec.booking_id, 
                rec.booking_time, 
                rec.number_of_guests, 
                rec.status,
                rec.deposit_amount,
                rec.created_at;
        END LOOP;
        
        IF v_booking_count > 20 THEN
            RAISE NOTICE '   ... và % booking(s) khác', v_booking_count - 20;
        END IF;
        
        RAISE NOTICE '';
        
        -- Đếm các bảng liên quan
        SELECT COUNT(*) INTO v_payment_count 
        FROM payment 
        WHERE booking_id IN (SELECT booking_id FROM booking WHERE restaurant_id = v_restaurant_id);
        
        SELECT COUNT(*) INTO v_booking_dish_count 
        FROM booking_dish 
        WHERE booking_id IN (SELECT booking_id FROM booking WHERE restaurant_id = v_restaurant_id);
        
        SELECT COUNT(*) INTO v_booking_service_count 
        FROM booking_service 
        WHERE booking_id IN (SELECT booking_id FROM booking WHERE restaurant_id = v_restaurant_id);
        
        SELECT COUNT(*) INTO v_booking_table_count 
        FROM booking_table 
        WHERE booking_id IN (SELECT booking_id FROM booking WHERE restaurant_id = v_restaurant_id);
        
        RAISE NOTICE '📊 DỮ LIỆU LIÊN QUAN ĐẾN BOOKINGS:';
        RAISE NOTICE '----------------------------------------------------';
        RAISE NOTICE '   - Payments: % bản ghi', v_payment_count;
        RAISE NOTICE '   - Booking Dishes: % bản ghi', v_booking_dish_count;
        RAISE NOTICE '   - Booking Services: % bản ghi', v_booking_service_count;
        RAISE NOTICE '   - Booking Tables: % bản ghi', v_booking_table_count;
        RAISE NOTICE '';
        RAISE NOTICE '⚠️  CẦN XÓA CÁC BOOKINGS VÀ DỮ LIỆU LIÊN QUAN TRƯỚC!';
        RAISE NOTICE '   Sử dụng script: delete_restaurant_AVVVV.sql';
    END IF;
    
    RAISE NOTICE '====================================================';
    
END $$;

-- Query đơn giản để xem nhanh
SELECT 
    'Tổng số bookings' AS loại,
    COUNT(*) AS số_lượng
FROM booking 
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name IN ('AVVVV', 'AVVVVV') LIMIT 1)

UNION ALL

SELECT 
    'Payments liên quan',
    COUNT(*)
FROM payment 
WHERE booking_id IN (
    SELECT booking_id FROM booking 
    WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name IN ('AVVVV', 'AVVVVV') LIMIT 1)
)

UNION ALL

SELECT 
    'Booking Dishes',
    COUNT(*)
FROM booking_dish 
WHERE booking_id IN (
    SELECT booking_id FROM booking 
    WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name IN ('AVVVV', 'AVVVVV') LIMIT 1)
)

UNION ALL

SELECT 
    'Booking Services',
    COUNT(*)
FROM booking_service 
WHERE booking_id IN (
    SELECT booking_id FROM booking 
    WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name IN ('AVVVV', 'AVVVVV') LIMIT 1)
)

UNION ALL

SELECT 
    'Booking Tables',
    COUNT(*)
FROM booking_table 
WHERE booking_id IN (
    SELECT booking_id FROM booking 
    WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name IN ('AVVVV', 'AVVVVV') LIMIT 1)
);





