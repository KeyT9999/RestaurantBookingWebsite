-- =====================================================
-- SQL Script: TẠO LẠI NHÀ HÀNG "AI"
-- =====================================================
-- Script này sẽ tạo lại nhà hàng "AI" với thông tin cơ bản
-- =====================================================

DO $$
DECLARE
    v_user_id UUID;
    v_owner_id UUID;
    v_restaurant_id INTEGER;
    v_username VARCHAR(255) := 'Taiphan';  -- Có thể thay đổi username
BEGIN
    -- Kiểm tra xem nhà hàng đã tồn tại chưa
    SELECT restaurant_id INTO v_restaurant_id
    FROM restaurant_profile
    WHERE restaurant_name ILIKE '%AI%';
    
    IF v_restaurant_id IS NOT NULL THEN
        RAISE NOTICE '⚠️  Nhà hàng "AI" đã tồn tại với ID: %', v_restaurant_id;
        RAISE NOTICE '   Tên nhà hàng: %', (SELECT restaurant_name FROM restaurant_profile WHERE restaurant_id = v_restaurant_id);
        RETURN;
    END IF;
    
    -- Kiểm tra xem ID 37 đã được sử dụng chưa
    SELECT restaurant_id INTO v_restaurant_id
    FROM restaurant_profile
    WHERE restaurant_id = 37;
    
    IF v_restaurant_id IS NOT NULL THEN
        RAISE EXCEPTION '⚠️  Restaurant ID 37 đã được sử dụng bởi nhà hàng: %', 
            (SELECT restaurant_name FROM restaurant_profile WHERE restaurant_id = 37);
    END IF;
    
    RAISE NOTICE '🚀 Bắt đầu tạo nhà hàng "AI" với ID = 37...';
    
    -- BƯỚC 1: Tìm hoặc tạo User
    -- Tìm user Taiphan (hoặc user RESTAURANT_OWNER đầu tiên)
    SELECT id INTO v_user_id
    FROM users
    WHERE username = v_username OR role = 'RESTAURANT_OWNER'
    ORDER BY CASE WHEN username = v_username THEN 0 ELSE 1 END
    LIMIT 1;
    
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'Không tìm thấy user với username "%" hoặc user có role RESTAURANT_OWNER. Vui lòng tạo user trước!', v_username;
    END IF;
    
    RAISE NOTICE '✅ Tìm thấy user: %', v_user_id;
    
    -- BƯỚC 2: Tìm hoặc tạo RestaurantOwner
    SELECT owner_id INTO v_owner_id
    FROM restaurant_owner
    WHERE user_id = v_user_id;
    
    IF v_owner_id IS NULL THEN
        -- Tạo RestaurantOwner nếu chưa có
        INSERT INTO restaurant_owner (owner_id, user_id, owner_name, created_at, updated_at)
        VALUES (
            gen_random_uuid(),
            v_user_id,
            COALESCE((SELECT full_name FROM users WHERE id = v_user_id), 'Restaurant Owner'),
            NOW(),
            NOW()
        )
        RETURNING owner_id INTO v_owner_id;
        
        RAISE NOTICE '✅ Đã tạo RestaurantOwner: %', v_owner_id;
    ELSE
        RAISE NOTICE '✅ Đã tìm thấy RestaurantOwner: %', v_owner_id;
    END IF;
    
    -- BƯỚC 3: Tạo Restaurant Profile với ID = 37
    -- Cần tạm thời set sequence để có thể insert với ID cụ thể
    PERFORM setval('restaurant_profile_restaurant_id_seq', GREATEST(37, (SELECT COALESCE(MAX(restaurant_id), 0) FROM restaurant_profile)));
    
    INSERT INTO restaurant_profile (
        restaurant_id,  -- Chỉ định ID = 37
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
        contract_signed, 
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
    ) VALUES (
        37,  -- ID cụ thể
        v_owner_id, 
        'AI Restaurant',
        '123 Đường AI, Phường AI, Quận AI, TP. Hồ Chí Minh',
        '0909123456',
        'Nhà hàng AI chuyên về các món ăn được đề xuất bởi trí tuệ nhân tạo. Menu được tối ưu hóa dựa trên sở thích và xu hướng ẩm thực hiện đại. Không gian hiện đại, phù hợp cho giới trẻ và những người yêu thích công nghệ.',
        'Fusion',
        '10:00 - 22:00',
        250000.00,
        NULL,
        'PENDING',
        FALSE,
        TRUE,
        NOW(),
        '1.0',
        NOW(),
        NOW(),
        'TP. Hồ Chí Minh',
        'Nhà hàng AI - Ẩm thực thông minh',
        'Trải nghiệm ẩm thực được tối ưu bởi AI',
        'Menu AI, Không gian hiện đại, Công nghệ tiên tiến, Phù hợp giới trẻ',
        'Món AI đặc biệt, Set menu AI, Combo AI',
        'WiFi miễn phí, Điều hòa, Chỗ đậu xe, Quầy bar hiện đại, Nhạc nền',
        'Có chỗ đậu xe máy. Không có chỗ đậu ô tô riêng.',
        'Đặt bàn trước 30 phút. Nhà hàng nhận đặt bàn từ 10:00 - 21:30.',
        'Combo cho 2 người giảm 10%. Happy hour từ 15:00 - 17:00.'
    )
    RETURNING restaurant_id INTO v_restaurant_id;
    
    -- Cập nhật sequence sau khi insert
    PERFORM setval('restaurant_profile_restaurant_id_seq', GREATEST(37, (SELECT MAX(restaurant_id) FROM restaurant_profile)));
    
    -- Xác minh ID
    IF v_restaurant_id != 37 THEN
        RAISE EXCEPTION '❌ Lỗi: Restaurant ID không đúng! Mong đợi 37 nhưng nhận được %', v_restaurant_id;
    END IF;
    
    RAISE NOTICE '';
    RAISE NOTICE '✅ ✅ ✅ HOÀN TẤT!';
    RAISE NOTICE '   Đã tạo thành công nhà hàng "AI Restaurant"';
    RAISE NOTICE '   Restaurant ID: % (đúng như yêu cầu)', v_restaurant_id;
    RAISE NOTICE '   Owner ID: %', v_owner_id;
    RAISE NOTICE '';
    RAISE NOTICE '📝 Lưu ý:';
    RAISE NOTICE '   - Nhà hàng đang ở trạng thái PENDING (cần admin duyệt)';
    RAISE NOTICE '   - Có thể thêm tables, dishes, services sau bằng script khác';
    RAISE NOTICE '';
    
END $$;

-- Kiểm tra lại
SELECT 
    restaurant_id,
    restaurant_name,
    address,
    phone,
    approval_status,
    created_at
FROM restaurant_profile 
WHERE restaurant_name ILIKE '%AI%'
ORDER BY created_at DESC;

