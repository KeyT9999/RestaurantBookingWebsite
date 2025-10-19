@echo off
REM AI Recommendation Engine Migration Script
REM File: run_ai_migration.bat

echo =====================================================
echo AI RECOMMENDATION ENGINE MIGRATION
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

REM Run migration
echo Running AI Recommendation Engine migration...
echo.

psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -f database/run_ai_migration.sql

if %errorlevel% equ 0 (
    echo.
    echo =====================================================
    echo MIGRATION COMPLETED SUCCESSFULLY!
    echo =====================================================
    echo.
    echo Next steps:
    echo 1. Update your .env file with OpenAI API key
    echo 2. Set AI_ENABLED=true in your environment
    echo 3. Restart your Spring Boot application
    echo 4. Test AI endpoints
    echo.
    echo AI Features available:
    echo - User Preferences Learning
    echo - AI Restaurant Recommendations
    echo - Real-time Chat Assistant
    echo - Smart Search with Context
    echo - Analytics and Monitoring
    echo =====================================================
) else (
    echo.
    echo =====================================================
    echo MIGRATION FAILED!
    echo =====================================================
    echo Please check the error messages above and try again
)

echo.
pause
