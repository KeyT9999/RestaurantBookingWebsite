# 🚀 HƯỚNG DẪN THÊM TẤT CẢ 7 NHÀ HÀNG MỘT LÚC

## 📋 QUY TRÌNH 2 BƯỚC

### ✅ BƯỚC 1: Upload ảnh lên Cloudinary

**File:** `scripts/upload_all_restaurants_images.py`

**Cách chạy:**
1. Mở **PowerShell**
2. Di chuyển đến project folder:
   ```bash
   cd C:\Users\ASUS\Desktop\RestaurantBookingWebsite
   ```
3. **Chạy PHẦN 1** của file SQL trước để tạo tất cả nhà hàng (sẽ tạo IDs 45-51)
4. Chạy Python script với `restaurant_id` bắt đầu:
   ```bash
   python scripts/upload_all_restaurants_images.py 45
   ```
   (45 = ID của nhà hàng đầu tiên, các nhà hàng tiếp theo sẽ là 46, 47, 48, 49, 50, 51)
5. Đợi upload xong → File `scripts/insert_all_restaurants_images.sql` sẽ được tạo

**Lưu ý:** Script sẽ tự động upload ảnh cho tất cả 7 nhà hàng theo thứ tự:
- Hải Sản Ngọc Hương (ID: 45)
- Akataiyo (ID: 46)
- Phố Biển (ID: 47)
- The Anchor (ID: 48)
- Vietbamboo (ID: 49)
- Vườn Nướng (ID: 50)
- Zzuggubbong (ID: 51)

---

### ✅ BƯỚC 2: Chạy SQL script đầy đủ

**File:** `scripts/add_all_restaurants_complete.sql`

1. Mở **pgAdmin** → Query Tool
2. Mở file `scripts/add_all_restaurants_complete.sql`
3. **Chạy PHẦN 1** (Thêm tất cả 7 nhà hàng) → Ghi lại các `restaurant_id` (45-51)
4. **Nếu chưa upload ảnh:**
   - Quay lại Bước 1, chạy Python script với `restaurant_id` bắt đầu = 45
   - Sau khi upload xong, mở file `scripts/insert_all_restaurants_images.sql`
   - Copy phần INSERT ảnh và paste vào **PHẦN 2** của file SQL chính
5. **Hoặc chạy trực tiếp file `insert_all_restaurants_images.sql`** (nếu đã upload)
6. **Chạy PHẦN 3** (Thêm bàn, món ăn, dịch vụ cho tất cả nhà hàng)
7. **Chạy PHẦN 4** (Cập nhật giá → 50.000 VNĐ cho tất cả)
8. **Chạy PHẦN 5** (Approve tất cả nhà hàng và fix status)
9. **Chạy PHẦN 6** (Verification - Kiểm tra kết quả)

---

## 📝 THÔNG TIN CÁC NHÀ HÀNG

| # | Nhà hàng | ID | Số ảnh | Số món |
|---|----------|-----|--------|--------|
| 1 | Hải Sản Ngọc Hương – Võ Nguyên Giáp | 45 | 9 | 9 |
| 2 | Nhà hàng Akataiyo Mặt Trời Đỏ - Nguyễn Du | 46 | 6 | 6 |
| 3 | Phố Biển – Đảo Xanh | 47 | 9 | 9 |
| 4 | The Anchor (Restaurant & Bierhaus) - Trần Phú | 48 | 11 | 11 |
| 5 | Vietbamboo Restaurant - Phạm Văn Đồng | 49 | 8 | 8 |
| 6 | Vườn Nướng - Đường 304 | 50 | 10 | 10 |
| 7 | Zzuggubbong - Nguyễn Hữu Thông | 51 | 11 | 11 |

**Tổng cộng:**
- 7 nhà hàng
- 64 ảnh (7 cover + 57 gallery)
- 64 món ăn (bằng số ảnh gallery)
- 70 bàn (10 bàn × 7 nhà hàng)
- 21 dịch vụ (3 dịch vụ × 7 nhà hàng)

---

## 📝 LƯU Ý QUAN TRỌNG

- ✅ **Cloudinary credentials** đã được hardcode trong Python script
- ✅ Tất cả giá: **50.000 VNĐ**
- ✅ Tất cả nhà hàng sẽ được **APPROVED** tự động sau khi chạy PHẦN 5
- ✅ Số món ăn = số ảnh gallery (tự động gán ảnh cho món ăn)

---

## 🔍 KIỂM TRA KẾT QUẢ

Sau khi chạy xong, kiểm tra trong pgAdmin:

```sql
SELECT 
    'Hải Sản Ngọc Hương' as nha_hang, 'BÀN' as loai, COUNT(*) as so_luong 
FROM restaurant_table 
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Hải Sản Ngọc Hương%' LIMIT 1)
UNION ALL
SELECT 'Hải Sản Ngọc Hương', 'MÓN ĂN', COUNT(*) 
FROM dish 
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Hải Sản Ngọc Hương%' LIMIT 1)
UNION ALL
SELECT 'Hải Sản Ngọc Hương', 'DỊCH VỤ', COUNT(*) 
FROM restaurant_service 
WHERE restaurant_id = (SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name LIKE '%Hải Sản Ngọc Hương%' LIMIT 1);
```

**Kết quả mong đợi cho mỗi nhà hàng:**
- BÀN: 10
- MÓN ĂN: (theo số ảnh gallery, xem bảng trên)
- DỊCH VỤ: 3

---

## 🐛 TROUBLESHOOTING

### Lỗi: "Python not found"
→ Cài Python 3 từ https://www.python.org/

### Lỗi: "ModuleNotFoundError: No module named 'cloudinary'"
→ Chạy: `pip install cloudinary`

### Lỗi: "Restaurant với ID X không tồn tại"
→ Kiểm tra lại restaurant_id đã nhập đúng chưa. Đảm bảo đã chạy PHẦN 1 của SQL trước.

### Menu không hiển thị sau khi chạy xong
→ Restart Spring Boot application và refresh trang booking

---

## ✅ TÓM TẮT NHANH

1. **Chạy PHẦN 1 SQL** → Tạo 7 nhà hàng (IDs: 45-51)
2. **Chạy Python script:** `python scripts/upload_all_restaurants_images.py 45`
3. **Chạy file `insert_all_restaurants_images.sql`** (được tạo tự động)
4. **Chạy các PHẦN còn lại** trong file SQL (3, 4, 5, 6)
5. **Done!** ✅


