@echo off
REM ================================================================
REM Fast Controller Tests Runner với JaCoCo Report
REM Chỉ chạy tests trong package web.controller
REM ================================================================

echo.
echo ================================================================
echo   Fast Controller Tests + JaCoCo Report
echo ================================================================
echo.

echo Option 1: Chạy chỉ Controller tests...
mvn test -Dtest="*Controller*Test,*Controller*WebMvcTest" jacoco:report -DfailIfNoTests=false

echo.
echo ================================================================
echo   Report tại: target/site/jacoco/index.html
echo ================================================================
pause

