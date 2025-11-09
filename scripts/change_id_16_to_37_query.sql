-- =====================================================
-- QUERY ĐỔI RESTAURANT_ID: 16 → 37
-- =====================================================

-- Kiểm tra trước
SELECT restaurant_id, restaurant_name FROM restaurant_profile WHERE restaurant_id IN (16, 37);

-- BƯỚC 1: Cập nhật restaurant_profile TRƯỚC (quan trọng!)
-- Tạm thời disable foreign key check bằng cách update trực tiếp
UPDATE restaurant_profile SET restaurant_id = 37 WHERE restaurant_id = 16;

-- BƯỚC 2: Cập nhật tất cả các bảng có foreign key đến restaurant_id
UPDATE booking SET restaurant_id = 37 WHERE restaurant_id = 16;
UPDATE restaurant_table SET restaurant_id = 37 WHERE restaurant_id = 16;
UPDATE dish SET restaurant_id = 37 WHERE restaurant_id = 16;
UPDATE restaurant_service SET restaurant_id = 37 WHERE restaurant_id = 16;
UPDATE restaurant_media SET restaurant_id = 37 WHERE restaurant_id = 16;
UPDATE review SET restaurant_id = 37 WHERE restaurant_id = 16;
UPDATE review_report SET restaurant_id = 37 WHERE restaurant_id = 16;
UPDATE customer_favorite SET restaurant_id = 37 WHERE restaurant_id = 16;
UPDATE voucher SET restaurant_id = 37 WHERE restaurant_id = 16;
UPDATE waitlist SET restaurant_id = 37 WHERE restaurant_id = 16;
UPDATE restaurant_availability SET restaurant_id = 37 WHERE restaurant_id = 16;
UPDATE withdrawal_request SET restaurant_id = 37 WHERE restaurant_id = 16;
UPDATE refund_request SET restaurant_id = 37 WHERE restaurant_id = 16;
UPDATE restaurant_bank_account SET restaurant_id = 37 WHERE restaurant_id = 16;
UPDATE restaurant_balance SET restaurant_id = 37 WHERE restaurant_id = 16;
UPDATE restaurant_contract SET restaurant_id = 37 WHERE restaurant_id = 16;
UPDATE chat_room SET restaurant_id = 37 WHERE restaurant_id = 16;

-- Cập nhật sequence
SELECT setval('restaurant_profile_restaurant_id_seq', GREATEST(37, (SELECT MAX(restaurant_id) FROM restaurant_profile)));

-- Kiểm tra kết quả
SELECT restaurant_id, restaurant_name FROM restaurant_profile WHERE restaurant_id = 37;

