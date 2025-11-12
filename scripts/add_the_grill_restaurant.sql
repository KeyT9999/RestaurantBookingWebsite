-- Create/attach THE GRILL restaurant with owner user 'Taiphan'
-- Works in pgAdmin 4 or psql

BEGIN;

-- Ensure pgcrypto for gen_random_uuid (safe if already exists)
DO $$
BEGIN
    PERFORM 1 FROM pg_extension WHERE extname = 'pgcrypto';
    IF NOT FOUND THEN
        EXECUTE 'CREATE EXTENSION IF NOT EXISTS pgcrypto';
    END IF;
END $$;

DO $$
DECLARE
    v_user_id UUID;
    v_owner_id UUID;
    v_restaurant_id INTEGER;
BEGIN
    -- 1) Resolve user Taiphan
    SELECT id INTO v_user_id FROM users WHERE username = 'Taiphan' AND deleted_at IS NULL LIMIT 1;
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'User Taiphan không tồn tại. Hãy tạo user này trước.';
    END IF;

    -- 2) Ensure restaurant_owner row
    SELECT owner_id INTO v_owner_id FROM restaurant_owner WHERE user_id = v_user_id LIMIT 1;
    IF v_owner_id IS NULL THEN
        INSERT INTO restaurant_owner (owner_id, user_id, owner_name, created_at, updated_at)
        VALUES (gen_random_uuid(), v_user_id, 'Taiphan', NOW(), NOW())
        RETURNING owner_id INTO v_owner_id;
    END IF;

    -- 3) Insert restaurant_profile (APPROVED)
    INSERT INTO restaurant_profile (
        owner_id, restaurant_name, address, phone, description,
        cuisine_type, opening_hours, average_price, website_url,
        hero_city, approval_status, approved_by, approved_at,
        terms_accepted, terms_accepted_at, contract_signed, contract_signed_at,
        created_at, updated_at
    ) VALUES (
        v_owner_id,
        'The Grill',
        '35 Trường Sa, Hòa Hải, Ngũ Hành Sơn, Đà Nẵng 550000',
        '0236 3988 999',
        'The Grill - nhà hàng bít tết cao cấp tại Sheraton Grand Danang Resort & Convention Center.',
        'Nhà hàng bít tết',
        '10:00-22:30',
        1000000,
        'thegrilldanang.com',
        'Đà Nẵng',
        'APPROVED',
        'system',
        NOW(),
        TRUE,
        NOW(),
        TRUE,
        NOW(),
        NOW(),
        NOW()
    ) RETURNING restaurant_id INTO v_restaurant_id;

    RAISE NOTICE 'The Grill created with restaurant_id=% (owner=%)', v_restaurant_id, v_owner_id;
END $$;

COMMIT;

-- Create or attach "The Grill" restaurant to existing owner user `Taiphan`
-- PostgreSQL script

-- 1) Resolve owner user by username
WITH owner_user AS (
    SELECT id AS user_id, username
    FROM users
    WHERE username = 'Taiphan'
),

-- 2) Ensure a restaurant_owner row exists for that user
ensured_owner AS (
    INSERT INTO restaurant_owner (owner_id, user_id, owner_name, created_at, updated_at)
    SELECT gen_random_uuid(), u.user_id, COALESCE(u.username, 'Owner'), NOW(), NOW()
    FROM owner_user u
    WHERE NOT EXISTS (
        SELECT 1 FROM restaurant_owner ro WHERE ro.user_id = u.user_id
    )
    RETURNING owner_id, user_id
),

existing_owner AS (
    SELECT ro.owner_id, ro.user_id
    FROM restaurant_owner ro
    JOIN owner_user u ON ro.user_id = u.user_id
),
final_owner AS (
    SELECT owner_id, user_id FROM ensured_owner
    UNION ALL
    SELECT owner_id, user_id FROM existing_owner
    LIMIT 1
),

-- 3) Insert restaurant profile
inserted_restaurant AS (
    INSERT INTO restaurant_profile (
        owner_id,
        restaurant_name,
        address,
        phone,
        description,
        cuisine_type,
        opening_hours,
        average_price,
        website_url,
        hero_city,
        approval_status,
        approved_by,
        approved_at,
        terms_accepted,
        terms_accepted_at,
        contract_signed,
        contract_signed_at,
        created_at,
        updated_at
    )
    SELECT
        fo.owner_id,
        'The Grill',
        '35 Trường Sa, Hòa Hải, Ngũ Hành Sơn, Đà Nẵng 550000',
        '0236 3988 999',
        'The Grill - nhà hàng bít tết cao cấp tại Sheraton Grand Danang Resort & Convention Center.',
        'Nhà hàng bít tết',
        '10:00-22:30',
        1000000, -- giá trung bình trên 1.000.000đ/người
        'thegrilldanang.com',
        'Đà Nẵng',
        'APPROVED',
        'system',
        NOW(),
        TRUE,
        NOW(),
        TRUE,
        NOW(),
        NOW(),
        NOW()
    FROM final_owner fo
    RETURNING restaurant_id
)

SELECT restaurant_id AS new_restaurant_id FROM inserted_restaurant;

-- After running: copy the returned restaurant_id and run the uploader:
--   python scripts/upload_the_grill.py <restaurant_id> "Media_update/The Grill - Hoà Hải, Ngũ Hành Sơn, Đà Nẵng"


