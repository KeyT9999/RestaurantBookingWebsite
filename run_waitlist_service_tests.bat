@echo off
REM ===============================================
REM Batch Script to Run WaitlistService Tests
REM ===============================================
REM
REM This script runs JUnit tests for WaitlistService
REM Coverage:
REM   1. addToWaitlist() - 10 test cases
REM   2. convertWaitlistToBooking() (confirmWaitlistToBooking) - 12 test cases
REM   3. getWaitlistByCustomer() - 5 test cases
REM   4. calculateEstimatedWaitTime() - 7 test cases
REM   5. removeFromWaitlist() (cancelWaitlist) - 6 test cases
REM   Total: 40 test cases
REM
REM Usage:
REM   run_waitlist_service_tests.bat
REM
REM ===============================================

echo.
echo ===============================================
echo   WaitlistService Test Suite
echo ===============================================
echo.
echo Running JUnit tests for WaitlistService...
echo.

REM Run the specific test class
mvn test "-Dtest=WaitlistServiceTest" "-Dspring.profiles.active=test"

echo.
echo ===============================================
echo   Test Execution Complete
echo ===============================================
echo.

REM Check if tests passed
if %errorlevel% neq 0 (
    echo [ERROR] Some tests failed! Please check the output above.
    echo.
    pause
    exit /b %errorlevel%
) else (
    echo [SUCCESS] All tests passed!
    echo.
)

pause

