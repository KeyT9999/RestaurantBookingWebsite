-- =====================================================
-- SQL Script: Thêm ảnh cho nhà hàng "Hải Sản Bà Cường – Hoàng Sa"
-- Restaurant ID: 44
-- =====================================================

DO $$
DECLARE
    v_restaurant_id INTEGER := 44;
    v_image_count INTEGER;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM restaurant_profile WHERE restaurant_id = v_restaurant_id) THEN
        RAISE EXCEPTION 'Restaurant với ID % không tồn tại!', v_restaurant_id;
    END IF;
    
    -- COVER IMAGE (ảnh đầu tiên)
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (44, 'cover', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762352937/restaurants/44/media/cover/cover_0_1762351355.webp', NOW());
    
    -- GALLERY IMAGES
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (44, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762352940/restaurants/44/media/gallery/gallery_1_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (44, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762352942/restaurants/44/media/gallery/gallery_2_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (44, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762352944/restaurants/44/media/gallery/gallery_3_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (44, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762352946/restaurants/44/media/gallery/gallery_4_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (44, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762352948/restaurants/44/media/gallery/gallery_5_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (44, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762352949/restaurants/44/media/gallery/gallery_6_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (44, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762352951/restaurants/44/media/gallery/gallery_7_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (44, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762352952/restaurants/44/media/gallery/gallery_8_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (44, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762352953/restaurants/44/media/gallery/gallery_9_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (44, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762352955/restaurants/44/media/gallery/gallery_10_1762351355.webp', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (44, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762352957/restaurants/44/media/gallery/gallery_11_1762351355.webp', NOW());
    
    SELECT COUNT(*) INTO v_image_count
    FROM restaurant_media
    WHERE restaurant_id = v_restaurant_id;
    
    RAISE NOTICE '✅ IMAGES ADDED SUCCESSFULLY!';
    RAISE NOTICE 'Restaurant ID: %', v_restaurant_id;
    RAISE NOTICE 'Total images: %', v_image_count;
    
END $$;
