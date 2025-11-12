# Restaurant Scripts

This folder contains SQL and helper scripts to create restaurants, attach owners, and upload image galleries.

## The Grill - Hòa Hải, Ngũ Hành Sơn, Đà Nẵng

### Cách chạy bằng Terminal (Windows PowerShell) - DÀNH CHO 1 NHÀ HÀNG (The Grill)

1) Export thông số kết nối PostgreSQL (đổi nếu khác):

```powershell
$env:PGHOST="localhost"; $env:PGPORT="5432"; $env:PGUSER="postgres"; $env:PGPASSWORD="password"; $env:PGDATABASE="bookeat_db"
```

2) Bật extension cần thiết:

```powershell
psql -v ON_ERROR_STOP=1 -c "CREATE EXTENSION IF NOT EXISTS pgcrypto;"
```

3) Chạy playbook 1-file (gợi ý nhanh nhất):

```powershell
psql -v ON_ERROR_STOP=1 -f "scripts\add_the_grill_complete.sql"
```

Sau bước này PART 1 sẽ tạo nhà hàng; PART 3 sẽ thêm món/bàn/dịch vụ. Ảnh sẽ chèn ở PART 2 sau khi upload.

4) Upload ảnh (cần Python + cloudinary):

```powershell
py -m pip install cloudinary
python scripts\upload_the_grill.py (psql -Atc "SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name='The Grill' ORDER BY created_at DESC LIMIT 1;") "Media_update\The Grill - Hoà Hải, Ngũ Hành Sơn, Đà Nẵng"
```

Script sẽ tạo `scripts\insert_the_grill_images.sql`.

5) Chèn ảnh vào DB:

```powershell
psql -v ON_ERROR_STOP=1 -f "scripts\insert_the_grill_images.sql"
```

6) Kiểm tra nhanh:

```powershell
psql -Atc "SELECT restaurant_id, restaurant_name, approval_status FROM restaurant_profile WHERE restaurant_name='The Grill' ORDER BY created_at DESC LIMIT 1;"
psql -Atc "SELECT COUNT(*) FROM dish WHERE restaurant_id=(SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name='The Grill' ORDER BY created_at DESC LIMIT 1);"
psql -Atc "SELECT COUNT(*) FROM restaurant_table WHERE restaurant_id=(SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name='The Grill' ORDER BY created_at DESC LIMIT 1);"
psql -Atc "SELECT COUNT(*) FROM restaurant_service WHERE restaurant_id=(SELECT restaurant_id FROM restaurant_profile WHERE restaurant_name='The Grill' ORDER BY created_at DESC LIMIT 1);"
```


### Chạy từng file trong pgAdmin (tùy chọn)

1) Create the restaurant profile (owner = user `Taiphan`):

```bash
# In pgAdmin or psql
\i scripts/add_the_grill_restaurant.sql
```

This script:
- Resolves the existing user `Taiphan`
- Ensures a `restaurant_owner` row for that user (creates if missing)
- Inserts a new row into `restaurant_profile` and APPROVES it
- Prints the new `restaurant_id`

2) Upload images to Cloudinary and generate SQL to insert them:

```powershell
# From the project root on Windows PowerShell
python scripts/upload_the_grill.py <RESTAURANT_ID> "Media_update/The Grill - Hoà Hải, Ngũ Hành Sơn, Đà Nẵng"
```

This will:
- Upload all `.webp`, `.jpg`, `.jpeg`, `.png` files in the folder to Cloudinary
- Create `scripts/insert_the_grill_images.sql`

3) Insert the image URLs:

```bash
# In pgAdmin or psql
\i scripts/insert_the_grill_images.sql
```

Notes:
- If `Taiphan` does not exist in table `users`, create that account first or adjust the username in the SQL.
- The uploader expects Cloudinary credentials via environment variables: `CLOUDINARY_URL` or the tuple `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`.


