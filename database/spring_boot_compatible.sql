-- =========================
-- SPRING BOOT COMPATIBLE DATABASE SCHEMA
-- Khớp chính xác với entities: Restaurant, DiningTable, Booking
-- =========================

-- Bật extension UUID
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Drop existing tables
DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS dining_tables CASCADE; 
DROP TABLE IF EXISTS restaurants CASCADE;

-- Table: restaurants (khớp với Restaurant entity)
CREATE TABLE restaurants (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    address     VARCHAR(255),
    phone       VARCHAR(20),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Table: dining_tables (khớp với DiningTable entity)  
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

-- Table: bookings (khớp với Booking entity)
CREATE TABLE bookings (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id    UUID NOT NULL,  -- Sẽ là fixed UUID cho demo
    restaurant_id  UUID NOT NULL,
    table_id       UUID,  -- Optional (nullable)
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

-- Indexes for performance
CREATE INDEX idx_dining_tables_restaurant ON dining_tables(restaurant_id);
CREATE INDEX idx_bookings_restaurant_time ON bookings(restaurant_id, booking_time);
CREATE INDEX idx_bookings_table_time      ON bookings(table_id, booking_time);
CREATE INDEX idx_bookings_customer        ON bookings(customer_id);

-- Trigger function for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers
CREATE TRIGGER update_restaurants_updated_at BEFORE UPDATE ON restaurants 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    
CREATE TRIGGER update_dining_tables_updated_at BEFORE UPDATE ON dining_tables 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    
CREATE TRIGGER update_bookings_updated_at BEFORE UPDATE ON bookings 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =========================
-- INSERT SAMPLE DATA
-- =========================

-- Insert sample restaurants (same as DataInitializer)
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
-- VERIFICATION QUERIES
-- =========================

-- Check data
-- SELECT 'Restaurants' as table_name, COUNT(*) as count FROM restaurants
-- UNION ALL
-- SELECT 'Dining Tables' as table_name, COUNT(*) as count FROM dining_tables
-- UNION ALL  
-- SELECT 'Bookings' as table_name, COUNT(*) as count FROM bookings;

-- Check relationships
-- SELECT r.name as restaurant_name, t.name as table_name, t.capacity 
-- FROM restaurants r 
-- JOIN dining_tables t ON r.id = t.restaurant_id 
-- ORDER BY r.name, t.name; 