# REQUIREMENT & DESIGN SPECIFICATION (RDS)
## HỆ THỐNG ĐẶT BÀN NHÀ HÀNG - RESTAURANT BOOKING PLATFORM

---

## 1. OVERVIEW

### 1.1 Phạm vi dự án (Project Scope)

**Hệ thống đặt bàn nhà hàng** là một nền tảng trực tuyến cho phép khách hàng (customer) tìm kiếm, xem thông tin và đặt bàn tại các nhà hàng đối tác. Hệ thống cung cấp các tính năng quản lý toàn diện cho cả khách hàng, chủ nhà hàng (restaurant owner) và quản trị viên (admin).

### 1.2 Mục tiêu dự án (Project Objectives)

#### Mục tiêu chính:
- **Tự động hóa quy trình đặt bàn**: Loại bỏ việc gọi điện đặt bàn thủ công, cung cấp hệ thống đặt bàn 24/7
- **Tối ưu hóa quản lý nhà hàng**: Giúp chủ nhà hàng quản lý bàn, booking, và doanh thu hiệu quả
- **Tăng trải nghiệm khách hàng**: Cung cấp giao diện thân thiện, tính năng tìm kiếm thông minh và đặt bàn dễ dàng
- **Quản lý tập trung**: Admin có thể quản lý toàn bộ hệ thống, duyệt nhà hàng mới, và giám sát hoạt động

#### Mục tiêu kinh doanh:
- Tăng doanh thu cho nhà hàng đối tác thông qua việc tăng lượt đặt bàn
- Tạo nguồn thu từ hoa hồng (commission) từ mỗi giao dịch đặt bàn thành công
- Xây dựng cộng đồng người dùng trung thành thông qua hệ thống đánh giá và voucher

### 1.3 Đối tượng sử dụng (Target Users)

#### 1.3.1 Khách hàng (Customer)
- **Đặc điểm**: Người dùng cuối muốn đặt bàn ăn uống tại nhà hàng
- **Nhu cầu**: Tìm kiếm nhà hàng, xem thông tin chi tiết, đặt bàn nhanh chóng, thanh toán đặt cọc
- **Quyền hạn**: Xem nhà hàng, đặt bàn, hủy bàn, đánh giá, sử dụng voucher, chat với nhà hàng

#### 1.3.2 Chủ nhà hàng (Restaurant Owner)
- **Đặc điểm**: Chủ sở hữu hoặc người quản lý nhà hàng muốn gia nhập nền tảng
- **Nhu cầu**: Quản lý thông tin nhà hàng, duyệt booking, quản lý bàn, theo dõi doanh thu, rút tiền
- **Quyền hạn**: Quản lý nhà hàng, duyệt/từ chối booking, cập nhật trạng thái bàn, xem báo cáo

#### 1.3.3 Quản trị viên (Admin)
- **Đặc điểm**: Người quản lý toàn bộ hệ thống, đảm bảo chất lượng và an toàn
- **Nhu cầu**: Duyệt nhà hàng mới, quản lý người dùng, xử lý khiếu nại, giám sát hệ thống
- **Quyền hạn**: Duyệt/từ chối nhà hàng, khóa/mở khóa tài khoản, duyệt rút tiền, quản lý voucher

### 1.4 Phạm vi chức năng (Functional Scope)

#### 1.4.1 Chức năng dành cho Khách hàng:
- Đăng ký/đăng nhập tài khoản (bao gồm OAuth Google)
- Tìm kiếm và lọc nhà hàng theo nhiều tiêu chí
- Xem thông tin chi tiết nhà hàng (menu, đánh giá, hình ảnh)
- Đặt bàn với lựa chọn bàn và thời gian
- Thanh toán đặt cọc qua PayOS
- Quản lý booking (xem, chỉnh sửa, hủy)
- Đánh giá và bình luận về nhà hàng
- Sử dụng voucher giảm giá
- Chat trực tiếp với nhà hàng
- Quản lý danh sách yêu thích

#### 1.4.2 Chức năng dành cho Chủ nhà hàng:
- Đăng ký tài khoản và tạo hồ sơ nhà hàng
- Upload giấy phép kinh doanh và ký hợp đồng
- Quản lý thông tin nhà hàng (menu, bàn, dịch vụ)
- Duyệt và xác nhận booking từ khách hàng
- Quản lý trạng thái bàn (available, occupied, maintenance)
- Xem báo cáo doanh thu và thống kê
- Quản lý danh sách chờ (waitlist) khi hết bàn
- Chat với khách hàng và admin
- Tạo và quản lý voucher
- Yêu cầu rút tiền doanh thu

#### 1.4.3 Chức năng dành cho Admin:
- Duyệt và phê duyệt nhà hàng mới
- Quản lý danh sách người dùng (khóa/mở khóa tài khoản)
- Duyệt yêu cầu rút tiền của nhà hàng
- Quản lý voucher toàn hệ thống
- Xử lý báo cáo và khiếu nại từ người dùng
- Giám sát hệ thống và bảo mật (rate limiting, audit log)
- Chat hỗ trợ với nhà hàng
- Quản lý danh sách ngân hàng

### 1.5 Phạm vi kỹ thuật (Technical Scope)

#### 1.5.1 Công nghệ sử dụng:
- **Backend**: Spring Boot 3.2.0, Spring Security, Spring Data JPA
- **Frontend**: Thymeleaf + Bootstrap 5, JavaScript
- **Database**: PostgreSQL
- **Payment Gateway**: PayOS
- **File Storage**: Cloudinary
- **Real-time Communication**: WebSocket
- **Authentication**: JWT + OAuth2 (Google)
- **Build Tool**: Maven
- **Java Version**: 17+

#### 1.5.2 Kiến trúc hệ thống:
- **Mô hình**: MVC (Model-View-Controller)
- **Pattern**: Layered Architecture (Controller → Service → Repository → Entity)
- **Security**: Role-based Access Control (RBAC)
- **API Design**: RESTful APIs
- **Data Validation**: Bean Validation (JSR-303)

### 1.6 Giả định và ràng buộc (Assumptions & Constraints)

#### 1.6.1 Giả định (Assumptions):
- Người dùng có kết nối internet ổn định
- Khách hàng có tài khoản ngân hàng hoặc ví điện tử để thanh toán
- Nhà hàng đối tác có giấy phép kinh doanh hợp lệ
- Hệ thống hoạt động 24/7 với độ tin cậy cao
- Dữ liệu được backup định kỳ và có kế hoạch disaster recovery

#### 1.6.2 Ràng buộc (Constraints):
- **Bảo mật**: Tuân thủ các tiêu chuẩn bảo mật PCI DSS cho thanh toán
- **Hiệu suất**: Thời gian phản hồi API < 2 giây, hỗ trợ đồng thời 1000+ người dùng
- **Tương thích**: Hỗ trợ các trình duyệt web hiện đại (Chrome, Firefox, Safari, Edge)
- **Dung lượng**: Giới hạn upload file 5MB, tổng dung lượng database < 100GB
- **Thời gian**: Dự án phải hoàn thành trong 6 tháng

### 1.7 Loại trừ (Exclusions)

#### 1.7.1 Chức năng không bao gồm:
- Ứng dụng di động (mobile app) - chỉ phát triển web application
- Tích hợp với hệ thống POS của nhà hàng
- Dịch vụ giao hàng tận nơi (delivery service)
- Quản lý nhân viên và lương của nhà hàng
- Hệ thống kế toán phức tạp
- Tích hợp với các nền tảng mạng xã hội khác ngoài Google

#### 1.7.2 Đối tượng không hỗ trợ:
- Khách hàng không có tài khoản ngân hàng
- Nhà hàng không có giấy phép kinh doanh
- Người dùng sử dụng trình duyệt cũ (IE, Opera cũ)
- Các giao dịch thanh toán quốc tế (chỉ hỗ trợ VNĐ)

---

*Phần Overview này cung cấp cái nhìn tổng quan về dự án Restaurant Booking Platform, định nghĩa rõ phạm vi, mục tiêu và đối tượng sử dụng của hệ thống.*
