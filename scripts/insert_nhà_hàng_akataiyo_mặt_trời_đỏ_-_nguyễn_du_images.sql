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
