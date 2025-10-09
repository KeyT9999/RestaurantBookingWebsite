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

