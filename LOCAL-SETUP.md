# 🔧 Local Development Setup - BookEat

## 🚨 **Quan trọng: Setup Environment Variables**

Dự án sử dụng **Environment Variables** cho secrets, không hardcode trong code.

### **Bước 1: Copy template file**
```bash
# Copy template file để tạo config local
cp src/main/resources/application-dev.example.yml src/main/resources/application-dev.yml
```

### **Bước 2: Set Environment Variables**

#### **PowerShell (Windows):**
```powershell
# Email (Gmail)
$env:MAIL_USERNAME = "your-email@gmail.com"
$env:MAIL_PASSWORD = "your-app-password"

# Google OAuth2
$env:GOOGLE_CLIENT_ID = "your-google-client-id"
$env:GOOGLE_CLIENT_SECRET = "your-google-client-secret"

# Chạy app
mvn "-Dspring-boot.run.profiles=dev" spring-boot:run
```

#### **Bash (Linux/Mac):**
```bash
# Email (Gmail)
export MAIL_USERNAME="your-email@gmail.com"
export MAIL_PASSWORD="your-app-password"

# Google OAuth2
export GOOGLE_CLIENT_ID="your-google-client-id"
export GOOGLE_CLIENT_SECRET="your-google-client-secret"

# Chạy app
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### **Bước 3: Lấy Google OAuth2 Credentials**

1. **Google Cloud Console**: https://console.developers.google.com
2. **Create Project** hoặc chọn project có sẵn
3. **APIs & Services** → **Credentials**
4. **Create OAuth 2.0 Client ID**:
   - **Application type**: Web application
   - **Authorized redirect URIs**: `http://localhost:8080/login/oauth2/code/google`
5. **Copy Client ID và Client Secret**

### **Bước 4: Lấy Gmail App Password**

1. **Google Account**: https://myaccount.google.com/security
2. **2-Step Verification** → Enable (nếu chưa có)
3. **App passwords** → **Generate**
4. **Select app**: Mail
5. **Copy 16-digit password**

## 📁 **File Structure**

```
src/main/resources/
├── application.yml              ✅ (commit)
├── application-dev.example.yml  ✅ (commit - template)
├── application-dev.yml          ❌ (gitignore - local only)
└── application-prod.yml         ✅ (commit - no secrets)
```

## 🔒 **Security Best Practices**

### ✅ **Safe to commit:**
- Template files với placeholders
- Production config với ENV variables
- Documentation và guides

### ❌ **Never commit:**
- Files chứa real secrets
- API keys, passwords, tokens
- Local environment configs

## 🚀 **Quick Start**

```bash
# 1. Clone repo
git clone https://github.com/YOUR_USERNAME/BookEat.git
cd BookEat

# 2. Setup config
cp src/main/resources/application-dev.example.yml src/main/resources/application-dev.yml

# 3. Set environment variables (see above)

# 4. Run
mvn "-Dspring-boot.run.profiles=dev" spring-boot:run
```

## 🌐 **Production Deployment**

Trên **Render/Heroku/AWS**, chỉ cần set Environment Variables:
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`  
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `DATABASE_URL`

**App sẽ tự động đọc từ ENV, không cần file config!**

## ❓ **FAQ**

**Q: Tại sao không thấy application-dev.yml sau khi clone?**  
A: File này được gitignore. Copy từ `application-dev.example.yml` và set ENV variables.

**Q: Deploy có bị lỗi không khi ignore file config?**  
A: Không! Production dùng `application-prod.yml` + ENV variables, không phụ thuộc vào file dev.

**Q: Team member khác setup thế nào?**  
A: Follow hướng dẫn này, copy template và set ENV của riêng họ. 