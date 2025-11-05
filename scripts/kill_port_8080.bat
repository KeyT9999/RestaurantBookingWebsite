@echo off
REM Script để tắt process đang dùng port 8080
echo ========================================
echo TẮT PROCESS TRÊN PORT 8080
echo ========================================
echo.

REM Tìm process đang dùng port 8080
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do (
    set PID=%%a
    echo Found process: PID %%a
    echo Killing process...
    taskkill /PID %%a /F >nul 2>&1
    if errorlevel 1 (
        echo ERROR: Khong the kill process %%a
    ) else (
        echo SUCCESS: Da tat process %%a
    )
    goto :done
)

echo Khong co process nao dang dung port 8080

:done
echo.
echo ========================================
pause

