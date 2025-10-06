-- Bước 1: Tạo chat_room table
CREATE TABLE IF NOT EXISTS chat_room (
    room_id VARCHAR(100) PRIMARY KEY,
    customer_id UUID REFERENCES customer(customer_id),
    restaurant_id INTEGER REFERENCES restaurant_profile(restaurant_id),
    admin_id UUID REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_message_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- Bước 2: Thêm columns vào message table
ALTER TABLE message ADD COLUMN IF NOT EXISTS room_id VARCHAR(100);
ALTER TABLE message ADD COLUMN IF NOT EXISTS sender_id UUID;
ALTER TABLE message ADD COLUMN IF NOT EXISTS message_type VARCHAR(20) DEFAULT 'TEXT';
ALTER TABLE message ADD COLUMN IF NOT EXISTS file_url VARCHAR(500);
ALTER TABLE message ADD COLUMN IF NOT EXISTS is_read BOOLEAN DEFAULT FALSE;

-- Bước 3: Tạo enum
CREATE TYPE message_type_enum AS ENUM ('TEXT', 'IMAGE', 'FILE', 'SYSTEM');

-- Bước 4: Chuyển đổi message_type (QUAN TRỌNG!)
ALTER TABLE message ALTER COLUMN message_type DROP DEFAULT;
ALTER TABLE message ALTER COLUMN message_type TYPE message_type_enum USING message_type::message_type_enum;
ALTER TABLE message ALTER COLUMN message_type SET DEFAULT 'TEXT';

-- Bước 5: Thêm foreign keys
ALTER TABLE message ADD CONSTRAINT fk_message_room 
    FOREIGN KEY (room_id) REFERENCES chat_room(room_id);
ALTER TABLE message ADD CONSTRAINT fk_message_sender 
    FOREIGN KEY (sender_id) REFERENCES users(id);

-- Bước 6: Thêm indexes
CREATE INDEX idx_message_room ON message(room_id);
CREATE INDEX idx_message_sender ON message(sender_id);
CREATE INDEX idx_message_sent_at ON message(sent_at);
CREATE INDEX idx_message_is_read ON message(is_read);