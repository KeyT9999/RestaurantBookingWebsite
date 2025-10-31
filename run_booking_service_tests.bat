@echo off
cd /d "%~dp0"
mvn clean test -Dtest=BookingServiceTest,BookingServiceComprehensiveTest,BookingServiceCoverageTest,BookingServiceStatusTransitionTest jacoco:report
echo.
echo ============================================
echo Test completed. Check coverage report at:
echo target\site\jacoco\index.html
echo ============================================
pause

