# Coverage Analysis Report - BookEAT Restaurant Booking Platform

**Total Project Coverage: 29%** (91,120 instructions total, 64,229 missed, 26,891 covered)

---

## 📊 HIGH PRIORITY - Modules giúp tăng coverage NHIỀU NHẤT

### 1. 🥇 **com.example.booking.web.controller.admin** (3% → Target: 70%+) 
**Current: 3% (123 covered, 3,676 missed)**  
**Impact: Tăng ~5% coverage tổng thể**  
**Classes cần test:**

- ✅ AdminVoucherController (0%) - **TESTS FAILED - CẦN FIX**
- ✅ AdminFavoriteController (1%) - **TESTS FAILED - CẦN FIX** 
- AdminNotificationController (1%)
- AdminVoucherAnalyticsController (4%)
- WorkingRateLimitingController (0%) - 1,482 missed instructions
- RateLimitingAdminController (0%)
- SimpleRateLimitingController (0%)
- UltraSimpleController (0%)

**Recommendation:** Ưu tiên fix tests thất bại + thêm tests cho WorkingRateLimitingController (class lớn nhất)

---

### 2. 🥈 **com.example.booking.service.impl** (16% → Target: 70%+)
**Current: 16% (583 covered, 2,894 missed)**  
**Impact: Tăng ~4% coverage tổng thể**  
**Classes cần test:**

- ✅ NotificationServiceImpl (44%) - **HAS TESTS**
- VoucherServiceImpl (28%) - **HAS TESTS**
- ReviewReportServiceImpl (2%) - 699 missed instructions
- FavoriteServiceImpl (0%) - **1,082 MISSED INSTRUCTIONS** ⚠️ **ƯU TIÊN CAO**

**Recommendation:** Tạo FavoriteServiceImplTest và ReviewReportServiceImplTest

---

### 3. 🥉 **com.example.booking.web.controller.restaurantowner** (3% → Target: 70%+)
**Current: 3% (41 covered, 1,320 missed)**  
**Impact: Tăng ~3% coverage tổng thể**  
**Classes cần test:**

- ✅ RestaurantVoucherController (0%) - **TESTS FAILED - CẦN FIX**
- RestaurantFavoriteController (11%)

**Recommendation:** Fix RestaurantVoucherControllerTest thất bại

---

### 4. 🏅 **com.example.booking.dto.admin** (2% → Target: 50%+)
**Current: 2% (24 covered, 796 missed)**  
**Impact: Tăng ~2% coverage tổng thể**  
**Classes cần test:**

- UserEditForm (0%)
- WithdrawalStatsDto (0%)
- VoucherCreateForm (0%)
- VoucherEditForm (0%)
- RestaurantBalanceInfoDto (0%)
- AuditLogDto (0%)
- UserCreateForm (0%)
- CommissionSettingsDto (0%)
- VoucherAssignForm (0%)
- ✅ FavoriteStatisticsDto (31%)

**Recommendation:** Thêm tests cho VoucherCreateForm và VoucherEditForm (được dùng nhiều)

---

## 📈 MEDIUM PRIORITY - Tăng coverage vừa phải

### 5. com.example.booking.web.controller.api (8% → Target: 50%+)
**Current: 8% (334 covered, 3,397 missed)**  
**Impact: Tăng ~2% coverage tổng thể**

- BookingApiController (0%)
- ChatApiController (0%)
- VoucherApiController (0%)
- RestaurantWithdrawalApiController (6%)
- ✅ SmartWaitlistApiController (52%) - **HAS TESTS**
- ✅ BankAccountApiController (23%) - **HAS TESTS**
- ✅ AIActionsController (43%) - **HAS TESTS**

**Recommendation:** Bổ sung tests cho BookingApiController, ChatApiController, VoucherApiController

---

### 6. com.example.booking.dto (15% → Target: 40%+)
**Current: 15% (523 covered, 2,889 missed)**  
**Impact: Tăng ~1% coverage tổng thể**

**Recommendation:** Thường không cần test DTOs (acceptors/getters), nhưng có thể test validation logic

---

### 7. com.example.booking.common.util (0% → Target: 70%+)
**Current: 0% (0 covered, 6 missed)**  
**Classes:**

- DateTimeUtil (0%) - **EMPTY CLASS**
- SecurityUtils (0%) - **EMPTY CLASS**

**Recommendation:** Đây là empty classes, không cần test

---

## ✅ COMPLETED - Modules đã có coverage tốt

### ✅ com.example.booking.validation (100%)
**FuturePlusValidator** - 100% coverage

### ✅ com.example.booking.util (75%)
- GeoUtils - ✅ HAS TESTS
- InputSanitizer - ✅ HAS TESTS  
- PayOSSignatureGenerator - ✅ HAS TESTS

### ✅ com.example.booking.aspect (53%)
- AuditAspect (50%) - ✅ HAS TESTS
- RateLimitingAspect (79%) - ✅ HAS TESTS

### ✅ com.example.booking.service (32%)
**Most service classes have good coverage**

### ✅ com.example.booking.config (46%)
**Most config classes have tests**

---

## 🎯 SUMMARY - Thứ tự thực hiện

### PHASE 1: Fix Tests Thất Bại (Ưu tiên cao nhất)
1. ✅ Fix AdminVoucherControllerTest (18 test failures)
2. ✅ Fix AdminFavoriteControllerTest (7 test failures)
3. ✅ Fix RestaurantVoucherControllerTest (15 test failures)

### PHASE 2: High Impact Tests
4. ⬜ FavoriteServiceImplTest (0% → 70%+) - **1,082 missed instructions**
5. ⬜ ReviewReportServiceImplTest (2% → 70%+)
6. ⬜ WorkingRateLimitingControllerTest (0%)

### PHASE 3: Medium Impact Tests
7. ⬜ AdminNotificationControllerTest
8. ⬜ AdminVoucherAnalyticsControllerTest
9. ⬜ RestaurantFavoriteControllerTest (thêm tests)
10. ⬜ BookingApiControllerTest
11. ⬜ ChatApiControllerTest
12. ⬜ VoucherApiControllerTest

### PHASE 4: DTOs & Low Priority
13. ⬜ VoucherCreateFormTest
14. ⬜ VoucherEditFormTest
15. ⬜ DTO tests khác (optional)

---

## 📌 Notes

1. **Tests hiện tại bị lỗi do context loading issues** - Cần fix trước khi chạy coverage mới
2. **FavoriteServiceImpl và ReviewReportServiceImpl** là service core lớn chưa có tests
3. **WorkingRateLimitingController** là controller lớn nhất trong admin package (1,482 instructions)
4. **Total project coverage hiện tại: 29%** (target: 50%+)
5. **Priority calculation:** Dựa trên missed instructions × tỉ lệ code trong tổng project

---

## 🚀 Expected Coverage After Fixes

| Module | Current | Target | Expected Gain |
|--------|---------|--------|---------------|
| web.controller.admin | 3% | 50% | +4% |
| service.impl | 16% | 60% | +3% |
| web.controller.restaurantowner | 3% | 50% | +2% |
| dto.admin | 2% | 40% | +1% |
| **TOTAL** | **29%** | **~39%** | **+10%** |



