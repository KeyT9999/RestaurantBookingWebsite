-- Script SQL để tạo lại bảng users với đúng schema
-- =====================================================
-- RESTAURANT BOOKING PLATFORM - COMPLETE DATABASE SCHEMA
-- =====================================================
-- Cách 1: Xóa sạch dữ liệu trong bảng phụ thuộc trước
TRUNCATE TABLE restaurant_owner CASCADE;
TRUNCATE TABLE customer CASCADE;
TRUNCATE TABLE voucher CASCADE;
TRUNCATE TABLE notification CASCADE;
TRUNCATE TABLE users CASCADE;

-- Cách 2: Xóa trực tiếp dữ liệu trong users (tự động xóa các bảng con nhờ ON DELETE CASCADE)
DROP TABLE IF EXISTS users CASCADE;


-- Drop existing tables if they exist (for clean recreation)
DROP TABLE IF EXISTS persistent_logins CASCADE;
DROP TABLE IF EXISTS restaurant_media CASCADE;
DROP TABLE IF EXISTS waitlist CASCADE;
DROP TABLE IF EXISTS message CASCADE;
DROP TABLE IF EXISTS notification CASCADE;
DROP TABLE IF EXISTS customer_favorite CASCADE;
DROP TABLE IF EXISTS review CASCADE;
DROP TABLE IF EXISTS payment CASCADE;
DROP TABLE IF EXISTS customer_voucher CASCADE;
DROP TABLE IF EXISTS voucher CASCADE;
DROP TABLE IF EXISTS booking_dish CASCADE;
DROP TABLE IF EXISTS dish CASCADE;
DROP TABLE IF EXISTS booking_table CASCADE;
DROP TABLE IF EXISTS restaurant_table CASCADE;
DROP TABLE IF EXISTS booking CASCADE;
DROP TABLE IF EXISTS restaurant_profile CASCADE;
DROP TABLE IF EXISTS customer CASCADE;
DROP TABLE IF EXISTS restaurant_owner CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- =====================================================
-- 1. USERS TABLE (Core authentication table)
-- =====================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('admin','customer','restaurant_owner','ADMIN','CUSTOMER','RESTAURANT_OWNER')),
    
    -- Profile information
    full_name VARCHAR(255),
    phone_number VARCHAR(20),
    address VARCHAR(500),
    profile_image_url VARCHAR(500),
    
    -- OAuth2 integration
    google_id VARCHAR(255),
    
    -- Email verification
    email_verified BOOLEAN DEFAULT FALSE,
    email_verification_token VARCHAR(255),
    
    -- Password reset
    password_reset_token VARCHAR(255),
    password_reset_token_expiry TIMESTAMPTZ,
    
    -- Account status
    active BOOLEAN DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    
    -- Audit fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_login TIMESTAMPTZ
);

-- =====================================================
-- 2. RESTAURANT OWNER TABLE
-- =====================================================
CREATE TABLE restaurant_owner (
    owner_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID UNIQUE NOT NULL,
    owner_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    address VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_restaurant_owner_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =====================================================
-- 3. CUSTOMER TABLE
-- =====================================================
CREATE TABLE customer (
    customer_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID UNIQUE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    address VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_customer_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =====================================================
-- 4. RESTAURANT PROFILE TABLE
-- =====================================================
CREATE TABLE restaurant_profile (
    restaurant_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    owner_id UUID NOT NULL,
    restaurant_name VARCHAR(255) NOT NULL,
    address VARCHAR(500),
    phone VARCHAR(20),
    description TEXT,
    cuisine_type VARCHAR(100),
    opening_hours VARCHAR(100),
    average_price NUMERIC(18,2),
    website_url VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_restaurant_profile_owner
        FOREIGN KEY (owner_id) REFERENCES restaurant_owner(owner_id) ON DELETE CASCADE
);

-- =====================================================
-- 5. RESTAURANT TABLE TABLE
-- =====================================================
CREATE TABLE restaurant_table (
    table_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    restaurant_id INTEGER NOT NULL,
    table_name VARCHAR(100) NOT NULL,
    capacity INTEGER NOT NULL,
    table_image VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'available',
    CONSTRAINT fk_restaurant_table_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurant_profile(restaurant_id) ON DELETE CASCADE
);

-- =====================================================
-- 6. BOOKING TABLE
-- =====================================================
CREATE TABLE booking (
    booking_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id UUID NOT NULL,
    booking_time TIMESTAMPTZ NOT NULL,
    number_of_guests INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    deposit_amount NUMERIC(18,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_booking_customer
        FOREIGN KEY (customer_id) REFERENCES customer(customer_id)
);

-- =====================================================
-- 7. BOOKING TABLE ASSIGNMENT
-- =====================================================
CREATE TABLE booking_table (
    booking_table_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    booking_id INTEGER NOT NULL,
    table_id INTEGER NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_booking_table_booking
        FOREIGN KEY (booking_id) REFERENCES booking(booking_id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_table_table
        FOREIGN KEY (table_id) REFERENCES restaurant_table(table_id) ON DELETE CASCADE
);

-- =====================================================
-- 8. DISH TABLE
-- =====================================================
CREATE TABLE dish (
    dish_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    restaurant_id INTEGER NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(18,2) NOT NULL,
    category VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'available',
    CONSTRAINT fk_dish_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurant_profile(restaurant_id) ON DELETE CASCADE
);

-- =====================================================
-- 9. BOOKING DISH TABLE
-- =====================================================
CREATE TABLE booking_dish (
    booking_dish_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    booking_id INTEGER NOT NULL,
    dish_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    price NUMERIC(18,2) NOT NULL,
    CONSTRAINT fk_booking_dish_booking
        FOREIGN KEY (booking_id) REFERENCES booking(booking_id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_dish_dish
        FOREIGN KEY (dish_id) REFERENCES dish(dish_id)
);

-- =====================================================
-- 10. VOUCHER TABLE
-- =====================================================
CREATE TABLE voucher (
    voucher_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    discount_type VARCHAR(20) CHECK (discount_type IN ('percent','fixed')),
    discount_value NUMERIC(18,2),
    start_date DATE,
    end_date DATE,
    usage_limit INTEGER NOT NULL DEFAULT 1,
    created_by_user UUID NOT NULL,
    restaurant_id INTEGER,
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_voucher_user
        FOREIGN KEY (created_by_user) REFERENCES users(id),
    CONSTRAINT fk_voucher_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurant_profile(restaurant_id)
);

-- =====================================================
-- 11. CUSTOMER VOUCHER TABLE
-- =====================================================
CREATE TABLE customer_voucher (
    customer_voucher_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id UUID NOT NULL,
    voucher_id INTEGER NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    used_at TIMESTAMPTZ,
    CONSTRAINT fk_customer_voucher_customer
        FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    CONSTRAINT fk_customer_voucher_voucher
        FOREIGN KEY (voucher_id) REFERENCES voucher(voucher_id)
);

-- =====================================================
-- 12. PAYMENT TABLE
-- =====================================================
CREATE TABLE payment (
    payment_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id UUID NOT NULL,
    booking_id INTEGER NOT NULL,
    amount NUMERIC(18,2) NOT NULL,
    payment_method VARCHAR(50) CHECK (payment_method IN ('cash','card','momo','zalopay')),
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    voucher_id INTEGER,
    paid_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_payment_customer
        FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    CONSTRAINT fk_payment_booking
        FOREIGN KEY (booking_id) REFERENCES booking(booking_id),
    CONSTRAINT fk_payment_voucher
        FOREIGN KEY (voucher_id) REFERENCES voucher(voucher_id)
);

-- =====================================================
-- 13. REVIEW TABLE
-- =====================================================
CREATE TABLE review (
    review_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id UUID NOT NULL,
    restaurant_id INTEGER NOT NULL,
    rating INTEGER CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_review_customer
        FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    CONSTRAINT fk_review_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurant_profile(restaurant_id)
);

-- =====================================================
-- 14. CUSTOMER FAVORITE TABLE
-- =====================================================
CREATE TABLE customer_favorite (
    favorite_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id UUID NOT NULL,
    restaurant_id INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_customer_favorite_customer
        FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    CONSTRAINT fk_customer_favorite_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurant_profile(restaurant_id)
);

-- =====================================================
-- 15. NOTIFICATION TABLE
-- =====================================================
CREATE TABLE notification (
    notification_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    recipient_user_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    content TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_notification_user
        FOREIGN KEY (recipient_user_id) REFERENCES users(id)
);

-- =====================================================
-- 16. MESSAGE TABLE
-- =====================================================
CREATE TABLE message (
    message_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id UUID NOT NULL,
    owner_id UUID NOT NULL,
    content TEXT NOT NULL,
    sent_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_message_customer
        FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE,
    CONSTRAINT fk_message_owner
        FOREIGN KEY (owner_id) REFERENCES restaurant_owner(owner_id) ON DELETE NO ACTION
);

-- =====================================================
-- 17. WAITLIST TABLE
-- =====================================================
CREATE TABLE waitlist (
    waitlist_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id UUID NOT NULL,
    restaurant_id INTEGER NOT NULL,
    party_size INTEGER NOT NULL,
    join_time TIMESTAMPTZ NOT NULL DEFAULT now(),
    status VARCHAR(50) NOT NULL DEFAULT 'waiting',
    CONSTRAINT fk_waitlist_customer
        FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    CONSTRAINT fk_waitlist_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurant_profile(restaurant_id)
);

-- =====================================================
-- 18. RESTAURANT MEDIA TABLE
-- =====================================================
CREATE TABLE restaurant_media (
    media_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    restaurant_id INTEGER NOT NULL,
    type VARCHAR(50) NOT NULL,   -- logo | cover | table_layout
    url VARCHAR(500) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_restaurant_media_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurant_profile(restaurant_id) ON DELETE CASCADE
);

-- =====================================================
-- 19. PERSISTENT LOGINS TABLE (Spring Security)
-- =====================================================
CREATE TABLE persistent_logins (
    username VARCHAR(64) NOT NULL,
    series VARCHAR(64) PRIMARY KEY,
    token VARCHAR(64) NOT NULL,
    last_used TIMESTAMP NOT NULL
);

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_active ON users(active);
CREATE INDEX idx_users_deleted_at ON users(deleted_at);

CREATE INDEX idx_booking_customer_id ON booking(customer_id);
CREATE INDEX idx_booking_status ON booking(status);
CREATE INDEX idx_booking_time ON booking(booking_time);

CREATE INDEX idx_restaurant_owner_user_id ON restaurant_owner(user_id);
CREATE INDEX idx_customer_user_id ON customer(user_id);

-- =====================================================
-- SAMPLE DATA INSERTION
-- =====================================================

-- Insert sample users
INSERT INTO users (username, email, password, role, full_name, phone_number, email_verified, active) VALUES
('admin1', 'admin1@example.com', '$2a$10$J2o9sMXgIITozR5z/61n5OqaQF7dwUdNA4wbN0BvnxthclLs5vRae', 'admin', 'System Admin', '0868899104', true, true),
('res1', 'res1@example.com', '$2a$10$7wUDhHRAvlsRLkNwMogHluGU5t8IXt6ZzsIB2RP.d/K3FK7PahJn6', 'restaurant_owner', 'Restaurant Owner', '0868899104', true, true),
('customer1', 'customer1@example.com', '$2a$10$6PFcCALseylbBqV6iaDcPOLJhp4mZGGTD4Iwg6TXXDl913cximAUe', 'customer', 'Demo Customer', '0123456789', true, true);

-- Insert restaurant owner
INSERT INTO restaurant_owner (user_id, owner_name, phone, address) 
SELECT id, full_name, phone_number, 'FPT Plaza 1' FROM users WHERE username = 'res1';

-- Insert customer
INSERT INTO customer (user_id, full_name, phone, address) 
SELECT id, full_name, phone_number, 'FPT Plaza 1' FROM users WHERE username = 'customer1';

-- Insert restaurant profile
INSERT INTO restaurant_profile (owner_id, restaurant_name, address, phone, description, cuisine_type, opening_hours, average_price)
SELECT ro.owner_id, 'Sample Restaurant', 'FPT Plaza 1', '0868899104', 'A sample restaurant for testing', 'Vietnamese', '9:00-22:00', 150000
FROM restaurant_owner ro
JOIN users u ON ro.user_id = u.id
WHERE u.username = 'res1';

-- Insert sample tables
INSERT INTO restaurant_table (restaurant_id, table_name, capacity, table_image, status)
SELECT rp.restaurant_id, 'Table ' || generate_series(1, 5), 4, 'table_' || generate_series(1, 5) || '.jpg', 'available'
FROM restaurant_profile rp
JOIN restaurant_owner ro ON rp.owner_id = ro.owner_id
JOIN users u ON ro.user_id = u.id
WHERE u.username = 'res1';

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================

-- Check users table structure
SELECT column_name, data_type, is_nullable, column_default 
FROM information_schema.columns 
WHERE table_name = 'users' 
ORDER BY ordinal_position;

-- Check sample data
SELECT username, email, role, active, email_verified FROM users ORDER BY username;

-- Check constraints
SELECT constraint_name, constraint_type 
FROM information_schema.table_constraints 
WHERE table_name = 'users';