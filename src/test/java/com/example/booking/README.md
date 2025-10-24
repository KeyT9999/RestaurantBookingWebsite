# Booking Flow JUnit Tests Documentation

## Tổng quan

Bộ test JUnit cho luồng Booking được tổ chức thành các layer khác nhau để test toàn diện chức năng đặt bàn.

## Cấu trúc Test

```
src/test/java/com/example/booking/
├── web/
│   └── controller/
│       └── BookingControllerTest.java          # Controller Layer Tests
├── service/
│   └── BookingServiceTest.java                 # Service Layer Tests  
├── integration/
│   └── BookingIntegrationTest.java             # Integration Tests
├── test/
│   ├── base/
│   │   └── BookingTestBase.java                # Base Test Class
│   └── util/
│       └── TestDataFactory.java                # Test Data Factory
└── resources/
    └── application-test.yml                     # Test Configuration
```

## Các loại Test

### 1. Controller Tests (`BookingControllerTest`)

**Mục đích**: Test web layer, HTTP requests/responses, security

**Test Cases**:
- ✅ **Happy Path Tests**:
  - `testShowBookingForm_WithCustomerRole_ShouldReturnForm()`
  - `testCreateBooking_WithValidData_ShouldSuccess()`
  - `testShowBookingForm_WithRestaurantId_ShouldPreSelectRestaurant()`

- ❌ **Error Handling Tests**:
  - `testCreateBooking_WithConflict_ShouldReturnError()`
  - `testCreateBooking_WithValidationErrors_ShouldReturnToForm()`
  - `testCreateBooking_WithGeneralException_ShouldReturnError()`

- 🔒 **Security Tests**:
  - `testShowBookingForm_WithRestaurantOwnerRole_ShouldRedirect()`
  - `testCreateBooking_WithRestaurantOwnerRole_ShouldRedirect()`
  - `testShowBookingForm_WithoutAuthentication_ShouldRedirectToLogin()`
  - `testCreateBooking_WithoutAuthentication_ShouldRedirectToLogin()`

- 🔄 **Edge Cases**:
  - `testShowBookingForm_WithNoRestaurants_ShouldShowEmptyList()`
  - `testCreateBooking_WithLargeGuestCount_ShouldHandleGracefully()`
  - `testCreateBooking_WithSpecialCharactersInNote_ShouldHandleGracefully()`

### 2. Service Tests (`BookingServiceTest`)

**Mục đích**: Test business logic, validation, calculations

**Test Cases**:
- ✅ **Happy Path Tests**:
  - `testCreateBooking_WithValidData_ShouldSuccess()`
  - `testCalculateTotalAmount_WithDishesAndServices_ShouldReturnCorrectTotal()`
  - `testCalculateTotalAmount_WithOnlyDeposit_ShouldReturnDepositAmount()`
  - `testValidateBookingTime_WithFutureTime_ShouldPass()`
  - `testValidateGuestCount_WithValidCount_ShouldPass()`

- ❌ **Error Handling Tests**:
  - `testCreateBooking_WithCustomerNotFound_ShouldThrowException()`
  - `testCreateBooking_WithRestaurantNotFound_ShouldThrowException()`
  - `testCreateBooking_WithTableNotFound_ShouldThrowException()`
  - `testCreateBooking_WithConflict_ShouldThrowBookingConflictException()`
  - `testValidateBookingTime_WithPastTime_ShouldThrowException()`
  - `testValidateGuestCount_WithZeroCount_ShouldThrowException()`
  - `testValidateGuestCount_WithNegativeCount_ShouldThrowException()`
  - `testValidateGuestCount_WithTooLargeCount_ShouldThrowException()`

- 🔄 **Edge Cases**:
  - `testCreateBooking_WithEmptyDishIds_ShouldSkipDishAssignment()`
  - `testCreateBooking_WithEmptyServiceIds_ShouldSkipServiceAssignment()`
  - `testCreateBooking_WithNullNote_ShouldHandleGracefully()`

- 💼 **Business Logic Tests**:
  - `testCreateBooking_ShouldSetCorrectStatus()`
  - `testCreateBooking_ShouldSetCorrectDepositAmount()`
  - `testCreateBooking_ShouldCreateNotification()`

### 3. Integration Tests (`BookingIntegrationTest`)

**Mục đích**: Test toàn bộ luồng với database thật

**Test Cases**:
- 🔄 **End-to-End Tests**:
  - `testBookingFlow_EndToEnd()`
  - `testBookingWithDishes_ShouldCreateBookingDishes()`
  - `testBookingWithMultipleTables_ShouldCreateMultipleBookingTables()`
  - `testBookingTransaction_RollbackOnError()`
  - `testBookingConflictDetection_ShouldPreventDoubleBooking()`
  - `testBookingAmountCalculation_WithDishes_ShouldBeCorrect()`
  - `testBookingStatusFlow_ShouldUpdateCorrectly()`

- 🔄 **Edge Cases**:
  - `testBookingWithLargeGuestCount_ShouldHandleGracefully()`
  - `testBookingWithSpecialCharactersInNote_ShouldPersistCorrectly()`
  - `testBookingWithEmptyNote_ShouldHandleGracefully()`
  - `testBookingWithNullNote_ShouldHandleGracefully()`

## Test Data Factory

### `TestDataFactory` - Tạo dữ liệu test nhất quán

```java
// Customer
Customer customer = TestDataFactory.createTestCustomer();
Customer customer = TestDataFactory.createTestCustomer("test@example.com");

// Restaurant
RestaurantProfile restaurant = TestDataFactory.createTestRestaurant();
RestaurantProfile restaurant = TestDataFactory.createTestRestaurant("My Restaurant");

// Table
RestaurantTable table = TestDataFactory.createTestTable(restaurant);
RestaurantTable table = TestDataFactory.createTestTable(restaurant, 1, "Table 1", 4);

// Dish
Dish dish = TestDataFactory.createTestDish(restaurant);
Dish dish = TestDataFactory.createTestDish(restaurant, "Pizza", new BigDecimal("100000"));

// Booking
Booking booking = TestDataFactory.createTestBooking();
Booking booking = TestDataFactory.createTestBooking(customer, restaurant);

// BookingForm
BookingForm form = TestDataFactory.createValidBookingForm();
BookingForm form = TestDataFactory.createValidBookingForm(1, 1);
BookingForm form = TestDataFactory.createBookingFormWithDishes(1, 1, "1,2");
```

### `BookingTestBase` - Base class cho các test

```java
public class MyBookingTest extends BookingTestBase {
    
    @Test
    void testSomething() {
        // Sử dụng dữ liệu test có sẵn
        BookingForm form = createBookingFormWithGuestCount(6);
        Booking booking = createTestBooking();
        
        // Assertions
        assertBookingCreated(booking);
        assertBookingFormValid(form);
    }
}
```

## Test Configuration

### `application-test.yml`

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    
  h2:
    console:
      enabled: true

logging:
  level:
    com.example.booking: DEBUG
    org.hibernate.SQL: DEBUG
```

## Chạy Tests

### Chạy tất cả tests
```bash
mvn test
```

### Chạy test cụ thể
```bash
# Controller tests
mvn test -Dtest=BookingControllerTest

# Service tests  
mvn test -Dtest=BookingServiceTest

# Integration tests
mvn test -Dtest=BookingIntegrationTest

# Tất cả booking tests
mvn test -Dtest="*Booking*Test"
```

### Chạy với coverage
```bash
mvn test jacoco:report
```

## Test Coverage

### Mục tiêu Coverage
- **Line Coverage**: > 90%
- **Branch Coverage**: > 85%
- **Method Coverage**: > 95%

### Các method quan trọng cần test
- `BookingController.showBookingForm()`
- `BookingController.createBooking()`
- `BookingService.createBooking()`
- `BookingService.calculateTotalAmount()`
- `BookingService.validateBookingTime()`
- `BookingService.validateGuestCount()`

## Best Practices

### 1. Naming Convention
```java
// Pattern: test{MethodName}_{Condition}_{ExpectedResult}
testCreateBooking_WithValidData_ShouldSuccess()
testCreateBooking_WithInvalidData_ShouldThrowException()
testCreateBooking_WithConflict_ShouldReturnError()
```

### 2. Test Structure (AAA Pattern)
```java
@Test
void testCreateBooking_WithValidData_ShouldSuccess() {
    // Arrange (Given)
    BookingForm form = createValidBookingForm();
    when(bookingService.createBooking(any(), any())).thenReturn(mockBooking);
    
    // Act (When)
    Booking result = bookingService.createBooking(form, customerId);
    
    // Assert (Then)
    assertNotNull(result);
    assertEquals(4, result.getGuestCount());
    verify(bookingRepository).save(any(Booking.class));
}
```

### 3. Mock Usage
```java
// Mock dependencies
@MockBean
private BookingService bookingService;

// Setup mock behavior
when(bookingService.createBooking(any(), any())).thenReturn(mockBooking);

// Verify interactions
verify(bookingRepository).save(any(Booking.class));
```

### 4. Assertions
```java
// Basic assertions
assertNotNull(result);
assertEquals(expected, actual);
assertTrue(condition);
assertFalse(condition);

// Exception assertions
assertThrows(IllegalArgumentException.class, () -> {
    service.method();
});

// Collection assertions
assertFalse(list.isEmpty());
assertEquals(2, list.size());
```

## Troubleshooting

### Common Issues

1. **Test fails with "Customer not found"**
   - Kiểm tra mock setup cho `customerRepository.findById()`

2. **Test fails with "Restaurant not found"**
   - Kiểm tra mock setup cho `restaurantProfileRepository.findById()`

3. **Integration test fails with database**
   - Kiểm tra `@DataJpaTest` annotation
   - Kiểm tra `application-test.yml` configuration

4. **Security test fails**
   - Kiểm tra `@WithMockUser` annotation
   - Kiểm tra role configuration

### Debug Tips

1. **Enable debug logging**
```yaml
logging:
  level:
    com.example.booking: DEBUG
```

2. **Use TestEntityManager for integration tests**
```java
@Autowired
private TestEntityManager entityManager;

entityManager.persistAndFlush(customer);
```

3. **Print test data for debugging**
```java
System.out.println("Test data: " + bookingForm);
```

## Kết luận

Bộ test này cung cấp coverage toàn diện cho luồng Booking:
- **14 test cases** cho Controller layer
- **15 test cases** cho Service layer  
- **10 test cases** cho Integration layer
- **Tổng cộng: 39 test cases**

Tất cả test cases đều tuân theo best practices và có thể chạy độc lập hoặc cùng nhau.
