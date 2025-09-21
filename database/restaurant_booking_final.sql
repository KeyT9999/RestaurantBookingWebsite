-- =========================
-- RESTAURANT BOOKING DATABASE - FINAL VERSION
-- Khớp hoàn toàn với Spring Boot Entities
-- =========================

-- Bật hàm sinh UUID
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- User/Restaurant chung một bảng người dùng
DROP TABLE IF EXISTS customer_history      CASCADE;
DROP TABLE IF EXISTS restaurant_history    CASCADE;
DROP TABLE IF EXISTS restaurant_report     CASCADE;
DROP TABLE IF EXISTS notification          CASCADE;
DROP TABLE IF EXISTS customer_favorite     CASCADE;
DROP TABLE IF EXISTS customer_voucher      CASCADE;
DROP TABLE IF EXISTS voucher               CASCADE;
DROP TABLE IF EXISTS review                CASCADE;
DROP TABLE IF EXISTS booking               CASCADE;
DROP TABLE IF EXISTS dining_table          CASCADE;
DROP TABLE IF EXISTS restaurant_profile    CASCADE;
DROP TABLE IF EXISTS app_user              CASCADE;

CREATE TABLE app_user (
  user_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_type       VARCHAR(20) NOT NULL CHECK (user_type IN ('customer','restaurant','admin')),
  user_name       VARCHAR(100) NOT NULL,
  email           VARCHAR(100) NOT NULL UNIQUE,
  phone_number    VARCHAR(20),
  password_hash   VARCHAR(255) NOT NULL,
  address         VARCHAR(255),
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE restaurant_profile (
  restaurant_profile_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id         UUID NOT NULL REFERENCES app_user(user_id) ON DELETE CASCADE,
  restaurant_name VARCHAR(255) NOT NULL,
  description     TEXT,
  cuisine_type    VARCHAR(100),
  opening_hours   VARCHAR(100),
  average_price   NUMERIC(18,2),
  logo_url        VARCHAR(500),
  cover_image_url VARCHAR(500),
  website_url     VARCHAR(255),
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE dining_table (
  table_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  restaurant_id UUID NOT NULL REFERENCES app_user(user_id) ON DELETE CASCADE,
  table_name    VARCHAR(100) NOT NULL,
  capacity      INT NOT NULL,
  image_url     VARCHAR(255),
  status        VARCHAR(20) NOT NULL DEFAULT 'available'
);

CREATE TABLE booking (
  booking_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id    UUID NOT NULL REFERENCES app_user(user_id) ON DELETE CASCADE,
  restaurant_id  UUID NOT NULL REFERENCES app_user(user_id) ON DELETE CASCADE,
  table_id       UUID NOT NULL REFERENCES dining_table(table_id) ON DELETE RESTRICT,
  booking_time   TIMESTAMPTZ NOT NULL,
  guest_count    INT NOT NULL,
  status         VARCHAR(20) NOT NULL DEFAULT 'pending',
  deposit_amount NUMERIC(18,2) DEFAULT 0,
  note           VARCHAR(500),
  created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE review (
  review_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id   UUID NOT NULL REFERENCES app_user(user_id) ON DELETE CASCADE,
  restaurant_id UUID NOT NULL REFERENCES app_user(user_id) ON DELETE CASCADE,
  rating        INT CHECK (rating BETWEEN 1 AND 5),
  comment       TEXT,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE voucher (
  voucher_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  created_by     VARCHAR(50) NOT NULL, -- 'system' hoặc chuỗi restaurantId
  code           VARCHAR(50) NOT NULL UNIQUE,
  description    VARCHAR(255),
  discount_type  VARCHAR(20) CHECK (discount_type IN ('percent','fixed')),
  discount_value NUMERIC(18,2),
  start_date     DATE,
  end_date       DATE,
  status         VARCHAR(20) NOT NULL DEFAULT 'active'
);

CREATE TABLE customer_voucher (
  customer_voucher_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id         UUID NOT NULL REFERENCES app_user(user_id) ON DELETE CASCADE,
  voucher_id          UUID NOT NULL REFERENCES voucher(voucher_id) ON DELETE CASCADE,
  is_used             BOOLEAN DEFAULT FALSE,
  assigned_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  used_at             TIMESTAMPTZ,
  CONSTRAINT uq_cv_unique UNIQUE (customer_id, voucher_id)
);

CREATE TABLE customer_favorite (
  favorite_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id   UUID NOT NULL REFERENCES app_user(user_id) ON DELETE CASCADE,
  restaurant_id UUID NOT NULL REFERENCES app_user(user_id) ON DELETE CASCADE,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_cf_unique UNIQUE (customer_id, restaurant_id)
);

CREATE TABLE notification (
  notification_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  recipient_id    UUID NOT NULL REFERENCES app_user(user_id) ON DELETE CASCADE,
  type            VARCHAR(50) NOT NULL,
  content         TEXT,
  status          VARCHAR(20) NOT NULL DEFAULT 'pending',
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE restaurant_report (
  report_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  restaurant_id      UUID NOT NULL REFERENCES app_user(user_id) ON DELETE CASCADE,
  report_date        DATE NOT NULL,
  total_bookings     INT DEFAULT 0,
  total_revenue      NUMERIC(18,2) DEFAULT 0,
  cancelled_bookings INT DEFAULT 0,
  generated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_rr_unique UNIQUE (restaurant_id, report_date)
);

CREATE TABLE restaurant_history (
  history_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  restaurant_id UUID NOT NULL REFERENCES app_user(user_id) ON DELETE CASCADE,
  booking_id    UUID NOT NULL REFERENCES booking(booking_id) ON DELETE CASCADE,
  action        VARCHAR(50) NOT NULL,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE customer_history (
  history_id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id UUID NOT NULL REFERENCES app_user(user_id) ON DELETE CASCADE,
  booking_id  UUID NOT NULL REFERENCES booking(booking_id) ON DELETE CASCADE,
  action      VARCHAR(50) NOT NULL,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes hay dùng
CREATE INDEX idx_booking_restaurant_time ON booking (restaurant_id, booking_time);
CREATE INDEX idx_booking_table_time      ON booking (table_id, booking_time);
CREATE INDEX idx_review_restaurant       ON review (restaurant_id);
CREATE INDEX idx_notification_recipient  ON notification (recipient_id);

-- (Tùy chọn) trigger tự cập nhật updated_at
CREATE OR REPLACE FUNCTION touch_updated_at() RETURNS TRIGGER AS $$
BEGIN NEW.updated_at := NOW(); RETURN NEW; END; $$ LANGUAGE plpgsql;

CREATE TRIGGER trg_touch_app_user  BEFORE UPDATE ON app_user  FOR EACH ROW EXECUTE FUNCTION touch_updated_at();
CREATE TRIGGER trg_touch_booking   BEFORE UPDATE ON booking   FOR EACH ROW EXECUTE FUNCTION touch_updated_at();
CREATE TRIGGER trg_touch_rest_prof BEFORE UPDATE ON restaurant_profile FOR EACH ROW EXECUTE FUNCTION touch_updated_at();

-- Seed dữ liệu mẫu nhỏ để test nhanh
INSERT INTO app_user (user_type, user_name, email, password_hash) VALUES
('restaurant','Nhà hàng A','a@rest.vn','hash-a'),
('restaurant','Nhà hàng B','b@rest.vn','hash-b'),
('customer','Khách 1','c1@user.vn','hash-c1'),
('admin','Quản trị','admin@sys.vn','hash-admin');

INSERT INTO dining_table (restaurant_id, table_name, capacity)
SELECT user_id, 'Bàn 1', 4 FROM app_user WHERE user_type='restaurant' LIMIT 1; 