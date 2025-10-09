-- Script thêm cột mới cho withdrawal_request để hỗ trợ chuyển khoản thủ công
-- Chạy trong pgAdmin

-- Thêm các cột mới vào withdrawal_request
ALTER TABLE withdrawal_request 
ADD COLUMN IF NOT EXISTS manual_transfer_ref VARCHAR(100),
ADD COLUMN IF NOT EXISTS manual_transferred_at TIMESTAMPTZ,
ADD COLUMN IF NOT EXISTS manual_transferred_by UUID,
ADD COLUMN IF NOT EXISTS manual_note TEXT,
ADD COLUMN IF NOT EXISTS manual_proof_url TEXT;

-- Thêm comment cho các cột mới
COMMENT ON COLUMN withdrawal_request.manual_transfer_ref IS 'Mã tham chiếu chuyển khoản (UT code, Ref No.)';
COMMENT ON COLUMN withdrawal_request.manual_transferred_at IS 'Thời điểm admin chuyển khoản';
COMMENT ON COLUMN withdrawal_request.manual_transferred_by IS 'ID admin thực hiện chuyển khoản';
COMMENT ON COLUMN withdrawal_request.manual_note IS 'Ghi chú của admin khi chuyển khoản';
COMMENT ON COLUMN withdrawal_request.manual_proof_url IS 'URL ảnh chứng từ chuyển khoản';

-- Kiểm tra kết quả
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns 
WHERE table_name = 'withdrawal_request' 
AND column_name LIKE 'manual_%'
ORDER BY ordinal_position;
