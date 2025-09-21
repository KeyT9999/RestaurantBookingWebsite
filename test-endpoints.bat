@echo off
REM Test Endpoints Script for Restaurant Booking App
REM Usage: test-endpoints.bat [dev|prod]

set PROFILE=%1
if "%PROFILE%"=="" set PROFILE=dev
set BASE_URL=http://localhost:8080

echo 🧪 Testing endpoints for profile: %PROFILE%
echo Base URL: %BASE_URL%
echo ================================

REM Test Health Check
echo 1️⃣ Testing Health Check:
curl -s "%BASE_URL%/actuator/health" 2>nul || echo Health endpoint failed
echo.

REM Test Home Page
echo 2️⃣ Testing Home Page:
curl -s -I "%BASE_URL%/" 2>nul | findstr "HTTP"
echo.

REM Test Login Page
echo 3️⃣ Testing Login Page:
curl -s -I "%BASE_URL%/login" 2>nul | findstr "HTTP"
echo.

REM Test Register Page
echo 4️⃣ Testing Register Page:
curl -s -I "%BASE_URL%/auth/register" 2>nul | findstr "HTTP"
echo.

REM Test H2 Console (only for dev profile)
if "%PROFILE%"=="dev" (
    echo 5️⃣ Testing H2 Console (dev only):
    curl -s -I "%BASE_URL%/h2-console" 2>nul | findstr "HTTP"
    echo.
    echo 📝 H2 Console Access Info:
    echo    URL: %BASE_URL%/h2-console
    echo    JDBC URL: jdbc:h2:mem:devdb
    echo    Username: sa
    echo    Password: (leave empty)
    echo.
)

echo ✅ Test completed!
echo.
echo 🔗 Quick Links:
echo    App: %BASE_URL%
echo    Login: %BASE_URL%/login
echo    Register: %BASE_URL%/auth/register
echo    Health: %BASE_URL%/actuator/health
if "%PROFILE%"=="dev" (
    echo    H2 Console: %BASE_URL%/h2-console
)
pause 