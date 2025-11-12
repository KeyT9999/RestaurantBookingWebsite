@echo off
REM Script để upload ảnh lên Cloudinary và tạo SQL script
REM Cách sử dụng: scripts\upload.bat

echo ========================================
echo UPLOAD ẢNH NHÀ HÀNG LÊN CLOUDINARY
echo ========================================
echo.

REM Kiểm tra Python
python --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Python not found!
    echo Please install Python 3 first.
    pause
    exit /b 1
)

REM Kiểm tra cloudinary package
python -c "import cloudinary" >nul 2>&1
if errorlevel 1 (
    echo Installing cloudinary package...
    pip install cloudinary
    if errorlevel 1 (
        echo ERROR: Failed to install cloudinary package
        pause
        exit /b 1
    )
)

REM Set Cloudinary credentials (nếu chưa có trong environment)
if "%CLOUDINARY_CLOUD_NAME%"=="" (
    echo ERROR: Cloudinary credentials not set!
    echo Please set the following environment variables:
    echo   CLOUDINARY_CLOUD_NAME=your_cloudinary_cloud_name
    echo   CLOUDINARY_API_KEY=your_cloudinary_api_key
    echo   CLOUDINARY_API_SECRET=your_cloudinary_api_secret
    echo.
    echo Or create a .env file with these values.
    pause
    exit /b 1
)

echo Running upload script...
echo.

python scripts/upload_images_to_cloudinary.py

if errorlevel 1 (
    echo.
    echo ERROR: Upload failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo Upload completed!
echo Check scripts/insert_images.sql
echo ========================================
pause

