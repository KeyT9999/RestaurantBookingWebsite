@echo off
echo ============================================
echo Running CORE Unit Tests Only
echo ============================================
echo.

REM Run only the core business logic test suites
mvn test "-Dtest=PayOsServiceTest,BookingServiceTest,RefundServiceTest,NotificationServiceImplTest,RestaurantManagementServiceTest,RestaurantOwnerServiceTest,RestaurantApprovalServiceTest,SimpleUserServiceTest,RestaurantOwnerTest,RestaurantOwnerControllerTest,RestaurantRegistrationControllerTest" -DfailIfNoTests=false

echo.
echo ============================================
echo Test Execution Complete
echo ============================================
pause

