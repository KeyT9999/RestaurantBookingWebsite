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
