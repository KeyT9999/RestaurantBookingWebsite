# CustomerService Test Commands

## Test Results Summary
- **Total Tests**: 29
- **Pass Rate**: 100% ✅
- **Execution Time**: ~1.3 seconds

## Quick Run Command

### Windows (PowerShell)
```bash
run_customer_service_tests.bat
```

### Maven Direct Command
```bash
mvn test "-Dtest=CustomerServiceTest" "-Dspring.profiles.active=test"
```

---

## Run Individual Test Suites

### 1. FindByUsername Tests (6 tests)
```bash
mvn test "-Dtest=CustomerServiceTest$FindByUsernameTests" "-Dspring.profiles.active=test"
```

**Test Cases:**
- ✅ testFindByUsername_WithExistingCustomer_ShouldReturnCustomer
- ✅ testFindByUsername_WithCaseInsensitive_ShouldFindCorrectCustomer
- ✅ testFindByUsername_ShouldLoadUserRelationship
- ✅ testFindByUsername_WithNonExistentUsername_ShouldReturnEmpty
- ✅ testFindByUsername_WithNullUsername_ShouldHandleGracefully
- ✅ testFindByUsername_WithMultipleCustomersSameUserId_ShouldReturnFirst

---

### 2. FindById Tests (5 tests)
```bash
mvn test "-Dtest=CustomerServiceTest$FindByIdTests" "-Dspring.profiles.active=test"
```

**Test Cases:**
- ✅ testFindById_WithExistingCustomerId_ShouldReturnCustomer
- ✅ testFindById_ShouldLoadAllRelationships
- ✅ testFindById_WithNonExistentId_ShouldReturnEmpty
- ✅ testFindById_WithNullId_ShouldReturnEmpty
- ✅ testFindById_ShouldMaintainTransactionalContext

---

### 3. FindByUserId Tests (4 tests)
```bash
mvn test "-Dtest=CustomerServiceTest$FindByUserIdTests" "-Dspring.profiles.active=test"
```

**Test Cases:**
- ✅ testFindByUserId_WithValidUserId_ShouldReturnCustomer
- ✅ testFindByUserId_ShouldFindCorrectCustomer
- ✅ testFindByUserId_WithUserWithoutCustomer_ShouldReturnEmpty
- ✅ testFindByUserId_WithNonExistentUserId_ShouldReturnEmpty

---

### 4. FindAllCustomers Tests (5 tests)
```bash
mvn test "-Dtest=CustomerServiceTest$FindAllCustomersTests" "-Dspring.profiles.active=test"
```

**Test Cases:**
- ✅ testFindAllCustomers_WithMultipleCustomers_ShouldReturnAll
- ✅ testFindAllCustomers_ShouldOrderByIdOrName
- ✅ testFindAllCustomers_WithEmptyDatabase_ShouldReturnEmptyList
- ✅ testFindAllCustomers_ShouldLoadUserRelationships
- ✅ testFindAllCustomers_WithLargeDataset_ShouldHandleEfficiently

---

### 5. Save Tests (9 tests)
```bash
mvn test "-Dtest=CustomerServiceTest$SaveTests" "-Dspring.profiles.active=test"
```

**Test Cases:**
- ✅ testSave_WithNewCustomer_ShouldCreateNewRecord
- ✅ testSave_WithExistingCustomer_ShouldUpdateRecord
- ✅ testSave_ShouldCreateAtAndUpdatedAtTimestamps
- ✅ testSave_ShouldUpdateAtTimestamps
- ✅ testSave_WithUserRelationship_ShouldMaintainRelationship
- ✅ testSave_WithNullUser_ShouldThrowException
- ✅ testSave_WithDuplicateUserId_ShouldThrowException
- ✅ testSave_ShouldPersistImmediatelyToDatabase
- ✅ testSave_ReturnSavedCustomerWithId

---

## Expected Output (Success)

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.example.booking.service.CustomerServiceTest$SaveTests
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.example.booking.service.CustomerServiceTest$FindAllCustomersTests
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.example.booking.service.CustomerServiceTest$FindByUserIdTests
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.example.booking.service.CustomerServiceTest$FindByIdTests
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.example.booking.service.CustomerServiceTest$FindByUsernameTests
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 29, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---

## Troubleshooting

### Issue: Maven command not recognized
**Solution**: Ensure Maven is installed and in PATH
```bash
mvn --version
```

### Issue: Tests fail with "Profile test not found"
**Solution**: Check `src/test/resources/application-test.yml` exists

### Issue: Database connection errors
**Solution**: Test profile should use H2 in-memory database

---

## Test File Location
```
src/test/java/com/example/booking/service/CustomerServiceTest.java
```

## Coverage Report Location
```
target/surefire-reports/com.example.booking.service.CustomerServiceTest.txt
```

---

## Related Documentation
- `16_CustomerService_COVERAGE.md` - Detailed test coverage report
- `16_CustomerService_PROMPT_SUMMARY.md` - Test development summary
- Test images showing test case specifications

---

**Last Updated**: 28/10/2025  
**Status**: ✅ ALL TESTS PASSING (29/29)




