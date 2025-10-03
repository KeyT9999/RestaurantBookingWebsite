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



