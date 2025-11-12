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
