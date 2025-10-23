# Hướng dẫn cập nhật hệ thống chia tiền và hoàn tiền

## 🚨 **Lỗi đã sửa**

**Lỗi gốc:** `ERROR: column "restaurant_id" of relation "payout_audit_log" does not exist`

**Nguyên nhân:** Script `update_commission_to_30_percent.sql` sử dụng cấu trúc bảng `payout_audit_log` sai.

**Giải pháp:** Đã tạo script mới `update_commission_simple.sql` không sử dụng audit log.

## 📁 **Files đã tạo/sửa**

### **1. Scripts Database:**
- ✅ `database/update_commission_simple.sql` - Script cập nhật hoa hồng đơn giản
- ✅ `database/update_commission_to_30_percent.sql` - Script có audit log (đã sửa)
- ✅ `test_commission_update.sql` - Script test

### **2. Java Services:**
- ✅ `src/main/java/com/example/booking/service/EnhancedRefundService.java` - Logic hoàn tiền mới
- ✅ `src/main/java/com/example/booking/service/NotificationService.java` - Gửi thông báo
- ✅ `src/main/java/com/example/booking/service/RestaurantBalanceService.java` - Cập nhật hoa hồng 30%
- ✅ `src/main/java/com/example/booking/domain/RestaurantBalance.java` - Cập nhật default commission
- ✅ `src/main/java/com/example/booking/service/RefundService.java` - Tích hợp logic mới

### **3. Documentation:**
- ✅ `NEW_PAYMENT_SYSTEM_DOCUMENTATION.md` - Hướng dẫn chi tiết
- ✅ `test_new_payment_system.sql` - Test đầy đủ hệ thống

## 🚀 **Cách deploy**

### **Bước 1: Cập nhật Database**
```sql
-- Chạy script đơn giản (khuyến nghị)
\i database/update_commission_simple.sql

-- Hoặc chạy script có audit log (nếu muốn)
\i database/update_commission_to_30_percent.sql
```

### **Bước 2: Deploy Code**
1. Deploy các file Java đã cập nhật
2. Restart application
3. Kiểm tra logs

### **Bước 3: Test**
```sql
-- Test cập nhật hoa hồng
\i test_commission_update.sql

-- Test toàn bộ hệ thống
\i test_new_payment_system.sql
```

## 📊 **Kiểm tra kết quả**

### **1. Kiểm tra hoa hồng đã cập nhật:**
```sql
SELECT 
    restaurant_id,
    commission_rate,
    total_revenue,
    total_commission,
    available_balance
FROM restaurant_balance 
ORDER BY restaurant_id;
```

### **2. Kiểm tra logic hoàn tiền:**
- Tạo booking với deposit 100,000 VNĐ
- Admin nhận: 30,000 VNĐ (30%)
- Restaurant nhận: 70,000 VNĐ (70%)
- Khi hoàn tiền: Restaurant bị trừ 30,000 VNĐ

### **3. Kiểm tra số dư âm:**
- Restaurant có thể có số dư âm
- Không thể rút tiền khi số dư âm
- Cần booking mới để recovery

## ⚠️ **Lưu ý quan trọng**

### **1. Backup Database:**
```sql
-- Backup trước khi chạy script
pg_dump -h localhost -U username -d database_name > backup_before_commission_update.sql
```

### **2. Test trên môi trường dev trước:**
- Chạy script trên database test
- Verify kết quả
- Sau đó mới chạy trên production

### **3. Monitor sau khi deploy:**
- Kiểm tra logs application
- Monitor commission calculation
- Verify refund process

## 🔧 **Troubleshooting**

### **Lỗi thường gặp:**

1. **"Column does not exist"**
   - Sử dụng `update_commission_simple.sql` thay vì script có audit log

2. **"Commission rate not updated"**
   - Kiểm tra có records nào có `commission_rate = 7.50` không
   - Chạy lại script UPDATE

3. **"Available balance calculation wrong"**
   - Chạy lại phần recalculate trong script
   - Kiểm tra trigger database

### **Rollback nếu cần:**
```sql
-- Rollback về hoa hồng 7.5%
UPDATE restaurant_balance 
SET commission_rate = 7.50 
WHERE commission_rate = 30.00;

-- Recalculate balance
UPDATE restaurant_balance 
SET 
    total_commission = total_revenue * (commission_rate / 100),
    available_balance = total_revenue - (total_revenue * (commission_rate / 100)) - total_withdrawn - pending_withdrawal,
    last_calculated_at = now();
```

## 📞 **Support**

Nếu có vấn đề:
1. Kiểm tra logs trong application
2. Verify database schema
3. Test với script `test_commission_update.sql`
4. Check documentation trong `NEW_PAYMENT_SYSTEM_DOCUMENTATION.md`
