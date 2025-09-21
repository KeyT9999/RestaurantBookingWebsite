-- =========================
-- RESTAURANT BOOKING DATABASE - FIXED VERSION
-- Compatible with Spring Boot Entities
-- =========================

CREATE DATABASE restaurant_db;
-- \c restaurant_db;

-- =========================
-- BẢNG RESTAURANTS
-- =========================
CREATE TABLE restaurants (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    address     VARCHAR(255),
    phone       VARCHAR(20),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =========================
-- BẢNG DINING_TABLES
-- =========================
CREATE TABLE dining_tables (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id UUID NOT NULL,
    name          VARCHAR(50) NOT NULL,
    capacity      INTEGER NOT NULL,
    description   VARCHAR(255),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_table_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_dining_tables_restaurant ON dining_tables(restaurant_id);

-- =========================
-- BẢNG BOOKINGS
-- =========================
CREATE TABLE bookings (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id    UUID NOT NULL,
    restaurant_id  UUID NOT NULL,
    table_id       UUID,  -- Optional, có thể null
    guest_count    INTEGER NOT NULL CHECK (guest_count >= 1 AND guest_count <= 20),
    booking_time   TIMESTAMPTZ NOT NULL,
    deposit_amount DECIMAL(10,2) DEFAULT 0 CHECK (deposit_amount >= 0),
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING' 
                   CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'NO_SHOW')),
    note           VARCHAR(500),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_booking_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_table      FOREIGN KEY (table_id)      REFERENCES dining_tables(id) ON DELETE SET NULL
);

CREATE INDEX idx_bookings_restaurant_time ON bookings(restaurant_id, booking_time);
CREATE INDEX idx_bookings_table_time      ON bookings(table_id, booking_time);
CREATE INDEX idx_bookings_customer        ON bookings(customer_id);

-- =========================
-- INSERT SAMPLE DATA
-- =========================

-- Insert sample restaurants
INSERT INTO restaurants (id, name, description, address, phone) VALUES
('550e8400-e29b-41d4-a716-446655440000', 'Nhà hàng Sài Gòn', 'Nhà hàng phục vụ các món ăn truyền thống Việt Nam với không gian ấm cúng', '123 Nguyễn Huệ, Quận 1, TP.HCM', '028-1234-5678'),
('550e8400-e29b-41d4-a716-446655440001', 'Golden Dragon', 'Nhà hàng Trung Hoa cao cấp với các món ăn đặc sắc', '456 Lê Lợi, Quận 1, TP.HCM', '028-9876-5432'),
('550e8400-e29b-41d4-a716-446655440002', 'Bistro Français', 'Nhà hàng Pháp sang trọng với phong cách châu Âu', '789 Đồng Khởi, Quận 1, TP.HCM', '028-1111-2222');

-- Insert sample tables for Restaurant 1
INSERT INTO dining_tables (id, restaurant_id, name, capacity, description) VALUES
('650e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440000', 'Bàn A1', 2, 'Bàn 2 người gần cửa sổ'),
('650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440000', 'Bàn A2', 4, 'Bàn 4 người ở khu vực chính'),
('650e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440000', 'Bàn A3', 6, 'Bàn 6 người phù hợp cho gia đình'),
('650e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440000', 'Bàn VIP', 8, 'Bàn VIP riêng tư');

-- Insert sample tables for Restaurant 2
INSERT INTO dining_tables (id, restaurant_id, name, capacity, description) VALUES
('650e8400-e29b-41d4-a716-446655440010', '550e8400-e29b-41d4-a716-446655440001', 'Dragon 1', 4, 'Bàn tròn truyền thống'),
('650e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440001', 'Dragon 2', 6, 'Bàn tròn lớn'),
('650e8400-e29b-41d4-a716-446655440012', '550e8400-e29b-41d4-a716-446655440001', 'Dragon 3', 8, 'Bàn tròn cho nhóm đông'),
('650e8400-e29b-41d4-a716-446655440013', '550e8400-e29b-41d4-a716-446655440001', 'Private Room', 12, 'Phòng riêng cao cấp');

-- Insert sample tables for Restaurant 3
INSERT INTO dining_tables (id, restaurant_id, name, capacity, description) VALUES
('650e8400-e29b-41d4-a716-446655440020', '550e8400-e29b-41d4-a716-446655440002', 'Table 1', 2, 'Intimate table for two'),
('650e8400-e29b-41d4-a716-446655440021', '550e8400-e29b-41d4-a716-446655440002', 'Table 2', 4, 'Standard table'),
('650e8400-e29b-41d4-a716-446655440022', '550e8400-e29b-41d4-a716-446655440002', 'Table 3', 6, 'Family table'),
('650e8400-e29b-41d4-a716-446655440023', '550e8400-e29b-41d4-a716-446655440002', 'Terrace', 4, 'Outdoor terrace seating');

-- =========================
-- USEFUL QUERIES FOR TESTING
-- =========================

-- Check restaurants
-- SELECT * FROM restaurants;

-- Check tables by restaurant
-- SELECT r.name as restaurant_name, t.name as table_name, t.capacity 
-- FROM restaurants r 
-- JOIN dining_tables t ON r.id = t.restaurant_id 
-- ORDER BY r.name, t.name;

-- Check bookings with details
-- SELECT 
--     b.id as booking_id,
--     r.name as restaurant_name,
--     t.name as table_name,
--     b.guest_count,
--     b.booking_time,
--     b.status,
--     b.deposit_amount
-- FROM bookings b
-- JOIN restaurants r ON b.restaurant_id = r.id
-- LEFT JOIN dining_tables t ON b.table_id = t.id
-- ORDER BY b.booking_time DESC; 