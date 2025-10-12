

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

CREATE TABLE IF NOT EXISTS review_report (
    report_id SERIAL PRIMARY KEY,
    review_id INTEGER REFERENCES review(review_id) ON DELETE SET NULL,
    restaurant_id INTEGER NOT NULL REFERENCES restaurant_profile(restaurant_id) ON DELETE CASCADE,
    owner_id UUID NOT NULL REFERENCES restaurant_owner(owner_id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reason_text TEXT NOT NULL,
    review_id_snapshot INTEGER,
    review_rating_snapshot INTEGER,
    review_comment_snapshot TEXT,
    review_created_at_snapshot TIMESTAMPTZ,
    customer_name_snapshot VARCHAR(255),
    resolution_message TEXT,
    resolved_at TIMESTAMPTZ,
    resolved_by_admin_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_review_report_status ON review_report(status);
CREATE INDEX IF NOT EXISTS idx_review_report_restaurant ON review_report(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_review_report_review ON review_report(review_id);

CREATE OR REPLACE FUNCTION update_review_report_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_review_report_updated_at ON review_report;
CREATE TRIGGER trg_review_report_updated_at
BEFORE UPDATE ON review_report
FOR EACH ROW
EXECUTE FUNCTION update_review_report_updated_at();

CREATE TABLE IF NOT EXISTS review_report_evidence (
    evidence_id SERIAL PRIMARY KEY,
    report_id INTEGER NOT NULL REFERENCES review_report(report_id) ON DELETE CASCADE,
    file_url VARCHAR(500) NOT NULL,
    content_type VARCHAR(100),
    file_size BIGINT,
    sort_order INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_review_report_evidence_report ON review_report_evidence(report_id);

CREATE OR REPLACE FUNCTION update_review_report_evidence_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_review_report_evidence_updated_at ON review_report_evidence;
CREATE TRIGGER trg_review_report_evidence_updated_at
BEFORE UPDATE ON review_report_evidence
FOR EACH ROW
EXECUTE FUNCTION update_review_report_evidence_updated_at();

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



BEGIN;

-- 1) Gỡ các cột riêng của MoMo (không còn dùng)
ALTER TABLE payment
  DROP COLUMN IF EXISTS momo_order_id,
  DROP COLUMN IF EXISTS momo_request_id,
  DROP COLUMN IF EXISTS momo_trans_id,
  DROP COLUMN IF EXISTS momo_result_code,
  DROP COLUMN IF EXISTS momo_message;

-- 2) Thêm các cột tùy chọn cho PayOS (nếu muốn lưu thêm thông tin)
ALTER TABLE payment
  ADD COLUMN IF NOT EXISTS payos_payment_link_id varchar(128),
  ADD COLUMN IF NOT EXISTS payos_checkout_url text,
  ADD COLUMN IF NOT EXISTS payos_code varchar(8),
  ADD COLUMN IF NOT EXISTS payos_desc text;

COMMIT;

ALTER TABLE payment RENAME COLUMN pay_url TO payos_checkout_url;




-- =====================================================
-- PAYOUT SYSTEM FOR RESTAURANT WITHDRAWAL
-- =====================================================
-- Migration script to add payout/withdrawal functionality
-- This allows restaurants to withdraw money from their balance

BEGIN;

-- =====================================================
-- 1. RESTAURANT BANK ACCOUNT TABLE
-- =====================================================
-- Stores bank account information for restaurants
CREATE TABLE IF NOT EXISTS restaurant_bank_account (
    account_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    restaurant_id INTEGER NOT NULL REFERENCES restaurant_profile(restaurant_id) ON DELETE CASCADE,
    
    -- Bank information
    bank_code VARCHAR(20) NOT NULL,           -- Mã ngân hàng (BIN code)
    bank_name VARCHAR(255),                   -- Tên ngân hàng
    account_number VARCHAR(50) NOT NULL,      -- Số tài khoản
    account_holder_name VARCHAR(255) NOT NULL, -- Tên chủ tài khoản
    
    -- Status
    is_verified BOOLEAN DEFAULT FALSE,        -- Đã xác minh qua PayOS chưa
    is_default BOOLEAN DEFAULT TRUE,          -- Tài khoản mặc định
    
    -- Metadata
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    
    -- Constraints
    CONSTRAINT unique_restaurant_account UNIQUE(restaurant_id, account_number)
);

CREATE INDEX idx_bank_account_restaurant ON restaurant_bank_account(restaurant_id);
COMMENT ON TABLE restaurant_bank_account IS 'Thông tin tài khoản ngân hàng của nhà hàng để rút tiền';

-- =====================================================
-- 2. WITHDRAWAL REQUEST TABLE
-- =====================================================
-- Stores withdrawal requests from restaurants
CREATE TABLE IF NOT EXISTS withdrawal_request (
    request_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    restaurant_id INTEGER NOT NULL REFERENCES restaurant_profile(restaurant_id) ON DELETE CASCADE,
    bank_account_id INTEGER NOT NULL REFERENCES restaurant_bank_account(account_id),
    
    -- Request information
    amount NUMERIC(18,2) NOT NULL CHECK (amount > 0),
    description TEXT,
    
    -- Status tracking
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' 
        CHECK (status IN ('PENDING','APPROVED','REJECTED','PROCESSING','SUCCEEDED','FAILED','CANCELLED')),
    
    -- Admin review
    reviewed_by_user_id UUID REFERENCES users(id),
    reviewed_at TIMESTAMPTZ,
    rejection_reason TEXT,
    admin_notes TEXT,
    
    -- Financial tracking
    commission_amount NUMERIC(18,2) DEFAULT 0,  -- Phí hoa hồng đã trừ
    net_amount NUMERIC(18,2),                   -- Số tiền thực nhận = amount - commission
    
    -- Metadata
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_withdrawal_restaurant ON withdrawal_request(restaurant_id);
CREATE INDEX idx_withdrawal_status ON withdrawal_request(status);
CREATE INDEX idx_withdrawal_created ON withdrawal_request(created_at DESC);
COMMENT ON TABLE withdrawal_request IS 'Yêu cầu rút tiền từ nhà hàng';

-- =====================================================
-- 3. PAYOUT TRANSACTION TABLE
-- =====================================================
-- Stores PayOS payout transactions
CREATE TABLE IF NOT EXISTS payout_transaction (
    transaction_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    withdrawal_request_id INTEGER NOT NULL REFERENCES withdrawal_request(request_id) ON DELETE CASCADE,
    
    -- PayOS identifiers
    payos_payout_id VARCHAR(128),             -- ID từ PayOS response
    payos_reference_id VARCHAR(128) UNIQUE NOT NULL,  -- referenceId gửi lên PayOS (idempotency)
    payos_transaction_id VARCHAR(128),        -- ID transaction từ PayOS
    
    -- Transaction details
    amount NUMERIC(18,2) NOT NULL,
    to_bank_code VARCHAR(20) NOT NULL,
    to_account_number VARCHAR(50) NOT NULL,
    to_account_name VARCHAR(255),
    description TEXT,
    category VARCHAR(50) DEFAULT 'restaurant_withdrawal',
    
    -- Status & result
    state VARCHAR(20) CHECK (state IN ('SUCCEEDED','FAILED','PROCESSING','CANCELLED')),
    approval_state VARCHAR(20),               -- APPROVED, REJECTED
    error_code VARCHAR(50),
    error_message TEXT,
    
    -- Raw responses from PayOS
    create_response JSONB,                    -- Response khi tạo lệnh chi
    webhook_data JSONB,                       -- Data từ webhook
    
    -- Timing
    transaction_datetime TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_payout_reference ON payout_transaction(payos_reference_id);
CREATE INDEX idx_payout_request ON payout_transaction(withdrawal_request_id);
CREATE INDEX idx_payout_state ON payout_transaction(state);
COMMENT ON TABLE payout_transaction IS 'Chi tiết giao dịch chi tiền qua PayOS';

-- =====================================================
-- 4. RESTAURANT BALANCE TABLE
-- =====================================================
-- Tracks restaurant balance (can be calculated real-time or cached)
CREATE TABLE IF NOT EXISTS restaurant_balance (
    balance_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    restaurant_id INTEGER NOT NULL UNIQUE REFERENCES restaurant_profile(restaurant_id) ON DELETE CASCADE,
    
    -- Revenue tracking
    total_revenue NUMERIC(18,2) DEFAULT 0,           -- Tổng doanh thu từ booking completed
    total_bookings_completed INTEGER DEFAULT 0,       -- Số booking đã hoàn thành
    
    -- Commission tracking
    commission_rate NUMERIC(5,2) DEFAULT 7.50,        -- % hoa hồng (default 7.5%)
    commission_type VARCHAR(20) DEFAULT 'PERCENTAGE'  -- PERCENTAGE or FIXED
        CHECK (commission_type IN ('PERCENTAGE','FIXED')),
    commission_fixed_amount NUMERIC(18,2) DEFAULT 15000, -- Phí cố định (15k VNĐ)
    total_commission NUMERIC(18,2) DEFAULT 0,         -- Tổng hoa hồng đã tính
    
    -- Withdrawal tracking
    total_withdrawn NUMERIC(18,2) DEFAULT 0,          -- Tổng đã rút thành công
    pending_withdrawal NUMERIC(18,2) DEFAULT 0,       -- Đang chờ rút
    total_withdrawal_requests INTEGER DEFAULT 0,      -- Số lần yêu cầu rút
    
    -- Calculated balance
    available_balance NUMERIC(18,2) DEFAULT 0,        -- Số dư khả dụng
    
    -- Metadata
    last_calculated_at TIMESTAMPTZ DEFAULT now(),
    last_withdrawal_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_restaurant_balance_restaurant ON restaurant_balance(restaurant_id);
COMMENT ON TABLE restaurant_balance IS 'Số dư và thống kê tài chính của nhà hàng';

-- =====================================================
-- 5. PAYOUT AUDIT LOG TABLE
-- =====================================================
-- Logs all payout-related activities for debugging and auditing
CREATE TABLE IF NOT EXISTS payout_audit_log (
    log_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    withdrawal_request_id INTEGER REFERENCES withdrawal_request(request_id),
    
    -- Action tracking
    action VARCHAR(50) NOT NULL,              -- CREATE, APPROVE, REJECT, PAYOUT_CALL, WEBHOOK, etc.
    status VARCHAR(20),                       -- SUCCESS, FAILED, PENDING
    
    -- User tracking
    performed_by_user_id UUID REFERENCES users(id),
    ip_address VARCHAR(50),
    
    -- Data logging
    request_data TEXT,                        -- Request payload
    response_data TEXT,                       -- Response payload
    error_message TEXT,
    
    -- Metadata
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_withdrawal ON payout_audit_log(withdrawal_request_id);
CREATE INDEX idx_audit_action ON payout_audit_log(action);
CREATE INDEX idx_audit_created ON payout_audit_log(created_at DESC);
COMMENT ON TABLE payout_audit_log IS 'Nhật ký kiểm toán cho tất cả hoạt động rút tiền';

-- =====================================================
-- 6. TRIGGER TO AUTO-UPDATE restaurant_balance
-- =====================================================
-- Trigger khi có booking completed
CREATE OR REPLACE FUNCTION update_restaurant_balance_on_booking()
RETURNS TRIGGER AS $$
BEGIN
    -- Chỉ xử lý khi status chuyển sang COMPLETED
    IF NEW.status = 'COMPLETED' AND (OLD.status IS NULL OR OLD.status != 'COMPLETED') THEN
        INSERT INTO restaurant_balance (restaurant_id, total_revenue, total_bookings_completed)
        VALUES (NEW.restaurant_id, NEW.deposit_amount, 1)
        ON CONFLICT (restaurant_id) DO UPDATE SET
            total_revenue = restaurant_balance.total_revenue + NEW.deposit_amount,
            total_bookings_completed = restaurant_balance.total_bookings_completed + 1,
            updated_at = now();
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_update_balance_on_booking ON booking;
CREATE TRIGGER trg_update_balance_on_booking
AFTER INSERT OR UPDATE OF status ON booking
FOR EACH ROW
EXECUTE FUNCTION update_restaurant_balance_on_booking();

-- =====================================================
-- 7. FUNCTION TO CALCULATE AVAILABLE BALANCE
-- =====================================================
CREATE OR REPLACE FUNCTION calculate_available_balance(p_restaurant_id INTEGER)
RETURNS NUMERIC AS $$
DECLARE
    v_balance restaurant_balance%ROWTYPE;
    v_commission NUMERIC;
    v_available NUMERIC;
BEGIN
    -- Get restaurant balance record
    SELECT * INTO v_balance FROM restaurant_balance WHERE restaurant_id = p_restaurant_id;
    
    IF NOT FOUND THEN
        RETURN 0;
    END IF;
    
    -- Calculate commission
    IF v_balance.commission_type = 'PERCENTAGE' THEN
        v_commission := v_balance.total_revenue * (v_balance.commission_rate / 100);
    ELSE
        v_commission := v_balance.total_bookings_completed * v_balance.commission_fixed_amount;
    END IF;
    
    -- Calculate available balance
    v_available := v_balance.total_revenue - v_commission - v_balance.total_withdrawn - v_balance.pending_withdrawal;
    
    -- Update the record
    UPDATE restaurant_balance 
    SET 
        total_commission = v_commission,
        available_balance = v_available,
        last_calculated_at = now()
    WHERE restaurant_id = p_restaurant_id;
    
    RETURN v_available;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- 8. TRIGGER TO UPDATE BALANCE ON WITHDRAWAL STATUS CHANGE
-- =====================================================
CREATE OR REPLACE FUNCTION update_balance_on_withdrawal_status()
RETURNS TRIGGER AS $$
BEGIN
    -- Khi withdrawal được approve/processing -> lock balance
    IF NEW.status IN ('APPROVED', 'PROCESSING') AND OLD.status = 'PENDING' THEN
        UPDATE restaurant_balance
        SET 
            pending_withdrawal = pending_withdrawal + NEW.amount,
            updated_at = now()
        WHERE restaurant_id = NEW.restaurant_id;
        
    -- Khi withdrawal succeeded -> move to withdrawn
    ELSIF NEW.status = 'SUCCEEDED' AND OLD.status IN ('APPROVED', 'PROCESSING') THEN
        UPDATE restaurant_balance
        SET 
            total_withdrawn = total_withdrawn + NEW.amount,
            pending_withdrawal = pending_withdrawal - NEW.amount,
            total_withdrawal_requests = total_withdrawal_requests + 1,
            last_withdrawal_at = now(),
            updated_at = now()
        WHERE restaurant_id = NEW.restaurant_id;
        
    -- Khi withdrawal failed/rejected/cancelled -> unlock balance
    ELSIF NEW.status IN ('FAILED', 'REJECTED', 'CANCELLED') AND OLD.status IN ('PENDING', 'APPROVED', 'PROCESSING') THEN
        UPDATE restaurant_balance
        SET 
            pending_withdrawal = GREATEST(0, pending_withdrawal - NEW.amount),
            updated_at = now()
        WHERE restaurant_id = NEW.restaurant_id;
    END IF;
    
    -- Recalculate available balance
    PERFORM calculate_available_balance(NEW.restaurant_id);
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_update_balance_on_withdrawal ON withdrawal_request;
CREATE TRIGGER trg_update_balance_on_withdrawal
AFTER UPDATE OF status ON withdrawal_request
FOR EACH ROW
EXECUTE FUNCTION update_balance_on_withdrawal_status();

-- =====================================================
-- 9. BANK DIRECTORY TABLE (Cache từ VietQR API)
-- =====================================================
-- Bảng cache danh sách ngân hàng từ VietQR API
-- Được refresh định kỳ (6-24h) từ https://api.vietqr.io/v2/banks
CREATE TABLE IF NOT EXISTS bank_directory (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    vietqr_id INTEGER,                       -- ID từ VietQR API
    bin VARCHAR(20) UNIQUE NOT NULL,         -- BIN code (6 số)
    code VARCHAR(20),                        -- Bank code (ABB, VCB, etc)
    name VARCHAR(255) NOT NULL,              -- Tên đầy đủ
    short_name VARCHAR(100),                 -- Tên ngắn gọn
    logo_url VARCHAR(500),                   -- URL logo
    transfer_supported BOOLEAN DEFAULT TRUE, -- Hỗ trợ chuyển tiền
    lookup_supported BOOLEAN DEFAULT TRUE,   -- Hỗ trợ lookup account
    is_active BOOLEAN DEFAULT TRUE,          -- Còn hoạt động
    
    -- Metadata
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_synced_at TIMESTAMPTZ,              -- Lần sync cuối từ VietQR
    
    -- Indexes
    CONSTRAINT unique_bin UNIQUE(bin)
);

CREATE INDEX idx_bank_directory_bin ON bank_directory(bin);
CREATE INDEX idx_bank_directory_code ON bank_directory(code);
CREATE INDEX idx_bank_directory_active ON bank_directory(is_active);

COMMENT ON TABLE bank_directory IS 'Cache danh sách ngân hàng từ VietQR API';
COMMENT ON COLUMN bank_directory.bin IS 'BIN code 6 số - dùng cho PayOS toBin';
COMMENT ON COLUMN bank_directory.last_synced_at IS 'Thời điểm sync cuối từ VietQR API';

-- Thêm vài bank phổ biến để system có thể chạy ngay (sẽ được sync đầy đủ sau)
INSERT INTO bank_directory (vietqr_id, bin, code, name, short_name) VALUES
(17, '970415', 'VCB', 'Ngân hàng TMCP Ngoại Thương Việt Nam', 'Vietcombank'),
(9, '970436', 'ICB', 'Ngân hàng TMCP Công Thương Việt Nam', 'VietinBank'),
(49, '970422', 'MBB', 'Ngân hàng TMCP Quân Đội', 'MB Bank'),
(54, '970407', 'TCB', 'Ngân hàng TMCP Kỹ Thương Việt Nam', 'Techcombank'),
(3, '970418', 'BIDV', 'Ngân hàng TMCP Đầu Tư và Phát Triển Việt Nam', 'BIDV')
ON CONFLICT (bin) DO NOTHING;

COMMIT;

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================
-- Uncomment to verify tables were created
-- SELECT table_name FROM information_schema.tables 
-- WHERE table_schema = 'public' 
-- AND table_name IN ('restaurant_bank_account', 'withdrawal_request', 'payout_transaction', 'restaurant_balance', 'payout_audit_log', 'bank_code_list')
-- ORDER BY table_name;

-- =====================================================
-- CẬP NHẬT DATABASE CHO LUỒNG RÚT TIỀN THỦ CÔNG
-- =====================================================
-- Chuyển đổi từ PayOS sang luồng manual hoàn toàn

BEGIN;

-- =====================================================
-- 1. CẬP NHẬT RESTAURANT_BANK_ACCOUNT
-- =====================================================

-- Thêm unique index cho tài khoản mặc định
CREATE UNIQUE INDEX IF NOT EXISTS uq_bank_default_per_restaurant 
ON restaurant_bank_account(restaurant_id) 
WHERE is_default = TRUE;

-- Thêm ràng buộc tham chiếu bank_directory (nếu muốn validate)
-- ALTER TABLE restaurant_bank_account 
-- ADD CONSTRAINT fk_bank_code 
-- FOREIGN KEY (bank_code) REFERENCES bank_directory(bin);

-- =====================================================
-- 2. CẬP NHẬT WITHDRAWAL_REQUEST
-- =====================================================

-- Thêm các cột manual transfer
ALTER TABLE withdrawal_request 
ADD COLUMN IF NOT EXISTS manual_transfer_ref VARCHAR(64),
ADD COLUMN IF NOT EXISTS manual_transferred_at TIMESTAMPTZ,
ADD COLUMN IF NOT EXISTS manual_transferred_by UUID REFERENCES users(id),
ADD COLUMN IF NOT EXISTS manual_note TEXT,
ADD COLUMN IF NOT EXISTS manual_proof_url TEXT;

-- Cập nhật constraint status - chỉ giữ PENDING, SUCCEEDED, REJECTED
ALTER TABLE withdrawal_request 
DROP CONSTRAINT IF EXISTS withdrawal_request_status_check;

ALTER TABLE withdrawal_request 
ADD CONSTRAINT withdrawal_request_status_check 
CHECK (status IN ('PENDING','SUCCEEDED','REJECTED'));

-- Thêm constraint cho amount tối thiểu
ALTER TABLE withdrawal_request 
ADD CONSTRAINT withdrawal_amount_min_check 
CHECK (amount >= 100000);

-- Thêm constraint cho net_amount calculation
ALTER TABLE withdrawal_request 
ADD CONSTRAINT withdrawal_net_amount_check 
CHECK (net_amount = amount - COALESCE(commission_amount, 0));

-- =====================================================
-- 3. XÓA BẢNG PAYOUT_TRANSACTION (PayOS)
-- =====================================================

DROP TABLE IF EXISTS payout_transaction CASCADE;

-- =====================================================
-- 4. CẬP NHẬT RESTAURANT_BALANCE
-- =====================================================

-- Đổi kiểu dữ liệu amount sang NUMERIC(18,0) cho VND
ALTER TABLE restaurant_balance 
ALTER COLUMN total_revenue TYPE NUMERIC(18,0),
ALTER COLUMN commission_fixed_amount TYPE NUMERIC(18,0),
ALTER COLUMN total_commission TYPE NUMERIC(18,0),
ALTER COLUMN total_withdrawn TYPE NUMERIC(18,0),
ALTER COLUMN pending_withdrawal TYPE NUMERIC(18,0),
ALTER COLUMN available_balance TYPE NUMERIC(18,0);

-- =====================================================
-- 5. CẬP NHẬT WITHDRAWAL_REQUEST AMOUNT
-- =====================================================

-- Đổi kiểu dữ liệu amount sang NUMERIC(18,0) cho VND
ALTER TABLE withdrawal_request 
ALTER COLUMN amount TYPE NUMERIC(18,0),
ALTER COLUMN commission_amount TYPE NUMERIC(18,0),
ALTER COLUMN net_amount TYPE NUMERIC(18,0);

-- =====================================================
-- 6. ĐỔI TÊN BẢNG AUDIT
-- =====================================================

-- Đổi tên bảng audit từ payout_audit_log sang withdrawal_audit_log
ALTER TABLE payout_audit_log RENAME TO withdrawal_audit_log;

-- Cập nhật comment
COMMENT ON TABLE withdrawal_audit_log IS 'Nhật ký kiểm toán cho tất cả hoạt động rút tiền thủ công';

-- =====================================================
-- 7. CẬP NHẬT TRIGGER CHO WITHDRAWAL
-- =====================================================

-- Xóa trigger cũ
DROP TRIGGER IF EXISTS trg_update_balance_on_withdrawal ON withdrawal_request;
DROP FUNCTION IF EXISTS update_balance_on_withdrawal_status();

-- Tạo function mới cho luồng manual
CREATE OR REPLACE FUNCTION update_balance_on_withdrawal_status()
RETURNS TRIGGER AS $$
BEGIN
    -- Khi tạo yêu cầu rút (PENDING) -> tăng pending_withdrawal
    IF NEW.status = 'PENDING' AND (OLD.status IS NULL OR OLD.status != 'PENDING') THEN
        UPDATE restaurant_balance
        SET 
            pending_withdrawal = pending_withdrawal + NEW.amount,
            total_withdrawal_requests = total_withdrawal_requests + 1,
            updated_at = now()
        WHERE restaurant_id = NEW.restaurant_id;
        
    -- Khi withdrawal succeeded -> move to withdrawn
    ELSIF NEW.status = 'SUCCEEDED' AND OLD.status = 'PENDING' THEN
        UPDATE restaurant_balance
        SET 
            total_withdrawn = total_withdrawn + NEW.amount,
            pending_withdrawal = pending_withdrawal - NEW.amount,
            last_withdrawal_at = now(),
            updated_at = now()
        WHERE restaurant_id = NEW.restaurant_id;
        
    -- Khi withdrawal rejected -> giảm pending_withdrawal
    ELSIF NEW.status = 'REJECTED' AND OLD.status = 'PENDING' THEN
        UPDATE restaurant_balance
        SET 
            pending_withdrawal = GREATEST(0, pending_withdrawal - NEW.amount),
            updated_at = now()
        WHERE restaurant_id = NEW.restaurant_id;
    END IF;
    
    -- Recalculate available balance
    PERFORM calculate_available_balance(NEW.restaurant_id);
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Tạo trigger mới
CREATE TRIGGER trg_update_balance_on_withdrawal
AFTER INSERT OR UPDATE OF status ON withdrawal_request
FOR EACH ROW
EXECUTE FUNCTION update_balance_on_withdrawal_status();

-- =====================================================
-- 8. TẠO TRIGGER AUTO-UPDATE updated_at
-- =====================================================

-- Function chung để update updated_at
CREATE OR REPLACE FUNCTION touch_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger cho restaurant_bank_account
DROP TRIGGER IF EXISTS trg_bank_account_updated_at ON restaurant_bank_account;
CREATE TRIGGER trg_bank_account_updated_at
BEFORE UPDATE ON restaurant_bank_account
FOR EACH ROW
EXECUTE FUNCTION touch_updated_at();

-- Trigger cho withdrawal_request
DROP TRIGGER IF EXISTS trg_withdrawal_updated_at ON withdrawal_request;
CREATE TRIGGER trg_withdrawal_updated_at
BEFORE UPDATE ON withdrawal_request
FOR EACH ROW
EXECUTE FUNCTION touch_updated_at();

-- Trigger cho restaurant_balance
DROP TRIGGER IF EXISTS trg_balance_updated_at ON restaurant_balance;
CREATE TRIGGER trg_balance_updated_at
BEFORE UPDATE ON restaurant_balance
FOR EACH ROW
EXECUTE FUNCTION touch_updated_at();

-- Trigger cho withdrawal_audit_log
DROP TRIGGER IF EXISTS trg_audit_updated_at ON withdrawal_audit_log;
CREATE TRIGGER trg_audit_updated_at
BEFORE UPDATE ON withdrawal_audit_log
FOR EACH ROW
EXECUTE FUNCTION touch_updated_at();

-- =====================================================
-- 9. CẬP NHẬT FUNCTION CALCULATE_AVAILABLE_BALANCE
-- =====================================================

-- Cập nhật function để sử dụng NUMERIC(18,0)
CREATE OR REPLACE FUNCTION calculate_available_balance(p_restaurant_id INTEGER)
RETURNS NUMERIC AS $$
DECLARE
    v_balance restaurant_balance%ROWTYPE;
    v_commission NUMERIC(18,0);
    v_available NUMERIC(18,0);
BEGIN
    -- Get restaurant balance record
    SELECT * INTO v_balance FROM restaurant_balance WHERE restaurant_id = p_restaurant_id;
    
    IF NOT FOUND THEN
        RETURN 0;
    END IF;
    
    -- Calculate commission
    IF v_balance.commission_type = 'PERCENTAGE' THEN
        v_commission := v_balance.total_revenue * (v_balance.commission_rate / 100);
    ELSE
        v_commission := v_balance.total_bookings_completed * v_balance.commission_fixed_amount;
    END IF;
    
    -- Calculate available balance
    v_available := v_balance.total_revenue - v_commission - v_balance.total_withdrawn - v_balance.pending_withdrawal;
    
    -- Update the record
    UPDATE restaurant_balance 
    SET 
        total_commission = v_commission,
        available_balance = v_available,
        last_calculated_at = now()
    WHERE restaurant_id = p_restaurant_id;
    
    RETURN v_available;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- 10. CẬP NHẬT TRIGGER CHO BOOKING
-- =====================================================

-- Cập nhật trigger để sử dụng NUMERIC(18,0)
CREATE OR REPLACE FUNCTION update_restaurant_balance_on_booking()
RETURNS TRIGGER AS $$
BEGIN
    -- Chỉ xử lý khi status chuyển sang COMPLETED
    IF NEW.status = 'COMPLETED' AND (OLD.status IS NULL OR OLD.status != 'COMPLETED') THEN
        INSERT INTO restaurant_balance (restaurant_id, total_revenue, total_bookings_completed)
        VALUES (NEW.restaurant_id, NEW.deposit_amount, 1)
        ON CONFLICT (restaurant_id) DO UPDATE SET
            total_revenue = restaurant_balance.total_revenue + NEW.deposit_amount,
            total_bookings_completed = restaurant_balance.total_bookings_completed + 1,
            updated_at = now();
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- 11. XÓA BANK_DIRECTORY (TÙY CHỌN)
-- =====================================================
-- Uncomment nếu không muốn dùng bank_directory
-- DROP TABLE IF EXISTS bank_directory CASCADE;

-- =====================================================
-- 12. CẬP NHẬT INDEXES
-- =====================================================

-- Xóa index cũ cho payout_transaction
DROP INDEX IF EXISTS idx_payout_reference;
DROP INDEX IF EXISTS idx_payout_request;
DROP INDEX IF EXISTS idx_payout_state;

-- Thêm index cho manual transfer
CREATE INDEX IF NOT EXISTS idx_withdrawal_manual_ref ON withdrawal_request(manual_transfer_ref);
CREATE INDEX IF NOT EXISTS idx_withdrawal_manual_transferred_by ON withdrawal_request(manual_transferred_by);
CREATE INDEX IF NOT EXISTS idx_withdrawal_manual_transferred_at ON withdrawal_request(manual_transferred_at);

-- =====================================================
-- 13. VERIFICATION
-- =====================================================

-- Kiểm tra các bảng còn tồn tại
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN (
    'restaurant_bank_account', 
    'withdrawal_request', 
    'restaurant_balance', 
    'withdrawal_audit_log'
)
ORDER BY table_name;

-- Kiểm tra constraint mới
SELECT constraint_name, constraint_type 
FROM information_schema.table_constraints 
WHERE table_name = 'withdrawal_request' 
AND constraint_type = 'CHECK';

-- Kiểm tra index mới
SELECT indexname, indexdef 
FROM pg_indexes 
WHERE tablename = 'restaurant_bank_account' 
AND indexname = 'uq_bank_default_per_restaurant';

COMMIT;

-- =====================================================
-- 14. CLEANUP SCRIPT (CHẠY SAU KHI XÁC NHẬN)
-- =====================================================
-- Uncomment để xóa hoàn toàn các bảng PayOS
-- DROP TABLE IF EXISTS payout_transaction CASCADE;
-- DROP TABLE IF EXISTS bank_directory CASCADE;

PRINT '✅ Database đã được cập nhật cho luồng rút tiền thủ công!';
PRINT '📋 Các thay đổi chính:';
PRINT '   - Thêm cột manual transfer vào withdrawal_request';
PRINT '   - Đơn giản hóa status: PENDING → SUCCEEDED/REJECTED';
PRINT '   - Xóa bảng payout_transaction (PayOS)';
PRINT '   - Đổi tên payout_audit_log → withdrawal_audit_log';
PRINT '   - Thêm unique index cho tài khoản mặc định';
PRINT '   - Chuyển amount sang NUMERIC(18,0) cho VND';
PRINT '   - Thêm trigger auto-update updated_at';
PRINT '   - Cập nhật trigger cho luồng manual';


-- =====================================================
-- TEST RESTAURANT APPROVAL MIGRATION
-- Test script to verify approval fields work correctly
-- =====================================================

-- Test 1: Insert a new restaurant with approval fields
-- First, let's get a valid owner_id from restaurant_owner table
INSERT INTO restaurant_profile (
    owner_id, 
    restaurant_name, 
    address, 
    phone, 
    description, 
    cuisine_type, 
    opening_hours, 
    average_price,
    approval_status,
    approval_reason,
    business_license_file,
    contract_signed
) 
SELECT 
    (SELECT owner_id FROM restaurant_owner LIMIT 1), -- Get first available owner_id
    'Test Restaurant Approval',
    '123 Test Street, Test City',
    '0123456789',
    'Test restaurant for approval workflow',
    'Vietnamese',
    '09:00-22:00',
    150000.00,
    'PENDING',
    'New restaurant waiting for approval',
    '/uploads/test_license.pdf',
    false;

-- Test 2: Update approval status to APPROVED
UPDATE restaurant_profile 
SET 
    approval_status = 'APPROVED',
    approval_reason = 'All documents verified, restaurant approved',
    approved_by = 'admin_user',
    approved_at = NOW()
WHERE restaurant_name = 'Test Restaurant Approval';

-- Test 3: Test rejection scenario
UPDATE restaurant_profile 
SET 
    approval_status = 'REJECTED',
    rejection_reason = 'Incomplete business license documentation',
    approved_by = 'admin_user',
    approved_at = NOW()
WHERE restaurant_name = 'Test Restaurant Approval';

-- Test 4: Test contract signing
UPDATE restaurant_profile 
SET 
    contract_signed = true,
    contract_signed_at = NOW()
WHERE restaurant_name = 'Test Restaurant Approval';

-- Test 5: Query restaurants by approval status
SELECT 
    restaurant_id,
    restaurant_name,
    approval_status,
    approval_reason,
    approved_by,
    approved_at,
    rejection_reason,
    business_license_file,
    contract_signed,
    contract_signed_at
FROM restaurant_profile 
WHERE approval_status = 'PENDING';

-- Test 6: Query approved restaurants
SELECT 
    restaurant_id,
    restaurant_name,
    approval_status,
    approval_reason,
    approved_by,
    approved_at
FROM restaurant_profile 
WHERE approval_status = 'APPROVED';

-- Test 7: Query rejected restaurants
SELECT 
    restaurant_id,
    restaurant_name,
    approval_status,
    rejection_reason,
    approved_by,
    approved_at
FROM restaurant_profile 
WHERE approval_status = 'REJECTED';

-- Test 8: Count restaurants by status
SELECT 
    approval_status,
    COUNT(*) as count
FROM restaurant_profile 
GROUP BY approval_status
ORDER BY approval_status;

-- Test 9: Clean up test data
DELETE FROM restaurant_profile 
WHERE restaurant_name = 'Test Restaurant Approval';

-- Test 10: Verify constraints work
-- This should fail:
-- INSERT INTO restaurant_profile (owner_id, restaurant_name, approval_status) 
-- VALUES ((SELECT owner_id FROM restaurant_owner LIMIT 1), 'Invalid Status Test', 'INVALID_STATUS');
