# Hướng dẫn Test Tính năng Authentication - BookEat

## 🎯 Tổng quan
Dự án đã được bổ sung đầy đủ các tính năng authentication theo yêu cầu:

### ✅ Tính năng đã hoàn thành:
1. **Đăng ký với xác thực email** - `/auth/register`
2. **Mã hóa password BCrypt** - Tự động
3. **Đăng nhập form & Google OAuth2** - `/login`
4. **Đăng xuất** - `/logout`
5. **Đổi mật khẩu** - `/auth/change-password`
6. **Quên mật khẩu (Reset qua email)** - `/auth/forgot-password`
7. **Upload ảnh đại diện** - `/auth/profile/avatar`
8. **Xem profile** - `/auth/profile`
9. **Chỉnh sửa profile** - `/auth/profile/edit`

## 🚀 Hướng dẫn Test (Dev Profile)

### 1. Khởi động ứng dụng
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 2. Truy cập ứng dụng
- URL: http://localhost:8080
- Database H2 Console: http://localhost:8080/h2-console
  - URL: `jdbc:h2:mem:devdb`
  - Username: `sa`
  - Password: (để trống)

### 3. Test Flow Đăng ký + Xác thực Email

#### Bước 1: Đăng ký tài khoản mới
1. Truy cập: http://localhost:8080/auth/register
2. Điền thông tin:
   - Username: `testuser`
   - Email: `test@example.com`
   - Họ tên: `Test User`
   - Số điện thoại: `0123456789` (tùy chọn)
   - Mật khẩu: `password123`
   - Xác nhận mật khẩu: `password123`
3. Click "Đăng ký"

#### Bước 2: Kiểm tra email xác thực
- **Trong môi trường dev**: Email được mock, check console/log để thấy link xác thực
- Tìm dòng log: `📧 [MOCK EMAIL] Message:`
- Copy link xác thực từ log (dạng: `/auth/verify-email?token=...`)

#### Bước 3: Xác thực email
1. Truy cập link xác thực từ log
2. Tài khoản sẽ được kích hoạt
3. Redirect về trang login với thông báo thành công

#### Bước 4: Đăng nhập
1. Truy cập: http://localhost:8080/login
2. Username: `testuser`
3. Password: `password123`

### 4. Test Google OAuth2 Login

#### Bước 1: Click "Đăng nhập với Google"
1. Truy cập: http://localhost:8080/login
2. Click nút "Đăng nhập với Google"
3. Đăng nhập bằng Google account
4. User sẽ được tự động tạo/sync trong database

**Lưu ý**: Cần cấu hình Google OAuth2 credentials trong `application-dev.yml`

### 5. Test Quên mật khẩu

#### Bước 1: Yêu cầu reset password
1. Truy cập: http://localhost:8080/auth/forgot-password
2. Nhập email: `test@example.com`
3. Click "Gửi link đặt lại mật khẩu"

#### Bước 2: Kiểm tra email reset
- Check console log để thấy reset link
- Tìm dòng: `📧 [MOCK EMAIL] Message:`
- Copy link reset (dạng: `/auth/reset-password?token=...`)

#### Bước 3: Đặt lại mật khẩu
1. Truy cập reset link từ log
2. Nhập mật khẩu mới: `newpassword123`
3. Xác nhận mật khẩu: `newpassword123`
4. Click "Đặt lại mật khẩu"

#### Bước 4: Đăng nhập bằng mật khẩu mới
- Username: `testuser`
- Password: `newpassword123`

### 6. Test Đổi mật khẩu (khi đã đăng nhập)

1. Truy cập: http://localhost:8080/auth/change-password
2. Mật khẩu hiện tại: `newpassword123`
3. Mật khẩu mới: `anotherpassword`
4. Xác nhận: `anotherpassword`
5. Click "Đổi mật khẩu"

### 7. Test Upload Avatar

1. Truy cập: http://localhost:8080/auth/profile
2. Chọn file ảnh (JPG, PNG)
3. Click "Đổi ảnh đại diện"
4. Ảnh sẽ được lưu trong thư mục `uploads/`
5. Refresh trang để thấy ảnh mới

### 8. Test Chỉnh sửa Profile

1. Truy cập: http://localhost:8080/auth/profile/edit
2. Cập nhật thông tin:
   - Họ tên
   - Số điện thoại
   - Địa chỉ
3. Click "Lưu thay đổi"

## 🔍 Kiểm tra Database

### Xem dữ liệu User trong H2
```sql
-- Xem tất cả users
SELECT * FROM users;

-- Kiểm tra password được mã hóa BCrypt
SELECT username, email, password, email_verified FROM users;

-- Xem tokens
SELECT username, email_verification_token, password_reset_token FROM users;
```

### Kiểm tra logs quan trọng
Các log cần chú ý trong console:
- `✅ User registered successfully: testuser`
- `📧 Verification email sent to: test@example.com`
- `✅ Email verified for user: test@example.com`
- `✅ Password reset token sent to: test@example.com`
- `✅ Password reset successfully for user: test@example.com`
- `✅ Password changed successfully for user: testuser`
- `✅ Profile updated for user: testuser`
- `✅ Profile image updated for user: testuser`

## 🛡️ Security Features Verification

### 1. BCrypt Password Encoding
- Tất cả passwords trong DB được mã hóa BCrypt
- Không có plaintext passwords

### 2. Email Verification Required
- Users mới có `email_verified = false`
- Không thể login cho đến khi verify email
- Verification token được generate và gửi qua email

### 3. Token Expiry
- Email verification: không hết hạn (có thể cấu hình)
- Password reset token: hết hạn sau 1 giờ

### 4. OAuth2 Integration
- Google users được tự động verify email
- Sync thông tin từ Google profile
- Tự động assign role CUSTOMER

### 5. Route Protection
- `/auth/register`, `/auth/verify-email`, `/auth/forgot-password`, `/auth/reset-password`: Public
- Tất cả routes `/auth/**` khác: Require authentication
- `/booking/**`: Require authentication

## 🐛 Troubleshooting

### Lỗi thường gặp:

1. **Email không gửi được**
   - Dev mode sử dụng mock email
   - Check console logs để thấy email content

2. **Google OAuth2 lỗi**
   - Kiểm tra client-id và client-secret trong `application-dev.yml`
   - Đảm bảo redirect URL được config đúng trong Google Console

3. **Upload ảnh lỗi**
   - Kiểm tra thư mục `uploads/` có tồn tại
   - Kiểm tra quyền write

4. **Token hết hạn**
   - Password reset token chỉ valid 1 giờ
   - Phải request lại nếu hết hạn

## 📊 Performance & Logging

### Debug Levels (application-dev.yml)
```yaml
logging:
  level:
    org.springframework.security: DEBUG
    com.example.booking: DEBUG
```

### Monitoring
- Health check: http://localhost:8080/actuator/health
- H2 Console: http://localhost:8080/h2-console

## 🎉 Kết luận

Tất cả 9 tính năng authentication đã được implement đầy đủ và test được ngay. Dự án ready để demo và phát triển tiếp!

### Next Steps:
1. Cấu hình SMTP server thật cho production
2. Thêm rate limiting cho auth endpoints
3. Implement 2FA (optional)
4. Add audit logging cho security events 