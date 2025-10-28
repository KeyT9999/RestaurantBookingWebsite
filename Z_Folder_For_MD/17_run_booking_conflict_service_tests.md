# BookingConflictService Test - Command Reference

## Test Coverage

### Methods Tested: 7/7 (100%)
1. ✅ `validateBookingConflicts()` - 15 tests
2. ✅ `validateBookingUpdateConflicts()` - 8 tests
3. ✅ `validateBookingTime()` - 7 tests (6 from spec + 1 additional)
4. ✅ `validateRestaurantHours()` - 7 tests (5 from spec + 2 additional)
5. ✅ `validateTableStatus()` - 6 tests
6. ✅ `validateTableConflicts()` - 8 tests
7. ✅ `getAvailableTimeSlots()` - 7 tests (6 from spec + 1 additional)

**Total: 58 test cases** (54 from specification + 4 additional)

---

## Commands

### Run All BookingConflictService Tests

**PowerShell (Windows):**
```powershell
mvn test "-Dtest=BookingConflictServiceTest" "-Dspring.profiles.active=test"
```

**CMD or Bash:**
```bash
mvn test -Dtest=BookingConflictServiceTest -Dspring.profiles.active=test
```

### Run Specific Test Method

**PowerShell (Windows):**
```powershell
# Test validateBookingConflicts()
mvn test "-Dtest=BookingConflictServiceTest`$ValidateBookingConflictsTests" "-Dspring.profiles.active=test"

# Test validateBookingUpdateConflicts()
mvn test "-Dtest=BookingConflictServiceTest`$ValidateBookingUpdateConflictsTests" "-Dspring.profiles.active=test"

# Test validateBookingTime()
mvn test "-Dtest=BookingConflictServiceTest`$ValidateBookingTimeTests" "-Dspring.profiles.active=test"

# Test validateRestaurantHours()
mvn test "-Dtest=BookingConflictServiceTest`$ValidateRestaurantHoursTests" "-Dspring.profiles.active=test"

# Test validateTableStatus()
mvn test "-Dtest=BookingConflictServiceTest`$ValidateTableStatusTests" "-Dspring.profiles.active=test"

# Test validateTableConflicts()
mvn test "-Dtest=BookingConflictServiceTest`$ValidateTableConflictsTests" "-Dspring.profiles.active=test"

# Test getAvailableTimeSlots()
mvn test "-Dtest=BookingConflictServiceTest`$GetAvailableTimeSlotsTests" "-Dspring.profiles.active=test"
```

**CMD or Bash:**
```bash
# Test validateBookingConflicts()
mvn test -Dtest=BookingConflictServiceTest$ValidateBookingConflictsTests -Dspring.profiles.active=test

# Test validateBookingUpdateConflicts()
mvn test -Dtest=BookingConflictServiceTest$ValidateBookingUpdateConflictsTests -Dspring.profiles.active=test

# Test validateBookingTime()
mvn test -Dtest=BookingConflictServiceTest$ValidateBookingTimeTests -Dspring.profiles.active=test

# Test validateRestaurantHours()
mvn test -Dtest=BookingConflictServiceTest$ValidateRestaurantHoursTests -Dspring.profiles.active=test

# Test validateTableStatus()
mvn test -Dtest=BookingConflictServiceTest$ValidateTableStatusTests -Dspring.profiles.active=test

# Test validateTableConflicts()
mvn test -Dtest=BookingConflictServiceTest$ValidateTableConflictsTests -Dspring.profiles.active=test

# Test getAvailableTimeSlots()
mvn test -Dtest=BookingConflictServiceTest$GetAvailableTimeSlotsTests -Dspring.profiles.active=test
```

### Run with Coverage Report

**PowerShell (Windows):**
```powershell
mvn clean test jacoco:report "-Dtest=BookingConflictServiceTest" "-Dspring.profiles.active=test"
```

**CMD or Bash:**
```bash
mvn clean test jacoco:report -Dtest=BookingConflictServiceTest -Dspring.profiles.active=test
```

### Using Batch Script (Windows) - RECOMMENDED

```bash
run_booking_conflict_service_tests.bat
```

> **Note**: Sử dụng batch script để tránh lỗi parsing trong PowerShell!

---

## Test Categories

### 1. validateBookingConflicts() - 15 Tests

| Test # | Category | Description |
|--------|----------|-------------|
| 1 | Happy Path | With No Conflicts, Should Pass |
| 2 | Happy Path | With Available Table, Should Pass |
| 3 | Business Logic | Should Check Restaurant Exists |
| 4 | Business Logic | Should Check Minimum Advance Time |
| 5 | Business Logic | Should Check Maximum Advance Time |
| 6 | Business Logic | Should Validate Operating Hours |
| 7 | Business Logic | With Occupied Table, Should Fail |
| 8 | Business Logic | With Maintenance Table, Should Fail |
| 9 | Business Logic | With Time Overlap, Should Fail |
| 10 | Business Logic | With Buffer Time, Should Detect Conflict |
| 11 | Integration | Should Validate Customer Exists |
| 12 | Integration | Should Validate Restaurant Exists |
| 13 | Integration | Should Check Multiple Tables Conflict |
| 14 | Business Logic | Should Determine Conflict Type |
| 15 | Error Handling | With Null Booking Time, Should Fail |

### 2. validateBookingUpdateConflicts() - 8 Tests

| Test # | Category | Description |
|--------|----------|-------------|
| 16 | Happy Path | With No Conflicts, Should Pass |
| 17 | Happy Path | With Time Change, Should Check New Time |
| 18 | Business Logic | With Time Overlap, Should Fail |
| 19 | Business Logic | Should Exclude Current Booking |
| 20 | Business Logic | With Table Change, Should Validate New Table |
| 21 | Business Logic | With Restaurant Change, Should Validate New Restaurant |
| 22 | Validation | With Wrong Customer, Should Fail |
| 23 | Error Handling | With Non-Existent Booking, Should Fail |

### 3. validateBookingTime() - 7 Tests

| Test # | Category | Description |
|--------|----------|-------------|
| 24 | Happy Path | With Valid Future Time, Should Pass |
| 25 | Validation | In The Past, Should Add Conflict |
| 26 | Validation | Less Than 30 Minutes, Should Add Conflict |
| 27 | Validation | More Than 30 Days, Should Add Conflict |
| 28 | Validation | With Exact Minimum Time, Should Pass |
| 29 | Business Logic | With Exact Maximum Time, Should Pass |
| 30 | Error Handling | With Null Time, Should Add Conflict |

### 4. validateRestaurantHours() - 7 Tests

| Test # | Category | Description |
|--------|----------|-------------|
| 31 | Happy Path | Within Operating Hours, Should Pass |
| 32 | Validation | Before Opening, Should Add Conflict |
| 33 | Validation | After Closing, Should Add Conflict |
| 34 | Business Logic | With Custom Hours, Should Parse Correctly |
| 35 | Business Logic | With Invalid Format, Should Use Default |
| 36 | Business Logic | With Null Hours, Should Use Default |
| 37 | Integration | At Exact Open Time, Should Pass |

### 5. validateTableStatus() - 6 Tests

| Test # | Category | Description |
|--------|----------|-------------|
| 38 | Happy Path | With Available Table, Should Pass |
| 39 | Validation | With Occupied Table, Should Add Conflict |
| 40 | Validation | With Maintenance Table, Should Add Conflict |
| 41 | Business Logic | With Reserved Table, Should Allow |
| 42 | Error Handling | With Non-Existent Table, Should Fail |
| 43 | Integration | Should Load Table From Database |

### 6. validateTableConflicts() - 8 Tests

| Test # | Category | Description |
|--------|----------|-------------|
| 44 | Happy Path | With No Overlaps, Should Pass |
| 45 | Validation | With Exact Time Overlap, Should Fail |
| 46 | Business Logic | With Partial Overlap, Should Fail |
| 47 | Business Logic | With Buffer Overlap, Should Fail |
| 48 | Business Logic | Should 2-hour Duration Buffer |
| 49 | Business Logic | Should Check Conflicts Correctly |
| 50 | Integration | Should Query Booking Repository |
| 51 | Error Handling | With Non-Existent Table, Should Fail |

### 7. getAvailableTimeSlots() - 7 Tests

| Test # | Category | Description |
|--------|----------|-------------|
| 52 | Happy Path | With No Bookings, Should Return All Slots |
| 53 | Happy Path | With Some Bookings, Should Filter Slots |
| 54 | Business Logic | Should Apply 2-hour Duration |
| 55 | Business Logic | Should Generate Hourly Slots |
| 56 | Business Logic | With Fully Booked, Should Return Empty |
| 57 | Error Handling | With Non-Existent Table, Should Fail |
| 58 | Integration | Should Query Booking Conflict Data |

---

## Expected Results

- **Total Tests**: 58
- **Expected Pass Rate**: >90%
- **Target Pass Rate**: 95-100%

---

## Files

- **Test File**: `src/test/java/com/example/booking/service/BookingConflictServiceTest.java`
- **Service File**: `src/main/java/com/example/booking/service/BookingConflictService.java`
- **Batch Script**: `run_booking_conflict_service_tests.bat`

---

## Test Execution Notes

### Key Points:
1. **Mocking Strategy**: All repository dependencies are mocked using Mockito
2. **Time Handling**: Tests use `LocalDateTime.now()` with relative offsets to avoid time-based failures
3. **Conflict Types**: Tests verify both exception throwing and correct conflict type determination
4. **Buffer Time**: Tests verify the 30-minute buffer before/after bookings
5. **Duration**: Tests verify the 2-hour booking duration constant
6. **Operating Hours**: Tests verify default hours (10:00-22:00) and custom hour parsing
7. **Table Status**: Tests verify OCCUPIED and MAINTENANCE blocking, but RESERVED allowing

### Common Issues:
- **Time-sensitive tests**: May fail if system clock is significantly off
- **Mock behavior**: Ensure mocks return appropriate values for chained method calls
- **Exception types**: Tests expect specific exception types (IllegalArgumentException vs BookingConflictException)

---

**Date:** 28/10/2025  
**Status:** Ready for Testing


