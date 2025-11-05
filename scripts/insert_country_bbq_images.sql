-- =====================================================
-- SQL Script: Thêm ảnh cho nhà hàng "Country BBQ & Beer"
-- Restaurant ID: 43
-- =====================================================

DO $$
DECLARE
    v_restaurant_id INTEGER := 43;
    v_image_count INTEGER;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM restaurant_profile WHERE restaurant_id = v_restaurant_id) THEN
        RAISE EXCEPTION 'Restaurant với ID % không tồn tại!', v_restaurant_id;
    END IF;
    
    -- COVER IMAGE (ảnh đầu tiên)
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (43, 'cover', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762352145/restaurants/43/media/cover/cover_0_1762351355.jpg', NOW());
    
    -- GALLERY IMAGES
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (43, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762352147/restaurants/43/media/gallery/gallery_1_1762351355.jpg', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (43, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762352150/restaurants/43/media/gallery/gallery_2_1762351355.jpg', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (43, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762352152/restaurants/43/media/gallery/gallery_3_1762351355.jpg', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (43, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762352153/restaurants/43/media/gallery/gallery_4_1762351355.jpg', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (43, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762352154/restaurants/43/media/gallery/gallery_5_1762351355.jpg', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (43, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762352157/restaurants/43/media/gallery/gallery_6_1762351355.jpg', NOW());
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (43, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762352159/restaurants/43/media/gallery/gallery_7_1762351355.jpg', NOW());
    
    SELECT COUNT(*) INTO v_image_count
    FROM restaurant_media
    WHERE restaurant_id = v_restaurant_id;
    
    RAISE NOTICE '✅ IMAGES ADDED SUCCESSFULLY!';
    RAISE NOTICE 'Restaurant ID: %', v_restaurant_id;
    RAISE NOTICE 'Total images: %', v_image_count;
    
END $$;
