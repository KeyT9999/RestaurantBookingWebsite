-- =====================================================
-- QUERY ĐƠN GIẢN: Kiểm tra các bảng liên quan đến nhà hàng
-- =====================================================
-- Thay đổi 'AVVVV' thành tên nhà hàng bạn muốn kiểm tra
-- =====================================================

SELECT 
    '1. booking' AS bảng,
    COUNT(*) AS số_bản_ghi
FROM booking WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name = 'AVVVV')

UNION ALL SELECT '2. restaurant_table', COUNT(*) FROM restaurant_table WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name = 'AVVVV')
UNION ALL SELECT '3. dish', COUNT(*) FROM dish WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name = 'AVVVV')
UNION ALL SELECT '4. restaurant_service', COUNT(*) FROM restaurant_service WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name = 'AVVVV')
UNION ALL SELECT '5. restaurant_media', COUNT(*) FROM restaurant_media WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name = 'AVVVV')
UNION ALL SELECT '6. review', COUNT(*) FROM review WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name = 'AVVVV')
UNION ALL SELECT '7. review_report', COUNT(*) FROM review_report WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name = 'AVVVV')
UNION ALL SELECT '8. customer_favorite', COUNT(*) FROM customer_favorite WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name = 'AVVVV')
UNION ALL SELECT '9. voucher', COUNT(*) FROM voucher WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name = 'AVVVV')
UNION ALL SELECT '10. waitlist', COUNT(*) FROM waitlist WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name = 'AVVVV')
UNION ALL SELECT '11. restaurant_availability', COUNT(*) FROM restaurant_availability WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name = 'AVVVV')
UNION ALL SELECT '12. withdrawal_request', COUNT(*) FROM withdrawal_request WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name = 'AVVVV')
UNION ALL SELECT '13. refund_request', COUNT(*) FROM refund_request WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name = 'AVVVV')
UNION ALL SELECT '14. restaurant_bank_account', COUNT(*) FROM restaurant_bank_account WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name = 'AVVVV')
UNION ALL SELECT '15. restaurant_balance', COUNT(*) FROM restaurant_balance WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name = 'AVVVV')
UNION ALL SELECT '16. restaurant_contract', COUNT(*) FROM restaurant_contract WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name = 'AVVVV')
UNION ALL SELECT '17. chat_room', COUNT(*) FROM chat_room WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name = 'AVVVV')
UNION ALL SELECT '18. payment', COUNT(*) FROM payment WHERE booking_id IN (SELECT booking_id FROM booking WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name = 'AVVVV'))
UNION ALL SELECT '19. booking_dish', COUNT(*) FROM booking_dish WHERE booking_id IN (SELECT booking_id FROM booking WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name = 'AVVVV'))
UNION ALL SELECT '20. booking_service', COUNT(*) FROM booking_service WHERE booking_id IN (SELECT booking_id FROM booking WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name = 'AVVVV'))
UNION ALL SELECT '21. booking_table', COUNT(*) FROM booking_table WHERE booking_id IN (SELECT booking_id FROM booking WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name = 'AVVVV'))
UNION ALL SELECT '22. message', COUNT(*) FROM message WHERE room_id IN (SELECT room_id FROM chat_room WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name = 'AVVVV'))
UNION ALL SELECT '23. review_report_evidence', COUNT(*) FROM review_report_evidence WHERE report_id IN (SELECT report_id FROM review_report WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name = 'AVVVV'))
UNION ALL SELECT '24. voucher_redemption (booking)', COUNT(*) FROM voucher_redemption WHERE booking_id IN (SELECT booking_id FROM booking WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name = 'AVVVV'))
UNION ALL SELECT '25. voucher_redemption (payment)', COUNT(*) FROM voucher_redemption WHERE payment_id IN (SELECT payment_id FROM payment WHERE booking_id IN (SELECT booking_id FROM booking WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name = 'AVVVV')))

ORDER BY số_bản_ghi DESC;




