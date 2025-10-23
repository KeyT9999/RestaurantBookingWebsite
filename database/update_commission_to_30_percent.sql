-- Cập nhật tỷ lệ hoa hồng từ 7.5% thành 30%
-- File: update_commission_to_30_percent.sql

-- 1. Cập nhật default commission rate trong bảng restaurant_balance
ALTER TABLE restaurant_balance 
ALTER COLUMN commission_rate SET DEFAULT 30.00;

-- 2. Cập nhật tất cả records hiện tại có commission_rate = 7.50 thành 30.00
UPDATE restaurant_balance 
SET commission_rate = 30.00 
WHERE commission_rate = 7.50;

-- 3. Recalculate tất cả available_balance với tỷ lệ mới
UPDATE restaurant_balance 
SET 
    total_commission = total_revenue * (commission_rate / 100),
    available_balance = total_revenue - (total_revenue * (commission_rate / 100)) - total_withdrawn - pending_withdrawal,
    last_calculated_at = now();

-- 4. Cập nhật comment trong database
COMMENT ON COLUMN restaurant_balance.commission_rate IS 'Tỷ lệ hoa hồng admin (default 30%)';

-- 5. Log thay đổi (sử dụng cấu trúc bảng payout_audit_log thực tế)
INSERT INTO payout_audit_log (
    action, 
    status, 
    request_data, 
    response_data
) VALUES (
    'COMMISSION_RATE_UPDATE', 
    'SUCCESS', 
    '{"old_rate": 7.5, "new_rate": 30.0, "action": "Update commission rate from 7.5% to 30%"}',
    '{"updated_at": "' || now() || '", "affected_restaurants": (SELECT COUNT(*) FROM restaurant_balance)}'
);

-- 6. Hiển thị kết quả
SELECT 
    restaurant_id,
    commission_rate,
    total_revenue,
    total_commission,
    available_balance,
    'Updated to 30% commission' as status
FROM restaurant_balance 
ORDER BY restaurant_id;
