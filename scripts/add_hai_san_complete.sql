-- =====================================================
-- SQL Script: THÊM ĐẦY ĐỦ nhà hàng "Hải Sản Bà Cường – Hoàng Sa"
-- Owner: Taiphan
-- Bao gồm: Nhà hàng + Ảnh + Bàn + Món ăn + Dịch vụ + Giá + Approve
-- =====================================================

-- =====================================================
-- PHẦN 1: THÊM NHÀ HÀNG
-- =====================================================

DO $$
DECLARE
    v_user_id UUID;
    v_owner_id UUID;
    v_restaurant_id INTEGER;
BEGIN
    -- Tìm user Taiphan
    SELECT id INTO v_user_id
    FROM users
    WHERE username = 'Taiphan';
    
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'User "Taiphan" not found! Please create the user first.';
    END IF;
    
    RAISE NOTICE 'Found user Taiphan: %', v_user_id;
    
    -- Tìm hoặc tạo RestaurantOwner
    SELECT owner_id INTO v_owner_id
    FROM restaurant_owner
    WHERE user_id = v_user_id;
    
    IF v_owner_id IS NULL THEN
        INSERT INTO restaurant_owner (owner_id, user_id, owner_name, created_at, updated_at)
        VALUES (
            gen_random_uuid(),
            v_user_id,
            COALESCE((SELECT full_name FROM users WHERE id = v_user_id), 'Taiphan'),
            NOW(),
            NOW()
        )
        RETURNING owner_id INTO v_owner_id;
        
        RAISE NOTICE 'Created RestaurantOwner: %', v_owner_id;
    ELSE
        RAISE NOTICE 'Found existing RestaurantOwner: %', v_owner_id;
    END IF;
    
    -- Kiểm tra nhà hàng đã tồn tại chưa
    SELECT restaurant_id INTO v_restaurant_id
    FROM restaurant_profile
    WHERE restaurant_name LIKE '%Hải Sản Bà Cường%' OR restaurant_name LIKE '%Hoàng Sa%';
    
    IF v_restaurant_id IS NOT NULL THEN
        RAISE NOTICE 'Restaurant already exists with ID: %. Skipping insert.', v_restaurant_id;
        RETURN;
    END IF;
    
    -- INSERT vào restaurant_profile với status PENDING
    INSERT INTO restaurant_profile (
        owner_id,
        restaurant_name,
        address,
        phone,
        description,
        cuisine_type,
        opening_hours,
        average_price,
        website_url,
        approval_status,
        approved_by,
        approved_at,
        contract_signed,
        contract_signed_at,
        terms_accepted,
        terms_accepted_at,
        terms_version,
        created_at,
        updated_at,
        hero_city,
        hero_headline,
        hero_subheadline,
        summary_highlights,
        signature_dishes,
        amenities,
        parking_details,
        booking_information,
        booking_notes
    )
    VALUES (
        v_owner_id,
        'Hải Sản Bà Cường – Hoàng Sa',
        '123 Hoàng Sa, Phường 1, Quận Tân Bình, TP. Hồ Chí Minh',
        '0909123457',
        'Nhà hàng hải sản tươi sống với không gian rộng rãi, thoáng mát. Chuyên về các món hải sản tươi sống, được chế biến theo phong cách đặc trưng miền biển. Hải sản được nhập mỗi ngày từ các vùng biển, đảm bảo tươi ngon nhất. Không gian phù hợp cho gia đình, nhóm bạn và các buổi tiệc.',
        'Hải sản',
        '10:00 - 22:00',
        250000.00,
        NULL,
        'PENDING',
        NULL,
        NULL,
        FALSE,
        NULL,
        TRUE,
        NOW(),
        '1.0',
        NOW(),
        NOW(),
        'TP. Hồ Chí Minh',
        'Hải sản tươi sống đặc sản miền biển',
        'Tươi ngon mỗi ngày, hương vị đậm đà',
        'Hải sản tươi sống, Chế biến đa dạng, Không gian rộng rãi, Phù hợp gia đình, Nhóm lớn',
        'Cá mú hấp xì dầu, Tôm sú nướng muối ớt, Cua rang me, Nghêu hấp thái, Cá điêu hồng chiên giòn',
        'WiFi miễn phí, Điều hòa, Chỗ đậu xe, Khu vực ngoài trời, Phục vụ nhanh, TV màn hình lớn',
        'Có chỗ đậu xe máy và ô tô miễn phí. Bãi đậu xe rộng rãi phía trước nhà hàng.',
        'Đặt bàn trước 1 giờ để đảm bảo có chỗ. Nhà hàng nhận đặt bàn từ 10:00 - 21:30 hàng ngày. Nhóm từ 6 người trở lên nên đặt trước.',
        'Khuyến mãi đặc biệt cho nhóm từ 6 người trở lên: giảm 10% tổng hóa đơn. Combo hải sản cho 2-4 người giảm 15%.'
    )
    RETURNING restaurant_id INTO v_restaurant_id;
    
    RAISE NOTICE '========================================';
    RAISE NOTICE '✅ RESTAURANT ADDED SUCCESSFULLY!';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Restaurant ID: %', v_restaurant_id;
    RAISE NOTICE 'Restaurant Name: Hải Sản Bà Cường – Hoàng Sa';
    RAISE NOTICE 'Owner: Taiphan';
    RAISE NOTICE 'Status: PENDING (cần admin duyệt)';
    RAISE NOTICE '========================================';
    RAISE NOTICE '';
    RAISE NOTICE '📋 BƯỚC TIẾP THEO:';
    RAISE NOTICE '1. Chạy Python script: python scripts/upload_hai_san_images.py <restaurant_id>';
    RAISE NOTICE '2. Nhập restaurant_id: %', v_restaurant_id;
    RAISE NOTICE '3. Sau khi upload xong, file insert_hai_san_images.sql sẽ được tạo';
    RAISE NOTICE '4. Mở file insert_hai_san_images.sql và copy phần INSERT ảnh vào đây (PHẦN 2)';
    RAISE NOTICE '5. Sau đó tiếp tục chạy PHẦN 3, 4, 5, 6';
    RAISE NOTICE '========================================';
    
END $$;

-- =====================================================
-- PHẦN 2: THÊM ẢNH (CHẠY SAU KHI UPLOAD ẢNH LÊN CLOUDINARY)
-- =====================================================
-- 
-- HƯỚNG DẪN:
-- 1. Chạy: python scripts/upload_hai_san_images.py <restaurant_id>
-- 2. Sau khi upload xong, mở file: scripts/insert_hai_san_images.sql
-- 3. Copy phần INSERT ảnh từ file đó và paste vào đây (thay thế comment này)
-- 4. Hoặc chạy trực tiếp file insert_hai_san_images.sql trước
-- 
-- Format ví dụ:
-- INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
-- VALUES (restaurant_id, 'cover', 'https://res.cloudinary.com/...', NOW());
-- INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
-- VALUES (restaurant_id, 'gallery', 'https://res.cloudinary.com/...', NOW());
-- ... (11 ảnh gallery nữa)
--

-- =====================================================
-- PHẦN 3: THÊM BÀN, MÓN ĂN VÀ DỊCH VỤ
-- =====================================================

-- 3.1. THÊM BÀN (10 bàn)
INSERT INTO restaurant_table (restaurant_id, table_name, capacity, status, depositamount)
SELECT 
    r.restaurant_id,
    'Bàn 1', 2, 'available', 50000
FROM restaurant_profile r
WHERE (r.restaurant_name LIKE '%Hải Sản Bà Cường%' OR r.restaurant_name LIKE '%Hoàng Sa%')
  AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 1')

UNION ALL
SELECT r.restaurant_id, 'Bàn 2', 4, 'available', 50000
FROM restaurant_profile r
WHERE (r.restaurant_name LIKE '%Hải Sản Bà Cường%' OR r.restaurant_name LIKE '%Hoàng Sa%')
  AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 2')

UNION ALL
SELECT r.restaurant_id, 'Bàn 3', 4, 'available', 50000
FROM restaurant_profile r
WHERE (r.restaurant_name LIKE '%Hải Sản Bà Cường%' OR r.restaurant_name LIKE '%Hoàng Sa%')
  AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 3')

UNION ALL
SELECT r.restaurant_id, 'Bàn 4', 6, 'available', 50000
FROM restaurant_profile r
WHERE (r.restaurant_name LIKE '%Hải Sản Bà Cường%' OR r.restaurant_name LIKE '%Hoàng Sa%')
  AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 4')

UNION ALL
SELECT r.restaurant_id, 'Bàn 5', 6, 'available', 50000
FROM restaurant_profile r
WHERE (r.restaurant_name LIKE '%Hải Sản Bà Cường%' OR r.restaurant_name LIKE '%Hoàng Sa%')
  AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 5')

UNION ALL
SELECT r.restaurant_id, 'Bàn 6', 8, 'available', 50000
FROM restaurant_profile r
WHERE (r.restaurant_name LIKE '%Hải Sản Bà Cường%' OR r.restaurant_name LIKE '%Hoàng Sa%')
  AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 6')

UNION ALL
SELECT r.restaurant_id, 'Bàn 7', 10, 'available', 50000
FROM restaurant_profile r
WHERE (r.restaurant_name LIKE '%Hải Sản Bà Cường%' OR r.restaurant_name LIKE '%Hoàng Sa%')
  AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 7')

UNION ALL
SELECT r.restaurant_id, 'Phòng VIP 1', 12, 'available', 50000
FROM restaurant_profile r
WHERE (r.restaurant_name LIKE '%Hải Sản Bà Cường%' OR r.restaurant_name LIKE '%Hoàng Sa%')
  AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Phòng VIP 1')

UNION ALL
SELECT r.restaurant_id, 'Phòng VIP 2', 15, 'available', 50000
FROM restaurant_profile r
WHERE (r.restaurant_name LIKE '%Hải Sản Bà Cường%' OR r.restaurant_name LIKE '%Hoàng Sa%')
  AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Phòng VIP 2')

UNION ALL
SELECT r.restaurant_id, 'Sân ngoài trời', 20, 'available', 50000
FROM restaurant_profile r
WHERE (r.restaurant_name LIKE '%Hải Sản Bà Cường%' OR r.restaurant_name LIKE '%Hoàng Sa%')
  AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Sân ngoài trời');

-- 3.2. THÊM MÓN ĂN (12 món - bằng số ảnh gallery)
INSERT INTO dish (restaurant_id, name, description, price, category, status)
SELECT r.restaurant_id, 'Cá mú hấp xì dầu', 'Cá mú tươi hấp với xì dầu, gừng, hành, thơm ngon đậm đà', 50000, 'Món chính', 'AVAILABLE'
FROM restaurant_profile r
WHERE (r.restaurant_name LIKE '%Hải Sản Bà Cường%' OR r.restaurant_name LIKE '%Hoàng Sa%')
  AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Cá mú hấp xì dầu');

INSERT INTO dish (restaurant_id, name, description, price, category, status)
SELECT r.restaurant_id, 'Tôm sú nướng muối ớt', 'Tôm sú tươi nướng với muối ớt, thơm lừng, đậm vị', 50000, 'Món chính', 'AVAILABLE'
FROM restaurant_profile r
WHERE (r.restaurant_name LIKE '%Hải Sản Bà Cường%' OR r.restaurant_name LIKE '%Hoàng Sa%')
  AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Tôm sú nướng muối ớt');

INSERT INTO dish (restaurant_id, name, description, price, category, status)
SELECT r.restaurant_id, 'Cua rang me', 'Cua tươi rang me chua ngọt, thịt cua chắc ngọt', 50000, 'Món chính', 'AVAILABLE'
FROM restaurant_profile r
WHERE (r.restaurant_name LIKE '%Hải Sản Bà Cường%' OR r.restaurant_name LIKE '%Hoàng Sa%')
  AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Cua rang me');

INSERT INTO dish (restaurant_id, name, description, price, category, status)
SELECT r.restaurant_id, 'Nghêu hấp thái', 'Nghêu tươi hấp với nước dừa, sả, ớt thái, thơm ngon', 50000, 'Món chính', 'AVAILABLE'
FROM restaurant_profile r
WHERE (r.restaurant_name LIKE '%Hải Sản Bà Cường%' OR r.restaurant_name LIKE '%Hoàng Sa%')
  AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Nghêu hấp thái');

INSERT INTO dish (restaurant_id, name, description, price, category, status)
SELECT r.restaurant_id, 'Cá điêu hồng chiên giòn', 'Cá điêu hồng tươi chiên giòn, ăn kèm nước mắm gừng', 50000, 'Món chính', 'AVAILABLE'
FROM restaurant_profile r
WHERE (r.restaurant_name LIKE '%Hải Sản Bà Cường%' OR r.restaurant_name LIKE '%Hoàng Sa%')
  AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Cá điêu hồng chiên giòn');

INSERT INTO dish (restaurant_id, name, description, price, category, status)
SELECT r.restaurant_id, 'Mực nướng sa tế', 'Mực tươi nướng với sa tế, thơm cay đậm đà', 50000, 'Món chính', 'AVAILABLE'
FROM restaurant_profile r
WHERE (r.restaurant_name LIKE '%Hải Sản Bà Cường%' OR r.restaurant_name LIKE '%Hoàng Sa%')
  AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Mực nướng sa tế');

INSERT INTO dish (restaurant_id, name, description, price, category, status)
SELECT r.restaurant_id, 'Sò điệp nướng phô mai', 'Sò điệp tươi nướng với phô mai, bơ tỏi, thơm ngon', 50000, 'Món chính', 'AVAILABLE'
FROM restaurant_profile r
WHERE (r.restaurant_name LIKE '%Hải Sản Bà Cường%' OR r.restaurant_name LIKE '%Hoàng Sa%')
  AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Sò điệp nướng phô mai');

INSERT INTO dish (restaurant_id, name, description, price, category, status)
SELECT r.restaurant_id, 'Cá hồng kho tộ', 'Cá hồng tươi kho tộ với nước mắm, ớt, tiêu', 50000, 'Món chính', 'AVAILABLE'
FROM restaurant_profile r
WHERE (r.restaurant_name LIKE '%Hải Sản Bà Cường%' OR r.restaurant_name LIKE '%Hoàng Sa%')
  AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Cá hồng kho tộ');

INSERT INTO dish (restaurant_id, name, description, price, category, status)
SELECT r.restaurant_id, 'Tôm càng nướng muối', 'Tôm càng tươi nướng muối, thịt chắc ngọt', 50000, 'Món chính', 'AVAILABLE'
FROM restaurant_profile r
WHERE (r.restaurant_name LIKE '%Hải Sản Bà Cường%' OR r.restaurant_name LIKE '%Hoàng Sa%')
  AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Tôm càng nướng muối');

INSERT INTO dish (restaurant_id, name, description, price, category, status)
SELECT r.restaurant_id, 'Cá ngừ đại dương nướng', 'Cá ngừ tươi nướng than hoa, thịt chắc ngọt', 50000, 'Món chính', 'AVAILABLE'
FROM restaurant_profile r
WHERE (r.restaurant_name LIKE '%Hải Sản Bà Cường%' OR r.restaurant_name LIKE '%Hoàng Sa%')
  AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Cá ngừ đại dương nướng');

INSERT INTO dish (restaurant_id, name, description, price, category, status)
SELECT r.restaurant_id, 'Gỏi cá trích', 'Gỏi cá trích tươi sống, rau thơm, chanh, đậu phộng', 50000, 'Khai vị', 'AVAILABLE'
FROM restaurant_profile r
WHERE (r.restaurant_name LIKE '%Hải Sản Bà Cường%' OR r.restaurant_name LIKE '%Hoàng Sa%')
  AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Gỏi cá trích');

INSERT INTO dish (restaurant_id, name, description, price, category, status)
SELECT r.restaurant_id, 'Canh chua cá lóc', 'Canh chua chua ngọt với cá lóc tươi, rau thơm', 50000, 'Món phụ', 'AVAILABLE'
FROM restaurant_profile r
WHERE (r.restaurant_name LIKE '%Hải Sản Bà Cường%' OR r.restaurant_name LIKE '%Hoàng Sa%')
  AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Canh chua cá lóc');

-- 3.3. THÊM ẢNH CHO MÓN ĂN (dùng gallery images đã có)
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
    WHERE restaurant_name LIKE '%Hải Sản Bà Cường%' OR restaurant_name LIKE '%Hoàng Sa%'
    LIMIT 1;
    
    IF v_restaurant_id IS NULL THEN
        RAISE NOTICE 'Không tìm thấy nhà hàng!';
        RETURN;
    END IF;
    
    -- Lấy danh sách gallery URLs
    SELECT ARRAY_AGG(url ORDER BY created_at) INTO v_gallery_urls
    FROM restaurant_media
    WHERE restaurant_id = v_restaurant_id AND type = 'gallery';
    
    IF v_gallery_urls IS NULL THEN
        RAISE NOTICE 'Không có gallery images. Vui lòng chạy PHẦN 2 trước!';
        RETURN;
    END IF;
    
    -- Gán ảnh cho từng món ăn
    FOR v_dish_id IN 
        SELECT dish_id FROM dish 
        WHERE restaurant_id = v_restaurant_id 
        ORDER BY dish_id DESC 
        LIMIT 12
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

-- 3.4. THÊM DỊCH VỤ (3 dịch vụ)
INSERT INTO restaurant_service (restaurant_id, name, category, description, price, status)
SELECT r.restaurant_id, 'Gọi món trước', 'Đặt món', 'Đặt món trước khi đến, giảm thời gian chờ đợi', 0, 'AVAILABLE'
FROM restaurant_profile r
WHERE (r.restaurant_name LIKE '%Hải Sản Bà Cường%' OR r.restaurant_name LIKE '%Hoàng Sa%')
  AND NOT EXISTS (SELECT 1 FROM restaurant_service WHERE restaurant_id = r.restaurant_id AND name = 'Gọi món trước');

INSERT INTO restaurant_service (restaurant_id, name, category, description, price, status)
SELECT r.restaurant_id, 'Giao hàng tận nơi', 'Giao hàng', 'Giao hàng trong bán kính 5km', 50000, 'AVAILABLE'
FROM restaurant_profile r
WHERE (r.restaurant_name LIKE '%Hải Sản Bà Cường%' OR r.restaurant_name LIKE '%Hoàng Sa%')
  AND NOT EXISTS (SELECT 1 FROM restaurant_service WHERE restaurant_id = r.restaurant_id AND name = 'Giao hàng tận nơi');

INSERT INTO restaurant_service (restaurant_id, name, category, description, price, status)
SELECT r.restaurant_id, 'Đặt bàn VIP', 'Đặt bàn', 'Đặt trước phòng VIP, có view đẹp', 50000, 'AVAILABLE'
FROM restaurant_profile r
WHERE (r.restaurant_name LIKE '%Hải Sản Bà Cường%' OR r.restaurant_name LIKE '%Hoàng Sa%')
  AND NOT EXISTS (SELECT 1 FROM restaurant_service WHERE restaurant_id = r.restaurant_id AND name = 'Đặt bàn VIP');

-- =====================================================
-- PHẦN 4: CẬP NHẬT GIÁ THÀNH 50.000 VNĐ
-- =====================================================

-- 4.1. Update giá món ăn → 50.000
UPDATE dish
SET price = 50000
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Hải Sản Bà Cường%' OR restaurant_name LIKE '%Hoàng Sa%' LIMIT 1);

-- 4.2. Update giá dịch vụ (có phí) → 50.000
UPDATE restaurant_service
SET price = 50000
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Hải Sản Bà Cường%' OR restaurant_name LIKE '%Hoàng Sa%' LIMIT 1)
  AND price > 0;

-- 4.3. Update deposit amount của bàn → 50.000
UPDATE restaurant_table
SET depositamount = 50000
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Hải Sản Bà Cường%' OR restaurant_name LIKE '%Hoàng Sa%' LIMIT 1);

-- 4.4. Update average_price của nhà hàng → 50.000
UPDATE restaurant_profile
SET average_price = 50000
WHERE restaurant_name LIKE '%Hải Sản Bà Cường%' OR restaurant_name LIKE '%Hoàng Sa%';

-- =====================================================
-- PHẦN 5: APPROVE NHÀ HÀNG VÀ FIX STATUS
-- =====================================================

-- 5.1. Approve nhà hàng
UPDATE restaurant_profile
SET approval_status = 'APPROVED',
    approved_at = NOW(),
    approved_by = (SELECT id FROM users WHERE username = 'admin' LIMIT 1)
WHERE (restaurant_name LIKE '%Hải Sản Bà Cường%' OR restaurant_name LIKE '%Hoàng Sa%')
  AND approval_status = 'PENDING';

-- 5.2. Đảm bảo tất cả món ăn có status = 'AVAILABLE'
UPDATE dish
SET status = 'AVAILABLE'
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Hải Sản Bà Cường%' OR restaurant_name LIKE '%Hoàng Sa%' LIMIT 1)
  AND status != 'AVAILABLE';

-- =====================================================
-- PHẦN 6: VERIFICATION - Kiểm tra dữ liệu đã thêm
-- =====================================================

SELECT 
    'BÀN' as loai,
    COUNT(*) as so_luong
FROM restaurant_table
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Hải Sản Bà Cường%' OR restaurant_name LIKE '%Hoàng Sa%' LIMIT 1)

UNION ALL

SELECT 
    'MÓN ĂN' as loai,
    COUNT(*) as so_luong
FROM dish
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Hải Sản Bà Cường%' OR restaurant_name LIKE '%Hoàng Sa%' LIMIT 1)

UNION ALL

SELECT 
    'DỊCH VỤ' as loai,
    COUNT(*) as so_luong
FROM restaurant_service
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Hải Sản Bà Cường%' OR restaurant_name LIKE '%Hoàng Sa%' LIMIT 1)

UNION ALL

SELECT 
    'ẢNH GALLERY' as loai,
    COUNT(*) as so_luong
FROM restaurant_media
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Hải Sản Bà Cường%' OR restaurant_name LIKE '%Hoàng Sa%' LIMIT 1)
  AND type = 'gallery';


