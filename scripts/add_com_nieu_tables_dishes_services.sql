-- =====================================================
-- SQL Script: Bổ sung BÀN, THỰC ĐƠN và DỊCH VỤ
-- cho nhà hàng "Cơm niêu 3 Cá Bống – Nguyễn Tri Phương"
-- =====================================================

-- Lấy restaurant_id
DO $$
DECLARE
    v_restaurant_id INTEGER;
BEGIN
    SELECT restaurant_id INTO v_restaurant_id
    FROM restaurant_profile
    WHERE restaurant_name LIKE '%Cơm niêu 3 Cá Bống%'
    LIMIT 1;
    
    IF v_restaurant_id IS NULL THEN
        RAISE EXCEPTION 'Không tìm thấy nhà hàng!';
    END IF;
    
    RAISE NOTICE 'Restaurant ID: %', v_restaurant_id;
END $$;

-- 1. THÊM BÀN (10 bàn) - Chỉ insert các cột bắt buộc
INSERT INTO restaurant_table (restaurant_id, table_name, capacity, status, depositamount)
SELECT 
    r.restaurant_id,
    'Bàn 1', 2, 'available', 50000
FROM restaurant_profile r
WHERE r.restaurant_name LIKE '%Cơm niêu 3 Cá Bống%'
  AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 1')

UNION ALL
SELECT r.restaurant_id, 'Bàn 2', 4, 'available', 100000
FROM restaurant_profile r
WHERE r.restaurant_name LIKE '%Cơm niêu 3 Cá Bống%'
  AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 2')

UNION ALL
SELECT r.restaurant_id, 'Bàn 3', 4, 'available', 100000
FROM restaurant_profile r
WHERE r.restaurant_name LIKE '%Cơm niêu 3 Cá Bống%'
  AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 3')

UNION ALL
SELECT r.restaurant_id, 'Bàn 4', 6, 'available', 150000
FROM restaurant_profile r
WHERE r.restaurant_name LIKE '%Cơm niêu 3 Cá Bống%'
  AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 4')

UNION ALL
SELECT r.restaurant_id, 'Bàn 5', 6, 'available', 150000
FROM restaurant_profile r
WHERE r.restaurant_name LIKE '%Cơm niêu 3 Cá Bống%'
  AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 5')

UNION ALL
SELECT r.restaurant_id, 'Bàn 6', 8, 'available', 200000
FROM restaurant_profile r
WHERE r.restaurant_name LIKE '%Cơm niêu 3 Cá Bống%'
  AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 6')

UNION ALL
SELECT r.restaurant_id, 'Bàn 7', 10, 'available', 250000
FROM restaurant_profile r
WHERE r.restaurant_name LIKE '%Cơm niêu 3 Cá Bống%'
  AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 7')

UNION ALL
SELECT r.restaurant_id, 'Phòng VIP 1', 12, 'available', 300000
FROM restaurant_profile r
WHERE r.restaurant_name LIKE '%Cơm niêu 3 Cá Bống%'
  AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Phòng VIP 1')

UNION ALL
SELECT r.restaurant_id, 'Phòng VIP 2', 15, 'available', 400000
FROM restaurant_profile r
WHERE r.restaurant_name LIKE '%Cơm niêu 3 Cá Bống%'
  AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Phòng VIP 2')

UNION ALL
SELECT r.restaurant_id, 'Sân ngoài trời', 20, 'available', 500000
FROM restaurant_profile r
WHERE r.restaurant_name LIKE '%Cơm niêu 3 Cá Bống%'
  AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Sân ngoài trời');

-- 2. THÊM MÓN ĂN (8 món - bằng số ảnh gallery)
INSERT INTO dish (restaurant_id, name, description, price, category, status)
SELECT r.restaurant_id, 'Cơm niêu cá bống nướng than hoa', 'Cơm niêu truyền thống với cá bống tươi nướng than hoa', 85000, 'Món chính', 'AVAILABLE'
FROM restaurant_profile r
WHERE r.restaurant_name LIKE '%Cơm niêu 3 Cá Bống%'
  AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Cơm niêu cá bống nướng than hoa');

INSERT INTO dish (restaurant_id, name, description, price, category, status)
SELECT r.restaurant_id, 'Cơm niêu cá bống kho tộ', 'Cá bống kho tộ đậm đà, ăn kèm cơm niêu', 90000, 'Món chính', 'AVAILABLE'
FROM restaurant_profile r
WHERE r.restaurant_name LIKE '%Cơm niêu 3 Cá Bống%'
  AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Cơm niêu cá bống kho tộ');

INSERT INTO dish (restaurant_id, name, description, price, category, status)
SELECT r.restaurant_id, 'Cơm niêu thịt kho tàu', 'Thịt ba chỉ kho tàu thơm ngon', 95000, 'Món chính', 'AVAILABLE'
FROM restaurant_profile r
WHERE r.restaurant_name LIKE '%Cơm niêu 3 Cá Bống%'
  AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Cơm niêu thịt kho tàu');

INSERT INTO dish (restaurant_id, name, description, price, category, status)
SELECT r.restaurant_id, 'Cơm niêu gà nướng', 'Gà ta nướng than hoa, da giòn', 100000, 'Món chính', 'AVAILABLE'
FROM restaurant_profile r
WHERE r.restaurant_name LIKE '%Cơm niêu 3 Cá Bống%'
  AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Cơm niêu gà nướng');

INSERT INTO dish (restaurant_id, name, description, price, category, status)
SELECT r.restaurant_id, 'Cá bống kho tộ', 'Cá bống tươi kho với nước mắm, ớt, tiêu', 120000, 'Món phụ', 'AVAILABLE'
FROM restaurant_profile r
WHERE r.restaurant_name LIKE '%Cơm niêu 3 Cá Bống%'
  AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Cá bống kho tộ');

INSERT INTO dish (restaurant_id, name, description, price, category, status)
SELECT r.restaurant_id, 'Canh chua cá bống', 'Canh chua chua ngọt với cá bống tươi', 80000, 'Món phụ', 'AVAILABLE'
FROM restaurant_profile r
WHERE r.restaurant_name LIKE '%Cơm niêu 3 Cá Bống%'
  AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Canh chua cá bống');

INSERT INTO dish (restaurant_id, name, description, price, category, status)
SELECT r.restaurant_id, 'Cá bống chiên giòn', 'Cá bống chiên giòn rụm', 100000, 'Món phụ', 'AVAILABLE'
FROM restaurant_profile r
WHERE r.restaurant_name LIKE '%Cơm niêu 3 Cá Bống%'
  AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Cá bống chiên giòn');

INSERT INTO dish (restaurant_id, name, description, price, category, status)
SELECT r.restaurant_id, 'Gỏi cá bống tươi', 'Gỏi cá bống tươi sống, rau thơm', 90000, 'Khai vị', 'AVAILABLE'
FROM restaurant_profile r
WHERE r.restaurant_name LIKE '%Cơm niêu 3 Cá Bống%'
  AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Gỏi cá bống tươi');

-- 3. THÊM DỊCH VỤ (3 dịch vụ)
INSERT INTO restaurant_service (restaurant_id, name, category, description, price, status)
SELECT r.restaurant_id, 'Gọi món trước', 'Đặt món', 'Đặt món trước khi đến', 0, 'AVAILABLE'
FROM restaurant_profile r
WHERE r.restaurant_name LIKE '%Cơm niêu 3 Cá Bống%'
  AND NOT EXISTS (SELECT 1 FROM restaurant_service WHERE restaurant_id = r.restaurant_id AND name = 'Gọi món trước');

INSERT INTO restaurant_service (restaurant_id, name, category, description, price, status)
SELECT r.restaurant_id, 'Giao hàng tận nơi', 'Giao hàng', 'Giao hàng trong bán kính 5km', 20000, 'AVAILABLE'
FROM restaurant_profile r
WHERE r.restaurant_name LIKE '%Cơm niêu 3 Cá Bống%'
  AND NOT EXISTS (SELECT 1 FROM restaurant_service WHERE restaurant_id = r.restaurant_id AND name = 'Giao hàng tận nơi');

INSERT INTO restaurant_service (restaurant_id, name, category, description, price, status)
SELECT r.restaurant_id, 'Đặt bàn VIP', 'Đặt bàn', 'Đặt trước phòng VIP', 50000, 'AVAILABLE'
FROM restaurant_profile r
WHERE r.restaurant_name LIKE '%Cơm niêu 3 Cá Bống%'
  AND NOT EXISTS (SELECT 1 FROM restaurant_service WHERE restaurant_id = r.restaurant_id AND name = 'Đặt bàn VIP');

-- 4. THÊM ẢNH CHO MÓN ĂN (dùng gallery images đã có)
DO $$
DECLARE
    v_restaurant_id INTEGER;
    v_dish_id INTEGER;
    v_image_url TEXT;
    v_counter INTEGER := 0;
    v_gallery_urls TEXT[];
BEGIN
    SELECT restaurant_id INTO v_restaurant_id
    FROM restaurant_profile
    WHERE restaurant_name LIKE '%Cơm niêu 3 Cá Bống%'
    LIMIT 1;
    
    -- Lấy danh sách gallery URLs
    SELECT ARRAY_AGG(url ORDER BY created_at) INTO v_gallery_urls
    FROM restaurant_media
    WHERE restaurant_id = v_restaurant_id AND type = 'gallery';
    
    IF v_gallery_urls IS NULL THEN
        RAISE NOTICE 'Không có gallery images';
        RETURN;
    END IF;
    
    -- Gán ảnh cho từng món ăn
    FOR v_dish_id IN 
        SELECT dish_id FROM dish 
        WHERE restaurant_id = v_restaurant_id 
        ORDER BY dish_id DESC 
        LIMIT 8
    LOOP
        v_image_url := v_gallery_urls[(v_counter % array_length(v_gallery_urls, 1)) + 1];
        
        IF v_image_url IS NOT NULL AND NOT EXISTS (
            SELECT 1 FROM restaurant_media 
            WHERE restaurant_id = v_restaurant_id 
              AND type = 'dish' 
              AND url = v_image_url
        ) THEN
            INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
            VALUES (v_restaurant_id, 'dish', v_image_url, NOW());
        END IF;
        
        v_counter := v_counter + 1;
    END LOOP;
    
    RAISE NOTICE 'Đã thêm ảnh cho món ăn';
END $$;

-- =====================================================
-- VERIFICATION - Kiểm tra dữ liệu đã thêm
-- =====================================================

SELECT 
    'BÀN' as loai,
    COUNT(*) as so_luong
FROM restaurant_table
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Cơm niêu 3 Cá Bống%' LIMIT 1)

UNION ALL

SELECT 
    'MÓN ĂN' as loai,
    COUNT(*) as so_luong
FROM dish
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Cơm niêu 3 Cá Bống%' LIMIT 1)

UNION ALL

SELECT 
    'DỊCH VỤ' as loai,
    COUNT(*) as so_luong
FROM restaurant_service
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Cơm niêu 3 Cá Bống%' LIMIT 1)

UNION ALL

SELECT 
    'ẢNH GALLERY' as loai,
    COUNT(*) as so_luong
FROM restaurant_media
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Cơm niêu 3 Cá Bống%' LIMIT 1)
  AND type = 'gallery';
