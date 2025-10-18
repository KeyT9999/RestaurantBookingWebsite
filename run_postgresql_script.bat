@echo off
echo 🚀 Chạy SQL Script cho PostgreSQL Rate Limiting
echo ================================================

REM Cấu hình database
set DB_HOST=localhost
set DB_PORT=5432
set DB_NAME=restaurant_booking
set DB_USER=postgres
set DB_PASSWORD=your_password

REM Đường dẫn file SQL
set SQL_FILE=database\rate_limiting_tables_postgresql.sql

echo 📁 File SQL: %SQL_FILE%
echo 🗄️ Database: %DB_NAME%
echo 👤 User: %DB_USER%
echo.

REM Kiểm tra file SQL có tồn tại không
if not exist "%SQL_FILE%" (
    echo ❌ File SQL không tồn tại: %SQL_FILE%
    echo 💡 Vui lòng đảm bảo file SQL đã được tạo
    pause
    exit /b 1
)

echo 🔍 Đang tìm psql.exe...

REM Tìm psql.exe trong các đường dẫn phổ biến
set PSQL_PATH=
if exist "C:\Program Files\PostgreSQL\15\bin\psql.exe" set PSQL_PATH=C:\Program Files\PostgreSQL\15\bin\psql.exe
if exist "C:\Program Files\PostgreSQL\14\bin\psql.exe" set PSQL_PATH=C:\Program Files\PostgreSQL\14\bin\psql.exe
if exist "C:\Program Files\PostgreSQL\13\bin\psql.exe" set PSQL_PATH=C:\Program Files\PostgreSQL\13\bin\psql.exe
if exist "C:\Program Files\PostgreSQL\12\bin\psql.exe" set PSQL_PATH=C:\Program Files\PostgreSQL\12\bin\psql.exe
if exist "C:\Program Files\PostgreSQL\11\bin\psql.exe" set PSQL_PATH=C:\Program Files\PostgreSQL\11\bin\psql.exe

if "%PSQL_PATH%"=="" (
    echo ❌ Không tìm thấy psql.exe
    echo 💡 Vui lòng cài đặt PostgreSQL hoặc thêm vào PATH
    echo.
    echo 🔧 Hướng dẫn thủ công:
    echo 1. Mở pgAdmin
    echo 2. Kết nối đến database: %DB_NAME%
    echo 3. Mở Query Tool
    echo 4. Copy nội dung file: %SQL_FILE%
    echo 5. Paste và chạy script
    pause
    exit /b 1
)

echo ✅ Tìm thấy psql tại: %PSQL_PATH%
echo.

REM Set password environment variable
set PGPASSWORD=%DB_PASSWORD%

echo 📝 Đang chạy SQL script...
echo.

REM Chạy psql
"%PSQL_PATH%" -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -f "%SQL_FILE%"

if %ERRORLEVEL% equ 0 (
    echo.
    echo ✅ Script chạy thành công!
    echo 🎉 Các bảng rate limiting đã được tạo
    echo.
    echo 📋 Bước tiếp theo:
    echo 1. Kiểm tra database có các bảng mới không
    echo 2. Restart ứng dụng Spring Boot
    echo 3. Truy cập dashboard: http://localhost:8080/admin/rate-limiting
) else (
    echo.
    echo ❌ Có lỗi khi chạy script
    echo 💡 Kiểm tra lại:
    echo - Database có tồn tại không
    echo - User có quyền tạo bảng không
    echo - Password có đúng không
)

REM Xóa password khỏi environment
set PGPASSWORD=

echo.
pause
