-- =====================================================
-- SQL Script: Thêm nhà hàng "Country BBQ & Beer - Trần Bạch Đằng"
-- Owner: Taiphan
-- Status: PENDING (cần admin duyệt)
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
    WHERE restaurant_name LIKE '%Country BBQ%' OR restaurant_name LIKE '%Trần Bạch Đằng%';
    
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
        'Country BBQ & Beer - Trần Bạch Đằng',
        '123 Trần Bạch Đằng, Phường 2, Quận Tân Bình, TP. Hồ Chí Minh',
        '0909123456',
        'Nhà hàng BBQ và Bia thơm ngon với không gian rộng rãi, phù hợp cho các buổi tụ tập bạn bè và gia đình. Chuyên về các món BBQ nướng than hoa, kèm theo bia craft đa dạng. Không gian thoáng đãng, có sân ngoài trời, phù hợp cho các buổi tiệc nhóm lớn.',
        'BBQ & Beer',
        '17:00 - 23:00',
        350000.00,
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
        'BBQ & Beer - Nướng than hoa thơm lừng',
        'Hương vị BBQ đậm đà, bia craft đa dạng',
        'BBQ nướng than hoa, Bia craft đa dạng, Không gian rộng rãi, Sân ngoài trời, Phù hợp nhóm lớn',
        'Sườn nướng BBQ, Gà nướng mật ong, Bò nướng sốt đặc biệt, Tôm nướng tỏi, Thịt ba chỉ nướng',
        'WiFi miễn phí, Điều hòa, Chỗ đậu xe, Sân ngoài trời, Nhạc sống cuối tuần, TV màn hình lớn',
        'Có chỗ đậu xe máy và ô tô miễn phí. Bãi đậu xe rộng rãi phía sau nhà hàng.',
        'Đặt bàn trước 2 giờ để đảm bảo có chỗ. Nhà hàng nhận đặt bàn từ 17:00 - 22:30 hàng ngày. Nhóm từ 8 người trở lên nên đặt trước 1 ngày.',
        'Khuyến mãi đặc biệt cho nhóm từ 8 người trở lên: giảm 15% tổng hóa đơn. Happy hour từ 17:00 - 19:00: bia giảm 20%.'
    )
    RETURNING restaurant_id INTO v_restaurant_id;
    
    RAISE NOTICE '========================================';
    RAISE NOTICE '✅ RESTAURANT ADDED SUCCESSFULLY!';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Restaurant ID: %', v_restaurant_id;
    RAISE NOTICE 'Restaurant Name: Country BBQ & Beer - Trần Bạch Đằng';
    RAISE NOTICE 'Owner: Taiphan';
    RAISE NOTICE 'Status: PENDING (cần admin duyệt)';
    RAISE NOTICE '========================================';
    
END $$;

