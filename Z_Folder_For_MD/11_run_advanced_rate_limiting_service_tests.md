# AdvancedRateLimitingService

| # | Mô tả | Câu lệnh |
|---|-------|----------|
| 1 | Chạy TẤT CẢ tests (27 tests) | mvn test -Dtest=AdvancedRateLimitingServiceTest |
| 2 | Chạy nhóm CHECK RATE LIMIT tests (10 tests) | mvn test -Dtest=AdvancedRateLimitingServiceTest$CheckRateLimitTests |
| 3 | Chạy nhóm RESET RATE LIMIT tests (7 tests) | mvn test -Dtest=AdvancedRateLimitingServiceTest$ResetRateLimitTests |
| 4 | Chạy nhóm GET RATE LIMIT STATS tests (8 tests) | mvn test -Dtest=AdvancedRateLimitingServiceTest$GetRateLimitStatsTests |
| 5 | Chạy 1 test cụ thể (ví dụ) | mvn test -Dtest=AdvancedRateLimitingServiceTest$CheckRateLimitTests#testCheckRateLimit_WithValidRequest_ShouldAllowLogin |
| 6 | Chạy với DEBUG mode (xem log chi tiết) | mvn test -Dtest=AdvancedRateLimitingServiceTest -X |
| 7 | Chạy và IGNORE failures (tiếp tục chạy hết) | mvn test -Dtest=AdvancedRateLimitingServiceTest -Dmaven.test.failure.ignore=true |
| 8 | Chạy với code coverage (JaCoCo) | mvn test -Dtest=AdvancedRateLimitingServiceTest jacoco:report |

