-- Simple setup for RestaurantOwner
-- First, create a RestaurantOwner record
INSERT INTO restaurant_owner (owner_id, user_id, created_at, updated_at)
VALUES (
    '550e8400-e29b-41d4-a716-446655440000',
    (SELECT id FROM users WHERE role = 'restaurant_owner' LIMIT 1),
    NOW(),
    NOW()
);

-- Link RestaurantProfile to RestaurantOwner
UPDATE restaurant_profile 
SET owner_id = '550e8400-e29b-41d4-a716-446655440000'
WHERE restaurant_id = 1;

-- Show the result
SELECT 
    u.username,
    u.role,
    ro.owner_id,
    rp.restaurant_name,
    rp.restaurant_id
FROM users u
JOIN restaurant_owner ro ON u.id = ro.user_id
LEFT JOIN restaurant_profile rp ON ro.owner_id = rp.owner_id
WHERE u.role = 'restaurant_owner';
