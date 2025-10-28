# AdminRestaurantController

| # | Mô tả | Câu lệnh |
|---|-------|----------|
| 1 | Chạy TẤT CẢ tests (36 tests) | mvn test -Dtest=AdminRestaurantControllerTest |
| 2 | Chạy nhóm APPROVE tests (8 tests) | mvn test -Dtest=AdminRestaurantControllerTest$ApproveRestaurantTests |
| 3 | Chạy nhóm REJECT tests (8 tests) | mvn test -Dtest=AdminRestaurantControllerTest$RejectRestaurantTests |
| 4 | Chạy nhóm GET tests (13 tests) | mvn test -Dtest=AdminRestaurantControllerTest$GetRestaurantsTests |
| 5 | Chạy nhóm SECURITY tests (7 tests) | mvn test -Dtest=AdminRestaurantControllerTest$SecurityTests |
| 6 | Chạy 1 test cụ thể (ví dụ) | mvn test -Dtest=AdminRestaurantControllerTest$ApproveRestaurantTests#testApproveRestaurant_WithPendingStatus_ShouldApproveSuccessfully |
| 7 | Chạy với DEBUG mode (xem log chi tiết) | mvn test -Dtest=AdminRestaurantControllerTest -X |
| 8 | Chạy và IGNORE failures (tiếp tục chạy hết) | mvn test -Dtest=AdminRestaurantControllerTest -Dmaven.test.failure.ignore=true |
