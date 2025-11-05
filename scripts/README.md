# 📚 HƯỚNG DẪN THÊM NHÀ HÀNG VÀ UPLOAD ẢNH

## 🎯 QUY TRÌNH 3 BƯỚC

### ✅ BƯỚC 1: Thêm nhà hàng vào database

**File:** `scripts/add_com_nieu_restaurant.sql`

1. Mở **pgAdmin**
2. Kết nối database `bookeat_db`
3. Mở **Query Tool**
4. Copy toàn bộ nội dung file `scripts/add_com_nieu_restaurant.sql`
5. Paste vào Query Tool
6. Chạy (F5)
7. **Ghi lại RESTAURANT_ID** từ kết quả (ví dụ: 45)

---

### ✅ BƯỚC 2: Upload ảnh lên Cloudinary

**File:** `scripts/upload.bat`

**LƯU Ý:** Script đã có sẵn Cloudinary credentials, không cần set thủ công!

1. Mở **PowerShell**
2. Di chuyển đến project folder:
   ```bash
   cd C:\Users\ASUS\Desktop\RestaurantBookingWebsite
   ```
3. Chạy script:
   ```bash
   scripts\upload.bat
   ```
4. Nhập **restaurant_id** khi script hỏi (số ID bạn đã ghi ở Bước 1)
5. Đợi upload xong → Sẽ có file `scripts/insert_images.sql`

---

### ✅ BƯỚC 3: Thêm URLs vào database

**File:** `scripts/insert_images.sql` (được tạo tự động sau Bước 2)

1. Mở **pgAdmin**
2. Kết nối database `bookeat_db`
3. Mở **Query Tool**
4. Mở file `scripts/insert_images.sql`
5. Copy toàn bộ nội dung
6. Paste vào Query Tool
7. Chạy (F5)
8. **Done!** ✅

---

## 🔍 KIỂM TRA ẢNH ĐÃ CÓ CHƯA

Chạy query này trong pgAdmin:

```sql
SELECT 
    r.restaurant_id,
    r.restaurant_name,
    COUNT(rm.media_id) as total_images
FROM restaurant_profile r
LEFT JOIN restaurant_media rm ON r.restaurant_id = rm.restaurant_id
WHERE r.restaurant_name LIKE '%Cơm niêu 3 Cá Bống%'
GROUP BY r.restaurant_id, r.restaurant_name;
```

**Kết quả:**
- `total_images = 9` → ✅ Đã có đủ 9 ảnh
- `total_images = 0` → ❌ Chưa có ảnh, cần làm Bước 2 và 3

---

## 📁 CÁC FILE QUAN TRỌNG

| File | Mô tả |
|------|-------|
| `add_com_nieu_restaurant.sql` | SQL script để thêm nhà hàng |
| `upload.bat` | Script upload ảnh (đã có sẵn credentials) |
| `upload_images_to_cloudinary.py` | Python script để upload ảnh |
| `insert_images.sql` | SQL script được tạo tự động (sau upload) |

---

## ⚠️ LƯU Ý QUAN TRỌNG

- ✅ **Cloudinary credentials đã được cấu hình sẵn** trong `upload.bat`, không cần set thủ công
- ✅ Phải có **Python 3** đã cài
- ✅ Phải **ghi lại restaurant_id** sau Bước 1
- ✅ File `insert_images.sql` chỉ được tạo sau khi upload thành công

---

## 🐛 TROUBLESHOOTING

### Lỗi: "Python not found"
→ Cài Python 3 từ https://www.python.org/

### Lỗi: "ModuleNotFoundError: No module named 'cloudinary'"
→ Chạy: `pip install cloudinary`

### Lỗi: "Restaurant với ID X không tồn tại"
→ Kiểm tra lại restaurant_id đã nhập đúng chưa

### Lỗi: "FATAL: password authentication failed for user 'postgres'"
→ **Password PostgreSQL không đúng!**

**Cách sửa:**
1. Mở file `.env` ở thư mục gốc project
2. Tìm dòng: `DB_PASSWORD=password`
3. Thay `password` bằng password PostgreSQL thực tế của bạn
4. Lưu file và chạy lại: `mvn spring-boot:run`

**Nếu không nhớ password:**
- Mở pgAdmin, kết nối đến PostgreSQL
- Nếu kết nối được → đó là password đúng
- Copy password đó vào `.env`

---

## 📞 CẦN HỖ TRỢ?

Xem lại phần **TROUBLESHOOTING** ở trên hoặc kiểm tra log Spring Boot để biết lỗi cụ thể.
