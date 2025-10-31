package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.booking.common.enums.WithdrawalStatus;
import com.example.booking.domain.RestaurantBalance;
import com.example.booking.domain.RestaurantBankAccount;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.WithdrawalRequest;
import com.example.booking.dto.payout.CreateWithdrawalRequestDto;
import com.example.booking.dto.payout.ManualPayDto;
import com.example.booking.dto.payout.WithdrawalRequestDto;
import com.example.booking.exception.BadRequestException;
import com.example.booking.exception.ResourceNotFoundException;
import com.example.booking.repository.RestaurantBalanceRepository;
import com.example.booking.repository.RestaurantBankAccountRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.WithdrawalRequestRepository;

import java.util.List;
import java.util.ArrayList;

/**
 * Unit tests for WithdrawalService
 * 
 * Test Coverage:
 * 1. requestWithdrawal() - 8+ test cases
 * 2. processWithdrawal() (approveWithdrawal) - 6+ test cases
 * 3. rejectWithdrawal() - 4+ test cases
 * 4. getWithdrawalHistory() (getWithdrawalsByRestaurant) - 3+ test cases
 * 
 * Total: 21+ test cases
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WithdrawalService Tests")
public class WithdrawalServiceTest {

    @Mock
    private WithdrawalRequestRepository withdrawalRepository;
    
    @Mock
    private RestaurantBankAccountRepository bankAccountRepository;
    
    @Mock
    private RestaurantProfileRepository restaurantRepository;
    
    @Mock
    private RestaurantBalanceRepository balanceRepository;
    
    @Mock
    private RestaurantBalanceService balanceService;
    
    @Mock
    private WithdrawalNotificationService notificationService;
    
    @InjectMocks
    private WithdrawalService withdrawalService;
    
    // Test data
    private Integer restaurantId;
    private Integer bankAccountId;
    private UUID adminUserId;
    private RestaurantProfile restaurant;
    private RestaurantBankAccount bankAccount;
    private RestaurantBalance balance;
    private CreateWithdrawalRequestDto createDto;
    private WithdrawalRequest withdrawalRequest;
    
    @BeforeEach
    void setUp() {
        restaurantId = 1;
        bankAccountId = 10;
        adminUserId = UUID.randomUUID();
        
        // Setup restaurant
        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(restaurantId);
        restaurant.setRestaurantName("Test Restaurant");
        
        // Setup owner
        User ownerUser = new User();
        ownerUser.setId(UUID.randomUUID());
        ownerUser.setFullName("Test Owner");
        ownerUser.setEmail("owner@test.com");
        
        RestaurantOwner owner = new RestaurantOwner();
        owner.setUser(ownerUser);
        restaurant.setOwner(owner);
        
        // Setup bank account
        bankAccount = new RestaurantBankAccount();
        bankAccount.setAccountId(bankAccountId);
        bankAccount.setRestaurant(restaurant);
        bankAccount.setAccountNumber("1234567890");
        bankAccount.setAccountHolderName("Test Owner");
        bankAccount.setBankCode("VCB");
        bankAccount.setBankName("Vietcombank");
        
        // Setup balance
        balance = new RestaurantBalance();
        balance.setBalanceId(1);
        balance.setRestaurant(restaurant);
        balance.setTotalRevenue(new BigDecimal("1000000")); // 1M VND
        balance.setAvailableBalance(new BigDecimal("1000000"));
        balance.setPendingWithdrawal(BigDecimal.ZERO);
        
        // Setup CreateWithdrawalRequestDto
        createDto = new CreateWithdrawalRequestDto();
        createDto.setBankAccountId(bankAccountId);
        createDto.setAmount(new BigDecimal("500000")); // 500k VND
        createDto.setDescription("Test withdrawal");
        
        // Setup WithdrawalRequest
        withdrawalRequest = new WithdrawalRequest();
        withdrawalRequest.setRequestId(1);
        withdrawalRequest.setRestaurant(restaurant);
        withdrawalRequest.setBankAccount(bankAccount);
        withdrawalRequest.setAmount(new BigDecimal("500000"));
        withdrawalRequest.setDescription("Test withdrawal");
        withdrawalRequest.setStatus(WithdrawalStatus.PENDING);
        withdrawalRequest.setCommissionAmount(BigDecimal.ZERO);
        withdrawalRequest.setNetAmount(new BigDecimal("500000"));
    }
    
    // ==================== 1. requestWithdrawal() - 8+ Cases ====================
    
    @Nested
    @DisplayName("1. requestWithdrawal() - Create Withdrawal Tests")
    class RequestWithdrawalTests {
        
        @Test
        @DisplayName("Test 1: Happy Path - With Sufficient Balance, Should Create Request")
        void testRequestWithdrawal_WithSufficientBalance_ShouldCreateRequest() {
            // Arrange
            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(bankAccountRepository.findById(bankAccountId)).thenReturn(Optional.of(bankAccount));
            when(balanceService.getOrCreateBalance(restaurantId)).thenReturn(balance);
            when(withdrawalRepository.countByRestaurantIdAndDateRange(eq(restaurantId), any(), any())).thenReturn(0L);
            when(withdrawalRepository.save(any(WithdrawalRequest.class))).thenReturn(withdrawalRequest);
            
            // Act
            WithdrawalRequestDto result = withdrawalService.createWithdrawal(restaurantId, createDto);
            
            // Assert
            assertNotNull(result);
            assertEquals(WithdrawalStatus.PENDING, result.getStatus());
            verify(withdrawalRepository).save(any(WithdrawalRequest.class));
            verify(notificationService).notifyWithdrawalCreated(any(WithdrawalRequest.class));
        }
        
        @Test
        @DisplayName("Test 2: Happy Path - Should Calculate Net Amount")
        void testRequestWithdrawal_ShouldCalculateNetAmount() {
            // Arrange
            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(bankAccountRepository.findById(bankAccountId)).thenReturn(Optional.of(bankAccount));
            when(balanceService.getOrCreateBalance(restaurantId)).thenReturn(balance);
            when(withdrawalRepository.countByRestaurantIdAndDateRange(eq(restaurantId), any(), any())).thenReturn(0L);
            when(withdrawalRepository.save(any(WithdrawalRequest.class))).thenReturn(withdrawalRequest);
            
            createDto.setAmount(new BigDecimal("500000"));
            createDto.setDescription(null); // Test default description
            
            // Act
            WithdrawalRequestDto result = withdrawalService.createWithdrawal(restaurantId, createDto);
            
            // Assert
            assertNotNull(result);
            assertEquals(new BigDecimal("500000"), result.getNetAmount());
        }
        
        @Test
        @DisplayName("Test 3: Business Logic - Minimum Amount Should Succeed")
        void testRequestWithdrawal_WithMinimumAmount_ShouldSucceed() {
            // Arrange
            createDto.setAmount(new BigDecimal("100000")); // Minimum = 100k
            
            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(bankAccountRepository.findById(bankAccountId)).thenReturn(Optional.of(bankAccount));
            when(balanceService.getOrCreateBalance(restaurantId)).thenReturn(balance);
            when(withdrawalRepository.countByRestaurantIdAndDateRange(eq(restaurantId), any(), any())).thenReturn(0L);
            
            withdrawalRequest.setAmount(new BigDecimal("100000"));
            withdrawalRequest.setNetAmount(new BigDecimal("100000"));
            when(withdrawalRepository.save(any(WithdrawalRequest.class))).thenReturn(withdrawalRequest);
            
            // Act
            WithdrawalRequestDto result = withdrawalService.createWithdrawal(restaurantId, createDto);
            
            // Assert
            assertNotNull(result);
            assertEquals(WithdrawalStatus.PENDING, result.getStatus());
        }
        
        @Test
        @DisplayName("Test 4: Validation - Insufficient Balance, Should Throw Exception")
        void testRequestWithdrawal_WithInsufficientBalance_ShouldThrowException() {
            // Arrange
            balance.setAvailableBalance(new BigDecimal("50000")); // Less than requested
            balance.setPendingWithdrawal(BigDecimal.ZERO);
            balance.setTotalRevenue(new BigDecimal("50000"));
            
            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(bankAccountRepository.findById(bankAccountId)).thenReturn(Optional.of(bankAccount));
            when(balanceService.getOrCreateBalance(restaurantId)).thenReturn(balance);
            
            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                withdrawalService.createWithdrawal(restaurantId, createDto);
            });
            
            assertTrue(exception.getMessage().contains("Số dư không đủ"));
        }

        @Test
        @DisplayName("Test 4b: Validation - Wrong Restaurant (Bank Account), Should Throw Exception")
        void testRequestWithdrawal_WithWrongRestaurant_ShouldThrowException() {
            // Arrange
            RestaurantProfile otherRestaurant = new RestaurantProfile();
            otherRestaurant.setRestaurantId(999);

            RestaurantBankAccount wrongBankAccount = new RestaurantBankAccount();
            wrongBankAccount.setAccountId(bankAccountId);
            wrongBankAccount.setRestaurant(otherRestaurant); // Bank account belongs to different restaurant

            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(bankAccountRepository.findById(bankAccountId)).thenReturn(Optional.of(wrongBankAccount));

            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                withdrawalService.createWithdrawal(restaurantId, createDto);
            });

            assertTrue(exception.getMessage().contains("không thuộc nhà hàng này"));
        }

        @Test
        @DisplayName("Test 4c: Validation - Bank Account Not Found, Should Throw Exception")
        void testRequestWithdrawal_WithBankAccountNotFound_ShouldThrowException() {
            // Arrange
            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(bankAccountRepository.findById(bankAccountId)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                withdrawalService.createWithdrawal(restaurantId, createDto);
            });

            assertTrue(exception.getMessage().contains("tài khoản ngân hàng"));
        }

        @Test
        @DisplayName("Test 4d: Validation - Restaurant Not Found, Should Throw Exception")
        void testRequestWithdrawal_WithRestaurantNotFound_ShouldThrowException() {
            // Arrange
            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                withdrawalService.createWithdrawal(restaurantId, createDto);
            });

            assertTrue(exception.getMessage().contains("nhà hàng"));
        }

        @Test
        @DisplayName("Test 5: Validation - Below Minimum Amount, Should Throw Exception")
        void testRequestWithdrawal_BelowMinimumAmount_ShouldThrowException() {
            // Arrange
            createDto.setAmount(new BigDecimal("50000")); // Below 100k minimum
            
            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                withdrawalService.createWithdrawal(restaurantId, createDto);
            });
            
            assertTrue(exception.getMessage().contains("tối thiểu"));
        }
        
        @Test
        @DisplayName("Test 6: Business Logic - Exceed Daily Limit, Should Throw Exception")
        void testRequestWithdrawal_ExceedDailyLimit_ShouldThrowException() {
            // Arrange  
            when(withdrawalRepository.countByRestaurantIdAndDateRange(eq(restaurantId), any(), any()))
                .thenReturn(3L); // Already 3 withdrawals today (limit reached)
            
            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                withdrawalService.createWithdrawal(restaurantId, createDto);
            });
            
            assertTrue(exception.getMessage().contains("giới hạn"));
        }
        
        @Test
        @DisplayName("Test 7: Business Logic - Should Send Notification")
        void testRequestWithdrawal_ShouldSendNotification() {
            // Arrange
            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(bankAccountRepository.findById(bankAccountId)).thenReturn(Optional.of(bankAccount));
            when(balanceService.getOrCreateBalance(restaurantId)).thenReturn(balance);
            when(withdrawalRepository.countByRestaurantIdAndDateRange(eq(restaurantId), any(), any())).thenReturn(0L);
            when(withdrawalRepository.save(any(WithdrawalRequest.class))).thenReturn(withdrawalRequest);
            
            // Act
            withdrawalService.createWithdrawal(restaurantId, createDto);
            
            // Assert
            verify(notificationService).notifyWithdrawalCreated(any(WithdrawalRequest.class));
        }
        
        @Test
        @DisplayName("Test 8: Error Handling - Invalid Bank Account, Should Throw Exception")
        void testRequestWithdrawal_WithInvalidBankAccount_ShouldThrowException() {
            // Arrange
            RestaurantProfile anotherRestaurant = new RestaurantProfile();
            anotherRestaurant.setRestaurantId(999);
            RestaurantBankAccount invalidBankAccount = new RestaurantBankAccount();
            invalidBankAccount.setAccountId(bankAccountId);
            invalidBankAccount.setRestaurant(anotherRestaurant); // Bank account belongs to different restaurant
            
            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(bankAccountRepository.findById(bankAccountId)).thenReturn(Optional.of(invalidBankAccount));
            when(balanceService.getOrCreateBalance(restaurantId)).thenReturn(balance);
            
            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                withdrawalService.createWithdrawal(restaurantId, createDto);
            });
            
            assertTrue(exception.getMessage().contains("không thuộc nhà hàng"));
        }
        
        @Test
        @DisplayName("Test 9: Error Handling - Non-Existent Restaurant, Should Throw Exception")
        void testRequestWithdrawal_WithNonExistentRestaurant_ShouldThrowException() {
            // Arrange
            when(balanceService.getOrCreateBalance(restaurantId)).thenReturn(balance);
            when(withdrawalRepository.countByRestaurantIdAndDateRange(eq(restaurantId), any(), any())).thenReturn(0L);
            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThrows(ResourceNotFoundException.class, () -> {
                withdrawalService.createWithdrawal(restaurantId, createDto);
            });
        }
    }
    
    // ==================== 2. processWithdrawal() - 6+ Cases ====================
    
    @Nested
    @DisplayName("2. processWithdrawal() (approveWithdrawal) - Approve Withdrawal Tests")
    class ProcessWithdrawalTests {
        
        @Test
        @DisplayName("Test 11: Happy Path - With Pending Request, Should Approve Successfully")
        void testProcessWithdrawal_WithPendingRequest_ShouldApproveSuccessfully() {
            // Arrange
            Integer requestId = 1;
            String adminNotes = "Approved by admin";
            
            when(withdrawalRepository.findByIdForUpdate(requestId)).thenReturn(Optional.of(withdrawalRequest));
            when(balanceRepository.findByRestaurantIdForUpdate(restaurantId)).thenReturn(Optional.of(balance));
            when(withdrawalRepository.save(any(WithdrawalRequest.class))).thenReturn(withdrawalRequest);
            
            // Act
            WithdrawalRequestDto result = withdrawalService.approveWithdrawal(requestId, adminUserId, adminNotes);
            
            // Assert
            assertNotNull(result);
            assertEquals(WithdrawalStatus.APPROVED, result.getStatus());
            verify(balanceRepository).save(any(RestaurantBalance.class));
            verify(notificationService).notifyWithdrawalApproved(any(WithdrawalRequest.class));
        }
        
        @Test
        @DisplayName("Test 12: Happy Path - Should Lock Restaurant Balance")
        void testProcessWithdrawal_ShouldLockRestaurantBalance() {
            // Arrange
            Integer requestId = 1;
            String adminNotes = "Approved";
            
            when(withdrawalRepository.findByIdForUpdate(requestId)).thenReturn(Optional.of(withdrawalRequest));
            when(balanceRepository.findByRestaurantIdForUpdate(restaurantId)).thenReturn(Optional.of(balance));
            when(withdrawalRepository.save(any(WithdrawalRequest.class))).thenReturn(withdrawalRequest);
            
            // Act
            withdrawalService.approveWithdrawal(requestId, adminUserId, adminNotes);
            
            // Assert
            verify(balanceRepository).save(argThat(bal -> 
                bal.getPendingWithdrawal().compareTo(BigDecimal.ZERO) > 0
            ));
        }
        
        @Test
        @DisplayName("Test 13: Business Logic - Insufficient Balance, Should Throw Exception")
        void testProcessWithdrawal_WithInsufficientBalance_ShouldThrowException() {
            // Arrange
            Integer requestId = 1;
            balance.setTotalRevenue(new BigDecimal("100000"));
            balance.setAvailableBalance(new BigDecimal("100000")); // Less than withdrawal amount (500k)
            balance.setPendingWithdrawal(BigDecimal.ZERO);
            
            when(withdrawalRepository.findByIdForUpdate(requestId)).thenReturn(Optional.of(withdrawalRequest));
            when(balanceRepository.findByRestaurantIdForUpdate(restaurantId)).thenReturn(Optional.of(balance));
            
            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                withdrawalService.approveWithdrawal(requestId, adminUserId, "Approve");
            });
            
            assertTrue(exception.getMessage().contains("Số dư không đủ"));
        }
        
        @Test
        @DisplayName("Test 14: Business Logic - Should Use Pessimistic Locking")
        void testProcessWithdrawal_ShouldUsePessimisticLocking() {
            // Arrange
            Integer requestId = 1;
            
            when(withdrawalRepository.findByIdForUpdate(requestId)).thenReturn(Optional.of(withdrawalRequest));
            when(balanceRepository.findByRestaurantIdForUpdate(restaurantId)).thenReturn(Optional.of(balance));
            when(withdrawalRepository.save(any(WithdrawalRequest.class))).thenReturn(withdrawalRequest);
            
            // Act
            withdrawalService.approveWithdrawal(requestId, adminUserId, "Notes");
            
            // Assert
            verify(withdrawalRepository).findByIdForUpdate(requestId);
            verify(balanceRepository).findByRestaurantIdForUpdate(restaurantId);
        }
        
        @Test
        @DisplayName("Test 16: Validation - Cannot Approve Non-PENDING Status, Should Throw Exception")
        void testProcessWithdrawal_WithNonPendingStatus_ShouldThrowException() {
            // Arrange
            Integer requestId = 1;
            withdrawalRequest.setStatus(WithdrawalStatus.APPROVED); // Already approved
            
            when(withdrawalRepository.findByIdForUpdate(requestId)).thenReturn(Optional.of(withdrawalRequest));
            
            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                withdrawalService.approveWithdrawal(requestId, adminUserId, "Notes");
            });
            
            assertTrue(exception.getMessage().contains("không thể duyệt"));
        }
        
        @Test
        @DisplayName("Test 18: Integration - Should Update Admin Notes")
        void testProcessWithdrawal_ShouldUpdateAdminNotes() {
            // Arrange
            Integer requestId = 1;
            String adminNotes = "Approved with special review";
            
            when(withdrawalRepository.findByIdForUpdate(requestId)).thenReturn(Optional.of(withdrawalRequest));
            when(balanceRepository.findByRestaurantIdForUpdate(restaurantId)).thenReturn(Optional.of(balance));
            when(withdrawalRepository.save(any(WithdrawalRequest.class))).thenAnswer(invocation -> {
                WithdrawalRequest req = invocation.getArgument(0);
                assertNotNull(req.getAdminNotes());
                assertEquals(adminNotes, req.getAdminNotes());
                assertNotNull(req.getReviewedAt());
                assertNotNull(req.getReviewedByUserId());
                return req;
            });
            
            // Act
            withdrawalService.approveWithdrawal(requestId, adminUserId, adminNotes);
            
            // Assert
            verify(withdrawalRepository).save(any(WithdrawalRequest.class));
        }
    }
    
    // ==================== 3. rejectWithdrawal() - 4+ Cases ====================
    
    @Nested
    @DisplayName("3. rejectWithdrawal() - Reject Withdrawal Tests")
    class RejectWithdrawalTests {
        
        @Test
        @DisplayName("Test 19: Happy Path - With Pending Request, Should Reject Successfully")
        void testRejectWithdrawal_WithPendingRequest_ShouldRejectSuccessfully() {
            // Arrange
            Integer requestId = 1;
            String rejectReason = "Insufficient documentation";
            
            when(withdrawalRepository.findByIdForUpdate(requestId)).thenReturn(Optional.of(withdrawalRequest));
            when(withdrawalRepository.save(any(WithdrawalRequest.class))).thenReturn(withdrawalRequest);
            
            // Act
            WithdrawalRequestDto result = withdrawalService.rejectWithdrawal(requestId, adminUserId, rejectReason);
            
            // Assert
            assertNotNull(result);
            assertEquals(WithdrawalStatus.REJECTED, result.getStatus());
            verify(withdrawalRepository).save(any(WithdrawalRequest.class));
            verify(notificationService).notifyWithdrawalRejected(any(WithdrawalRequest.class));
        }
        
        @Test
        @DisplayName("Test 20: Business Logic - Should Not Unlock Balance When Rejected")
        void testRejectWithdrawal_ShouldNotUnlockBalance() {
            // Arrange
            Integer requestId = 1;
            String rejectReason = "Invalid request";
            
            when(withdrawalRepository.findByIdForUpdate(requestId)).thenReturn(Optional.of(withdrawalRequest));
            when(withdrawalRepository.save(any(WithdrawalRequest.class))).thenReturn(withdrawalRequest);
            
            // Act
            withdrawalService.rejectWithdrawal(requestId, adminUserId, rejectReason);
            
            // Assert - Balance should not be updated for PENDING rejections
            verify(balanceRepository, never()).save(any(RestaurantBalance.class));
        }
        
        @Test
        @DisplayName("Test 21: Business Logic - Should Send Notification")
        void testRejectWithdrawal_ShouldSendNotification() {
            // Arrange
            Integer requestId = 1;
            String rejectReason = "Policy violation";
            
            when(withdrawalRepository.findByIdForUpdate(requestId)).thenReturn(Optional.of(withdrawalRequest));
            when(withdrawalRepository.save(any(WithdrawalRequest.class))).thenReturn(withdrawalRequest);
            
            // Act
            withdrawalService.rejectWithdrawal(requestId, adminUserId, rejectReason);
            
            // Assert
            verify(notificationService).notifyWithdrawalRejected(any(WithdrawalRequest.class));
        }
        
        @Test
        @DisplayName("Test 22: Validation - Cannot Reject Non-PENDING, Should Throw Exception")
        void testRejectWithdrawal_WithAlreadyApproved_ShouldThrowException() {
            // Arrange
            Integer requestId = 1;
            withdrawalRequest.setStatus(WithdrawalStatus.APPROVED);
            
            when(withdrawalRepository.findByIdForUpdate(requestId)).thenReturn(Optional.of(withdrawalRequest));
            
            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                withdrawalService.rejectWithdrawal(requestId, adminUserId, "Cannot reject");
            });
            
            assertTrue(exception.getMessage().contains("không thể từ chối"));
        }
        
        @Test
        @DisplayName("Test 23: Error Handling - Non-Existent Request, Should Throw Exception")
        void testRejectWithdrawal_WithNonExistentRequest_ShouldThrowException() {
            // Arrange
            Integer requestId = 999;
            
            when(withdrawalRepository.findByIdForUpdate(requestId)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThrows(ResourceNotFoundException.class, () -> {
                withdrawalService.rejectWithdrawal(requestId, adminUserId, "Reason");
            });
        }
        
        @Test
        @DisplayName("Test 25: Integration - Should Update Rejection Timestamps")
        void testRejectWithdrawal_ShouldUpdateRejectionTimestamps() {
            // Arrange
            Integer requestId = 1;
            String rejectReason = "Test reason";
            
            when(withdrawalRepository.findByIdForUpdate(requestId)).thenReturn(Optional.of(withdrawalRequest));
            when(withdrawalRepository.save(any(WithdrawalRequest.class))).thenAnswer(invocation -> {
                WithdrawalRequest req = invocation.getArgument(0);
                assertNotNull(req.getRejectionReason());
                assertNotNull(req.getReviewedAt());
                assertNotNull(req.getReviewedByUserId());
                return req;
            });
            
            // Act
            withdrawalService.rejectWithdrawal(requestId, adminUserId, rejectReason);
            
            // Assert
            verify(withdrawalRepository).save(any(WithdrawalRequest.class));
        }
    }
    
    // ==================== 4. markWithdrawalPaid() - Additional Coverage
    // ====================

    @Nested
    @DisplayName("4. markWithdrawalPaid() - Mark Withdrawal Paid Tests")
    class MarkWithdrawalPaidTests {

        @Test
        @DisplayName("Test 30: Happy Path - Should mark withdrawal as paid successfully")
        void testMarkWithdrawalPaid_WithValidRequest_ShouldMarkAsPaid() {
            // Arrange
            Integer requestId = 1;
            ManualPayDto dto = new ManualPayDto();
            dto.setTransferRef("TRANSFER123");
            dto.setNote("Manual transfer");
            dto.setProofUrl("https://proof.url");

            when(withdrawalRepository.findByIdForUpdate(requestId)).thenReturn(Optional.of(withdrawalRequest));
            when(balanceRepository.findByRestaurantIdForUpdate(restaurantId)).thenReturn(Optional.of(balance));
            when(withdrawalRepository.save(any(WithdrawalRequest.class))).thenReturn(withdrawalRequest);
            doNothing().when(notificationService).notifyWithdrawalSucceeded(any(WithdrawalRequest.class));

            // Act
            withdrawalService.markWithdrawalPaid(requestId, adminUserId, dto);

            // Assert
            verify(withdrawalRepository).save(any(WithdrawalRequest.class));
            verify(notificationService).notifyWithdrawalSucceeded(any(WithdrawalRequest.class));
        }

        @Test
        @DisplayName("Test 31: Validation - Withdrawal Not Found, Should Throw Exception")
        void testMarkWithdrawalPaid_WithWithdrawalNotFound_ShouldThrowException() {
            // Arrange
            Integer requestId = 999;
            ManualPayDto dto = new ManualPayDto();

            when(withdrawalRepository.findByIdForUpdate(requestId)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                withdrawalService.markWithdrawalPaid(requestId, adminUserId, dto);
            });

            assertTrue(exception.getMessage().contains("yêu cầu rút tiền"));
        }

        @Test
        @DisplayName("Test 32: Validation - Insufficient Balance, Should Throw Exception")
        void testMarkWithdrawalPaid_WithInsufficientBalance_ShouldThrowException() {
            // Arrange
            Integer requestId = 1;
            ManualPayDto dto = new ManualPayDto();
            dto.setTransferRef("TRANSFER123");

            balance.setAvailableBalance(new BigDecimal("30000")); // Less than withdrawal amount (500k)

            when(withdrawalRepository.findByIdForUpdate(requestId)).thenReturn(Optional.of(withdrawalRequest));
            when(balanceRepository.findByRestaurantIdForUpdate(restaurantId)).thenReturn(Optional.of(balance));

            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                withdrawalService.markWithdrawalPaid(requestId, adminUserId, dto);
            });

            assertTrue(exception.getMessage().contains("Số dư không đủ"));
        }

        @Test
        @DisplayName("Test 33: Validation - Invalid Status, Should Throw Exception")
        void testMarkWithdrawalPaid_WithInvalidStatus_ShouldThrowException() {
            // Arrange
            Integer requestId = 1;
            ManualPayDto dto = new ManualPayDto();
            dto.setTransferRef("TRANSFER123");

            withdrawalRequest.setStatus(WithdrawalStatus.REJECTED); // Cannot mark REJECTED as paid

            when(withdrawalRepository.findByIdForUpdate(requestId)).thenReturn(Optional.of(withdrawalRequest));

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                withdrawalService.markWithdrawalPaid(requestId, adminUserId, dto);
            });

            assertTrue(exception.getMessage().contains("không ở trạng thái có thể đánh dấu đã chi"));
        }

        @Test
        @DisplayName("Test 34: Validation - Balance Not Found, Should Throw Exception")
        void testMarkWithdrawalPaid_WithBalanceNotFound_ShouldThrowException() {
            // Arrange
            Integer requestId = 1;
            ManualPayDto dto = new ManualPayDto();
            dto.setTransferRef("TRANSFER123");

            when(withdrawalRepository.findByIdForUpdate(requestId)).thenReturn(Optional.of(withdrawalRequest));
            when(balanceRepository.findByRestaurantIdForUpdate(restaurantId)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                withdrawalService.markWithdrawalPaid(requestId, adminUserId, dto);
            });

            assertTrue(exception.getMessage().contains("số dư nhà hàng"));
        }
    }

    // ==================== 5. getWithdrawalHistory() - 3+ Cases
    // ====================
    
    @Nested
    @DisplayName("5. getWithdrawalHistory() (getWithdrawalsByRestaurant) - Get Withdrawal History Tests")
    class GetWithdrawalHistoryTests {
        
        @Test
        @DisplayName("Test 26: Happy Path - Valid Restaurant, Should Return All Requests")
        void testGetWithdrawalHistory_WithValidRestaurant_ShouldReturnAllRequests() {
            // Arrange
            List<WithdrawalRequest> requests = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                WithdrawalRequest req = new WithdrawalRequest();
                req.setRequestId(i + 1);
                req.setRestaurant(restaurant);
                req.setBankAccount(bankAccount);
                req.setAmount(new BigDecimal("100000"));
                req.setStatus(WithdrawalStatus.PENDING);
                requests.add(req);
            }
            
            Pageable pageable = PageRequest.of(0, 10);
            Page<WithdrawalRequest> page = new PageImpl<>(requests, pageable, requests.size());
            
            when(withdrawalRepository.findByRestaurantRestaurantId(restaurantId, pageable))
                .thenReturn(page);
            
            // Act
            Page<WithdrawalRequestDto> result = withdrawalService.getWithdrawalsByRestaurant(restaurantId, pageable);
            
            // Assert
            assertNotNull(result);
            assertEquals(10, result.getTotalElements());
            assertEquals(10, result.getContent().size());
        }
        
        @Test
        @DisplayName("Test 27: Happy Path - Returns Paginated Results")
        void testGetWithdrawalHistory_ReturnsPaginatedResults() {
            // Arrange
            List<WithdrawalRequest> requests = new ArrayList<>();
            requests.add(withdrawalRequest);
            
            Pageable pageable = PageRequest.of(0, 10);
            Page<WithdrawalRequest> page = new PageImpl<>(requests, pageable, 25); // Total 25 items
            
            when(withdrawalRepository.findByRestaurantRestaurantId(restaurantId, pageable))
                .thenReturn(page);
            
            // Act
            Page<WithdrawalRequestDto> result = withdrawalService.getWithdrawalsByRestaurant(restaurantId, pageable);
            
            // Assert
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals(25, result.getTotalElements());
            assertEquals(10, result.getSize());
        }
        
        @Test
        @DisplayName("Test 28: Business Logic - Filter By Restaurant")
        void testGetWithdrawalHistory_ShouldFilterByRestaurant() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            when(withdrawalRepository.findByRestaurantRestaurantId(restaurantId, pageable))
                .thenReturn(Page.empty());
            
            // Act
            withdrawalService.getWithdrawalsByRestaurant(restaurantId, pageable);
            
            // Assert
            verify(withdrawalRepository).findByRestaurantRestaurantId(restaurantId, pageable);
        }
        
        @Test
        @DisplayName("Test 30: Edge Case - No History, Should Return Empty Page")
        void testGetWithdrawalHistory_WithNoHistory_ShouldReturnEmptyPage() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            when(withdrawalRepository.findByRestaurantRestaurantId(restaurantId, pageable))
                .thenReturn(Page.empty());
            
            // Act
            Page<WithdrawalRequestDto> result = withdrawalService.getWithdrawalsByRestaurant(restaurantId, pageable);
            
            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            assertEquals(0, result.getTotalElements());
        }
    }

    // ==================== 5. getWithdrawalStats() Tests ====================

    @Nested
    @DisplayName("5. getWithdrawalStats() - Get Withdrawal Statistics Tests")
    class GetWithdrawalStatsTests {

        @Test
        @DisplayName("Test 31: Happy Path - Should Return Statistics")
        void testGetWithdrawalStats_ShouldReturnStatistics() {
            // Arrange
            when(withdrawalRepository.countByStatus(WithdrawalStatus.PENDING)).thenReturn(5L);
            when(withdrawalRepository.countByStatus(WithdrawalStatus.SUCCEEDED)).thenReturn(10L);
            when(withdrawalRepository.countByStatus(WithdrawalStatus.REJECTED)).thenReturn(2L);
            when(withdrawalRepository.sumAmountByStatus(WithdrawalStatus.PENDING))
                    .thenReturn(new BigDecimal("1000000"));
            when(withdrawalRepository.sumAmountByStatus(WithdrawalStatus.SUCCEEDED))
                    .thenReturn(new BigDecimal("5000000"));
            when(withdrawalRepository.sumCommissionByStatus(WithdrawalStatus.SUCCEEDED))
                    .thenReturn(new BigDecimal("500000"));
            when(withdrawalRepository.calculateAverageProcessingTimeHours()).thenReturn(2.5);

            // Act
            com.example.booking.dto.admin.WithdrawalStatsDto result = withdrawalService.getWithdrawalStats();

            // Assert
            assertNotNull(result);
            assertEquals(5L, result.getPendingCount());
            assertEquals(10L, result.getSucceededCount());
            assertEquals(2L, result.getRejectedCount());
            assertEquals(new BigDecimal("1000000"), result.getPendingAmount());
            assertEquals(new BigDecimal("5000000"), result.getSucceededAmount());
            assertNotNull(result.getSuccessRate());
        }
    }

    // ==================== 6. getTotalCommissionEarned() Tests ====================

    @Nested
    @DisplayName("6. getTotalCommissionEarned() - Get Total Commission Tests")
    class GetTotalCommissionEarnedTests {

        @Test
        @DisplayName("Test 32: Happy Path - Should Return Total Commission")
        void testGetTotalCommissionEarned_ShouldReturnTotalCommission() {
            // Arrange
            BigDecimal expectedCommission = new BigDecimal("1000000");
            when(balanceRepository.getTotalCommissionEarned()).thenReturn(expectedCommission);

            // Act
            BigDecimal result = withdrawalService.getTotalCommissionEarned();

            // Assert
            assertNotNull(result);
            assertEquals(expectedCommission, result);
            verify(balanceRepository).getTotalCommissionEarned();
        }

        @Test
        @DisplayName("Test 33: Should Return Zero When No Commission")
        void testGetTotalCommissionEarned_ShouldReturnZeroWhenNoCommission() {
            // Arrange
            when(balanceRepository.getTotalCommissionEarned()).thenReturn(BigDecimal.ZERO);

            // Act
            BigDecimal result = withdrawalService.getTotalCommissionEarned();

            // Assert
            assertNotNull(result);
            assertEquals(BigDecimal.ZERO, result);
        }
    }

    // ==================== 7. convertToRestaurantBalanceInfoDto() Tests
    // ====================

    @Nested
    @DisplayName("7. convertToRestaurantBalanceInfoDto() - Convert Balance to DTO Tests")
    class ConvertToRestaurantBalanceInfoDtoTests {

        @Test
        @DisplayName("Test 34: Happy Path - Should Convert Balance to DTO")
        void testConvertToRestaurantBalanceInfoDto_ShouldConvertSuccessfully() {
            // Arrange
            RestaurantBalance testBalance = new RestaurantBalance();
            testBalance.setBalanceId(1);
            testBalance.setRestaurant(restaurant);
            testBalance.setTotalRevenue(new BigDecimal("2000000"));
            testBalance.setAvailableBalance(new BigDecimal("1500000"));
            testBalance.setPendingWithdrawal(new BigDecimal("300000"));
            testBalance.setTotalWithdrawn(new BigDecimal("200000"));
            testBalance.setTotalCommission(new BigDecimal("100000"));
            testBalance.setTotalBookingsCompleted(50);
            testBalance.setTotalWithdrawalRequests(5);

            // This method is private, so we test it through getAllRestaurantBalances
            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
            when(balanceRepository.findAll(pageable))
                    .thenReturn(new org.springframework.data.domain.PageImpl<>(java.util.Arrays.asList(testBalance)));

            // Act
            org.springframework.data.domain.Page<com.example.booking.dto.admin.RestaurantBalanceInfoDto> result = withdrawalService
                    .getAllRestaurantBalances(null, pageable);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            com.example.booking.dto.admin.RestaurantBalanceInfoDto dto = result.getContent().get(0);
            assertEquals(restaurantId, dto.getRestaurantId());
            assertEquals(new BigDecimal("2000000"), dto.getTotalRevenue());
            assertEquals(new BigDecimal("1500000"), dto.getAvailableBalance());
        }
    }
}

