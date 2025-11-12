-- =====================================================
-- SQL Script: Thêm ảnh cho nhà hàng "Cơm niêu 3 Cá Bống"
-- Restaurant ID: 42
-- URLs đã được upload tự động từ Cloudinary
-- Số lượng ảnh: 9
-- =====================================================

DO $$
DECLARE
    v_restaurant_id INTEGER := 42;
    v_image_count INTEGER;
BEGIN
    -- Kiểm tra restaurant có tồn tại không
    IF NOT EXISTS (SELECT 1 FROM restaurant_profile WHERE restaurant_id = v_restaurant_id) THEN
        RAISE EXCEPTION 'Restaurant với ID % không tồn tại! Hãy kiểm tra lại restaurant_id.', v_restaurant_id;
    END IF;
    
    -- Kiểm tra xem đã có ảnh chưa
    SELECT COUNT(*) INTO v_image_count
    FROM restaurant_media
    WHERE restaurant_id = v_restaurant_id;
    
    IF v_image_count > 0 THEN
        RAISE NOTICE '⚠️  Đã có % ảnh cho nhà hàng này. Tiếp tục thêm ảnh mới...', v_image_count;
    END IF;
    
    -- COVER IMAGE (ảnh đầu tiên)
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (42, 'cover', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762345966/restaurants/42/media/cover/cover_0_1762224555.webp', NOW());
    
    -- GALLERY IMAGES (8 ảnh còn lại)
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (42, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762345968/restaurants/42/media/gallery/gallery_1_1762224560.webp', NOW());  -- Ảnh 2
    
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (42, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762345970/restaurants/42/media/gallery/gallery_2_1762224569.webp', NOW());  -- Ảnh 3
    
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (42, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762345971/restaurants/42/media/gallery/gallery_3_1762224573.webp', NOW());  -- Ảnh 4
    
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (42, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762345973/restaurants/42/media/gallery/gallery_4_1762224582.webp', NOW());  -- Ảnh 5
    
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (42, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762345974/restaurants/42/media/gallery/gallery_5_1762224577.webp', NOW());  -- Ảnh 6
    
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (42, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762345976/restaurants/42/media/gallery/gallery_6_1762224586.webp', NOW());  -- Ảnh 7
    
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (42, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762345978/restaurants/42/media/gallery/gallery_7_1762224589.webp', NOW());  -- Ảnh 8
    
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES (42, 'gallery', 'https://res.cloudinary.com/drcly5nge/image/upload/v1762345979/restaurants/42/media/gallery/gallery_8_1762224595.webp', NOW());  -- Ảnh 9
    
    -- Đếm tổng số ảnh sau khi insert
    SELECT COUNT(*) INTO v_image_count
    FROM restaurant_media
    WHERE restaurant_id = v_restaurant_id;
    
    RAISE NOTICE '========================================';
    RAISE NOTICE '✅ IMAGES ADDED SUCCESSFULLY!';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Restaurant ID: %', v_restaurant_id;
    RAISE NOTICE 'Total images after insert: %', v_image_count;
    RAISE NOTICE '========================================';
    
END $$;

-- =====================================================
-- VERIFICATION - Kiểm tra ảnh đã được thêm
-- =====================================================
SELECT 
    rm.media_id,
    rm.type,
    rm.url,
    r.restaurant_name
FROM restaurant_media rm
JOIN restaurant_profile r ON rm.restaurant_id = r.restaurant_id
WHERE r.restaurant_id = 42
ORDER BY 
    CASE rm.type 
        WHEN 'cover' THEN 1 
        WHEN 'gallery' THEN 2 
        ELSE 3 
    END,
    rm.media_id;
