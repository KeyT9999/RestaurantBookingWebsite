# REQUIREMENT & DESIGN SPECIFICATION (RDS)
## HỆ THỐNG ĐẶT BÀN NHÀ HÀNG - RESTAURANT BOOKING PLATFORM

---

## 3. USE CASES (CA SỬ DỤNG)

### 3.1 Bảng Tổng quan Use Cases

| UC ID | Tên Use Case | Actor chính | Actor phụ | Mô tả ngắn gọn | Độ ưu tiên |
|-------|--------------|-------------|-----------|----------------|-------------|
| UC-01 | Đăng ký tài khoản | Customer | Email Service | Khách hàng tạo tài khoản mới | High |
| UC-02 | Đăng nhập hệ thống | Customer | Google OAuth | Khách hàng đăng nhập vào hệ thống | High |
| UC-03 | Đăng nhập Google | Customer | Google OAuth Service | Đăng nhập nhanh qua Google OAuth | Medium |
| UC-04 | Quên mật khẩu | Customer | Email Service | Khách hàng đặt lại mật khẩu | Medium |
| UC-05 | Tìm kiếm nhà hàng | Customer | - | Khách hàng tìm kiếm nhà hàng theo tiêu chí | High |
| UC-06 | Xem chi tiết nhà hàng | Customer | - | Khách hàng xem thông tin chi tiết nhà hàng | High |
| UC-07 | Đặt bàn | Customer | PayOS Payment Gateway | Khách hàng đặt bàn và thanh toán đặt cọc | High |
| UC-08 | Xem danh sách booking | Customer | - | Khách hàng xem lịch sử đặt bàn | High |
| UC-09 | Chỉnh sửa booking | Customer | - | Khách hàng cập nhật thông tin đặt bàn | Medium |
| UC-10 | Hủy booking | Customer | PayOS Payment Gateway | Khách hàng hủy đặt bàn và yêu cầu hoàn tiền | Medium |
| UC-11 | Đánh giá nhà hàng | Customer | - | Khách hàng đánh giá và bình luận về nhà hàng | Medium |
| UC-12 | Sử dụng voucher | Customer | - | Khách hàng áp dụng voucher giảm giá | Low |
| UC-13 | Quản lý yêu thích | Customer | - | Khách hàng thêm/bỏ nhà hàng khỏi danh sách yêu thích | Low |
| UC-14 | Chat với nhà hàng | Customer | WebSocket Service | Khách hàng chat trực tiếp với nhà hàng | Medium |
| UC-15 | Đăng ký nhà hàng | Restaurant Owner | Admin | Chủ nhà hàng đăng ký tham gia nền tảng | High |
| UC-16 | Duyệt nhà hàng | Admin | Restaurant Owner | Admin duyệt yêu cầu đăng ký nhà hàng | High |
| UC-17 | Quản lý thông tin nhà hàng | Restaurant Owner | Cloudinary Service | Chủ nhà hàng cập nhật thông tin nhà hàng | High |
| UC-18 | Quản lý bàn | Restaurant Owner | - | Chủ nhà hàng thêm/sửa/xóa bàn | High |
| UC-19 | Duyệt booking | Restaurant Owner | - | Chủ nhà hàng xác nhận/từ chối booking | High |
| UC-20 | Quản lý waitlist | Restaurant Owner | - | Chủ nhà hàng quản lý danh sách chờ | Medium |
| UC-21 | Xem báo cáo doanh thu | Restaurant Owner | - | Chủ nhà hàng xem thống kê doanh thu | Medium |
| UC-22 | Yêu cầu rút tiền | Restaurant Owner | Admin | Chủ nhà hàng yêu cầu rút tiền doanh thu | Medium |
| UC-23 | Duyệt rút tiền | Admin | PayOS Payment Gateway | Admin duyệt yêu cầu rút tiền | High |
| UC-24 | Quản lý người dùng | Admin | - | Admin khóa/mở khóa tài khoản người dùng | High |
| UC-25 | Quản lý voucher | Admin | - | Admin tạo và quản lý voucher toàn hệ thống | Medium |
| UC-26 | Chat hỗ trợ | Admin | WebSocket Service | Admin chat hỗ trợ với nhà hàng | Medium |

### 3.2 Chi tiết các Use Case chính

#### 3.2.1 UC-01: Đăng ký tài khoản

**Mô tả**: Khách hàng tạo tài khoản mới để sử dụng hệ thống đặt bàn.

**Actor chính**: Customer  
**Actor phụ**: Email Service

**Trigger**: Khách hàng nhấn nút "Đăng ký" trên trang web.

**Preconditions**:
- Khách hàng chưa có tài khoản trong hệ thống
- Email chưa được sử dụng bởi tài khoản khác

**Postconditions**:
- Tài khoản mới được tạo với trạng thái chưa xác thực
- Email xác thực được gửi đến khách hàng

**Normal Flow**:
1. Khách hàng truy cập trang đăng ký
2. Hệ thống hiển thị form đăng ký
3. Khách hàng nhập thông tin: username, email, password, full_name, phone
4. Hệ thống validate dữ liệu đầu vào
5. Hệ thống kiểm tra username và email chưa tồn tại
6. Hệ thống tạo tài khoản mới
7. Hệ thống gửi email xác thực
8. Hệ thống hiển thị thông báo thành công

**Alternative Flows**:
- 4a. Dữ liệu không hợp lệ: Hệ thống hiển thị lỗi validation
- 5a. Username/email đã tồn tại: Hệ thống hiển thị thông báo lỗi
- 7a. Gửi email thất bại: Hệ thống ghi log lỗi và thông báo cho khách hàng

**Business Rules**:
- BR-01: Username phải từ 3-50 ký tự
- BR-02: Password phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt
- BR-03: Email phải thuộc domain được phép: @gmail.com, @outlook.com.vn, @yahoo.com, @hotmail.com, @student.ctu.edu.vn, @ctu.edu.vn
- BR-04: Số điện thoại phải là 10 số và bắt đầu bằng 03, 05, 07, 08, 09

#### 3.2.2 UC-02: Đăng nhập hệ thống

**Mô tả**: Khách hàng đăng nhập vào hệ thống bằng username/email và password.

**Actor chính**: Customer  
**Actor phụ**: Rate Limiting Service

**Trigger**: Khách hàng nhấn nút "Đăng nhập" với thông tin đăng nhập.

**Preconditions**:
- Tài khoản khách hàng đã tồn tại
- Tài khoản chưa bị khóa

**Postconditions**:
- Khách hàng đăng nhập thành công và được chuyển hướng
- Thời gian đăng nhập cuối được cập nhật

**Normal Flow**:
1. Khách hàng truy cập trang đăng nhập
2. Hệ thống hiển thị form đăng nhập
3. Khách hàng nhập username/email và password
4. Hệ thống validate dữ liệu
5. Hệ thống kiểm tra rate limiting
6. Hệ thống xác thực thông tin đăng nhập
7. Hệ thống cập nhật last_login
8. Hệ thống chuyển hướng đến trang chủ

**Alternative Flows**:
- 4a. Dữ liệu không hợp lệ: Hệ thống hiển thị lỗi validation
- 5a. Vượt quá rate limit: Hệ thống chặn IP tạm thời
- 6a. Thông tin đăng nhập sai: Hệ thống tăng số lần thử sai
- 6b. Tài khoản bị khóa: Hệ thống hiển thị thông báo tài khoản bị khóa
- 6c. Email chưa xác thực: Hệ thống yêu cầu xác thực email

**Business Rules**:
- BR-05: Tài khoản bị khóa sau 6 lần đăng nhập sai liên tiếp
- BR-06: Rate limit: tối đa 5 lần đăng nhập trong 1 phút
- BR-07: Tài khoản phải xác thực email mới có thể đăng nhập

#### 3.2.3 UC-07: Đặt bàn

**Mô tả**: Khách hàng đặt bàn tại nhà hàng và thanh toán đặt cọc.

**Actor chính**: Customer  
**Actor phụ**: PayOS Payment Gateway, Restaurant Owner

**Trigger**: Khách hàng nhấn nút "Đặt bàn" sau khi chọn nhà hàng và thông tin.

**Preconditions**:
- Khách hàng đã đăng nhập
- Nhà hàng đã được duyệt và hoạt động
- Bàn được chọn còn trống

**Postconditions**:
- Booking mới được tạo với trạng thái PENDING
- Payment được tạo và chờ thanh toán
- Restaurant Owner nhận thông báo booking mới

**Normal Flow**:
1. Khách hàng chọn nhà hàng và xem chi tiết
2. Khách hàng chọn ngày, giờ và số khách
3. Hệ thống hiển thị danh sách bàn có sẵn
4. Khách hàng chọn bàn
5. Khách hàng nhập thông tin bổ sung (note)
6. Hệ thống tính toán số tiền đặt cọc
7. Hệ thống tạo booking với trạng thái PENDING
8. Hệ thống tạo payment và redirect đến PayOS
9. Khách hàng thanh toán qua PayOS
10. PayOS gửi webhook xác nhận thanh toán
11. Hệ thống cập nhật trạng thái payment thành PAID
12. Restaurant Owner nhận thông báo

**Alternative Flows**:
- 3a. Không có bàn trống: Hệ thống đề xuất tham gia waitlist
- 6a. Số tiền đặt cọc = 0: Hệ thống tự động xác nhận booking
- 8a. Tạo payment thất bại: Hệ thống rollback booking và hiển thị lỗi
- 10a. Thanh toán thất bại: Hệ thống giữ booking với trạng thái PENDING
- 10b. Webhook timeout: Hệ thống đánh dấu cần kiểm tra thủ công

**Business Rules**:
- BR-08: Thời gian đặt bàn phải >= hiện tại + 30 phút
- BR-09: Số khách từ 1-20 người
- BR-10: Đặt cọc = 10% tổng tiền nếu > 500k VNĐ, ngược lại = 10k VNĐ
- BR-11: Không cho phép trùng bàn trong khung 2 giờ
- BR-12: Ghi chú tối đa 500 ký tự

#### 3.2.4 UC-19: Duyệt booking

**Mô tả**: Restaurant Owner xác nhận hoặc từ chối booking từ khách hàng.

**Actor chính**: Restaurant Owner  
**Actor phụ**: Customer, Email Service

**Trigger**: Restaurant Owner nhấn nút "Xác nhận" hoặc "Từ chối" booking.

**Preconditions**:
- Booking ở trạng thái PENDING
- Payment đã được thanh toán thành công
- Restaurant Owner đã đăng nhập

**Postconditions**:
- Trạng thái booking được cập nhật
- Khách hàng nhận thông báo kết quả
- Bàn được cập nhật trạng thái nếu được xác nhận

**Normal Flow**:
1. Restaurant Owner đăng nhập và truy cập dashboard
2. Hệ thống hiển thị danh sách booking PENDING
3. Restaurant Owner chọn booking cần duyệt
4. Hệ thống hiển thị chi tiết booking
5. Restaurant Owner chọn "Xác nhận" hoặc "Từ chối"
6. Nếu từ chối: Restaurant Owner nhập lý do
7. Hệ thống cập nhật trạng thái booking
8. Hệ thống cập nhật trạng thái bàn (nếu xác nhận)
9. Hệ thống gửi email thông báo cho khách hàng
10. Hệ thống hiển thị thông báo thành công

**Alternative Flows**:
- 6a. Từ chối không có lý do: Hệ thống yêu cầu nhập lý do bắt buộc
- 7a. Cập nhật thất bại: Hệ thống rollback và hiển thị lỗi
- 9a. Gửi email thất bại: Hệ thống ghi log và thông báo

**Business Rules**:
- BR-13: Restaurant Owner phải duyệt booking trong vòng 2 giờ
- BR-14: Nếu từ chối, phải có lý do rõ ràng
- BR-15: Booking tự động hủy nếu không được duyệt trong 24 giờ

#### 3.2.5 UC-16: Duyệt nhà hàng

**Mô tả**: Admin duyệt yêu cầu đăng ký nhà hàng mới.

**Actor chính**: Admin  
**Actor phụ**: Restaurant Owner, Email Service

**Trigger**: Admin nhấn nút "Duyệt" hoặc "Từ chối" yêu cầu đăng ký nhà hàng.

**Preconditions**:
- Nhà hàng đã gửi yêu cầu đăng ký với đầy đủ thông tin
- Giấy phép kinh doanh đã được upload
- Admin đã đăng nhập

**Postconditions**:
- Trạng thái nhà hàng được cập nhật
- Restaurant Owner nhận thông báo kết quả
- Nếu duyệt: Nhà hàng có thể nhận booking

**Normal Flow**:
1. Admin đăng nhập và truy cập dashboard
2. Hệ thống hiển thị danh sách nhà hàng chờ duyệt
3. Admin chọn nhà hàng cần duyệt
4. Hệ thống hiển thị thông tin chi tiết nhà hàng
5. Admin xem giấy phép kinh doanh
6. Admin chọn "Duyệt" hoặc "Từ chối"
7. Nếu từ chối: Admin nhập lý do
8. Hệ thống cập nhật trạng thái nhà hàng
9. Hệ thống gửi email thông báo cho Restaurant Owner
10. Hệ thống hiển thị thông báo thành công

**Alternative Flows**:
- 5a. Không có giấy phép: Admin từ chối với lý do thiếu giấy tờ
- 7a. Từ chối không có lý do: Hệ thống yêu cầu nhập lý do bắt buộc
- 8a. Cập nhật thất bại: Hệ thống rollback và hiển thị lỗi

**Business Rules**:
- BR-16: Nhà hàng phải có giấy phép kinh doanh hợp lệ
- BR-17: Admin phải duyệt trong vòng 3 ngày làm việc
- BR-18: Nếu từ chối, Restaurant Owner có thể nộp lại sau 30 ngày

### 3.3 Use Cases theo Actor

#### 3.3.1 Customer Use Cases
- UC-01: Đăng ký tài khoản
- UC-02: Đăng nhập hệ thống
- UC-03: Đăng nhập Google
- UC-04: Quên mật khẩu
- UC-05: Tìm kiếm nhà hàng
- UC-06: Xem chi tiết nhà hàng
- UC-07: Đặt bàn
- UC-08: Xem danh sách booking
- UC-09: Chỉnh sửa booking
- UC-10: Hủy booking
- UC-11: Đánh giá nhà hàng
- UC-12: Sử dụng voucher
- UC-13: Quản lý yêu thích
- UC-14: Chat với nhà hàng

#### 3.3.2 Restaurant Owner Use Cases
- UC-15: Đăng ký nhà hàng
- UC-17: Quản lý thông tin nhà hàng
- UC-18: Quản lý bàn
- UC-19: Duyệt booking
- UC-20: Quản lý waitlist
- UC-21: Xem báo cáo doanh thu
- UC-22: Yêu cầu rút tiền

#### 3.3.3 Admin Use Cases
- UC-16: Duyệt nhà hàng
- UC-23: Quản lý người dùng
- UC-24: Quản lý voucher
- UC-25: Chat hỗ trợ

---

*Phần Use Cases này mô tả chi tiết các chức năng chính của hệ thống, bao gồm luồng xử lý bình thường, các luồng thay thế và quy tắc nghiệp vụ.*
