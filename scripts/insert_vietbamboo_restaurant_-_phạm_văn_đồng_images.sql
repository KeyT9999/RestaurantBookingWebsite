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
