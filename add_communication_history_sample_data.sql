-- Add sample communication history data
-- This script adds sample communication history entries for existing bookings

-- Insert sample communication history entries
INSERT INTO communication_history (booking_id, type, content, direction, timestamp, author, status) VALUES
-- Booking ID 108 (existing booking)
(108, 'MESSAGE', 'Chào anh/chị, tôi đã nhận được thông tin đặt bàn của anh/chị. Chúng tôi sẽ chuẩn bị sẵn sàng cho buổi tối nay.', 'OUTGOING', '2025-10-23 15:05:00', 'admin', 'SENT'),
(108, 'CALL', 'Gọi điện xác nhận đặt bàn - khách hàng đã xác nhận sẽ đến đúng giờ', 'OUTGOING', '2025-10-23 15:30:00', 'admin', 'DELIVERED'),
(108, 'MESSAGE', 'Cảm ơn nhà hàng đã gọi điện xác nhận. Tôi sẽ đến đúng giờ.', 'INCOMING', '2025-10-23 15:35:00', 'customer', 'READ'),
(108, 'EMAIL', 'Gửi email thông báo thay đổi menu đặc biệt cho buổi tối', 'OUTGOING', '2025-10-23 16:00:00', 'admin', 'SENT'),

-- Booking ID 72 (existing booking)
(72, 'MESSAGE', 'Xin chào, tôi muốn đặt bàn cho 4 người vào tối nay', 'INCOMING', '2025-10-23 14:00:00', 'customer', 'READ'),
(72, 'MESSAGE', 'Chào anh/chị, chúng tôi có thể sắp xếp bàn cho 4 người vào 19:00 tối nay. Anh/chị có đồng ý không?', 'OUTGOING', '2025-10-23 14:05:00', 'admin', 'SENT'),
(72, 'CALL', 'Gọi điện thảo luận về menu đặc biệt', 'OUTGOING', '2025-10-23 14:15:00', 'admin', 'DELIVERED'),
(72, 'EMAIL', 'Gửi thông tin chi tiết về booking và menu', 'OUTGOING', '2025-10-23 14:20:00', 'admin', 'SENT'),

-- Booking ID 109 (if exists)
(109, 'MESSAGE', 'Tôi muốn hủy đặt bàn do có việc đột xuất', 'INCOMING', '2025-10-23 17:00:00', 'customer', 'READ'),
(109, 'MESSAGE', 'Chúng tôi hiểu và sẽ hủy đặt bàn cho anh/chị. Phí đặt cọc sẽ được hoàn lại trong 3-5 ngày làm việc.', 'OUTGOING', '2025-10-23 17:05:00', 'admin', 'SENT'),

-- Booking ID 110 (if exists)
(110, 'CALL', 'Gọi điện nhắc nhở khách hàng về booking sắp tới', 'OUTGOING', '2025-10-23 18:00:00', 'admin', 'DELIVERED'),
(110, 'MESSAGE', 'Cảm ơn nhà hàng đã nhắc nhở. Tôi sẽ đến đúng giờ.', 'INCOMING', '2025-10-23 18:05:00', 'customer', 'READ');

-- Verify the data was inserted
SELECT 
    ch.id,
    ch.booking_id,
    ch.type,
    ch.content,
    ch.direction,
    ch.timestamp,
    ch.author,
    ch.status
FROM communication_history ch
ORDER BY ch.booking_id, ch.timestamp DESC;
