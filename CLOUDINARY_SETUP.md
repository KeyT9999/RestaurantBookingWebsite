# Cloudinary Integration Setup

## Tổng quan
Dự án đã được tích hợp Cloudinary để quản lý ảnh online thay vì lưu trữ local. Cloudinary cung cấp:
- Lưu trữ ảnh trên cloud
- Tự động tối ưu hóa ảnh
- Tạo thumbnail và resize ảnh động
- CDN để tải ảnh nhanh hơn

## Cấu hình

### 1. Thêm thông tin Cloudinary vào file .env
```bash
# Cloudinary Configuration for Image Management
CLOUDINARY_CLOUD_NAME=your_cloudinary_cloud_name
CLOUDINARY_API_KEY=your_cloudinary_api_key
CLOUDINARY_API_SECRET=your_cloudinary_api_secret
CLOUDINARY_SECURE=true
```

### 2. Cấu hình trong application.yml
```yaml
app:
  upload:
    use-cloudinary: true  # Set to false to use local storage

# Cloudinary Configuration
cloudinary:
  cloud-name: ${CLOUDINARY_CLOUD_NAME}
  api-key: ${CLOUDINARY_API_KEY}
  api-secret: ${CLOUDINARY_API_SECRET}
  secure: ${CLOUDINARY_SECURE:true}
```

## Các Service đã tạo

### 1. CloudinaryConfig
- Cấu hình Cloudinary bean
- Định nghĩa các upload options cho từng loại ảnh

### 2. CloudinaryService
- Xử lý upload ảnh lên Cloudinary
- Tạo thumbnail và optimize ảnh
- Xóa ảnh từ Cloudinary

### 3. ImageUploadService
- Service wrapper tích hợp Cloudinary và local storage
- Fallback tự động nếu Cloudinary lỗi
- Có thể chuyển đổi giữa Cloudinary và local storage

## Cách sử dụng

### 1. Upload ảnh nhà hàng
```java
@Autowired
private ImageUploadService imageUploadService;

// Upload ảnh chính của nhà hàng
String imageUrl = imageUploadService.uploadRestaurantImage(file, restaurantId, "main");

// Upload ảnh banner
String bannerUrl = imageUploadService.uploadRestaurantImage(file, restaurantId, "banner");
```

### 2. Upload ảnh món ăn
```java
String dishImageUrl = imageUploadService.uploadDishImage(file, dishId);
```

### 3. Upload avatar người dùng
```java
String avatarUrl = imageUploadService.uploadAvatar(file, userId);
```

### 4. Upload ảnh bằng chứng review
```java
String evidenceUrl = imageUploadService.uploadReviewEvidence(file, reviewId);
```

### 5. Tạo thumbnail và optimize ảnh
```java
// Tạo thumbnail 150x150
String thumbnailUrl = imageUploadService.getThumbnailUrl(originalUrl);

// Tạo ảnh optimize với kích thước tùy chỉnh
String optimizedUrl = imageUploadService.getOptimizedImageUrl(originalUrl, 400, 300);
```

### 6. Xóa ảnh
```java
boolean deleted = imageUploadService.deleteImage(imageUrl);
```

## Test Cloudinary Integration

### 1. Truy cập trang test
```
http://localhost:8081/test/cloudinary
```

### 2. Các chức năng test có sẵn
- Upload ảnh nhà hàng
- Upload ảnh món ăn  
- Upload avatar
- Xóa ảnh
- Kiểm tra cấu hình

## Cấu trúc thư mục trên Cloudinary

```
cloudinary.com/
├── restaurants/
│   └── {restaurantId}/
│       ├── main_*.jpg
│       ├── banner_*.jpg
│       └── gallery_*.jpg
├── dishes/
│   └── {dishId}/
│       └── dish_*.jpg
├── avatars/
│   └── {userId}/
│       └── avatar_*.jpg
└── review_evidence/
    └── {reviewId}/
        └── evidence_*.jpg
```

## Tối ưu hóa ảnh

Cloudinary tự động áp dụng các tối ưu hóa:
- **Format**: Tự động chọn format tốt nhất (WebP, AVIF)
- **Quality**: Auto quality để cân bằng chất lượng và kích thước
- **Resize**: Crop và resize theo yêu cầu
- **Compression**: Nén ảnh để giảm dung lượng

## Fallback Mechanism

Nếu Cloudinary gặp lỗi, hệ thống sẽ tự động fallback về local storage:
1. Thử upload lên Cloudinary
2. Nếu lỗi, tự động chuyển sang local storage
3. Log cảnh báo để admin biết

## Chuyển đổi giữa Cloudinary và Local Storage

Để tắt Cloudinary và dùng local storage:
```yaml
app:
  upload:
    use-cloudinary: false
```

## Bảo mật

- API keys được lưu trong environment variables
- Chỉ admin mới có thể xóa ảnh
- Validate file type và size trước khi upload
- Tên file được generate ngẫu nhiên để tránh conflict

## Monitoring

- Log tất cả hoạt động upload/delete
- Track performance của Cloudinary
- Monitor storage usage
- Alert khi có lỗi xảy ra

## Troubleshooting

### 1. Lỗi cấu hình
- Kiểm tra environment variables
- Verify API keys trong Cloudinary dashboard
- Check network connectivity

### 2. Upload thất bại
- Kiểm tra file size (max 10MB)
- Verify file format (jpg, png, gif, webp)
- Check Cloudinary quota

### 3. Ảnh không hiển thị
- Verify URL format
- Check CDN status
- Test với browser developer tools

## Production Deployment

1. Set environment variables trên server
2. Enable Cloudinary secure URLs
3. Configure CDN settings
4. Set up monitoring và alerting
5. Test fallback mechanism

## Cost Optimization

- Sử dụng auto quality để giảm bandwidth
- Implement lazy loading cho ảnh
- Cache ảnh ở client side
- Monitor storage usage thường xuyên
