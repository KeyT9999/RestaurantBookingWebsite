-- =====================================================
-- SCRIPT XÓA CÁC FILE KHÔNG CẦN THIẾT CHO LUỒNG THỦ CÔNG
-- =====================================================

-- Xóa các file documentation cũ
-- (Đã xóa trong code, đây là danh sách để tham khảo)

/*
ĐÃ XÓA:
- HUONG_DAN_THANH_TOAN_PAYOS.md
- FIX_NUT_THANH_TOAN.md  
- BAO_CAO_KIEM_TRA_PAYOS.md
- ADMIN_APPROVE_TEST_GUIDE.md
- test_admin_approve_flow.sql
- E2E_WITHDRAWAL_TEST_GUIDE.md
- create_test_data_for_e2e.sql
- check_e2e_withdrawal.sql
- ADMIN_APPROVE_FLOW_STANDARDIZED.md
- THYMELEAF_FIX_COMPLETE.md
- DEBUG_ADMIN_WITHDRAWAL_GUIDE.md
- test_after_thymeleaf_fix.sql
- test_with_correct_schema.sql
- quick_check_withdrawal_data.sql
- SCHEMA_AND_THYMELEAF_FIX_COMPLETE.md
- ADMIN_WITHDRAWAL_RECREATED.md

ĐÃ XÓA JAVA FILES:
- src/main/java/com/example/booking/domain/PayoutTransaction.java
- src/main/java/com/example/booking/domain/PayoutAuditLog.java
- src/main/java/com/example/booking/repository/PayoutTransactionRepository.java
- src/main/java/com/example/booking/repository/PayoutAuditLogRepository.java
- src/main/java/com/example/booking/service/PayoutAuditService.java
- src/main/java/com/example/booking/service/PayosPayoutService.java
- src/main/java/com/example/booking/service/PayoutWebhookService.java
- src/main/java/com/example/booking/scheduler/PayoutPollerScheduler.java
- src/main/java/com/example/booking/web/controller/PayosPayoutWebhookController.java
- src/main/java/com/example/booking/dto/payout/PayosPayoutRequest.java
- src/main/java/com/example/booking/dto/payout/PayosPayoutResponse.java
*/

-- =====================================================
-- CẬP NHẬT APPLICATION.YML (LOẠI BỎ PAYOS CONFIG)
-- =====================================================

/*
Cần xóa/sửa trong application.yml:
- payos.client-id
- payos.api-key  
- payos.checksum-key
- payos.payout-endpoint
- Các config liên quan đến PayOS webhook
*/

-- =====================================================
-- CẬP NHẬT POM.XML (LOẠI BỎ PAYOS DEPENDENCIES)
-- =====================================================

/*
Cần xóa/sửa trong pom.xml:
- PayOS SDK dependencies
- Các dependency không cần thiết cho luồng manual
*/

-- =====================================================
-- CẬP NHẬT SECURITY CONFIG
-- =====================================================

/*
Cần xóa/sửa trong SecurityConfig:
- PayOS webhook endpoints
- Các endpoint không cần thiết
*/

-- =====================================================
-- CẬP NHẬT SCHEDULING CONFIG
-- =====================================================

/*
Cần xóa/sửa trong SchedulingConfig:
- PayoutPollerScheduler
- Các scheduled job liên quan PayOS
*/

-- =====================================================
-- VERIFICATION
-- =====================================================

-- Kiểm tra các bảng còn tồn tại
SELECT 'Tables còn lại:' as info;
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

-- Kiểm tra các bảng đã xóa
SELECT 'Tables đã xóa:' as info;
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN (
    'payout_transaction'
);

-- Kiểm tra constraint status mới
SELECT 'Status constraints:' as info;
SELECT constraint_name, check_clause 
FROM information_schema.check_constraints 
WHERE constraint_name = 'withdrawal_request_status_check';

-- Kiểm tra unique index mới
SELECT 'Unique indexes:' as info;
SELECT indexname, indexdef 
FROM pg_indexes 
WHERE tablename = 'restaurant_bank_account' 
AND indexname = 'uq_bank_default_per_restaurant';

PRINT '✅ Cleanup hoàn tất!';
PRINT '📋 Các thay đổi:';
PRINT '   - Xóa tất cả file PayOS';
PRINT '   - Xóa bảng payout_transaction';
PRINT '   - Đơn giản hóa luồng withdrawal';
PRINT '   - Chỉ giữ lại luồng thủ công';
