-- =====================================================
-- FIX PAYOS INTEGRATION - CRITICAL DATABASE UPDATES
-- =====================================================
-- Ngày: 07/10/2025
-- Mục đích: Thêm order_code và fix constraints để PayOS hoạt động
-- 
-- LƯU Ý: Script này BẮT BUỘC phải chạy trước khi test PayOS!
-- =====================================================

BEGIN;

-- =====================================================
-- BƯỚC 1: Thêm cột order_code (CRITICAL!)
-- =====================================================
-- Cột này là UNIQUE IDENTIFIER cho mỗi giao dịch PayOS
-- Code Java đã dùng nhưng database chưa có!

ALTER TABLE payment 
    ADD COLUMN IF NOT EXISTS order_code BIGINT;

COMMENT ON COLUMN payment.order_code IS 
    'Unique order code for PayOS payment tracking. Format: bookingId*1000000 + timestamp%1000000';

-- =====================================================
-- BƯỚC 2: Cập nhật constraint payment_method
-- =====================================================
-- Thêm 'payos' vào danh sách payment methods được phép

ALTER TABLE payment DROP CONSTRAINT IF EXISTS payment_payment_method_check;

ALTER TABLE payment ADD CONSTRAINT payment_payment_method_check 
    CHECK (payment_method IN (
        'cash',      -- Tiền mặt
        'card',      -- Thẻ ngân hàng
        'momo',      -- MoMo (deprecated)
        'zalopay',   -- ZaloPay
        'payos'      -- PayOS (THÊM MỚI!)
    ));

-- =====================================================
-- BƯỚC 3: Update existing data (nếu có)
-- =====================================================
-- Nếu bảng payment đã có data với order_code = NULL,
-- cần generate orderCode cho các payment cũ

UPDATE payment 
SET order_code = payment_id * 1000000 + EXTRACT(EPOCH FROM paid_at)::BIGINT % 1000000 
WHERE order_code IS NULL;

-- =====================================================
-- BƯỚC 4: Thêm UNIQUE constraint cho order_code
-- =====================================================
-- Đảm bảo mỗi orderCode là duy nhất trong hệ thống

ALTER TABLE payment 
    ADD CONSTRAINT uq_payment_order_code UNIQUE (order_code);

-- =====================================================
-- BƯỚC 5: Set order_code NOT NULL
-- =====================================================
-- Từ giờ mọi payment MỚI phải có orderCode

ALTER TABLE payment 
    ALTER COLUMN order_code SET NOT NULL;

-- =====================================================
-- BƯỚC 6: Thêm INDEX cho performance
-- =====================================================
-- PayOS webhook sẽ query payment theo order_code rất thường xuyên
-- Index này giúp tăng tốc độ query từ O(n) → O(log n)

CREATE INDEX IF NOT EXISTS idx_payment_order_code 
    ON payment(order_code);

CREATE INDEX IF NOT EXISTS idx_payment_booking_id 
    ON payment(booking_id);

-- Index cho các query phổ biến khác
CREATE INDEX IF NOT EXISTS idx_payment_status 
    ON payment(status);

CREATE INDEX IF NOT EXISTS idx_payment_method 
    ON payment(payment_method);

CREATE INDEX IF NOT EXISTS idx_payment_customer_id 
    ON payment(customer_id);

-- =====================================================
-- BƯỚC 7: Fix status constraint (nếu chưa đúng)
-- =====================================================
-- Đảm bảo status constraint có đầy đủ các giá trị

ALTER TABLE payment DROP CONSTRAINT IF EXISTS payment_status_check;

ALTER TABLE payment ADD CONSTRAINT payment_status_check 
    CHECK (status IN (
        'pending',      -- Chờ thanh toán
        'processing',   -- Đang xử lý
        'completed',    -- Thành công
        'failed',       -- Thất bại
        'refunded',     -- Đã hoàn tiền
        'cancelled'     -- Đã hủy
    ));

-- =====================================================
-- BƯỚC 8: Fix payment_type constraint
-- =====================================================

ALTER TABLE payment DROP CONSTRAINT IF EXISTS payment_type_check;

ALTER TABLE payment ADD CONSTRAINT payment_type_check 
    CHECK (payment_type IN ('DEPOSIT', 'FULL_PAYMENT'));

COMMIT;

-- =====================================================
-- VERIFICATION: Kiểm tra kết quả
-- =====================================================

-- 1. Kiểm tra structure của payment table
SELECT 
    column_name, 
    data_type, 
    is_nullable,
    character_maximum_length,
    column_default
FROM information_schema.columns 
WHERE table_name = 'payment' 
  AND column_name IN (
      'order_code', 
      'payos_payment_link_id', 
      'payos_checkout_url',
      'payos_code',
      'payos_desc',
      'payment_method',
      'status',
      'payment_type'
  )
ORDER BY ordinal_position;

-- 2. Kiểm tra constraints
SELECT 
    constraint_name, 
    constraint_type 
FROM information_schema.table_constraints 
WHERE table_name = 'payment'
ORDER BY constraint_type, constraint_name;

-- 3. Kiểm tra indexes
SELECT 
    indexname, 
    indexdef 
FROM pg_indexes 
WHERE tablename = 'payment'
ORDER BY indexname;

-- 4. Đếm số payment records
SELECT 
    COUNT(*) as total_payments,
    COUNT(order_code) as payments_with_ordercode,
    COUNT(CASE WHEN order_code IS NULL THEN 1 END) as payments_without_ordercode
FROM payment;

-- =====================================================
-- SUCCESS MESSAGE
-- =====================================================
DO $$ 
BEGIN 
    RAISE NOTICE '=====================================================';
    RAISE NOTICE '✅ PayOS Database Migration Completed Successfully!';
    RAISE NOTICE '=====================================================';
    RAISE NOTICE 'Changes applied:';
    RAISE NOTICE '1. ✅ Added order_code column (BIGINT NOT NULL UNIQUE)';
    RAISE NOTICE '2. ✅ Updated payment_method constraint (added PAYOS)';
    RAISE NOTICE '3. ✅ Updated existing data with order_code';
    RAISE NOTICE '4. ✅ Added UNIQUE constraint on order_code';
    RAISE NOTICE '5. ✅ Added indexes for performance';
    RAISE NOTICE '6. ✅ Fixed status constraint';
    RAISE NOTICE '=====================================================';
    RAISE NOTICE 'Next steps:';
    RAISE NOTICE '1. Restart your Spring Boot application';
    RAISE NOTICE '2. Test PayOS payment flow';
    RAISE NOTICE '3. Deploy to production';
    RAISE NOTICE '4. Confirm webhook URL with PayOS';
    RAISE NOTICE '=====================================================';
END $$;

