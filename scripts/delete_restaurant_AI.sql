-- =====================================================
-- SQL Script: XÓA NHÀ HÀNG "AI" VÀ TẤT CẢ DỮ LIỆU LIÊN QUAN
-- =====================================================
-- 
-- Script này sẽ xóa nhà hàng có tên chứa "AI" và tất cả dữ liệu liên quan:
-- - Bookings và các bản ghi liên quan (payments, booking_tables)
-- - Tables, Dishes, Services, Media
-- - Reviews, Favorites, Vouchers, Waitlists
-- - Restaurant balance, bank accounts, withdrawal requests
-- - Chat rooms, AI interactions, Audit logs
-- - Và tất cả các bản ghi khác liên quan
--
-- ⚠️ CẢNH BÁO: Thao tác này KHÔNG THỂ HOÀN TÁC!
-- =====================================================

DO $$
DECLARE
    v_restaurant_id INTEGER;
    v_restaurant_name VARCHAR(255);
    v_deleted_count INTEGER;
BEGIN
    -- Tìm restaurant_id của nhà hàng có tên chứa "AI"
    SELECT restaurant_id, restaurant_name INTO v_restaurant_id, v_restaurant_name
    FROM restaurant_profile 
    WHERE restaurant_name ILIKE '%AI%'
    LIMIT 1;
    
    IF v_restaurant_id IS NULL THEN
        RAISE NOTICE '❌ Không tìm thấy nhà hàng có tên chứa "AI"';
        RETURN;
    END IF;
    
    RAISE NOTICE '🔍 Tìm thấy nhà hàng "%" với ID: %', v_restaurant_name, v_restaurant_id;
    RAISE NOTICE '🚀 Bắt đầu xóa dữ liệu...';
    
    -- =====================================================
    -- BƯỚC 1: XÓA CÁC BẢN GHI LIÊN QUAN ĐẾN BOOKING
    -- =====================================================
    
    -- Xóa refund_requests TRƯỚC (vì có foreign key đến payment)
    -- Xóa refund_requests liên quan trực tiếp đến restaurant
    DELETE FROM refund_request WHERE restaurant_id = v_restaurant_id;
    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã xóa % refund_requests (trực tiếp)', v_deleted_count;
    
    -- Xóa refund_requests liên quan đến payments từ bookings của nhà hàng này
    DELETE FROM refund_request 
    WHERE payment_id IN (
        SELECT payment_id FROM payment 
        WHERE booking_id IN (
            SELECT booking_id FROM booking WHERE restaurant_id = v_restaurant_id
        )
    );
    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã xóa % refund_requests (qua payments)', v_deleted_count;
    
    -- Xóa voucher_redemptions liên quan đến payments từ bookings của nhà hàng này
    DELETE FROM voucher_redemption 
    WHERE payment_id IN (
        SELECT payment_id FROM payment 
        WHERE booking_id IN (
            SELECT booking_id FROM booking WHERE restaurant_id = v_restaurant_id
        )
    );
    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã xóa % voucher_redemptions (qua payments)', v_deleted_count;
    
    -- Xóa payments liên quan đến bookings của nhà hàng này
    DELETE FROM payment 
    WHERE booking_id IN (
        SELECT booking_id FROM booking WHERE restaurant_id = v_restaurant_id
    );
    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã xóa % payments', v_deleted_count;
    
    -- Xóa booking_dishes liên quan đến bookings của nhà hàng này
    DELETE FROM booking_dish 
    WHERE booking_id IN (
        SELECT booking_id FROM booking WHERE restaurant_id = v_restaurant_id
    );
    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã xóa % booking_dishes', v_deleted_count;
    
    -- Xóa booking_services liên quan đến bookings của nhà hàng này
    DELETE FROM booking_service 
    WHERE booking_id IN (
        SELECT booking_id FROM booking WHERE restaurant_id = v_restaurant_id
    );
    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã xóa % booking_services', v_deleted_count;
    
    -- Xóa booking_tables liên quan đến bookings của nhà hàng này
    DELETE FROM booking_table 
    WHERE booking_id IN (
        SELECT booking_id FROM booking WHERE restaurant_id = v_restaurant_id
    );
    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã xóa % booking_tables', v_deleted_count;
    
    -- Xóa voucher_redemptions liên quan đến bookings của nhà hàng này (còn lại qua booking_id)
    DELETE FROM voucher_redemption 
    WHERE booking_id IN (
        SELECT booking_id FROM booking WHERE restaurant_id = v_restaurant_id
    );
    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã xóa % voucher_redemptions (qua bookings)', v_deleted_count;
    
    -- Xóa internal_notes liên quan đến bookings của nhà hàng này
    BEGIN
        DELETE FROM internal_notes 
        WHERE booking_id IN (
            SELECT booking_id FROM booking WHERE restaurant_id = v_restaurant_id
        );
        GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
        RAISE NOTICE '   ✅ Đã xóa % internal_notes', v_deleted_count;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE '   ⚠️  Không thể xóa internal_notes (bảng có thể không tồn tại)';
    END;
    
    -- Xóa communication_history liên quan đến bookings của nhà hàng này
    BEGIN
        DELETE FROM communication_history 
        WHERE booking_id IN (
            SELECT booking_id FROM booking WHERE restaurant_id = v_restaurant_id
        );
        GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
        RAISE NOTICE '   ✅ Đã xóa % communication_history records', v_deleted_count;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE '   ⚠️  Không thể xóa communication_history (bảng có thể không tồn tại)';
    END;
    
    -- Xóa bookings của nhà hàng này
    DELETE FROM booking WHERE restaurant_id = v_restaurant_id;
    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã xóa % bookings', v_deleted_count;
    
    -- =====================================================
    -- BƯỚC 2: XÓA CÁC BẢN GHI TRỰC TIẾP LIÊN QUAN ĐẾN RESTAURANT
    -- =====================================================
    
    -- Xóa booking_tables liên quan đến tables của nhà hàng (nếu còn sót)
    DELETE FROM booking_table 
    WHERE table_id IN (
        SELECT table_id FROM restaurant_table WHERE restaurant_id = v_restaurant_id
    );
    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã xóa thêm % booking_tables (qua tables)', v_deleted_count;
    
    -- Xóa restaurant_tables
    DELETE FROM restaurant_table WHERE restaurant_id = v_restaurant_id;
    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã xóa % restaurant_tables', v_deleted_count;
    
    -- Xóa dishes
    DELETE FROM dish WHERE restaurant_id = v_restaurant_id;
    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã xóa % dishes', v_deleted_count;
    
    -- Xóa restaurant_services
    DELETE FROM restaurant_service WHERE restaurant_id = v_restaurant_id;
    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã xóa % restaurant_services', v_deleted_count;
    
    -- Xóa restaurant_media
    DELETE FROM restaurant_media WHERE restaurant_id = v_restaurant_id;
    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã xóa % restaurant_media', v_deleted_count;
    
    -- Xóa reviews
    DELETE FROM review WHERE restaurant_id = v_restaurant_id;
    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã xóa % reviews', v_deleted_count;
    
    -- Xóa review_report_evidence TRƯỚC (vì có foreign key đến review_report)
    DELETE FROM review_report_evidence 
    WHERE report_id IN (
        SELECT report_id FROM review_report WHERE restaurant_id = v_restaurant_id
    );
    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã xóa % review_report_evidence records', v_deleted_count;
    
    -- Xóa review_reports liên quan đến reviews của nhà hàng này
    DELETE FROM review_report 
    WHERE restaurant_id = v_restaurant_id;
    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã xóa % review_reports', v_deleted_count;
    
    -- Xóa customer_favorites
    DELETE FROM customer_favorite WHERE restaurant_id = v_restaurant_id;
    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã xóa % customer_favorites', v_deleted_count;
    
    -- Xóa vouchers (chỉ xóa vouchers của nhà hàng, không xóa admin vouchers)
    DELETE FROM voucher WHERE restaurant_id = v_restaurant_id;
    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã xóa % vouchers', v_deleted_count;
    
    -- Xóa waitlists
    DELETE FROM waitlist WHERE restaurant_id = v_restaurant_id;
    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã xóa % waitlists', v_deleted_count;
    
    -- Xóa restaurant_availability
    DELETE FROM restaurant_availability WHERE restaurant_id = v_restaurant_id;
    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã xóa % restaurant_availability records', v_deleted_count;
    
    -- Xóa withdrawal_requests
    DELETE FROM withdrawal_request WHERE restaurant_id = v_restaurant_id;
    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã xóa % withdrawal_requests', v_deleted_count;
    
    -- Lưu ý: refund_requests đã được xóa ở BƯỚC 1
    
    -- Xóa restaurant_bank_accounts
    DELETE FROM restaurant_bank_account WHERE restaurant_id = v_restaurant_id;
    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã xóa % restaurant_bank_accounts', v_deleted_count;
    
    -- Xóa restaurant_balance
    DELETE FROM restaurant_balance WHERE restaurant_id = v_restaurant_id;
    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã xóa % restaurant_balance records', v_deleted_count;
    
    -- Xóa restaurant_contract
    DELETE FROM restaurant_contract WHERE restaurant_id = v_restaurant_id;
    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã xóa % restaurant_contract records', v_deleted_count;
    
    -- Xóa messages TRƯỚC (vì có foreign key đến chat_room)
    DELETE FROM message 
    WHERE room_id IN (
        SELECT room_id FROM chat_room WHERE restaurant_id = v_restaurant_id
    );
    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã xóa % messages', v_deleted_count;
    
    -- Xóa chat_rooms (nếu có restaurant_id)
    DELETE FROM chat_room WHERE restaurant_id = v_restaurant_id;
    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
    RAISE NOTICE '   ✅ Đã xóa % chat_rooms', v_deleted_count;
    
    -- Xóa ai_interactions (tên bảng là số nhiều)
    BEGIN
        DELETE FROM ai_interactions WHERE restaurant_id = v_restaurant_id;
        GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
        RAISE NOTICE '   ✅ Đã xóa % ai_interactions records', v_deleted_count;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE '   ⚠️  Không thể xóa ai_interactions (bảng có thể không tồn tại)';
    END;
    
    -- Xóa ai_recommendation_diversity
    BEGIN
        DELETE FROM ai_recommendation_diversity WHERE restaurant_id = v_restaurant_id;
        GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
        RAISE NOTICE '   ✅ Đã xóa % ai_recommendation_diversity records', v_deleted_count;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE '   ⚠️  Không thể xóa ai_recommendation_diversity (bảng có thể không tồn tại)';
    END;
    
    -- Xóa audit_log (nếu có restaurant_id)
    BEGIN
        DELETE FROM audit_log WHERE restaurant_id = v_restaurant_id;
        GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
        RAISE NOTICE '   ✅ Đã xóa % audit_log records', v_deleted_count;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE '   ⚠️  Không thể xóa audit_log (bảng có thể không tồn tại hoặc không có restaurant_id)';
    END;
    
    -- =====================================================
    -- BƯỚC 3: XÓA RESTAURANT_PROFILE
    -- =====================================================
    
    DELETE FROM restaurant_profile WHERE restaurant_id = v_restaurant_id;
    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
    
    IF v_deleted_count > 0 THEN
        RAISE NOTICE '';
        RAISE NOTICE '✅ ✅ ✅ HOÀN TẤT!';
        RAISE NOTICE '   Đã xóa thành công nhà hàng "%" (ID: %) và tất cả dữ liệu liên quan', v_restaurant_name, v_restaurant_id;
    ELSE
        RAISE NOTICE '';
        RAISE NOTICE '⚠️ Không thể xóa restaurant_profile. Có thể đã bị xóa trước đó hoặc có lỗi xảy ra.';
    END IF;
    
END $$;

-- Kiểm tra lại xem nhà hàng đã bị xóa chưa
SELECT 
    CASE 
        WHEN EXISTS (SELECT 1 FROM restaurant_profile WHERE restaurant_name ILIKE '%AI%') 
        THEN '❌ Nhà hàng "AI" vẫn còn tồn tại trong database!'
        ELSE '✅ Nhà hàng "AI" đã được xóa thành công!'
    END AS deletion_status;




