# 📋 DANH SÁCH CÁC FILE TEST SẼ CHẠY KHI DÙNG `mvn test`

## 🔍 Quy tắc Maven Surefire Plugin

Khi chạy `mvn test`, Maven Surefire Plugin sẽ tự động chạy **TẤT CẢ** các file test có tên theo pattern:

- `**/Test*.java` - Ví dụ: `TestSomething.java`
- `**/*Test.java` - Ví dụ: `SomethingTest.java` ✅ **Pattern chính**
- `**/*Tests.java` - Ví dụ: `SomethingTests.java`
- `**/*TestCase.java` - Ví dụ: `SomethingTestCase.java`

## 📊 Tổng hợp các file test sẽ chạy

**Tổng số file test: 79 file**

### ✅ Danh sách đầy đủ (79 files):

#### 🎯 **Application Tests (1 file)**
1. `RestaurantBookingApplicationTest.java`

#### 🧪 **Integration Tests (2 files)**
2. `integration/BookingEndToEndIntegrationTest.java`
3. `integration/BookingIntegrationTest.java`

#### 🔧 **Config Tests (6 files)**
4. `config/AiCacheConfigTest.java`
5. `config/AiSyncConfigTest.java`
6. `config/AiSyncPropertiesTest.java`
7. `config/AsyncConfigTest.java`
8. `config/RestTemplateConfigTest.java`
9. `config/TestRateLimitingConfig.java`

#### 📦 **Domain Tests (5 files)**
10. `domain/CommunicationHistoryTest.java`
11. `domain/InternalNoteTest.java`
12. `domain/PopularRestaurantDtoTest.java`
13. `domain/RestaurantOwnerTest.java`
14. `domain/converter/DishStatusConverterTest.java`
15. `domain/converter/ServiceStatusConverterTest.java`
16. `domain/converter/TableStatusConverterTest.java`

#### 🏷️ **DTO Tests (3 files)**
17. `dto/AIActionRequestTest.java`
18. `dto/AIActionResponseTest.java`
19. `dto/DtosTest.java`

#### 🗄️ **Repository Tests (2 files)**
20. `repository/CommunicationHistoryRepositoryTest.java`
21. `repository/InternalNoteRepositoryTest.java`

#### ⚙️ **Service Tests (33 files)**
22. `service/AdvancedRateLimitingServiceTest.java`
23. `service/AIIntentDispatcherServiceTest.java`
24. `service/AIResponseProcessorServiceTest.java`
25. `service/AIServiceTest.java`
26. `service/AiSyncEventPublisherTest.java`
27. `service/AuditServiceTest.java`
28. `service/BankAccountServiceTest.java`
29. `service/BookingConflictServiceTest.java`
30. `service/BookingServiceStatusTransitionTest.java`
31. `service/BookingServiceTest.java`
32. `service/ChatServiceTest.java`
33. `service/CustomerServiceTest.java`
34. `service/EnhancedRefundServiceUnitTest.java`
35. `service/impl/NotificationServiceImplTest.java`
36. `service/PaymentServiceTest.java`
37. `service/PaymentServiceUnitTest.java`
38. `service/PayOsServiceTest.java`
39. `service/RefundServiceTest.java`
40. `service/RestaurantApprovalServiceTest.java`
41. `service/RestaurantManagementServiceTest.java`
42. `service/RestaurantOwnerServiceTest.java`
43. `service/RestaurantSecurityServiceTest.java`
44. `service/ReviewReportServiceImplTest.java`
45. `service/SimpleUserServiceTest.java`
46. `service/VietQRServiceTest.java`
47. `service/VoucherServiceTest.java`
48. `service/WaitlistServiceTest.java`
49. `service/WithdrawalServiceTest.java`
50. `service/ai/OpenAIServiceTest.java`
51. `service/ai/RecommendationServiceTest.java`

#### 📅 **Scheduler Tests (2 files)**
52. `scheduler/PayOSReconciliationSchedulerTest.java`
53. `scheduler/VoucherSchedulerTest.java`

#### 🌐 **Controller Tests (17 files)**
54. `web/controller/AdminDashboardControllerTest.java`
55. `web/controller/AdminRestaurantControllerTest.java`
56. `web/controller/AISearchControllerTest.java`
57. `web/controller/BookingControllerTest.java`
58. `web/controller/BookingControllerWebMvcTest.java`
59. `web/controller/GlobalExceptionHandlerTest.java`
60. `web/controller/HomeControllerTest.java`
61. `web/controller/PaymentControllerWebMvcTest.java`
62. `web/controller/RestaurantOwnerControllerTest.java`
63. `web/controller/RestaurantRegistrationControllerTest.java`
64. `web/controller/admin/AdminModerationControllerTest.java`
65. `web/controller/admin/AdminVoucherAnalyticsControllerTest.java`
66. `web/controller/api/AIActionsControllerTest.java`
67. `web/controller/api/BankAccountApiControllerTest.java`
68. `web/controller/api/ChatApiControllerTest.java`
69. `web/controller/customer/FavoriteControllerTest.java`

#### 🔌 **WebSocket Tests (1 file)**
70. `websocket/ChatMessageControllerTest.java`

#### 💡 **Advice Tests (2 files)**
71. `advice/NotificationHeaderAdviceTest.java`
72. `web/advice/ApiResponseAdviceTest.java`

#### 🔍 **Aspect Tests (1 file)**
73. `aspect/AuditAspectTest.java`

#### 🔄 **Converter Tests (1 file)**
74. `converter/UserRoleStringConverterTest.java`

#### 📊 **Enum Tests (1 file)**
75. `common/enums/RestaurantApprovalStatusTest.java`

#### 🛠️ **Util Tests (1 file)**
76. `util/InputSanitizerTest.java`

#### 🧪 **Simple/Test Tests (4 files)**
77. `test/simple/AssertJDemoTest.java`
78. `test/simple/BookingControllerSimpleTest.java`
79. `test/simple/SimpleBookingTest.java`
80. `test/util/MockDataFactoryTest.java`

## 📝 Lưu ý quan trọng

### ✅ Các file SẼ chạy:
- Tất cả file có tên kết thúc bằng `Test.java` (pattern: `*Test.java`)
- Nằm trong thư mục `src/test/java`

### ❌ Các file KHÔNG chạy:
- File có tên không khớp pattern trên
- File trong thư mục `src/main/java`
- File có annotation `@Ignore` hoặc `@Disabled`
- File không compile được

## 🎯 Cách kiểm tra lại

### Xem danh sách test sẽ chạy (không chạy thực tế):
```bash
mvn test -Dtest=*Test -DskipTests=true
```

### Chạy tất cả tests:
```bash
mvn test
```

### Chạy test cụ thể:
```bash
mvn test -Dtest=InputSanitizerTest
```

### Xem danh sách test sau khi chạy:
```bash
mvn test
# Xem trong: target/surefire-reports/
```

## 📊 Phân loại theo loại test

- **Unit Tests**: ~60 files
- **Integration Tests**: 2 files
- **Controller Tests**: 17 files
- **Service Tests**: 33 files
- **Config/Utility Tests**: ~10 files

---

**Lưu ý**: File này được tạo tự động. Để xem danh sách chính xác, chạy:
```bash
mvn test -Dtest=*Test -DskipTests=true
```

