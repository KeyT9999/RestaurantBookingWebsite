@echo off
echo ============================================
echo Running ALL Unit Tests
echo ============================================
echo.

REM Run all test classes matching *Test pattern
mvn test "-Dtest=*Test" -DfailIfNoTests=false

echo.
echo ============================================
echo Test Execution Complete
echo ============================================
pause

