@echo off
echo ================================================
echo   BookingConflictService Test Runner
echo ================================================
echo.
echo Running all BookingConflictService tests...
echo.

mvn test "-Dtest=BookingConflictServiceTest" "-Dspring.profiles.active=test"

echo.
echo ================================================
echo   Test execution completed!
echo ================================================
pause

