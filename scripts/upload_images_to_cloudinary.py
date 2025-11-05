#!/usr/bin/env python3
"""
Script để upload 9 ảnh nhà hàng lên Cloudinary và tạo SQL script
Cách sử dụng: python scripts/upload_images_to_cloudinary.py
"""

import os
import sys
from pathlib import Path
from cloudinary import uploader
import cloudinary

# Hàm load .env file nếu có
def load_env_file():
    """Load environment variables from .env file if exists"""
    env_file = Path(__file__).parent.parent / '.env'
    if env_file.exists():
        print(f"📁 Found .env file: {env_file}")
        with open(env_file, 'r', encoding='utf-8') as f:
            for line in f:
                line = line.strip()
                if line and not line.startswith('#') and '=' in line:
                    key, value = line.split('=', 1)
                    key = key.strip()
                    value = value.strip().strip('"').strip("'")
                    # Chỉ set nếu chưa có trong environment
                    if key.startswith('CLOUDINARY_') and not os.getenv(key):
                        os.environ[key] = value
                        print(f"  ✅ Loaded: {key}")
        print()
    else:
        # Thử load từ env.example để hướng dẫn
        env_example = Path(__file__).parent.parent / 'env.example'
        if env_example.exists():
            print(f"💡 Tip: Create .env file from {env_example.name}")
            print()

# Load .env file trước
load_env_file()

# Cấu hình Cloudinary từ environment variables
cloudinary.config(
    cloud_name=os.getenv('CLOUDINARY_CLOUD_NAME'),
    api_key=os.getenv('CLOUDINARY_API_KEY'),
    api_secret=os.getenv('CLOUDINARY_API_SECRET'),
    secure=True
)

# Đường dẫn folder ảnh
IMAGE_FOLDER = r"C:\Users\ASUS\Desktop\RestaurantBookingWebsite\Media_update\Cơm niêu 3 Cá Bống – Nguyễn Tri Phương"

# Danh sách file ảnh theo thứ tự
IMAGE_FILES = [
    "nha-hang-com-nieu-3-ca-bong-nguyen-tri-phuong-1-normal-503616729882.webp",
    "nha-hang-com-nieu-3-ca-bong-nguyen-tri-phuong-2-normal-503617429883.webp",
    "nha-hang-com-nieu-3-ca-bong-nguyen-tri-phuong-4-normal-503617829885.webp",
    "nha-hang-com-nieu-3-ca-bong-nguyen-tri-phuong-6-normal-503619229887.webp",
    "nha-hang-com-nieu-3-ca-bong-nguyen-tri-phuong-7-normal-503619929888.webp",
    "nha-hang-com-nieu-3-ca-bong-nguyen-tri-phuong-8-normal-503620429889.webp",
    "nha-hang-com-nieu-3-ca-bong-nguyen-tri-phuong-9-normal-503620829890.webp",
    "nha-hang-com-nieu-3-ca-bong-nguyen-tri-phuong-10-normal-503621329891.webp",
    "nha-hang-com-nieu-3-ca-bong-nguyen-tri-phuong-12-normal-503622729893.webp"
]

def upload_image(file_path, restaurant_id, media_type, index):
    """Upload một ảnh lên Cloudinary và trả về URL"""
    try:
        folder = f"restaurants/{restaurant_id}/media/{media_type}"
        public_id = f"{media_type}_{index}_{int(os.path.getmtime(file_path))}"
        
        print(f"  📤 Uploading: {os.path.basename(file_path)}...")
        
        result = uploader.upload(
            file_path,
            folder=folder,
            public_id=public_id,
            use_filename=False,
            unique_filename=True,
            overwrite=False,
            resource_type="image",
            transformation=[
                {"width": 1200, "height": 800, "crop": "fill", "quality": "auto:good"}
            ]
        )
        
        image_url = result.get('secure_url')
        print(f"  ✅ Done: {image_url[:80]}...")
        return image_url
        
    except Exception as e:
        print(f"  ❌ ERROR: {str(e)}")
        return None

def generate_sql_script(urls, restaurant_id):
    """Tạo SQL script với URLs đã upload"""
    
    # Filter URLs hợp lệ
    valid_urls = [url for url in urls if url is not None]
    
    if not valid_urls:
        return None
    
    sql_content = f"""-- =====================================================
-- SQL Script: Thêm ảnh cho nhà hàng "Cơm niêu 3 Cá Bống"
-- Restaurant ID: {restaurant_id}
-- URLs đã được upload tự động từ Cloudinary
-- Số lượng ảnh: {len(valid_urls)}
-- =====================================================

DO $$
DECLARE
    v_restaurant_id INTEGER := {restaurant_id};
    v_image_count INTEGER;
BEGIN
    -- Kiểm tra restaurant có tồn tại không
    IF NOT EXISTS (SELECT 1 FROM restaurant_profile WHERE restaurant_id = v_restaurant_id) THEN
        RAISE EXCEPTION 'Restaurant với ID % không tồn tại! Hãy kiểm tra lại restaurant_id.', v_restaurant_id;
    END IF;
    
    -- Kiểm tra xem đã có ảnh chưa
    SELECT COUNT(*) INTO v_image_count
    FROM restaurant_media
    WHERE restaurant_id = v_restaurant_id;
    
    IF v_image_count > 0 THEN
        RAISE NOTICE '⚠️  Đã có % ảnh cho nhà hàng này. Tiếp tục thêm ảnh mới...', v_image_count;
    END IF;
    
    -- COVER IMAGE (ảnh đầu tiên)
"""
    
    # Cover image (ảnh đầu tiên)
    if urls[0]:
        sql_content += f"""    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES ({restaurant_id}, 'cover', '{urls[0]}', NOW());
    
"""
    
    # Gallery images (8 ảnh còn lại)
    sql_content += """    -- GALLERY IMAGES (8 ảnh còn lại)
"""
    
    for i, url in enumerate(urls[1:], start=2):
        if url:
            sql_content += f"""    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES ({restaurant_id}, 'gallery', '{url}', NOW());  -- Ảnh {i}
    
"""
    
    sql_content += f"""    -- Đếm tổng số ảnh sau khi insert
    SELECT COUNT(*) INTO v_image_count
    FROM restaurant_media
    WHERE restaurant_id = v_restaurant_id;
    
    RAISE NOTICE '========================================';
    RAISE NOTICE '✅ IMAGES ADDED SUCCESSFULLY!';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Restaurant ID: %', v_restaurant_id;
    RAISE NOTICE 'Total images after insert: %', v_image_count;
    RAISE NOTICE '========================================';
    
END $$;

-- =====================================================
-- VERIFICATION - Kiểm tra ảnh đã được thêm
-- =====================================================
SELECT 
    rm.media_id,
    rm.type,
    rm.url,
    r.restaurant_name
FROM restaurant_media rm
JOIN restaurant_profile r ON rm.restaurant_id = r.restaurant_id
WHERE r.restaurant_id = {restaurant_id}
ORDER BY 
    CASE rm.type 
        WHEN 'cover' THEN 1 
        WHEN 'gallery' THEN 2 
        ELSE 3 
    END,
    rm.media_id;
"""
    
    return sql_content

def main():
    # Kiểm tra credentials
    if not cloudinary.config().cloud_name:
        print("="*60)
        print("❌ ERROR: Cloudinary credentials not found!")
        print("="*60)
        print()
        print("Vui lòng set environment variables:")
        print()
        print("PowerShell:")
        print('  $env:CLOUDINARY_CLOUD_NAME="your_cloud_name"')
        print('  $env:CLOUDINARY_API_KEY="your_api_key"')
        print('  $env:CLOUDINARY_API_SECRET="your_api_secret"')
        print()
        print("CMD:")
        print('  set CLOUDINARY_CLOUD_NAME=your_cloud_name')
        print('  set CLOUDINARY_API_KEY=your_api_key')
        print('  set CLOUDINARY_API_SECRET=your_api_secret')
        print()
        sys.exit(1)
    
    # Kiểm tra folder tồn tại
    if not os.path.exists(IMAGE_FOLDER):
        print("="*60)
        print(f"❌ ERROR: Folder not found!")
        print("="*60)
        print(f"Folder: {IMAGE_FOLDER}")
        print()
        print("Vui lòng kiểm tra đường dẫn folder ảnh.")
        sys.exit(1)
    
    print("="*60)
    print("UPLOAD ẢNH NHÀ HÀNG LÊN CLOUDINARY")
    print("="*60)
    print(f"Folder: {IMAGE_FOLDER}")
    print()
    
    # Nhập restaurant_id
    print("="*60)
    print("NHẬP RESTAURANT_ID")
    print("="*60)
    print()
    print("📌 Để lấy restaurant_id, chạy query sau trong pgAdmin:")
    print()
    print("   SELECT restaurant_id, restaurant_name")
    print("   FROM restaurant_profile")
    print("   WHERE restaurant_name LIKE '%Cơm niêu 3 Cá Bống%';")
    print()
    print("-" * 60)
    
    while True:
        restaurant_id = input("Nhập restaurant_id (ví dụ: 45, 46, 47...): ").strip()
        
        if not restaurant_id:
            print()
            print("⚠️  Bạn chưa nhập ID!")
            print("   Vui lòng chạy query trên để lấy restaurant_id")
            print()
            continue
        
        try:
            restaurant_id = int(restaurant_id)
            break
        except ValueError:
            print()
            print("❌ ID không hợp lệ! Vui lòng nhập số (ví dụ: 45)")
            print()
    
    print()
    print(f"✅ Sử dụng restaurant_id: {restaurant_id}")
    print()
    
    print("="*60)
    print("UPLOADING IMAGES...")
    print("="*60)
    print()
    
    uploaded_urls = []
    
    for i, filename in enumerate(IMAGE_FILES):
        file_path = os.path.join(IMAGE_FOLDER, filename)
        
        if not os.path.exists(file_path):
            print(f"⚠️  File not found: {filename}")
            uploaded_urls.append(None)
            continue
        
        # Ảnh đầu tiên là cover, còn lại là gallery
        media_type = "cover" if i == 0 else "gallery"
        print(f"[{i+1}/9] Type: {media_type}")
        
        url = upload_image(file_path, restaurant_id, media_type, i)
        uploaded_urls.append(url)
        print()
    
    # Summary
    success_count = sum(1 for url in uploaded_urls if url is not None)
    print("="*60)
    print("UPLOAD SUMMARY")
    print("="*60)
    print(f"✅ Uploaded successfully: {success_count}/{len(IMAGE_FILES)}")
    print()
    
    if success_count == 0:
        print("❌ ERROR: No images uploaded successfully!")
        print("   Vui lòng kiểm tra Cloudinary credentials và thử lại.")
        sys.exit(1)
    
    # Generate SQL script
    print("="*60)
    print("GENERATING SQL SCRIPT...")
    print("="*60)
    print()
    
    sql_content = generate_sql_script(uploaded_urls, restaurant_id)
    
    if not sql_content:
        print("❌ ERROR: Cannot generate SQL script (no valid URLs)")
        sys.exit(1)
    
    # Save SQL script
    output_file = "scripts/insert_images.sql"
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(sql_content)
    
    print(f"✅ SQL script saved to: {output_file}")
    print()
    
    # Save URLs backup
    urls_file = "scripts/uploaded_urls.txt"
    with open(urls_file, 'w', encoding='utf-8') as f:
        f.write(f"=== UPLOADED IMAGE URLs ===\n\n")
        f.write(f"Restaurant ID: {restaurant_id}\n\n")
        f.write("COVER IMAGE:\n")
        if uploaded_urls[0]:
            f.write(f"{uploaded_urls[0]}\n\n")
        f.write("GALLERY IMAGES:\n")
        for i, url in enumerate(uploaded_urls[1:], start=2):
            if url:
                f.write(f"{url}  -- Ảnh {i}\n")
    
    print(f"✅ URLs saved to: {urls_file}")
    print()
    
    print("="*60)
    print("✅ HOÀN TẤT!")
    print("="*60)
    print()
    print("📋 BƯỚC TIẾP THEO:")
    print()
    print(f"1. Mở file: {output_file}")
    print("2. Copy TOÀN BỘ nội dung")
    print("3. Paste vào pgAdmin Query Tool")
    print("4. Chạy (F5 hoặc nút Execute)")
    print()
    print("="*60)
    print(f"✅ {success_count} ảnh đã được upload lên Cloudinary!")
    print("✅ SQL script đã sẵn sàng để chạy trong pgAdmin!")
    print("="*60)

if __name__ == "__main__":
    main()

