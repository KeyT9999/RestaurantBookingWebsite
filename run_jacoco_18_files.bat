@echo off
REM ==========================================
REM Run JaCoCo Report for 18 Files Only
REM ==========================================

echo.
echo ==========================================
echo   JaCoCo Report - 18 Files Only
echo ==========================================
echo.

echo [1/2] Running tests with ignore failures...
mvn clean test -Dmaven.test.failure.ignore=true

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [WARNING] Tests had failures, but continuing...
)

echo.
echo [2/2] Generating JaCoCo report...
mvn jacoco:report

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ==========================================
    echo   Report generated successfully!
    echo ==========================================
    echo.
    echo Report location: target\site\jacoco\index.html
    echo.
    
    REM Try to open report
    if exist "target\site\jacoco\index.html" (
        echo Opening report in browser...
        start target\site\jacoco\index.html
    ) else (
        echo [ERROR] Report file not found!
        echo Please check if tests ran successfully.
    )
) else (
    echo.
    echo [ERROR] Failed to generate report!
    echo Please check Maven output above for errors.
)

echo.
pause

