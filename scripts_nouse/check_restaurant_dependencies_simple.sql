-- =====================================================
-- SQL Script: KIỂM TRA NHANH CÁC BẢNG LIÊN QUAN
-- =====================================================
-- Script đơn giản để xem nhanh các bảng liên quan đến nhà hàng
-- Thay đổi tên nhà hàng ở dưới
-- =====================================================

-- Thay đổi tên nhà hàng ở đây
\set restaurant_name 'AVVVV'

-- Hoặc dùng ID: \set restaurant_id 36

-- Tìm restaurant_id
SELECT 
    restaurant_id,
    restaurant_name,
    owner_id
INTO TEMP temp_restaurant
FROM restaurant_profile 
WHERE restaurant_name = :'restaurant_name';

-- Hiển thị thông tin nhà hàng
SELECT * FROM temp_restaurant;

-- =====================================================
-- ĐẾM SỐ BẢN GHI TRONG CÁC BẢNG LIÊN QUAN
-- =====================================================

WITH restaurant_info AS (
    SELECT restaurant_id FROM temp_restaurant
),
booking_info AS (
    SELECT booking_id FROM booking 
    WHERE restaurant_id IN (SELECT restaurant_id FROM restaurant_info)
),
payment_info AS (
    SELECT payment_id FROM payment 
    WHERE booking_id IN (SELECT booking_id FROM booking_info)
),
chat_room_info AS (
    SELECT room_id FROM chat_room 
    WHERE restaurant_id IN (SELECT restaurant_id FROM restaurant_info)
),
review_report_info AS (
    SELECT report_id FROM review_report 
    WHERE restaurant_id IN (SELECT restaurant_id FROM restaurant_info)
)
SELECT 
    'restaurant_profile' AS table_name,
    COUNT(*) AS record_count,
    'Trực tiếp' AS relationship_type
FROM restaurant_profile 
WHERE restaurant_id IN (SELECT restaurant_id FROM restaurant_info)

UNION ALL

SELECT 'booking', COUNT(*), 'Trực tiếp' 
FROM booking WHERE restaurant_id IN (SELECT restaurant_id FROM restaurant_info)

UNION ALL

SELECT 'restaurant_table', COUNT(*), 'Trực tiếp' 
FROM restaurant_table WHERE restaurant_id IN (SELECT restaurant_id FROM restaurant_info)

UNION ALL

SELECT 'dish', COUNT(*), 'Trực tiếp' 
FROM dish WHERE restaurant_id IN (SELECT restaurant_id FROM restaurant_info)

UNION ALL

SELECT 'restaurant_service', COUNT(*), 'Trực tiếp' 
FROM restaurant_service WHERE restaurant_id IN (SELECT restaurant_id FROM restaurant_info)

UNION ALL

SELECT 'restaurant_media', COUNT(*), 'Trực tiếp' 
FROM restaurant_media WHERE restaurant_id IN (SELECT restaurant_id FROM restaurant_info)

UNION ALL

SELECT 'review', COUNT(*), 'Trực tiếp' 
FROM review WHERE restaurant_id IN (SELECT restaurant_id FROM restaurant_info)

UNION ALL

SELECT 'review_report', COUNT(*), 'Trực tiếp' 
FROM review_report WHERE restaurant_id IN (SELECT restaurant_id FROM restaurant_info)

UNION ALL

SELECT 'customer_favorite', COUNT(*), 'Trực tiếp' 
FROM customer_favorite WHERE restaurant_id IN (SELECT restaurant_id FROM restaurant_info)

UNION ALL

SELECT 'voucher', COUNT(*), 'Trực tiếp' 
FROM voucher WHERE restaurant_id IN (SELECT restaurant_id FROM restaurant_info)

UNION ALL

SELECT 'waitlist', COUNT(*), 'Trực tiếp' 
FROM waitlist WHERE restaurant_id IN (SELECT restaurant_id FROM restaurant_info)

UNION ALL

SELECT 'restaurant_availability', COUNT(*), 'Trực tiếp' 
FROM restaurant_availability WHERE restaurant_id IN (SELECT restaurant_id FROM restaurant_info)

UNION ALL

SELECT 'withdrawal_request', COUNT(*), 'Trực tiếp' 
FROM withdrawal_request WHERE restaurant_id IN (SELECT restaurant_id FROM restaurant_info)

UNION ALL

SELECT 'refund_request (trực tiếp)', COUNT(*), 'Trực tiếp' 
FROM refund_request WHERE restaurant_id IN (SELECT restaurant_id FROM restaurant_info)

UNION ALL

SELECT 'restaurant_bank_account', COUNT(*), 'Trực tiếp' 
FROM restaurant_bank_account WHERE restaurant_id IN (SELECT restaurant_id FROM restaurant_info)

UNION ALL

SELECT 'restaurant_balance', COUNT(*), 'Trực tiếp' 
FROM restaurant_balance WHERE restaurant_id IN (SELECT restaurant_id FROM restaurant_info)

UNION ALL

SELECT 'restaurant_contract', COUNT(*), 'Trực tiếp' 
FROM restaurant_contract WHERE restaurant_id IN (SELECT restaurant_id FROM restaurant_info)

UNION ALL

SELECT 'chat_room', COUNT(*), 'Trực tiếp' 
FROM chat_room WHERE restaurant_id IN (SELECT restaurant_id FROM restaurant_info)

UNION ALL

SELECT 'payment', COUNT(*), 'Qua booking' 
FROM payment WHERE booking_id IN (SELECT booking_id FROM booking_info)

UNION ALL

SELECT 'booking_dish', COUNT(*), 'Qua booking' 
FROM booking_dish WHERE booking_id IN (SELECT booking_id FROM booking_info)

UNION ALL

SELECT 'booking_service', COUNT(*), 'Qua booking' 
FROM booking_service WHERE booking_id IN (SELECT booking_id FROM booking_info)

UNION ALL

SELECT 'booking_table', COUNT(*), 'Qua booking' 
FROM booking_table WHERE booking_id IN (SELECT booking_id FROM booking_info)

UNION ALL

SELECT 'voucher_redemption (qua booking)', COUNT(*), 'Qua booking' 
FROM voucher_redemption WHERE booking_id IN (SELECT booking_id FROM booking_info)

UNION ALL

SELECT 'voucher_redemption (qua payment)', COUNT(*), 'Qua payment' 
FROM voucher_redemption WHERE payment_id IN (SELECT payment_id FROM payment_info)

UNION ALL

SELECT 'refund_request (qua payment)', COUNT(*), 'Qua payment' 
FROM refund_request WHERE payment_id IN (SELECT payment_id FROM payment_info)

UNION ALL

SELECT 'message', COUNT(*), 'Qua chat_room' 
FROM message WHERE room_id IN (SELECT room_id FROM chat_room_info)

UNION ALL

SELECT 'review_report_evidence', COUNT(*), 'Qua review_report' 
FROM review_report_evidence WHERE report_id IN (SELECT report_id FROM review_report_info)

ORDER BY 
    CASE relationship_type 
        WHEN 'Trực tiếp' THEN 1 
        WHEN 'Qua booking' THEN 2 
        WHEN 'Qua payment' THEN 3 
        ELSE 4 
    END,
    table_name;

-- Dọn dẹp
DROP TABLE IF EXISTS temp_restaurant;




