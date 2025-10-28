# RestaurantSecurityService

| # | Mô tả | Câu lệnh |
|---|-------|----------|
| 1 | Chạy TẤT CẢ tests (21 tests: 17 mới + 4 legacy) | mvn test -Dtest=RestaurantSecurityServiceTest |
| 2 | Chạy nhóm CHECK SECURITY STATUS tests (8 tests) | mvn test -Dtest=RestaurantSecurityServiceTest$CheckSecurityStatusTests |
| 3 | Chạy nhóm REPORT SUSPICIOUS ACTIVITY tests (9 tests) | mvn test -Dtest=RestaurantSecurityServiceTest$ReportSuspiciousActivityTests |
| 4 | Chạy 1 test cụ thể (ví dụ) | mvn test -Dtest=RestaurantSecurityServiceTest$CheckSecurityStatusTests#testCheckSecurityStatus_WithActiveAndApprovedUser_ShouldReturnTrue |
| 5 | Chạy với DEBUG mode (xem log chi tiết) | mvn test -Dtest=RestaurantSecurityServiceTest -X |
| 6 | Chạy và IGNORE failures (tiếp tục chạy hết) | mvn test -Dtest=RestaurantSecurityServiceTest -Dmaven.test.failure.ignore=true |
| 7 | Chạy với code coverage (JaCoCo) | mvn test -Dtest=RestaurantSecurityServiceTest jacoco:report |
| 8 | Chạy và tạo report HTML | mvn surefire-report:report -Dtest=RestaurantSecurityServiceTest |


