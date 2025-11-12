# Phân tích Mapping giữa 3 bảng: Restaurant, RestaurantOwner, và User

## Cấu trúc Entity và Mapping

### 1. **RestaurantProfile** (restaurant_profile)
```java
@Table(name = "restaurant_profile")
public class RestaurantProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restaurant_id")
    private Integer restaurantId;  // PK: Integer (auto-generated)
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private RestaurantOwner owner;  // FK -> restaurant_owner.owner_id
}
```

**Mapping:**
- `restaurant_profile.owner_id` (UUID) → `restaurant_owner.owner_id` (UUID, PK)

### 2. **RestaurantOwner** (restaurant_owner)
```java
@Table(name = "restaurant_owner")
public class RestaurantOwner {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "owner_id")
    private UUID ownerId;  // PK: UUID (auto-generated)
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;  // FK -> user.id
}
```

**Mapping:**
- `restaurant_owner.owner_id` (UUID, PK)
- `restaurant_owner.user_id` (UUID, FK, unique) → `user.id` (UUID, PK)

### 3. **Customer** (customer)
```java
@Table(name = "customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "customer_id")
    private UUID customerId;  // PK: UUID (auto-generated)
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;  // FK -> user.id
}
```

**Mapping:**
- `customer.customer_id` (UUID, PK)
- `customer.user_id` (UUID, FK, unique) → `user.id` (UUID, PK)

### 4. **User** (user)
```java
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;  // PK: UUID (auto-generated)
}
```

## Sơ đồ Mapping

```
┌─────────────────────┐
│  RestaurantProfile  │
│  (restaurant_id)    │
│  owner_id (FK) ─────┼──┐
└─────────────────────┘  │
                         │
                         ▼
              ┌──────────────────────┐
              │  RestaurantOwner     │
              │  owner_id (PK)       │
              │  user_id (FK) ───────┼──┐
              └──────────────────────┘  │
                                        │
                                        ▼
                              ┌─────────────────┐
                              │      User       │
                              │  id (PK)        │
                              └─────────────────┘
                                        ▲
                                        │
              ┌──────────────────────┐ │
              │     Customer         │ │
              │  customer_id (PK)    │ │
              │  user_id (FK) ───────┼─┘
              └──────────────────────┘
```

## Phân tích dữ liệu mẫu

### Ví dụ 1: Restaurant "The Grill" (ID: 3)
```
RestaurantProfile:
  - restaurant_id: 3
  - owner_id: "15216749-1da0-4245-943e-239647f94876"

RestaurantOwner:
  - owner_id: "15216749-1da0-4245-943e-239647f94876"
  - user_id: "15216749-1da0-4245-943e-239647f94876"
  - owner_name: "PhanThanhTai"

User:
  - id: "15216749-1da0-4245-943e-239647f94876"
  - username: "KeyT02073"
  - role: "restaurant_owner"
```

**✅ Mapping ĐÚNG:**
- RestaurantProfile.owner_id → RestaurantOwner.owner_id ✅
- RestaurantOwner.user_id → User.id ✅

### Ví dụ 2: Customer "Thắng Trần Kim"
```
User:
  - id: "904aee0b-ba5a-4ae0-bee8-90bece2151b0"
  - username: "trankimthang857@gmail.com"
  - role: "CUSTOMER"

Customer:
  - customer_id: (cần kiểm tra)
  - user_id: "904aee0b-ba5a-4ae0-bee8-90bece2151b0"
```

**⚠️ VẤN ĐỀ:** User có role CUSTOMER nhưng có thể chưa có Customer entity!

## Vấn đề phát hiện

### 1. **Customer entity có thể chưa được tạo tự động**
- Khi user đăng ký với role CUSTOMER, Customer entity không được tạo tự động
- Chỉ được tạo khi:
  - User đặt booking
  - User chat với restaurant (nhưng bị lỗi nếu chưa có Customer)
  - User thêm favorite

**Giải pháp:** Đã đề xuất tự động tạo Customer entity trong `ChatService.createCustomerRestaurantRoom()` nhưng bị reject.

### 2. **RestaurantOwner.owner_id và RestaurantOwner.user_id có thể trùng**
- Trong dữ liệu mẫu, cả hai đều là "15216749-1da0-4245-943e-239647f94876"
- Điều này hợp lệ về mặt kỹ thuật nhưng có thể gây nhầm lẫn

### 3. **Mapping chain dài**
```
RestaurantProfile → RestaurantOwner → User
Customer → User
```

Khi query, cần join nhiều bảng:
```sql
SELECT r.*, ro.owner_name, u.username 
FROM restaurant_profile r
JOIN restaurant_owner ro ON r.owner_id = ro.owner_id
JOIN user u ON ro.user_id = u.id;
```

## Kiểm tra tính hợp lệ

### ✅ Mapping hợp lệ:
1. RestaurantProfile.owner_id → RestaurantOwner.owner_id (ManyToOne)
2. RestaurantOwner.user_id → User.id (OneToOne, unique)
3. Customer.user_id → User.id (OneToOne, unique)

### ⚠️ Vấn đề cần lưu ý:
1. **Customer entity không tự động tạo** khi user đăng ký
2. **Lazy loading** có thể gây N+1 query nếu không fetch join đúng cách
3. **Cascade operations** cần được xử lý cẩn thận khi xóa User

## Khuyến nghị

1. **Tự động tạo Customer entity** khi user đăng ký với role CUSTOMER
2. **Tự động tạo RestaurantOwner entity** khi user đăng ký với role RESTAURANT_OWNER
3. **Sử dụng @EntityListeners** hoặc service layer để đảm bảo consistency
4. **Thêm validation** để đảm bảo mapping đúng trước khi lưu

