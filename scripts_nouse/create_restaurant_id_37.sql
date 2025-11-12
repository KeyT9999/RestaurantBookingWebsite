-- =====================================================
-- TẠO NHÀ HÀNG MỚI VỚI ID = 37
-- Owner: Taiphan
-- =====================================================

-- BƯỚC 1: Lấy owner_id của Taiphan
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
        RAISE EXCEPTION '❌ Không tìm thấy user "Taiphan"!';
    END IF;
    
    -- Tìm owner_id của Taiphan
    SELECT owner_id INTO v_owner_id
    FROM restaurant_owner
    WHERE user_id = v_user_id;
    
    IF v_owner_id IS NULL THEN
        RAISE EXCEPTION '❌ User "Taiphan" chưa có owner_id!';
    END IF;
    
    -- Kiểm tra ID 37 đã được sử dụng chưa
    SELECT restaurant_id INTO v_restaurant_id
    FROM restaurant_profile
    WHERE restaurant_id = 37;
    
    IF v_restaurant_id IS NOT NULL THEN
        RAISE EXCEPTION '❌ ID 37 đã được sử dụng bởi nhà hàng: %', 
            (SELECT restaurant_name FROM restaurant_profile WHERE restaurant_id = 37);
    END IF;
    
    RAISE NOTICE '✅ Tìm thấy owner_id của Taiphan: %', v_owner_id;
    RAISE NOTICE '🚀 Bắt đầu tạo nhà hàng với ID = 37...';
    
    -- Insert nhà hàng mới (không cần set sequence vì insert với ID cụ thể)
    INSERT INTO restaurant_profile (
        restaurant_id,
        owner_id,
        restaurant_name,
        address,
        phone,
        description,
        cuisine_type,
        opening_hours,
        average_price,
        approval_status,
        contract_signed,
        terms_accepted,
        terms_accepted_at,
        terms_version,
        created_at,
        updated_at
    ) VALUES (
        37,
        v_owner_id,
        'Phở Bò ABC',
        '123 Đường ABC, Phường XYZ, Quận 1, TP. Hồ Chí Minh',
        '0909123456',
        'Nhà hàng phở bò truyền thống với hương vị đặc biệt. Không gian ấm cúng, phù hợp cho gia đình và nhóm bạn.',
        'Phở',
        '06:00 - 22:00',
        80000.00,
        'PENDING',
        FALSE,
        TRUE,
        NOW(),
        '1.0',
        NOW(),
        NOW()
    )
    RETURNING restaurant_id INTO v_restaurant_id;
    
    RAISE NOTICE '';
    RAISE NOTICE '✅ ✅ ✅ HOÀN TẤT!';
    RAISE NOTICE '   Đã tạo nhà hàng "Phở Bò ABC" với ID = %', v_restaurant_id;
    RAISE NOTICE '   Owner ID: %', v_owner_id;
    
END $$;

-- Kiểm tra lại
SELECT 
    restaurant_id,
    restaurant_name,
    owner_id,
    address,
    phone,
    approval_status,
    created_at
FROM restaurant_profile 
WHERE restaurant_id = 37;

