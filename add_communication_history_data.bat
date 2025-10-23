@echo off
echo Adding sample communication history data...
echo.

REM Wait for Docker to be ready
echo Waiting for Docker to be ready...
timeout /t 5 /nobreak >nul

REM Run the SQL script
echo Running SQL script...
Get-Content add_communication_history_sample_data.sql | docker exec -i restaurantbooking_postgres_1 psql -U postgres -d book_eat_db

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Sample communication history data added successfully!
    echo.
    echo You can now test the communication history APIs:
    echo - GET /restaurant-owner/bookings/{id}/communication-history
    echo - POST /restaurant-owner/bookings/{id}/add-communication
    echo - POST /restaurant-owner/bookings/{id}/delete-communication
) else (
    echo.
    echo ❌ Error adding sample data. Please check Docker is running.
)

echo.
pause
