# Hướng dẫn setup nhanh (không commit secrets)

Tài liệu này giúp bạn cấu hình các file/biến MÀ KHÔNG ĐƯỢC push lên GitHub để chạy dự án BookEat ở môi trường local (dev), Docker và Render.

## 1) Các file/biến KHÔNG commit
- `.env` (ở thư mục gốc): chứa các secrets như Gmail App Password, Google OAuth2 Client.
- Biến môi trường hệ điều hành (Windows/macOS/Linux) hoặc Render Dashboard.
- Tuyệt đối KHÔNG đẩy các giá trị thật lên repo.

## 2) Tạo file `.env` tại thư mục gốc
Tạo file `./.env` (mỗi dòng `KEY=VALUE`, không để khoảng trắng hai bên dấu `=`):

```
MAIL_USERNAME=your_gmail@gmail.com
MAIL_PASSWORD=your_gmail_app_password_16_chars
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
# Tuỳ chọn
APP_BASE_URL=http://localhost:8081
```

Lưu ý Gmail:
- Bật 2FA cho tài khoản Gmail, tạo **App Password** 16 ký tự (không có khoảng trắng) để dùng cho `MAIL_PASSWORD`.

## 3) Chạy local (dev)
Dự án đã có `spring-dotenv`, nên `mvn spring-boot:run` sẽ tự nạp `.env`.

- Cách A (khuyến nghị):
```
mvn spring-boot:run
```

- Cách B (xuất biến thủ công nếu cần):
  - Windows PowerShell:
```
Get-Content .env | ForEach-Object { if ($_ -match '^\s*([^=]+)=(.*)$') { [System.Environment]::SetEnvironmentVariable($matches[1], $matches[2], 'Process') } } ; mvn spring-boot:run
```
  - Linux/macOS:
```
export $(grep -v '^#' .env | xargs) && mvn spring-boot:run
```

Mặc định dev dùng H2 in-memory. Nếu muốn Postgres local, tự tạo DB và đặt biến:
```
JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/restaurant_db
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

## 4) Google OAuth2 (bắt buộc nếu dùng Login Google)
- Vào Google Cloud Console → Credentials → OAuth 2.0 Client IDs.
- Thêm Authorized redirect URI:
  - Dev: `http://localhost:8081/login/oauth2/code/google`
  - Prod: `https://<your-domain>/login/oauth2/code/google`
- Lấy `Client ID` và `Client Secret` điền vào `.env` như trên.

## 5) SMTP gửi email (Forgot/Verify)
- Dev/Prod có thể dùng Gmail SMTP:
  - Host: `smtp.gmail.com`
  - Port: `587`
  - STARTTLS: bật (đã cấu hình sẵn trong app)
  - Dùng `MAIL_USERNAME` và `MAIL_PASSWORD` (App Password 16 ký tự)
- Nếu chưa cấu hình SMTP, hệ thống sẽ chạy **MOCK email** (log link xác thực/đặt lại mật khẩu trong console).

## 6) Docker (tuỳ chọn)
Dự án đã có `docker-compose.yml`. Chạy:
```
docker-compose up --build
```
- Compose sẽ nạp biến từ `.env` và map port `8081:8081`.

## 7) Render (Prod) – chỉ đặt biến, không dùng `.env`
Trong Render Dashboard → Service → Environment:
- App:
  - `SPRING_PROFILES_ACTIVE=prod`
  - `APP_BASE_URL=https://<your-app>.onrender.com`
- Database:
  - `JDBC_DATABASE_URL=jdbc:postgresql://...:5432/<db>?sslmode=require`
  - `DB_USERNAME=...`
  - `DB_PASSWORD=...`
- Mail:
  - `MAIL_USERNAME=your_gmail@gmail.com`
  - `MAIL_PASSWORD=your_gmail_app_password_16_chars`
- Google OAuth2:
  - `GOOGLE_CLIENT_ID=...`
  - `GOOGLE_CLIENT_SECRET=...`

## 8) Upload avatar (dev)
- Vào `/auth/profile`, chọn ảnh và Tải lên.
- Giới hạn mặc định 5MB. Nếu cần tăng, thêm vào `application-dev.yml`:
```
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
```

## 9) Kiểm tra nhanh
- Mở `http://localhost:8081`
- Google Login → quay về `/` (trang chủ), bấm Profile → `/auth/profile`.
- Register/Forgot Password → nếu đã cấu hình SMTP đúng, sẽ nhận email thật; nếu không, xem link trong log (MOCK).

## 10) Troubleshoot ngắn
- `.env` không nạp: kiểm tra đường dẫn, không có BOM/ký tự lạ, mỗi dòng `KEY=VALUE`.
- Google không login: kiểm tra Redirect URI đã add đúng trong Google Console.
- Email không gửi: kiểm tra App Password, host `smtp.gmail.com`, port `587`, STARTTLS.
- Avatar không đổi: kiểm tra giới hạn upload, quyền ghi vào thư mục `uploads/`, hard refresh trình duyệt. 