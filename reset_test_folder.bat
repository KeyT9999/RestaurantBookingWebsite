@echo off
echo Resetting test folder only...
echo ===================================
cd /d %~dp0

echo Cleaning test classes...
if exist "target\test-classes" (
    rmdir /s /q "target\test-classes"
    echo [OK] Removed target\test-classes
)

echo Cleaning test reports...
if exist "target\surefire-reports" (
    rmdir /s /q "target\surefire-reports"
    echo [OK] Removed target\surefire-reports
)

echo Cleaning coverage data...
if exist "target\jacoco.exec" (
    del /q "target\jacoco.exec"
    echo [OK] Removed target\jacoco.exec
)

echo Cleaning coverage reports...
if exist "target\site\jacoco" (
    rmdir /s /q "target\site\jacoco"
    echo [OK] Removed target\site\jacoco
)

echo Cleaning generated test sources...
if exist "target\generated-test-sources" (
    rmdir /s /q "target\generated-test-sources"
    echo [OK] Removed target\generated-test-sources
)

echo.
echo ===================================
echo Test folder reset complete!
echo You can now run tests fresh with: mvn test
pause















