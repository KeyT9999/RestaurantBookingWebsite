-- =====================================================
-- SQL Script: Thêm nhà hàng "Cơm niêu 3 Cá Bống – Nguyễn Tri Phương"
-- Owner: Taiphan
-- Status: PENDING (cần admin duyệt)
-- =====================================================

-- BƯỚC 1: Kiểm tra và tìm owner "Taiphan"
-- =====================================================
-- Kiểm tra xem user Taiphan có tồn tại không
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
        -- Tạo RestaurantOwner nếu chưa có
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
    
    -- BƯỚC 2: Kiểm tra nhà hàng đã tồn tại chưa
    SELECT restaurant_id INTO v_restaurant_id
    FROM restaurant_profile
    WHERE restaurant_name LIKE '%Cơm niêu 3 Cá Bống%';
    
    IF v_restaurant_id IS NOT NULL THEN
        RAISE NOTICE 'Restaurant already exists with ID: %. Skipping insert.', v_restaurant_id;
        RETURN;
    END IF;
    
    -- BƯỚC 3: INSERT vào restaurant_profile với status PENDING
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
        
        -- Approval fields - SET PENDING để admin duyệt
        approval_status,
        approved_by,
        approved_at,
        contract_signed,
        contract_signed_at,
        terms_accepted,
        terms_accepted_at,
        terms_version,
        
        -- Timestamps
        created_at,
        updated_at,
        
        -- Extended fields
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
        v_owner_id,  -- owner_id từ Taiphan
        
        'Cơm niêu 3 Cá Bống – Nguyễn Tri Phương',  -- restaurant_name
        
        '123 Nguyễn Tri Phương, Phường 8, Quận 10, TP. Hồ Chí Minh',  -- address
        
        '0905123456',  -- phone (random Vietnamese phone number)
        
        'Nhà hàng chuyên về các món cơm niêu và cá bống nướng truyền thống. Không gian ấm cúng, phù hợp cho gia đình và nhóm bạn. Đặc biệt nổi tiếng với món cơm niêu cá bống nướng than hoa thơm ngon, đậm đà hương vị quê nhà.',  -- description
        
        'Việt Nam',  -- cuisine_type
        
        '10:00 - 22:00',  -- opening_hours (quán ăn Việt Nam thường mở từ 10h sáng đến 10h tối)
        
        85000.00,  -- average_price (giá thấp để dễ test - 85k)
        
        NULL,  -- website_url
        
        -- Approval fields - SET PENDING để admin duyệt
        'PENDING',  -- approval_status (cần admin duyệt)
        NULL,  -- approved_by (chưa được duyệt)
        NULL,  -- approved_at (chưa được duyệt)
        FALSE,  -- contract_signed (chưa ký hợp đồng)
        NULL,  -- contract_signed_at
        TRUE,  -- terms_accepted (đã chấp nhận điều khoản)
        NOW(),  -- terms_accepted_at
        '1.0',  -- terms_version
        
        -- Timestamps
        NOW(),  -- created_at
        NOW(),  -- updated_at
        
        -- Extended fields
        'TP. Hồ Chí Minh',  -- hero_city
        'Cơm niêu cá bống nướng than hoa',  -- hero_headline
        'Hương vị quê nhà đậm đà',  -- hero_subheadline
        'Cơm niêu cá bống nướng than hoa, Cá bống kho tộ, Canh chua cá bống, Thịt kho tàu',  -- summary_highlights
        'Cơm niêu cá bống nướng, Cá bống kho tộ, Canh chua cá bống',  -- signature_dishes
        'WiFi miễn phí, Điều hòa, Chỗ đậu xe, Phục vụ nhanh',  -- amenities
        'Có chỗ đậu xe máy miễn phí trước nhà hàng',  -- parking_details
        'Đặt bàn trước 30 phút để đảm bảo có chỗ. Nhà hàng nhận đặt bàn từ 10:00 - 21:30 hàng ngày.',  -- booking_information
        'Khuyến mãi đặc biệt cho nhóm từ 5 người trở lên. Giảm 10% cho khách hàng đặt bàn online.'  -- booking_notes
    )
    RETURNING restaurant_id INTO v_restaurant_id;
    
    RAISE NOTICE '========================================';
    RAISE NOTICE '✅ RESTAURANT ADDED SUCCESSFULLY!';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Restaurant ID: %', v_restaurant_id;
    RAISE NOTICE 'Restaurant Name: Cơm niêu 3 Cá Bống – Nguyễn Tri Phương';
    RAISE NOTICE 'Owner: Taiphan';
    RAISE NOTICE 'Status: PENDING (cần admin duyệt)';
    RAISE NOTICE '========================================';
    
END $$;

-- =====================================================
-- VERIFICATION - Kiểm tra kết quả
-- =====================================================
-- Kiểm tra nhà hàng đã được tạo thành công
SELECT 
    r.restaurant_id,
    r.restaurant_name,
    r.address,
    r.phone,
    r.approval_status,
    r.average_price,
    r.opening_hours,
    u.username as owner_username,
    u.full_name as owner_name,
    ro.owner_id
FROM restaurant_profile r
JOIN restaurant_owner ro ON r.owner_id = ro.owner_id
JOIN users u ON ro.user_id = u.id
WHERE r.restaurant_name LIKE '%Cơm niêu 3 Cá Bống%'
ORDER BY r.created_at DESC
LIMIT 1;

-- =====================================================
-- HOÀN TẤT
-- =====================================================
-- ✅ Nhà hàng đã được thêm với status PENDING
-- 
-- ⚠️  QUAN TRỌNG: Ghi lại RESTAURANT_ID từ kết quả trên!
--    (Sẽ cần khi upload ảnh ở bước tiếp theo)
-- 
-- 📋 CÁC BƯỚC TIẾP THEO:
-- 
-- 1. Upload ảnh lên Cloudinary:
--    - Mở PowerShell
--    - Set environment variables:
--      $env:CLOUDINARY_CLOUD_NAME="your_cloud_name"
--      $env:CLOUDINARY_API_KEY="your_api_key"
--      $env:CLOUDINARY_API_SECRET="your_api_secret"
--    - Chạy: scripts\upload.bat
--    - Nhập restaurant_id khi script hỏi
--    - Script sẽ tạo file: scripts/insert_images.sql
-- 
-- 2. Chạy SQL script insert ảnh:
--    - Mở file: scripts/insert_images.sql
--    - Copy toàn bộ → Paste vào pgAdmin → Chạy (F5)
-- 
-- 3. Admin duyệt nhà hàng → status = APPROVED
-- 
-- 4. Nhà hàng sẽ hiển thị cho khách hàng

