#!/usr/bin/env python3
"""
Script để upload ảnh nhà hàng "Country BBQ & Beer" lên Cloudinary
"""

import os
import sys
from pathlib import Path
from cloudinary import uploader
import cloudinary

def load_env_file():
    env_file = Path(__file__).parent.parent / '.env'
    if env_file.exists():
        with open(env_file, 'r', encoding='utf-8') as f:
            for line in f:
                line = line.strip()
                if line and not line.startswith('#') and '=' in line:
                    key, value = line.split('=', 1)
                    key = key.strip()
                    value = value.strip().strip('"').strip("'")
                    if key.startswith('CLOUDINARY_') and not os.getenv(key):
                        os.environ[key] = value

load_env_file()

cloudinary.config(
    cloud_name=os.getenv('CLOUDINARY_CLOUD_NAME', 'drcly5nge'),
    api_key=os.getenv('CLOUDINARY_API_KEY', '574438289271325'),
    api_secret=os.getenv('CLOUDINARY_API_SECRET', 'dDyQjA3bmFgf_7fdsJFEXs4DTSA'),
    secure=True
)

IMAGE_FOLDER = r"C:\Users\ASUS\Desktop\RestaurantBookingWebsite\Media_update\Country BBQ & Beer - Trần Bạch Đằng"

IMAGE_FILES = [
    "slide-country-bbq-1-300-2710885070324.webp",
    "slide-country-bbq-2-300-2710885170325.webp",
    "slide-country-bbq-5-300-2710885470328.webp",
    "slide-country-bbq-6-300-2710885570329.webp",
    "slide-country-bbq-7-300-2710885670330.webp",
    "slide-country-bbq-8-300-2710885770331.webp",
    "slide-country-bbq-11-300-2710886070334.webp",
    "slide-country-bbq-12-300-2710886170335.webp"
]

def upload_image(file_path, restaurant_id, media_type, index):
    try:
        folder = f"restaurants/{restaurant_id}/media/{media_type}"
        public_id = f"{media_type}_{index}_{int(os.path.getmtime(file_path))}"
        
        result = uploader.upload(
            file_path,
            folder=folder,
            public_id=public_id,
            resource_type="image",
            transformation=[
                {"width": 1200, "height": 800, "crop": "fill", "quality": "auto:good"}
            ]
        )
        return result.get('secure_url')
    except Exception as e:
        print(f"  ❌ ERROR: {str(e)}")
        return None

def generate_sql_script(urls, restaurant_id):
    valid_urls = [url for url in urls if url is not None]
    
    if not valid_urls:
        return None
    
    sql_content = f"""-- =====================================================
-- SQL Script: Thêm ảnh cho nhà hàng "Country BBQ & Beer"
-- Restaurant ID: {restaurant_id}
-- =====================================================

DO $$
DECLARE
    v_restaurant_id INTEGER := {restaurant_id};
    v_image_count INTEGER;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM restaurant_profile WHERE restaurant_id = v_restaurant_id) THEN
        RAISE EXCEPTION 'Restaurant với ID % không tồn tại!', v_restaurant_id;
    END IF;
    
    -- COVER IMAGE (ảnh đầu tiên)
    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)
    VALUES ({restaurant_id}, 'cover', '{valid_urls[0]}', NOW());
    
    -- GALLERY IMAGES
"""
    
    for i, url in enumerate(valid_urls[1:], start=2):
        sql_content += f"    INSERT INTO restaurant_media (restaurant_id, type, url, created_at)\n"
        sql_content += f"    VALUES ({restaurant_id}, 'gallery', '{url}', NOW());\n"
    
    sql_content += f"""    
    SELECT COUNT(*) INTO v_image_count
    FROM restaurant_media
    WHERE restaurant_id = v_restaurant_id;
    
    RAISE NOTICE '✅ IMAGES ADDED SUCCESSFULLY!';
    RAISE NOTICE 'Restaurant ID: %', v_restaurant_id;
    RAISE NOTICE 'Total images: %', v_image_count;
    
END $$;
"""
    return sql_content

def main():
    print("="*60)
    print("UPLOAD ẢNH: Country BBQ & Beer")
    print("="*60)
    print()
    
    if not os.path.exists(IMAGE_FOLDER):
        print(f"❌ ERROR: Folder not found: {IMAGE_FOLDER}")
        sys.exit(1)
    
    restaurant_id = input("Nhập restaurant_id (từ kết quả SQL script): ").strip()
    
    try:
        restaurant_id = int(restaurant_id)
    except ValueError:
        print("❌ ID không hợp lệ!")
        sys.exit(1)
    
    print(f"\n📤 Uploading {len(IMAGE_FILES)} images...\n")
    
    uploaded_urls = []
    for i, filename in enumerate(IMAGE_FILES):
        file_path = os.path.join(IMAGE_FOLDER, filename)
        if not os.path.exists(file_path):
            print(f"⚠️  File not found: {filename}")
            uploaded_urls.append(None)
            continue
        
        media_type = "cover" if i == 0 else "gallery"
        print(f"[{i+1}/{len(IMAGE_FILES)}] {filename} ({media_type})...")
        url = upload_image(file_path, restaurant_id, media_type, i)
        uploaded_urls.append(url)
        if url:
            print(f"  ✅ Done")
        print()
    
    success_count = sum(1 for url in uploaded_urls if url is not None)
    print(f"✅ Uploaded: {success_count}/{len(IMAGE_FILES)}\n")
    
    if success_count == 0:
        print("❌ ERROR: No images uploaded!")
        sys.exit(1)
    
    sql_content = generate_sql_script(uploaded_urls, restaurant_id)
    if sql_content:
        output_file = "scripts/insert_country_bbq_images.sql"
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write(sql_content)
        print(f"✅ SQL script saved: {output_file}")
        print("\n📋 Next: Chạy file SQL trong pgAdmin")
    else:
        print("❌ ERROR: Cannot generate SQL script")

if __name__ == "__main__":
    main()

