-- Insert sample tables for testing
-- First, check if restaurant exists
SELECT restaurant_id, restaurant_name FROM restaurant_profile WHERE restaurant_id = 1;

-- Insert sample tables if restaurant exists
INSERT INTO restaurant_table (restaurant_id, table_name, capacity, table_image, status, depositamount)
VALUES 
    (1, 'Table 1', 4, 'table1.jpg', 'available', 100000),
    (1, 'Table 2', 6, 'table2.jpg', 'available', 150000),
    (1, 'Table 3', 2, 'table3.jpg', 'available', 50000),
    (1, 'Table 4', 8, 'table4.jpg', 'available', 200000),
    (1, 'VIP Table', 10, 'vip_table.jpg', 'available', 500000);

-- Verify insertion
SELECT * FROM restaurant_table WHERE restaurant_id = 1;

-- Insert sample dishes for testing
INSERT INTO dish (restaurant_id, name, description, price, category, status)
VALUES 
    (1, 'Pho Bo', 'Traditional Vietnamese beef noodle soup', 80000, 'Main Course', 'available'),
    (1, 'Banh Mi', 'Vietnamese sandwich with meat and vegetables', 45000, 'Sandwich', 'available'),
    (1, 'Spring Rolls', 'Fresh Vietnamese spring rolls', 60000, 'Appetizer', 'available'),
    (1, 'Com Tam', 'Broken rice with grilled pork', 70000, 'Main Course', 'available'),
    (1, 'Ca Phe Sua Da', 'Vietnamese iced coffee with condensed milk', 25000, 'Beverage', 'available');

-- Verify dishes insertion
SELECT * FROM dish WHERE restaurant_id = 1;

-- Insert sample services for testing
INSERT INTO restaurant_service (restaurant_id, name, category, description, price, status)
VALUES 
    (1, 'Live Music', 'Entertainment', 'Live traditional Vietnamese music performance', 200000, 'available'),
    (1, 'Private Room', 'Venue', 'Private dining room for special occasions', 500000, 'available'),
    (1, 'Wine Service', 'Beverage', 'Professional wine service with sommelier', 300000, 'available'),
    (1, 'Cake Service', 'Dessert', 'Custom birthday cake service', 150000, 'available');

-- Verify services insertion
SELECT * FROM restaurant_service WHERE restaurant_id = 1;
