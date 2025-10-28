@echo off
echo ================================================
echo RestaurantSecurityService - Test Runner
echo ================================================
echo.
echo Chon loai test ban muon chay:
echo.
echo 1. Chay TAT CA tests (21 tests: 17 moi + 4 legacy)
echo 2. Chay CHECK SECURITY STATUS tests (8 tests)
echo 3. Chay REPORT SUSPICIOUS ACTIVITY tests (9 tests)
echo 4. Chay voi DEBUG mode
echo 5. Chay va tao coverage report
echo 6. Chay va IGNORE failures
echo 7. Chay va tao HTML report
echo 8. Thoat
echo.
set /p choice="Nhap lua chon cua ban (1-8): "

if "%choice%"=="1" (
    echo.
    echo [INFO] Chay TAT CA tests...
    echo ================================================
    mvn test -Dtest=RestaurantSecurityServiceTest
    goto end
)

if "%choice%"=="2" (
    echo.
    echo [INFO] Chay CHECK SECURITY STATUS tests...
    echo ================================================
    mvn test -Dtest=RestaurantSecurityServiceTest$CheckSecurityStatusTests
    goto end
)

if "%choice%"=="3" (
    echo.
    echo [INFO] Chay REPORT SUSPICIOUS ACTIVITY tests...
    echo ================================================
    mvn test -Dtest=RestaurantSecurityServiceTest$ReportSuspiciousActivityTests
    goto end
)

if "%choice%"=="4" (
    echo.
    echo [INFO] Chay voi DEBUG mode...
    echo ================================================
    mvn test -Dtest=RestaurantSecurityServiceTest -X
    goto end
)

if "%choice%"=="5" (
    echo.
    echo [INFO] Chay va tao coverage report...
    echo ================================================
    mvn test -Dtest=RestaurantSecurityServiceTest jacoco:report
    echo.
    echo [INFO] Coverage report: target/site/jacoco/index.html
    goto end
)

if "%choice%"=="6" (
    echo.
    echo [INFO] Chay va IGNORE failures...
    echo ================================================
    mvn test -Dtest=RestaurantSecurityServiceTest -Dmaven.test.failure.ignore=true
    goto end
)

if "%choice%"=="7" (
    echo.
    echo [INFO] Chay va tao HTML report...
    echo ================================================
    mvn test -Dtest=RestaurantSecurityServiceTest
    mvn surefire-report:report
    echo.
    echo [INFO] HTML report: target/site/surefire-report.html
    goto end
)

if "%choice%"=="8" (
    echo.
    echo [INFO] Thoat chuong trinh.
    goto end
)

echo.
echo [ERROR] Lua chon khong hop le! Vui long chon tu 1-8.
pause
goto :eof

:end
echo.
echo ================================================
echo Hoan thanh!
echo ================================================
pause

