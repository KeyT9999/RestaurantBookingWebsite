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
