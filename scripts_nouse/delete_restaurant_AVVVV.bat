@echo off
REM =====================================================
REM Batch Script: XÓA NHÀ HÀNG "AVVVV"
REM =====================================================

echo.
echo =====================================================
echo   XÓA NHÀ HÀNG "AVVVV" VÀ TẤT CẢ DỮ LIỆU LIÊN QUAN
echo =====================================================
echo.
echo ⚠️  CẢNH BÁO: Thao tác này KHÔNG THỂ HOÀN TÁC!
echo.
echo Bạn có muốn tiếp tục? (Y/N)
set /p confirm="> "

if /i not "%confirm%"=="Y" (
    echo.
    echo Đã hủy thao tác.
    pause
    exit /b
)

echo.
echo Đang kết nối đến database...

REM Đọc thông tin database từ env.example hoặc yêu cầu người dùng nhập
REM Bạn có thể sửa các giá trị này theo cấu hình của bạn
set DB_HOST=localhost
set DB_PORT=5432
set DB_NAME=bookeat_db
set DB_USER=postgres

echo.
echo Nhập mật khẩu database (hoặc để trống nếu không cần):
set /p DB_PASSWORD="> "

if "%DB_PASSWORD%"=="" (
    psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -f "%~dp0delete_restaurant_AVVVV.sql"
) else (
    set PGPASSWORD=%DB_PASSWORD%
    psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -f "%~dp0delete_restaurant_AVVVV.sql"
)

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Hoàn tất!
) else (
    echo.
    echo ❌ Có lỗi xảy ra. Kiểm tra lại thông tin kết nối database.
    echo.
    echo Bạn cũng có thể chạy SQL script trực tiếp trong pgAdmin:
    echo   1. Mở pgAdmin
    echo   2. Kết nối database
    echo   3. Mở Query Tool
    echo   4. Copy nội dung file delete_restaurant_AVVVV.sql
    echo   5. Paste và chạy (F5)
)

echo.
pause




