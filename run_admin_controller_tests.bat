@echo off
echo Running Admin Controller Tests...
echo ===================================
cd /d %~dp0
mvn test -Dtest="com.example.booking.web.controller.admin.UltraSimpleControllerTest","com.example.booking.web.controller.admin.WorkingRateLimitingControllerTest","com.example.booking.web.controller.admin.RateLimitingAdminControllerTest","com.example.booking.web.controller.admin.AdminFavoriteControllerTest" jacoco:report
echo.
echo ===================================
echo Tests completed! Coverage report generated at: target\site\jacoco\index.html
pause

















