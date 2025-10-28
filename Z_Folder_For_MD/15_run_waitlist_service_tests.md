# WaitlistService Test - Command Reference

## Test Coverage

### Methods Tested: 5/5 (100%)
1. ✅ `addToWaitlist()` - 10 tests
2. ✅ `convertWaitlistToBooking()` (confirmWaitlistToBooking) - 12 tests
3. ✅ `getWaitlistByCustomer()` - 5 tests
4. ✅ `calculateEstimatedWaitTime()` - 7 tests
5. ✅ `removeFromWaitlist()` (cancelWaitlist) - 6 tests

**Total: 40 test cases**

---

## Commands

### Run All WaitlistService Tests

**PowerShell (Windows):**
```powershell
mvn test "-Dtest=WaitlistServiceTest" "-Dspring.profiles.active=test"
```


**CMD or Bash:**
```bash
mvn test -Dtest=WaitlistServiceTest -Dspring.profiles.active=test
```

### Run Specific Test Method

**PowerShell (Windows):**
```powershell
# Test addToWaitlist()
mvn test "-Dtest=WaitlistServiceTest`$AddToWaitlistTests" "-Dspring.profiles.active=test"

# Test convertWaitlistToBooking()
mvn test "-Dtest=WaitlistServiceTest`$ConvertWaitlistToBookingTests" "-Dspring.profiles.active=test"

# Test getWaitlistByCustomer()
mvn test "-Dtest=WaitlistServiceTest`$GetWaitlistByCustomerTests" "-Dspring.profiles.active=test"

# Test calculateEstimatedWaitTime()
mvn test "-Dtest=WaitlistServiceTest`$CalculateEstimatedWaitTimeTests" "-Dspring.profiles.active=test"

# Test removeFromWaitlist()
mvn test "-Dtest=WaitlistServiceTest`$RemoveFromWaitlistTests" "-Dspring.profiles.active=test"
```

**CMD or Bash:**
```bash
# Test addToWaitlist()
mvn test -Dtest=WaitlistServiceTest$AddToWaitlistTests -Dspring.profiles.active=test

# Test convertWaitlistToBooking()
mvn test -Dtest=WaitlistServiceTest$ConvertWaitlistToBookingTests -Dspring.profiles.active=test

# Test getWaitlistByCustomer()
mvn test -Dtest=WaitlistServiceTest$GetWaitlistByCustomerTests -Dspring.profiles.active=test

# Test calculateEstimatedWaitTime()
mvn test -Dtest=WaitlistServiceTest$CalculateEstimatedWaitTimeTests -Dspring.profiles.active=test

# Test removeFromWaitlist()
mvn test -Dtest=WaitlistServiceTest$RemoveFromWaitlistTests -Dspring.profiles.active=test
```

### Run with Coverage Report

**PowerShell (Windows):**
```powershell
mvn clean test jacoco:report "-Dtest=WaitlistServiceTest" "-Dspring.profiles.active=test"
```

**CMD or Bash:**
```bash
mvn clean test jacoco:report -Dtest=WaitlistServiceTest -Dspring.profiles.active=test
```

### Using Batch Script (Windows) - RECOMMENDED

```bash
run_waitlist_service_tests.bat
```

> **Note**: Sử dụng batch script để tránh lỗi parsing trong PowerShell!

---

## Test Categories

### 1. addToWaitlist() - 10 Tests

| Test # | Category | Description |
|--------|----------|-------------|
| 1 | Happy Path | With Valid Data, Should Create Waitlist |
| 2 | Happy Path | With Minimum Party Size, Should Succeed |
| 3 | Happy Path | With Max Waitlist Size (6), Should Succeed |
| 4 | Business Logic | Should Calculate Queue Position |
| 5 | Business Logic | 5th Entry, Should Add to Correct Position |
| 6 | Validation | Above Waitlist Limit (>6), Should Throw Exception |
| 7 | Validation | Party Size = 0, Should Throw Exception |
| 8 | Validation | Party Size = 21, Should Throw Exception |
| 9 | Validation | Customer Already In Waitlist, Should Throw Exception |
| 10 | Integration | Should Set Join Time Automatically |

### 2. convertWaitlistToBooking() (confirmWaitlistToBooking) - 12 Tests

| Test # | Category | Description |
|--------|----------|-------------|
| 11 | Happy Path | With Valid Waitlist, Should Convert To Booking |
| 12 | Happy Path | Should Copy Dishes From Waitlist |
| 13 | Happy Path | Should Copy Services From Waitlist |
| 14 | Happy Path | Should Copy Tables From Waitlist |
| 15 | Business Logic | Should Calculate Total Amount |
| 16 | Business Logic | Should Update Waitlist Status To SEATED |
| 17 | Validation | With Non-WAITING Status, Should Throw Exception |
| 18 | Validation | With Wrong Restaurant ID, Should Throw Exception |
| 19 | Validation | Booking Time In Past, Should Throw Exception |
| 20 | Validation | Should Validate Booking Conflicts |
| 21 | Error Handling | Invalid Dish ID, Should Ignore Gracefully |
| 22 | Integration | Should Create Booking With Correct Fields |

### 3. getWaitlistByCustomer() - 5 Tests

| Test # | Category | Description |
|--------|----------|-------------|
| 23 | Happy Path | Customer With 3 Entries, Should Return All |
| 24 | Happy Path | Should Order By Join Time DESC |
| 25 | Business Logic | Customer With No Entries, Should Return Empty List |
| 26 | Business Logic | Should Include All Statuses |
| 27 | Integration | Each Entry Should Have Customer Relationship Loaded |

### 4. calculateEstimatedWaitTime() - 7 Tests

| Test # | Category | Description |
|--------|----------|-------------|
| 28 | Happy Path | Waitlist In Position 3, Should Calculate Minutes |
| 29 | Happy Path | First In Queue, Should Return 30 Minutes |
| 30 | Business Logic | Different Restaurant Queues, Should Calculate Based On Restaurant |
| 31 | Business Logic | High Value (Position 10), Should Return Correct Time |
| 32 | Edge Case | Only 1 Waitlist Entry For Restaurant, Should Return Minimum Wait Time |
| 33 | Error Handling | No Current Waitlist ID, Should Throw Exception |
| 34 | Integration | Waitlist Estimated Time Updated To Waitlist |

### 5. removeFromWaitlist() (cancelWaitlist) - 6 Tests

| Test # | Category | Description |
|--------|----------|-------------|
| 35 | Happy Path | With Valid Waitlist ID, Should Cancel Successfully |
| 36 | Happy Path | By Correct Customer, Should Cancel |
| 37 | Business Logic | Should Not Delete Record, Only Update Status |
| 38 | Validation | Another Customer Tries To Cancel, Should Throw Exception |
| 39 | Validation | With Non-WAITING Status, Should Throw Exception |
| 40 | Error Handling | Non-Existent ID, Should Throw Exception |

---

## Expected Results

- **Total Tests**: 40
- **Expected Pass Rate**: >90%
- **Target Pass Rate**: 100%

---

## Files

- **Test File**: `src/test/java/com/example/booking/service/WaitlistServiceTest.java`
- **Service File**: `src/main/java/com/example/booking/service/WaitlistService.java`
- **Batch Script**: `run_waitlist_service_tests.bat`

---

**Date:** 28/10/2025  
**Status:** Ready for Testing

