@echo off
echo ============================================
echo Running All Tests with JaCoCo Coverage
echo ============================================
echo.

REM Run all tests with JaCoCo coverage report
mvn clean test jacoco:report

echo.
echo ============================================
echo Test execution completed!
echo.
echo JaCoCo coverage report generated at:
echo target\site\jacoco\index.html
echo ============================================

pause

