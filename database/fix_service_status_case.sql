-- Fix services status to use uppercase to match ServiceStatus enum
-- Update existing services for restaurant ID 16
UPDATE restaurant_service 
SET status = 'AVAILABLE' 
WHERE restaurant_id = 16 AND status = 'available';

-- Verify the update
SELECT service_id, restaurant_id, name, status 
FROM restaurant_service 
WHERE restaurant_id = 16;
