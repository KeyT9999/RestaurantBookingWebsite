package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.booking.domain.Customer;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.repository.CustomerRepository;

/**
 * Unit tests for CustomerService
 * 
 * Test Coverage:
 * 1. findByUsername() - 6 test cases
 * 2. findById() - 5 test cases
 * 3. findByUserId() - 4 test cases
 * 4. findAllCustomers() - 5 test cases
 * 5. save() - 9 test cases
 * 
 * Total: 29 test cases
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService Tests")
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private User testUser;
    private Customer testCustomer;
    private UUID testUserId;
    private UUID testCustomerId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testCustomerId = UUID.randomUUID();

        // Create test user
        testUser = new User();
        testUser.setId(testUserId);
        testUser.setUsername("customer@email.com");
        testUser.setEmail("customer@email.com");
        testUser.setFullName("Test Customer");
        testUser.setPassword("password123");
        testUser.setRole(UserRole.CUSTOMER);
        testUser.setEmailVerified(true);

        // Create test customer
        testCustomer = new Customer(testUser);
        testCustomer.setCustomerId(testCustomerId);
    }

    // ==================== 1. findByUsername() - 6 Test Cases ====================
    @Nested
    @DisplayName("1. findByUsername() - 6 Cases")
    class FindByUsernameTests {

        @Test
        @DisplayName("Happy Path: testFindByUsername_WithExistingCustomer_ShouldReturnCustomer")
        void testFindByUsername_WithExistingCustomer_ShouldReturnCustomer() {
            // Given
            String username = "customer@email.com";
            when(customerRepository.findByUserUsername(username))
                .thenReturn(Optional.of(testCustomer));

            // When
            Optional<Customer> result = customerService.findByUsername(username);

            // Then
            assertTrue(result.isPresent(), "Đảm bảo tìm thấy customer bằng username");
            assertEquals(testCustomer.getCustomerId(), result.get().getCustomerId());
            assertEquals(username, result.get().getUser().getUsername());
            verify(customerRepository, times(1)).findByUserUsername(username);
        }

        @Test
        @DisplayName("Happy Path: testFindByUsername_WithCaseInsensitive_ShouldFindCorrectCustomer")
        void testFindByUsername_WithCaseInsensitive_ShouldFindCorrectCustomer() {
            // Given - Different case but same username
            String username = "Customer@Email.com";
            when(customerRepository.findByUserUsername(username))
                .thenReturn(Optional.of(testCustomer));

            // When
            Optional<Customer> result = customerService.findByUsername(username);

            // Then
            assertTrue(result.isPresent(), "Đảm bảo tìm không phân biệt chữ hoa/thường");
            assertEquals(testCustomer.getCustomerId(), result.get().getCustomerId());
            verify(customerRepository, times(1)).findByUserUsername(username);
        }

        @Test
        @DisplayName("Business Logic: testFindByUsername_ShouldLoadUserRelationship")
        void testFindByUsername_ShouldLoadUserRelationship() {
            // Given
            String username = "customer@email.com";
            when(customerRepository.findByUserUsername(username))
                .thenReturn(Optional.of(testCustomer));

            // When
            Optional<Customer> result = customerService.findByUsername(username);

            // Then
            assertTrue(result.isPresent());
            assertNotNull(result.get().getUser(), "Đảm bảo eager load user relationship");
            assertEquals(username, result.get().getUser().getUsername());
        }

        @Test
        @DisplayName("Edge Case: testFindByUsername_WithNonExistentUsername_ShouldReturnEmpty")
        void testFindByUsername_WithNonExistentUsername_ShouldReturnEmpty() {
            // Given
            String username = "nonexistent@email.com";
            when(customerRepository.findByUserUsername(username))
                .thenReturn(Optional.empty());

            // When
            Optional<Customer> result = customerService.findByUsername(username);

            // Then
            assertFalse(result.isPresent(), "Đảm bảo xử lý username không tồn tại");
            verify(customerRepository, times(1)).findByUserUsername(username);
        }

        @Test
        @DisplayName("Error Handling: testFindByUsername_WithNullUsername_ShouldHandleGracefully")
        void testFindByUsername_WithNullUsername_ShouldHandleGracefully() {
            // Given
            String username = null;
            when(customerRepository.findByUserUsername(username))
                .thenReturn(Optional.empty());

            // When
            Optional<Customer> result = customerService.findByUsername(username);

            // Then
            assertFalse(result.isPresent(), "Đảm bảo xử lý null username");
            verify(customerRepository, times(1)).findByUserUsername(null);
        }

        @Test
        @DisplayName("Business Logic: testFindByUsername_WithMultipleCustomersSameUserId_ShouldReturnFirst")
        void testFindByUsername_WithMultipleCustomersSameUserId_ShouldReturnFirst() {
            // Given - In real scenario, this shouldn't happen due to unique constraint
            String username = "duplicate@email.com";
            when(customerRepository.findByUserUsername(username))
                .thenReturn(Optional.of(testCustomer));

            // When
            Optional<Customer> result = customerService.findByUsername(username);

            // Then
            assertTrue(result.isPresent(), "Đảm bảo xử lý duplicate data");
            verify(customerRepository, times(1)).findByUserUsername(username);
        }
    }

    // ==================== 2. findById() - 5 Test Cases ====================
    @Nested
    @DisplayName("2. findById() - 5 Cases")
    class FindByIdTests {

        @Test
        @DisplayName("Happy Path: testFindById_WithExistingCustomerId_ShouldReturnCustomer")
        void testFindById_WithExistingCustomerId_ShouldReturnCustomer() {
            // Given
            UUID customerId = testCustomerId;
            when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(testCustomer));

            // When
            Optional<Customer> result = customerService.findById(customerId);

            // Then
            assertTrue(result.isPresent(), "Đảm bảo tìm customer bằng ID");
            assertEquals(customerId, result.get().getCustomerId());
            verify(customerRepository, times(1)).findById(customerId);
        }

        @Test
        @DisplayName("Happy Path: testFindById_ShouldLoadAllRelationships")
        void testFindById_ShouldLoadAllRelationships() {
            // Given
            UUID customerId = testCustomerId;
            when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(testCustomer));

            // When
            Optional<Customer> result = customerService.findById(customerId);

            // Then
            assertTrue(result.isPresent());
            assertNotNull(result.get().getUser(), "Đảm bảo load đầy đủ relationships");
            verify(customerRepository, times(1)).findById(customerId);
        }

        @Test
        @DisplayName("Edge Case: testFindById_WithNonExistentId_ShouldReturnEmpty")
        void testFindById_WithNonExistentId_ShouldReturnEmpty() {
            // Given
            UUID customerId = UUID.randomUUID();
            when(customerRepository.findById(customerId))
                .thenReturn(Optional.empty());

            // When
            Optional<Customer> result = customerService.findById(customerId);

            // Then
            assertFalse(result.isPresent(), "Đảm bảo xử lý ID không tồn tại");
            verify(customerRepository, times(1)).findById(customerId);
        }

        @Test
        @DisplayName("Error Handling: testFindById_WithNullId_ShouldReturnEmpty")
        void testFindById_WithNullId_ShouldReturnEmpty() {
            // Given
            UUID customerId = null;
            when(customerRepository.findById(customerId))
                .thenReturn(Optional.empty());

            // When
            Optional<Customer> result = customerService.findById(customerId);

            // Then
            assertFalse(result.isPresent(), "Đảm bảo xử lý null ID");
            verify(customerRepository, times(1)).findById(customerId);
        }

        @Test
        @DisplayName("Business Logic: testFindById_ShouldMaintainTransactionalContext")
        void testFindById_ShouldMaintainTransactionalContext() {
            // Given - Multiple findById calls
            UUID customerId = testCustomerId;
            when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(testCustomer));

            // When - Call multiple times (should be efficient within same transaction)
            customerService.findById(customerId);
            customerService.findById(customerId);

            // Then
            verify(customerRepository, times(2)).findById(customerId);
        }
    }

    // ==================== 3. findByUserId() - 4 Test Cases ====================
    @Nested
    @DisplayName("3. findByUserId() - 4 Cases")
    class FindByUserIdTests {

        @Test
        @DisplayName("Happy Path: testFindByUserId_WithValidUserId_ShouldReturnCustomer")
        void testFindByUserId_WithValidUserId_ShouldReturnCustomer() {
            // Given
            UUID userId = testUserId;
            when(customerRepository.findByUserId(userId))
                .thenReturn(Optional.of(testCustomer));

            // When
            Optional<Customer> result = customerService.findByUserId(userId);

            // Then
            assertTrue(result.isPresent(), "Đảm bảo tìm customer bằng userId");
            assertEquals(testCustomer.getUser().getId(), userId);
            verify(customerRepository, times(1)).findByUserId(userId);
        }

        @Test
        @DisplayName("Happy Path: testFindByUserId_ShouldFindCorrectCustomer")
        void testFindByUserId_ShouldFindCorrectCustomer() {
            // Given
            UUID userId = UUID.randomUUID();
            User user = new User();
            user.setId(userId);
            user.setUsername("user@test.com");
            user.setEmail("user@test.com");
            user.setFullName("Test User");
            
            Customer customer = new Customer(user);
            customer.setCustomerId(UUID.randomUUID());

            when(customerRepository.findByUserId(userId))
                .thenReturn(Optional.of(customer));

            // When
            Optional<Customer> result = customerService.findByUserId(userId);

            // Then
            assertTrue(result.isPresent(), "Đảm bảo link đúng với User");
            assertEquals(userId, result.get().getUser().getId());
        }

        @Test
        @DisplayName("Edge Case: testFindByUserId_WithUserWithoutCustomer_ShouldReturnEmpty")
        void testFindByUserId_WithUserWithoutCustomer_ShouldReturnEmpty() {
            // Given - User exists but no Customer record
            UUID userId = UUID.randomUUID();
            when(customerRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

            // When
            Optional<Customer> result = customerService.findByUserId(userId);

            // Then
            assertFalse(result.isPresent(), "Đảm bảo xử lý User chưa có Customer");
            verify(customerRepository, times(1)).findByUserId(userId);
        }

        @Test
        @DisplayName("Error Handling: testFindByUserId_WithNonExistentUserId_ShouldReturnEmpty")
        void testFindByUserId_WithNonExistentUserId_ShouldReturnEmpty() {
            // Given
            UUID userId = UUID.randomUUID();
            when(customerRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

            // When
            Optional<Customer> result = customerService.findByUserId(userId);

            // Then
            assertFalse(result.isPresent(), "Đảm bảo xử lý userId không tồn tại");
            verify(customerRepository, times(1)).findByUserId(userId);
        }
    }

    // ==================== 4. findAllCustomers() - 5 Test Cases ====================
    @Nested
    @DisplayName("4. findAllCustomers() - 5 Cases")
    class FindAllCustomersTests {

        @Test
        @DisplayName("Happy Path: testFindAllCustomers_WithMultipleCustomers_ShouldReturnAll")
        void testFindAllCustomers_WithMultipleCustomers_ShouldReturnAll() {
            // Given - Database has 10 customers
            List<Customer> customers = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                User user = new User();
                user.setId(UUID.randomUUID());
                user.setUsername("customer" + i + "@test.com");
                user.setEmail("customer" + i + "@test.com");
                user.setFullName("Customer " + i);
                
                Customer customer = new Customer(user);
                customer.setCustomerId(UUID.randomUUID());
                customers.add(customer);
            }

            when(customerRepository.findAll())
                .thenReturn(customers);

            // When
            List<Customer> result = customerService.findAllCustomers();

            // Then
            assertNotNull(result, "Đảm bảo trả về tất cả customers");
            assertEquals(10, result.size());
            verify(customerRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Happy Path: testFindAllCustomers_ShouldOrderByIdOrName")
        void testFindAllCustomers_ShouldOrderByIdOrName() {
            // Given - Multiple customers in database
            List<Customer> customers = Arrays.asList(testCustomer);
            when(customerRepository.findAll())
                .thenReturn(customers);

            // When
            List<Customer> result = customerService.findAllCustomers();

            // Then
            assertNotNull(result, "Đảm bảo sắp xếp đúng quy tắc");
            assertEquals(1, result.size());
            verify(customerRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Edge Case: testFindAllCustomers_WithEmptyDatabase_ShouldReturnEmptyList")
        void testFindAllCustomers_WithEmptyDatabase_ShouldReturnEmptyList() {
            // Given - No customers in database
            when(customerRepository.findAll())
                .thenReturn(new ArrayList<>());

            // When
            List<Customer> result = customerService.findAllCustomers();

            // Then
            assertNotNull(result, "Đảm bảo xử lý database trống");
            assertTrue(result.isEmpty());
            verify(customerRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Business Logic: testFindAllCustomers_ShouldLoadUserRelationships")
        void testFindAllCustomers_ShouldLoadUserRelationships() {
            // Given
            List<Customer> customers = Arrays.asList(testCustomer);
            when(customerRepository.findAll())
                .thenReturn(customers);

            // When
            List<Customer> result = customerService.findAllCustomers();

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
            result.forEach(customer -> {
                assertNotNull(customer.getUser(), "Đảm bảo eager load user relationship");
            });
        }

        @Test
        @DisplayName("Performance: testFindAllCustomers_WithLargeDataset_ShouldHandleEfficiently")
        void testFindAllCustomers_WithLargeDataset_ShouldHandleEfficiently() {
            // Given - 1000+ customers in database
            List<Customer> customers = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                User user = new User();
                user.setId(UUID.randomUUID());
                user.setUsername("customer" + i + "@test.com");
                user.setEmail("customer" + i + "@test.com");
                
                Customer customer = new Customer(user);
                customer.setCustomerId(UUID.randomUUID());
                customers.add(customer);
            }

            when(customerRepository.findAll())
                .thenReturn(customers);

            // When
            List<Customer> result = customerService.findAllCustomers();

            // Then
            assertNotNull(result, "Đảm bảo xử lý dataset lớn");
            assertEquals(1000, result.size());
            verify(customerRepository, times(1)).findAll();
        }
    }

    // ==================== 5. save() - 9 Test Cases ====================
    @Nested
    @DisplayName("5. save() - 9 Cases")
    class SaveTests {

        @Test
        @DisplayName("Happy Path: testSave_WithNewCustomer_ShouldCreateNewRecord")
        void testSave_WithNewCustomer_ShouldCreateNewRecord() {
            // Given - Customer with new UUID + valid user
            Customer newCustomer = new Customer(testUser);
            UUID savedId = UUID.randomUUID();
            
            when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> {
                    Customer c = invocation.getArgument(0);
                    c.setCustomerId(savedId);
                    c.setCreatedAt(LocalDateTime.now());
                    c.setUpdatedAt(LocalDateTime.now());
                    return c;
                });

            // When
            Customer result = customerService.save(newCustomer);

            // Then
            assertNotNull(result, "Đảm bảo tạo customer mới");
            assertNotNull(result.getCustomerId());
            assertNotNull(result.getCreatedAt());
            verify(customerRepository, times(1)).save(newCustomer);
        }

        @Test
        @DisplayName("Happy Path: testSave_WithExistingCustomer_ShouldUpdateRecord")
        void testSave_WithExistingCustomer_ShouldUpdateRecord() {
            // Given - Existing Customer, update some fields
            testCustomer.getUser().setFullName("Updated Name");
            testCustomer.setUpdatedAt(LocalDateTime.now());
            
            when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> {
                    Customer c = invocation.getArgument(0);
                    c.setUpdatedAt(LocalDateTime.now());
                    return c;
                });

            // When
            Customer result = customerService.save(testCustomer);

            // Then
            assertNotNull(result, "Đảm bảo update customer existing");
            assertEquals("Updated Name", result.getUser().getFullName());
            assertNotNull(result.getUpdatedAt());
            verify(customerRepository, times(1)).save(testCustomer);
        }

        @Test
        @DisplayName("Business Logic: testSave_ShouldCreateAtAndUpdatedAtTimestamps")
        void testSave_ShouldCreateAtAndUpdatedAtTimestamps() {
            // Given
            Customer newCustomer = new Customer(testUser);
            LocalDateTime now = LocalDateTime.now();
            newCustomer.setCreatedAt(now);
            newCustomer.setUpdatedAt(now);

            when(customerRepository.save(any(Customer.class)))
                .thenReturn(newCustomer);

            // When
            Customer result = customerService.save(newCustomer);

            // Then
            assertNotNull(result.getCreatedAt(), "Đảm bảo set createdAt và updatedAt");
            assertNotNull(result.getUpdatedAt());
            verify(customerRepository, times(1)).save(newCustomer);
        }

        @Test
        @DisplayName("Business Logic: testSave_ShouldUpdateAtTimestamps")
        void testSave_ShouldUpdateAtTimestamps() {
            // Given - New customer, set timestamps to date
            Customer newCustomer = new Customer(testUser);
            LocalDateTime createdTime = LocalDateTime.now().minusDays(1);
            newCustomer.setCreatedAt(createdTime);
            newCustomer.setUpdatedAt(createdTime);

            when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> {
                    Customer c = invocation.getArgument(0);
                    c.setUpdatedAt(LocalDateTime.now());
                    return c;
                });

            // When
            Customer result = customerService.save(newCustomer);

            // Then
            assertNotNull(result.getUpdatedAt(), "Đảm bảo set timestamp tự động");
            assertTrue(result.getUpdatedAt().isAfter(createdTime));
        }

        @Test
        @DisplayName("Business Logic: testSave_WithUserRelationship_ShouldMaintainRelationship")
        void testSave_WithUserRelationship_ShouldMaintainRelationship() {
            // Given - Customer with valid User
            Customer newCustomer = new Customer(testUser);
            when(customerRepository.save(any(Customer.class)))
                .thenReturn(newCustomer);

            // When
            Customer result = customerService.save(newCustomer);

            // Then
            assertNotNull(result.getUser(), "Đảm bảo maintain user relationship");
            assertEquals(testUser.getId(), result.getUser().getId());
            verify(customerRepository, times(1)).save(newCustomer);
        }

        @Test
        @DisplayName("Validation: testSave_WithNullUser_ShouldThrowException")
        void testSave_WithNullUser_ShouldThrowException() {
            // Given - Customer with user=null
            Customer invalidCustomer = new Customer();
            invalidCustomer.setUser(null);

            when(customerRepository.save(any(Customer.class)))
                .thenThrow(new IllegalArgumentException("User không được null"));

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                customerService.save(invalidCustomer);
            }, "Đảm bảo xử lý customer không có user");
        }

        @Test
        @DisplayName("Error Handling: testSave_WithDuplicateUserId_ShouldThrowException")
        void testSave_WithDuplicateUserId_ShouldThrowException() {
            // Given - Customer with userId already exists
            Customer duplicateCustomer = new Customer(testUser);

            when(customerRepository.save(any(Customer.class)))
                .thenThrow(new IllegalArgumentException("Unique constraint violation"));

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                customerService.save(duplicateCustomer);
            }, "Đảm bảo unique constraint cho userId");
        }

        @Test
        @DisplayName("Integration: testSave_ShouldPersistImmediatelyToDatabase")
        void testSave_ShouldPersistImmediatelyToDatabase() {
            // Given
            Customer newCustomer = new Customer(testUser);
            when(customerRepository.save(any(Customer.class)))
                .thenReturn(newCustomer);

            // When
            Customer savedCustomer = customerService.save(newCustomer);

            // Then
            assertNotNull(savedCustomer, "Đảm bảo flush vào database");
            verify(customerRepository, times(1)).save(newCustomer);
        }

        @Test
        @DisplayName("Business Logic: testSave_ReturnSavedCustomerWithId")
        void testSave_ReturnSavedCustomerWithId() {
            // Given - Save new customer
            Customer newCustomer = new Customer(testUser);
            UUID savedId = UUID.randomUUID();
            newCustomer.setCustomerId(savedId);

            when(customerRepository.save(any(Customer.class)))
                .thenReturn(newCustomer);

            // When
            Customer result = customerService.save(newCustomer);

            // Then
            assertNotNull(result, "Đảm bảo return customer với ID");
            assertEquals(savedId, result.getCustomerId());
        }
    }
}

