# 📊 PHÂN TÍCH COVERAGE: FILE TEST VS PACKAGE COVERAGE < 80%

## 🎯 Tổng quan

Báo cáo này so sánh **79 file test** sẽ chạy khi dùng `mvn test` với các **package có coverage < 80%** từ báo cáo JaCoCo.

---

## 📉 CÁC PACKAGE CÓ COVERAGE < 80% VÀ FILE TEST LIÊN QUAN

### 🔴 **0% Coverage** - Cần test khẩn cấp

#### 1. `com.example.booking.web.controller.admin` - **0% coverage**
**File test hiện có:**
- ✅ `web/controller/admin/AdminModerationControllerTest.java`
- ✅ `web/controller/admin/AdminVoucherAnalyticsControllerTest.java`

**⚠️ Phân tích:** Có 2 file test nhưng coverage = 0%, có thể:
- Test chưa cover đủ methods
- Test bị disabled/skip
- Test không chạy đúng cách

---

#### 2. `com.example.booking.web.controller.api` - **0% coverage**
**File test hiện có:**
- ✅ `web/controller/api/AIActionsControllerTest.java`
- ✅ `web/controller/api/BankAccountApiControllerTest.java`
- ✅ `web/controller/api/ChatApiControllerTest.java`

**⚠️ Phân tích:** Có 3 file test nhưng coverage = 0%

---

#### 3. `com.example.booking.web.controller.restaurantowner` - **0% coverage**
**File test hiện có:**
- ✅ `web/controller/RestaurantOwnerControllerTest.java`

**⚠️ Phân tích:** Có file test nhưng coverage = 0%

---

#### 4. `com.example.booking.web.controller.customer` - **0% coverage**
**File test hiện có:**
- ✅ `web/controller/customer/FavoriteControllerTest.java`

**⚠️ Phân tích:** Có file test nhưng coverage = 0%

---

#### 5. `com.example.booking.websocket` - **0% coverage**
**File test hiện có:**
- ✅ `websocket/ChatMessageControllerTest.java`

**⚠️ Phân tích:** Có file test nhưng coverage = 0%

---

#### 6. `com.example.booking.aspect` - **0% coverage**
**File test hiện có:**
- ✅ `aspect/AuditAspectTest.java`

**⚠️ Phân tích:** Có file test nhưng coverage = 0%

---

#### 7. `com.example.booking.dto.admin` - **0% coverage**
**File test hiện có:**
- ❌ **KHÔNG CÓ FILE TEST**

**⚠️ Cần tạo test cho package này**

---

#### 8. `com.example.booking.dto.customer` - **0% coverage**
**File test hiện có:**
- ❌ **KHÔNG CÓ FILE TEST**

**⚠️ Cần tạo test cho package này**

---

#### 9. `com.example.booking.dto.notification` - **29% coverage**
**File test hiện có:**
- ❌ **KHÔNG CÓ FILE TEST RIÊNG**

**⚠️ Có thể được test gián tiếp qua các test khác**

---

#### 10. `com.example.booking.test` - **0% coverage**
**File test hiện có:**
- ✅ `test/simple/AssertJDemoTest.java`
- ✅ `test/simple/BookingControllerSimpleTest.java`
- ✅ `test/simple/SimpleBookingTest.java`
- ✅ `test/util/MockDataFactoryTest.java`

**⚠️ Phân tích:** Đây là package test utility, coverage thấp là bình thường

---

#### 11. `com.example.booking.validation` - **0% coverage**
**File test hiện có:**
- ❌ **KHÔNG CÓ FILE TEST**

**⚠️ Cần tạo test cho package này**

---

#### 12. `com.example.booking.mapper` - **0% coverage**
**File test hiện có:**
- ❌ **KHÔNG CÓ FILE TEST**

**⚠️ Cần tạo test cho package này**

---

#### 13. `com.example.booking.common.api` - **0% coverage**
**File test hiện có:**
- ❌ **KHÔNG CÓ FILE TEST**

**⚠️ Cần tạo test cho package này**

---

#### 14. `com.example.booking.common.util` - **0% coverage**
**File test hiện có:**
- ❌ **KHÔNG CÓ FILE TEST**

**⚠️ Cần tạo test cho package này**

---

#### 15. `com.example.booking.common.base` - **0% coverage**
**File test hiện có:**
- ❌ **KHÔNG CÓ FILE TEST**

**⚠️ Cần tạo test cho package này**

---

#### 16. `com.example.booking.common.constants` - **0% coverage**
**File test hiện có:**
- ❌ **KHÔNG CÓ FILE TEST**

**⚠️ Cần tạo test cho package này**

---

### 🟠 **Low Coverage (1-30%)** - Cần cải thiện

#### 17. `com.example.booking.web.controller` - **2% coverage**
**File test hiện có:**
- ✅ `web/controller/AdminDashboardControllerTest.java`
- ✅ `web/controller/AdminRestaurantControllerTest.java`
- ✅ `web/controller/AISearchControllerTest.java`
- ✅ `web/controller/BookingControllerTest.java`
- ✅ `web/controller/BookingControllerWebMvcTest.java`
- ✅ `web/controller/GlobalExceptionHandlerTest.java`
- ✅ `web/controller/HomeControllerTest.java`
- ✅ `web/controller/PaymentControllerWebMvcTest.java`
- ✅ `web/controller/RestaurantOwnerControllerTest.java`
- ✅ `web/controller/RestaurantRegistrationControllerTest.java`

**⚠️ Phân tích:** Có 10 file test nhưng coverage chỉ 2% - **RẤT NGHIÊM TRỌNG**

---

#### 18. `com.example.booking.config` - **3% coverage**
**File test hiện có:**
- ✅ `config/AiCacheConfigTest.java`
- ✅ `config/AiSyncConfigTest.java`
- ✅ `config/AiSyncPropertiesTest.java`
- ✅ `config/AsyncConfigTest.java`
- ✅ `config/RestTemplateConfigTest.java`
- ✅ `config/TestRateLimitingConfig.java`

**⚠️ Phân tích:** Có 6 file test nhưng coverage chỉ 3%

---

#### 19. `com.example.booking.dto` - **11% coverage**
**File test hiện có:**
- ✅ `dto/AIActionRequestTest.java`
- ✅ `dto/AIActionResponseTest.java`
- ✅ `dto/DtosTest.java`

**⚠️ Phân tích:** Chỉ có 3 file test cho toàn bộ DTO package

---

#### 20. `com.example.booking.service.impl` - **16% coverage**
**File test hiện có:**
- ✅ `service/impl/NotificationServiceImplTest.java`

**⚠️ Phân tích:** Chỉ có 1 file test cho service.impl package

---

#### 21. `com.example.booking.dto.ai` - **19% coverage**
**File test hiện có:**
- ❌ **KHÔNG CÓ FILE TEST RIÊNG**

**⚠️ Có thể được test gián tiếp qua AI service tests**

---

### 🟡 **Medium Coverage (30-79%)** - Cần cải thiện

#### 22. `com.example.booking.service` - **31% coverage**
**File test hiện có:**
- ✅ 33 file test trong `service/` package

**⚠️ Phân tích:** Có nhiều file test nhưng coverage chỉ 31% - cần kiểm tra:
- Test có chạy đúng không?
- Test có cover đủ methods không?
- Có methods nào bị skip không?

---

#### 23. `com.example.booking.util` - **32% coverage**
**File test hiện có:**
- ✅ `util/InputSanitizerTest.java`

**⚠️ Phân tích:** Chỉ có 1 file test cho util package

---

#### 24. `com.example.booking.domain` - **44% coverage**
**File test hiện có:**
- ✅ `domain/CommunicationHistoryTest.java`
- ✅ `domain/InternalNoteTest.java`
- ✅ `domain/PopularRestaurantDtoTest.java`
- ✅ `domain/RestaurantOwnerTest.java`
- ✅ `domain/converter/DishStatusConverterTest.java`
- ✅ `domain/converter/ServiceStatusConverterTest.java`
- ✅ `domain/converter/TableStatusConverterTest.java`

**⚠️ Phân tích:** Có 7 file test nhưng coverage 44%

---

#### 25. `com.example.booking.exception` - **56% coverage**
**File test hiện có:**
- ✅ `web/controller/GlobalExceptionHandlerTest.java`

**⚠️ Phân tích:** Có test cho exception handler nhưng có thể thiếu test cho exception classes

---

#### 26. `com.example.booking.dto.vietqr` - **70% coverage**
**File test hiện có:**
- ❌ **KHÔNG CÓ FILE TEST RIÊNG**

**⚠️ Có thể được test qua VietQRServiceTest**

---

## 📋 TÓM TẮT

### ✅ Package có test file NHƯNG coverage vẫn thấp (< 80%)
1. `web.controller.admin` - 2 test files → 0% coverage ❌
2. `web.controller.api` - 3 test files → 0% coverage ❌
3. `web.controller.restaurantowner` - 1 test file → 0% coverage ❌
4. `web.controller.customer` - 1 test file → 0% coverage ❌
5. `web.controller` - 10 test files → 2% coverage ❌❌
6. `config` - 6 test files → 3% coverage ❌
7. `websocket` - 1 test file → 0% coverage ❌
8. `aspect` - 1 test file → 0% coverage ❌
9. `service` - 33 test files → 31% coverage ⚠️
10. `domain` - 7 test files → 44% coverage ⚠️
11. `util` - 1 test file → 32% coverage ⚠️

### ❌ Package KHÔNG CÓ test file
1. `dto.admin` - 0% coverage
2. `dto.customer` - 0% coverage
3. `validation` - 0% coverage
4. `mapper` - 0% coverage
5. `common.api` - 0% coverage
6. `common.util` - 0% coverage
7. `common.base` - 0% coverage
8. `common.constants` - 0% coverage

## 🎯 KHUYẾN NGHỊ

### Ưu tiên cao (Coverage = 0% và có file test):
1. **Kiểm tra tại sao test không chạy:**
   - Xem test có bị `@Disabled` không?
   - Test có compile được không?
   - Test có bị skip trong Maven không?

2. **Cải thiện test coverage:**
   - `web.controller.admin` - 2 test files nhưng 0% coverage
   - `web.controller.api` - 3 test files nhưng 0% coverage
   - `web.controller` - 10 test files nhưng chỉ 2% coverage

### Ưu tiên trung bình (Không có test file):
1. Tạo test cho các package:
   - `validation`
   - `mapper`
   - `common.*` packages
   - `dto.admin`, `dto.customer`

### Kiểm tra ngay:
```bash
# Chạy test và xem coverage
mvn test jacoco:report

# Xem report
# target/site/jacoco/index.html
```



