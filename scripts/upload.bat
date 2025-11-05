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
    echo Setting Cloudinary credentials...
    set CLOUDINARY_CLOUD_NAME=drcly5nge
    set CLOUDINARY_API_KEY=574438289271325
    set CLOUDINARY_API_SECRET=dDyQjA3bmFgf_7fdsJFEXs4DTSA
    set CLOUDINARY_SECURE=true
    echo Credentials set successfully!
    echo.
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

