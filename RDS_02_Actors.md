# REQUIREMENT & DESIGN SPECIFICATION (RDS)
## HỆ THỐNG ĐẶT BÀN NHÀ HÀNG - RESTAURANT BOOKING PLATFORM

---

## 2. ACTORS (TÁC NHÂN)

### 2.1 Bảng Tổng quan Actors

| Actor ID | Tên Actor | Mô tả | Vai trò trong hệ thống |
|----------|-----------|-------|----------------------|
| ACT-01 | Customer (Khách hàng) | Người dùng cuối muốn đặt bàn ăn uống tại nhà hàng | Sử dụng hệ thống để tìm kiếm và đặt bàn |
| ACT-02 | Restaurant Owner (Chủ nhà hàng) | Chủ sở hữu hoặc người quản lý nhà hàng đối tác | Quản lý nhà hàng và xử lý booking |
| ACT-03 | Admin (Quản trị viên) | Người quản lý toàn bộ hệ thống | Giám sát và điều hành hệ thống |
| ACT-04 | Guest User (Khách mời) | Người dùng chưa đăng ký tài khoản | Xem thông tin nhà hàng, không thể đặt bàn |
| ACT-05 | PayOS Payment Gateway | Hệ thống thanh toán trực tuyến PayOS | Xử lý thanh toán đặt cọc |
| ACT-06 | Google OAuth Service | Dịch vụ xác thực Google | Cung cấp đăng nhập qua Google |
| ACT-07 | Cloudinary Service | Dịch vụ lưu trữ và xử lý hình ảnh | Quản lý file media của nhà hàng |
| ACT-08 | Email Service | Dịch vụ gửi email | Gửi thông báo và xác thực |
| ACT-09 | WebSocket Service | Dịch vụ chat real-time | Hỗ trợ chat giữa các actor |
| ACT-10 | Rate Limiting Service | Dịch vụ giới hạn tần suất truy cập | Bảo vệ hệ thống khỏi tấn công |

### 2.2 Chi tiết các Actor chính

#### 2.2.1 ACT-01: Customer (Khách hàng)

**Mô tả**: Người dùng cuối sử dụng hệ thống để tìm kiếm và đặt bàn tại nhà hàng.

**Đặc điểm**:
- Có tài khoản đã đăng ký và xác thực
- Có khả năng thanh toán trực tuyến
- Sử dụng thiết bị có kết nối internet

**Chức năng chính**:
- Tìm kiếm và lọc nhà hàng
- Xem thông tin chi tiết nhà hàng
- Đặt bàn và thanh toán đặt cọc
- Quản lý booking cá nhân
- Đánh giá và bình luận
- Sử dụng voucher giảm giá
- Chat với nhà hàng
- Quản lý danh sách yêu thích

**Quyền hạn**:
- Truy cập tất cả thông tin công khai của nhà hàng
- Tạo và quản lý booking của mình
- Thực hiện thanh toán qua PayOS
- Tương tác với nhà hàng qua chat

**Ràng buộc**:
- Phải đăng ký tài khoản để đặt bàn
- Chỉ có thể chỉnh sửa/hủy booking trong trạng thái PENDING/CONFIRMED
- Thanh toán đặt cọc bắt buộc để xác nhận booking

#### 2.2.2 ACT-02: Restaurant Owner (Chủ nhà hàng)

**Mô tả**: Chủ sở hữu hoặc người quản lý nhà hàng muốn tham gia nền tảng.

**Đặc điểm**:
- Có giấy phép kinh doanh hợp lệ
- Có khả năng quản lý và vận hành nhà hàng
- Có tài khoản ngân hàng để nhận thanh toán

**Chức năng chính**:
- Đăng ký và tạo hồ sơ nhà hàng
- Upload giấy tờ pháp lý
- Quản lý thông tin nhà hàng (menu, bàn, dịch vụ)
- Duyệt và xác nhận booking
- Quản lý trạng thái bàn
- Xem báo cáo doanh thu
- Quản lý waitlist
- Tạo voucher giảm giá
- Yêu cầu rút tiền

**Quyền hạn**:
- Quản lý toàn bộ thông tin nhà hàng của mình
- Duyệt/từ chối booking từ khách hàng
- Cập nhật trạng thái bàn real-time
- Xem thống kê và báo cáo doanh thu

**Ràng buộc**:
- Phải được admin duyệt mới có thể nhận booking
- Phải ký hợp đồng với nền tảng
- Chỉ có thể quản lý nhà hàng của mình

#### 2.2.3 ACT-03: Admin (Quản trị viên)

**Mô tả**: Người quản lý toàn bộ hệ thống, đảm bảo chất lượng và an toàn.

**Đặc điểm**:
- Có quyền cao nhất trong hệ thống
- Có kiến thức về quản lý và vận hành hệ thống
- Có trách nhiệm giám sát và bảo trì hệ thống

**Chức năng chính**:
- Duyệt và phê duyệt nhà hàng mới
- Quản lý danh sách người dùng
- Xử lý yêu cầu rút tiền
- Quản lý voucher toàn hệ thống
- Xử lý báo cáo và khiếu nại
- Giám sát bảo mật và hiệu suất
- Chat hỗ trợ với nhà hàng
- Quản lý danh sách ngân hàng

**Quyền hạn**:
- Truy cập toàn bộ dữ liệu hệ thống
- Khóa/mở khóa tài khoản người dùng
- Duyệt/từ chối yêu cầu rút tiền
- Quản lý cấu hình hệ thống

**Ràng buộc**:
- Phải có quyền admin được cấp bởi super admin
- Mọi thao tác đều được ghi log audit
- Không thể tự xóa tài khoản admin

### 2.3 Các Actor phụ trợ (Supporting Actors)

#### 2.3.1 ACT-04: Guest User (Khách mời)

**Mô tả**: Người dùng chưa đăng ký tài khoản, chỉ có thể xem thông tin công khai.

**Chức năng**:
- Xem danh sách nhà hàng
- Xem thông tin chi tiết nhà hàng
- Xem menu và đánh giá

**Hạn chế**:
- Không thể đặt bàn
- Không thể sử dụng các tính năng cá nhân hóa

#### 2.3.2 ACT-05: PayOS Payment Gateway

**Mô tả**: Hệ thống thanh toán trực tuyến của Việt Nam, xử lý các giao dịch đặt cọc.

**Chức năng**:
- Tạo payment link
- Xử lý thanh toán
- Gửi webhook notification
- Hỗ trợ hoàn tiền

**Tích hợp**:
- RESTful API
- Webhook callback
- IPN (Instant Payment Notification)

#### 2.3.3 ACT-06: Google OAuth Service

**Mô tả**: Dịch vụ xác thực của Google, cho phép đăng nhập nhanh.

**Chức năng**:
- Xác thực người dùng
- Cung cấp thông tin profile cơ bản
- Tạo tài khoản tự động

#### 2.3.4 ACT-07: Cloudinary Service

**Mô tả**: Dịch vụ lưu trữ và xử lý hình ảnh trên cloud.

**Chức năng**:
- Upload và lưu trữ hình ảnh
- Tự động resize và optimize
- CDN delivery

#### 2.3.5 ACT-08: Email Service

**Mô tả**: Dịch vụ gửi email thông báo và xác thực.

**Chức năng**:
- Gửi email xác thực tài khoản
- Gửi thông báo booking
- Gửi thông báo hệ thống

#### 2.3.6 ACT-09: WebSocket Service

**Mô tả**: Dịch vụ chat real-time giữa các actor.

**Chức năng**:
- Chat giữa customer và restaurant
- Chat giữa admin và restaurant
- Thông báo real-time

#### 2.3.7 ACT-10: Rate Limiting Service

**Mô tả**: Dịch vụ bảo vệ hệ thống khỏi tấn công và spam.

**Chức năng**:
- Giới hạn số lượng request
- Phát hiện và chặn IP đáng ngờ
- Bảo vệ khỏi brute force attack

### 2.4 Mối quan hệ giữa các Actors

```
Customer ←→ Restaurant Owner
    ↓              ↓
    ↓         Admin ←→ Restaurant Owner
    ↓              ↓
    ↓         PayOS ←→ Restaurant Owner
    ↓              ↓
    ↓         Google OAuth
    ↓              ↓
    ↓         Cloudinary
    ↓              ↓
    ↓         Email Service
    ↓              ↓
    ↓         WebSocket Service
    ↓              ↓
    ↓         Rate Limiting Service
```

### 2.5 Phân quyền Actors

| Actor | Role | Quyền truy cập |
|-------|------|----------------|
| Customer | ROLE_CUSTOMER | Customer features, booking management |
| Restaurant Owner | ROLE_RESTAURANT_OWNER | Restaurant management, booking approval |
| Admin | ROLE_ADMIN | Full system access, user management |
| Guest | ROLE_ANONYMOUS | Public information only |

---

*Phần Actors này định nghĩa rõ các tác nhân tham gia vào hệ thống, vai trò và quyền hạn của từng actor, cũng như mối quan hệ giữa chúng.*
