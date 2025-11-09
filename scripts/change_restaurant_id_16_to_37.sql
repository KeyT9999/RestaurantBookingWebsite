-- =====================================================
-- SQL Script: ĐỔI RESTAURANT_ID TỪ 16 SANG 37
-- Nhà hàng: Phở Bò ABC
-- =====================================================
-- 
-- Script này sẽ đổi restaurant_id của nhà hàng "Phở Bò ABC" từ 16 sang 37
-- Cần cập nhật tất cả các bảng có foreign key đến restaurant_id
--
-- ⚠️ CẢNH BÁO: Thao tác này KHÔNG THỂ HOÀN TÁC!
-- ⚠️ Hãy backup database trước khi chạy!
-- =====================================================

DO $$
DECLARE
    v_old_id INTEGER := 16;
    v_new_id INTEGER := 37;
    v_restaurant_name VARCHAR(255);
    v_count INTEGER;
BEGIN
    -- Kiểm tra nhà hàng ID 16 có tồn tại không
    SELECT restaurant_name INTO v_restaurant_name
    FROM restaurant_profile
    WHERE restaurant_id = v_old_id;
    
    IF v_restaurant_name IS NULL THEN
        RAISE EXCEPTION '❌ Không tìm thấy nhà hàng với ID %', v_old_id;
    END IF;
    
    -- Kiểm tra tên nhà hàng
    IF v_restaurant_name NOT ILIKE '%Phở Bò ABC%' THEN
        RAISE NOTICE '⚠️  Cảnh báo: Nhà hàng ID % có tên "%" không khớp với "Phở Bò ABC"', v_old_id, v_restaurant_name;
        RAISE NOTICE 'Bạn có muốn tiếp tục? (Script sẽ tiếp tục)';
    END IF;
    
    -- Kiểm tra ID 37 đã được sử dụng chưa
    SELECT COUNT(*) INTO v_count
    FROM restaurant_profile
    WHERE restaurant_id = v_new_id;
    
    IF v_count > 0 THEN
        RAISE EXCEPTION '❌ ID % đã được sử dụng bởi nhà hàng: %', 
            v_new_id, 
            (SELECT restaurant_name FROM restaurant_profile WHERE restaurant_id = v_new_id);
    END IF;
    
    RAISE NOTICE '';
    RAISE NOTICE '====================================================';
    RAISE NOTICE '🔄 BẮT ĐẦU ĐỔI RESTAURANT_ID';
    RAISE NOTICE '====================================================';
    RAISE NOTICE 'Nhà hàng: %', v_restaurant_name;
    RAISE NOTICE 'Từ ID: % → Sang ID: %', v_old_id, v_new_id;
    RAISE NOTICE '';
    
    -- =====================================================
    -- BƯỚC 1: CẬP NHẬT CÁC BẢNG CÓ FOREIGN KEY ĐẾN RESTAURANT_ID
    -- =====================================================
    
    RAISE NOTICE '📝 BƯỚC 1: Cập nhật các bảng có foreign key...';
    
    -- 1. booking
    UPDATE booking SET restaurant_id = v_new_id WHERE restaurant_id = v_old_id;
    GET DIAGNOSTICS v_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã cập nhật % bookings', v_count;
    
    -- 2. restaurant_table
    UPDATE restaurant_table SET restaurant_id = v_new_id WHERE restaurant_id = v_old_id;
    GET DIAGNOSTICS v_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã cập nhật % restaurant_tables', v_count;
    
    -- 3. dish
    UPDATE dish SET restaurant_id = v_new_id WHERE restaurant_id = v_old_id;
    GET DIAGNOSTICS v_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã cập nhật % dishes', v_count;
    
    -- 4. restaurant_service
    UPDATE restaurant_service SET restaurant_id = v_new_id WHERE restaurant_id = v_old_id;
    GET DIAGNOSTICS v_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã cập nhật % restaurant_services', v_count;
    
    -- 5. restaurant_media
    UPDATE restaurant_media SET restaurant_id = v_new_id WHERE restaurant_id = v_old_id;
    GET DIAGNOSTICS v_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã cập nhật % restaurant_media', v_count;
    
    -- 6. review
    UPDATE review SET restaurant_id = v_new_id WHERE restaurant_id = v_old_id;
    GET DIAGNOSTICS v_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã cập nhật % reviews', v_count;
    
    -- 7. review_report
    UPDATE review_report SET restaurant_id = v_new_id WHERE restaurant_id = v_old_id;
    GET DIAGNOSTICS v_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã cập nhật % review_reports', v_count;
    
    -- 8. customer_favorite
    UPDATE customer_favorite SET restaurant_id = v_new_id WHERE restaurant_id = v_old_id;
    GET DIAGNOSTICS v_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã cập nhật % customer_favorites', v_count;
    
    -- 9. voucher
    UPDATE voucher SET restaurant_id = v_new_id WHERE restaurant_id = v_old_id;
    GET DIAGNOSTICS v_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã cập nhật % vouchers', v_count;
    
    -- 10. waitlist
    UPDATE waitlist SET restaurant_id = v_new_id WHERE restaurant_id = v_old_id;
    GET DIAGNOSTICS v_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã cập nhật % waitlists', v_count;
    
    -- 11. restaurant_availability
    UPDATE restaurant_availability SET restaurant_id = v_new_id WHERE restaurant_id = v_old_id;
    GET DIAGNOSTICS v_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã cập nhật % restaurant_availability records', v_count;
    
    -- 12. withdrawal_request
    UPDATE withdrawal_request SET restaurant_id = v_new_id WHERE restaurant_id = v_old_id;
    GET DIAGNOSTICS v_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã cập nhật % withdrawal_requests', v_count;
    
    -- 13. refund_request
    UPDATE refund_request SET restaurant_id = v_new_id WHERE restaurant_id = v_old_id;
    GET DIAGNOSTICS v_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã cập nhật % refund_requests', v_count;
    
    -- 14. restaurant_bank_account
    UPDATE restaurant_bank_account SET restaurant_id = v_new_id WHERE restaurant_id = v_old_id;
    GET DIAGNOSTICS v_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã cập nhật % restaurant_bank_accounts', v_count;
    
    -- 15. restaurant_balance
    UPDATE restaurant_balance SET restaurant_id = v_new_id WHERE restaurant_id = v_old_id;
    GET DIAGNOSTICS v_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã cập nhật % restaurant_balance records', v_count;
    
    -- 16. restaurant_contract
    UPDATE restaurant_contract SET restaurant_id = v_new_id WHERE restaurant_id = v_old_id;
    GET DIAGNOSTICS v_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã cập nhật % restaurant_contract records', v_count;
    
    -- 17. chat_room
    UPDATE chat_room SET restaurant_id = v_new_id WHERE restaurant_id = v_old_id;
    GET DIAGNOSTICS v_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã cập nhật % chat_rooms', v_count;
    
    -- 18. ai_interactions
    BEGIN
        UPDATE ai_interactions SET restaurant_id = v_new_id WHERE restaurant_id = v_old_id;
        GET DIAGNOSTICS v_count = ROW_COUNT;
        RAISE NOTICE '   ✅ Đã cập nhật % ai_interactions records', v_count;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE '   ⚠️  Không thể cập nhật ai_interactions (bảng có thể không tồn tại)';
    END;
    
    -- 19. ai_recommendation_diversity
    BEGIN
        UPDATE ai_recommendation_diversity SET restaurant_id = v_new_id WHERE restaurant_id = v_old_id;
        GET DIAGNOSTICS v_count = ROW_COUNT;
        RAISE NOTICE '   ✅ Đã cập nhật % ai_recommendation_diversity records', v_count;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE '   ⚠️  Không thể cập nhật ai_recommendation_diversity (bảng có thể không tồn tại)';
    END;
    
    -- 20. audit_log
    BEGIN
        UPDATE audit_log SET restaurant_id = v_new_id WHERE restaurant_id = v_old_id;
        GET DIAGNOSTICS v_count = ROW_COUNT;
        RAISE NOTICE '   ✅ Đã cập nhật % audit_log records', v_count;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE '   ⚠️  Không thể cập nhật audit_log (bảng có thể không tồn tại hoặc không có restaurant_id)';
    END;
    
    RAISE NOTICE '';
    
    -- =====================================================
    -- BƯỚC 2: CẬP NHẬT RESTAURANT_PROFILE
    -- =====================================================
    
    RAISE NOTICE '📝 BƯỚC 2: Cập nhật restaurant_profile...';
    
    UPDATE restaurant_profile 
    SET restaurant_id = v_new_id 
    WHERE restaurant_id = v_old_id;
    
    GET DIAGNOSTICS v_count = ROW_COUNT;
    
    IF v_count = 0 THEN
        RAISE EXCEPTION '❌ Không thể cập nhật restaurant_profile!';
    END IF;
    
    RAISE NOTICE '   ✅ Đã cập nhật restaurant_profile';
    RAISE NOTICE '';
    
    -- =====================================================
    -- BƯỚC 3: CẬP NHẬT SEQUENCE
    -- =====================================================
    
    RAISE NOTICE '📝 BƯỚC 3: Cập nhật sequence...';
    
    PERFORM setval('restaurant_profile_restaurant_id_seq', GREATEST(v_new_id, (SELECT MAX(restaurant_id) FROM restaurant_profile)));
    
    RAISE NOTICE '   ✅ Đã cập nhật sequence';
    RAISE NOTICE '';
    
    -- =====================================================
    -- BƯỚC 4: XÁC MINH
    -- =====================================================
    
    RAISE NOTICE '📝 BƯỚC 4: Xác minh...';
    
    SELECT COUNT(*) INTO v_count
    FROM restaurant_profile
    WHERE restaurant_id = v_new_id;
    
    IF v_count = 0 THEN
        RAISE EXCEPTION '❌ Lỗi: Không tìm thấy nhà hàng với ID % sau khi cập nhật!', v_new_id;
    END IF;
    
    SELECT restaurant_name INTO v_restaurant_name
    FROM restaurant_profile
    WHERE restaurant_id = v_new_id;
    
    RAISE NOTICE '';
    RAISE NOTICE '====================================================';
    RAISE NOTICE '✅ ✅ ✅ HOÀN TẤT!';
    RAISE NOTICE '====================================================';
    RAISE NOTICE 'Nhà hàng "%" đã được đổi ID:', v_restaurant_name;
    RAISE NOTICE '   Từ ID: % → Sang ID: %', v_old_id, v_new_id;
    RAISE NOTICE '';
    RAISE NOTICE '✅ Đã cập nhật thành công!';
    RAISE NOTICE '';
    
END $$;

-- =====================================================
-- KIỂM TRA LẠI
-- =====================================================

-- Kiểm tra nhà hàng với ID mới
SELECT 
    restaurant_id,
    restaurant_name,
    address,
    phone,
    approval_status,
    created_at
FROM restaurant_profile 
WHERE restaurant_id = 37;

-- Kiểm tra xem ID cũ còn tồn tại không
SELECT 
    CASE 
        WHEN EXISTS (SELECT 1 FROM restaurant_profile WHERE restaurant_id = 16) 
        THEN '❌ ID 16 vẫn còn tồn tại!'
        ELSE '✅ ID 16 đã được đổi thành công!'
    END AS trạng_thái;




