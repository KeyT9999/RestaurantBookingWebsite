-- Check existing users
SELECT id, username, email, role, full_name FROM users;

-- Check existing customers
SELECT customer_id, user_id FROM customer;

-- Check existing bookings
SELECT booking_id, customer_id, status, created_at FROM booking ORDER BY booking_id DESC LIMIT 10;
