@echo off
REM ================================================================
REM Chỉ generate JaCoCo report từ tests đã chạy (NHANH NHẤT)
REM Sử dụng khi tests đã chạy trước đó và chỉ cần report
REM ================================================================

echo.
echo ================================================================
echo   Generating JaCoCo Report Only (No Tests)
echo ================================================================
echo.
echo Đang generate report từ kết quả tests đã có...
echo.

REM Skip tests và chỉ generate report
mvn jacoco:report -DskipTests

echo.
echo ================================================================
echo   Report tại: target/site/jacoco/index.html
echo ================================================================
pause

