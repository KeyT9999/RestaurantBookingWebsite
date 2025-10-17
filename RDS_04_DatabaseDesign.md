# REQUIREMENT & DESIGN SPECIFICATION (RDS)
## HỆ THỐNG ĐẶT BÀN NHÀ HÀNG - RESTAURANT BOOKING PLATFORM

---

## 4. DATABASE DESIGN (THIẾT KẾ CƠ SỞ DỮ LIỆU)

### 4.1 Tổng quan Database

**Hệ quản trị cơ sở dữ liệu**: PostgreSQL 12+  
**Kiến trúc**: Relational Database với các bảng chính và bảng liên kết  
**Encoding**: UTF-8  
**Timezone**: Asia/Ho_Chi_Minh

### 4.2 Sơ đồ ERD (Entity Relationship Diagram)

```
Users (1) ←→ (1) Customer
Users (1) ←→ (1) RestaurantOwner
RestaurantOwner (1) ←→ (N) RestaurantProfile
RestaurantProfile (1) ←→ (N) RestaurantTable
RestaurantProfile (1) ←→ (N) Dish
RestaurantProfile (1) ←→ (N) Review
RestaurantProfile (1) ←→ (N) Voucher
Customer (1) ←→ (N) Booking
RestaurantProfile (1) ←→ (N) Booking
Booking (1) ←→ (N) Payment
Booking (1) ←→ (N) BookingTable
RestaurantTable (1) ←→ (N) BookingTable
Customer (1) ←→ (N) Review
Customer (1) ←→ (N) CustomerFavorite
Customer (1) ←→ (N) CustomerVoucher
Voucher (1) ←→ (N) CustomerVoucher
ChatRoom (1) ←→ (N) Message
Customer (1) ←→ (N) ChatRoom
RestaurantProfile (1) ←→ (N) ChatRoom
User (1) ←→ (N) ChatRoom (Admin)
```

### 4.3 Mô tả chi tiết các bảng

#### 4.3.1 Bảng Users

**Mục đích**: Lưu trữ thông tin tài khoản người dùng chung cho cả Customer và Restaurant Owner.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | UUID | PRIMARY KEY, NOT NULL | Khóa chính, UUID tự sinh |
| username | VARCHAR(50) | UNIQUE, NOT NULL | Tên đăng nhập, duy nhất |
| email | VARCHAR(255) | UNIQUE, NOT NULL | Email người dùng, duy nhất |
| password | VARCHAR(255) | NOT NULL | Mật khẩu đã mã hóa |
| full_name | VARCHAR(255) | NOT NULL | Họ tên đầy đủ |
| phone_number | VARCHAR(15) | NULL | Số điện thoại |
| address | VARCHAR(500) | NULL | Địa chỉ |
| profile_image_url | VARCHAR(500) | NULL | URL ảnh đại diện |
| role | VARCHAR(20) | NOT NULL, DEFAULT 'CUSTOMER' | Vai trò: CUSTOMER, RESTAURANT_OWNER, ADMIN |
| email_verified | BOOLEAN | DEFAULT FALSE | Trạng thái xác thực email |
| email_verification_token | VARCHAR(255) | NULL | Token xác thực email |
| password_reset_token | VARCHAR(255) | NULL | Token đặt lại mật khẩu |
| password_reset_token_expiry | TIMESTAMP | NULL | Thời hạn token đặt lại |
| google_id | VARCHAR(255) | NULL | ID Google OAuth |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Thời gian tạo |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Thời gian cập nhật |
| last_login | TIMESTAMP | NULL | Lần đăng nhập cuối |
| active | BOOLEAN | DEFAULT TRUE | Trạng thái hoạt động |
| deleted_at | TIMESTAMP | NULL | Thời gian xóa mềm |

**Indexes**:
- PRIMARY KEY (id)
- UNIQUE INDEX (username)
- UNIQUE INDEX (email)
- INDEX (role)
- INDEX (active)

#### 4.3.2 Bảng Customer

**Mục đích**: Lưu trữ thông tin bổ sung của khách hàng.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| customer_id | UUID | PRIMARY KEY, NOT NULL | Khóa chính, UUID tự sinh |
| user_id | UUID | FOREIGN KEY, UNIQUE, NOT NULL | Liên kết với Users |
| full_name | VARCHAR(255) | NOT NULL | Họ tên khách hàng |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Thời gian tạo |
| updated_at | TIMESTAMP | NULL | Thời gian cập nhật |

**Foreign Keys**:
- user_id → Users(id) ON DELETE CASCADE

#### 4.3.3 Bảng RestaurantOwner

**Mục đích**: Lưu trữ thông tin bổ sung của chủ nhà hàng.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| owner_id | UUID | PRIMARY KEY, NOT NULL | Khóa chính, UUID tự sinh |
| user_id | UUID | FOREIGN KEY, UNIQUE, NOT NULL | Liên kết với Users |
| owner_name | VARCHAR(255) | NOT NULL | Tên chủ nhà hàng |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Thời gian tạo |
| updated_at | TIMESTAMP | NULL | Thời gian cập nhật |

**Foreign Keys**:
- user_id → Users(id) ON DELETE CASCADE

#### 4.3.4 Bảng RestaurantProfile

**Mục đích**: Lưu trữ thông tin chi tiết của nhà hàng.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| restaurant_id | INTEGER | PRIMARY KEY, NOT NULL | Khóa chính, tự tăng |
| owner_id | UUID | FOREIGN KEY, NOT NULL | Liên kết với RestaurantOwner |
| restaurant_name | VARCHAR(255) | NOT NULL | Tên nhà hàng |
| address | VARCHAR(500) | NULL | Địa chỉ nhà hàng |
| phone | VARCHAR(20) | NULL | Số điện thoại nhà hàng |
| description | TEXT | NULL | Mô tả nhà hàng |
| cuisine_type | VARCHAR(100) | NULL | Loại ẩm thực |
| opening_hours | VARCHAR(100) | NULL | Giờ mở cửa |
| average_price | DECIMAL(18,2) | NULL | Giá trung bình |
| website_url | VARCHAR(255) | NULL | URL website |
| approval_status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | Trạng thái duyệt |
| approval_reason | TEXT | NULL | Lý do duyệt |
| approved_by | VARCHAR(255) | NULL | Người duyệt |
| approved_at | TIMESTAMP | NULL | Thời gian duyệt |
| rejection_reason | TEXT | NULL | Lý do từ chối |
| business_license_file | VARCHAR(500) | NULL | File giấy phép kinh doanh |
| contract_signed | BOOLEAN | DEFAULT FALSE | Trạng thái ký hợp đồng |
| contract_signed_at | TIMESTAMP | NULL | Thời gian ký hợp đồng |
| terms_accepted | BOOLEAN | NOT NULL, DEFAULT FALSE | Đã chấp nhận điều khoản |
| terms_accepted_at | TIMESTAMP | NULL | Thời gian chấp nhận điều khoản |
| terms_version | VARCHAR(20) | DEFAULT '1.0' | Phiên bản điều khoản |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Thời gian tạo |
| updated_at | TIMESTAMP | NULL | Thời gian cập nhật |

**Foreign Keys**:
- owner_id → RestaurantOwner(owner_id) ON DELETE CASCADE

**Indexes**:
- PRIMARY KEY (restaurant_id)
- INDEX (owner_id)
- INDEX (approval_status)

#### 4.3.5 Bảng RestaurantTable

**Mục đích**: Lưu trữ thông tin các bàn của nhà hàng.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| table_id | INTEGER | PRIMARY KEY, NOT NULL | Khóa chính, tự tăng |
| restaurant_id | INTEGER | FOREIGN KEY, NOT NULL | Liên kết với RestaurantProfile |
| table_name | VARCHAR(100) | NOT NULL | Tên bàn |
| capacity | INTEGER | NOT NULL, CHECK (capacity >= 1 AND capacity <= 20) | Sức chứa bàn |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'AVAILABLE' | Trạng thái bàn |
| depositamount | DECIMAL(18,2) | NOT NULL, DEFAULT 0 | Số tiền đặt cọc |

**Foreign Keys**:
- restaurant_id → RestaurantProfile(restaurant_id) ON DELETE CASCADE

**Indexes**:
- PRIMARY KEY (table_id)
- INDEX (restaurant_id)
- INDEX (status)

#### 4.3.6 Bảng Booking

**Mục đích**: Lưu trữ thông tin đặt bàn của khách hàng.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| booking_id | INTEGER | PRIMARY KEY, NOT NULL | Khóa chính, tự tăng |
| customer_id | UUID | FOREIGN KEY, NOT NULL | Liên kết với Customer |
| restaurant_id | INTEGER | FOREIGN KEY, NOT NULL | Liên kết với RestaurantProfile |
| booking_time | TIMESTAMP | NOT NULL | Thời gian đặt bàn |
| number_of_guests | INTEGER | NOT NULL, CHECK (number_of_guests >= 1 AND number_of_guests <= 20) | Số khách |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | Trạng thái booking |
| deposit_amount | DECIMAL(18,2) | NOT NULL, DEFAULT 0 | Số tiền đặt cọc |
| note | TEXT | NULL | Ghi chú |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Thời gian tạo |
| updated_at | TIMESTAMP | NULL | Thời gian cập nhật |

**Foreign Keys**:
- customer_id → Customer(customer_id)
- restaurant_id → RestaurantProfile(restaurant_id) ON DELETE CASCADE

**Indexes**:
- PRIMARY KEY (booking_id)
- INDEX (customer_id)
- INDEX (restaurant_id)
- INDEX (booking_time)
- INDEX (status)

#### 4.3.7 Bảng Payment

**Mục đích**: Lưu trữ thông tin thanh toán đặt cọc.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| payment_id | INTEGER | PRIMARY KEY, NOT NULL | Khóa chính, tự tăng |
| customer_id | UUID | FOREIGN KEY, NOT NULL | Liên kết với Customer |
| booking_id | INTEGER | FOREIGN KEY, NOT NULL | Liên kết với Booking |
| amount | DECIMAL(18,2) | NOT NULL, CHECK (amount >= 0) | Số tiền thanh toán |
| payment_method | VARCHAR(20) | NULL | Phương thức thanh toán |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | Trạng thái thanh toán |
| voucher_id | INTEGER | FOREIGN KEY, NULL | Liên kết với Voucher |
| paid_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Thời gian thanh toán |
| payos_payment_link_id | VARCHAR(255) | NULL | PayOS Payment Link ID |
| payos_checkout_url | VARCHAR(500) | NULL | PayOS Checkout URL |
| payos_code | VARCHAR(255) | NULL | PayOS Code |
| payos_desc | VARCHAR(500) | NULL | PayOS Description |
| order_code | BIGINT | UNIQUE, NOT NULL | Mã đơn hàng |
| pay_url | VARCHAR(500) | NULL | URL thanh toán |
| ipn_raw | JSONB | NULL | Dữ liệu IPN thô |
| redirect_raw | JSONB | NULL | Dữ liệu redirect thô |
| refunded_at | TIMESTAMP | NULL | Thời gian hoàn tiền |
| refund_amount | DECIMAL(18,2) | NULL | Số tiền hoàn |
| refund_reason | VARCHAR(500) | NULL | Lý do hoàn tiền |
| payment_type | VARCHAR(20) | NOT NULL, DEFAULT 'DEPOSIT' | Loại thanh toán |

**Foreign Keys**:
- customer_id → Customer(customer_id)
- booking_id → Booking(booking_id) ON DELETE CASCADE
- voucher_id → Voucher(voucher_id)

**Indexes**:
- PRIMARY KEY (payment_id)
- UNIQUE INDEX (order_code)
- INDEX (customer_id)
- INDEX (booking_id)
- INDEX (status)

#### 4.3.8 Bảng ChatRoom

**Mục đích**: Lưu trữ thông tin phòng chat giữa các actor.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| room_id | VARCHAR(100) | PRIMARY KEY, NOT NULL | ID phòng chat |
| customer_id | UUID | FOREIGN KEY, NULL | Liên kết với Customer |
| restaurant_id | INTEGER | FOREIGN KEY, NULL | Liên kết với RestaurantProfile |
| admin_id | UUID | FOREIGN KEY, NULL | Liên kết với Admin User |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Thời gian tạo |
| last_message_at | TIMESTAMP | NULL | Thời gian tin nhắn cuối |
| is_active | BOOLEAN | DEFAULT TRUE | Trạng thái hoạt động |

**Foreign Keys**:
- customer_id → Customer(customer_id)
- restaurant_id → RestaurantProfile(restaurant_id) ON DELETE CASCADE
- admin_id → Users(id)

**Indexes**:
- PRIMARY KEY (room_id)
- INDEX (customer_id)
- INDEX (restaurant_id)
- INDEX (admin_id)

#### 4.3.9 Bảng Message

**Mục đích**: Lưu trữ tin nhắn trong các phòng chat.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| message_id | INTEGER | PRIMARY KEY, NOT NULL | Khóa chính, tự tăng |
| room_id | VARCHAR(100) | FOREIGN KEY, NOT NULL | Liên kết với ChatRoom |
| sender_id | UUID | FOREIGN KEY, NOT NULL | Liên kết với User gửi |
| message_type | VARCHAR(20) | NOT NULL, DEFAULT 'TEXT' | Loại tin nhắn |
| content | TEXT | NOT NULL | Nội dung tin nhắn |
| sent_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Thời gian gửi |
| is_read | BOOLEAN | DEFAULT FALSE | Trạng thái đã đọc |

**Foreign Keys**:
- room_id → ChatRoom(room_id) ON DELETE CASCADE
- sender_id → Users(id)

**Indexes**:
- PRIMARY KEY (message_id)
- INDEX (room_id)
- INDEX (sender_id)
- INDEX (sent_at)

#### 4.3.10 Bảng Review

**Mục đích**: Lưu trữ đánh giá của khách hàng về nhà hàng.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| review_id | INTEGER | PRIMARY KEY, NOT NULL | Khóa chính, tự tăng |
| customer_id | UUID | FOREIGN KEY, NOT NULL | Liên kết với Customer |
| restaurant_id | INTEGER | FOREIGN KEY, NOT NULL | Liên kết với RestaurantProfile |
| rating | INTEGER | NOT NULL, CHECK (rating >= 1 AND rating <= 5) | Điểm đánh giá (1-5) |
| comment | TEXT | NULL | Bình luận đánh giá |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Thời gian tạo |
| updated_at | TIMESTAMP | NULL | Thời gian cập nhật |

**Foreign Keys**:
- customer_id → Customer(customer_id)
- restaurant_id → RestaurantProfile(restaurant_id) ON DELETE CASCADE

**Indexes**:
- PRIMARY KEY (review_id)
- INDEX (customer_id)
- INDEX (restaurant_id)
- INDEX (rating)
- INDEX (created_at)

#### 4.3.11 Bảng Voucher

**Mục đích**: Lưu trữ thông tin voucher giảm giá.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| voucher_id | INTEGER | PRIMARY KEY, NOT NULL | Khóa chính, tự tăng |
| restaurant_id | INTEGER | FOREIGN KEY, NULL | Liên kết với RestaurantProfile |
| voucher_code | VARCHAR(50) | UNIQUE, NOT NULL | Mã voucher, duy nhất |
| description | TEXT | NULL | Mô tả voucher |
| discount_type | VARCHAR(20) | NOT NULL | Loại giảm giá |
| discount_value | DECIMAL(18,2) | NULL | Giá trị giảm giá |
| min_order_amount | DECIMAL(18,2) | NULL | Đơn hàng tối thiểu |
| max_discount_amount | DECIMAL(18,2) | NULL | Giảm giá tối đa |
| usage_limit | INTEGER | NULL | Giới hạn sử dụng |
| used_count | INTEGER | DEFAULT 0 | Số lần đã sử dụng |
| valid_from | TIMESTAMP | NULL | Thời gian bắt đầu hiệu lực |
| valid_until | TIMESTAMP | NULL | Thời gian kết thúc hiệu lực |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE' | Trạng thái voucher |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Thời gian tạo |
| updated_at | TIMESTAMP | NULL | Thời gian cập nhật |

**Foreign Keys**:
- restaurant_id → RestaurantProfile(restaurant_id) ON DELETE SET NULL

**Indexes**:
- PRIMARY KEY (voucher_id)
- UNIQUE INDEX (voucher_code)
- INDEX (restaurant_id)
- INDEX (status)
- INDEX (valid_from, valid_until)

### 4.4 Bảng liên kết (Junction Tables)

#### 4.4.1 Bảng BookingTable

**Mục đích**: Liên kết nhiều bàn với một booking (booking có thể sử dụng nhiều bàn).

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| booking_table_id | INTEGER | PRIMARY KEY, NOT NULL | Khóa chính, tự tăng |
| booking_id | INTEGER | FOREIGN KEY, NOT NULL | Liên kết với Booking |
| table_id | INTEGER | FOREIGN KEY, NOT NULL | Liên kết với RestaurantTable |

**Foreign Keys**:
- booking_id → Booking(booking_id) ON DELETE CASCADE
- table_id → RestaurantTable(table_id) ON DELETE CASCADE

#### 4.4.2 Bảng CustomerFavorite

**Mục đích**: Liên kết khách hàng với nhà hàng yêu thích.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| favorite_id | INTEGER | PRIMARY KEY, NOT NULL | Khóa chính, tự tăng |
| customer_id | UUID | FOREIGN KEY, NOT NULL | Liên kết với Customer |
| restaurant_id | INTEGER | FOREIGN KEY, NOT NULL | Liên kết với RestaurantProfile |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Thời gian tạo |

**Foreign Keys**:
- customer_id → Customer(customer_id) ON DELETE CASCADE
- restaurant_id → RestaurantProfile(restaurant_id) ON DELETE CASCADE

#### 4.4.3 Bảng CustomerVoucher

**Mục đích**: Liên kết khách hàng với voucher đã sử dụng.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| customer_voucher_id | INTEGER | PRIMARY KEY, NOT NULL | Khóa chính, tự tăng |
| customer_id | UUID | FOREIGN KEY, NOT NULL | Liên kết với Customer |
| voucher_id | INTEGER | FOREIGN KEY, NOT NULL | Liên kết với Voucher |
| used_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Thời gian sử dụng |

**Foreign Keys**:
- customer_id → Customer(customer_id) ON DELETE CASCADE
- voucher_id → Voucher(voucher_id) ON DELETE CASCADE

### 4.5 Constraints và Business Rules

#### 4.5.1 Check Constraints
- `RestaurantTable.capacity`: 1 <= capacity <= 20
- `Booking.number_of_guests`: 1 <= number_of_guests <= 20
- `Payment.amount`: amount >= 0
- `Review.rating`: 1 <= rating <= 5

#### 4.5.2 Unique Constraints
- `Users.username`: Duy nhất
- `Users.email`: Duy nhất
- `Payment.order_code`: Duy nhất
- `Voucher.voucher_code`: Duy nhất

#### 4.5.3 Foreign Key Constraints
- Tất cả foreign key đều có ON DELETE CASCADE hoặc ON DELETE SET NULL
- Không cho phép xóa User nếu còn Customer hoặc RestaurantOwner liên kết

### 4.6 Indexes cho Performance

#### 4.6.1 Primary Indexes
- Tất cả bảng đều có PRIMARY KEY

#### 4.6.2 Unique Indexes
- username, email trong Users
- order_code trong Payment
- voucher_code trong Voucher

#### 4.6.3 Performance Indexes
- role trong Users (để filter theo role)
- approval_status trong RestaurantProfile (để admin duyệt)
- booking_time trong Booking (để tìm booking theo thời gian)
- status trong Payment (để filter theo trạng thái thanh toán)

### 4.7 Data Types và Sizing

#### 4.7.1 UUID vs INTEGER
- **UUID**: Sử dụng cho User, Customer, RestaurantOwner (bảo mật cao)
- **INTEGER**: Sử dụng cho RestaurantProfile, Booking, Payment (performance tốt)

#### 4.7.2 DECIMAL Precision
- **DECIMAL(18,2)**: Cho tất cả các field tiền tệ (18 digits, 2 decimal places)

#### 4.7.3 VARCHAR Sizing
- **VARCHAR(255)**: Cho tên, email, URL
- **VARCHAR(100)**: Cho mã, loại
- **VARCHAR(20)**: Cho status, role
- **TEXT**: Cho mô tả, comment dài

---

*Phần Database Design này mô tả chi tiết cấu trúc cơ sở dữ liệu, bao gồm các bảng chính, bảng liên kết, constraints và indexes để đảm bảo tính toàn vẹn dữ liệu và hiệu suất truy vấn.*
