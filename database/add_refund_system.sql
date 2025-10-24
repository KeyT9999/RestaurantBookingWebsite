-- Add refund system tables and columns
-- This script adds the refund functionality to the existing database

-- 1. Create refund_request table
CREATE TABLE IF NOT EXISTS refund_request (
    refund_request_id SERIAL PRIMARY KEY,
    payment_id INTEGER NOT NULL,
    customer_id UUID NOT NULL,
    restaurant_id INTEGER NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    reason VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    processed_by UUID,
    transfer_reference VARCHAR(255),
    qr_code_data TEXT,
    qr_code_url VARCHAR(500),
    customer_bank_code VARCHAR(20),
    customer_account_number VARCHAR(50),
    customer_account_holder VARCHAR(255),
    admin_note VARCHAR(500),
    
    -- Foreign key constraints
    CONSTRAINT fk_refund_request_payment FOREIGN KEY (payment_id) REFERENCES payment(payment_id),
    CONSTRAINT fk_refund_request_customer FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    CONSTRAINT fk_refund_request_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant_profile(restaurant_id),
    
    -- Check constraints
    CONSTRAINT chk_refund_status CHECK (status IN ('PENDING', 'COMPLETED', 'REJECTED')),
    CONSTRAINT chk_refund_amount_positive CHECK (amount > 0)
);

-- 2. Add refund tracking columns to restaurant_balance table
ALTER TABLE restaurant_balance 
ADD COLUMN IF NOT EXISTS pending_refund DECIMAL(18,2) DEFAULT 0,
ADD COLUMN IF NOT EXISTS total_refunded DECIMAL(18,2) DEFAULT 0;

-- 3. Add refund_request_id column to payment table
ALTER TABLE payment 
ADD COLUMN IF NOT EXISTS refund_request_id INTEGER;

-- Add foreign key constraint for refund_request_id
ALTER TABLE payment 
ADD CONSTRAINT IF NOT EXISTS fk_payment_refund_request 
FOREIGN KEY (refund_request_id) REFERENCES refund_request(refund_request_id);

-- 4. Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_refund_request_status ON refund_request(status);
CREATE INDEX IF NOT EXISTS idx_refund_request_customer ON refund_request(customer_id);
CREATE INDEX IF NOT EXISTS idx_refund_request_restaurant ON refund_request(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_refund_request_requested_at ON refund_request(requested_at);
CREATE INDEX IF NOT EXISTS idx_payment_refund_request_id ON payment(refund_request_id);

-- 5. Create enum type for refund status (if not exists)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'refund_status') THEN
        CREATE TYPE refund_status AS ENUM ('PENDING', 'COMPLETED', 'REJECTED');
    END IF;
END $$;

-- 6. Update existing restaurant_balance records to have default values
UPDATE restaurant_balance 
SET pending_refund = 0, total_refunded = 0 
WHERE pending_refund IS NULL OR total_refunded IS NULL;

-- 7. Create function to update restaurant balance on refund complete
CREATE OR REPLACE FUNCTION update_restaurant_balance_on_refund_complete()
RETURNS TRIGGER AS $$
BEGIN
    -- Only process when status changes to COMPLETED
    IF NEW.status = 'COMPLETED' AND (OLD.status IS NULL OR OLD.status != 'COMPLETED') THEN
        -- Update restaurant balance
        UPDATE restaurant_balance 
        SET 
            pending_refund = pending_refund - NEW.amount,
            total_refunded = total_refunded + NEW.amount,
            updated_at = CURRENT_TIMESTAMP
        WHERE restaurant_id = NEW.restaurant_id;
        
        -- Update payment status to REFUNDED
        UPDATE payment 
        SET 
            status = 'REFUNDED',
            refunded_at = CURRENT_TIMESTAMP
        WHERE payment_id = NEW.payment_id;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 8. Create trigger for refund complete
DROP TRIGGER IF EXISTS trigger_refund_complete ON refund_request;
CREATE TRIGGER trigger_refund_complete
    AFTER UPDATE ON refund_request
    FOR EACH ROW
    EXECUTE FUNCTION update_restaurant_balance_on_refund_complete();

-- 9. Create function to update restaurant balance on refund reject
CREATE OR REPLACE FUNCTION update_restaurant_balance_on_refund_reject()
RETURNS TRIGGER AS $$
BEGIN
    -- Only process when status changes to REJECTED
    IF NEW.status = 'REJECTED' AND (OLD.status IS NULL OR OLD.status != 'REJECTED') THEN
        -- Restore restaurant balance
        UPDATE restaurant_balance 
        SET 
            available_balance = available_balance + NEW.amount,
            pending_refund = pending_refund - NEW.amount,
            updated_at = CURRENT_TIMESTAMP
        WHERE restaurant_id = NEW.restaurant_id;
        
        -- Update payment status back to COMPLETED
        UPDATE payment 
        SET 
            status = 'COMPLETED',
            refund_request_id = NULL
        WHERE payment_id = NEW.payment_id;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 10. Create trigger for refund reject
DROP TRIGGER IF EXISTS trigger_refund_reject ON refund_request;
CREATE TRIGGER trigger_refund_reject
    AFTER UPDATE ON refund_request
    FOR EACH ROW
    EXECUTE FUNCTION update_restaurant_balance_on_refund_reject();

-- 11. Create function to update restaurant balance on refund request
CREATE OR REPLACE FUNCTION update_restaurant_balance_on_refund_request()
RETURNS TRIGGER AS $$
BEGIN
    -- Only process when new refund request is created
    IF NEW.status = 'PENDING' THEN
        -- Update restaurant balance (allow negative available_balance)
        UPDATE restaurant_balance 
        SET 
            available_balance = available_balance - NEW.amount,
            pending_refund = pending_refund + NEW.amount,
            updated_at = CURRENT_TIMESTAMP
        WHERE restaurant_id = NEW.restaurant_id;
        
        -- Update payment status to REFUND_PENDING
        UPDATE payment 
        SET 
            status = 'REFUND_PENDING',
            refund_amount = NEW.amount,
            refund_reason = NEW.reason,
            refund_request_id = NEW.refund_request_id
        WHERE payment_id = NEW.payment_id;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 12. Create trigger for refund request
DROP TRIGGER IF EXISTS trigger_refund_request ON refund_request;
CREATE TRIGGER trigger_refund_request
    AFTER INSERT ON refund_request
    FOR EACH ROW
    EXECUTE FUNCTION update_restaurant_balance_on_refund_request();

-- 13. Add comments for documentation
COMMENT ON TABLE refund_request IS 'Stores refund requests for cancelled bookings';
COMMENT ON COLUMN refund_request.amount IS 'Amount to be refunded';
COMMENT ON COLUMN refund_request.status IS 'Status of refund request: PENDING, COMPLETED, REJECTED';
COMMENT ON COLUMN refund_request.qr_code_data IS 'QR code data for admin transfer';
COMMENT ON COLUMN refund_request.qr_code_url IS 'QR code URL for admin transfer';
COMMENT ON COLUMN refund_request.customer_bank_code IS 'Customer bank code (e.g., 970422 for MB Bank)';
COMMENT ON COLUMN refund_request.customer_account_number IS 'Customer bank account number';
COMMENT ON COLUMN refund_request.customer_account_holder IS 'Customer account holder name';
COMMENT ON COLUMN refund_request.transfer_reference IS 'Reference number from admin transfer';

COMMENT ON COLUMN restaurant_balance.pending_refund IS 'Amount pending refund (locked from available balance)';
COMMENT ON COLUMN restaurant_balance.total_refunded IS 'Total amount refunded to customers';

COMMENT ON COLUMN payment.refund_request_id IS 'Reference to refund request if payment is being refunded';

-- 14. Insert sample data for testing (optional)
-- INSERT INTO refund_request (payment_id, customer_id, restaurant_id, amount, reason, status)
-- VALUES (1, '550e8400-e29b-41d4-a716-446655440000', 1, 50000.00, 'Booking cancelled by customer', 'PENDING');

-- 15. Create view for admin refund management
CREATE OR REPLACE VIEW admin_refund_requests AS
SELECT 
    rr.refund_request_id,
    rr.payment_id,
    rr.amount,
    rr.reason,
    rr.status,
    rr.requested_at,
    rr.processed_at,
    rr.transfer_reference,
    rr.admin_note,
    c.full_name as customer_name,
    c.phone_number as customer_phone,
    rp.restaurant_name,
    p.payment_method,
    p.order_code
FROM refund_request rr
JOIN customer c ON rr.customer_id = c.customer_id
JOIN restaurant_profile rp ON rr.restaurant_id = rp.restaurant_id
JOIN payment p ON rr.payment_id = p.payment_id
ORDER BY rr.requested_at DESC;

COMMENT ON VIEW admin_refund_requests IS 'View for admin to manage refund requests with customer and restaurant details';

-- 16. Grant permissions (adjust as needed for your setup)
-- GRANT SELECT, INSERT, UPDATE ON refund_request TO your_app_user;
-- GRANT SELECT ON admin_refund_requests TO your_app_user;

-- 17. Final verification
SELECT 'Refund system setup completed successfully!' as status;
