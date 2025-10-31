@echo off
echo Running API Controller Tests...
echo ===================================
cd /d %~dp0
mvn test -Dtest="SmartWaitlistApiControllerTest","BookingConflictApiControllerTest","BankAccountApiControllerTest","AdminApiControllerTest","AdminApiControllerExpandedTest" jacoco:report
echo.
echo ===================================
echo Tests completed! Coverage report generated at: target\site\jacoco\index.html
pause

