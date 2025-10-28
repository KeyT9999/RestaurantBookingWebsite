@echo off
REM ===============================================
REM Batch Script to Run WithdrawalService Tests
REM ===============================================
REM
REM This script runs JUnit tests for WithdrawalService
REM Coverage:
REM   1. requestWithdrawal() - 8+ test cases
REM   2. processWithdrawal() (approveWithdrawal) - 6+ test cases
REM   3. rejectWithdrawal() - 4+ test cases
REM   4. getWithdrawalHistory() (getWithdrawalsByRestaurant) - 3+ test cases
REM   Total: 21+ test cases
REM
REM Usage:
REM   run_withdrawal_service_tests.bat
REM
REM ===============================================

echo.
echo ===============================================
echo   WithdrawalService Test Suite
echo ===============================================
echo.
echo Running JUnit tests for WithdrawalService...
echo.

REM Run the specific test class
mvn test "-Dtest=WithdrawalServiceTest" "-Dspring.profiles.active=test"

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

