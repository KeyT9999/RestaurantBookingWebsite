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
