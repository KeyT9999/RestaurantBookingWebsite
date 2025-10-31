@echo off
echo ===================================
echo Running WebSocket Tests
echo ===================================
cd /d %~dp0

echo.
echo Running only DTO tests to improve coverage...
mvn test -Dtest=WebSocketDTOTest -DfailIfNoTests=false

echo.
echo Generating coverage report...
mvn jacoco:report -DfailIfNoTests=false

echo.
echo ===================================
echo WebSocket test run complete!
echo ===================================
echo.
echo Coverage report generated at:
echo target/site/jacoco/com.example.booking.websocket/index.html
echo.
pause


