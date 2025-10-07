# Restaurant Favorites Feature

## Tổng quan
Tính năng Restaurant Favorites cho phép khách hàng lưu yêu thích các nhà hàng và quản lý danh sách cá nhân hóa của họ.

## Tính năng chính

### 1. Lưu yêu thích tức thì
- **Heart Toggle**: Khách hàng có thể "thả tim" để thêm/xóa nhà hàng yêu thích
- **AJAX**: Hoạt động không cần reload trang
- **Animation**: Hiệu ứng heartbeat khi click
- **Real-time**: Cập nhật trạng thái ngay lập tức

### 2. Danh sách cá nhân hóa
- **Trang Favorites**: Hiển thị tất cả nhà hàng đã lưu
- **Filter/Sort**: Lọc theo loại ẩm thực, khoảng giá, sắp xếp theo tên/giá/ngày
- **Pagination**: Phân trang cho danh sách lớn
- **Quick Actions**: Đặt bàn nhanh từ danh sách yêu thích

### 3. Hiển thị trực quan
- **Heart Icon**: Icon trái tim với animation mượt mà
- **Toast Notifications**: Thông báo khi thêm/xóa yêu thích
- **Status Indicators**: Hiển thị trạng thái yêu thích rõ ràng
- **Responsive Design**: Tối ưu cho mọi thiết bị

### 4. REST APIs
- **Toggle Favorite**: `POST /customer/favorites/toggle`
- **Check Status**: `GET /customer/favorites/check/{restaurantId}`
- **Get Count**: `GET /customer/favorites/count`
- **Get Restaurant IDs**: `GET /customer/favorites/restaurant-ids`
- **Remove Favorite**: `POST /customer/favorites/remove/{restaurantId}`

### 5. Truy vấn tối ưu
- **Repository Methods**: Query tối ưu với JOIN FETCH
- **Pagination**: Hỗ trợ phân trang hiệu quả
- **Caching**: Có thể cache kết quả truy vấn
- **Indexing**: Database indexes cho performance tốt

### 6. Thống kê cho Admin
- **Dashboard**: Trang thống kê mức độ yêu thích
- **Charts**: Biểu đồ top nhà hàng được yêu thích
- **Export**: Xuất dữ liệu Excel (planned)
- **Analytics**: Phân tích xu hướng yêu thích

## Cấu trúc Code

### Domain Layer
```
src/main/java/com/example/booking/domain/
├── CustomerFavorite.java          # Entity chính
├── Customer.java                  # Entity khách hàng
└── RestaurantProfile.java         # Entity nhà hàng
```

### Repository Layer
```
src/main/java/com/example/booking/repository/
└── CustomerFavoriteRepository.java # Repository với query tối ưu
```

### Service Layer
```
src/main/java/com/example/booking/service/
├── FavoriteService.java           # Interface service
└── impl/FavoriteServiceImpl.java # Implementation
```

### Controller Layer
```
src/main/java/com/example/booking/web/controller/
├── customer/FavoriteController.java    # Customer APIs
└── admin/AdminFavoriteController.java  # Admin statistics
```

### DTOs
```
src/main/java/com/example/booking/dto/
├── customer/
│   ├── FavoriteRestaurantDto.java      # DTO cho restaurant yêu thích
│   ├── ToggleFavoriteRequest.java      # Request DTO
│   └── ToggleFavoriteResponse.java     # Response DTO
└── admin/
    └── FavoriteStatisticsDto.java      # DTO cho thống kê
```

### Templates
```
src/main/resources/templates/
├── customer/favorites.html             # Trang favorites
├── admin/favorite-statistics.html      # Trang thống kê admin
└── test-favorites.html                 # Trang test
```

## Database Schema

### Table: customer_favorite
```sql
CREATE TABLE customer_favorite (
    favorite_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES customer(customer_id),
    restaurant_id INTEGER NOT NULL REFERENCES restaurant_profile(restaurant_id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(customer_id, restaurant_id)  -- Prevent duplicates
);
```

### Indexes
```sql
-- Performance indexes
CREATE INDEX idx_customer_favorite_customer_id ON customer_favorite(customer_id);
CREATE INDEX idx_customer_favorite_restaurant_id ON customer_favorite(restaurant_id);
CREATE INDEX idx_customer_favorite_created_at ON customer_favorite(created_at);
```

## API Endpoints

### Customer Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/customer/favorites` | Trang danh sách yêu thích |
| POST | `/customer/favorites/toggle` | Toggle favorite status |
| GET | `/customer/favorites/check/{id}` | Check if restaurant is favorited |
| GET | `/customer/favorites/count` | Get favorite count |
| GET | `/customer/favorites/restaurant-ids` | Get favorited restaurant IDs |
| POST | `/customer/favorites/remove/{id}` | Remove from favorites |

### Admin Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/favorites` | Trang thống kê admin |

## Usage Examples

### 1. Toggle Favorite (JavaScript)
```javascript
function toggleFavorite(heartIcon) {
    const restaurantId = heartIcon.dataset.restaurantId;
    const request = {
        restaurantId: parseInt(restaurantId),
        isFavorited: !heartIcon.classList.contains('favorited')
    };
    
    fetch('/customer/favorites/toggle', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(request)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            heartIcon.classList.toggle('favorited');
            showToast(data.message);
        }
    });
}
```

### 2. Load Favorite Status
```javascript
function loadFavoriteStatus() {
    fetch('/customer/favorites/restaurant-ids')
        .then(response => response.json())
        .then(restaurantIds => {
            restaurantIds.forEach(id => {
                const heartIcon = document.querySelector(`[data-restaurant-id="${id}"]`);
                if (heartIcon) heartIcon.classList.add('favorited');
            });
        });
}
```

### 3. Service Usage (Java)
```java
@Autowired
private FavoriteService favoriteService;

// Add to favorites
favoriteService.addToFavorites(customerId, restaurantId);

// Check if favorited
boolean isFavorited = favoriteService.isFavorited(customerId, restaurantId);

// Get favorite restaurants
Page<FavoriteRestaurantDto> favorites = favoriteService.getFavoriteRestaurants(customerId, pageable);
```

## Testing

### 1. Unit Tests
- Test service methods
- Test repository queries
- Test DTO conversions

### 2. Integration Tests
- Test API endpoints
- Test database operations
- Test authentication

### 3. UI Tests
- Test heart toggle functionality
- Test animations
- Test responsive design

### 4. Manual Testing
- Visit `/test-favorites` for interactive testing
- Test with different user roles
- Test with various data scenarios

## Performance Considerations

### 1. Database Optimization
- Use appropriate indexes
- Optimize queries with JOIN FETCH
- Consider caching for frequently accessed data

### 2. Frontend Optimization
- Lazy load favorite status
- Debounce rapid clicks
- Use efficient DOM updates

### 3. Caching Strategy
- Cache favorite counts
- Cache restaurant lists
- Use Redis for session data

## Security Considerations

### 1. Authentication
- Require customer login for favorite operations
- Validate user permissions
- Prevent unauthorized access

### 2. Data Validation
- Validate restaurant IDs
- Sanitize input data
- Prevent SQL injection

### 3. Rate Limiting
- Limit toggle requests per user
- Prevent spam operations
- Monitor suspicious activity

## Future Enhancements

### 1. Advanced Features
- **Wishlist Categories**: Phân loại yêu thích
- **Share Favorites**: Chia sẻ danh sách
- **Recommendations**: Gợi ý dựa trên yêu thích
- **Social Features**: Theo dõi bạn bè

### 2. Analytics
- **Heat Maps**: Bản đồ yêu thích theo khu vực
- **Trend Analysis**: Phân tích xu hướng
- **Customer Insights**: Hiểu rõ hành vi khách hàng

### 3. Integration
- **Email Notifications**: Thông báo nhà hàng mới
- **Mobile App**: API cho mobile
- **Third-party**: Tích hợp với social media

## Troubleshooting

### Common Issues

1. **Heart icon không hiển thị**
   - Kiểm tra CSS và JavaScript
   - Verify authentication status
   - Check console for errors

2. **Toggle không hoạt động**
   - Check API endpoint
   - Verify request format
   - Check authentication

3. **Performance chậm**
   - Check database indexes
   - Optimize queries
   - Consider caching

### Debug Tools
- Use `/test-favorites` page
- Check browser console
- Monitor network requests
- Review server logs

## Conclusion

Tính năng Restaurant Favorites cung cấp trải nghiệm người dùng mượt mà và hiệu quả cho việc quản lý danh sách yêu thích. Với kiến trúc tối ưu và API RESTful, tính năng này có thể dễ dàng mở rộng và tích hợp với các tính năng khác trong tương lai.
