-- Populate dishes, tables, and extra services for THE GRILL
-- Auto-detects restaurant_id by name = 'The Grill'

DO $$
DECLARE
    v_restaurant_id INTEGER;
BEGIN
    SELECT restaurant_id
    INTO v_restaurant_id
    FROM restaurant_profile
    WHERE restaurant_name = 'The Grill'
    ORDER BY created_at DESC
    LIMIT 1;

    IF v_restaurant_id IS NULL THEN
        RAISE EXCEPTION 'Không tìm thấy nhà hàng The Grill. Hãy chạy add_the_grill_restaurant.sql trước.';
    END IF;

    -- ===== Dishes =====
    INSERT INTO dish (restaurant_id, name, description, price, category, status)
    VALUES
    (v_restaurant_id, 'Beef Tenderloin Steak', 'Thăn nội bò Úc nướng than, sốt tiêu xanh', 950000, 'Steak', 'AVAILABLE'),
    (v_restaurant_id, 'Ribeye on the Bone', 'Ribeye hảo hạng nướng medium rare, bơ tỏi thảo mộc', 1200000, 'Steak', 'AVAILABLE'),
    (v_restaurant_id, 'Lobster Bisque', 'Súp tôm hùm cô đặc kiểu Pháp', 320000, 'Soup', 'AVAILABLE'),
    (v_restaurant_id, 'Truffle Mashed Potatoes', 'Khoai tây nghiền truffle kem béo', 180000, 'Side', 'AVAILABLE'),
    (v_restaurant_id, 'Chocolate Lava Cake', 'Bánh chocolate nhân chảy, kem vani', 220000, 'Dessert', 'AVAILABLE');

    -- ===== Tables =====
    INSERT INTO restaurant_table (restaurant_id, table_name, capacity, status, depositamount)
    VALUES
    (v_restaurant_id, 'Table A1', 2, 'AVAILABLE', 0),
    (v_restaurant_id, 'Table A2', 2, 'AVAILABLE', 0),
    (v_restaurant_id, 'Table B1', 4, 'AVAILABLE', 0),
    (v_restaurant_id, 'Table B2', 4, 'AVAILABLE', 0),
    (v_restaurant_id, 'Chef''s Table', 6, 'AVAILABLE', 0);

    -- ===== Extra Services =====
    INSERT INTO restaurant_service (restaurant_id, name, category, description, price, status, created_at, updated_at)
    VALUES
    (v_restaurant_id, 'Wine Pairing', 'Beverage', 'Chọn rượu vang theo món', 350000, 'AVAILABLE', NOW(), NOW()),
    (v_restaurant_id, 'Birthday Setup', 'Event', 'Trang trí sinh nhật, bánh nhỏ và nến', 500000, 'AVAILABLE', NOW(), NOW()),
    (v_restaurant_id, 'Private Dining Room', 'Experience', 'Phòng riêng tối đa 8 khách', 1500000, 'AVAILABLE', NOW(), NOW()),
    (v_restaurant_id, 'Live Music Request', 'Entertainment', 'Sắp xếp nghệ sĩ/playlist theo yêu cầu', 700000, 'AVAILABLE', NOW(), NOW());

    RAISE NOTICE 'Inserted menu/tables/services for restaurant_id=%', v_restaurant_id;
END $$;

-- Quick verify
SELECT 'DISHES' AS section, COUNT(*) AS total
FROM dish
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name = 'The Grill' ORDER BY created_at DESC LIMIT 1);

SELECT 'TABLES' AS section, COUNT(*) AS total
FROM restaurant_table
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name = 'The Grill' ORDER BY created_at DESC LIMIT 1);

SELECT 'SERVICES' AS section, COUNT(*) AS total
FROM restaurant_service
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name = 'The Grill' ORDER BY created_at DESC LIMIT 1);





