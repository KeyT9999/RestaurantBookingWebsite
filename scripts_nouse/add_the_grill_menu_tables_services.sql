-- Populate menu dishes, dining tables, and extra services for The Grill
-- How to use in pgAdmin:
--   1) Set the restaurant id below (v_restaurant_id := <ID>;)
--   2) Run the whole script

DO $$
DECLARE
    -- Leave NULL to auto-detect by name; or set an explicit ID here
    v_restaurant_id INTEGER := NULL;
    v_name TEXT := 'The Grill';
BEGIN
    -- Auto-detect latest restaurant_id by name if not provided
    IF v_restaurant_id IS NULL THEN
        SELECT rp.restaurant_id
        INTO v_restaurant_id
        FROM restaurant_profile rp
        WHERE rp.restaurant_name = v_name
        ORDER BY rp.created_at DESC
        LIMIT 1;
    END IF;

    -- Validate restaurant exists
    IF NOT EXISTS (SELECT 1 FROM restaurant_profile WHERE restaurant_id = v_restaurant_id) THEN
        RAISE EXCEPTION 'restaurant_id % not found in restaurant_profile (name lookup=%)', v_restaurant_id, v_name;
    END IF;

    -- ===== Dishes (4-5 items) =====
    INSERT INTO dish (restaurant_id, name, description, price, category, status)
    VALUES
    (v_restaurant_id, 'Beef Tenderloin Steak', 'Thăn nội bò Úc nướng than, sốt tiêu xanh', 950000, 'Steak', 'AVAILABLE'),
    (v_restaurant_id, 'Ribeye on the Bone', 'Ribeye hảo hạng nướng medium rare, bơ tỏi thảo mộc', 1200000, 'Steak', 'AVAILABLE'),
    (v_restaurant_id, 'Lobster Bisque', 'Súp tôm hùm cô đặc kiểu Pháp', 320000, 'Soup', 'AVAILABLE'),
    (v_restaurant_id, 'Truffle Mashed Potatoes', 'Khoai tây nghiền truffle kem béo', 180000, 'Side', 'AVAILABLE'),
    (v_restaurant_id, 'Chocolate Lava Cake', 'Bánh chocolate nhân chảy, kem vani', 220000, 'Dessert', 'AVAILABLE');

    -- ===== Dining Tables (5 tables) =====
    INSERT INTO restaurant_table (restaurant_id, table_name, capacity, status, depositamount)
    VALUES
    (v_restaurant_id, 'Table A1', 2, 'AVAILABLE', 0),
    (v_restaurant_id, 'Table A2', 2, 'AVAILABLE', 0),
    (v_restaurant_id, 'Table B1', 4, 'AVAILABLE', 0),
    (v_restaurant_id, 'Table B2', 4, 'AVAILABLE', 0),
    (v_restaurant_id, 'Chef''s Table', 6, 'AVAILABLE', 0);

    -- ===== Extra Services (4 items) =====
    INSERT INTO restaurant_service (restaurant_id, name, category, description, price, status, created_at, updated_at)
    VALUES
    (v_restaurant_id, 'Wine Pairing', 'Beverage', 'Chọn rượu vang theo món', 350000, 'AVAILABLE', NOW(), NOW()),
    (v_restaurant_id, 'Birthday Setup', 'Event', 'Trang trí sinh nhật, bánh nhỏ và nến', 500000, 'AVAILABLE', NOW(), NOW()),
    (v_restaurant_id, 'Private Dining Room', 'Experience', 'Phòng riêng tối đa 8 khách', 1500000, 'AVAILABLE', NOW(), NOW()),
    (v_restaurant_id, 'Live Music Request', 'Entertainment', 'Sắp xếp nghệ sĩ/playlist theo yêu cầu', 700000, 'AVAILABLE', NOW(), NOW());

    RAISE NOTICE 'Inserted data for restaurant_id=%', v_restaurant_id;
    RAISE NOTICE 'DISH COUNT: %', (SELECT COUNT(*) FROM dish WHERE restaurant_id = v_restaurant_id);
    RAISE NOTICE 'TABLE COUNT: %', (SELECT COUNT(*) FROM restaurant_table WHERE restaurant_id = v_restaurant_id);
    RAISE NOTICE 'SERVICE COUNT: %', (SELECT COUNT(*) FROM restaurant_service WHERE restaurant_id = v_restaurant_id);
END $$;


