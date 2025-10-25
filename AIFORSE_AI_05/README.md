# BookingService Unit Tests Documentation

## Tổng quan

Bộ test JUnit cho `BookingService` tập trung vào việc test business logic, validation, và các tính toán trong service layer của chức năng đặt bàn.

Link Canva Slide: https://www.canva.com/design/DAG2vdA6nfo/gPC2aeLD_FmPMMdJ0rwaTw/edit?ui=e30


## Cấu trúc Test

```
src/test/java/com/example/booking/
├── service/
│   └── BookingServiceTest.java                 # Service Layer Unit Tests
├── test/
│   ├── base/
│   │   └── BookingTestBase.java                # Base Test Class
│   └── util/
│       └── TestDataFactory.java                # Test Data Factory
└── resources/
    └── application-test.yml                     # Test Configuration
```

## BookingService Unit Tests

**Mục đích**: Test business logic, validation, calculations, và error handling trong BookingService

**Framework sử dụng**:
- JUnit 5 (`@ExtendWith(MockitoExtension.class)`)
- Mockito (`@Mock`, `@InjectMocks`)
- Strictness: LENIENT

### Dependencies được Mock

```java
@Mock private BookingRepository bookingRepository;
@Mock private CustomerRepository customerRepository;
@Mock private RestaurantProfileRepository restaurantProfileRepository;
@Mock private RestaurantTableRepository restaurantTableRepository;
@Mock private BookingTableRepository bookingTableRepository;
@Mock private BookingConflictService conflictService;
@Mock private VoucherService voucherService;
@Mock private BookingDishRepository bookingDishRepository;
@Mock private BookingServiceRepository bookingServiceRepository;
@Mock private NotificationRepository notificationRepository;
@Mock private EntityManager entityManager;
```

### Test Cases

#### ✅ **Happy Path Tests**

1. **`testCreateBooking_WithValidData_ShouldSuccess()`**
   - Test tạo booking với dữ liệu hợp lệ
   - Verify: booking được tạo thành công, customer và restaurant được set đúng

2. **`testCalculateTotalAmount_WithOnlyDeposit_ShouldReturnDepositAmount()`**
   - Test tính tổng tiền chỉ với deposit
   - Verify: trả về đúng số tiền deposit

#### ❌ **Error Handling Tests**

3. **`testCreateBooking_WithCustomerNotFound_ShouldThrowException()`**
   - Test khi customer không tồn tại
   - Expected: `IllegalArgumentException` với message "Customer not found"

4. **`testCreateBooking_WithRestaurantNotFound_ShouldThrowException()`**
   - Test khi restaurant không tồn tại
   - Expected: `IllegalArgumentException` với message "Restaurant not found"

5. **`testCreateBooking_WithTableNotFound_ShouldThrowException()`**
   - Test khi table không tồn tại
   - Expected: `IllegalArgumentException` với message "Table not found"

6. **`testCreateBooking_WithNullBookingForm_ShouldThrowException()`**
   - Test với BookingForm null
   - Expected: `IllegalArgumentException` với message "BookingForm cannot be null"

7. **`testCreateBooking_WithNullCustomerId_ShouldThrowException()`**
   - Test với CustomerId null
   - Expected: `IllegalArgumentException` với message "Customer ID cannot be null"

8. **`testCreateBooking_WithInvalidBookingTime_ShouldThrowException()`**
   - Test với thời gian booking trong quá khứ
   - Expected: `IllegalArgumentException` với message "Booking time cannot be in the past"

9. **`testCreateBooking_WithInvalidGuestCount_ShouldThrowException()`**
   - Test với số khách = 0
   - Expected: `IllegalArgumentException` với message "Guest count must be greater than 0"

10. **`testCreateBooking_WithNegativeDepositAmount_ShouldThrowException()`**
    - Test với số tiền deposit âm
    - Expected: `IllegalArgumentException` với message "Deposit amount cannot be negative"

11. **`testCalculateTotalAmount_WithNullBooking_ShouldThrowException()`**
    - Test tính tổng tiền với booking null
    - Expected: `IllegalArgumentException` với message "Booking cannot be null"

#### 💼 **Business Logic Tests**

12. **`testCreateBooking_ShouldSetCorrectStatus()`**
    - Verify: booking status được set là `PENDING`

13. **`testCreateBooking_ShouldSetCorrectDepositAmount()`**
    - Verify: deposit amount được set đúng

14. **`testCreateBooking_WithDishes_ShouldCreateBookingWithDishes()`**
    - Test tạo booking với dishes
    - Verify: booking được tạo với dish IDs

15. **`testCreateBooking_WithServices_ShouldCreateBookingWithServices()`**
    - Test tạo booking với services
    - Verify: booking được tạo với service IDs

16. **`testCreateBooking_WithDishesAndServices_ShouldCreateBookingWithBoth()`**
    - Test tạo booking với cả dishes và services
    - Verify: booking được tạo với cả hai

17. **`testCreateBooking_ShouldCreateBookingTable()`**
    - Verify: BookingTable được tạo và lưu

18. **`testCreateBooking_ShouldCreateNotification()`**
    - Verify: Notification được tạo và lưu

19. **`testCreateBooking_ShouldSetCorrectCreatedAt()`**
    - Verify: createdAt được set

20. **`testCreateBooking_ShouldSetCorrectUpdatedAt()`**
    - Verify: updatedAt được set

#### 🔄 **Edge Cases**

21. **`testCreateBooking_WithEmptyDishIds_ShouldSuccess()`**
    - Test với dish IDs rỗng
    - Verify: booking vẫn được tạo thành công

22. **`testCreateBooking_WithEmptyServiceIds_ShouldSuccess()`**
    - Test với service IDs rỗng
    - Verify: booking vẫn được tạo thành công

23. **`testCreateBooking_WithNullNote_ShouldSuccess()`**
    - Test với note null
    - Verify: booking vẫn được tạo thành công

24. **`testCreateBooking_WithVeryLongNote_ShouldSuccess()`**
    - Test với note rất dài (2000 ký tự)
    - Verify: booking vẫn được tạo thành công

#### 💰 **Calculation Tests**

25. **`testCalculateTotalAmount_WithDishes_ShouldReturnCorrectTotal()`**
    - Test tính tổng tiền với dishes
    - Verify: trả về đúng tổng tiền

26. **`testCalculateTotalAmount_WithServices_ShouldReturnCorrectTotal()`**
    - Test tính tổng tiền với services
    - Verify: trả về đúng tổng tiền

27. **`testCalculateTotalAmount_WithDishesAndServices_ShouldReturnCorrectTotal()`**
    - Test tính tổng tiền với cả dishes và services
    - Verify: trả về đúng tổng tiền

28. **`testCalculateTotalAmount_WithZeroDeposit_ShouldReturnZero()`**
    - Test tính tổng tiền với deposit = 0
    - Verify: trả về 0

## Test Setup và Mock Configuration

### Setup trong `@BeforeEach`

```java
@BeforeEach
void setUp() {
    customerId = UUID.randomUUID();
    
    // Setup BookingForm
    bookingForm = new BookingForm();
    bookingForm.setRestaurantId(1);
    bookingForm.setTableId(1);
    bookingForm.setGuestCount(4);
    bookingForm.setBookingTime(LocalDateTime.now().plusDays(1));
    bookingForm.setDepositAmount(new BigDecimal("100000"));
    bookingForm.setNote("Test booking");

    // Setup Customer
    customer = new Customer();
    customer.setCustomerId(customerId);
    customer.setFullName("Test Customer");

    // Setup Restaurant
    restaurant = new RestaurantProfile();
    restaurant.setRestaurantId(1);
    restaurant.setRestaurantName("Test Restaurant");
    restaurant.setAddress("123 Test Street");
    restaurant.setPhone("0987654321");

    // Setup Table
    table = new RestaurantTable();
    table.setTableId(1);
    table.setTableName("Table 1");
    table.setCapacity(4);
    table.setRestaurant(restaurant);
    table.setDepositAmount(new BigDecimal("100000"));

    // Setup Mock Booking
    mockBooking = new Booking();
    mockBooking.setBookingId(1);
    mockBooking.setCustomer(customer);
    mockBooking.setRestaurant(restaurant);
    mockBooking.setBookingTime(LocalDateTime.now().plusDays(1));
    mockBooking.setDepositAmount(new BigDecimal("100000"));
    mockBooking.setStatus(BookingStatus.PENDING);
    mockBooking.setNumberOfGuests(4);
}
```

### Mock Setup Helper Method

```java
private void prepareCreateBookingStubs() {
    when(bookingDishRepository.findByBooking(any(Booking.class))).thenReturn(Collections.emptyList());
    when(bookingServiceRepository.findByBooking(any(Booking.class))).thenReturn(Collections.emptyList());
    when(bookingTableRepository.findByBooking(any(Booking.class))).thenReturn(Collections.emptyList());
    when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(bookingRepository.save(any(Booking.class))).thenReturn(mockBooking);
    when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(invocation -> invocation.getArgument(0));
    doNothing().when(entityManager).flush();
}
```

## Test Data Factory

### `TestDataFactory` - Tạo dữ liệu test nhất quán

```java
// Customer
Customer customer = TestDataFactory.createTestCustomer();

// Restaurant
RestaurantProfile restaurant = TestDataFactory.createTestRestaurant();
RestaurantProfile restaurant = TestDataFactory.createTestRestaurant("My Restaurant");

// Table
RestaurantTable table = TestDataFactory.createTestTable(restaurant);

// Dish
Dish dish = TestDataFactory.createTestDish(restaurant);

// Booking
Booking booking = TestDataFactory.createTestBooking();
Booking booking = TestDataFactory.createTestBooking(customer, restaurant);

// BookingForm
BookingForm form = TestDataFactory.createValidBookingForm();
BookingForm form = TestDataFactory.createValidBookingForm(1, 1);

// User
User user = TestDataFactory.createTestUser();
User user = TestDataFactory.createTestUser(UserRole.CUSTOMER);
```

### `BookingTestBase` - Base class cho các test

```java
public class MyBookingTest extends BookingTestBase {
    
    @Test
    void testSomething() {
        // Sử dụng dữ liệu test có sẵn
        BookingForm form = createBookingFormWithRestaurant(1);
        Booking booking = createTestBooking();
        
        // Assertions
        assertBookingCreated(booking);
        assertBookingFormValid(form);
    }
}
```

## Chạy Tests

### Lệnh cơ bản
```bash
# Chạy tất cả tests trong project
mvn test

# Chạy tests với verbose output
mvn test -X

# Chạy tests và bỏ qua failures
mvn test -Dmaven.test.failure.ignore=true
```

### Chạy BookingService tests cụ thể
```bash
# Chạy chỉ BookingServiceTest class
mvn test -Dtest=BookingServiceTest

# Chạy BookingServiceTest với package đầy đủ
mvn test -Dtest=com.example.booking.service.BookingServiceTest

# Chạy tất cả test classes có tên chứa "BookingService"
mvn test -Dtest="*BookingService*Test"
```

### Chạy test method cụ thể
```bash
# Chạy 1 test method cụ thể
mvn test -Dtest=BookingServiceTest#testCreateBooking_WithValidData_ShouldSuccess

# Chạy nhiều test methods
mvn test -Dtest=BookingServiceTest#testCreateBooking_WithValidData_ShouldSuccess,testCalculateTotalAmount_WithOnlyDeposit_ShouldReturnDepositAmount

# Chạy tất cả test methods có tên chứa "testCreateBooking"
mvn test -Dtest=BookingServiceTest#testCreateBooking*
```

### Chạy theo loại test
```bash
# Chạy chỉ Happy Path tests (có thể cần tag)
mvn test -Dtest=BookingServiceTest -Dgroups="happy-path"

# Chạy chỉ Error Handling tests
mvn test -Dtest=BookingServiceTest -Dgroups="error-handling"

# Chạy chỉ Business Logic tests
mvn test -Dtest=BookingServiceTest -Dgroups="business-logic"
```

### Chạy với coverage
```bash
# Chạy tests và tạo coverage report
mvn test jacoco:report

# Chạy tests với coverage và mở report
mvn test jacoco:report
# Sau đó mở file: target/site/jacoco/index.html

# Chạy tests với coverage cho chỉ BookingService
mvn test -Dtest=BookingServiceTest jacoco:report
```

### Chạy với IDE
```bash
# Chạy tests từ IntelliJ IDEA
# Right-click trên BookingServiceTest.java -> Run 'BookingServiceTest'

# Chạy tests từ Eclipse
# Right-click trên BookingServiceTest.java -> Run As -> JUnit Test

# Chạy tests từ VS Code
# Mở Command Palette (Ctrl+Shift+P) -> "Java: Run Tests"
```

### Debug tests
```bash
# Chạy tests với debug mode
mvn test -Dtest=BookingServiceTest -Dmaven.surefire.debug

# Chạy tests với specific JVM options
mvn test -Dtest=BookingServiceTest -DargLine="-Xmx1024m -XX:+UseG1GC"

# Chạy tests và tạo detailed report
mvn test -Dtest=BookingServiceTest -Dsurefire.reportFormat=xml
```

### Lệnh hữu ích khác
```bash
# Clean và compile trước khi test
mvn clean compile test

# Chạy tests và skip compilation
mvn test -Dmaven.main.skip=true

# Chạy tests với parallel execution
mvn test -Dtest=BookingServiceTest -DforkCount=2

# Chạy tests và tạo test report
mvn test -Dtest=BookingServiceTest surefire-report:report
```

## Test Coverage

### Mục tiêu Coverage cho BookingService
- **Line Coverage**: > 90%
- **Branch Coverage**: > 85%
- **Method Coverage**: > 95%

### Các method quan trọng được test
- `BookingService.createBooking(BookingForm, UUID)`
- `BookingService.calculateTotalAmount(Booking)`
- Validation logic trong createBooking
- Error handling cho các trường hợp null/invalid

## Best Practices

### 1. Naming Convention
```java
// Pattern: test{MethodName}_{Condition}_{ExpectedResult}
testCreateBooking_WithValidData_ShouldSuccess()
testCreateBooking_WithCustomerNotFound_ShouldThrowException()
testCalculateTotalAmount_WithOnlyDeposit_ShouldReturnDepositAmount()
```

### 2. Test Structure (AAA Pattern)
```java
@Test
void testCreateBooking_WithValidData_ShouldSuccess() {
    // Arrange (Given)
    prepareCreateBookingStubs();
    when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
    when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
    when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));
    
    // Act (When)
    Booking result = bookingService.createBooking(bookingForm, customerId);
    
    // Assert (Then)
    assertNotNull(result);
    assertEquals(customerId, result.getCustomer().getCustomerId());
    verify(bookingRepository, atLeastOnce()).save(any(Booking.class));
}
```

### 3. Mock Usage
```java
// Mock dependencies với @Mock
@Mock
private BookingRepository bookingRepository;

// Setup mock behavior
when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
when(bookingRepository.save(any(Booking.class))).thenReturn(mockBooking);

// Verify interactions
verify(bookingRepository, atLeastOnce()).save(any(Booking.class));
verify(bookingTableRepository).save(any(BookingTable.class));
```

### 4. Exception Testing
```java
// Test exception với message
IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
    bookingService.createBooking(bookingForm, customerId);
});
assertEquals("Customer not found", exception.getMessage());
```

### 5. Helper Methods
```java
// Sử dụng helper method để setup mock
private void prepareCreateBookingStubs() {
    when(bookingDishRepository.findByBooking(any(Booking.class))).thenReturn(Collections.emptyList());
    when(bookingServiceRepository.findByBooking(any(Booking.class))).thenReturn(Collections.emptyList());
    when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(bookingRepository.save(any(Booking.class))).thenReturn(mockBooking);
}
```

## Troubleshooting

### Common Issues

1. **Test fails with "Customer not found"**
   - Kiểm tra mock setup: `when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer))`
   - Đảm bảo customerId được set đúng trong setUp()

2. **Test fails with "Restaurant not found"**
   - Kiểm tra mock setup: `when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant))`
   - Đảm bảo restaurantId trong BookingForm khớp với mock

3. **Test fails with "Table not found"**
   - Kiểm tra mock setup: `when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table))`
   - Đảm bảo tableId trong BookingForm khớp với mock

4. **Mockito strictness issues**
   - Sử dụng `@MockitoSettings(strictness = Strictness.LENIENT)` để tránh lỗi unused stubs

5. **Test fails with null pointer**
   - Gọi `prepareCreateBookingStubs()` trước khi test
   - Đảm bảo tất cả dependencies cần thiết được mock

### Debug Tips

1. **Enable debug logging**
```yaml
logging:
  level:
    com.example.booking: DEBUG
    org.mockito: DEBUG
```

2. **Print test data for debugging**
```java
System.out.println("Customer ID: " + customerId);
System.out.println("Booking Form: " + bookingForm);
System.out.println("Mock Booking: " + mockBooking);
```

3. **Verify mock interactions**
```java
// Kiểm tra mock có được gọi đúng không
verify(customerRepository).findById(customerId);
verify(bookingRepository, atLeastOnce()).save(any(Booking.class));
```

4. **Check mock setup**
```java
// Đảm bảo mock trả về đúng dữ liệu
when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
// Thay vì
when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
```

## Kết luận

Bộ test `BookingServiceTest` cung cấp coverage toàn diện cho BookingService:
- **28 test cases** bao gồm:
  - 2 Happy Path tests
  - 11 Error Handling tests  
  - 9 Business Logic tests
  - 4 Edge Cases tests
  - 4 Calculation tests

**Các điểm mạnh**:
- Test đầy đủ các trường hợp validation
- Mock tất cả dependencies
- Test cả success và error scenarios
- Sử dụng helper methods để tái sử dụng code
- Tuân theo AAA pattern (Arrange-Act-Assert)

**Coverage đạt được**:
- Test tất cả public methods của BookingService
- Test validation logic
- Test business rules
- Test error handling
- Test edge cases

Tất cả test cases đều có thể chạy độc lập và tuân theo best practices của JUnit 5 và Mockito.
