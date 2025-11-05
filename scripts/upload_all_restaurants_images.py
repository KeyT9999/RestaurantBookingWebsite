#!/usr/bin/env python3
"""
Script để upload ảnh cho TẤT CẢ các nhà hàng còn lại lên Cloudinary
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
    cloud_name=os.getenv('CLOUDINARY_CLOUD_NAME', 'your_cloudinary_cloud_name'),
    api_key=os.getenv('CLOUDINARY_API_KEY', 'your_cloudinary_api_key'),
    api_secret=os.getenv('CLOUDINARY_API_SECRET', 'your_cloudinary_api_secret'),
    secure=True
)

# Cấu hình các nhà hàng (restaurant_id sẽ được cập nhật sau khi chạy SQL)
RESTAURANTS = [
    {
        'name': 'Hải Sản Ngọc Hương – Võ Nguyên Giáp',
        'folder': r'C:\Users\ASUS\Desktop\RestaurantBookingWebsite\Media_update\Hải Sản Ngọc Hương – Võ Nguyên Giáp',
        'images': [
            'nha-hang-hai-san-ngoc-huong-vo-nguyen-giap-1-normal-1744785229187.webp',
            'nha-hang-hai-san-ngoc-huong-vo-nguyen-giap-2-normal-1744785329188.webp',
            'nha-hang-hai-san-ngoc-huong-vo-nguyen-giap-3-normal-1744785429189.webp',
            'nha-hang-hai-san-ngoc-huong-vo-nguyen-giap-4-normal-1744785629190.webp',
            'nha-hang-hai-san-ngoc-huong-vo-nguyen-giap-5-normal-1744788829191.webp',
            'nha-hang-hai-san-ngoc-huong-vo-nguyen-giap-6-normal-1744785929192.webp',
            'nha-hang-hai-san-ngoc-huong-vo-nguyen-giap-7-normal-1744786429193.webp',
            'nha-hang-hai-san-ngoc-huong-vo-nguyen-giap-8-normal-1744786929194.webp',
            'nha-hang-hai-san-ngoc-huong-vo-nguyen-giap-11-normal-1744787629197.webp'
        ],
        'restaurant_id': None  # Sẽ được cập nhật từ SQL
    },
    {
        'name': 'Nhà hàng Akataiyo Mặt Trời Đỏ - Nguyễn Du',
        'folder': r'C:\Users\ASUS\Desktop\RestaurantBookingWebsite\Media_update\Nhà hàng Akataiyo Mặt Trời Đỏ - Nguyễn Du',
        'images': [
            'nha-hang-akataiyo-mat-troi-do-nguyen-du-slide-3-normal-130839815015.webp',
            'nha-hang-akataiyo-mat-troi-do-nguyen-du-slide-4-300-130839915016.webp',
            'nha-hang-akataiyo-mat-troi-do-nguyen-du-slide-6-300-130840115018.webp',
            'nha-hang-akataiyo-mat-troi-do-nguyen-du-slide-7-normal-130840215019.webp',
            'nha-hang-akataiyo-mat-troi-do-nguyen-du-slide-8-normal-130840315020.webp',
            'nha-hang-akataiyo-mat-troi-do-nguyen-du-slide-9-normal-130840415021.webp',
            'nha-hang-akataiyo-mat-troi-do-nguyen-du-slide-11-normal-130840615023.webp'
        ],
        'restaurant_id': None
    },
    {
        'name': 'Phố Biển – Đảo Xanh',
        'folder': r'C:\Users\ASUS\Desktop\RestaurantBookingWebsite\Media_update\Phố Biển – Đảo Xanh',
        'images': [
            'nha-hang-pho-bien-dao-xanh-1-300-466633429253.webp',
            'nha-hang-pho-bien-dao-xanh-2-300-466633629254.webp',
            'nha-hang-pho-bien-dao-xanh-3-300-466635029255.webp',
            'nha-hang-pho-bien-dao-xanh-4-300-466635129256.webp',
            'nha-hang-pho-bien-dao-xanh-5-300-466635629257.webp',
            'nha-hang-pho-bien-dao-xanh-6-300-466636229258.webp',
            'nha-hang-pho-bien-dao-xanh-7-300-466637029259.webp',
            'nha-hang-pho-bien-dao-xanh-8-normal-466638329260.webp',
            'nha-hang-pho-bien-dao-xanh-13-normal-483715829262.webp',
            'nha-hang-pho-bien-dao-xanh-15-normal-483717029264.webp'
        ],
        'restaurant_id': None
    },
    {
        'name': 'The Anchor (Restaurant & Bierhaus) - Trần Phú',
        'folder': r'C:\Users\ASUS\Desktop\RestaurantBookingWebsite\Media_update\The Anchor (Restaurant & Bierhaus) - Trần Phú',
        'images': [
            'nha-hang-the-anchor-bierhaus-tran-phu-1-300-2216330354130.webp',
            'nha-hang-the-anchor-bierhaus-tran-phu-2-normal-2216330454131.webp',
            'nha-hang-the-anchor-bierhaus-tran-phu-3-normal-2216330554132.webp',
            'nha-hang-the-anchor-bierhaus-tran-phu-4-normal-2216330654133.webp',
            'nha-hang-the-anchor-bierhaus-tran-phu-5-normal-2216330754134.webp',
            'nha-hang-the-anchor-bierhaus-tran-phu-6-normal-2216330854135.webp',
            'nha-hang-the-anchor-bierhaus-tran-phu-7-normal-2216331054136.webp',
            'nha-hang-the-anchor-bierhaus-tran-phu-8-normal-2216331154137.webp',
            'nha-hang-the-anchor-bierhaus-tran-phu-9-normal-2216331254138.webp',
            'nha-hang-the-anchor-bierhaus-tran-phu-10-normal-2216331354139.webp',
            'nha-hang-the-anchor-bierhaus-tran-phu-11-normal-2216331454140.webp',
            'nha-hang-the-anchor-bierhaus-tran-phu-12-normal-2216331854141.webp'
        ],
        'restaurant_id': None
    },
    {
        'name': 'Vietbamboo Restaurant - Phạm Văn Đồng',
        'folder': r'C:\Users\ASUS\Desktop\RestaurantBookingWebsite\Media_update\Vietbamboo Restaurant - Phạm Văn Đồng',
        'images': [
            'nha-hang-vietbamboo-restaurant-pham-van-dong-1-normal-472680929515.webp',
            'nha-hang-vietbamboo-restaurant-pham-van-dong-2-normal-472681129516.webp',
            'nha-hang-vietbamboo-restaurant-pham-van-dong-3-300-472682429517.webp',
            'nha-hang-vietbamboo-restaurant-pham-van-dong-5-normal-472683429519.webp',
            'nha-hang-vietbamboo-restaurant-pham-van-dong-6-normal-472685129520.webp',
            'nha-hang-vietbamboo-restaurant-pham-van-dong-7-normal-472686729521.webp',
            'nha-hang-vietbamboo-restaurant-pham-van-dong-8-normal-472687129522.webp',
            'nha-hang-vietbamboo-restaurant-pham-van-dong-9-normal-472689929523.webp',
            'nha-hang-vietbamboo-restaurant-pham-van-dong-10-normal-472690829524.webp'
        ],
        'restaurant_id': None
    },
    {
        'name': 'Vườn Nướng - Đường 304',
        'folder': r'C:\Users\ASUS\Desktop\RestaurantBookingWebsite\Media_update\Vườn Nướng - Đường 304',
        'images': [
            'nha-hang-vuon-nuong-slide-1up-300-209937821018.webp',
            'nha-hang-vuon-nuong-slide-2up-300-209937921020.webp',
            'nha-hang-vuon-nuong-slide-3up-300-209938021022.webp',
            'nha-hang-vuon-nuong-slide-7up-300-209938121027.webp',
            'nha-hang-vuon-nuong-duong-30-4-slide-4-300-160180021024.webp',
            'nha-hang-vuon-nuong-duong-30-4-slide-5-300-160180121025.webp',
            'nha-hang-vuon-nuong-duong-30-4-slide-6-300-160180221026.webp',
            'nha-hang-vuon-nuong-duong-30-4-slide-8-normal-160180421028.webp',
            'nha-hang-vuon-nuong-duong-30-4-slide-9-normal-160180521029.webp',
            'nha-hang-vuon-nuong-duong-30-4-slide-10-300-160180621030.webp',
            'nha-hang-vuon-nuong-duong-30-4-slide-11-normal-160180721031.webp'
        ],
        'restaurant_id': None
    },
    {
        'name': 'Zzuggubbong - Nguyễn Hữu Thông',
        'folder': r'C:\Users\ASUS\Desktop\RestaurantBookingWebsite\Media_update\Zzuggubbong - Nguyễn Hữu Thông',
        'images': [
            'nha-hang-zzuggubbong-nguyen-huu-thong-1-normal-1418900045058.webp',
            'nha-hang-zzuggubbong-nguyen-huu-thong-2-300-1418900145059.webp',
            'nha-hang-zzuggubbong-nguyen-huu-thong-3-300-1418900345060.webp',
            'nha-hang-zzuggubbong-nguyen-huu-thong-4-300-1418900645061.webp',
            'nha-hang-zzuggubbong-nguyen-huu-thong-5-300-1418901145062.webp',
            'nha-hang-zzuggubbong-nguyen-huu-thong-6-300-1418901445063.webp',
            'nha-hang-zzuggubbong-nguyen-huu-thong-7-300-1418901945064.webp',
            'nha-hang-zzuggubbong-nguyen-huu-thong-8-300-1418902045065.webp',
            'nha-hang-zzuggubbong-nguyen-huu-thong-9-300-1418903045066.webp',
            'nha-hang-zzuggubbong-nguyen-huu-thong-10-300-1418903145067.webp',
            'nha-hang-zzuggubbong-nguyen-huu-thong-11-300-1418903445068.webp',
            'nha-hang-zzuggubbong-nguyen-huu-thong-12-300-1418903645069.webp'
        ],
        'restaurant_id': None
    }
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

def generate_sql_script(restaurant_data):
    """Tạo SQL script cho một nhà hàng"""
    valid_urls = [url for url in restaurant_data['urls'] if url is not None]
    
    if not valid_urls:
        return None
    
    restaurant_id = restaurant_data['restaurant_id']
    sql_content = f"""-- =====================================================
-- SQL Script: Thêm ảnh cho nhà hàng "{restaurant_data['name']}"
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
    print("="*70)
    print("UPLOAD ẢNH CHO TẤT CẢ NHÀ HÀNG")
    print("="*70)
    print()
    
    # Nhận restaurant_id bắt đầu từ command line hoặc input
    if len(sys.argv) > 1:
        start_id = int(sys.argv[1])
    else:
        try:
            start_id_str = input("Nhập restaurant_id bắt đầu (ví dụ: 45, nếu Hải Sản Bà Cường là 44): ").strip()
            start_id = int(start_id_str)
        except (EOFError, KeyboardInterrupt, ValueError):
            print("\n❌ ERROR: Bạn cần cung cấp restaurant_id bắt đầu!")
            print("   Cách 1: Chạy với argument: python scripts/upload_all_restaurants_images.py <start_id>")
            print("   Cách 2: Chạy script và nhập ID khi được hỏi")
            sys.exit(1)
    
    print(f"🚀 Bắt đầu từ restaurant_id: {start_id}")
    print(f"📋 Sẽ upload ảnh cho {len(RESTAURANTS)} nhà hàng")
    print()
    
    all_results = []
    
    for idx, restaurant in enumerate(RESTAURANTS):
        restaurant_id = start_id + idx
        restaurant['restaurant_id'] = restaurant_id
        
        print("="*70)
        print(f"[{idx+1}/{len(RESTAURANTS)}] {restaurant['name']}")
        print(f"Restaurant ID: {restaurant_id}")
        print("="*70)
        print()
        
        if not os.path.exists(restaurant['folder']):
            print(f"❌ ERROR: Folder not found: {restaurant['folder']}")
            all_results.append({
                'name': restaurant['name'],
                'restaurant_id': restaurant_id,
                'success': False,
                'urls': []
            })
            continue
        
        print(f"📤 Uploading {len(restaurant['images'])} images...\n")
        
        uploaded_urls = []
        for i, filename in enumerate(restaurant['images']):
            file_path = os.path.join(restaurant['folder'], filename)
            if not os.path.exists(file_path):
                print(f"⚠️  File not found: {filename}")
                uploaded_urls.append(None)
                continue
            
            media_type = "cover" if i == 0 else "gallery"
            print(f"[{i+1}/{len(restaurant['images'])}] {filename} ({media_type})...")
            url = upload_image(file_path, restaurant_id, media_type, i)
            uploaded_urls.append(url)
            if url:
                print(f"  ✅ Done")
            print()
        
        success_count = sum(1 for url in uploaded_urls if url is not None)
        print(f"✅ Uploaded: {success_count}/{len(restaurant['images'])}\n")
        
        restaurant['urls'] = uploaded_urls
        all_results.append({
            'name': restaurant['name'],
            'restaurant_id': restaurant_id,
            'success': success_count > 0,
            'urls': uploaded_urls
        })
    
    # Tạo SQL scripts cho từng nhà hàng
    print("="*70)
    print("GENERATING SQL SCRIPTS...")
    print("="*70)
    print()
    
    for result in all_results:
        if result['success']:
            sql_content = generate_sql_script(result)
            if sql_content:
                # Tạo tên file an toàn từ tên nhà hàng
                safe_name = result['name'].replace(' ', '_').replace('/', '_').replace('(', '').replace(')', '').replace('–', '_')
                safe_name = ''.join(c for c in safe_name if c.isalnum() or c in ('_', '-'))
                output_file = f"scripts/insert_{safe_name.lower()}_images.sql"
                
                with open(output_file, 'w', encoding='utf-8') as f:
                    f.write(sql_content)
                print(f"✅ {result['name']}: {output_file}")
    
    # Tạo file SQL tổng hợp
    combined_sql = "-- =====================================================\n"
    combined_sql += "-- SQL Script: Thêm ảnh cho TẤT CẢ các nhà hàng\n"
    combined_sql += "-- =====================================================\n\n"
    
    for result in all_results:
        if result['success']:
            sql_content = generate_sql_script(result)
            if sql_content:
                combined_sql += sql_content + "\n\n"
    
    with open("scripts/insert_all_restaurants_images.sql", 'w', encoding='utf-8') as f:
        f.write(combined_sql)
    
    print()
    print("="*70)
    print("✅ HOÀN TẤT!")
    print("="*70)
    print()
    print("📋 TỔNG KẾT:")
    for result in all_results:
        status = "✅" if result['success'] else "❌"
        print(f"  {status} {result['name']} (ID: {result['restaurant_id']})")
    print()
    print("📁 File SQL tổng hợp: scripts/insert_all_restaurants_images.sql")
    print("📁 File SQL riêng lẻ: scripts/insert_*_images.sql")
    print()
    print("📋 Next: Chạy file SQL trong pgAdmin")

if __name__ == "__main__":
    main()

