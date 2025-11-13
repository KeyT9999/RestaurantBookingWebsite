# Hướng dẫn sửa lỗi Database

## Lỗi 1: Unable to find User with id

```
Lỗi khi tải dữ liệu: Unable to find com.example.booking.domain.User with id 7bc1aae0-f73e-43f5-954e-0986b8bc566c
```

**Nguyên nhân**: Trong bảng `restaurant_owner` có record có `user_id` trỏ đến UUID không tồn tại trong bảng `users`. Đây là lỗi dữ liệu không nhất quán (orphaned foreign key).

---

## Lỗi 2: Duplicate key value violates unique constraint

```
ERROR: duplicate key value violates unique constraint "restaurant_owner_user_id_key"
Key (user_id)=(ae9a0000-67a2-470e-a445-7d81b2bf75cf) already exists.
```

**Nguyên nhân**: `user_id` bạn đang cố gắng cập nhật đã được sử dụng bởi một `restaurant_owner` khác. Trong bảng `restaurant_owner` có constraint unique trên `user_id` (mỗi user chỉ có thể có 1 restaurant_owner).

## Cách sửa

### Bước 1: Kiểm tra dữ liệu lỗi

Chạy query sau để tìm tất cả các record bị lỗi:

```sql
-- Tìm restaurant_owner có user_id không tồn tại
SELECT 
    ro.owner_id,
    ro.user_id,
    ro.owner_name,
    'ORPHANED - User không tồn tại' AS status
FROM restaurant_owner ro
LEFT JOIN users u ON ro.user_id = u.id
WHERE u.id IS NULL;
```

### Bước 2: Chọn cách sửa

#### **Cách 1: Cập nhật user_id trỏ đến user hợp lệ (KHUYẾN NGHỊ)**

1. Tìm một user hợp lệ:
```sql
SELECT id, username, email 
FROM users 
WHERE role = 'RESTAURANT_OWNER' 
ORDER BY created_at DESC 
LIMIT 1;
```

2. Cập nhật user_id (thay `YOUR_VALID_USER_ID` bằng ID từ bước 1):
```sql
UPDATE restaurant_owner 
SET user_id = 'YOUR_VALID_USER_ID'
WHERE user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c';
```

#### **Cách 2: Xóa restaurant_owner bị lỗi (CHỈ DÙNG NẾU KHÔNG CÓ RESTAURANT NÀO)**

⚠️ **CẢNH BÁO**: Chỉ xóa nếu không có `restaurant_profile` nào liên kết với owner này.

1. Kiểm tra xem có restaurant nào liên kết không:
```sql
SELECT COUNT(*) as restaurant_count
FROM restaurant_profile rp
INNER JOIN restaurant_owner ro ON rp.owner_id = ro.owner_id
WHERE ro.user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c';
```

2. Nếu `restaurant_count = 0`, có thể xóa an toàn:
```sql
DELETE FROM restaurant_owner 
WHERE user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c';
```

#### **Cách 3: Tạo user mới (CHỈ DÙNG NẾU BIẾT THÔNG TIN USER)**

Nếu bạn biết thông tin user cần tạo:

```sql
INSERT INTO users (id, username, email, password, full_name, role, enabled, created_at, updated_at)
VALUES (
    '7bc1aae0-f73e-43f5-954e-0986b8bc566c',
    'owner_username',  -- Thay bằng username thực tế
    'owner@example.com',  -- Thay bằng email thực tế
    '$2a$10$...',  -- Thay bằng password đã hash (dùng BCrypt)
    'Owner Name',  -- Thay bằng tên thực tế
    'RESTAURANT_OWNER',
    true,
    NOW(),
    NOW()
);
```

### Bước 3: Xác minh sau khi sửa

```sql
-- Kiểm tra lại xem còn lỗi không
SELECT 
    COUNT(*) as orphaned_count
FROM restaurant_owner ro
LEFT JOIN users u ON ro.user_id = u.id
WHERE u.id IS NULL;
```

Nếu `orphaned_count = 0`, đã sửa xong! ✅

---

## Sửa lỗi Duplicate Key

### Bước 1: Kiểm tra user_id đang được sử dụng

```sql
-- Xem user_id đang được sử dụng bởi owner nào
SELECT 
    ro.owner_id,
    ro.user_id,
    ro.owner_name,
    u.username,
    u.email
FROM restaurant_owner ro
INNER JOIN users u ON ro.user_id = u.id
WHERE ro.user_id = 'ae9a0000-67a2-470e-a445-7d81b2bf75cf';
```

### Bước 2: Tìm user_id hợp lệ chưa được sử dụng

```sql
-- Tìm user RESTAURANT_OWNER chưa có restaurant_owner
SELECT 
    u.id,
    u.username,
    u.email,
    u.full_name
FROM users u
LEFT JOIN restaurant_owner ro ON u.id = ro.user_id
WHERE u.role = 'RESTAURANT_OWNER'
  AND ro.owner_id IS NULL
ORDER BY u.created_at DESC
LIMIT 1;
```

### Bước 3: Chọn cách sửa

#### **Cách 1: Cập nhật với user_id mới chưa được sử dụng (KHUYẾN NGHỊ)**

```sql
-- Sử dụng user_id từ bước 2 (thay YOUR_NEW_USER_ID)
UPDATE restaurant_owner 
SET user_id = 'YOUR_NEW_USER_ID'  -- Phải là user_id CHƯA được sử dụng
WHERE user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c';
```

#### **Cách 2: Xóa owner bị lỗi (chỉ nếu không có restaurant nào)**

```sql
-- Kiểm tra trước
SELECT COUNT(*) as restaurant_count
FROM restaurant_profile rp
INNER JOIN restaurant_owner ro ON rp.owner_id = ro.owner_id
WHERE ro.user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c';

-- Nếu = 0, xóa
DELETE FROM restaurant_owner 
WHERE user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c';
```

#### **Cách 3: Merge restaurants vào owner hợp lệ**

Nếu owner bị lỗi có restaurants, bạn có thể gộp vào owner đang dùng user_id hợp lệ:

```sql
-- 1. Kiểm tra restaurants của owner bị lỗi
SELECT rp.restaurant_id, rp.restaurant_name
FROM restaurant_profile rp
INNER JOIN restaurant_owner ro ON rp.owner_id = ro.owner_id
WHERE ro.user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c';

-- 2. Tìm owner_id hợp lệ (owner đang dùng user_id 'ae9a0000-67a2-470e-a445-7d81b2bf75cf')
SELECT owner_id FROM restaurant_owner 
WHERE user_id = 'ae9a0000-67a2-470e-a445-7d81b2bf75cf';

-- 3. Cập nhật restaurants để trỏ đến owner hợp lệ (thay YOUR_VALID_OWNER_ID)
UPDATE restaurant_profile
SET owner_id = 'YOUR_VALID_OWNER_ID'
WHERE owner_id IN (
    SELECT owner_id FROM restaurant_owner 
    WHERE user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c'
);

-- 4. Xóa owner bị lỗi sau khi merge
DELETE FROM restaurant_owner 
WHERE user_id = '7bc1aae0-f73e-43f5-954e-0986b8bc566c';
```

### Bước 4: Xác minh

```sql
-- Kiểm tra duplicate
SELECT user_id, COUNT(*) as count
FROM restaurant_owner
GROUP BY user_id
HAVING COUNT(*) > 1;
-- Nếu không có kết quả, đã sửa xong!
```

---

## File SQL đầy đủ

- **`fix_orphaned_restaurant_owner.sql`** - Script để sửa lỗi "Unable to find User"
- **`fix_duplicate_user_id.sql`** - Script để sửa lỗi "Duplicate key" (bao gồm giải pháp tự động)

## Lưu ý

- **Luôn backup database** trước khi chạy các query UPDATE hoặc DELETE
- Nên chạy các query kiểm tra trước để xem có bao nhiêu record bị ảnh hưởng
- Nếu có nhiều record bị lỗi, nên sửa từng record một và kiểm tra kỹ

