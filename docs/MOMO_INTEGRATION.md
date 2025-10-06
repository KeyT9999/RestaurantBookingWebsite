# MoMo Payment Integration Guide

## Tổng quan

Dự án đã được tích hợp thanh toán MoMo theo chuẩn API v2 của MoMo. Hệ thống hỗ trợ:

- Tạo giao dịch thanh toán MoMo
- Xử lý IPN (Instant Payment Notification)
- Fallback query mechanism
- Frontend polling để cập nhật trạng thái real-time

## Cấu hình

### 1. Environment Variables

Tạo file `.env` từ `env.example` và cấu hình:

```bash
# MoMo Configuration
MOMO_PARTNER_CODE=your_partner_code
MOMO_ACCESS_KEY=your_access_key
MOMO_SECRET_KEY=your_secret_key
MOMO_ENDPOINT=https://test-payment.momo.vn
MOMO_RETURN_URL=http://localhost:8081/payment/momo/return
MOMO_NOTIFY_URL=http://localhost:8081/payment/api/momo/ipn
```

### 2. Database Migration

Chạy migration để thêm các trường MoMo:

```sql
-- Chạy file database/add_momo_fields.sql
\i database/add_momo_fields.sql
```

## Luồng thanh toán

### 1. Tạo Payment
```
Customer → Booking → Payment Form → Chọn MoMo → Tạo Payment
```

### 2. MoMo Payment Flow
```
Payment → MoMo API → PayUrl → User Payment → IPN → Status Update
```

### 3. Frontend Flow
```
Payment Form → MoMo Redirect → Return Page → Polling → Result
```

## API Endpoints

### Payment Controller
- `GET /payment/{bookingId}` - Hiển thị form thanh toán
- `POST /payment/process` - Xử lý thanh toán
- `GET /payment/momo/create` - Tạo MoMo payment
- `GET /payment/momo/return` - Xử lý return từ MoMo
- `GET /payment/result/{paymentId}` - Hiển thị kết quả

### API Endpoints
- `GET /payment/api/status/{paymentId}` - Lấy trạng thái payment
- `GET /payment/api/history` - Lịch sử thanh toán
- `POST /payment/api/momo/ipn` - Webhook IPN từ MoMo
- `POST /payment/api/cancel/{paymentId}` - Hủy payment

## Database Schema

### Payment Table (Updated)
```sql
-- Các trường MoMo đã được thêm
momo_order_id       VARCHAR(64)
momo_request_id     VARCHAR(64)
momo_trans_id       VARCHAR(64)
momo_result_code    VARCHAR(10)
momo_message        VARCHAR(255)
pay_url             VARCHAR(500)
ipn_raw             JSONB
redirect_raw        JSONB
refunded_at         TIMESTAMPTZ
```

### Payment Status
- `PENDING` - Chờ thanh toán
- `PROCESSING` - Đang xử lý (MoMo)
- `COMPLETED` - Hoàn thành
- `FAILED` - Thất bại
- `REFUNDED` - Hoàn tiền
- `CANCELLED` - Đã hủy

## Testing

### 1. Test Environment
- Sử dụng MoMo Test Environment
- Endpoint: `https://test-payment.momo.vn`
- OTP mặc định: `000000`

### 2. Test Flow
1. Tạo booking
2. Chọn thanh toán MoMo
3. Redirect đến MoMo test page
4. Nhập OTP `000000`
5. Kiểm tra IPN callback
6. Xác nhận payment status

### 3. Test Cases
- ✅ Payment thành công
- ✅ Payment thất bại
- ✅ IPN callback
- ✅ Fallback query
- ✅ Frontend polling

## Security

### 1. Signature Verification
- HMAC-SHA256 cho tất cả API calls
- Verify signature trong IPN
- Validate partner code và amount

### 2. Data Protection
- Mask sensitive data trong logs
- Secure API endpoints
- Validate input data

## Monitoring

### 1. Logging
- Log tất cả MoMo API calls
- Log IPN processing
- Log payment status changes

### 2. Scheduled Tasks
- Query pending payments mỗi 30 giây
- Fallback mechanism cho missed IPN

## Troubleshooting

### 1. Common Issues
- **IPN không nhận được**: Kiểm tra notify URL và network
- **Signature invalid**: Kiểm tra secret key và query string
- **Payment stuck**: Sử dụng fallback query

### 2. Debug Steps
1. Kiểm tra logs
2. Verify configuration
3. Test với MoMo test environment
4. Check database payment records

## Production Deployment

### 1. Configuration
- Đổi endpoint sang production
- Cập nhật return URL và notify URL
- Sử dụng production credentials

### 2. Monitoring
- Setup monitoring cho payment flow
- Alert cho failed payments
- Track payment success rate

## Support

- MoMo Documentation: https://developers.momo.vn
- Test Instructions: https://developers.momo.vn/v3/docs/payment/onboarding/test-instructions
- API Reference: https://developers.momo.vn/v3/docs/payment/api/wallet/onetime
