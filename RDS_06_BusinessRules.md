# REQUIREMENT & DESIGN SPECIFICATION (RDS)
## HỆ THỐNG ĐẶT BÀN NHÀ HÀNG - RESTAURANT BOOKING PLATFORM

---

## 6. BUSINESS RULES (QUY TẮC NGHIỆP VỤ)

### 6.1 Tổng quan Business Rules

**Mục đích**: Định nghĩa các quy tắc nghiệp vụ (business rules) chi phối hoạt động của hệ thống đặt bàn nhà hàng, đảm bảo tính nhất quán và tuân thủ các chính sách kinh doanh.

### 6.2 Bảng tổng quan Business Rules

| BR ID | Tên Business Rule | Mô tả | Áp dụng cho UC | Trạng thái |
|-------|-------------------|-------|----------------|------------|
| BR-01 | Username Validation | Username phải từ 3-50 ký tự | UC-01 | Active |
| BR-02 | Password Complexity | Password phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt | UC-01, UC-02 | Active |
| BR-03 | Email Domain Validation | Email phải thuộc domain được phép | UC-01 | Active |
| BR-04 | Phone Number Format | Số điện thoại phải là 10 số và bắt đầu bằng 03, 05, 07, 08, 09 | UC-01, UC-05 | Active |
| BR-05 | Account Lock Policy | Tài khoản bị khóa sau 6 lần đăng nhập sai liên tiếp | UC-02 | Active |
| BR-06 | Rate Limiting Policy | Tối đa 5 lần đăng nhập trong 1 phút | UC-02 | Active |
| BR-07 | Email Verification | Tài khoản phải xác thực email mới có thể đăng nhập | UC-01, UC-02 | Active |
| BR-08 | Booking Time Validation | Thời gian đặt bàn phải >= hiện tại + 30 phút | UC-07 | Active |
| BR-09 | Guest Count Limit | Số khách từ 1-20 người | UC-07 | Active |
| BR-10 | Deposit Calculation | Đặt cọc = 10% tổng nếu > 500k VNĐ, ngược lại = 10k VNĐ | UC-07 | Active |
| BR-11 | Table Conflict Prevention | Không cho phép trùng bàn trong khung 2 giờ | UC-07 | Active |
| BR-12 | Note Length Limit | Ghi chú tối đa 500 ký tự | UC-07, UC-09 | Active |
| BR-13 | Booking Approval Timeout | Restaurant Owner phải duyệt booking trong vòng 2 giờ | UC-19 | Active |
| BR-14 | Rejection Reason Required | Nếu từ chối booking, phải có lý do rõ ràng | UC-19 | Active |
| BR-15 | Auto Cancel Policy | Booking tự động hủy nếu không được duyệt trong 24 giờ | UC-19 | Active |
| BR-16 | Business License Required | Nhà hàng phải có giấy phép kinh doanh hợp lệ | UC-15, UC-16 | Active |
| BR-17 | Admin Approval Deadline | Admin phải duyệt nhà hàng trong vòng 3 ngày làm việc | UC-16 | Active |
| BR-18 | Resubmission Policy | Nếu từ chối, Restaurant Owner có thể nộp lại sau 30 ngày | UC-16 | Active |
| BR-19 | Rating Scale | Điểm đánh giá từ 1-5 sao | UC-11 | Active |
| BR-20 | Voucher Usage Limit | Mỗi voucher có giới hạn sử dụng | UC-12 | Active |
| BR-21 | Commission Rate | Admin giữ 3% commission từ mỗi giao dịch | UC-22, UC-23 | Active |
| BR-22 | Minimum Withdrawal | Số tiền rút tối thiểu 100k VNĐ | UC-22 | Active |
| BR-23 | Withdrawal Processing Time | Admin xử lý rút tiền trong 24 giờ làm việc | UC-23 | Active |

### 6.3 Chi tiết Business Rules

#### 6.3.1 Authentication & User Management Rules

**BR-01: Username Validation**
- **Mô tả**: Username phải từ 3-50 ký tự
- **Validation**: `@Size(min = 3, max = 50, message = "Username phải từ 3-50 ký tự")`
- **Áp dụng**: UC-01 (Đăng ký tài khoản)
- **Message**: MSG01 - "Username phải từ 3-50 ký tự"

**BR-02: Password Complexity**
- **Mô tả**: Password phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt
- **Validation**: `@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")`
- **Áp dụng**: UC-01, UC-02, UC-06
- **Message**: MSG02 - "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt"

**BR-03: Email Domain Validation**
- **Mô tả**: Email phải thuộc domain được phép: @gmail.com, @outlook.com.vn, @yahoo.com, @hotmail.com, @student.ctu.edu.vn, @ctu.edu.vn
- **Validation**: `@Pattern(regexp = ".*@(gmail\\.com|outlook\\.com\\.vn|yahoo\\.com|hotmail\\.com|student\\.ctu\\.edu\\.vn|ctu\\.edu\\.vn)$")`
- **Áp dụng**: UC-01
- **Message**: MSG03 - "Email phải thuộc một trong các domain được phép"

**BR-04: Phone Number Format**
- **Mô tả**: Số điện thoại phải là 10 số và bắt đầu bằng 03, 05, 07, 08, 09 hoặc để trống
- **Validation**: `@Pattern(regexp = "^(0[3|5|7|8|9])[0-9]{8}$|^$")`
- **Áp dụng**: UC-01, UC-05
- **Message**: MSG04 - "Số điện thoại phải là 10 số và bắt đầu bằng 03, 05, 07, 08, 09"

**BR-05: Account Lock Policy**
- **Mô tả**: Tài khoản bị khóa sau 6 lần đăng nhập sai liên tiếp
- **Implementation**: Spring Security + Rate Limiting
- **Áp dụng**: UC-02
- **Message**: MSG05 - "Tài khoản đã bị khóa do đăng nhập sai quá nhiều lần"

**BR-06: Rate Limiting Policy**
- **Mô tả**: Tối đa 5 lần đăng nhập trong 1 phút
- **Implementation**: Bucket4j Rate Limiting
- **Áp dụng**: UC-02
- **Message**: MSG06 - "Vượt quá giới hạn đăng nhập, vui lòng thử lại sau"

**BR-07: Email Verification**
- **Mô tả**: Tài khoản phải xác thực email mới có thể đăng nhập
- **Implementation**: Email verification token
- **Áp dụng**: UC-01, UC-02
- **Message**: MSG07 - "Vui lòng xác thực email trước khi đăng nhập"

#### 6.3.2 Booking Management Rules

**BR-08: Booking Time Validation**
- **Mô tả**: Thời gian đặt bàn phải >= hiện tại + 30 phút
- **Validation**: Custom validator `@FuturePlus(minutes = 30)`
- **Áp dụng**: UC-07
- **Message**: MSG08 - "Thời gian đặt bàn phải sau 30 phút kể từ bây giờ"

**BR-09: Guest Count Limit**
- **Mô tả**: Số khách từ 1-20 người
- **Validation**: `@Min(value = 1) @Max(value = 20)`
- **Áp dụng**: UC-07
- **Message**: MSG09 - "Số khách phải từ 1-20 người"

**BR-10: Deposit Calculation**
- **Mô tả**: 
  - Nếu tổng tiền > 500k VNĐ → deposit = 10% tổng tiền
  - Nếu tổng tiền ≤ 500k VNĐ → deposit = 10k VNĐ (minimum)
- **Implementation**: `PaymentService.calculateTotalAmount()`
- **Áp dụng**: UC-07
- **Code**: 
```java
if (fullTotal.compareTo(new BigDecimal("500000")) > 0) {
    paymentAmount = fullTotal.multiply(new BigDecimal("0.1")); // 10%
} else {
    paymentAmount = new BigDecimal("10000"); // 10k minimum
}
```

**BR-11: Table Conflict Prevention**
- **Mô tả**: Không cho phép trùng bàn trong khung 2 giờ
- **Implementation**: Database constraint + Service validation
- **Áp dụng**: UC-07
- **Message**: MSG10 - "Bàn đã được đặt trong khung thời gian này"

**BR-12: Note Length Limit**
- **Mô tả**: Ghi chú tối đa 500 ký tự
- **Validation**: `@Size(max = 500)`
- **Áp dụng**: UC-07, UC-09
- **Message**: MSG11 - "Ghi chú không được quá 500 ký tự"

#### 6.3.3 Restaurant Approval Rules

**BR-13: Booking Approval Timeout**
- **Mô tả**: Restaurant Owner phải duyệt booking trong vòng 2 giờ
- **Implementation**: Scheduled task check
- **Áp dụng**: UC-19
- **Message**: MSG12 - "Booking đã quá hạn chờ duyệt"

**BR-14: Rejection Reason Required**
- **Mô tả**: Nếu từ chối booking, phải có lý do rõ ràng
- **Validation**: Required field validation
- **Áp dụng**: UC-19
- **Message**: MSG13 - "Vui lòng nhập lý do từ chối"

**BR-15: Auto Cancel Policy**
- **Mô tả**: Booking tự động hủy nếu không được duyệt trong 24 giờ
- **Implementation**: Scheduled task + Email notification
- **Áp dụng**: UC-19
- **Message**: MSG14 - "Booking đã tự động hủy do quá hạn"

**BR-16: Business License Required**
- **Mô tả**: Nhà hàng phải có giấy phép kinh doanh hợp lệ
- **Validation**: File upload validation
- **Áp dụng**: UC-15, UC-16
- **Message**: MSG15 - "Vui lòng upload giấy phép kinh doanh"

**BR-17: Admin Approval Deadline**
- **Mô tả**: Admin phải duyệt nhà hàng trong vòng 3 ngày làm việc
- **Implementation**: Notification system
- **Áp dụng**: UC-16
- **Message**: MSG16 - "Nhà hàng đã quá hạn chờ duyệt"

**BR-18: Resubmission Policy**
- **Mô tả**: Nếu từ chối, Restaurant Owner có thể nộp lại sau 30 ngày
- **Implementation**: Date validation
- **Áp dụng**: UC-16
- **Message**: MSG17 - "Có thể nộp lại sau 30 ngày kể từ ngày từ chối"

#### 6.3.4 Review & Voucher Rules

**BR-19: Rating Scale**
- **Mô tả**: Điểm đánh giá từ 1-5 sao
- **Validation**: `@Min(value = 1) @Max(value = 5)`
- **Áp dụng**: UC-11
- **Message**: MSG18 - "Điểm đánh giá phải từ 1-5 sao"

**BR-20: Voucher Usage Limit**
- **Mô tả**: Mỗi voucher có giới hạn sử dụng
- **Implementation**: Usage count tracking
- **Áp dụng**: UC-12
- **Message**: MSG19 - "Voucher đã hết lượt sử dụng"

#### 6.3.5 Financial Rules

**BR-21: Commission Rate**
- **Mô tả**: Admin giữ 3% commission từ mỗi giao dịch
- **Implementation**: `RestaurantBalanceService.calculateCommission()`
- **Áp dụng**: UC-22, UC-23
- **Code**:
```java
BigDecimal commission = amount.multiply(new BigDecimal("0.03")); // 3%
BigDecimal netAmount = amount.subtract(commission);
```

**BR-22: Minimum Withdrawal**
- **Mô tả**: Số tiền rút tối thiểu 100k VNĐ
- **Validation**: `@DecimalMin(value = "100000")`
- **Áp dụng**: UC-22
- **Message**: MSG20 - "Số tiền rút tối thiểu là 100,000 VNĐ"

**BR-23: Withdrawal Processing Time**
- **Mô tả**: Admin xử lý rút tiền trong 24 giờ làm việc
- **Implementation**: SLA tracking
- **Áp dụng**: UC-23
- **Message**: MSG21 - "Yêu cầu rút tiền sẽ được xử lý trong 24 giờ làm việc"

### 6.4 Business Rules theo Use Case

#### 6.4.1 UC-01: Đăng ký tài khoản
- BR-01: Username Validation
- BR-02: Password Complexity
- BR-03: Email Domain Validation
- BR-04: Phone Number Format

#### 6.4.2 UC-02: Đăng nhập hệ thống
- BR-02: Password Complexity
- BR-05: Account Lock Policy
- BR-06: Rate Limiting Policy
- BR-07: Email Verification

#### 6.4.3 UC-07: Đặt bàn
- BR-08: Booking Time Validation
- BR-09: Guest Count Limit
- BR-10: Deposit Calculation
- BR-11: Table Conflict Prevention
- BR-12: Note Length Limit

#### 6.4.4 UC-19: Duyệt booking
- BR-13: Booking Approval Timeout
- BR-14: Rejection Reason Required
- BR-15: Auto Cancel Policy

#### 6.4.5 UC-16: Duyệt nhà hàng
- BR-16: Business License Required
- BR-17: Admin Approval Deadline
- BR-18: Resubmission Policy

#### 6.4.6 UC-22: Yêu cầu rút tiền
- BR-21: Commission Rate
- BR-22: Minimum Withdrawal

#### 6.4.7 UC-23: Duyệt rút tiền
- BR-21: Commission Rate
- BR-23: Withdrawal Processing Time

### 6.5 Implementation Notes

#### 6.5.1 Validation Implementation
- **Bean Validation**: Sử dụng JSR-303 annotations
- **Custom Validators**: Tạo custom validators cho các rule phức tạp
- **Service Layer**: Business logic validation trong service layer

#### 6.5.2 Error Messages
- **Internationalization**: Hỗ trợ đa ngôn ngữ (Vietnamese/English)
- **Message Codes**: Sử dụng message codes (MSG01, MSG02, etc.)
- **User-Friendly**: Thông báo lỗi thân thiện với người dùng

#### 6.5.3 Monitoring & Logging
- **Audit Trail**: Ghi log tất cả vi phạm business rules
- **Metrics**: Theo dõi tỷ lệ vi phạm các rules
- **Alerting**: Cảnh báo khi có vi phạm nghiêm trọng

---

*Phần Business Rules này định nghĩa chi tiết các quy tắc nghiệp vụ chi phối hoạt động của hệ thống, đảm bảo tính nhất quán và tuân thủ chính sách kinh doanh.*
