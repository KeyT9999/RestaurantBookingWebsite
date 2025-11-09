# 🗑️ HƯỚNG DẪN XÓA NHÀ HÀNG "AVVVV"

## 📋 Tổng quan

Script này sẽ xóa nhà hàng có tên **"AVVVV"** và **TẤT CẢ** dữ liệu liên quan, bao gồm:

- ✅ Bookings và payments
- ✅ Tables, Dishes, Services, Media
- ✅ Reviews, Favorites, Vouchers
- ✅ Waitlists, Availability
- ✅ Restaurant balance, bank accounts
- ✅ Withdrawal requests, refund requests
- ✅ Chat rooms, AI interactions
- ✅ Và tất cả các bản ghi khác

⚠️ **CẢNH BÁO: Thao tác này KHÔNG THỂ HOÀN TÁC!**

---

## 🚀 CÁCH 1: Chạy SQL Script trực tiếp trong pgAdmin (Khuyên dùng)

1. Mở **pgAdmin**
2. Kết nối đến database `bookeat_db`
3. Mở **Query Tool** (chuột phải vào database → Query Tool)
4. Mở file `scripts/delete_restaurant_AVVVV.sql`
5. Copy toàn bộ nội dung
6. Paste vào Query Tool
7. Nhấn **F5** để chạy
8. Kiểm tra kết quả trong tab Messages

---

## 🚀 CÁCH 2: Chạy bằng Batch Script

1. Mở **PowerShell** hoặc **Command Prompt**
2. Di chuyển đến thư mục project:
   ```bash
   cd C:\Users\ASUS\Desktop\RestaurantBookingWebsite
   ```
3. Chạy script:
   ```bash
   scripts\delete_restaurant_AVVVV.bat
   ```
4. Làm theo hướng dẫn trên màn hình

**Lưu ý:** Cần có `psql` trong PATH hoặc cài đặt PostgreSQL client tools.

---

## 🚀 CÁCH 3: Chạy trực tiếp bằng psql

```bash
psql -h localhost -U postgres -d bookeat_db -f scripts/delete_restaurant_AVVVV.sql
```

---

## ✅ Kiểm tra kết quả

Sau khi chạy script, bạn sẽ thấy:
- Thông báo số lượng bản ghi đã xóa từng bảng
- Thông báo cuối cùng xác nhận nhà hàng đã được xóa
- Một câu query kiểm tra để xác minh

---

## 🔍 Kiểm tra thủ công

Nếu muốn kiểm tra xem nhà hàng còn tồn tại không:

```sql
SELECT restaurant_id, restaurant_name, owner_id 
FROM restaurant_profile 
WHERE restaurant_name = 'AVVVV';
```

Nếu không có kết quả trả về → Nhà hàng đã bị xóa thành công! ✅

---

## ❓ Xử lý lỗi

### Lỗi: "Không tìm thấy nhà hàng có tên 'AVVVV'"
- Kiểm tra lại tên nhà hàng trong database
- Có thể tên nhà hàng có khoảng trắng hoặc ký tự đặc biệt khác

### Lỗi: Foreign key constraint violation
- Script đã được thiết kế để xóa theo thứ tự đúng
- Nếu vẫn gặp lỗi, có thể có bảng mới chưa được thêm vào script
- Kiểm tra log để xem bảng nào gây lỗi

---

## 📝 Lưu ý

- Script sẽ xóa **TẤT CẢ** dữ liệu liên quan đến nhà hàng
- Không thể khôi phục sau khi xóa
- Nên backup database trước khi chạy script (nếu cần)
- Script an toàn và chỉ xóa nhà hàng có tên chính xác là "AVVVV"




