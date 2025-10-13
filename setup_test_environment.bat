@echo off
echo 🚀 Setting up test environment for Refund API
echo.

echo 📋 Step 1: Check if PostgreSQL is running
pg_isready -h localhost -p 5432
if %errorlevel% neq 0 (
    echo ❌ PostgreSQL is not running. Please start PostgreSQL first.
    pause
    exit /b 1
)
echo ✅ PostgreSQL is running

echo.
echo 📋 Step 2: Create test database (if not exists)
psql -U postgres -c "CREATE DATABASE restaurant_booking_test;" 2>nul
if %errorlevel% equ 0 (
    echo ✅ Test database created
) else (
    echo ℹ️ Test database already exists
)

echo.
echo 📋 Step 3: Run payment ledger migration
echo Running add_payment_ledger.sql...
psql -U postgres -d restaurant_booking_test -f add_payment_ledger.sql
if %errorlevel% equ 0 (
    echo ✅ Payment ledger tables created successfully
) else (
    echo ❌ Failed to create payment ledger tables
    pause
    exit /b 1
)

echo.
echo 📋 Step 4: Insert test data
echo Running test_database_setup.sql...
psql -U postgres -d restaurant_booking_test -f test_database_setup.sql
if %errorlevel% equ 0 (
    echo ✅ Test data inserted successfully
) else (
    echo ❌ Failed to insert test data
    pause
    exit /b 1
)

echo.
echo 📋 Step 5: Verify test data
echo Checking test payments...
psql -U postgres -d restaurant_booking_test -c "SELECT payment_id, amount, status, payment_method FROM payment WHERE payment_id IN (1,2,3);"

echo.
echo 📋 Step 6: Check payment ledger
echo Checking payment ledger entries...
psql -U postgres -d restaurant_booking_test -c "SELECT COUNT(*) as ledger_count FROM payment_ledger;"

echo.
echo 🎉 Test environment setup completed!
echo.
echo 📝 Next steps:
echo    1. Start Spring Boot application: mvn spring-boot:run
echo    2. Run API tests: .\test_curl_commands.bat
echo    3. Or use Postman with Refund_API_Postman_Collection.json
echo.
pause
