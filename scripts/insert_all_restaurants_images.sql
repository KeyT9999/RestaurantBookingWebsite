-- =====================================================
-- SQL Script: Thêm ảnh cho TẤT CẢ các nhà hàng
-- =====================================================

-- =====================================================
-- SQL Script: Thêm ảnh cho nhà hàng "Hải Sản Ngọc Hương – Võ Nguyên Giáp"
-- Restaurant ID: 45
-- =====================================================

DO $$
DECLARE
    v_restaurant_id INTEGER := 45;
    v_image_count INTEGER;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM restaurant_profile WHERE restaurant_id = v_restaurant_id) THEN
        RAISE EXCEPTION 'Restaurant với ID % không tồn tại!', v_restaurant_id;
    END IF;
    
    -- COVER IMAGE (ảnh đầu tiên)
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (45, 'cover', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354649/restaurants/45/media/cover/cover_0_1762351355.webp', NOW());
    
    -- GALLERY IMAGES
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (45, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354652/restaurants/45/media/gallery/gallery_1_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (45, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354654/restaurants/45/media/gallery/gallery_2_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (45, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354655/restaurants/45/media/gallery/gallery_3_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (45, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354658/restaurants/45/media/gallery/gallery_4_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (45, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354659/restaurants/45/media/gallery/gallery_5_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (45, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354660/restaurants/45/media/gallery/gallery_6_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (45, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354661/restaurants/45/media/gallery/gallery_7_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (45, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354662/restaurants/45/media/gallery/gallery_8_1762351355.webp', NOW());
    
    SELECT COUNT(*) INTO v_image_count
    FROM restaurant_media
    WHERE restaurant_id = v_restaurant_id;
    
    RAISE NOTICE '✅ IMAGES ADDED SUCCESSFULLY!';
    RAISE NOTICE 'Restaurant ID: %', v_restaurant_id;
    RAISE NOTICE 'Total images: %', v_image_count;
    
END $$;


-- =====================================================
-- SQL Script: Thêm ảnh cho nhà hàng "Nhà hàng Akataiyo Mặt Trời Đỏ - Nguyễn Du"
-- Restaurant ID: 46
-- =====================================================

DO $$
DECLARE
    v_restaurant_id INTEGER := 46;
    v_image_count INTEGER;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM restaurant_profile WHERE restaurant_id = v_restaurant_id) THEN
        RAISE EXCEPTION 'Restaurant với ID % không tồn tại!', v_restaurant_id;
    END IF;
    
    -- COVER IMAGE (ảnh đầu tiên)
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (46, 'cover', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354663/restaurants/46/media/cover/cover_0_1762351355.webp', NOW());
    
    -- GALLERY IMAGES
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (46, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354665/restaurants/46/media/gallery/gallery_1_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (46, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354666/restaurants/46/media/gallery/gallery_2_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (46, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354667/restaurants/46/media/gallery/gallery_3_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (46, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354668/restaurants/46/media/gallery/gallery_4_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (46, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354669/restaurants/46/media/gallery/gallery_5_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (46, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354672/restaurants/46/media/gallery/gallery_6_1762351355.webp', NOW());
    
    SELECT COUNT(*) INTO v_image_count
    FROM restaurant_media
    WHERE restaurant_id = v_restaurant_id;
    
    RAISE NOTICE '✅ IMAGES ADDED SUCCESSFULLY!';
    RAISE NOTICE 'Restaurant ID: %', v_restaurant_id;
    RAISE NOTICE 'Total images: %', v_image_count;
    
END $$;


-- =====================================================
-- SQL Script: Thêm ảnh cho nhà hàng "Phố Biển – Đảo Xanh"
-- Restaurant ID: 47
-- =====================================================

DO $$
DECLARE
    v_restaurant_id INTEGER := 47;
    v_image_count INTEGER;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM restaurant_profile WHERE restaurant_id = v_restaurant_id) THEN
        RAISE EXCEPTION 'Restaurant với ID % không tồn tại!', v_restaurant_id;
    END IF;
    
    -- COVER IMAGE (ảnh đầu tiên)
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (47, 'cover', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354673/restaurants/47/media/cover/cover_0_1762351355.webp', NOW());
    
    -- GALLERY IMAGES
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (47, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354675/restaurants/47/media/gallery/gallery_1_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (47, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354676/restaurants/47/media/gallery/gallery_2_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (47, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354677/restaurants/47/media/gallery/gallery_3_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (47, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354678/restaurants/47/media/gallery/gallery_4_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (47, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354679/restaurants/47/media/gallery/gallery_5_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (47, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354680/restaurants/47/media/gallery/gallery_6_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (47, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354681/restaurants/47/media/gallery/gallery_7_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (47, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354682/restaurants/47/media/gallery/gallery_8_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (47, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354683/restaurants/47/media/gallery/gallery_9_1762351355.webp', NOW());
    
    SELECT COUNT(*) INTO v_image_count
    FROM restaurant_media
    WHERE restaurant_id = v_restaurant_id;
    
    RAISE NOTICE '✅ IMAGES ADDED SUCCESSFULLY!';
    RAISE NOTICE 'Restaurant ID: %', v_restaurant_id;
    RAISE NOTICE 'Total images: %', v_image_count;
    
END $$;


-- =====================================================
-- SQL Script: Thêm ảnh cho nhà hàng "The Anchor (Restaurant & Bierhaus) - Trần Phú"
-- Restaurant ID: 48
-- =====================================================

DO $$
DECLARE
    v_restaurant_id INTEGER := 48;
    v_image_count INTEGER;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM restaurant_profile WHERE restaurant_id = v_restaurant_id) THEN
        RAISE EXCEPTION 'Restaurant với ID % không tồn tại!', v_restaurant_id;
    END IF;
    
    -- COVER IMAGE (ảnh đầu tiên)
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (48, 'cover', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354684/restaurants/48/media/cover/cover_0_1762351355.webp', NOW());
    
    -- GALLERY IMAGES
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (48, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354686/restaurants/48/media/gallery/gallery_1_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (48, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354687/restaurants/48/media/gallery/gallery_2_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (48, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354689/restaurants/48/media/gallery/gallery_3_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (48, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354690/restaurants/48/media/gallery/gallery_4_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (48, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354691/restaurants/48/media/gallery/gallery_5_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (48, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354692/restaurants/48/media/gallery/gallery_6_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (48, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354693/restaurants/48/media/gallery/gallery_7_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (48, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354701/restaurants/48/media/gallery/gallery_8_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (48, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354702/restaurants/48/media/gallery/gallery_9_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (48, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354703/restaurants/48/media/gallery/gallery_10_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (48, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354705/restaurants/48/media/gallery/gallery_11_1762351355.webp', NOW());
    
    SELECT COUNT(*) INTO v_image_count
    FROM restaurant_media
    WHERE restaurant_id = v_restaurant_id;
    
    RAISE NOTICE '✅ IMAGES ADDED SUCCESSFULLY!';
    RAISE NOTICE 'Restaurant ID: %', v_restaurant_id;
    RAISE NOTICE 'Total images: %', v_image_count;
    
END $$;


-- =====================================================
-- SQL Script: Thêm ảnh cho nhà hàng "Vietbamboo Restaurant - Phạm Văn Đồng"
-- Restaurant ID: 49
-- =====================================================

DO $$
DECLARE
    v_restaurant_id INTEGER := 49;
    v_image_count INTEGER;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM restaurant_profile WHERE restaurant_id = v_restaurant_id) THEN
        RAISE EXCEPTION 'Restaurant với ID % không tồn tại!', v_restaurant_id;
    END IF;
    
    -- COVER IMAGE (ảnh đầu tiên)
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (49, 'cover', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354706/restaurants/49/media/cover/cover_0_1762351355.webp', NOW());
    
    -- GALLERY IMAGES
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (49, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354707/restaurants/49/media/gallery/gallery_1_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (49, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354709/restaurants/49/media/gallery/gallery_2_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (49, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354711/restaurants/49/media/gallery/gallery_3_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (49, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354712/restaurants/49/media/gallery/gallery_4_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (49, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354713/restaurants/49/media/gallery/gallery_5_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (49, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354714/restaurants/49/media/gallery/gallery_6_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (49, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354717/restaurants/49/media/gallery/gallery_7_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (49, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354718/restaurants/49/media/gallery/gallery_8_1762351355.webp', NOW());
    
    SELECT COUNT(*) INTO v_image_count
    FROM restaurant_media
    WHERE restaurant_id = v_restaurant_id;
    
    RAISE NOTICE '✅ IMAGES ADDED SUCCESSFULLY!';
    RAISE NOTICE 'Restaurant ID: %', v_restaurant_id;
    RAISE NOTICE 'Total images: %', v_image_count;
    
END $$;


-- =====================================================
-- SQL Script: Thêm ảnh cho nhà hàng "Vườn Nướng - Đường 304"
-- Restaurant ID: 50
-- =====================================================

DO $$
DECLARE
    v_restaurant_id INTEGER := 50;
    v_image_count INTEGER;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM restaurant_profile WHERE restaurant_id = v_restaurant_id) THEN
        RAISE EXCEPTION 'Restaurant với ID % không tồn tại!', v_restaurant_id;
    END IF;
    
    -- COVER IMAGE (ảnh đầu tiên)
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (50, 'cover', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354719/restaurants/50/media/cover/cover_0_1762351355.webp', NOW());
    
    -- GALLERY IMAGES
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (50, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354720/restaurants/50/media/gallery/gallery_1_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (50, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354721/restaurants/50/media/gallery/gallery_2_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (50, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354722/restaurants/50/media/gallery/gallery_3_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (50, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354723/restaurants/50/media/gallery/gallery_4_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (50, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354724/restaurants/50/media/gallery/gallery_5_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (50, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354725/restaurants/50/media/gallery/gallery_6_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (50, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354726/restaurants/50/media/gallery/gallery_7_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (50, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354728/restaurants/50/media/gallery/gallery_8_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (50, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354729/restaurants/50/media/gallery/gallery_9_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (50, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354731/restaurants/50/media/gallery/gallery_10_1762351355.webp', NOW());
    
    SELECT COUNT(*) INTO v_image_count
    FROM restaurant_media
    WHERE restaurant_id = v_restaurant_id;
    
    RAISE NOTICE '✅ IMAGES ADDED SUCCESSFULLY!';
    RAISE NOTICE 'Restaurant ID: %', v_restaurant_id;
    RAISE NOTICE 'Total images: %', v_image_count;
    
END $$;


-- =====================================================
-- SQL Script: Thêm ảnh cho nhà hàng "Zzuggubbong - Nguyễn Hữu Thông"
-- Restaurant ID: 51
-- =====================================================

DO $$
DECLARE
    v_restaurant_id INTEGER := 51;
    v_image_count INTEGER;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM restaurant_profile WHERE restaurant_id = v_restaurant_id) THEN
        RAISE EXCEPTION 'Restaurant với ID % không tồn tại!', v_restaurant_id;
    END IF;
    
    -- COVER IMAGE (ảnh đầu tiên)
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (51, 'cover', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354733/restaurants/51/media/cover/cover_0_1762351355.webp', NOW());
    
    -- GALLERY IMAGES
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (51, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354734/restaurants/51/media/gallery/gallery_1_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (51, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354735/restaurants/51/media/gallery/gallery_2_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (51, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354736/restaurants/51/media/gallery/gallery_3_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (51, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354738/restaurants/51/media/gallery/gallery_4_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (51, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354739/restaurants/51/media/gallery/gallery_5_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (51, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354741/restaurants/51/media/gallery/gallery_6_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (51, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354742/restaurants/51/media/gallery/gallery_7_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (51, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354743/restaurants/51/media/gallery/gallery_8_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (51, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354744/restaurants/51/media/gallery/gallery_9_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (51, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354745/restaurants/51/media/gallery/gallery_10_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (51, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762354746/restaurants/51/media/gallery/gallery_11_1762351355.webp', NOW());
    
    SELECT COUNT(*) INTO v_image_count
    FROM restaurant_media
    WHERE restaurant_id = v_restaurant_id;
    
    RAISE NOTICE '✅ IMAGES ADDED SUCCESSFULLY!';
    RAISE NOTICE 'Restaurant ID: %', v_restaurant_id;
    RAISE NOTICE 'Total images: %', v_image_count;
    
END $$;


