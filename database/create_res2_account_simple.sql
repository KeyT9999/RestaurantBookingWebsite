
-- Script tạo thông tin nhà hàng cho account res có sẵn
-- Account: res (ID: 7bc1aae0-f73e-43f5-954e-0986b8bc566c, email: trankimthang8547@gmail.com)
-- Nhà hàng: BBQ Hàn Quốc sang trọng

-- 1. Tạo restaurant owner record cho account res
INSERT INTO restaurant_owner (user_id, owner_name, phone, address)
SELECT 
    '7bc1aae0-f73e-43f5-954e-0986b8bc566c'::uuid, 
    'Kim Min Joon',
    '0908765432',
    '456 Đường Nguyễn Huệ, Quận 1, TP.HCM'
WHERE NOT EXISTS (
    SELECT 1 FROM restaurant_owner WHERE user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c'::uuid
);

-- 2. Tạo restaurant profile cho account res
INSERT INTO restaurant_profile (
    owner_id,
    restaurant_name,
    cuisine_type,
    opening_hours,
    average_price,
    website_url
) VALUES (
    (SELECT owner_id FROM restaurant_owner WHERE user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c'::uuid),
    'Seoul BBQ Premium',
    'BBQ Hàn Quốc',
    '11:00 - 23:00',
    350000.00,
    'https://www.seoulbbqpremium.com'
);

-- 3. Thêm các bàn BBQ cho nhà hàng
INSERT INTO restaurant_table (restaurant_id, table_name, capacity, table_image, status, depositamount)
SELECT 
    rp.restaurant_id,
    table_info.table_name,
    table_info.capacity,
    table_info.table_image,
    'available',
    table_info.depositamount
FROM restaurant_profile rp
CROSS JOIN (
    VALUES 
        ('BBQ Table 1', 2, 'bbq_table_2_seat.jpg', 80000),
        ('BBQ Table 2', 2, 'bbq_table_2_seat.jpg', 80000),
        ('BBQ Table 3', 4, 'bbq_table_4_seat.jpg', 150000),
        ('BBQ Table 4', 4, 'bbq_table_4_seat.jpg', 150000),
        ('BBQ Table 5', 6, 'bbq_table_6_seat.jpg', 200000),
        ('BBQ Table 6', 6, 'bbq_table_6_seat.jpg', 200000),
        ('VIP BBQ Room 1', 8, 'vip_bbq_room_8.jpg', 300000),
        ('VIP BBQ Room 2', 10, 'vip_bbq_room_10.jpg', 400000),
        ('VIP BBQ Room 3', 12, 'vip_bbq_room_12.jpg', 500000),
        ('Private Room 1', 4, 'private_bbq_room_4.jpg', 250000),
        ('Private Room 2', 6, 'private_bbq_room_6.jpg', 350000)
) AS table_info(table_name, capacity, table_image, depositamount)
WHERE rp.owner_id = (SELECT owner_id FROM restaurant_owner WHERE user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c'::uuid);

-- 4. Thêm các món BBQ Hàn Quốc cho nhà hàng
INSERT INTO dish (restaurant_id, name, description, price, category, status)
SELECT 
    rp.restaurant_id,
    dish_info.name,
    dish_info.description,
    dish_info.price,
    dish_info.category,
    'available'
FROM restaurant_profile rp
CROSS JOIN (
    VALUES 
        -- Thịt BBQ
        ('Samgyeopsal', 'Thịt ba chỉ nướng BBQ Hàn Quốc với kimchi', 180000, 'BBQ'),
        ('Galbi', 'Sườn bò nướng tẩm sốt đặc biệt', 250000, 'BBQ'),
        ('Bulgogi', 'Thịt bò nướng tẩm sốt bulgogi truyền thống', 220000, 'BBQ'),
        ('Chadolbaegi', 'Thịt bò mỏng nướng BBQ', 200000, 'BBQ'),
        ('Dwaeji Galbi', 'Sườn heo nướng BBQ', 190000, 'BBQ'),
        
        -- Món phụ
        ('Kimchi', 'Kimchi truyền thống Hàn Quốc', 50000, 'Món phụ'),
        ('Japchae', 'Miến trộn thịt bò và rau củ', 120000, 'Món phụ'),
        ('Bibimbap', 'Cơm trộn với thịt và rau củ', 150000, 'Món phụ'),
        ('Tteokbokki', 'Bánh gạo cay', 80000, 'Món phụ'),
        ('Korean Pancake', 'Bánh xèo Hàn Quốc', 100000, 'Món phụ'),
        
        -- Canh và lẩu
        ('Kimchi Jjigae', 'Canh kimchi cay', 120000, 'Canh'),
        ('Doenjang Jjigae', 'Canh tương đậu', 100000, 'Canh'),
        ('Budae Jjigae', 'Lẩu quân đội Hàn Quốc', 180000, 'Lẩu'),
        
        -- Đồ uống
        ('Soju', 'Rượu soju Hàn Quốc', 80000, 'Đồ uống'),
        ('Makgeolli', 'Rượu gạo truyền thống', 60000, 'Đồ uống'),
        ('Korean Tea', 'Trà Hàn Quốc', 30000, 'Đồ uống')
) AS dish_info(name, description, price, category)
WHERE rp.owner_id = (SELECT owner_id FROM restaurant_owner WHERE user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c'::uuid);

-- 5. Thêm các dịch vụ BBQ cho nhà hàng
INSERT INTO restaurant_service (restaurant_id, name, category, description, price, status)
SELECT 
    rp.restaurant_id,
    service_info.name,
    service_info.category,
    service_info.description,
    service_info.price,
    'available'
FROM restaurant_profile rp
CROSS JOIN (
    VALUES 
        ('Dịch vụ nướng BBQ', 'BBQ', 'Nhân viên hỗ trợ nướng BBQ chuyên nghiệp', 100000),
        ('Combo BBQ Premium', 'Combo', 'Combo BBQ cao cấp với thịt nhập khẩu', 600000),
        ('Phòng VIP riêng tư', 'VIP', 'Phòng VIP với hệ thống thông gió cao cấp', 200000),
        ('Dịch vụ giải trí K-Pop', 'Giải trí', 'Nhạc K-Pop và giải trí theo yêu cầu', 150000),
        ('Dịch vụ chụp ảnh Hanbok', 'Kỷ niệm', 'Chụp ảnh với trang phục Hanbok truyền thống', 200000),
        ('Dịch vụ giao hàng BBQ', 'Giao hàng', 'Giao hàng BBQ tận nơi trong bán kính 15km', 80000),
        ('Dịch vụ đặt bàn trước', 'Đặt bàn', 'Đặt bàn trước với ưu đãi đặc biệt', 0),
        ('Valet parking VIP', 'Parking', 'Dịch vụ đỗ xe valet cao cấp', 50000)
) AS service_info(name, category, description, price)
WHERE rp.owner_id = (SELECT owner_id FROM restaurant_owner WHERE user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c'::uuid);

-- 6. Thêm media BBQ cho nhà hàng
INSERT INTO restaurant_media (restaurant_id, type, url)
SELECT 
    rp.restaurant_id,
    media_info.type,
    media_info.url
FROM restaurant_profile rp
CROSS JOIN (
    VALUES 
        ('logo', 'https://example.com/seoul_bbq_logo.jpg'),
        ('cover', 'https://example.com/seoul_bbq_cover.jpg'),
        ('table_layout', 'https://example.com/bbq_table_layout.jpg'),
        ('interior', 'https://example.com/bbq_interior1.jpg'),
        ('interior', 'https://example.com/bbq_interior2.jpg'),
        ('exterior', 'https://example.com/seoul_bbq_exterior.jpg'),
        ('food', 'https://example.com/bbq_food1.jpg'),
        ('food', 'https://example.com/bbq_food2.jpg')
) AS media_info(type, url)
WHERE rp.owner_id = (SELECT owner_id FROM restaurant_owner WHERE user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c'::uuid);

-- Kiểm tra kết quả
SELECT 'Thông tin nhà hàng đã được tạo thành công cho account res!' as message;

-- Hiển thị thông tin account và nhà hàng
SELECT 
    u.username,
    u.email,
    u.full_name,
    u.phone_number,
    u.address,
    ro.owner_name,
    ro.phone as owner_phone,
    ro.address as owner_address,
    rp.restaurant_name,
    rp.cuisine_type,
    rp.opening_hours,
    rp.average_price,
    rp.website_url
FROM users u
JOIN restaurant_owner ro ON u.id = ro.user_id
JOIN restaurant_profile rp ON ro.owner_id = rp.owner_id
WHERE u.id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c'::uuid;

-- Hiển thị số lượng bàn, món ăn, dịch vụ đã tạo
SELECT 
    'Bàn' as loai,
    COUNT(*) as so_luong
FROM restaurant_table rt
JOIN restaurant_profile rp ON rt.restaurant_id = rp.restaurant_id
WHERE rp.owner_id = (SELECT owner_id FROM restaurant_owner WHERE user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c'::uuid)

UNION ALL

SELECT 
    'Món ăn' as loai,
    COUNT(*) as so_luong
FROM dish d
JOIN restaurant_profile rp ON d.restaurant_id = rp.restaurant_id
WHERE rp.owner_id = (SELECT owner_id FROM restaurant_owner WHERE user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c'::uuid)

UNION ALL

SELECT 
    'Dịch vụ' as loai,
    COUNT(*) as so_luong
FROM restaurant_service rs
JOIN restaurant_profile rp ON rs.restaurant_id = rp.restaurant_id
WHERE rp.owner_id = (SELECT owner_id FROM restaurant_owner WHERE user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c'::uuid)

UNION ALL

SELECT 
    'Media' as loai,
    COUNT(*) as so_luong
FROM restaurant_media rm
JOIN restaurant_profile rp ON rm.restaurant_id = rp.restaurant_id
WHERE rp.owner_id = (SELECT owner_id FROM restaurant_owner WHERE user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c'::uuid);

