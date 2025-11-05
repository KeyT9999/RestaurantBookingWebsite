@echo off
echo ===================================
echo Resetting WebSocket Tests Only
echo ===================================
cd /d %~dp0

echo.
echo [1/5] Cleaning test classes for websocket...
if exist "target\test-classes\com\example\booking\websocket" (
    rmdir /s /q "target\test-classes\com\example\booking\websocket"
    echo [OK] Removed websocket test classes
)

echo.
echo [2/5] Cleaning surefire reports for websocket...
if exist "target\surefire-reports" (
    del /f /q "target\surefire-reports\com.example.booking.websocket.*.txt" 2>nul
    echo [OK] Removed websocket test reports
)

echo.
echo [3/5] Cleaning jacoco coverage data...
if exist "target\jacoco.exec" (
    del /q "target\jacoco.exec"
    echo [OK] Removed old coverage data
)

echo.
echo [4/5] Cleaning jacoco reports...
if exist "target\site\jacoco\com.example.booking.websocket" (
    rmdir /s /q "target\site\jacoco\com.example.booking.websocket"
    echo [OK] Removed websocket coverage reports
)

echo.
echo [5/5] Done!
echo ===================================
echo WebSocket tests reset complete!
echo ===================================
echo.
echo Next: Run websocket tests with:
echo   mvn test -Dtest="ChatMessageControllerTest,ChatMessageControllerExpandedTest"
echo.
pause

















