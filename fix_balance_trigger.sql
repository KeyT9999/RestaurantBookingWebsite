-- Kiểm tra trigger hiện tại
SELECT 
    trigger_name,
    event_manipulation,
    event_object_table,
    action_statement
FROM information_schema.triggers 
WHERE event_object_table = 'withdrawal_request';

-- Xóa trigger cũ nếu có
DROP TRIGGER IF EXISTS trg_update_balance_on_withdrawal ON withdrawal_request;
DROP FUNCTION IF EXISTS update_balance_on_withdrawal_status();

-- Tạo lại function cho luồng manual
CREATE OR REPLACE FUNCTION update_balance_on_withdrawal_status()
RETURNS TRIGGER AS $$
BEGIN
    -- Debug log
    RAISE NOTICE 'Trigger fired: OLD.status=%, NEW.status=%, amount=%', 
        COALESCE(OLD.status, 'NULL'), NEW.status, NEW.amount;
    
    -- Khi tạo yêu cầu rút (PENDING) -> tăng pending_withdrawal
    IF NEW.status = 'PENDING' AND (OLD.status IS NULL OR OLD.status != 'PENDING') THEN
        UPDATE restaurant_balance
        SET 
            pending_withdrawal = pending_withdrawal + NEW.amount,
            total_withdrawal_requests = total_withdrawal_requests + 1,
            updated_at = now()
        WHERE restaurant_id = NEW.restaurant_id;
        
        RAISE NOTICE 'Added to pending: % for restaurant %', NEW.amount, NEW.restaurant_id;
        
    -- Khi withdrawal succeeded -> move to withdrawn
    ELSIF NEW.status = 'SUCCEEDED' AND OLD.status = 'PENDING' THEN
        UPDATE restaurant_balance
        SET 
            total_withdrawn = total_withdrawn + NEW.amount,
            pending_withdrawal = GREATEST(0, pending_withdrawal - NEW.amount),
            last_withdrawal_at = now(),
            updated_at = now()
        WHERE restaurant_id = NEW.restaurant_id;
        
        RAISE NOTICE 'Moved to withdrawn: % for restaurant %', NEW.amount, NEW.restaurant_id;
        
    -- Khi withdrawal rejected -> giảm pending_withdrawal
    ELSIF NEW.status = 'REJECTED' AND OLD.status = 'PENDING' THEN
        UPDATE restaurant_balance
        SET 
            pending_withdrawal = GREATEST(0, pending_withdrawal - NEW.amount),
            updated_at = now()
        WHERE restaurant_id = NEW.restaurant_id;
        
        RAISE NOTICE 'Removed from pending (rejected): % for restaurant %', NEW.amount, NEW.restaurant_id;
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

-- Test trigger bằng cách update một record
UPDATE withdrawal_request 
SET status = 'SUCCEEDED'
WHERE request_id = 5 AND status = 'SUCCEEDED';

-- Kiểm tra kết quả
SELECT 
    'AFTER TRIGGER TEST' as status,
    total_withdrawn,
    pending_withdrawal,
    available_balance,
    updated_at
FROM restaurant_balance 
WHERE restaurant_id = 18;
