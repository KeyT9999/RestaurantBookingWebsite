# 🎯 ƯU TIÊN CẢI THIỆN COVERAGE

## ⚠️ CÓ TEST NHƯNG COVERAGE = 0%

### `web.controller.admin` (0%)
- ✅ AdminModerationControllerTest.java
- ✅ AdminVoucherAnalyticsControllerTest.java

### `web.controller.api` (0%)
- ✅ AIActionsControllerTest.java
- ✅ BankAccountApiControllerTest.java
- ✅ ChatApiControllerTest.java

### `web.controller.restaurantowner` (0%)
- ✅ RestaurantOwnerControllerTest.java

### `web.controller.customer` (0%)
- ✅ FavoriteControllerTest.java

### `websocket` (0%)
- ✅ ChatMessageControllerTest.java

### `aspect` (0%)
- ✅ AuditAspectTest.java

---

## ❌ CÓ TEST NHƯNG COVERAGE < 30%

### `web.controller` (2% - 10 test files)
- AdminDashboardControllerTest.java (2 tests disabled)
- AdminRestaurantControllerTest.java
- AISearchControllerTest.java
- BookingControllerTest.java
- BookingControllerWebMvcTest.java
- GlobalExceptionHandlerTest.java
- HomeControllerTest.java
- PaymentControllerWebMvcTest.java
- RestaurantOwnerControllerTest.java
- RestaurantRegistrationControllerTest.java

### `config` (3% - 6 test files)
- AiCacheConfigTest.java
- AiSyncConfigTest.java
- AiSyncPropertiesTest.java
- AsyncConfigTest.java
- RestTemplateConfigTest.java
- TestRateLimitingConfig.java

### `service` (31% - 33 test files)
- AdvancedRateLimitingServiceTest.java
- AIIntentDispatcherServiceTest.java
- AIResponseProcessorServiceTest.java
- AIServiceTest.java
- AiSyncEventPublisherTest.java
- AuditServiceTest.java
- BankAccountServiceTest.java
- BookingConflictServiceTest.java
- BookingServiceStatusTransitionTest.java
- BookingServiceTest.java
- ChatServiceTest.java
- CustomerServiceTest.java
- EnhancedRefundServiceUnitTest.java
- PaymentServiceTest.java
- PaymentServiceUnitTest.java
- PayOsServiceTest.java
- RefundServiceTest.java
- RestaurantApprovalServiceTest.java
- RestaurantManagementServiceTest.java
- RestaurantOwnerServiceTest.java
- RestaurantSecurityServiceTest.java
- ReviewReportServiceImplTest.java
- SimpleUserServiceTest.java
- VietQRServiceTest.java
- VoucherServiceTest.java
- WaitlistServiceTest.java
- WithdrawalServiceTest.java
- ai/OpenAIServiceTest.java
- ai/RecommendationServiceTest.java
- impl/NotificationServiceImplTest.java

### `dto` (11% - 3 test files)
- AIActionRequestTest.java
- AIActionResponseTest.java
- DtosTest.java

### `service.impl` (16% - 1 test file)
- NotificationServiceImplTest.java

---

## ⚠️ CÓ TEST NHƯNG COVERAGE < 80%

### `util` (32% - 1 test file)
- InputSanitizerTest.java

### `domain` (44% - 7 test files)
- CommunicationHistoryTest.java
- InternalNoteTest.java
- PopularRestaurantDtoTest.java
- RestaurantOwnerTest.java
- converter/DishStatusConverterTest.java
- converter/ServiceStatusConverterTest.java
- converter/TableStatusConverterTest.java

### `exception` (56% - 1 test file)
- GlobalExceptionHandlerTest.java

---

## 📋 TÓM TẮT NGẮN GỌN

**Ưu tiên cao (0% coverage, có test):**
1. web.controller.admin
2. web.controller.api
3. web.controller.restaurantowner
4. web.controller.customer
5. websocket
6. aspect

**Ưu tiên trung (2-31%, có test):**
1. web.controller (2%)
2. config (3%)
3. dto (11%)
4. service.impl (16%)
5. service (31%)

**Ưu tiên thấp (32-79%, có test):**
1. util (32%)
2. domain (44%)
3. exception (56%)



