# 🚀 Hướng dẫn Deploy BookEat lên Render

## 📋 Tổng quan
Hướng dẫn deploy ứng dụng Spring Boot BookEat lên Render với PostgreSQL database và Gmail SMTP.

## 🛠️ Chuẩn bị trước khi deploy

### ✅ Files đã được tạo sẵn:
- `application-prod.yml` - Cấu hình production
- `system.properties` - Java version 17
- `Procfile` - Command để chạy app
- `.gitignore` - Ignore sensitive files

### ✅ JAR file đã build:
```bash
mvn clean package -DskipTests
# → target/restaurant-booking-0.0.1-SNAPSHOT.jar
```

## 🌐 Deploy trên Render

### **Bước 1: Tạo tài khoản Render**
1. Truy cập: https://render.com
2. Sign up/Login với GitHub account
3. Connect GitHub repository

### **Bước 2: Tạo PostgreSQL Database**
1. **Dashboard** → **New** → **PostgreSQL**
2. **Name**: `bookeat-db`
3. **User**: `bookeat_user` 
4. **Database**: `bookeat_db`
5. **Region**: Singapore (gần VN nhất)
6. **Plan**: Free tier
7. **Create Database**

📝 **Lưu thông tin database:**
- **Internal Database URL**: `postgresql://...` (dùng cho app)
- **External Database URL**: `postgresql://...` (dùng cho tools)

### **Bước 3: Tạo Web Service**
1. **Dashboard** → **New** → **Web Service**
2. **Connect Repository**: Chọn GitHub repo BookEat
3. **Name**: `bookeat-app`
4. **Region**: Singapore
5. **Branch**: `main`
6. **Runtime**: `Java`
7. **Build Command**: `mvn clean package -DskipTests`
8. **Start Command**: `java -Dserver.port=$PORT -Dspring.profiles.active=prod -jar target/restaurant-booking-0.0.1-SNAPSHOT.jar`

### **Bước 4: Cấu hình Environment Variables**

Trong **Environment** tab của Web Service, thêm:

#### **Database Configuration:**
```
DATABASE_URL = [Internal Database URL từ bước 2]
DB_USERNAME = bookeat_user
DB_PASSWORD = [Password từ database]
```

#### **Application Configuration:**
```
APP_BASE_URL = https://bookeat-app.onrender.com
UPLOAD_DIR = /tmp/uploads
PORT = 8080
SPRING_PROFILES_ACTIVE = prod
```

#### **Email Configuration (Gmail):**
```
MAIL_HOST = smtp.gmail.com
MAIL_PORT = 587
MAIL_USERNAME = <your-email@gmail.com>
MAIL_PASSWORD = <your-gmail-app-password>
```

#### **Google OAuth2:**
```
GOOGLE_CLIENT_ID = <get from Google Cloud Console>
GOOGLE_CLIENT_SECRET = <get from Google Cloud Console>
```

### **Bước 5: Deploy**
1. **Save** environment variables
2. **Manual Deploy** hoặc đợi auto-deploy
3. **Check logs** để debug nếu có lỗi

## 🔧 Cấu hình Google OAuth2 cho Production

### **Cập nhật Google Console:**
1. Truy cập: https://console.developers.google.com
2. Chọn project OAuth2 hiện tại
3. **Credentials** → **OAuth 2.0 Client IDs**
4. **Edit** client ID hiện tại
5. **Authorized redirect URIs** → **Add URI**:
   ```
   https://bookeat-app.onrender.com/login/oauth2/code/google
   ```
6. **Save**

## 📊 Kiểm tra Deployment

### **URLs sau khi deploy:**
- **App URL**: https://bookeat-app.onrender.com
- **Login**: https://bookeat-app.onrender.com/login
- **Register**: https://bookeat-app.onrender.com/auth/register
- **Health Check**: https://bookeat-app.onrender.com/actuator/health

### **Test checklist:**
- [ ] **Home page** load được
- [ ] **Registration** với email verification
- [ ] **Login** form và Google OAuth2
- [ ] **Profile** management
- [ ] **Booking** functionality
- [ ] **Email** sending (Gmail SMTP)

## 🐛 Troubleshooting

### **Common Issues:**

1. **Build Failed:**
   ```
   # Check Java version
   Build Command: mvn clean package -DskipTests -Dmaven.compiler.source=17 -Dmaven.compiler.target=17
   ```

2. **Database Connection Error:**
   ```
   # Verify DATABASE_URL format
   postgresql://user:password@host:port/database
   ```

3. **OAuth2 Redirect Error:**
   ```
   # Update Google Console redirect URI
   https://your-app.onrender.com/login/oauth2/code/google
   ```

4. **Email Not Sending:**
   ```
     # Check Gmail App Password
  MAIL_PASSWORD = <your-gmail-app-password>
   ```

## 📝 Environment Variables Template

Copy vào Render Environment tab:

```bash
# Database
DATABASE_URL=postgresql://bookeat_user:password@host:port/bookeat_db
DB_USERNAME=bookeat_user
DB_PASSWORD=your_db_password

# Application
APP_BASE_URL=https://bookeat-app.onrender.com
UPLOAD_DIR=/tmp/uploads
PORT=8080
SPRING_PROFILES_ACTIVE=prod

# Email (Gmail)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-gmail-app-password

# OAuth2 (Google)
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
```

## 🎉 Deployment Flow

1. **Push code** lên GitHub
2. **Render auto-detects** changes
3. **Build** với Maven
4. **Deploy** với environment variables
5. **Database** auto-migrate với Hibernate
6. **App ready** tại URL được assign

## 📱 Mobile Responsive

App đã được thiết kế responsive, sẽ hoạt động tốt trên:
- ✅ **Desktop** browsers
- ✅ **Tablet** devices  
- ✅ **Mobile** phones

## 🔒 Security Notes

- **Passwords** được mã hóa BCrypt
- **HTTPS** được enable tự động trên Render
- **Environment variables** được encrypt
- **CSRF protection** enabled
- **OAuth2** secure flow

## 🚀 Ready to Deploy!

**Commit và push lên GitHub, sau đó follow các bước trên để deploy lên Render!** 