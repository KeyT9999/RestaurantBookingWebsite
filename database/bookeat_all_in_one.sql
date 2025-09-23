-- =========================
-- BOOKEAT ALL-IN-ONE DATABASE SETUP
-- PostgreSQL compatible with Spring Boot 3 + JPA
-- Compatible with Render deployment
-- =========================

BEGIN;

-- =========================
-- EXTENSIONS
-- =========================
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- =========================
-- ENUMS
-- =========================
DO $$ BEGIN
    CREATE TYPE user_role AS ENUM ('CUSTOMER', 'RESTAURANT', 'ADMIN');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE booking_status AS ENUM ('PENDING', 'CONFIRMED', 'CANCELLED', 'NO_SHOW');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- =========================
-- DROP EXISTING TABLES (IDEMPOTENT)
-- =========================
DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS dining_tables CASCADE;
DROP TABLE IF EXISTS restaurants CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS email_verification_tokens CASCADE;
DROP TABLE IF EXISTS password_reset_tokens CASCADE;
DROP TABLE IF EXISTS oauth_accounts CASCADE;

-- =========================
-- CORE TABLES
-- =========================

-- Users table (Spring Boot User entity compatible)
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    phone_number VARCHAR(20),
    address VARCHAR(255),
    role user_role NOT NULL DEFAULT 'CUSTOMER',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    email_verification_token VARCHAR(255),
    password_reset_token VARCHAR(255),
    password_reset_token_expiry TIMESTAMPTZ,
    google_id VARCHAR(100),
    profile_image_url VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_login TIMESTAMPTZ
);

COMMENT ON TABLE users IS 'User accounts for customers, restaurants, and admins';
COMMENT ON COLUMN users.role IS 'User role: CUSTOMER, RESTAURANT, or ADMIN';
COMMENT ON COLUMN users.email_verified IS 'Whether email has been verified';
COMMENT ON COLUMN users.google_id IS 'Google OAuth ID for social login';

-- Restaurants table (Spring Boot Restaurant entity compatible)
CREATE TABLE restaurants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    address VARCHAR(255),
    phone VARCHAR(20),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE restaurants IS 'Restaurant information';
COMMENT ON COLUMN restaurants.name IS 'Restaurant name';
COMMENT ON COLUMN restaurants.description IS 'Restaurant description';

-- Dining tables table (Spring Boot DiningTable entity compatible)
CREATE TABLE dining_tables (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id UUID NOT NULL,
    name VARCHAR(50) NOT NULL,
    capacity INTEGER NOT NULL CHECK (capacity >= 1 AND capacity <= 20),
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_table_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
        ON DELETE CASCADE
);

COMMENT ON TABLE dining_tables IS 'Dining tables for each restaurant';
COMMENT ON COLUMN dining_tables.capacity IS 'Maximum number of guests';

-- Bookings table (Spring Boot Booking entity compatible)
CREATE TABLE bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    restaurant_id UUID NOT NULL,
    table_id UUID, -- Optional, nullable
    guest_count INTEGER NOT NULL CHECK (guest_count >= 1 AND guest_count <= 20),
    booking_time TIMESTAMPTZ NOT NULL,
    deposit_amount DECIMAL(10,2) DEFAULT 0 CHECK (deposit_amount >= 0),
    status booking_status NOT NULL DEFAULT 'PENDING',
    note VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_booking_customer FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_table FOREIGN KEY (table_id) REFERENCES dining_tables(id) ON DELETE SET NULL
);

COMMENT ON TABLE bookings IS 'Restaurant reservations';
COMMENT ON COLUMN bookings.status IS 'Booking status: PENDING, CONFIRMED, CANCELLED, NO_SHOW';
COMMENT ON COLUMN bookings.table_id IS 'Optional specific table assignment';

-- Email verification tokens
CREATE TABLE email_verification_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_verification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

COMMENT ON TABLE email_verification_tokens IS 'Email verification tokens for user registration';

-- Password reset tokens
CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_reset_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

COMMENT ON TABLE password_reset_tokens IS 'Password reset tokens for forgot password';

-- OAuth accounts
CREATE TABLE oauth_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    provider VARCHAR(50) NOT NULL,
    provider_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_oauth_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_oauth_provider UNIQUE (provider, provider_id)
);

COMMENT ON TABLE oauth_accounts IS 'OAuth provider accounts linked to users';

-- =========================
-- INDEXES FOR PERFORMANCE
-- =========================
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_dining_tables_restaurant ON dining_tables(restaurant_id);
CREATE INDEX idx_bookings_restaurant_time ON bookings(restaurant_id, booking_time);
CREATE INDEX idx_bookings_table_time ON bookings(table_id, booking_time);
CREATE INDEX idx_bookings_customer ON bookings(customer_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_email_tokens_token ON email_verification_tokens(token);
CREATE INDEX idx_password_tokens_token ON password_reset_tokens(token);
CREATE INDEX idx_oauth_provider ON oauth_accounts(provider, provider_id);

-- =========================
-- EXCLUSION CONSTRAINT FOR BOOKING OVERLAPS
-- =========================
ALTER TABLE bookings
ADD CONSTRAINT bookings_no_overlap
EXCLUDE USING gist (
    table_id WITH =,
    tstzrange(booking_time, booking_time + INTERVAL '2 hours', '[)') WITH &&
)
WHERE (status IN ('PENDING','CONFIRMED') AND table_id IS NOT NULL)
DEFERRABLE INITIALLY IMMEDIATE;

COMMENT ON CONSTRAINT bookings_no_overlap ON bookings IS 'Prevents overlapping bookings for the same table';

-- =========================
-- TRIGGER FUNCTION FOR UPDATED_AT
-- =========================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =========================
-- TRIGGERS
-- =========================
CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON users 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_restaurants_updated_at 
    BEFORE UPDATE ON restaurants 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_dining_tables_updated_at 
    BEFORE UPDATE ON dining_tables 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_bookings_updated_at 
    BEFORE UPDATE ON bookings 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =========================
-- SEED DATA
-- =========================

-- Insert sample users
INSERT INTO users (id, username, email, password, full_name, role, email_verified) VALUES
('11111111-1111-1111-1111-111111111111', 'admin', 'admin@bookeat.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyVhQrF4n1yJxw5f8v2K9xK9xK9', 'System Administrator', 'ADMIN', TRUE),
('22222222-2222-2222-2222-222222222222', 'customer', 'customer@bookeat.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyVhQrF4n1yJxw5f8v2K9xK9xK9', 'Demo Customer', 'CUSTOMER', TRUE),
('33333333-3333-3333-3333-333333333333', 'restaurant', 'restaurant@bookeat.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyVhQrF4n1yJxw5f8v2K9xK9xK9', 'Demo Restaurant Owner', 'RESTAURANT', TRUE);

-- Insert sample restaurants
INSERT INTO restaurants (id, name, description, address, phone) VALUES
('550e8400-e29b-41d4-a716-446655440000', 'Nhà hàng Sài Gòn', 'Nhà hàng phục vụ các món ăn truyền thống Việt Nam với không gian ấm cúng', '123 Nguyễn Huệ, Quận 1, TP.HCM', '028-1234-5678'),
('550e8400-e29b-41d4-a716-446655440001', 'Golden Dragon', 'Nhà hàng Trung Hoa cao cấp với các món ăn đặc sắc', '456 Lê Lợi, Quận 1, TP.HCM', '028-9876-5432'),
('550e8400-e29b-41d4-a716-446655440002', 'Bistro Français', 'Nhà hàng Pháp sang trọng với phong cách châu Âu', '789 Đồng Khởi, Quận 1, TP.HCM', '028-1111-2222');

-- Insert sample dining tables for Restaurant 1
INSERT INTO dining_tables (id, restaurant_id, name, capacity, description) VALUES
('650e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440000', 'Bàn A1', 2, 'Bàn 2 người gần cửa sổ'),
('650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440000', 'Bàn A2', 4, 'Bàn 4 người ở khu vực chính'),
('650e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440000', 'Bàn A3', 6, 'Bàn 6 người phù hợp cho gia đình'),
('650e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440000', 'Bàn VIP', 8, 'Bàn VIP riêng tư');

-- Insert sample dining tables for Restaurant 2
INSERT INTO dining_tables (id, restaurant_id, name, capacity, description) VALUES
('650e8400-e29b-41d4-a716-446655440010', '550e8400-e29b-41d4-a716-446655440001', 'Dragon 1', 4, 'Bàn tròn truyền thống'),
('650e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440001', 'Dragon 2', 6, 'Bàn tròn lớn'),
('650e8400-e29b-41d4-a716-446655440012', '550e8400-e29b-41d4-a716-446655440001', 'Dragon 3', 8, 'Bàn tròn cho nhóm đông'),
('650e8400-e29b-41d4-a716-446655440013', '550e8400-e29b-41d4-a716-446655440001', 'Private Room', 12, 'Phòng riêng cao cấp');

-- Insert sample dining tables for Restaurant 3
INSERT INTO dining_tables (id, restaurant_id, name, capacity, description) VALUES
('650e8400-e29b-41d4-a716-446655440020', '550e8400-e29b-41d4-a716-446655440002', 'Table 1', 2, 'Intimate table for two'),
('650e8400-e29b-41d4-a716-446655440021', '550e8400-e29b-41d4-a716-446655440002', 'Table 2', 4, 'Standard table'),
('650e8400-e29b-41d4-a716-446655440022', '550e8400-e29b-41d4-a716-446655440002', 'Table 3', 6, 'Family table'),
('650e8400-e29b-41d4-a716-446655440023', '550e8400-e29b-41d4-a716-446655440002', 'Terrace', 4, 'Outdoor terrace seating');

-- Insert sample bookings
INSERT INTO bookings (id, customer_id, restaurant_id, table_id, guest_count, booking_time, status, note) VALUES
('770e8400-e29b-41d4-a716-446655440000', '22222222-2222-2222-2222-222222222222', '550e8400-e29b-41d4-a716-446655440000', '650e8400-e29b-41d4-a716-446655440001', 4, NOW() + INTERVAL '1 day', 'CONFIRMED', 'Birthday dinner'),
('770e8400-e29b-41d4-a716-446655440001', '22222222-2222-2222-2222-222222222222', '550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440010', 2, NOW() + INTERVAL '2 days', 'PENDING', 'Anniversary dinner');

COMMIT;

-- =========================
-- MANUAL USER CREATION (Run separately if needed)
-- =========================
/*
-- Uncomment and run separately if you need to create application user
-- This section is commented out because it requires superuser privileges

-- Create application user (run as postgres superuser)
CREATE USER bookeat_user WITH PASSWORD 'your_secure_password';

-- Grant permissions to application user
GRANT CONNECT ON DATABASE bookeat_db TO bookeat_user;
GRANT USAGE ON SCHEMA public TO bookeat_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO bookeat_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO bookeat_user;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO bookeat_user;

-- Grant permissions for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO bookeat_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO bookeat_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT EXECUTE ON FUNCTIONS TO bookeat_user;
*/
