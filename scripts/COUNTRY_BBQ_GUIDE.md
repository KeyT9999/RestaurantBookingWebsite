# 🍖 Country BBQ & Beer - Trần Bạch Đằng

## 📋 QUY TRÌNH 2 BƯỚC

### ✅ BƯỚC 1: Thêm nhà hàng vào database

**File:** `scripts/add_country_bbq_restaurant.sql`

1. Mở **pgAdmin** → Query Tool
2. Copy toàn bộ nội dung file `add_country_bbq_restaurant.sql`
3. Paste và chạy (F5)
4. **Ghi lại RESTAURANT_ID** từ kết quả

---

### ✅ BƯỚC 2: Upload ảnh và tạo SQL

**File:** `scripts/upload_country_bbq_images.py`

1. Chạy trong PowerShell:
   ```bash
   python scripts/upload_country_bbq_images.py
   ```
2. Nhập **restaurant_id** (từ Bước 1)
3. Đợi upload xong → File `scripts/insert_country_bbq_images.sql` sẽ được tạo
4. Mở file `insert_country_bbq_images.sql` trong pgAdmin
5. Copy toàn bộ và chạy (F5)
6. **Done!** ✅

---

## 📝 LƯU Ý

- Cloudinary credentials đã được hardcode trong script
- Ảnh đầu tiên → `cover`, 7 ảnh còn lại → `gallery`
- Tổng cộng: **8 ảnh**

