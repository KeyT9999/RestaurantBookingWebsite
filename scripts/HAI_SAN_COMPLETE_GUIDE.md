# 🦐 HƯỚNG DẪN THÊM NHÀ HÀNG "Hải Sản Bà Cường – Hoàng Sa"

## 📋 QUY TRÌNH 2 BƯỚC

### ✅ BƯỚC 1: Upload ảnh lên Cloudinary

**File:** `scripts/upload_hai_san_images.py`

**Cách 1: Chạy với restaurant_id (Khuyến nghị)**
1. Mở **PowerShell**
2. Di chuyển đến project folder:
   ```bash
   cd C:\Users\ASUS\Desktop\RestaurantBookingWebsite
   ```
3. **Chạy PHẦN 1** của file SQL trước để lấy `restaurant_id`
4. Chạy Python script với `restaurant_id`:
   ```bash
   python scripts/upload_hai_san_images.py 44
   ```
   Ví dụ: `python scripts/upload_hai_san_images.py 46`
5. Đợi upload xong → File `scripts/insert_hai_san_images.sql` sẽ được tạo

**Cách 2: Chạy và nhập ID khi được hỏi**
1. Chạy: `python scripts/upload_hai_san_images.py`
2. Nhập `restaurant_id` khi script hỏi
3. Đợi upload xong

---

### ✅ BƯỚC 2: Chạy SQL script đầy đủ

**File:** `scripts/add_hai_san_complete.sql`

1. Mở **pgAdmin** → Query Tool
2. Mở file `scripts/add_hai_san_complete.sql`
3. **Chạy PHẦN 1** (Thêm nhà hàng) → Ghi lại `restaurant_id`
4. **Nếu chưa upload ảnh:**
   - Quay lại Bước 1, chạy Python script với `restaurant_id` vừa lấy
   - Sau khi upload xong, mở file `scripts/insert_hai_san_images.sql`
   - Copy phần INSERT ảnh và paste vào **PHẦN 2** của file SQL chính
5. **Hoặc chạy trực tiếp file `insert_hai_san_images.sql`** (nếu đã upload)
6. **Chạy PHẦN 3** (Thêm bàn, món ăn, dịch vụ)
7. **Chạy PHẦN 4** (Cập nhật giá → 50.000 VNĐ)
8. **Chạy PHẦN 5** (Approve nhà hàng và fix status)
9. **Chạy PHẦN 6** (Verification - Kiểm tra kết quả)

---

## 📝 LƯU Ý QUAN TRỌNG

- ✅ **Cloudinary credentials** đã được hardcode trong Python script
- ✅ Tổng cộng: **12 ảnh** (1 cover + 11 gallery)
- ✅ Số món ăn: **12 món** (bằng số ảnh gallery)
- ✅ Tất cả giá: **50.000 VNĐ**
- ✅ Nhà hàng sẽ được **APPROVED** tự động sau khi chạy PHẦN 5

---

## 🔍 KIỂM TRA KẾT QUẢ

Sau khi chạy xong, kiểm tra trong pgAdmin:

```sql
SELECT 
    'BÀN' as loai, COUNT(*) as so_luong
FROM restaurant_table
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Hải Sản Bà Cường%' LIMIT 1)

UNION ALL

SELECT 
    'MÓN ĂN' as loai, COUNT(*) as so_luong
FROM dish
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Hải Sản Bà Cường%' LIMIT 1)

UNION ALL

SELECT 
    'DỊCH VỤ' as loai, COUNT(*) as so_luong
FROM restaurant_service
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Hải Sản Bà Cường%' LIMIT 1);
    'GALLERY' as
```

**Kết quả mong đợi:**
- BÀN: 10
- MÓN ĂN: 12
- DỊCH VỤ: 3
- ẢNH GALLERY: 11

---

## 🐛 TROUBLESHOOTING

### Lỗi: "Python not found"
→ Cài Python 3 từ https://www.python.org/

### Lỗi: "ModuleNotFoundError: No module named 'cloudinary'"
→ Chạy: `pip install cloudinary`

### Lỗi: "Restaurant với ID X không tồn tại"
→ Kiểm tra lại restaurant_id đã nhập đúng chưa

### Menu không hiển thị sau khi chạy xong
→ Restart Spring Boot application và refresh trang booking


