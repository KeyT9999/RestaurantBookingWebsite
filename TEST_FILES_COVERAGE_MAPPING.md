# 🔗 BẢNG ĐỐI CHIẾU: FILE TEST ↔️ PACKAGE COVERAGE < 80%

## 📊 BẢNG TÓM TẮT

| Package | Coverage | Số File Test | File Test Có Sẵn | Vấn Đề |
|---------|----------|--------------|------------------|--------|
| `web.controller.admin` | **0%** | 2 | ✅ AdminModerationControllerTest<br>✅ AdminVoucherAnalyticsControllerTest | **Test có nhưng coverage = 0%** ⚠️ |
| `web.controller.api` | **0%** | 3 | ✅ AIActionsControllerTest<br>✅ BankAccountApiControllerTest<br>✅ ChatApiControllerTest | **Test có nhưng coverage = 0%** ⚠️ |
| `web.controller.restaurantowner` | **0%** | 1 | ✅ RestaurantOwnerControllerTest | **Test có nhưng coverage = 0%** ⚠️ |
| `web.controller.customer` | **0%** | 1 | ✅ FavoriteControllerTest | **Test có nhưng coverage = 0%** ⚠️ |
| `web.controller` | **2%** | 10 | ✅ AdminDashboardControllerTest (2 tests disabled)<br>✅ AdminRestaurantControllerTest<br>✅ AISearchControllerTest<br>✅ BookingControllerTest<br>✅ BookingControllerWebMvcTest<br>✅ GlobalExceptionHandlerTest<br>✅ HomeControllerTest<br>✅ PaymentControllerWebMvcTest<br>✅ RestaurantOwnerControllerTest<br>✅ RestaurantRegistrationControllerTest | **10 test files nhưng chỉ 2% coverage** ❌❌ |
| `config` | **3%** | 6 | ✅ AiCacheConfigTest<br>✅ AiSyncConfigTest<br>✅ AiSyncPropertiesTest<br>✅ AsyncConfigTest<br>✅ RestTemplateConfigTest<br>✅ TestRateLimitingConfig | **6 test files nhưng chỉ 3% coverage** ❌ |
| `websocket` | **0%** | 1 | ✅ ChatMessageControllerTest | **Test có nhưng coverage = 0%** ⚠️ |
| `aspect` | **0%** | 1 | ✅ AuditAspectTest | **Test có nhưng coverage = 0%** ⚠️ |
| `service.impl` | **16%** | 1 | ✅ NotificationServiceImplTest | **1 test file, 16% coverage** ⚠️ |
| `dto` | **11%** | 3 | ✅ AIActionRequestTest<br>✅ AIActionResponseTest<br>✅ DtosTest | **Chỉ 3 test files cho toàn bộ DTO** ⚠️ |
| `dto.admin` | **0%** | 0 | ❌ **KHÔNG CÓ** | **Cần tạo test** 🆕 |
| `dto.customer` | **0%** | 0 | ❌ **KHÔNG CÓ** | **Cần tạo test** 🆕 |
| `dto.ai` | **19%** | 0 | ❌ **KHÔNG CÓ RIÊNG** | Test gián tiếp qua AI service tests |
| `dto.vietqr` | **70%** | 0 | ❌ **KHÔNG CÓ RIÊNG** | Test qua VietQRServiceTest |
| `service` | **31%** | 33 | ✅ AdvancedRateLimitingServiceTest<br>✅ AIIntentDispatcherServiceTest<br>✅ AIResponseProcessorServiceTest<br>✅ AIServiceTest<br>✅ AiSyncEventPublisherTest<br>✅ AuditServiceTest<br>✅ BankAccountServiceTest<br>✅ BookingConflictServiceTest<br>✅ BookingServiceStatusTransitionTest<br>✅ BookingServiceTest<br>✅ ChatServiceTest<br>✅ CustomerServiceTest<br>✅ EnhancedRefundServiceUnitTest<br>✅ PaymentServiceTest<br>✅ PaymentServiceUnitTest<br>✅ PayOsServiceTest<br>✅ RefundServiceTest<br>✅ RestaurantApprovalServiceTest<br>✅ RestaurantManagementServiceTest<br>✅ RestaurantOwnerServiceTest<br>✅ RestaurantSecurityServiceTest<br>✅ SimpleUserServiceTest<br>✅ VietQRServiceTest<br>✅ VoucherServiceTest<br>✅ WaitlistServiceTest<br>✅ WithdrawalServiceTest<br>✅ ai/OpenAIServiceTest<br>✅ ai/RecommendationServiceTest<br>✅ impl/NotificationServiceImplTest<br>✅ ReviewReportServiceImplTest | **33 test files nhưng chỉ 31% coverage** ⚠️ |
| `domain` | **44%** | 7 | ✅ CommunicationHistoryTest<br>✅ InternalNoteTest<br>✅ PopularRestaurantDtoTest<br>✅ RestaurantOwnerTest<br>✅ converter/DishStatusConverterTest<br>✅ converter/ServiceStatusConverterTest<br>✅ converter/TableStatusConverterTest | **7 test files, 44% coverage** ⚠️ |
| `util` | **32%** | 1 | ✅ InputSanitizerTest | **1 test file, 32% coverage** ⚠️ |
| `exception` | **56%** | 1 | ✅ GlobalExceptionHandlerTest | **Có test cho handler nhưng thiếu test cho exception classes** ⚠️ |
| `validation` | **0%** | 0 | ❌ **KHÔNG CÓ** | **Cần tạo test** 🆕 |
| `mapper` | **0%** | 0 | ❌ **KHÔNG CÓ** | **Cần tạo test** 🆕 |
| `common.api` | **0%** | 0 | ❌ **KHÔNG CÓ** | **Cần tạo test** 🆕 |
| `common.util` | **0%** | 0 | ❌ **KHÔNG CÓ** | **Cần tạo test** 🆕 |
| `common.base` | **0%** | 0 | ❌ **KHÔNG CÓ** | **Cần tạo test** 🆕 |
| `common.constants` | **0%** | 0 | ❌ **KHÔNG CÓ** | **Cần tạo test** 🆕 |
| `scheduler` | **76%** | 2 | ✅ PayOSReconciliationSchedulerTest<br>✅ VoucherSchedulerTest | ✅ **Gần đạt mục tiêu** |
| `entity` | **76%** | 0 | ❌ **KHÔNG CÓ RIÊNG** | Test gián tiếp qua domain tests |
| `audit` | **79%** | 1 | ✅ AuditServiceTest | ✅ **Gần đạt mục tiêu** |

---

## 🔍 PHÂN TÍCH CHI TIẾT

### ⚠️ **NHÓM 1: CÓ TEST FILE NHƯNG COVERAGE = 0%** (NGUY HIỂM)

Các package này **CÓ file test** nhưng coverage = 0%. Có thể do:

1. **Test không chạy được:**
   - Compile error
   - Runtime exception
   - Test bị skip trong Maven config

2. **Test không map đúng với source code:**
   - Test sai package/class
   - Test không gọi đúng methods

3. **Test bị disabled:**
   - `@Disabled` annotation
   - Test bị skip trong surefire config

#### Package bị ảnh hưởng:
- ✅ `web.controller.admin` - 2 test files
- ✅ `web.controller.api` - 3 test files  
- ✅ `web.controller.restaurantowner` - 1 test file
- ✅ `web.controller.customer` - 1 test file
- ✅ `websocket` - 1 test file
- ✅ `aspect` - 1 test file

**👉 CẦN KIỂM TRA NGAY:**
```bash
# Chạy test riêng cho các package này
mvn test -Dtest=AdminModerationControllerTest
mvn test -Dtest=AdminVoucherAnalyticsControllerTest
mvn test -Dtest=*ApiControllerTest
mvn test -Dtest=RestaurantOwnerControllerTest
mvn test -Dtest=FavoriteControllerTest
mvn test -Dtest=ChatMessageControllerTest
mvn test -Dtest=AuditAspectTest
```

---

### ❌ **NHÓM 2: CÓ TEST FILE NHƯNG COVERAGE < 30%** (NGHIÊM TRỌNG)

#### `web.controller` - **2% coverage** với **10 test files** ❌❌

**File test:**
- AdminDashboardControllerTest (có 2 tests disabled)
- AdminRestaurantControllerTest
- AISearchControllerTest
- BookingControllerTest
- BookingControllerWebMvcTest
- GlobalExceptionHandlerTest
- HomeControllerTest
- PaymentControllerWebMvcTest
- RestaurantOwnerControllerTest
- RestaurantRegistrationControllerTest

**Phân tích:**
- Có thể test chỉ cover một phần nhỏ methods
- Có thể test bị mock quá nhiều, không chạy code thực tế
- Có thể test không cover các edge cases

#### `config` - **3% coverage** với **6 test files** ❌

**File test:**
- AiCacheConfigTest
- AiSyncConfigTest
- AiSyncPropertiesTest
- AsyncConfigTest
- RestTemplateConfigTest
- TestRateLimitingConfig

**Phân tích:**
- Config tests thường khó test vì phụ thuộc vào Spring context
- Có thể test không load đúng config classes

---

### ⚠️ **NHÓM 3: CÓ NHIỀU TEST FILE NHƯNG COVERAGE VẪN THẤP**

#### `service` - **31% coverage** với **33 test files** ⚠️

**File test:** 33 files (xem bảng trên)

**Phân tích:**
- Nhiều service có test nhưng coverage thấp
- Có thể test chỉ cover happy path, không cover error cases
- Có thể test mock quá nhiều dependencies

**👉 CẦN:**
- Kiểm tra từng service test
- Đảm bảo test cover cả success và error cases
- Kiểm tra test có chạy code thực tế hay chỉ mock

---

### 🆕 **NHÓM 4: KHÔNG CÓ TEST FILE** (CẦN TẠO)

Các package **KHÔNG CÓ file test** nào:

1. ❌ `dto.admin` - 0% coverage
2. ❌ `dto.customer` - 0% coverage
3. ❌ `validation` - 0% coverage
4. ❌ `mapper` - 0% coverage
5. ❌ `common.api` - 0% coverage
6. ❌ `common.util` - 0% coverage
7. ❌ `common.base` - 0% coverage
8. ❌ `common.constants` - 0% coverage

**👉 CẦN TẠO:**
- Test files cho các package này
- Ưu tiên: `validation`, `mapper`, `common.util`

---

## 🎯 KHUYẾN NGHỊ ƯU TIÊN

### 🔴 **Ưu tiên CAO** (Ngay lập tức):

1. **Kiểm tra tại sao test không chạy:**
   - `web.controller.admin` (2 test files → 0% coverage)
   - `web.controller.api` (3 test files → 0% coverage)
   - `web.controller.restaurantowner` (1 test file → 0% coverage)
   - `web.controller.customer` (1 test file → 0% coverage)

2. **Cải thiện test coverage:**
   - `web.controller` (10 test files → 2% coverage) ❌❌
   - `service` (33 test files → 31% coverage) ⚠️

### 🟠 **Ưu tiên TRUNG BÌNH:**

1. Tạo test cho các package không có test:
   - `validation`
   - `mapper`
   - `common.util`
   - `dto.admin`, `dto.customer`

### 🟢 **Ưu tiên THẤP:**

1. Cải thiện coverage cho các package đã gần đạt mục tiêu:
   - `scheduler` (76%)
   - `audit` (79%)

---

## 📝 LỆNH KIỂM TRA

```bash
# 1. Chạy tất cả test và xem coverage
mvn clean test jacoco:report

# 2. Xem coverage report
# Mở: target/site/jacoco/index.html

# 3. Chạy test riêng cho package có vấn đề
mvn test -Dtest=*ControllerTest
mvn test -Dtest=*ServiceTest

# 4. Xem test có chạy không
mvn test -Dtest=AdminModerationControllerTest -X

# 5. Kiểm tra test bị disabled
grep -r "@Disabled" src/test/java/
```

---

**Lưu ý:** Báo cáo này so sánh 79 file test sẽ chạy khi dùng `mvn test` với các package có coverage < 80% từ báo cáo JaCoCo.

