# Hệ thống chia tiền và hoàn tiền mới

## 📋 Tổng quan

Hệ thống đã được cập nhật để thực hiện logic chia tiền và hoàn tiền theo yêu cầu:

### **Chia tiền khi booking hoàn thành:**
- **Admin nhận**: 30% hoa hồng từ tiền đặt cọc
- **Restaurant nhận**: 70% còn lại vào ví nhà hàng

### **Hoàn tiền khi customer hủy booking:**
- **Hệ thống thông báo**: "Tiền của bạn sẽ về tài khoản trong 1-3 ngày"
- **Restaurant bị trừ**: 30% tiền đặt cọc từ ví nhà hàng
- **Admin chuyển tiền**: Toàn bộ tiền đặt cọc cho khách hàng
- **Cho phép số dư âm**: Nếu nhà hàng không đủ tiền, sẽ trừ âm và cộng trừ sau

## 🔧 Các thay đổi đã thực hiện

### 1. **Cập nhật tỷ lệ hoa hồng**
- Thay đổi từ 7.5% thành 30%
- Cập nhật trong `RestaurantBalanceService.java` và `RestaurantBalance.java`
- Script database: `database/update_commission_to_30_percent.sql`

### 2. **Tạo EnhancedRefundService**
- Logic hoàn tiền mới với trừ hoa hồng từ ví nhà hàng
- Cho phép số dư âm
- Thông báo khách hàng về thời gian hoàn tiền

### 3. **Tạo NotificationService**
- Gửi thông báo cho khách hàng về hoàn tiền
- Template thông báo "1-3 ngày làm việc"

### 4. **Cập nhật RefundService**
- Sử dụng `EnhancedRefundService` cho logic mới
- Giữ nguyên interface để không ảnh hưởng code hiện tại

## 📊 Luồng hoạt động

### **Khi booking hoàn thành:**
```
Customer đặt cọc 100,000 VNĐ
    ↓
Admin nhận: 30,000 VNĐ (30%)
    ↓
Restaurant nhận: 70,000 VNĐ (70%)
```

### **Khi customer hủy booking:**
```
Customer hủy booking
    ↓
Hệ thống thông báo: "Tiền sẽ về trong 1-3 ngày"
    ↓
Restaurant bị trừ: 30,000 VNĐ từ ví
    ↓
Admin chuyển: 100,000 VNĐ cho customer
    ↓
Restaurant có thể có số dư âm (OK)
```

### **Khi restaurant rút tiền:**
```
Restaurant yêu cầu rút tiền
    ↓
Kiểm tra số dư khả dụng
    ↓
Admin duyệt và chuyển tiền
    ↓
Cập nhật trạng thái withdrawal
```

## 🗄️ Database Schema

### **restaurant_balance table:**
- `commission_rate`: 30.00 (thay vì 7.50)
- `available_balance`: Có thể âm
- `total_commission`: Tổng hoa hồng đã tính

### **payout_audit_log table:**
- Log tất cả giao dịch hoa hồng và hoàn tiền
- Tracking commission deduction

## 🧪 Testing

### **Script test:**
- `test_new_payment_system.sql`: Test đầy đủ các scenario
- Test booking hoàn thành
- Test hoàn tiền với trừ hoa hồng
- Test số dư âm
- Test recovery từ số dư âm

### **Test cases:**
1. ✅ Booking 100k → Admin 30k, Restaurant 70k
2. ✅ Refund 100k → Restaurant trừ 30k, Customer nhận 100k
3. ✅ Restaurant số dư âm (-10k) → OK, cộng trừ sau
4. ✅ Restaurant rút tiền → Kiểm tra số dư khả dụng
5. ✅ Thông báo customer về thời gian hoàn tiền

## 🚀 Deployment Steps

### **1. Cập nhật database:**
```sql
-- Chạy script cập nhật hoa hồng
\i database/update_commission_to_30_percent.sql
```

### **2. Deploy code:**
- Deploy các file Java đã cập nhật
- Restart application

### **3. Test hệ thống:**
```sql
-- Chạy script test
\i test_new_payment_system.sql
```

## 📱 API Endpoints

### **Hoàn tiền:**
```
POST /api/payments/{paymentId}/refund
{
  "refundAmount": 100000,
  "reason": "Customer cancellation"
}
```

### **Kiểm tra số dư nhà hàng:**
```
GET /api/restaurants/{restaurantId}/balance
```

### **Rút tiền:**
```
POST /api/restaurants/{restaurantId}/withdrawals
{
  "amount": 50000,
  "bankAccountId": 1,
  "description": "Monthly withdrawal"
}
```

## ⚠️ Lưu ý quan trọng

### **1. Số dư âm:**
- Hệ thống cho phép restaurant có số dư âm
- Restaurant không thể rút tiền khi số dư âm
- Cần có booking mới để recovery

### **2. Thông báo khách hàng:**
- Tất cả hoàn tiền đều thông báo "1-3 ngày"
- Có thể customize thời gian trong NotificationService

### **3. Audit trail:**
- Tất cả giao dịch được log trong payout_audit_log
- Có thể track commission deduction

### **4. Backward compatibility:**
- RefundService giữ nguyên interface
- Không ảnh hưởng code hiện tại

## 🔍 Monitoring

### **Metrics cần theo dõi:**
- Tổng hoa hồng admin thu được
- Số restaurant có số dư âm
- Thời gian xử lý hoàn tiền
- Tỷ lệ hoàn tiền thành công

### **Alerts:**
- Restaurant có số dư âm quá lâu
- Hoàn tiền thất bại
- Commission calculation sai

## 📞 Support

Nếu có vấn đề với hệ thống mới:
1. Kiểm tra logs trong `payout_audit_log`
2. Verify commission rate = 30%
3. Check restaurant balance calculation
4. Test với script `test_new_payment_system.sql`
