@echo off
REM ================================================================
REM CustomerService Unit Test Runner
REM ================================================================
REM Script để chạy unit tests cho CustomerService
REM
REM Test Coverage:
REM 1. findByUsername() - 6 test cases
REM 2. findById() - 5 test cases
REM 3. findByUserId() - 4 test cases
REM 4. findAllCustomers() - 5 test cases
REM 5. save() - 9 test cases
REM
REM Total: 29 test cases
REM ================================================================

echo.
echo ================================================================
echo     RUNNING CUSTOMERSERVICE UNIT TESTS
echo ================================================================
echo.
echo Test Class: CustomerServiceTest
echo Total Tests: 29
echo.

REM Activate test profile
set SPRING_PROFILES_ACTIVE=test

echo Starting tests...
echo.

REM Run all CustomerService tests
call mvn test -Dtest=CustomerServiceTest -Dspring.profiles.active=test

echo.
echo ================================================================
echo     TEST EXECUTION COMPLETED
echo ================================================================
echo.

REM Display test results
if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] All tests passed! ✓
    echo.
    echo Check the detailed report at:
    echo target/surefire-reports/com.example.booking.service.CustomerServiceTest.txt
) else (
    echo [FAILED] Some tests failed! ✗
    echo.
    echo Please check the error messages above.
)

echo.
echo ================================================================
echo     AVAILABLE TEST SUITES
echo ================================================================
echo.
echo Run specific test suite:
echo.
echo 1. FindByUsername Tests:
echo    mvn test -Dtest=CustomerServiceTest$FindByUsernameTests -Dspring.profiles.active=test
echo.
echo 2. FindById Tests:
echo    mvn test -Dtest=CustomerServiceTest$FindByIdTests -Dspring.profiles.active=test
echo.
echo 3. FindByUserId Tests:
echo    mvn test -Dtest=CustomerServiceTest$FindByUserIdTests -Dspring.profiles.active=test
echo.
echo 4. FindAllCustomers Tests:
echo    mvn test -Dtest=CustomerServiceTest$FindAllCustomersTests -Dspring.profiles.active=test
echo.
echo 5. Save Tests:
echo    mvn test -Dtest=CustomerServiceTest$SaveTests -Dspring.profiles.active=test
echo.

pause




















