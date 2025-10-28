# WithdrawalService Test - Command Reference

## Test Coverage

### Methods Tested: 4/4 (100%)
1. ✅ `requestWithdrawal()` - 9 tests
2. ✅ `processWithdrawal()` (approveWithdrawal) - 6 tests
3. ✅ `rejectWithdrawal()` - 6 tests
4. ✅ `getWithdrawalHistory()` (getWithdrawalsByRestaurant) - 4 tests

**Total: 25 test cases**

---

## Commands

### Run All WithdrawalService Tests

**PowerShell (Windows):**
```powershell
mvn test "-Dtest=WithdrawalServiceTest" "-Dspring.profiles.active=test"
```


**CMD or Bash:**
```bash
mvn test -Dtest=WithdrawalServiceTest -Dspring.profiles.active=test
```

### Run Specific Test Method

**PowerShell (Windows):**
```powershell
# Test requestWithdrawal()
mvn test "-Dtest=WithdrawalServiceTest`$RequestWithdrawalTests" "-Dspring.profiles.active=test"

# Test processWithdrawal() (approveWithdrawal)
mvn test "-Dtest=WithdrawalServiceTest`$ProcessWithdrawalTests" "-Dspring.profiles.active=test"

# Test rejectWithdrawal()
mvn test "-Dtest=WithdrawalServiceTest`$RejectWithdrawalTests" "-Dspring.profiles.active=test"

# Test getWithdrawalHistory()
mvn test "-Dtest=WithdrawalServiceTest`$GetWithdrawalHistoryTests" "-Dspring.profiles.active=test"
```

**CMD or Bash:**
```bash
# Test requestWithdrawal()
mvn test -Dtest=WithdrawalServiceTest$RequestWithdrawalTests -Dspring.profiles.active=test

# Test processWithdrawal() (approveWithdrawal)
mvn test -Dtest=WithdrawalServiceTest$ProcessWithdrawalTests -Dspring.profiles.active=test

# Test rejectWithdrawal()
mvn test -Dtest=WithdrawalServiceTest$RejectWithdrawalTests -Dspring.profiles.active=test

# Test getWithdrawalHistory()
mvn test -Dtest=WithdrawalServiceTest$GetWithdrawalHistoryTests -Dspring.profiles.active=test
```

### Run with Coverage Report

**PowerShell (Windows):**
```powershell
mvn clean test jacoco:report "-Dtest=WithdrawalServiceTest" "-Dspring.profiles.active=test"
```

**CMD or Bash:**
```bash
mvn clean test jacoco:report -Dtest=WithdrawalServiceTest -Dspring.profiles.active=test
```

### Using Batch Script (Windows) - RECOMMENDED

```bash
run_withdrawal_service_tests.bat
```

> **Note**: Sử dụng batch script để tránh lỗi parsing trong PowerShell!

---

## Test Categories

### 1. requestWithdrawal() - 9 Tests

| Test # | Category | Description |
|--------|----------|-------------|
| 1 | Happy Path | With Sufficient Balance, Should Create Request |
| 2 | Happy Path | Should Calculate Net Amount |
| 3 | Business Logic | Minimum Amount Should Succeed |
| 4 | Validation | Insufficient Balance, Should Throw Exception |
| 5 | Validation | Below Minimum Amount, Should Throw Exception |
| 6 | Business Logic | Exceed Daily Limit, Should Throw Exception |
| 7 | Business Logic | Should Send Notification |
| 8 | Error Handling | Invalid Bank Account, Should Throw Exception |
| 9 | Error Handling | Non-Existent Restaurant, Should Throw Exception |

### 2. processWithdrawal() (approveWithdrawal) - 6 Tests

| Test # | Category | Description |
|--------|----------|-------------|
| 11 | Happy Path | With Pending Request, Should Approve Successfully |
| 12 | Happy Path | Should Lock Restaurant Balance |
| 13 | Business Logic | Insufficient Balance, Should Throw Exception |
| 14 | Business Logic | Should Use Pessimistic Locking |
| 16 | Validation | Cannot Approve Non-PENDING Status, Should Throw Exception |
| 18 | Integration | Should Update Admin Notes |

### 3. rejectWithdrawal() - 6 Tests

| Test # | Category | Description |
|--------|----------|-------------|
| 19 | Happy Path | With Pending Request, Should Reject Successfully |
| 20 | Business Logic | Should Not Unlock Balance When Rejected |
| 21 | Business Logic | Should Send Notification |
| 22 | Validation | Cannot Reject Non-PENDING, Should Throw Exception |
| 23 | Error Handling | Non-Existent Request, Should Throw Exception |
| 25 | Integration | Should Update Rejection Timestamps |

### 4. getWithdrawalHistory() (getWithdrawalsByRestaurant) - 4 Tests

| Test # | Category | Description |
|--------|----------|-------------|
| 26 | Happy Path | Valid Restaurant, Should Return All Requests |
| 27 | Happy Path | Returns Paginated Results |
| 28 | Business Logic | Should Filter By Restaurant |
| 30 | Edge Case | No History, Should Return Empty Page |

---

## Expected Results

- **Total Tests**: 25
- **Expected Pass Rate**: >90%
- **Target Pass Rate**: 100%

---

## Files

- **Test File**: `src/test/java/com/example/booking/service/WithdrawalServiceTest.java`
- **Service File**: `src/main/java/com/example/booking/service/WithdrawalService.java`
- **Batch Script**: `run_withdrawal_service_tests.bat`

---

**Date:** 28/10/2025  
**Status:** Ready for Testing

