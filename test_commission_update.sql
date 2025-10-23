-- Script test đơn giản để kiểm tra cập nhật hoa hồng
-- File: test_commission_update.sql

-- 1. Kiểm tra cấu trúc bảng payout_audit_log
\d payout_audit_log;

-- 2. Kiểm tra tỷ lệ hoa hồng hiện tại
SELECT 
    restaurant_id,
    commission_rate,
    total_revenue,
    total_commission,
    available_balance,
    'Before update' as status
FROM restaurant_balance 
ORDER BY restaurant_id;

-- 3. Chạy script cập nhật hoa hồng
\i database/update_commission_to_30_percent.sql

-- 4. Kiểm tra kết quả sau khi cập nhật
SELECT 
    restaurant_id,
    commission_rate,
    total_revenue,
    total_commission,
    available_balance,
    'After update' as status
FROM restaurant_balance 
ORDER BY restaurant_id;

-- 5. Kiểm tra audit log
SELECT 
    log_id,
    action,
    status,
    request_data,
    response_data,
    created_at
FROM payout_audit_log 
WHERE action = 'COMMISSION_RATE_UPDATE'
ORDER BY created_at DESC;
