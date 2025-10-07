-- Fix payment status constraint để chấp nhận CHỮ HOA
-- Vấn đề: Java enum lưu giá trị chữ HOA (PENDING, COMPLETED, etc.)
-- nhưng constraint cũ chỉ chấp nhận chữ thường (pending, completed, etc.)

BEGIN;

-- Xóa constraint cũ
ALTER TABLE payment DROP CONSTRAINT IF EXISTS payment_status_check;

-- Tạo constraint mới chấp nhận CHỮ HOA (theo Java enum)
ALTER TABLE payment ADD CONSTRAINT payment_status_check 
    CHECK (status IN (
        'PENDING',      -- Chờ thanh toán
        'PROCESSING',   -- Đang xử lý  
        'COMPLETED',    -- Thành công
        'FAILED',       -- Thất bại
        'REFUNDED',     -- Đã hoàn tiền
        'CANCELLED'     -- Đã hủy
    ));

-- Fix payment_method constraint (thêm PAYOS chữ HOA)
ALTER TABLE payment DROP CONSTRAINT IF EXISTS payment_payment_method_check;

ALTER TABLE payment ADD CONSTRAINT payment_payment_method_check 
    CHECK (payment_method IN (
        'CASH',      -- Tiền mặt
        'CARD',      -- Thẻ ngân hàng
        'MOMO',      -- MoMo (deprecated)
        'ZALOPAY',   -- ZaloPay
        'PAYOS'      -- PayOS
    ));

-- Update data cũ từ chữ thường sang chữ HOA (nếu có)
UPDATE payment SET status = UPPER(status) WHERE status IN ('pending', 'processing', 'completed', 'failed', 'refunded', 'cancelled');
UPDATE payment SET payment_method = UPPER(payment_method) WHERE payment_method IN ('cash', 'card', 'momo', 'zalopay', 'payos');

COMMIT;

-- Kiểm tra kết quả
SELECT 
    payment_id,
    booking_id,
    status,
    payment_method,
    amount
FROM payment
ORDER BY payment_id DESC
LIMIT 5;

