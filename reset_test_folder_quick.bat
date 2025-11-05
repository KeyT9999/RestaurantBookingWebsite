@echo off
echo Quick reset - removing test results only...
cd /d %~dp0

if exist "target\test-classes" rmdir /s /q "target\test-classes" >nul 2>&1
if exist "target\surefire-reports" rmdir /s /q "target\surefire-reports" >nul 2>&1
if exist "target\jacoco.exec" del /q "target\jacoco.exec" >nul 2>&1
if exist "target\site\jacoco" rmdir /s /q "target\site\jacoco" >nul 2>&1

echo Done! Test results cleared.

















