-- =====================================================
-- SQL Script: THÊM ĐẦY ĐỦ TẤT CẢ 7 NHÀ HÀNG
-- Owner: Taiphan
-- Restaurant IDs: 45-51 (sau Hải Sản Bà Cường = 44)
-- Bao gồm: Nhà hàng + Ảnh + Bàn + Món ăn + Dịch vụ + Giá + Approve
-- =====================================================

-- =====================================================
-- PHẦN 1: THÊM TẤT CẢ NHÀ HÀNG (45-51)
-- =====================================================

DO $$
DECLARE
    v_user_id UUID;
    v_owner_id UUID;
    v_restaurant_id INTEGER;
BEGIN
    -- Tìm user Taiphan
    SELECT id INTO v_user_id FROM users WHERE username = 'Taiphan';
    
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'User "Taiphan" not found! Please create the user first.';
    END IF;
    
    -- Tìm hoặc tạo RestaurantOwner
    SELECT owner_id INTO v_owner_id FROM restaurant_owner WHERE user_id = v_user_id;
    
    IF v_owner_id IS NULL THEN
        INSERT INTO restaurant_owner (owner_id, user_id, owner_name, created_at, updated_at)
        VALUES (gen_random_uuid(), v_user_id, COALESCE((SELECT full_name FROM users WHERE id = v_user_id), 'Taiphan'), NOW(), NOW())
        RETURNING owner_id INTO v_owner_id;
    END IF;
    
    -- 1. Hải Sản Ngọc Hương – Võ Nguyên Giáp (ID: 45)
    SELECT restaurant_id INTO v_restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Hải Sản Ngọc Hương%' OR restaurant_name LIKE '%Võ Nguyên Giáp%';
    IF v_restaurant_id IS NULL THEN
        INSERT INTO restaurant_profile (
            owner_id, restaurant_name, address, phone, description, cuisine_type,
            opening_hours, average_price, website_url, approval_status,
            contract_signed, terms_accepted, terms_accepted_at, terms_version,
            created_at, updated_at, hero_city, hero_headline, hero_subheadline,
            summary_highlights, signature_dishes, amenities, parking_details,
            booking_information, booking_notes
        ) VALUES (
            v_owner_id, 'Hải Sản Ngọc Hương – Võ Nguyên Giáp',
            '456 Võ Nguyên Giáp, Phường 2, Quận Tân Bình, TP. Hồ Chí Minh',
            '0909123458', 'Nhà hàng hải sản tươi sống với không gian sang trọng, chuyên về các món hải sản cao cấp. Hải sản được nhập mỗi ngày từ các vùng biển, đảm bảo tươi ngon nhất. Không gian phù hợp cho gia đình, nhóm bạn và các buổi tiệc sang trọng.',
            'Hải sản', '10:00 - 22:00', 300000.00, NULL, 'PENDING',
            FALSE, TRUE, NOW(), '1.0', NOW(), NOW(),
            'TP. Hồ Chí Minh', 'Hải sản tươi sống đặc sản', 'Tươi ngon mỗi ngày, hương vị đậm đà',
            'Hải sản tươi sống, Chế biến đa dạng, Không gian sang trọng, Phù hợp gia đình',
            'Cá mú hấp xì dầu, Tôm sú nướng muối ớt, Cua rang me, Nghêu hấp thái',
            'WiFi miễn phí, Điều hòa, Chỗ đậu xe, Khu vực VIP, Phục vụ tận tâm',
            'Có chỗ đậu xe máy và ô tô miễn phí. Bãi đậu xe rộng rãi.',
            'Đặt bàn trước 1 giờ để đảm bảo có chỗ. Nhà hàng nhận đặt bàn từ 10:00 - 21:30.',
            'Khuyến mãi đặc biệt cho nhóm từ 6 người: giảm 10% tổng hóa đơn.'
        ) RETURNING restaurant_id INTO v_restaurant_id;
        RAISE NOTICE '✅ Restaurant 1 added: Hải Sản Ngọc Hương (ID: %)', v_restaurant_id;
    END IF;
    
    -- 2. Nhà hàng Akataiyo Mặt Trời Đỏ - Nguyễn Du (ID: 46)
    SELECT restaurant_id INTO v_restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Akataiyo%' OR restaurant_name LIKE '%Mặt Trời Đỏ%';
    IF v_restaurant_id IS NULL THEN
        INSERT INTO restaurant_profile (
            owner_id, restaurant_name, address, phone, description, cuisine_type,
            opening_hours, average_price, website_url, approval_status,
            contract_signed, terms_accepted, terms_accepted_at, terms_version,
            created_at, updated_at, hero_city, hero_headline, hero_subheadline,
            summary_highlights, signature_dishes, amenities, parking_details,
            booking_information, booking_notes
        ) VALUES (
            v_owner_id, 'Nhà hàng Akataiyo Mặt Trời Đỏ - Nguyễn Du',
            '789 Nguyễn Du, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh',
            '0909123459', 'Nhà hàng Nhật Bản với không gian hiện đại, phục vụ các món ăn Nhật truyền thống và fusion. Menu đa dạng từ sushi, sashimi, ramen đến các món nướng. Không gian ấm cúng, phù hợp cho buổi tối với bạn bè và gia đình.',
            'Nhật Bản', '11:00 - 22:00', 400000.00, NULL, 'PENDING',
            FALSE, TRUE, NOW(), '1.0', NOW(), NOW(),
            'TP. Hồ Chí Minh', 'Ẩm thực Nhật Bản đích thực', 'Tươi ngon, chuẩn vị Nhật',
            'Sushi, Sashimi, Ramen, Món nướng, Không gian Nhật',
            'Sushi set, Sashimi tổng hợp, Ramen tonkotsu, Yakitori, Tempura',
            'WiFi miễn phí, Điều hòa, Chỗ đậu xe, Quầy bar, Nhạc nền',
            'Có chỗ đậu xe máy. Không có chỗ đậu ô tô riêng.',
            'Đặt bàn trước 30 phút. Nhà hàng nhận đặt bàn từ 11:00 - 21:30.',
            'Combo cho 2 người giảm 15%. Happy hour từ 17:00 - 19:00.'
        ) RETURNING restaurant_id INTO v_restaurant_id;
        RAISE NOTICE '✅ Restaurant 2 added: Akataiyo (ID: %)', v_restaurant_id;
    END IF;
    
    -- 3. Phố Biển – Đảo Xanh (ID: 47)
    SELECT restaurant_id INTO v_restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Phố Biển%' OR restaurant_name LIKE '%Đảo Xanh%';
    IF v_restaurant_id IS NULL THEN
        INSERT INTO restaurant_profile (
            owner_id, restaurant_name, address, phone, description, cuisine_type,
            opening_hours, average_price, website_url, approval_status,
            contract_signed, terms_accepted, terms_accepted_at, terms_version,
            created_at, updated_at, hero_city, hero_headline, hero_subheadline,
            summary_highlights, signature_dishes, amenities, parking_details,
            booking_information, booking_notes
        ) VALUES (
            v_owner_id, 'Phố Biển – Đảo Xanh',
            '321 Lê Văn Việt, Phường Hiệp Phú, Quận 9, TP. Hồ Chí Minh',
            '0909123460', 'Nhà hàng hải sản với không gian như một hòn đảo xanh, phục vụ các món hải sản tươi sống theo phong cách đặc trưng. Không gian rộng rãi, có sân ngoài trời, phù hợp cho các buổi tiệc lớn và sự kiện.',
            'Hải sản', '10:00 - 23:00', 280000.00, NULL, 'PENDING',
            FALSE, TRUE, NOW(), '1.0', NOW(), NOW(),
            'TP. Hồ Chí Minh', 'Hải sản tươi sống - Đảo xanh giữa lòng thành phố', 'Tươi ngon, không gian xanh',
            'Hải sản tươi sống, Không gian xanh, Sân ngoài trời, Phù hợp nhóm lớn',
            'Cá mú hấp, Tôm sú nướng, Cua rang me, Nghêu hấp, Mực chiên giòn',
            'WiFi miễn phí, Điều hòa, Chỗ đậu xe, Sân ngoài trời, Khu vực VIP',
            'Có chỗ đậu xe máy và ô tô miễn phí. Bãi đậu xe rộng rãi.',
            'Đặt bàn trước 1 giờ. Nhà hàng nhận đặt bàn từ 10:00 - 22:30.',
            'Combo hải sản cho 4-6 người giảm 12%. Nhóm từ 8 người giảm 15%.'
        ) RETURNING restaurant_id INTO v_restaurant_id;
        RAISE NOTICE '✅ Restaurant 3 added: Phố Biển (ID: %)', v_restaurant_id;
    END IF;
    
    -- 4. The Anchor (Restaurant & Bierhaus) - Trần Phú (ID: 48)
    SELECT restaurant_id INTO v_restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Anchor%' OR restaurant_name LIKE '%Bierhaus%';
    IF v_restaurant_id IS NULL THEN
        INSERT INTO restaurant_profile (
            owner_id, restaurant_name, address, phone, description, cuisine_type,
            opening_hours, average_price, website_url, approval_status,
            contract_signed, terms_accepted, terms_accepted_at, terms_version,
            created_at, updated_at, hero_city, hero_headline, hero_subheadline,
            summary_highlights, signature_dishes, amenities, parking_details,
            booking_information, booking_notes
        ) VALUES (
            v_owner_id, 'The Anchor (Restaurant & Bierhaus) - Trần Phú',
            '654 Trần Phú, Phường 4, Quận 5, TP. Hồ Chí Minh',
            '0909123461', 'Nhà hàng và quán bia với phong cách châu Âu, phục vụ các món ăn phương Tây và bia craft đa dạng. Không gian ấm cúng, có quầy bar, phù hợp cho các buổi tụ tập bạn bè và xem thể thao.',
            'Âu & Bia', '17:00 - 01:00', 350000.00, NULL, 'PENDING',
            FALSE, TRUE, NOW(), '1.0', NOW(), NOW(),
            'TP. Hồ Chí Minh', 'Nhà hàng & Bierhaus châu Âu', 'Món Âu ngon, bia craft đa dạng',
            'Món Âu, Bia craft, Quầy bar, Không gian ấm cúng, Xem thể thao',
            'Pizza, Burger, Steak, Pasta, Wings, Bia craft',
            'WiFi miễn phí, Điều hòa, Chỗ đậu xe, Quầy bar, TV màn hình lớn',
            'Có chỗ đậu xe máy. Không có chỗ đậu ô tô riêng.',
            'Đặt bàn trước 30 phút. Nhà hàng nhận đặt bàn từ 17:00 - 00:30.',
            'Happy hour từ 17:00 - 19:00: giảm 20% đồ uống. Combo cho 2 người giảm 10%.'
        ) RETURNING restaurant_id INTO v_restaurant_id;
        RAISE NOTICE '✅ Restaurant 4 added: The Anchor (ID: %)', v_restaurant_id;
    END IF;
    
    -- 5. Vietbamboo Restaurant - Phạm Văn Đồng (ID: 49)
    SELECT restaurant_id INTO v_restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Vietbamboo%' OR restaurant_name LIKE '%Phạm Văn Đồng%';
    IF v_restaurant_id IS NULL THEN
        INSERT INTO restaurant_profile (
            owner_id, restaurant_name, address, phone, description, cuisine_type,
            opening_hours, average_price, website_url, approval_status,
            contract_signed, terms_accepted, terms_accepted_at, terms_version,
            created_at, updated_at, hero_city, hero_headline, hero_subheadline,
            summary_highlights, signature_dishes, amenities, parking_details,
            booking_information, booking_notes
        ) VALUES (
            v_owner_id, 'Vietbamboo Restaurant - Phạm Văn Đồng',
            '987 Phạm Văn Đồng, Phường Linh Đông, Quận Thủ Đức, TP. Hồ Chí Minh',
            '0909123462', 'Nhà hàng Việt Nam với không gian truyền thống, phục vụ các món ăn Việt đặc trưng từ các vùng miền. Không gian ấm cúng, có khu vực ngoài trời, phù hợp cho gia đình và bạn bè.',
            'Món Việt', '10:00 - 22:00', 200000.00, NULL, 'PENDING',
            FALSE, TRUE, NOW(), '1.0', NOW(), NOW(),
            'TP. Hồ Chí Minh', 'Món Việt truyền thống', 'Đậm đà, chuẩn vị Việt',
            'Món Việt, Không gian truyền thống, Khu vực ngoài trời, Phù hợp gia đình',
            'Phở bò, Bún bò Huế, Cơm tấm, Bánh xèo, Gỏi cuốn, Chè',
            'WiFi miễn phí, Điều hòa, Chỗ đậu xe, Khu vực ngoài trời, Phục vụ nhanh',
            'Có chỗ đậu xe máy và ô tô miễn phí. Bãi đậu xe rộng rãi.',
            'Đặt bàn trước 30 phút. Nhà hàng nhận đặt bàn từ 10:00 - 21:30.',
            'Combo gia đình cho 4 người giảm 10%. Khuyến mãi đặc biệt vào cuối tuần.'
        ) RETURNING restaurant_id INTO v_restaurant_id;
        RAISE NOTICE '✅ Restaurant 5 added: Vietbamboo (ID: %)', v_restaurant_id;
    END IF;
    
    -- 6. Vườn Nướng - Đường 304 (ID: 50)
    SELECT restaurant_id INTO v_restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Vườn Nướng%' OR restaurant_name LIKE '%Đường 304%';
    IF v_restaurant_id IS NULL THEN
        INSERT INTO restaurant_profile (
            owner_id, restaurant_name, address, phone, description, cuisine_type,
            opening_hours, average_price, website_url, approval_status,
            contract_signed, terms_accepted, terms_accepted_at, terms_version,
            created_at, updated_at, hero_city, hero_headline, hero_subheadline,
            summary_highlights, signature_dishes, amenities, parking_details,
            booking_information, booking_notes
        ) VALUES (
            v_owner_id, 'Vườn Nướng - Đường 304',
            '159 Đường 30/4, Phường 9, Quận 4, TP. Hồ Chí Minh',
            '0909123463', 'Nhà hàng BBQ với không gian như một khu vườn, phục vụ các món nướng đa dạng. Không gian rộng rãi, có sân ngoài trời, phù hợp cho các buổi tiệc nhóm lớn và sự kiện.',
            'BBQ', '17:00 - 23:00', 300000.00, NULL, 'PENDING',
            FALSE, TRUE, NOW(), '1.0', NOW(), NOW(),
            'TP. Hồ Chí Minh', 'BBQ vườn nướng', 'Nướng tại bàn, không gian vườn',
            'BBQ, Nướng tại bàn, Không gian vườn, Sân ngoài trời, Phù hợp nhóm lớn',
            'Thịt nướng, Gà nướng, Hải sản nướng, Rau nướng, Combo nướng',
            'WiFi miễn phí, Điều hòa, Chỗ đậu xe, Sân ngoài trời, Khu vực nướng',
            'Có chỗ đậu xe máy và ô tô miễn phí. Bãi đậu xe rộng rãi.',
            'Đặt bàn trước 1 giờ. Nhà hàng nhận đặt bàn từ 17:00 - 22:30.',
            'Combo nướng cho 4-6 người giảm 12%. Nhóm từ 8 người giảm 15%.'
        ) RETURNING restaurant_id INTO v_restaurant_id;
        RAISE NOTICE '✅ Restaurant 6 added: Vườn Nướng (ID: %)', v_restaurant_id;
    END IF;
    
    -- 7. Zzuggubbong - Nguyễn Hữu Thông (ID: 51)
    SELECT restaurant_id INTO v_restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Zzuggubbong%' OR restaurant_name LIKE '%Nguyễn Hữu Thông%';
    IF v_restaurant_id IS NULL THEN
        INSERT INTO restaurant_profile (
            owner_id, restaurant_name, address, phone, description, cuisine_type,
            opening_hours, average_price, website_url, approval_status,
            contract_signed, terms_accepted, terms_accepted_at, terms_version,
            created_at, updated_at, hero_city, hero_headline, hero_subheadline,
            summary_highlights, signature_dishes, amenities, parking_details,
            booking_information, booking_notes
        ) VALUES (
            v_owner_id, 'Zzuggubbong - Nguyễn Hữu Thông',
            '753 Nguyễn Hữu Thông, Phường 7, Quận Gò Vấp, TP. Hồ Chí Minh',
            '0909123464', 'Nhà hàng Hàn Quốc với không gian hiện đại, phục vụ các món ăn Hàn đặc trưng. Menu đa dạng từ BBQ Hàn Quốc, lẩu, đến các món ăn vặt Hàn. Không gian ấm cúng, phù hợp cho gia đình và bạn bè.',
            'Hàn Quốc', '11:00 - 22:00', 320000.00, NULL, 'PENDING',
            FALSE, TRUE, NOW(), '1.0', NOW(), NOW(),
            'TP. Hồ Chí Minh', 'Ẩm thực Hàn Quốc đích thực', 'BBQ Hàn, lẩu, món ăn vặt',
            'BBQ Hàn Quốc, Lẩu, Món ăn vặt Hàn, Không gian Hàn, Phù hợp gia đình',
            'BBQ thịt ba chỉ, Lẩu kimchi, Gimbap, Tteokbokki, Canh sườn',
            'WiFi miễn phí, Điều hòa, Chỗ đậu xe, Khu vực nướng, Nhạc Hàn',
            'Có chỗ đậu xe máy. Không có chỗ đậu ô tô riêng.',
            'Đặt bàn trước 30 phút. Nhà hàng nhận đặt bàn từ 11:00 - 21:30.',
            'Combo BBQ cho 2 người giảm 10%. Happy hour từ 14:00 - 17:00.'
        ) RETURNING restaurant_id INTO v_restaurant_id;
        RAISE NOTICE '✅ Restaurant 7 added: Zzuggubbong (ID: %)', v_restaurant_id;
    END IF;
    
    RAISE NOTICE '========================================';
    RAISE NOTICE '✅ TẤT CẢ NHÀ HÀNG ĐÃ ĐƯỢC THÊM!';
    RAISE NOTICE '========================================';
    RAISE NOTICE '📋 BƯỚC TIẾP THEO:';
    RAISE NOTICE '1. Chạy Python script: python scripts/upload_all_restaurants_images.py 45';
    RAISE NOTICE '2. Sau khi upload xong, chạy file insert_all_restaurants_images.sql';
    RAISE NOTICE '3. Tiếp tục với PHẦN 3, 4, 5, 6';
    RAISE NOTICE '========================================';
    
END $$;

-- =====================================================
-- PHẦN 2: THÊM ẢNH (CHẠY SAU KHI UPLOAD ẢNH LÊN CLOUDINARY)
-- =====================================================
-- 
-- HƯỚNG DẪN:
-- 1. Chạy: python scripts/upload_all_restaurants_images.py 45
-- 2. Sau khi upload xong, mở file: scripts/insert_all_restaurants_images.sql
-- 3. Copy TOÀN BỘ nội dung và paste vào đây (thay thế comment này)
-- 4. Hoặc chạy trực tiếp file insert_all_restaurants_images.sql trước
-- 
-- =====================================================

-- =====================================================
-- PHẦN 3: THÊM BÀN, MÓN ĂN VÀ DỊCH VỤ CHO TẤT CẢ NHÀ HÀNG
-- =====================================================

-- 3.1. THÊM BÀN (10 bàn cho mỗi nhà hàng)
-- Hải Sản Ngọc Hương (9 gallery = 9 món)
INSERT INTO restaurant_table (restaurant_id, table_name, capacity, status, depositamount)
SELECT r.restaurant_id, 'Bàn 1', 2, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Hải Sản Ngọc Hương%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 1')
UNION ALL SELECT r.restaurant_id, 'Bàn 2', 4, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Hải Sản Ngọc Hương%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 2')
UNION ALL SELECT r.restaurant_id, 'Bàn 3', 4, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Hải Sản Ngọc Hương%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 3')
UNION ALL SELECT r.restaurant_id, 'Bàn 4', 6, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Hải Sản Ngọc Hương%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 4')
UNION ALL SELECT r.restaurant_id, 'Bàn 5', 6, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Hải Sản Ngọc Hương%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 5')
UNION ALL SELECT r.restaurant_id, 'Bàn 6', 8, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Hải Sản Ngọc Hương%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 6')
UNION ALL SELECT r.restaurant_id, 'Bàn 7', 10, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Hải Sản Ngọc Hương%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 7')
UNION ALL SELECT r.restaurant_id, 'Phòng VIP 1', 12, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Hải Sản Ngọc Hương%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Phòng VIP 1')
UNION ALL SELECT r.restaurant_id, 'Phòng VIP 2', 15, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Hải Sản Ngọc Hương%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Phòng VIP 2')
UNION ALL SELECT r.restaurant_id, 'Sân ngoài trời', 20, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Hải Sản Ngọc Hương%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Sân ngoài trời');

-- Akataiyo (6 gallery = 6 món)
INSERT INTO restaurant_table (restaurant_id, table_name, capacity, status, depositamount)
SELECT r.restaurant_id, 'Bàn 1', 2, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Akataiyo%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 1')
UNION ALL SELECT r.restaurant_id, 'Bàn 2', 4, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Akataiyo%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 2')
UNION ALL SELECT r.restaurant_id, 'Bàn 3', 4, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Akataiyo%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 3')
UNION ALL SELECT r.restaurant_id, 'Bàn 4', 6, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Akataiyo%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 4')
UNION ALL SELECT r.restaurant_id, 'Bàn 5', 6, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Akataiyo%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 5')
UNION ALL SELECT r.restaurant_id, 'Bàn 6', 8, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Akataiyo%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 6')
UNION ALL SELECT r.restaurant_id, 'Bàn 7', 10, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Akataiyo%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 7')
UNION ALL SELECT r.restaurant_id, 'Phòng VIP 1', 12, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Akataiyo%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Phòng VIP 1')
UNION ALL SELECT r.restaurant_id, 'Phòng VIP 2', 15, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Akataiyo%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Phòng VIP 2')
UNION ALL SELECT r.restaurant_id, 'Sân ngoài trời', 20, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Akataiyo%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Sân ngoài trời');

-- Phố Biển (9 gallery = 9 món)
INSERT INTO restaurant_table (restaurant_id, table_name, capacity, status, depositamount)
SELECT r.restaurant_id, 'Bàn 1', 2, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Phố Biển%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 1')
UNION ALL SELECT r.restaurant_id, 'Bàn 2', 4, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Phố Biển%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 2')
UNION ALL SELECT r.restaurant_id, 'Bàn 3', 4, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Phố Biển%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 3')
UNION ALL SELECT r.restaurant_id, 'Bàn 4', 6, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Phố Biển%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 4')
UNION ALL SELECT r.restaurant_id, 'Bàn 5', 6, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Phố Biển%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 5')
UNION ALL SELECT r.restaurant_id, 'Bàn 6', 8, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Phố Biển%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 6')
UNION ALL SELECT r.restaurant_id, 'Bàn 7', 10, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Phố Biển%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 7')
UNION ALL SELECT r.restaurant_id, 'Phòng VIP 1', 12, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Phố Biển%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Phòng VIP 1')
UNION ALL SELECT r.restaurant_id, 'Phòng VIP 2', 15, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Phố Biển%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Phòng VIP 2')
UNION ALL SELECT r.restaurant_id, 'Sân ngoài trời', 20, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Phố Biển%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Sân ngoài trời');

-- The Anchor (11 gallery = 11 món)
INSERT INTO restaurant_table (restaurant_id, table_name, capacity, status, depositamount)
SELECT r.restaurant_id, 'Bàn 1', 2, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Anchor%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 1')
UNION ALL SELECT r.restaurant_id, 'Bàn 2', 4, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Anchor%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 2')
UNION ALL SELECT r.restaurant_id, 'Bàn 3', 4, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Anchor%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 3')
UNION ALL SELECT r.restaurant_id, 'Bàn 4', 6, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Anchor%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 4')
UNION ALL SELECT r.restaurant_id, 'Bàn 5', 6, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Anchor%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 5')
UNION ALL SELECT r.restaurant_id, 'Bàn 6', 8, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Anchor%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 6')
UNION ALL SELECT r.restaurant_id, 'Bàn 7', 10, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Anchor%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 7')
UNION ALL SELECT r.restaurant_id, 'Phòng VIP 1', 12, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Anchor%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Phòng VIP 1')
UNION ALL SELECT r.restaurant_id, 'Phòng VIP 2', 15, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Anchor%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Phòng VIP 2')
UNION ALL SELECT r.restaurant_id, 'Sân ngoài trời', 20, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Anchor%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Sân ngoài trời');

-- Vietbamboo (8 gallery = 8 món)
INSERT INTO restaurant_table (restaurant_id, table_name, capacity, status, depositamount)
SELECT r.restaurant_id, 'Bàn 1', 2, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vietbamboo%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 1')
UNION ALL SELECT r.restaurant_id, 'Bàn 2', 4, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vietbamboo%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 2')
UNION ALL SELECT r.restaurant_id, 'Bàn 3', 4, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vietbamboo%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 3')
UNION ALL SELECT r.restaurant_id, 'Bàn 4', 6, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vietbamboo%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 4')
UNION ALL SELECT r.restaurant_id, 'Bàn 5', 6, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vietbamboo%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 5')
UNION ALL SELECT r.restaurant_id, 'Bàn 6', 8, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vietbamboo%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 6')
UNION ALL SELECT r.restaurant_id, 'Bàn 7', 10, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vietbamboo%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 7')
UNION ALL SELECT r.restaurant_id, 'Phòng VIP 1', 12, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vietbamboo%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Phòng VIP 1')
UNION ALL SELECT r.restaurant_id, 'Phòng VIP 2', 15, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vietbamboo%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Phòng VIP 2')
UNION ALL SELECT r.restaurant_id, 'Sân ngoài trời', 20, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vietbamboo%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Sân ngoài trời');

-- Vườn Nướng (10 gallery = 10 món)
INSERT INTO restaurant_table (restaurant_id, table_name, capacity, status, depositamount)
SELECT r.restaurant_id, 'Bàn 1', 2, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vườn Nướng%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 1')
UNION ALL SELECT r.restaurant_id, 'Bàn 2', 4, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vườn Nướng%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 2')
UNION ALL SELECT r.restaurant_id, 'Bàn 3', 4, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vườn Nướng%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 3')
UNION ALL SELECT r.restaurant_id, 'Bàn 4', 6, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vườn Nướng%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 4')
UNION ALL SELECT r.restaurant_id, 'Bàn 5', 6, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vườn Nướng%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 5')
UNION ALL SELECT r.restaurant_id, 'Bàn 6', 8, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vườn Nướng%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 6')
UNION ALL SELECT r.restaurant_id, 'Bàn 7', 10, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vườn Nướng%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 7')
UNION ALL SELECT r.restaurant_id, 'Phòng VIP 1', 12, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vườn Nướng%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Phòng VIP 1')
UNION ALL SELECT r.restaurant_id, 'Phòng VIP 2', 15, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vườn Nướng%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Phòng VIP 2')
UNION ALL SELECT r.restaurant_id, 'Sân ngoài trời', 20, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vườn Nướng%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Sân ngoài trời');

-- Zzuggubbong (11 gallery = 11 món)
INSERT INTO restaurant_table (restaurant_id, table_name, capacity, status, depositamount)
SELECT r.restaurant_id, 'Bàn 1', 2, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Zzuggubbong%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 1')
UNION ALL SELECT r.restaurant_id, 'Bàn 2', 4, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Zzuggubbong%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 2')
UNION ALL SELECT r.restaurant_id, 'Bàn 3', 4, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Zzuggubbong%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 3')
UNION ALL SELECT r.restaurant_id, 'Bàn 4', 6, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Zzuggubbong%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 4')
UNION ALL SELECT r.restaurant_id, 'Bàn 5', 6, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Zzuggubbong%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 5')
UNION ALL SELECT r.restaurant_id, 'Bàn 6', 8, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Zzuggubbong%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 6')
UNION ALL SELECT r.restaurant_id, 'Bàn 7', 10, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Zzuggubbong%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Bàn 7')
UNION ALL SELECT r.restaurant_id, 'Phòng VIP 1', 12, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Zzuggubbong%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Phòng VIP 1')
UNION ALL SELECT r.restaurant_id, 'Phòng VIP 2', 15, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Zzuggubbong%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Phòng VIP 2')
UNION ALL SELECT r.restaurant_id, 'Sân ngoài trời', 20, 'available', 50000 FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Zzuggubbong%' AND NOT EXISTS (SELECT 1 FROM restaurant_table WHERE restaurant_id = r.restaurant_id AND table_name = 'Sân ngoài trời');

-- 3.2. THÊM MÓN ĂN (số lượng = số ảnh gallery)
-- Hải Sản Ngọc Hương (9 món)
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Cá mú hấp xì dầu', 'Cá mú tươi hấp với xì dầu, gừng, hành', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Hải Sản Ngọc Hương%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Cá mú hấp xì dầu');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Tôm sú nướng muối ớt', 'Tôm sú tươi nướng với muối ớt', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Hải Sản Ngọc Hương%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Tôm sú nướng muối ớt');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Cua rang me', 'Cua tươi rang me chua ngọt', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Hải Sản Ngọc Hương%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Cua rang me');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Nghêu hấp thái', 'Nghêu tươi hấp với nước dừa, sả, ớt thái', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Hải Sản Ngọc Hương%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Nghêu hấp thái');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Cá điêu hồng chiên giòn', 'Cá điêu hồng tươi chiên giòn', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Hải Sản Ngọc Hương%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Cá điêu hồng chiên giòn');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Mực nướng sa tế', 'Mực tươi nướng với sa tế', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Hải Sản Ngọc Hương%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Mực nướng sa tế');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Sò điệp nướng phô mai', 'Sò điệp tươi nướng với phô mai, bơ tỏi', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Hải Sản Ngọc Hương%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Sò điệp nướng phô mai');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Cá hồng kho tộ', 'Cá hồng tươi kho tộ với nước mắm, ớt, tiêu', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Hải Sản Ngọc Hương%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Cá hồng kho tộ');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Tôm càng nướng muối', 'Tôm càng tươi nướng muối', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Hải Sản Ngọc Hương%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Tôm càng nướng muối');

-- Akataiyo (6 món)
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Sushi set tổng hợp', 'Sushi set đa dạng với cá tươi', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Akataiyo%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Sushi set tổng hợp');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Sashimi tổng hợp', 'Sashimi cá tươi sống, đa dạng', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Akataiyo%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Sashimi tổng hợp');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Ramen tonkotsu', 'Ramen nước dùng tonkotsu đậm đà', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Akataiyo%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Ramen tonkotsu');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Yakitori set', 'Yakitori thịt gà nướng than hoa', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Akataiyo%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Yakitori set');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Tempura tổng hợp', 'Tempura tôm, rau củ chiên giòn', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Akataiyo%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Tempura tổng hợp');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Miso soup', 'Miso soup truyền thống Nhật', 50000, 'Món phụ', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Akataiyo%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Miso soup');

-- Phố Biển (9 món)
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Cá mú hấp', 'Cá mú tươi hấp với xì dầu, gừng', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Phố Biển%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Cá mú hấp');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Tôm sú nướng', 'Tôm sú tươi nướng than hoa', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Phố Biển%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Tôm sú nướng');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Cua rang me', 'Cua tươi rang me chua ngọt', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Phố Biển%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Cua rang me');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Nghêu hấp', 'Nghêu tươi hấp với nước dừa, sả', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Phố Biển%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Nghêu hấp');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Mực chiên giòn', 'Mực tươi chiên giòn, ăn kèm nước mắm gừng', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Phố Biển%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Mực chiên giòn');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Cá điêu hồng nướng', 'Cá điêu hồng tươi nướng muối ớt', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Phố Biển%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Cá điêu hồng nướng');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Sò huyết nướng', 'Sò huyết tươi nướng bơ tỏi', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Phố Biển%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Sò huyết nướng');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Cá bớp nướng', 'Cá bớp tươi nướng muối ớt', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Phố Biển%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Cá bớp nướng');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Hải sản nướng tổng hợp', 'Hải sản đa dạng nướng than hoa', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Phố Biển%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Hải sản nướng tổng hợp');

-- The Anchor (11 món)
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Pizza Margherita', 'Pizza truyền thống Ý với phô mai mozzarella', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Anchor%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Pizza Margherita');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Burger Classic', 'Burger thịt bò với rau, phô mai, sốt đặc biệt', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Anchor%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Burger Classic');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Steak bò', 'Steak bò Úc, nướng vừa phải', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Anchor%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Steak bò');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Pasta Carbonara', 'Pasta sốt carbonara với thịt xông khói', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Anchor%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Pasta Carbonara');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Chicken Wings', 'Cánh gà nướng với sốt BBQ', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Anchor%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Chicken Wings');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Fish & Chips', 'Cá chiên giòn với khoai tây chiên', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Anchor%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Fish & Chips');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Caesar Salad', 'Salad Caesar với rau xanh, phô mai, sốt', 50000, 'Món phụ', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Anchor%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Caesar Salad');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Onion Rings', 'Hành tây chiên giòn', 50000, 'Món phụ', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Anchor%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Onion Rings');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Nachos', 'Nachos với phô mai, ớt, sốt', 50000, 'Món phụ', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Anchor%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Nachos');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Garlic Bread', 'Bánh mì tỏi nướng', 50000, 'Món phụ', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Anchor%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Garlic Bread');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Mozzarella Sticks', 'Que phô mai mozzarella chiên giòn', 50000, 'Món phụ', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Anchor%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Mozzarella Sticks');

-- Vietbamboo (8 món)
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Phở bò', 'Phở bò truyền thống, nước dùng đậm đà', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vietbamboo%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Phở bò');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Bún bò Huế', 'Bún bò Huế cay nồng, đậm đà', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vietbamboo%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Bún bò Huế');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Cơm tấm sườn', 'Cơm tấm với sườn nướng, bì, chả', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vietbamboo%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Cơm tấm sườn');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Bánh xèo', 'Bánh xèo giòn với tôm, thịt, giá', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vietbamboo%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Bánh xèo');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Gỏi cuốn', 'Gỏi cuốn tôm thịt, rau sống', 50000, 'Khai vị', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vietbamboo%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Gỏi cuốn');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Chả giò', 'Chả giò giòn với thịt, tôm, rau củ', 50000, 'Khai vị', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vietbamboo%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Chả giò');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Chè đậu xanh', 'Chè đậu xanh ngọt mát', 50000, 'Tráng miệng', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vietbamboo%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Chè đậu xanh');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Chè ba màu', 'Chè ba màu truyền thống', 50000, 'Tráng miệng', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vietbamboo%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Chè ba màu');

-- Vườn Nướng (10 món)
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Thịt nướng', 'Thịt ba chỉ nướng than hoa', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vườn Nướng%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Thịt nướng');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Gà nướng', 'Gà ta nướng than hoa, da giòn', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vườn Nướng%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Gà nướng');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Hải sản nướng', 'Hải sản đa dạng nướng than hoa', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vườn Nướng%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Hải sản nướng');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Rau nướng', 'Rau củ tươi nướng than hoa', 50000, 'Món phụ', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vườn Nướng%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Rau nướng');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Combo nướng 4 người', 'Combo đa dạng cho 4 người', 50000, 'Combo', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vườn Nướng%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Combo nướng 4 người');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Bò nướng', 'Bò tảng nướng than hoa', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vườn Nướng%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Bò nướng');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Tôm nướng', 'Tôm tươi nướng than hoa', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vườn Nướng%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Tôm nướng');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Mực nướng', 'Mực tươi nướng than hoa', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vườn Nướng%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Mực nướng');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Khoai tây nướng', 'Khoai tây nướng bơ tỏi', 50000, 'Món phụ', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vườn Nướng%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Khoai tây nướng');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Ngô nướng', 'Ngô nướng bơ', 50000, 'Món phụ', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vườn Nướng%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Ngô nướng');

-- Zzuggubbong (11 món)
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'BBQ thịt ba chỉ', 'Thịt ba chỉ nướng BBQ Hàn Quốc', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Zzuggubbong%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'BBQ thịt ba chỉ');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Lẩu kimchi', 'Lẩu kimchi Hàn Quốc cay nồng', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Zzuggubbong%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Lẩu kimchi');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Gimbap', 'Gimbap cuốn rong biển với thịt, rau', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Zzuggubbong%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Gimbap');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Tteokbokki', 'Bánh gạo sốt cay Hàn Quốc', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Zzuggubbong%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Tteokbokki');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Canh sườn', 'Canh sườn nấu kimchi, đậu phụ', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Zzuggubbong%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Canh sườn');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Bulgogi', 'Thịt bò nướng sốt Hàn Quốc', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Zzuggubbong%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Bulgogi');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Kimchi', 'Kimchi lên men Hàn Quốc', 50000, 'Món phụ', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Zzuggubbong%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Kimchi');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Bánh xèo Hàn', 'Bánh xèo Hàn Quốc với thịt, rau', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Zzuggubbong%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Bánh xèo Hàn');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Japchae', 'Miến trộn Hàn Quốc với thịt, rau', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Zzuggubbong%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Japchae');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Kimbap chiên', 'Kimbap chiên giòn', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Zzuggubbong%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Kimbap chiên');
INSERT INTO dish (restaurant_id, name, description, price, category, status) SELECT r.restaurant_id, 'Canh rong biển', 'Canh rong biển Hàn Quốc', 50000, 'Món chính', 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Zzuggubbong%' AND NOT EXISTS (SELECT 1 FROM dish WHERE restaurant_id = r.restaurant_id AND name = 'Canh rong biển');

-- 3.3. THÊM ẢNH CHO MÓN ĂN (dùng gallery images đã có)
DO $$
DECLARE
    v_restaurant_id INTEGER;
    v_dish_id INTEGER;
    v_counter INTEGER;
    v_gallery_urls TEXT[];
    v_url TEXT;
BEGIN
    -- Hải Sản Ngọc Hương
    SELECT restaurant_id INTO v_restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Hải Sản Ngọc Hương%' LIMIT 1;
    IF v_restaurant_id IS NOT NULL THEN
        SELECT ARRAY_AGG(url ORDER BY created_at) INTO v_gallery_urls FROM restaurant_media WHERE restaurant_id = v_restaurant_id AND type = 'gallery';
        IF v_gallery_urls IS NOT NULL THEN
            v_counter := 0;
            FOR v_dish_id IN SELECT dish_id FROM dish WHERE restaurant_id = v_restaurant_id ORDER BY dish_id LIMIT 9
            LOOP
                v_url := v_gallery_urls[(v_counter % array_length(v_gallery_urls, 1)) + 1];
                IF v_url IS NOT NULL AND NOT EXISTS (SELECT 1 FROM restaurant_media WHERE restaurant_id = v_restaurant_id AND type = 'dish' AND url = v_url) THEN
                    INSERT INTO restaurant_media (restaurant_id, type, url, created_at) VALUES (v_restaurant_id, 'dish', v_url, NOW());
                END IF;
                v_counter := v_counter + 1;
            END LOOP;
        END IF;
    END IF;
    
    -- Akataiyo
    SELECT restaurant_id INTO v_restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Akataiyo%' LIMIT 1;
    IF v_restaurant_id IS NOT NULL THEN
        SELECT ARRAY_AGG(url ORDER BY created_at) INTO v_gallery_urls FROM restaurant_media WHERE restaurant_id = v_restaurant_id AND type = 'gallery';
        IF v_gallery_urls IS NOT NULL THEN
            v_counter := 0;
            FOR v_dish_id IN SELECT dish_id FROM dish WHERE restaurant_id = v_restaurant_id ORDER BY dish_id LIMIT 6
            LOOP
                v_url := v_gallery_urls[(v_counter % array_length(v_gallery_urls, 1)) + 1];
                IF v_url IS NOT NULL AND NOT EXISTS (SELECT 1 FROM restaurant_media WHERE restaurant_id = v_restaurant_id AND type = 'dish' AND url = v_url) THEN
                    INSERT INTO restaurant_media (restaurant_id, type, url, created_at) VALUES (v_restaurant_id, 'dish', v_url, NOW());
                END IF;
                v_counter := v_counter + 1;
            END LOOP;
        END IF;
    END IF;
    
    -- Phố Biển
    SELECT restaurant_id INTO v_restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Phố Biển%' LIMIT 1;
    IF v_restaurant_id IS NOT NULL THEN
        SELECT ARRAY_AGG(url ORDER BY created_at) INTO v_gallery_urls FROM restaurant_media WHERE restaurant_id = v_restaurant_id AND type = 'gallery';
        IF v_gallery_urls IS NOT NULL THEN
            v_counter := 0;
            FOR v_dish_id IN SELECT dish_id FROM dish WHERE restaurant_id = v_restaurant_id ORDER BY dish_id LIMIT 9
            LOOP
                v_url := v_gallery_urls[(v_counter % array_length(v_gallery_urls, 1)) + 1];
                IF v_url IS NOT NULL AND NOT EXISTS (SELECT 1 FROM restaurant_media WHERE restaurant_id = v_restaurant_id AND type = 'dish' AND url = v_url) THEN
                    INSERT INTO restaurant_media (restaurant_id, type, url, created_at) VALUES (v_restaurant_id, 'dish', v_url, NOW());
                END IF;
                v_counter := v_counter + 1;
            END LOOP;
        END IF;
    END IF;
    
    -- The Anchor
    SELECT restaurant_id INTO v_restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Anchor%' LIMIT 1;
    IF v_restaurant_id IS NOT NULL THEN
        SELECT ARRAY_AGG(url ORDER BY created_at) INTO v_gallery_urls FROM restaurant_media WHERE restaurant_id = v_restaurant_id AND type = 'gallery';
        IF v_gallery_urls IS NOT NULL THEN
            v_counter := 0;
            FOR v_dish_id IN SELECT dish_id FROM dish WHERE restaurant_id = v_restaurant_id ORDER BY dish_id LIMIT 11
            LOOP
                v_url := v_gallery_urls[(v_counter % array_length(v_gallery_urls, 1)) + 1];
                IF v_url IS NOT NULL AND NOT EXISTS (SELECT 1 FROM restaurant_media WHERE restaurant_id = v_restaurant_id AND type = 'dish' AND url = v_url) THEN
                    INSERT INTO restaurant_media (restaurant_id, type, url, created_at) VALUES (v_restaurant_id, 'dish', v_url, NOW());
                END IF;
                v_counter := v_counter + 1;
            END LOOP;
        END IF;
    END IF;
    
    -- Vietbamboo
    SELECT restaurant_id INTO v_restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Vietbamboo%' LIMIT 1;
    IF v_restaurant_id IS NOT NULL THEN
        SELECT ARRAY_AGG(url ORDER BY created_at) INTO v_gallery_urls FROM restaurant_media WHERE restaurant_id = v_restaurant_id AND type = 'gallery';
        IF v_gallery_urls IS NOT NULL THEN
            v_counter := 0;
            FOR v_dish_id IN SELECT dish_id FROM dish WHERE restaurant_id = v_restaurant_id ORDER BY dish_id LIMIT 8
            LOOP
                v_url := v_gallery_urls[(v_counter % array_length(v_gallery_urls, 1)) + 1];
                IF v_url IS NOT NULL AND NOT EXISTS (SELECT 1 FROM restaurant_media WHERE restaurant_id = v_restaurant_id AND type = 'dish' AND url = v_url) THEN
                    INSERT INTO restaurant_media (restaurant_id, type, url, created_at) VALUES (v_restaurant_id, 'dish', v_url, NOW());
                END IF;
                v_counter := v_counter + 1;
            END LOOP;
        END IF;
    END IF;
    
    -- Vườn Nướng
    SELECT restaurant_id INTO v_restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Vườn Nướng%' LIMIT 1;
    IF v_restaurant_id IS NOT NULL THEN
        SELECT ARRAY_AGG(url ORDER BY created_at) INTO v_gallery_urls FROM restaurant_media WHERE restaurant_id = v_restaurant_id AND type = 'gallery';
        IF v_gallery_urls IS NOT NULL THEN
            v_counter := 0;
            FOR v_dish_id IN SELECT dish_id FROM dish WHERE restaurant_id = v_restaurant_id ORDER BY dish_id LIMIT 10
            LOOP
                v_url := v_gallery_urls[(v_counter % array_length(v_gallery_urls, 1)) + 1];
                IF v_url IS NOT NULL AND NOT EXISTS (SELECT 1 FROM restaurant_media WHERE restaurant_id = v_restaurant_id AND type = 'dish' AND url = v_url) THEN
                    INSERT INTO restaurant_media (restaurant_id, type, url, created_at) VALUES (v_restaurant_id, 'dish', v_url, NOW());
                END IF;
                v_counter := v_counter + 1;
            END LOOP;
        END IF;
    END IF;
    
    -- Zzuggubbong
    SELECT restaurant_id INTO v_restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Zzuggubbong%' LIMIT 1;
    IF v_restaurant_id IS NOT NULL THEN
        SELECT ARRAY_AGG(url ORDER BY created_at) INTO v_gallery_urls FROM restaurant_media WHERE restaurant_id = v_restaurant_id AND type = 'gallery';
        IF v_gallery_urls IS NOT NULL THEN
            v_counter := 0;
            FOR v_dish_id IN SELECT dish_id FROM dish WHERE restaurant_id = v_restaurant_id ORDER BY dish_id LIMIT 11
            LOOP
                v_url := v_gallery_urls[(v_counter % array_length(v_gallery_urls, 1)) + 1];
                IF v_url IS NOT NULL AND NOT EXISTS (SELECT 1 FROM restaurant_media WHERE restaurant_id = v_restaurant_id AND type = 'dish' AND url = v_url) THEN
                    INSERT INTO restaurant_media (restaurant_id, type, url, created_at) VALUES (v_restaurant_id, 'dish', v_url, NOW());
                END IF;
                v_counter := v_counter + 1;
            END LOOP;
        END IF;
    END IF;
END $$;

-- 3.4. THÊM DỊCH VỤ (3 dịch vụ cho mỗi nhà hàng)
INSERT INTO restaurant_service (restaurant_id, name, category, description, price, status) SELECT r.restaurant_id, 'Gọi món trước', 'Đặt món', 'Đặt món trước khi đến', 0, 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Hải Sản Ngọc Hương%' AND NOT EXISTS (SELECT 1 FROM restaurant_service WHERE restaurant_id = r.restaurant_id AND name = 'Gọi món trước');
INSERT INTO restaurant_service (restaurant_id, name, category, description, price, status) SELECT r.restaurant_id, 'Giao hàng tận nơi', 'Giao hàng', 'Giao hàng trong bán kính 5km', 50000, 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Hải Sản Ngọc Hương%' AND NOT EXISTS (SELECT 1 FROM restaurant_service WHERE restaurant_id = r.restaurant_id AND name = 'Giao hàng tận nơi');
INSERT INTO restaurant_service (restaurant_id, name, category, description, price, status) SELECT r.restaurant_id, 'Đặt bàn VIP', 'Đặt bàn', 'Đặt trước phòng VIP', 50000, 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Hải Sản Ngọc Hương%' AND NOT EXISTS (SELECT 1 FROM restaurant_service WHERE restaurant_id = r.restaurant_id AND name = 'Đặt bàn VIP');

INSERT INTO restaurant_service (restaurant_id, name, category, description, price, status) SELECT r.restaurant_id, 'Gọi món trước', 'Đặt món', 'Đặt món trước khi đến', 0, 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Akataiyo%' AND NOT EXISTS (SELECT 1 FROM restaurant_service WHERE restaurant_id = r.restaurant_id AND name = 'Gọi món trước');
INSERT INTO restaurant_service (restaurant_id, name, category, description, price, status) SELECT r.restaurant_id, 'Giao hàng tận nơi', 'Giao hàng', 'Giao hàng trong bán kính 5km', 50000, 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Akataiyo%' AND NOT EXISTS (SELECT 1 FROM restaurant_service WHERE restaurant_id = r.restaurant_id AND name = 'Giao hàng tận nơi');
INSERT INTO restaurant_service (restaurant_id, name, category, description, price, status) SELECT r.restaurant_id, 'Đặt bàn VIP', 'Đặt bàn', 'Đặt trước phòng VIP', 50000, 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Akataiyo%' AND NOT EXISTS (SELECT 1 FROM restaurant_service WHERE restaurant_id = r.restaurant_id AND name = 'Đặt bàn VIP');

INSERT INTO restaurant_service (restaurant_id, name, category, description, price, status) SELECT r.restaurant_id, 'Gọi món trước', 'Đặt món', 'Đặt món trước khi đến', 0, 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Phố Biển%' AND NOT EXISTS (SELECT 1 FROM restaurant_service WHERE restaurant_id = r.restaurant_id AND name = 'Gọi món trước');
INSERT INTO restaurant_service (restaurant_id, name, category, description, price, status) SELECT r.restaurant_id, 'Giao hàng tận nơi', 'Giao hàng', 'Giao hàng trong bán kính 5km', 50000, 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Phố Biển%' AND NOT EXISTS (SELECT 1 FROM restaurant_service WHERE restaurant_id = r.restaurant_id AND name = 'Giao hàng tận nơi');
INSERT INTO restaurant_service (restaurant_id, name, category, description, price, status) SELECT r.restaurant_id, 'Đặt bàn VIP', 'Đặt bàn', 'Đặt trước phòng VIP', 50000, 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Phố Biển%' AND NOT EXISTS (SELECT 1 FROM restaurant_service WHERE restaurant_id = r.restaurant_id AND name = 'Đặt bàn VIP');

INSERT INTO restaurant_service (restaurant_id, name, category, description, price, status) SELECT r.restaurant_id, 'Gọi món trước', 'Đặt món', 'Đặt món trước khi đến', 0, 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Anchor%' AND NOT EXISTS (SELECT 1 FROM restaurant_service WHERE restaurant_id = r.restaurant_id AND name = 'Gọi món trước');
INSERT INTO restaurant_service (restaurant_id, name, category, description, price, status) SELECT r.restaurant_id, 'Giao hàng tận nơi', 'Giao hàng', 'Giao hàng trong bán kính 5km', 50000, 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Anchor%' AND NOT EXISTS (SELECT 1 FROM restaurant_service WHERE restaurant_id = r.restaurant_id AND name = 'Giao hàng tận nơi');
INSERT INTO restaurant_service (restaurant_id, name, category, description, price, status) SELECT r.restaurant_id, 'Đặt bàn VIP', 'Đặt bàn', 'Đặt trước phòng VIP', 50000, 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Anchor%' AND NOT EXISTS (SELECT 1 FROM restaurant_service WHERE restaurant_id = r.restaurant_id AND name = 'Đặt bàn VIP');

INSERT INTO restaurant_service (restaurant_id, name, category, description, price, status) SELECT r.restaurant_id, 'Gọi món trước', 'Đặt món', 'Đặt món trước khi đến', 0, 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vietbamboo%' AND NOT EXISTS (SELECT 1 FROM restaurant_service WHERE restaurant_id = r.restaurant_id AND name = 'Gọi món trước');
INSERT INTO restaurant_service (restaurant_id, name, category, description, price, status) SELECT r.restaurant_id, 'Giao hàng tận nơi', 'Giao hàng', 'Giao hàng trong bán kính 5km', 50000, 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vietbamboo%' AND NOT EXISTS (SELECT 1 FROM restaurant_service WHERE restaurant_id = r.restaurant_id AND name = 'Giao hàng tận nơi');
INSERT INTO restaurant_service (restaurant_id, name, category, description, price, status) SELECT r.restaurant_id, 'Đặt bàn VIP', 'Đặt bàn', 'Đặt trước phòng VIP', 50000, 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vietbamboo%' AND NOT EXISTS (SELECT 1 FROM restaurant_service WHERE restaurant_id = r.restaurant_id AND name = 'Đặt bàn VIP');

INSERT INTO restaurant_service (restaurant_id, name, category, description, price, status) SELECT r.restaurant_id, 'Gọi món trước', 'Đặt món', 'Đặt món trước khi đến', 0, 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vườn Nướng%' AND NOT EXISTS (SELECT 1 FROM restaurant_service WHERE restaurant_id = r.restaurant_id AND name = 'Gọi món trước');
INSERT INTO restaurant_service (restaurant_id, name, category, description, price, status) SELECT r.restaurant_id, 'Giao hàng tận nơi', 'Giao hàng', 'Giao hàng trong bán kính 5km', 50000, 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vườn Nướng%' AND NOT EXISTS (SELECT 1 FROM restaurant_service WHERE restaurant_id = r.restaurant_id AND name = 'Giao hàng tận nơi');
INSERT INTO restaurant_service (restaurant_id, name, category, description, price, status) SELECT r.restaurant_id, 'Đặt bàn VIP', 'Đặt bàn', 'Đặt trước phòng VIP', 50000, 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Vườn Nướng%' AND NOT EXISTS (SELECT 1 FROM restaurant_service WHERE restaurant_id = r.restaurant_id AND name = 'Đặt bàn VIP');

INSERT INTO restaurant_service (restaurant_id, name, category, description, price, status) SELECT r.restaurant_id, 'Gọi món trước', 'Đặt món', 'Đặt món trước khi đến', 0, 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Zzuggubbong%' AND NOT EXISTS (SELECT 1 FROM restaurant_service WHERE restaurant_id = r.restaurant_id AND name = 'Gọi món trước');
INSERT INTO restaurant_service (restaurant_id, name, category, description, price, status) SELECT r.restaurant_id, 'Giao hàng tận nơi', 'Giao hàng', 'Giao hàng trong bán kính 5km', 50000, 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Zzuggubbong%' AND NOT EXISTS (SELECT 1 FROM restaurant_service WHERE restaurant_id = r.restaurant_id AND name = 'Giao hàng tận nơi');
INSERT INTO restaurant_service (restaurant_id, name, category, description, price, status) SELECT r.restaurant_id, 'Đặt bàn VIP', 'Đặt bàn', 'Đặt trước phòng VIP', 50000, 'AVAILABLE' FROM restaurant_profile r WHERE r.restaurant_name LIKE '%Zzuggubbong%' AND NOT EXISTS (SELECT 1 FROM restaurant_service WHERE restaurant_id = r.restaurant_id AND name = 'Đặt bàn VIP');

-- =====================================================
-- PHẦN 4: CẬP NHẬT GIÁ THÀNH 50.000 VNĐ CHO TẤT CẢ
-- =====================================================

UPDATE dish SET price = 50000 WHERE restaurant_id IN (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Hải Sản Ngọc Hương%' OR restaurant_name LIKE '%Akataiyo%' OR restaurant_name LIKE '%Phố Biển%' OR restaurant_name LIKE '%Anchor%' OR restaurant_name LIKE '%Vietbamboo%' OR restaurant_name LIKE '%Vườn Nướng%' OR restaurant_name LIKE '%Zzuggubbong%');
UPDATE restaurant_service SET price = 50000 WHERE restaurant_id IN (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Hải Sản Ngọc Hương%' OR restaurant_name LIKE '%Akataiyo%' OR restaurant_name LIKE '%Phố Biển%' OR restaurant_name LIKE '%Anchor%' OR restaurant_name LIKE '%Vietbamboo%' OR restaurant_name LIKE '%Vườn Nướng%' OR restaurant_name LIKE '%Zzuggubbong%') AND price > 0;
UPDATE restaurant_table SET depositamount = 50000 WHERE restaurant_id IN (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Hải Sản Ngọc Hương%' OR restaurant_name LIKE '%Akataiyo%' OR restaurant_name LIKE '%Phố Biển%' OR restaurant_name LIKE '%Anchor%' OR restaurant_name LIKE '%Vietbamboo%' OR restaurant_name LIKE '%Vườn Nướng%' OR restaurant_name LIKE '%Zzuggubbong%');
UPDATE restaurant_profile SET average_price = 50000 WHERE restaurant_name LIKE '%Hải Sản Ngọc Hương%' OR restaurant_name LIKE '%Akataiyo%' OR restaurant_name LIKE '%Phố Biển%' OR restaurant_name LIKE '%Anchor%' OR restaurant_name LIKE '%Vietbamboo%' OR restaurant_name LIKE '%Vườn Nướng%' OR restaurant_name LIKE '%Zzuggubbong%';

-- =====================================================
-- PHẦN 5: APPROVE NHÀ HÀNG VÀ FIX STATUS
-- =====================================================

UPDATE restaurant_profile SET approval_status = 'APPROVED', approved_at = NOW(), approved_by = (SELECT id FROM users WHERE username = 'admin' LIMIT 1) WHERE (restaurant_name LIKE '%Hải Sản Ngọc Hương%' OR restaurant_name LIKE '%Akataiyo%' OR restaurant_name LIKE '%Phố Biển%' OR restaurant_name LIKE '%Anchor%' OR restaurant_name LIKE '%Vietbamboo%' OR restaurant_name LIKE '%Vườn Nướng%' OR restaurant_name LIKE '%Zzuggubbong%') AND approval_status = 'PENDING';
UPDATE dish SET status = 'AVAILABLE' WHERE restaurant_id IN (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Hải Sản Ngọc Hương%' OR restaurant_name LIKE '%Akataiyo%' OR restaurant_name LIKE '%Phố Biển%' OR restaurant_name LIKE '%Anchor%' OR restaurant_name LIKE '%Vietbamboo%' OR restaurant_name LIKE '%Vườn Nướng%' OR restaurant_name LIKE '%Zzuggubbong%') AND status != 'AVAILABLE';

-- =====================================================
-- PHẦN 6: VERIFICATION - Kiểm tra dữ liệu đã thêm
-- =====================================================

SELECT 'Hải Sản Ngọc Hương' as nha_hang, 'BÀN' as loai, COUNT(*) as so_luong FROM restaurant_table WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Hải Sản Ngọc Hương%' LIMIT 1)
UNION ALL SELECT 'Hải Sản Ngọc Hương', 'MÓN ĂN', COUNT(*) FROM dish WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Hải Sản Ngọc Hương%' LIMIT 1)
UNION ALL SELECT 'Hải Sản Ngọc Hương', 'DỊCH VỤ', COUNT(*) FROM restaurant_service WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Hải Sản Ngọc Hương%' LIMIT 1)
UNION ALL SELECT 'Akataiyo', 'BÀN', COUNT(*) FROM restaurant_table WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Akataiyo%' LIMIT 1)
UNION ALL SELECT 'Akataiyo', 'MÓN ĂN', COUNT(*) FROM dish WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Akataiyo%' LIMIT 1)
UNION ALL SELECT 'Akataiyo', 'DỊCH VỤ', COUNT(*) FROM restaurant_service WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Akataiyo%' LIMIT 1)
UNION ALL SELECT 'Phố Biển', 'BÀN', COUNT(*) FROM restaurant_table WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Phố Biển%' LIMIT 1)
UNION ALL SELECT 'Phố Biển', 'MÓN ĂN', COUNT(*) FROM dish WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Phố Biển%' LIMIT 1)
UNION ALL SELECT 'Phố Biển', 'DỊCH VỤ', COUNT(*) FROM restaurant_service WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Phố Biển%' LIMIT 1)
UNION ALL SELECT 'The Anchor', 'BÀN', COUNT(*) FROM restaurant_table WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Anchor%' LIMIT 1)
UNION ALL SELECT 'The Anchor', 'MÓN ĂN', COUNT(*) FROM dish WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Anchor%' LIMIT 1)
UNION ALL SELECT 'The Anchor', 'DỊCH VỤ', COUNT(*) FROM restaurant_service WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Anchor%' LIMIT 1)
UNION ALL SELECT 'Vietbamboo', 'BÀN', COUNT(*) FROM restaurant_table WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Vietbamboo%' LIMIT 1)
UNION ALL SELECT 'Vietbamboo', 'MÓN ĂN', COUNT(*) FROM dish WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Vietbamboo%' LIMIT 1)
UNION ALL SELECT 'Vietbamboo', 'DỊCH VỤ', COUNT(*) FROM restaurant_service WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Vietbamboo%' LIMIT 1)
UNION ALL SELECT 'Vườn Nướng', 'BÀN', COUNT(*) FROM restaurant_table WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Vườn Nướng%' LIMIT 1)
UNION ALL SELECT 'Vườn Nướng', 'MÓN ĂN', COUNT(*) FROM dish WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Vườn Nướng%' LIMIT 1)
UNION ALL SELECT 'Vườn Nướng', 'DỊCH VỤ', COUNT(*) FROM restaurant_service WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Vườn Nướng%' LIMIT 1)
UNION ALL SELECT 'Zzuggubbong', 'BÀN', COUNT(*) FROM restaurant_table WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Zzuggubbong%' LIMIT 1)
UNION ALL SELECT 'Zzuggubbong', 'MÓN ĂN', COUNT(*) FROM dish WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Zzuggubbong%' LIMIT 1)
UNION ALL SELECT 'Zzuggubbong', 'DỊCH VỤ', COUNT(*) FROM restaurant_service WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Zzuggubbong%' LIMIT 1);

