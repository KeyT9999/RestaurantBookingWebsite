# 🚀 Quick Deploy to Render - BookEat

## ⚡ TL;DR - Quick Steps

### 1. **Push to GitHub**
```bash
# Tạo repo trên GitHub: BookEat
git remote add origin https://github.com/YOUR_USERNAME/BookEat.git
git branch -M main
git push -u origin main
```

### 2. **Render Setup** (5 phút)
1. **https://render.com** → Sign up với GitHub
2. **New PostgreSQL** → Name: `bookeat-db` → Create
3. **New Web Service** → Connect GitHub repo → Name: `bookeat-app`

### 3. **Web Service Config**
- **Build Command**: `mvn clean package -DskipTests`
- **Start Command**: `java -Dserver.port=$PORT -Dspring.profiles.active=prod -jar target/restaurant-booking-0.0.1-SNAPSHOT.jar`

### 4. **Environment Variables** (Copy-paste)
```bash
# Database (từ PostgreSQL service)
DATABASE_URL=postgresql://bookeat_user:password@host:port/bookeat_db

# App
APP_BASE_URL=https://bookeat-app.onrender.com
SPRING_PROFILES_ACTIVE=prod
PORT=8080

# Email (Replace with your Gmail credentials)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-gmail-app-password

# OAuth2 (Replace with your Google OAuth2 credentials)
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
```

### 5. **Google OAuth2 Update**
- **https://console.developers.google.com**
- **Credentials** → Edit OAuth2 client
- **Add redirect URI**: `https://bookeat-app.onrender.com/login/oauth2/code/google`

## ✅ **Ready URLs**
- **App**: https://bookeat-app.onrender.com
- **Login**: https://bookeat-app.onrender.com/login
- **Register**: https://bookeat-app.onrender.com/auth/register

## 🎯 **Features Ready for Production**
✅ **Complete Auth System** - Register, Login, OAuth2, Profile, Password Reset  
✅ **Email Verification** - Gmail SMTP integration  
✅ **Booking System** - Full CRUD with restaurants and tables  
✅ **Luxury UI** - Responsive design, modern UX  
✅ **Security** - BCrypt, CSRF, OAuth2, Route Protection  
✅ **Database** - PostgreSQL with Hibernate auto-migration  

**🚀 Deploy ngay và enjoy!** 