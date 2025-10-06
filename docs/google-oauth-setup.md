# 🌐 Hướng dẫn Setup Google OAuth2 Login

## 📋 **Bước 1: Tạo Google Cloud Project**

1. **Truy cập:** https://console.developers.google.com/
2. **Tạo project mới** hoặc chọn project có sẵn
3. **Enable Google+ API** (hoặc Google People API)

## 🔧 **Bước 2: Tạo OAuth Client ID**

### **2.1. Tạo Credentials:**
1. **Click:** `+ Create Credentials` (trên cùng)
2. **Chọn:** `OAuth client ID`

### **2.2. Cấu hình Application:**
- **Application type:** `Web application`
- **Name:** `SpringBoot-RestaurantBooking` (hoặc tên bạn muốn)

### **2.3. Authorized redirect URIs:**
**Bấm `Add URI` và nhập:**
```
http://localhost:8081/login/oauth2/code/google
```

**✅ URI này là CHÍNH XÁC - đây là endpoint mặc định của Spring Security OAuth2**

### **2.4. Tạo Client:**
- **Click:** `Create`
- **Copy:** `Client ID` và `Client Secret`

## ⚙️ **Bước 3: Cấu hình Spring Boot**

### **3.1. Sử dụng Environment Variables (Khuyến nghị):**

**Tạo file `.env` hoặc set trong IDE:**
```bash
GOOGLE_CLIENT_ID=your_actual_client_id_here
GOOGLE_CLIENT_SECRET=your_actual_client_secret_here
```

**Hoặc set trong Windows:**
```cmd
set GOOGLE_CLIENT_ID=your_actual_client_id_here
set GOOGLE_CLIENT_SECRET=your_actual_client_secret_here
```

### **3.2. Cấu hình application.yml (đã có sẵn):**
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID:your-google-client-id}
            client-secret: ${GOOGLE_CLIENT_SECRET:your-google-client-secret}
            scope: [openid, email, profile]
```

## 🧪 **Bước 4: Test Google Login**

### **4.1. Chạy ứng dụng:**
```bash
mvn spring-boot:run
```

### **4.2. Test flow:**
1. **Truy cập:** http://localhost:8081/login
2. **Click:** "Đăng nhập với Google"
3. **Chọn Google account**
4. **Authorize app**
5. **Redirect về:** http://localhost:8081/booking/my

## 🔍 **Troubleshooting:**

### **Lỗi "redirect_uri_mismatch":**
- Kiểm tra URI trong Google Console: `http://localhost:8081/login/oauth2/code/google`
- Đảm bảo không có trailing slash

### **Lỗi "invalid_client":**
- Kiểm tra Client ID và Client Secret
- Đảm bảo environment variables được set đúng

### **Lỗi "access_denied":**
- User từ chối authorize
- Kiểm tra scopes: `[openid, email, profile]`

## 📊 **Luồng hoạt động:**

1. **User click "Login with Google"**
2. **Redirect đến Google OAuth**
3. **User authorize app**
4. **Google redirect về:** `/login/oauth2/code/google`
5. **Spring Security xử lý OAuth2 response**
6. **OAuth2LoginSuccessHandler tạo/update user**
7. **Redirect đến:** `/booking/my`

## 🎯 **Kết quả:**

- ✅ **User mới:** Tự động tạo account với thông tin Google
- ✅ **User cũ:** Link Google ID với account hiện tại
- ✅ **Profile image:** Tự động lấy từ Google
- ✅ **Email verified:** Tự động set true cho Google users

## 🔒 **Bảo mật:**

- ✅ **Environment variables** thay vì hardcode
- ✅ **Secure redirect URI** 
- ✅ **Minimal scopes** (chỉ email, profile)
- ✅ **Auto-generated password** cho Google users

---

**🎉 Google OAuth2 Login đã sẵn sàng!** 