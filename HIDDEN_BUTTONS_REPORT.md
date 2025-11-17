# Báo Cáo Các Nút Bị Ẩn Trong Các Trang

## Tổng Quan
Báo cáo này liệt kê tất cả các nút (buttons) đang bị ẩn trong các trang HTML của hệ thống, bao gồm:
- Nút bị ẩn bằng CSS (`display: none`)
- Nút bị ẩn bằng JavaScript
- Nút bị ẩn theo điều kiện Thymeleaf (`th:if`/`th:unless`)
- Nút bị vô hiệu hóa (`disabled`)

---

## 1. Trang Chi Tiết Nhà Hàng (`restaurant-detail-simple.html`)

### 1.1. Nút Điều Hướng Banner (Ẩn mặc định)
- **Nút "Previous"** (`bannerPrevBtn`): `style="display: none;"`
  - Chỉ hiển thị khi có nhiều hơn 1 ảnh banner
  - Dòng 270: `<button class="banner-nav banner-nav-prev" id="bannerPrevBtn" ... style="display: none;">`

- **Nút "Next"** (`bannerNextBtn`): `style="display: none;"`
  - Chỉ hiển thị khi có nhiều hơn 1 ảnh banner
  - Dòng 273: `<button class="banner-nav banner-nav-next" id="bannerNextBtn" ... style="display: none;">`

**Lý do ẩn**: Chỉ hiển thị khi có nhiều ảnh để điều hướng.

---

## 2. Trang Đặt Bàn (`booking/form.html`)

### 2.1. Nút "Xem Sơ Đồ Bàn" (Bị Vô Hiệu Hóa)
- **Nút** (`viewTablesBtn`): `disabled`
  - Dòng 399: `<button ... id="viewTablesBtn" ... disabled>`
  - Chỉ được kích hoạt sau khi chọn nhà hàng

**Lý do**: Cần chọn nhà hàng trước để xem sơ đồ bàn.

---

## 3. Trang Quản Lý Booking (`restaurant-owner/bookings.html`)

### 3.1. Nút Điều Khiển Theo Trạng Thái Booking (Ẩn theo điều kiện)

#### Nút "Chỉnh Sửa"
- **Điều kiện**: Chỉ hiển thị khi `booking.status == 'PENDING'` hoặc `'CONFIRMED'`
- **Dòng 378-383**: `<a th:if="${booking.status.name() == 'PENDING' or booking.status.name() == 'CONFIRMED'}" ...>`

#### Nút "Xác Nhận"
- **Điều kiện**: Chỉ hiển thị khi `booking.status == 'PENDING'`
- **Dòng 386-394**: `<form th:if="${booking.status.name() == 'PENDING'}" ...>`

#### Nút "Hủy Booking"
- **Điều kiện**: Chỉ hiển thị khi `booking.status == 'PENDING'` hoặc `'CONFIRMED'`
- **Dòng 397-403**: `<button th:if="${booking.status.name() == 'PENDING' or booking.status.name() == 'CONFIRMED'}" ...>`

#### Nút "No Show"
- **Điều kiện**: Chỉ hiển thị khi `booking.status == 'CONFIRMED'`
- **Dòng 405-413**: `<form th:if="${booking.status.name() == 'CONFIRMED'}" ...>`

#### Nút "Check-in"
- **Điều kiện**: Chỉ hiển thị khi `booking.status == 'CONFIRMED'` hoặc `'COMPLETED'`
- **Dòng 416-421**: `<button th:if="${booking.status.name() == 'CONFIRMED' or booking.status.name() == 'COMPLETED'}" ...>`

#### Nút "Check-out"
- **Điều kiện**: Chỉ hiển thị khi `booking.status == 'CONFIRMED'` hoặc `'COMPLETED'`
- **Dòng 423-428**: `<button th:if="${booking.status.name() == 'CONFIRMED' or booking.status.name() == 'COMPLETED'}" ...>`

**Lý do**: Mỗi nút chỉ hiển thị khi trạng thái booking phù hợp với hành động.

---

## 4. Trang Danh Sách Booking (`booking/list.html`)

### 4.1. Nút Điều Khiển Theo Trạng Thái (Ẩn theo điều kiện)

#### Nút "Chỉnh Sửa" (Edit)
- **Điều kiện**: Chỉ hiển thị khi `booking.canBeEdited() == true`
- **Dòng 169-175**: `<a th:if="${booking.canBeEdited()}" ...>`

#### Nút "Thanh Toán" (Complete Payment)
- **Điều kiện**: Chỉ hiển thị khi `booking.status == 'PENDING'` và `booking.hasDeposit() == true`
- **Dòng 176-182**: `<a th:if="${booking.status != null and booking.status.name() == 'PENDING' and booking.hasDeposit()}" ...>`

#### Nút "Hủy" (Cancel)
- **Điều kiện**: Chỉ hiển thị khi `booking.canBeCancelled() == true`
- **Dòng 183-192**: `<button th:if="${booking.canBeCancelled()}" ...>`

**Lý do**: Mỗi nút chỉ hiển thị khi booking ở trạng thái phù hợp.

---

## 5. Header/Fragments (`fragments/header.html`)

### 5.1. Nút Chat (Ẩn bằng JavaScript)
- **Nút** (`chat-button`): Ẩn bằng JavaScript khi ở trang chat
- **Dòng 298**: `document.getElementById('chat-button').style.display = 'none';`
- **Điều kiện**: Ẩn khi URL chứa `/customer/chat`, `/restaurant-owner/chat`, hoặc `/admin/chat`

**Lý do**: Tránh hiển thị nút chat khi đang ở trang chat.

---

## 6. AI Chat Widget (`fragments/ai-chat-widget.html`)

### 6.1. Nút Toggle Chat (Ẩn/Hiện động)
- **Nút Toggle** (`aiChatToggle`): Ẩn khi chat window mở
  - Dòng 538: `aiChatToggle.style.display = 'none';` (khi mở chat)
  - Dòng 549: `aiChatToggle.style.display = 'flex';` (khi đóng chat)

- **Badge** (`aiChatBadge`): Ẩn khi mở chat hoặc đã đọc tin nhắn
  - Dòng 543: `aiChatBadge.style.display = 'none';`

- **Chat Window** (`aiChatWindow`): Ẩn mặc định, hiện khi mở
  - Dòng 548: `aiChatWindow.style.display = 'none';` (mặc định)

**Lý do**: Quản lý trạng thái mở/đóng của widget chat.

---

## 7. Trang Đăng Ký Tài Khoản OAuth (`auth/oauth-account-type.html`)

### 7.1. Nút Tiếp Tục (Bị Vô Hiệu Hóa)
- **Nút** (`continueBtn`): `disabled`
  - Dòng 231: `<button type="submit" class="btn btn-continue" id="continueBtn" disabled>`
  - Chỉ được kích hoạt sau khi chọn loại tài khoản

**Lý do**: Cần chọn loại tài khoản trước khi tiếp tục.

---

## 8. Trang Upload Giấy Phép Kinh Doanh (`restaurant-owner/business-license-upload.html`)

### 8.1. Nút Submit (Bị Vô Hiệu Hóa)
- **Nút** (`submitBtn`): `disabled`
  - Dòng 306: `<button type="submit" class="btn btn-upload" id="submitBtn" disabled>`
  - Chỉ được kích hoạt sau khi upload file

**Lý do**: Cần upload file trước khi submit.

---

## 9. Modal Tài Khoản Ngân Hàng (`customer/bank-account-modal.html`)

### 9.1. Nút Hủy (Bị Vô Hiệu Hóa)
- **Nút** (`submitCancelBtn`): `disabled`
  - Dòng 113: `<button type="submit" class="ds-btn ds-btn-primary" id="submitCancelBtn" disabled>`
  - Chỉ được kích hoạt khi có dữ liệu hợp lệ

**Lý do**: Cần nhập thông tin hợp lệ trước khi hủy.

---

## 10. Các Nút Bị Ẩn Theo Vai Trò Người Dùng

### 10.1. Trang Home (`public/home.html`)
- **Nút "Đăng Nhập"**: Chỉ hiển thị khi chưa đăng nhập (`sec:authorize="!isAuthenticated()"`)
- **Nút "Đăng Ký"**: Chỉ hiển thị khi chưa đăng nhập (`sec:authorize="!isAuthenticated()"`)
- **Nút "Dashboard" (Restaurant Owner)**: Chỉ hiển thị khi có role `RESTAURANT_OWNER`
- **Nút "Admin"**: Chỉ hiển thị khi có role `ADMIN`

### 10.2. Header (`fragments/header.html`)
- **Nút "Sign In"**: Chỉ hiển thị khi chưa đăng nhập (`sec:authorize="!isAuthenticated()"`)
- **Nút "Đăng Ký"**: Chỉ hiển thị khi chưa đăng nhập (`sec:authorize="!isAuthenticated()"`)
- **Menu "My Reservations"**: Chỉ hiển thị khi đã đăng nhập và không phải Restaurant Owner (`sec:authorize="isAuthenticated() and !hasRole('RESTAURANT_OWNER')"`)

---

## 11. Các Nút Bị Vô Hiệu Hóa Tạm Thời (Disabled)

### 11.1. Trong Quá Trình Xử Lý
Nhiều nút bị vô hiệu hóa tạm thời khi đang xử lý request:
- Nút submit trong các form (khi đang submit)
- Nút thanh toán (khi đang xử lý thanh toán)
- Nút upload (khi đang upload file)
- Nút tìm kiếm (khi đang tìm kiếm)

**Vị trí**: 
- `restaurant-owner/bookings.html` (dòng 1815, 1842, 1849)
- `booking/list.html` (dòng 738, 762, 769)
- `restaurant-owner/restaurant-form.html` (dòng 1611, 1651)
- `public/restaurant-detail-simple.html` (nhiều dòng)
- `payment/result.html` (nhiều dòng)
- Và nhiều file khác

---

## Tổng Kết

### Phân Loại Theo Cách Ẩn:

1. **Ẩn bằng CSS (`display: none`)**: 
   - Nút điều hướng banner (restaurant-detail-simple.html)
   - Nút chat toggle (ai-chat-widget.html)

2. **Ẩn bằng JavaScript**:
   - Nút chat trong header (khi ở trang chat)
   - Nhiều nút được ẩn/hiện động theo tương tác người dùng

3. **Ẩn theo điều kiện Thymeleaf (`th:if`/`th:unless`)**:
   - Nút điều khiển booking theo trạng thái
   - Nút theo vai trò người dùng (Admin, Restaurant Owner, Customer)
   - Nút chỉnh sửa/hủy booking theo điều kiện

4. **Bị vô hiệu hóa (`disabled`)**:
   - Nút cần điều kiện trước khi kích hoạt (chọn nhà hàng, upload file, v.v.)
   - Nút trong quá trình xử lý request

### Khuyến Nghị:

1. **Kiểm tra logic ẩn/hiện**: Đảm bảo các nút được hiển thị đúng lúc
2. **Cải thiện UX**: Thêm tooltip hoặc thông báo giải thích tại sao nút bị ẩn/disabled
3. **Accessibility**: Đảm bảo các nút ẩn không ảnh hưởng đến screen readers
4. **Testing**: Test các trường hợp edge case để đảm bảo nút hiển thị đúng

---

**Ngày tạo**: $(date)
**Phiên bản**: 1.0























