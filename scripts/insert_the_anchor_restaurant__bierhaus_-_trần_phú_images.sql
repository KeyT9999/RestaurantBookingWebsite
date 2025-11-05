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
