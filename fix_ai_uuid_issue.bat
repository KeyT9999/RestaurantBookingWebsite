@echo off
REM Fix AI Tables UUID Generation Issue
REM File: fix_ai_uuid_issue.bat

echo =====================================================
echo FIXING AI TABLES UUID GENERATION ISSUE
echo =====================================================

REM Check if PostgreSQL is running
echo Checking PostgreSQL connection...
psql --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: PostgreSQL is not installed or not in PATH
    echo Please install PostgreSQL and add it to your PATH
    pause
    exit /b 1
)

REM Set database connection parameters
set DB_HOST=localhost
set DB_PORT=5432
set DB_NAME=bookeat_db
set DB_USER=postgres

echo.
echo Database Configuration:
echo Host: %DB_HOST%
echo Port: %DB_PORT%
echo Database: %DB_NAME%
echo User: %DB_USER%
echo.

REM Prompt for password
set /p DB_PASSWORD=Enter PostgreSQL password: 

REM Test database connection
echo Testing database connection...
psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -c "SELECT version();" >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Cannot connect to database
    echo Please check your database configuration
    pause
    exit /b 1
)

echo Database connection successful!
echo.

REM Run fix script
echo Running AI UUID generation fix...
echo This will:
echo 1. Drop existing AI tables
echo 2. Recreate them with proper UUID generation
echo 3. Create indexes for performance
echo 4. Insert default configuration data
echo 5. Insert sample data for testing
echo.

psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -f database/fix_ai_uuid_generation.sql

if %errorlevel% equ 0 (
    echo.
    echo =====================================================
    echo UUID GENERATION ISSUE FIXED SUCCESSFULLY!
    echo =====================================================
    echo.
    echo What was fixed:
    echo - All AI tables recreated with proper UUID generation
    echo - Entity classes updated with correct column definitions
    echo - Indexes created for performance optimization
    echo - Default configuration data inserted
    echo - Sample data inserted for testing
    echo.
    echo Next steps:
    echo 1. Restart your Spring Boot application
    echo 2. Test AI entity creation
    echo 3. Verify UUID generation is working
    echo.
    echo AI Features now ready:
    echo - User Preferences Learning
    echo - AI Restaurant Recommendations
    echo - Real-time Chat Assistant
    echo - Smart Search with Context
    echo - Analytics and Monitoring
    echo =====================================================
) else (
    echo.
    echo =====================================================
    echo FIX FAILED!
    echo =====================================================
    echo Please check the error messages above and try again
    echo Make sure:
    echo 1. Database connection is working
    echo 2. You have proper permissions
    echo 3. No other processes are using the tables
)

echo.
pause
