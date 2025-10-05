

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username        VARCHAR(100) NOT NULL UNIQUE,
    email           VARCHAR(255) UNIQUE,
    password        VARCHAR(255) NOT NULL,
    role            VARCHAR(20) NOT NULL CHECK (role IN ('admin','customer','restaurant_owner')),

    full_name       VARCHAR(255),
    phone_number    VARCHAR(20),
    address         VARCHAR(500),

    profile_image_url      VARCHAR(500),
    google_id              VARCHAR(255),

    email_verified         BOOLEAN DEFAULT FALSE,
    email_verification_token VARCHAR(255),

    password_reset_token   VARCHAR(255),
    password_reset_token_expiry TIMESTAMPTZ,

    last_login      TIMESTAMPTZ,
    active          BOOLEAN DEFAULT TRUE,

    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);


-- 1) OWNER & CUSTOMER (UUID, tham chiếu users.id (UUID))
CREATE TABLE IF NOT EXISTS restaurant_owner (
    owner_id    UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id     UUID UNIQUE NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_restaurant_owner_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS customer (
    customer_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id     UUID UNIQUE NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_customer_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 2) RESTAURANT PROFILE (owner_id phải là UUID)
CREATE TABLE IF NOT EXISTS restaurant_profile (
    restaurant_id    INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    owner_id         UUID NOT NULL,
    restaurant_name  VARCHAR(255) NOT NULL,
    cuisine_type     VARCHAR(100),
    opening_hours    VARCHAR(100),
    average_price    NUMERIC(18,2),
    website_url      VARCHAR(255),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_restaurant_profile_owner
        FOREIGN KEY (owner_id) REFERENCES restaurant_owner(owner_id) ON DELETE CASCADE
);

-- 3) BOOKING & các bảng tham chiếu CUSTOMER (đều phải là UUID)
CREATE TABLE IF NOT EXISTS booking (
    booking_id        INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id       UUID NOT NULL,
    booking_time      TIMESTAMPTZ NOT NULL,
    number_of_guests  INTEGER NOT NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'pending',
    deposit_amount    NUMERIC(18,2) NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_booking_customer
        FOREIGN KEY (customer_id) REFERENCES customer(customer_id)
);

ALTER TABLE booking
ADD COLUMN IF NOT EXISTS restaurant_id INTEGER REFERENCES restaurant_profile(restaurant_id) ON DELETE CASCADE,
ADD COLUMN IF NOT EXISTS note TEXT;


-- các bảng khác giữ nguyên kiểu INTEGER cho khóa tới restaurant_profile/*
CREATE TABLE IF NOT EXISTS restaurant_table (
    table_id      INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    restaurant_id INTEGER NOT NULL REFERENCES restaurant_profile(restaurant_id) ON DELETE CASCADE,
    table_name    VARCHAR(100) NOT NULL,
    capacity      INTEGER NOT NULL,
    table_image   VARCHAR(100) NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'available',
    depositamount NUMERIC(18,2) NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS booking_table (
    booking_table_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    booking_id       INTEGER NOT NULL REFERENCES booking(booking_id) ON DELETE CASCADE,
    table_id         INTEGER NOT NULL REFERENCES restaurant_table(table_id) ON DELETE CASCADE,
    assigned_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS dish (
    dish_id       INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    restaurant_id INTEGER NOT NULL REFERENCES restaurant_profile(restaurant_id) ON DELETE CASCADE,
    name          VARCHAR(255) NOT NULL,
    description   TEXT,
    price         NUMERIC(18,2) NOT NULL,
    category      VARCHAR(100),
    status        VARCHAR(20) NOT NULL DEFAULT 'available'
);

CREATE TABLE IF NOT EXISTS booking_dish (
    booking_dish_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    booking_id      INTEGER NOT NULL REFERENCES booking(booking_id) ON DELETE CASCADE,
    dish_id         INTEGER NOT NULL REFERENCES dish(dish_id),
    quantity        INTEGER NOT NULL DEFAULT 1,
    price           NUMERIC(18,2) NOT NULL
);

-- 4) VOUCHER/NOTIFICATION (tham chiếu users.id -> UUID)
CREATE TABLE IF NOT EXISTS voucher (
    voucher_id      INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code            VARCHAR(50) UNIQUE NOT NULL,
    description     VARCHAR(255),
    discount_type   VARCHAR(20) CHECK (discount_type IN ('percent','fixed')),
    discount_value  NUMERIC(18,2),
    start_date      DATE,
    end_date        DATE,
    usage_limit     INTEGER NOT NULL DEFAULT 1,
    created_by_user UUID NOT NULL REFERENCES users(id),
    restaurant_id   INTEGER REFERENCES restaurant_profile(restaurant_id),
    status          VARCHAR(20) NOT NULL DEFAULT 'active',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS customer_voucher (
    customer_voucher_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id         UUID NOT NULL REFERENCES customer(customer_id),
    voucher_id          INTEGER NOT NULL REFERENCES voucher(voucher_id),
    is_used             BOOLEAN NOT NULL DEFAULT FALSE,
    assigned_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    used_at             TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS payment (
    payment_id     INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id    UUID NOT NULL REFERENCES customer(customer_id),
    booking_id     INTEGER NOT NULL REFERENCES booking(booking_id),
    amount         NUMERIC(18,2) NOT NULL,
    payment_method VARCHAR(50) CHECK (payment_method IN ('cash','card','momo','zalopay')),
    status         VARCHAR(20) NOT NULL DEFAULT 'pending',
    voucher_id     INTEGER REFERENCES voucher(voucher_id),
    paid_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE booking
ADD COLUMN IF NOT EXISTS restaurant_id INTEGER REFERENCES restaurant_profile(restaurant_id) ON DELETE CASCADE,
ADD COLUMN IF NOT EXISTS note TEXT;

-- 5) REVIEW/INTERACTION (tham chiếu customer UUID, owner UUID)
CREATE TABLE IF NOT EXISTS review (
    review_id     INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id   UUID NOT NULL REFERENCES customer(customer_id),
    restaurant_id INTEGER NOT NULL REFERENCES restaurant_profile(restaurant_id),
    rating        INTEGER CHECK (rating BETWEEN 1 AND 5),
    comment       TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS customer_favorite (
    favorite_id   INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id   UUID NOT NULL REFERENCES customer(customer_id),
    restaurant_id INTEGER NOT NULL REFERENCES restaurant_profile(restaurant_id),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS notification (
    notification_id    INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    recipient_user_id  UUID NOT NULL REFERENCES users(id),
    type               VARCHAR(50) NOT NULL,
    content            TEXT,
    status             VARCHAR(20) NOT NULL DEFAULT 'pending',
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS message (
    message_id   INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id  UUID NOT NULL REFERENCES customer(customer_id) ON DELETE CASCADE,
    owner_id     UUID NOT NULL REFERENCES restaurant_owner(owner_id) ON DELETE NO ACTION,
    content      TEXT NOT NULL,
    sent_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS waitlist (
    waitlist_id   INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id   UUID NOT NULL REFERENCES customer(customer_id),
    restaurant_id INTEGER NOT NULL REFERENCES restaurant_profile(restaurant_id),
    party_size    INTEGER NOT NULL,
    join_time     TIMESTAMPTZ NOT NULL DEFAULT now(),
    status        VARCHAR(50) NOT NULL DEFAULT 'waiting'
);
-- Media cho nhà hàng
CREATE TABLE IF NOT EXISTS restaurant_media (
  media_id      INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  restaurant_id INTEGER NOT NULL
      REFERENCES restaurant_profile(restaurant_id) ON DELETE CASCADE,
  type          VARCHAR(50) NOT NULL,   -- logo | cover | table_layout
  url           VARCHAR(500) NOT NULL,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- (Tùy chọn) Bảng remember-me persistent (Spring Security)
CREATE TABLE IF NOT EXISTS persistent_logins (
  username  VARCHAR(64) NOT NULL,
  series    VARCHAR(64) PRIMARY KEY,
  token     VARCHAR(64) NOT NULL,
  last_used TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS restaurant_service (
    service_id      INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    restaurant_id   INTEGER NOT NULL REFERENCES restaurant_profile(restaurant_id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    category        VARCHAR(100),              -- Ví dụ: buffet, đồ uống, trang trí, VIP, …
    description     TEXT,
    price           NUMERIC(18,2) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'available',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS booking_service (
    booking_service_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    booking_id         INTEGER NOT NULL REFERENCES booking(booking_id) ON DELETE CASCADE,
    service_id         INTEGER NOT NULL REFERENCES restaurant_service(service_id) ON DELETE CASCADE,
    quantity           INTEGER NOT NULL DEFAULT 1,
    price              NUMERIC(18,2) NOT NULL,   -- lưu giá lúc booking để tránh thay đổi giá sau này
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);


-- =====================================================
-- ADD MOMO PAYMENT FIELDS TO PAYMENT TABLE
-- =====================================================
-- Migration script to add MoMo payment integration fields
-- Run this after the main database schema is created

-- Add MoMo specific fields to payment table
ALTER TABLE payment ADD COLUMN IF NOT EXISTS momo_order_id VARCHAR(64);
ALTER TABLE payment ADD COLUMN IF NOT EXISTS momo_request_id VARCHAR(64);
ALTER TABLE payment ADD COLUMN IF NOT EXISTS momo_trans_id VARCHAR(64);
ALTER TABLE payment ADD COLUMN IF NOT EXISTS momo_result_code VARCHAR(10);
ALTER TABLE payment ADD COLUMN IF NOT EXISTS momo_message VARCHAR(255);
ALTER TABLE payment ADD COLUMN IF NOT EXISTS pay_url VARCHAR(500);
ALTER TABLE payment ADD COLUMN IF NOT EXISTS ipn_raw JSONB;
ALTER TABLE payment ADD COLUMN IF NOT EXISTS redirect_raw JSONB;
ALTER TABLE payment ADD COLUMN IF NOT EXISTS refunded_at TIMESTAMPTZ;

-- Add payment type field
ALTER TABLE payment ADD COLUMN IF NOT EXISTS payment_type VARCHAR(20) DEFAULT 'DEPOSIT';

-- Add indexes for better performance
CREATE INDEX IF NOT EXISTS idx_payment_momo_order_id ON payment(momo_order_id);
CREATE INDEX IF NOT EXISTS idx_payment_momo_request_id ON payment(momo_request_id);
CREATE INDEX IF NOT EXISTS idx_payment_status ON payment(status);
CREATE INDEX IF NOT EXISTS idx_payment_method ON payment(payment_method);

-- Add comments for documentation
COMMENT ON COLUMN payment.momo_order_id IS 'MoMo order ID (unique, max 64 chars)';
COMMENT ON COLUMN payment.momo_request_id IS 'MoMo request ID (unique per request)';
COMMENT ON COLUMN payment.momo_trans_id IS 'MoMo transaction ID (from IPN)';
COMMENT ON COLUMN payment.momo_result_code IS 'MoMo result code (0 = success)';
COMMENT ON COLUMN payment.momo_message IS 'MoMo response message';
COMMENT ON COLUMN payment.pay_url IS 'MoMo payment URL for redirect';
COMMENT ON COLUMN payment.ipn_raw IS 'Raw IPN payload from MoMo (JSON)';
COMMENT ON COLUMN payment.redirect_raw IS 'Raw redirect data from MoMo (JSON)';
COMMENT ON COLUMN payment.refunded_at IS 'Timestamp when payment was refunded';
COMMENT ON COLUMN payment.payment_type IS 'Payment type: DEPOSIT or FULL_PAYMENT';

-- Update payment_method constraint to include new methods
ALTER TABLE payment DROP CONSTRAINT IF EXISTS payment_payment_method_check;
ALTER TABLE payment ADD CONSTRAINT payment_payment_method_check 
    CHECK (payment_method IN ('cash', 'momo'));

-- Update status constraint to include processing status
ALTER TABLE payment DROP CONSTRAINT IF EXISTS payment_status_check;
ALTER TABLE payment ADD CONSTRAINT payment_status_check 
    CHECK (status IN ('pending','processing','completed','failed','refunded','cancelled'));

-- Add payment type constraint
ALTER TABLE payment DROP CONSTRAINT IF EXISTS payment_type_check;
ALTER TABLE payment ADD CONSTRAINT payment_type_check 
    CHECK (payment_type IN ('DEPOSIT','FULL_PAYMENT'));

	-- Xem cấu trúc bảng



