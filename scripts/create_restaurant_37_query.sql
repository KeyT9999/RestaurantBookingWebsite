-- =====================================================
-- QUERY TẠO NHÀ HÀNG MỚI ID = 37 (Owner: Taiphan)
-- =====================================================

-- Tạo nhà hàng mới (không cần set sequence vì insert với ID cụ thể)
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
    (SELECT owner_id FROM restaurant_owner WHERE user_id = (SELECT id FROM users WHERE username = 'Taiphan')),
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
);

-- Kiểm tra
SELECT restaurant_id, restaurant_name, owner_id FROM restaurant_profile WHERE restaurant_id = 37;

