# AdminDashboardController

| # | Mô tả | Câu lệnh |
|---|-------|----------|
| 1 | Chạy TẤT CẢ tests (24 tests) | mvn test -Dtest=AdminDashboardControllerTest |
| 2 | Chạy nhóm DASHBOARD tests (3 tests) | mvn test -Dtest=AdminDashboardControllerTest$DashboardTests |
| 3 | Chạy nhóm REFUND REQUESTS tests (4 tests) | mvn test -Dtest=AdminDashboardControllerTest$RefundRequestsTests |
| 4 | Chạy nhóm GET STATISTICS tests (7 tests) | mvn test -Dtest=AdminDashboardControllerTest$GetStatisticsTests |
| 5 | Chạy nhóm SECURITY tests (10 tests) | mvn test -Dtest=AdminDashboardControllerTest$SecurityTests |
| 6 | Chạy 1 test cụ thể (ví dụ) | mvn test -Dtest=AdminDashboardControllerTest$DashboardTests#testDashboard_WithValidData_ShouldLoadSuccessfully |
| 7 | Chạy với DEBUG mode (xem log chi tiết) | mvn test -Dtest=AdminDashboardControllerTest -X |
| 8 | Chạy và IGNORE failures (tiếp tục chạy hết) | mvn test -Dtest=AdminDashboardControllerTest -Dmaven.test.failure.ignore=true |
