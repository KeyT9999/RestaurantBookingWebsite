@echo off
echo Quick Test - Reset and Run...
echo ===================================
cd /d %~dp0

echo Step 1: Cleaning test results...
call reset_test_folder_quick.bat

echo.
echo Step 2: Compiling and running tests...
echo ===================================
mvn clean test-compile test jacoco:report

echo.
echo ===================================
echo Tests completed!
pause















