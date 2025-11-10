-- =====================================================
-- SQL Script: KIỂM TRA CÁC BẢNG LIÊN QUAN ĐẾN NHÀ HÀNG
-- =====================================================
-- Script này sẽ liệt kê tất cả các bảng và số lượng bản ghi
-- sẽ bị ảnh hưởng khi xóa một nhà hàng
-- =====================================================

DO $$
DECLARE
    v_restaurant_id INTEGER;
    v_restaurant_name VARCHAR(255);
    v_count INTEGER;
BEGIN
    -- Nhập tên nhà hàng cần kiểm tra (hoặc ID)
    -- Thay đổi giá trị này để kiểm tra nhà hàng khác
    v_restaurant_name := 'AVVVV'; -- Hoặc dùng: v_restaurant_id := 36;
    
    -- Tìm restaurant_id
    IF v_restaurant_id IS NULL THEN
        SELECT restaurant_id, restaurant_name INTO v_restaurant_id, v_restaurant_name
        FROM restaurant_profile 
        WHERE restaurant_name = v_restaurant_name;
    ELSE
        SELECT restaurant_name INTO v_restaurant_name
        FROM restaurant_profile 
        WHERE restaurant_id = v_restaurant_id;
    END IF;
    
    IF v_restaurant_id IS NULL THEN
        RAISE NOTICE '❌ Không tìm thấy nhà hàng!';
        RETURN;
    END IF;
    
    RAISE NOTICE '';
    RAISE NOTICE '====================================================';
    RAISE NOTICE '📊 KIỂM TRA CÁC BẢNG LIÊN QUAN ĐẾN NHÀ HÀNG';
    RAISE NOTICE '====================================================';
    RAISE NOTICE 'Nhà hàng: % (ID: %)', v_restaurant_name, v_restaurant_id;
    RAISE NOTICE '';
    
    -- =====================================================
    -- CÁC BẢNG CÓ FOREIGN KEY ĐẾN RESTAURANT_PROFILE
    -- =====================================================
    
    RAISE NOTICE '📋 CÁC BẢNG TRỰC TIẾP LIÊN QUAN (có restaurant_id):';
    RAISE NOTICE '----------------------------------------------------';
    
    -- 1. Booking
    SELECT COUNT(*) INTO v_count FROM booking WHERE restaurant_id = v_restaurant_id;
    RAISE NOTICE '   booking: % bản ghi', v_count;
    
    -- 2. Restaurant Table
    SELECT COUNT(*) INTO v_count FROM restaurant_table WHERE restaurant_id = v_restaurant_id;
    RAISE NOTICE '   restaurant_table: % bản ghi', v_count;
    
    -- 3. Dish
    SELECT COUNT(*) INTO v_count FROM dish WHERE restaurant_id = v_restaurant_id;
    RAISE NOTICE '   dish: % bản ghi', v_count;
    
    -- 4. Restaurant Service
    SELECT COUNT(*) INTO v_count FROM restaurant_service WHERE restaurant_id = v_restaurant_id;
    RAISE NOTICE '   restaurant_service: % bản ghi', v_count;
    
    -- 5. Restaurant Media
    SELECT COUNT(*) INTO v_count FROM restaurant_media WHERE restaurant_id = v_restaurant_id;
    RAISE NOTICE '   restaurant_media: % bản ghi', v_count;
    
    -- 6. Review
    SELECT COUNT(*) INTO v_count FROM review WHERE restaurant_id = v_restaurant_id;
    RAISE NOTICE '   review: % bản ghi', v_count;
    
    -- 7. Review Report
    SELECT COUNT(*) INTO v_count FROM review_report WHERE restaurant_id = v_restaurant_id;
    RAISE NOTICE '   review_report: % bản ghi', v_count;
    
    -- 8. Customer Favorite
    SELECT COUNT(*) INTO v_count FROM customer_favorite WHERE restaurant_id = v_restaurant_id;
    RAISE NOTICE '   customer_favorite: % bản ghi', v_count;
    
    -- 9. Voucher
    SELECT COUNT(*) INTO v_count FROM voucher WHERE restaurant_id = v_restaurant_id;
    RAISE NOTICE '   voucher: % bản ghi', v_count;
    
    -- 10. Waitlist
    SELECT COUNT(*) INTO v_count FROM waitlist WHERE restaurant_id = v_restaurant_id;
    RAISE NOTICE '   waitlist: % bản ghi', v_count;
    
    -- 11. Restaurant Availability
    SELECT COUNT(*) INTO v_count FROM restaurant_availability WHERE restaurant_id = v_restaurant_id;
    RAISE NOTICE '   restaurant_availability: % bản ghi', v_count;
    
    -- 12. Withdrawal Request
    SELECT COUNT(*) INTO v_count FROM withdrawal_request WHERE restaurant_id = v_restaurant_id;
    RAISE NOTICE '   withdrawal_request: % bản ghi', v_count;
    
    -- 13. Refund Request
    SELECT COUNT(*) INTO v_count FROM refund_request WHERE restaurant_id = v_restaurant_id;
    RAISE NOTICE '   refund_request: % bản ghi', v_count;
    
    -- 14. Restaurant Bank Account
    SELECT COUNT(*) INTO v_count FROM restaurant_bank_account WHERE restaurant_id = v_restaurant_id;
    RAISE NOTICE '   restaurant_bank_account: % bản ghi', v_count;
    
    -- 15. Restaurant Balance
    SELECT COUNT(*) INTO v_count FROM restaurant_balance WHERE restaurant_id = v_restaurant_id;
    RAISE NOTICE '   restaurant_balance: % bản ghi', v_count;
    
    -- 16. Restaurant Contract
    SELECT COUNT(*) INTO v_count FROM restaurant_contract WHERE restaurant_id = v_restaurant_id;
    RAISE NOTICE '   restaurant_contract: % bản ghi', v_count;
    
    -- 17. Chat Room
    SELECT COUNT(*) INTO v_count FROM chat_room WHERE restaurant_id = v_restaurant_id;
    RAISE NOTICE '   chat_room: % bản ghi', v_count;
    
    -- 18. AI Interactions
    BEGIN
        SELECT COUNT(*) INTO v_count FROM ai_interactions WHERE restaurant_id = v_restaurant_id;
        RAISE NOTICE '   ai_interactions: % bản ghi', v_count;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE '   ai_interactions: bảng không tồn tại';
    END;
    
    -- 19. AI Recommendation Diversity
    BEGIN
        SELECT COUNT(*) INTO v_count FROM ai_recommendation_diversity WHERE restaurant_id = v_restaurant_id;
        RAISE NOTICE '   ai_recommendation_diversity: % bản ghi', v_count;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE '   ai_recommendation_diversity: bảng không tồn tại';
    END;
    
    -- 20. Audit Log
    BEGIN
        SELECT COUNT(*) INTO v_count FROM audit_log WHERE restaurant_id = v_restaurant_id;
        RAISE NOTICE '   audit_log: % bản ghi', v_count;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE '   audit_log: bảng không tồn tại hoặc không có restaurant_id';
    END;
    
    RAISE NOTICE '';
    
    -- =====================================================
    -- CÁC BẢNG LIÊN QUAN ĐẾN BOOKING
    -- =====================================================
    
    RAISE NOTICE '📋 CÁC BẢNG LIÊN QUAN ĐẾN BOOKING:';
    RAISE NOTICE '----------------------------------------------------';
    
    -- 1. Payment
    SELECT COUNT(*) INTO v_count 
    FROM payment 
    WHERE booking_id IN (SELECT booking_id FROM booking WHERE restaurant_id = v_restaurant_id);
    RAISE NOTICE '   payment: % bản ghi', v_count;
    
    -- 2. Booking Dish
    SELECT COUNT(*) INTO v_count 
    FROM booking_dish 
    WHERE booking_id IN (SELECT booking_id FROM booking WHERE restaurant_id = v_restaurant_id);
    RAISE NOTICE '   booking_dish: % bản ghi', v_count;
    
    -- 3. Booking Service
    SELECT COUNT(*) INTO v_count 
    FROM booking_service 
    WHERE booking_id IN (SELECT booking_id FROM booking WHERE restaurant_id = v_restaurant_id);
    RAISE NOTICE '   booking_service: % bản ghi', v_count;
    
    -- 4. Booking Table
    SELECT COUNT(*) INTO v_count 
    FROM booking_table 
    WHERE booking_id IN (SELECT booking_id FROM booking WHERE restaurant_id = v_restaurant_id);
    RAISE NOTICE '   booking_table: % bản ghi', v_count;
    
    -- 5. Voucher Redemption (qua booking)
    SELECT COUNT(*) INTO v_count 
    FROM voucher_redemption 
    WHERE booking_id IN (SELECT booking_id FROM booking WHERE restaurant_id = v_restaurant_id);
    RAISE NOTICE '   voucher_redemption (qua booking): % bản ghi', v_count;
    
    -- 6. Voucher Redemption (qua payment)
    SELECT COUNT(*) INTO v_count 
    FROM voucher_redemption 
    WHERE payment_id IN (
        SELECT payment_id FROM payment 
        WHERE booking_id IN (SELECT booking_id FROM booking WHERE restaurant_id = v_restaurant_id)
    );
    RAISE NOTICE '   voucher_redemption (qua payment): % bản ghi', v_count;
    
    -- 7. Refund Request (qua payment)
    SELECT COUNT(*) INTO v_count 
    FROM refund_request 
    WHERE payment_id IN (
        SELECT payment_id FROM payment 
        WHERE booking_id IN (SELECT booking_id FROM booking WHERE restaurant_id = v_restaurant_id)
    );
    RAISE NOTICE '   refund_request (qua payment): % bản ghi', v_count;
    
    -- 8. Internal Notes
    BEGIN
        SELECT COUNT(*) INTO v_count 
        FROM internal_notes 
        WHERE booking_id IN (SELECT booking_id FROM booking WHERE restaurant_id = v_restaurant_id);
        RAISE NOTICE '   internal_notes: % bản ghi', v_count;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE '   internal_notes: bảng không tồn tại';
    END;
    
    -- 9. Communication History
    BEGIN
        SELECT COUNT(*) INTO v_count 
        FROM communication_history 
        WHERE booking_id IN (SELECT booking_id FROM booking WHERE restaurant_id = v_restaurant_id);
        RAISE NOTICE '   communication_history: % bản ghi', v_count;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE '   communication_history: bảng không tồn tại';
    END;
    
    RAISE NOTICE '';
    
    -- =====================================================
    -- CÁC BẢNG LIÊN QUAN ĐẾN REVIEW_REPORT
    -- =====================================================
    
    RAISE NOTICE '📋 CÁC BẢNG LIÊN QUAN ĐẾN REVIEW_REPORT:';
    RAISE NOTICE '----------------------------------------------------';
    
    -- Review Report Evidence
    SELECT COUNT(*) INTO v_count 
    FROM review_report_evidence 
    WHERE report_id IN (SELECT report_id FROM review_report WHERE restaurant_id = v_restaurant_id);
    RAISE NOTICE '   review_report_evidence: % bản ghi', v_count;
    
    RAISE NOTICE '';
    
    -- =====================================================
    -- CÁC BẢNG LIÊN QUAN ĐẾN CHAT_ROOM
    -- =====================================================
    
    RAISE NOTICE '📋 CÁC BẢNG LIÊN QUAN ĐẾN CHAT_ROOM:';
    RAISE NOTICE '----------------------------------------------------';
    
    -- Message
    SELECT COUNT(*) INTO v_count 
    FROM message 
    WHERE room_id IN (SELECT room_id FROM chat_room WHERE restaurant_id = v_restaurant_id);
    RAISE NOTICE '   message: % bản ghi', v_count;
    
    RAISE NOTICE '';
    RAISE NOTICE '====================================================';
    RAISE NOTICE '✅ Hoàn tất kiểm tra!';
    RAISE NOTICE '====================================================';
    
END $$;

-- =====================================================
-- QUERY ĐỂ TÌM TẤT CẢ FOREIGN KEYS THAM CHIẾU ĐẾN RESTAURANT_PROFILE
-- =====================================================

SELECT 
    tc.table_name AS "Bảng chứa FK",
    kcu.column_name AS "Cột FK",
    ccu.table_name AS "Bảng được tham chiếu",
    ccu.column_name AS "Cột được tham chiếu",
    tc.constraint_name AS "Tên constraint"
FROM information_schema.table_constraints AS tc 
JOIN information_schema.key_column_usage AS kcu
  ON tc.constraint_name = kcu.constraint_name
  AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu
  ON ccu.constraint_name = tc.constraint_name
  AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY' 
  AND ccu.table_name = 'restaurant_profile'
  AND ccu.column_name = 'restaurant_id'
ORDER BY tc.table_name;

-- =====================================================
-- QUERY ĐỂ TÌM TẤT CẢ CÁC BẢNG CÓ CỘT restaurant_id
-- =====================================================

SELECT 
    table_name AS "Tên bảng",
    column_name AS "Tên cột",
    data_type AS "Kiểu dữ liệu",
    is_nullable AS "Cho phép NULL"
FROM information_schema.columns
WHERE column_name = 'restaurant_id'
  AND table_schema = 'public'
ORDER BY table_name;




