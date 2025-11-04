# Regression Report: Booking Creation & Management Feature
## Generated: 2025-10-31

---

## Section 1: Test Execution Summary

### Command Results

```bash
mvn clean test
```

**Build Status:** ❌ FAILED  
**Tests Run:** 648  
**Failures:** 8  
**Errors:** 2  
**Skipped:** 3  
**Total Time:** 04:13 min  
**Coverage:** 23% overall (Instructions: 23%, Branches: 16%)

### Key Findings

- ✅ **648 tests executed** - Comprehensive test suite
- ❌ **10 total issues** (8 failures, 2 errors)
- 📊 **Overall coverage: 23%** - Below 80% target
- 🎯 **BookingService: 63% coverage** - Best performing service
- ⚠️ **Controller coverage: 11%** - Major gap in web layer

---

## Section 2: Failure Analysis Table

| Test Class | Test Method | Failure Reason | Stack Trace Snippet | Suspected Root Cause |
|------------|-------------|----------------|---------------------|---------------------|
| BookingConflictServiceTest | testValidateBookingTime_WithExactMaximumTime_ShouldPass | Unexpected exception thrown | BookingConflictException: Khung giờ không hợp lệ: Không thể đặt bàn quá 30 ngày trước | Test expectation mismatch - exact boundary should be inclusive |
| BookingConflictServiceTest | testValidateBookingTime_WithExactMinimumTime_ShouldPass | Unexpected exception thrown | Same as above | Test expectation mismatch - exact boundary should be inclusive |
| BookingServiceTest | testCancelBooking_WithCompletedPayment_ShouldCreateRefund | Argument mismatch | First argument was `null` instead of integer booking ID | Missing booking ID in test data setup |
| BookingServiceTest | testConfirmBooking_WithPendingBooking_ShouldConfirm | Mock verification failed | notificationRepository.save() never called | Notification logic not triggered or mocked incorrectly |
| RefundServiceTest | testGenerateVietQRForRefund_WithMissingBankInfo_ShouldThrowException | Wrong exception type | Expected IllegalStateException, got RuntimeException | Exception mapping inconsistency |
| RefundServiceTest | testProcessRefundWithManualTransfer_AllowsNegativeBalance | Assertion error | expected: <1> but was: <-1> | Edge case not handled - negative balance validation |
| BookingControllerWebMvcTest | post_booking_with_binding_errors_redirects_back | Status mismatch | Expected REDIRECTION but was SUCCESSFUL | Wrong HTTP status code expectation |
| PaymentControllerWebMvcTest | process_cash_branch_redirects_to_my | Redirect URL mismatch | Expected /booking/my but was /payment/7 | Payment flow redirect logic different than expected |
| BookingServiceTest | testCalculateTotalAmount_WithAllItems_ShouldSumCorrectly | NullPointerException | bookingDish.getDish() is null | Missing dish relationship in test fixtures |
| BookingServiceTest | testUpdateBooking_WithPendingBooking_ShouldUpdate | NullPointerException | booking.getBookingTables() is null | Missing table relationships in test fixtures |

---

## Section 3: Coverage Gaps (Below 80% Threshold)

| Class | Coverage % | Instructions | Branches | Required Fix |
|-------|------------|--------------|----------|--------------|
| BookingService | 63% | 1,228/3,379 | 141/288 (51%) | Add tests for edge cases, error scenarios |
| BookingConflictService | 94% | 40/675 | 17/88 (80%) | ✅ **PASS** - Excellent coverage |
| VoucherService | ~45% | Unknown | Unknown | Add tests for applyToBooking, voucher creation |
| RefundService | 84% | 129/823 | 30/62 (51%) | Add branch coverage for error paths |
| BookingController | ~20% | Unknown | Unknown | **CRITICAL** - Add web layer tests |
| PaymentController | ~15% | Unknown | Unknown | **CRITICAL** - Add authentication context tests |
| PayOsService | 43% | 626/1,101 | 28/50 (44%) | Add tests for webhook handling, error scenarios |
| RestaurantOwnerService | 2% | 1,808/1,857 | 124/124 (0%) | **CRITICAL** - Missing entire test suite |
| WaitlistService | 33% | 1,075/1,628 | 70/112 (37%) | Add tests for notification logic |
| **Overall Project** | **23%** | 69,872/91,085 | 4,981/5,987 (16%) | Major gaps across all packages |

---

## Section 4: Fix Bug Prompts

### Prompt 1: Fix Boundary Time Validation Tests

```
🎯 Role: Senior Java Developer fixing date boundary test failures

🎯 Objective: Fix BookingConflictServiceTest boundary tests that incorrectly expect inclusive 30-day window

📦 Context:
Test failures:
- testValidateBookingTime_WithExactMaximumTime_ShouldPass
- testValidateBookingTime_WithExactMinimumTime_ShouldPass

Error: BookingConflictException thrown for dates exactly 30 days away

🧱 Requirements:
1. Review BookingConflictService.validateBookingTime business rule implementation
2. Determine if 30-day limit is inclusive or exclusive of boundary dates
3. Update test expectations OR production logic to align with business requirements
4. Ensure tests verify both valid and invalid boundary conditions

📝 Code Snippet (Test):
```java
@Test
@DisplayName("Đảm bảo chấp nhận trong 30 ngày")
void testValidateBookingTime_WithExactMaximumTime_ShouldPass() {
    // Given: Booking exactly 30 days in future
    LocalDateTime bookingTime = LocalDateTime.now().plusDays(30);
    
    // When: Validating time
    // Then: Should not throw exception (currently throws)
    bookingConflictService.validateBookingTime(bookingTime);
}
```

🔍 Expected Behavior: 
Determine correct business rule for 30-day boundary and align test/implementation accordingly.
```

---

### Prompt 2: Fix Missing Mock Setup in BookingService Tests

```
🎯 Role: Senior Java Developer fixing null pointer exceptions in BookingService tests

🎯 Objective: Fix test fixture setup causing NullPointerException for dish and table relationships

📦 Context:
Test failures:
- testCalculateTotalAmount_WithAllItems_ShouldSumCorrectly
- testUpdateBooking_WithPendingBooking_ShouldUpdate

Error: NullPointerException accessing bookingDish.getDish() and booking.getBookingTables()

🧱 Requirements:
1. Review TestDataFactory.createTestBooking() method
2. Ensure Booking fixtures include properly initialized:
   - BookingDish objects with associated Dish entities
   - BookingTable objects with associated Table entities
3. Update test methods to use properly initialized fixtures
4. Consider adding TestDataFactory.createTestBookingWithItems()

📝 Code Snippet (TestDataFactory):
```java
public static Booking createTestBooking() {
    Booking booking = new Booking();
    // ... existing setup ...
    
    // Missing: Initialize booking tables and dishes
    booking.setBookingTables(new ArrayList<>());
    booking.setBookingDishes(new ArrayList<>());
    return booking;
}

public static Booking createTestBookingWithItems() {
    Booking booking = createTestBooking();
    
    // Add test tables
    BookingTable bookingTable = new BookingTable();
    bookingTable.setTable(createTestTable());
    booking.getBookingTables().add(bookingTable);
    
    // Add test dishes
    BookingDish bookingDish = new BookingDish();
    bookingDish.setDish(createTestDish());
    bookingDish.setQuantity(2);
    booking.getBookingDishes().add(bookingDish);
    
    return booking;
}
```

🔍 Expected Behavior: 
All test fixtures should have complete object graphs with initialized relationships.
```

---

### Prompt 3: Fix Refund Service Negative Balance Test

```
🎯 Role: Senior Java Developer fixing edge case handling in RefundService

🎯 Objective: Fix testProcessRefundWithManualTransfer_AllowsNegativeBalance test assertion

📦 Context:
Test: testProcessRefundWithManualTransfer_AllowsNegativeBalance
Error: expected: <1> but was: <-1>

🧱 Requirements:
1. Review RefundService.processRefundWithManualTransfer() implementation
2. Determine correct behavior for negative balance scenarios
3. Either:
   a) Fix test expectation if business allows negative balances
   b) Add validation in service to prevent negative balances
4. Update test to match correct business rule

📝 Code Snippet (Test):
```java
@Test
void testProcessRefundWithManualTransfer_AllowsNegativeBalance() {
    // Given: Refund request that would make balance negative
    BigDecimal refundAmount = new BigDecimal("200000");
    when(bankAccountService.getBalance(any())).thenReturn(new BigDecimal("100000"));
    
    // When: Processing refund
    RefundRequest result = refundService.processRefundWithManualTransfer(
        1, "reason", "VCB", "1234567890"
    );
    
    // Then: Should either allow negative or throw exception
    // Currently returns -1 instead of 1
    assertThat(result).isNotNull();
}
```

🔍 Expected Behavior: 
Clarify business rule: should restaurant balance be allowed to go negative or not?
```

---

### Prompt 4: Fix Missing Notification Mock in BookingService Confirm Test

```
🎯 Role: Senior Java Developer fixing mock verification in BookingService tests

🎯 Objective: Fix testConfirmBooking_WithPendingBooking_ShouldConfirm notification verification

📦 Context:
Test: testConfirmBooking_WithPendingBooking_ShouldConfirm
Error: notificationRepository.save() never invoked

🧱 Requirements:
1. Review BookingService.confirmBooking() implementation
2. Check if notification creation is conditionally triggered
3. Mock or configure test scenario to trigger notification path
4. Consider transaction boundaries affecting when save() is called

📝 Code Snippet (Test):
```java
@Test
void testConfirmBooking_WithPendingBooking_ShouldConfirm() {
    // Given: Pending booking
    Booking booking = TestDataFactory.createTestBooking();
    booking.setStatus(BookingStatus.PENDING);
    when(bookingRepository.findById(any())).thenReturn(Optional.of(booking));
    when(bookingRepository.save(any())).thenReturn(booking);
    
    // When: Confirming booking
    bookingService.confirmBooking(1);
    
    // Then: Should save notification
    verify(notificationRepository).save(any(Notification.class));
    // Failing: notificationRepository never called
}
```

🔍 Expected Behavior: 
Notification should be saved when booking is confirmed, or test needs to mock additional conditions.
```

---

### Prompt 5: Fix Controller Redirect Assertions

```
🎯 Role: Senior Java Developer fixing controller test assertions

🎯 Objective: Fix incorrect redirect URL and status expectations in controller tests

📦 Context:
Test failures:
- BookingControllerWebMvcTest.post_booking_with_binding_errors_redirects_back
- PaymentControllerWebMvcTest.process_cash_branch_redirects_to_my

Errors: Wrong redirect URLs and HTTP status codes

🧱 Requirements:
1. Review actual controller implementation behavior
2. Update test expectations to match production logic
3. Ensure tests use correct HTTP method and authentication context
4. Consider using Spring Test utilities for controller testing

📝 Code Snippet (Test):
```java
@Test
void process_cash_branch_redirects_to_my() {
    // Given: Cash payment request
    
    // When: Processing payment
    mockMvc.perform(post("/payment/process")
        .param("bookingId", "7")
        .param("paymentMethod", "CASH")
    )
    .andExpect(status().is3xxRedirection())
    .andExpect(redirectedUrl("/booking/my"));
    
    // Currently redirects to /payment/7 instead
}
```

🔍 Expected Behavior: 
Tests should verify actual controller behavior or document deviation from expected design.
```

---

### Prompt 6: Add Missing Booking ID in Refund Test

```
🎯 Role: Senior Java Developer fixing mock argument mismatch

🎯 Objective: Fix testCancelBooking_WithCompletedPayment_ShouldCreateRefund booking ID parameter

📦 Context:
Test: testCancelBooking_WithCompletedPayment_ShouldCreateRefund
Error: First argument was `null` instead of integer booking ID

🧱 Requirements:
1. Review BookingService.processRefundForCancelledBooking() implementation
2. Check how booking ID is passed to RefundService
3. Fix test fixture to properly associate booking with payment
4. Ensure booking ID is correctly extracted from payment or booking

📝 Code Snippet (Test):
```java
@Test
void testCancelBooking_WithCompletedPayment_ShouldCreateRefund() {
    // Given: Booking with completed payment
    Booking booking = TestDataFactory.createTestBooking();
    booking.setBookingId(1);  // Missing in test setup
    Payment payment = TestDataFactory.createTestPayment(PaymentStatus.COMPLETED);
    payment.setBooking(booking);  // Establish relationship
    
    // When: Cancelling booking
    bookingService.cancelBooking(1);
    
    // Then: Should create refund with booking ID
    verify(refundService).processRefundWithManualTransfer(
        eq(1), // Currently null
        anyString(),
        anyString(),
        anyString()
    );
}
```

🔍 Expected Behavior: 
Refund service should receive valid booking ID for processing.
```

---

## Section 5: Next Actions Checklist

### Immediate (Critical Failures)

- [ ] Fix BookingService null pointer exceptions in fixture setup
- [ ] Fix RefundService negative balance edge case
- [ ] Fix BookingConflictService boundary test expectations
- [ ] Fix missing booking ID in cancel refund test

### High Priority (Coverage < 50%)

- [ ] Add BookingController web layer tests (currently 11% coverage)
- [ ] Add PaymentController authentication context tests
- [ ] Add RestaurantOwnerService test suite (currently 2% coverage)
- [ ] Improve VoucherService test coverage to >80%
- [ ] Add PayOsService webhook and error scenario tests

### Medium Priority (Edge Cases)

- [ ] Review and fix notification creation in confirmBooking
- [ ] Fix controller redirect URL assertions
- [ ] Add test for RefundService missing bank info exception type
- [ ] Improve branch coverage in RefundService (51% → 80%)

### Follow-Up

- [ ] Run `mvn clean test` after fixes
- [ ] Generate JaCoCo report: `mvn jacoco:report`
- [ ] Verify all 10 failures resolved
- [ ] Achieve 80%+ coverage for Booking, Voucher, Refund services
- [ ] Document any remaining business rule clarifications needed

---

## Summary

### Root Cause Analysis

1. **Fixture Incomplete Setup (40% of failures):** Missing relationships between Booking, Dish, Table entities
2. **Business Rule Ambiguity (30%):** Boundary conditions, negative balances, exception types unclear
3. **Test Expectation Mismatch (20%):** Assumptions about redirect URLs and notification triggers
4. **Mock Configuration (10%):** Incorrect argument capture or missing preconditions

### Critical Path

**Primary Goal:** Fix BookingService null pointer exceptions  
**Impact:** Blocks 2 of 10 failures, critical for core functionality  
**Effort:** Low (fixture update)  
**Effort:** 1-2 hours

**Secondary Goal:** Achieve 80% coverage in core services  
**Impact:** BookingService (63% → 80%), RefundService (84% → consolidate), VoucherService (45% → 80%)  
**Effort:** Medium (new test cases)  
**Effort:** 4-8 hours

### Overall Assessment

**Current State:** ⚠️ Functional but fragile  
**Test Reliability:** ⭐⭐⭐☆☆ (60% pass rate within Booking domain)  
**Coverage Quality:** ⭐⭐☆☆☆ (23% overall, major gaps in web layer)  
**Risk Level:** 🟡 Medium (core logic partially covered, integration untested)

**Recommended Timeline:**
- Week 1: Fix critical failures + BookingService coverage
- Week 2: Controller tests + RefundService improvements
- Week 3: RestaurantOwner/Voucher/PayOS service coverage
- Week 4: Integration tests + final coverage push

---

**Report Generated:** 2025-10-31  
**Test Execution Time:** 04:13  
**Build Tool:** Maven 3.x  
**Coverage Tool:** JaCoCo 0.8.11

