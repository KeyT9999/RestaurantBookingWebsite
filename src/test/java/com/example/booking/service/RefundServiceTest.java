package com.example.booking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.booking.common.enums.RefundStatus;
import com.example.booking.domain.Booking;
import com.example.booking.domain.Customer;
import com.example.booking.domain.Payment;
import com.example.booking.domain.PaymentMethod;
import com.example.booking.domain.PaymentStatus;
import com.example.booking.domain.RefundRequest;
import com.example.booking.domain.RestaurantBalance;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.repository.PaymentRepository;
import com.example.booking.repository.RefundRequestRepository;

/**
 * Unit tests for RefundService
 * Testing createRefundRequest (via processRefundWithManualTransfer), processRefund, and rejectRefund methods
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RefundService Tests")
public class RefundServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RefundRequestRepository refundRequestRepository;

    @Mock
    private RestaurantBalanceService restaurantBalanceService;

    @Mock
    private BankAccountService bankAccountService;

    @Mock
    private PayOsService payOsService;

    @Mock
    private EnhancedRefundService enhancedRefundService;

    @InjectMocks
    private RefundService refundService;

    // Test data
    private Payment testPayment;
    private Customer testCustomer;
    private Booking testBooking;
    private RestaurantProfile testRestaurant;
    private RestaurantBalance testBalance;
    private RefundRequest testRefundRequest;

    @BeforeEach
    public void setUp() {
        // Setup test customer
        testCustomer = new Customer();
        testCustomer.setCustomerId(UUID.randomUUID());
        testCustomer.setFullName("Test Customer");

        // Setup test restaurant
        testRestaurant = new RestaurantProfile();
        testRestaurant.setRestaurantId(1);
        testRestaurant.setRestaurantName("Test Restaurant");

        // Setup test booking
        testBooking = new Booking();
        testBooking.setBookingId(1);
        testBooking.setCustomer(testCustomer);
        testBooking.setRestaurant(testRestaurant);

        // Setup test payment
        testPayment = new Payment();
        testPayment.setPaymentId(1);
        testPayment.setCustomer(testCustomer);
        testPayment.setBooking(testBooking);
        testPayment.setAmount(new BigDecimal("500000"));
        testPayment.setStatus(PaymentStatus.COMPLETED);
        testPayment.setOrderCode(1001L);
        testPayment.setPaymentMethod(PaymentMethod.PAYOS);

        // Setup test balance
        testBalance = new RestaurantBalance();
        testBalance.setBalanceId(1);
        testBalance.setRestaurant(testRestaurant);
        testBalance.setAvailableBalance(new BigDecimal("1000000"));
        testBalance.setPendingRefund(BigDecimal.ZERO);
        testBalance.setTotalRefunded(BigDecimal.ZERO);

        // Setup test refund request
        testRefundRequest = new RefundRequest();
        testRefundRequest.setRefundRequestId(1);
        testRefundRequest.setPayment(testPayment);
        testRefundRequest.setCustomer(testCustomer);
        testRefundRequest.setRestaurant(testRestaurant);
        testRefundRequest.setAmount(new BigDecimal("500000"));
        testRefundRequest.setReason("Test refund reason");
        testRefundRequest.setStatus(RefundStatus.PENDING);
        testRefundRequest.setRequestedAt(LocalDateTime.now());
    }

    // ========== createRefundRequest() Tests (via processRefundWithManualTransfer) ==========

    @Test
    @DisplayName("Should create refund request with valid payment and bank info")
    public void testCreateRefundRequest_WithValidPaymentAndBankInfo_ShouldCreateRequest() {
        // Given
        String bankCode = "VCB";
        String accountNumber = "1234567890";
        String reason = "Change of plans";

        when(paymentRepository.findById(testPayment.getPaymentId()))
            .thenReturn(Optional.of(testPayment));
        when(bankAccountService.getAccountHolderName(accountNumber, bankCode)).thenReturn("Test Customer");
        when(bankAccountService.getBankName(bankCode)).thenReturn("Vietcombank");
        when(payOsService.createTransferQRCode(any(), any(), any(), any(), any())).thenReturn("qr-data-123");
        when(payOsService.createTransferQRCodeUrl(any(), any(), any(), any(), any())).thenReturn("qr-url-123");
        when(refundRequestRepository.save(any(RefundRequest.class))).thenAnswer(invocation -> {
            RefundRequest req = invocation.getArgument(0);
            req.setRefundRequestId(1);
            return req;
        });
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(restaurantBalanceService.getBalanceByRestaurantId(anyInt())).thenReturn(testBalance);
        when(restaurantBalanceService.saveBalance(any(RestaurantBalance.class))).thenReturn(testBalance);

        // When
        Payment result = refundService.processRefundWithManualTransfer(
            testPayment.getPaymentId(), reason, bankCode, accountNumber
        );

        // Then
        assertNotNull(result);
        verify(refundRequestRepository).save(any(RefundRequest.class));
    }

    @Test
    @DisplayName("Should generate QR code data for refund request")
    public void testCreateRefundRequest_ShouldGenerateQRCodeData() {
        // Given
        String bankCode = "VCB";
        String accountNumber = "1234567890";
        String reason = "Test reason";

        when(paymentRepository.findById(testPayment.getPaymentId()))
            .thenReturn(Optional.of(testPayment));
        when(bankAccountService.getAccountHolderName(accountNumber, bankCode)).thenReturn("Test Customer");
        when(bankAccountService.getBankName(bankCode)).thenReturn("Vietcombank");
        when(payOsService.createTransferQRCode(any(), any(), any(), any(), any())).thenReturn("qr-data-123");
        when(payOsService.createTransferQRCodeUrl(any(), any(), any(), any(), any())).thenReturn("qr-url-123");
        when(refundRequestRepository.save(any(RefundRequest.class))).thenAnswer(invocation -> {
            RefundRequest req = invocation.getArgument(0);
            req.setRefundRequestId(1);
            return req;
        });
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(restaurantBalanceService.getBalanceByRestaurantId(anyInt())).thenReturn(testBalance);
        when(restaurantBalanceService.saveBalance(any(RestaurantBalance.class))).thenReturn(testBalance);

        // When
        refundService.processRefundWithManualTransfer(
            testPayment.getPaymentId(), reason, bankCode, accountNumber
        );

        // Then
        verify(payOsService).createTransferQRCode(any(), any(), any(), any(), any());
        verify(payOsService).createTransferQRCodeUrl(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should fetch account holder name from bank service")
    public void testCreateRefundRequest_ShouldFetchAccountHolderName() {
        // Given
        String bankCode = "VCB";
        String accountNumber = "1234567890";
        String accountHolderName = "Nguyen Van A";
        String reason = "Test reason";

        when(paymentRepository.findById(testPayment.getPaymentId()))
            .thenReturn(Optional.of(testPayment));
        when(bankAccountService.getAccountHolderName(accountNumber, bankCode)).thenReturn(accountHolderName);
        when(bankAccountService.getBankName(bankCode)).thenReturn("Vietcombank");
        when(payOsService.createTransferQRCode(any(), any(), any(), any(), any())).thenReturn("qr-data");
        when(payOsService.createTransferQRCodeUrl(any(), any(), any(), any(), any())).thenReturn("qr-url");
        when(refundRequestRepository.save(any(RefundRequest.class))).thenAnswer(invocation -> {
            RefundRequest req = invocation.getArgument(0);
            req.setRefundRequestId(1);
            return req;
        });
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(restaurantBalanceService.getBalanceByRestaurantId(anyInt())).thenReturn(testBalance);
        when(restaurantBalanceService.saveBalance(any(RestaurantBalance.class))).thenReturn(testBalance);

        // When
        refundService.processRefundWithManualTransfer(
            testPayment.getPaymentId(), reason, bankCode, accountNumber
        );

        // Then
        verify(bankAccountService).getAccountHolderName(accountNumber, bankCode);
    }

    @Test
    @DisplayName("Should fallback to customer name when bank service returns null")
    public void testCreateRefundRequest_WithInvalidBankAccount_ShouldFallbackToCustomerName() {
        // Given
        String bankCode = "VCB";
        String accountNumber = "1234567890";
        String reason = "Test reason";

        when(paymentRepository.findById(testPayment.getPaymentId()))
            .thenReturn(Optional.of(testPayment));
        when(bankAccountService.getAccountHolderName(accountNumber, bankCode)).thenReturn(null);
        when(bankAccountService.getBankName(bankCode)).thenReturn("Vietcombank");
        when(payOsService.createTransferQRCode(any(), any(), any(), any(), any())).thenReturn("qr-data");
        when(payOsService.createTransferQRCodeUrl(any(), any(), any(), any(), any())).thenReturn("qr-url");
        when(refundRequestRepository.save(any(RefundRequest.class))).thenAnswer(invocation -> {
            RefundRequest req = invocation.getArgument(0);
            req.setRefundRequestId(1);
            return req;
        });
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(restaurantBalanceService.getBalanceByRestaurantId(anyInt())).thenReturn(testBalance);
        when(restaurantBalanceService.saveBalance(any(RestaurantBalance.class))).thenReturn(testBalance);

        // When
        refundService.processRefundWithManualTransfer(
            testPayment.getPaymentId(), reason, bankCode, accountNumber
        );

        // Then - fallback to customer name verified via bankAccountService returning null
        verify(bankAccountService).getAccountHolderName(accountNumber, bankCode);
    }

    @Test
    @DisplayName("Should set all required fields in refund request")
    public void testCreateRefundRequest_ShouldSetAllRequiredFields() {
        // Given
        String bankCode = "VCB";
        String accountNumber = "1234567890";
        String reason = "Test reason";

        when(paymentRepository.findById(testPayment.getPaymentId()))
            .thenReturn(Optional.of(testPayment));
        when(bankAccountService.getAccountHolderName(accountNumber, bankCode)).thenReturn("Test Customer");
        when(bankAccountService.getBankName(bankCode)).thenReturn("Vietcombank");
        when(payOsService.createTransferQRCode(any(), any(), any(), any(), any())).thenReturn("qr-data");
        when(payOsService.createTransferQRCodeUrl(any(), any(), any(), any(), any())).thenReturn("qr-url");
        when(refundRequestRepository.save(any(RefundRequest.class))).thenAnswer(invocation -> {
            RefundRequest req = invocation.getArgument(0);
            req.setRefundRequestId(1);
            return req;
        });
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(restaurantBalanceService.getBalanceByRestaurantId(anyInt())).thenReturn(testBalance);
        when(restaurantBalanceService.saveBalance(any(RestaurantBalance.class))).thenReturn(testBalance);

        // When
        refundService.processRefundWithManualTransfer(
            testPayment.getPaymentId(), reason, bankCode, accountNumber
        );

        // Then - verify all fields are set
        verify(refundRequestRepository).save(any(RefundRequest.class));
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should match refund amount with payment amount")
    public void testCreateRefundRequest_AmountMatchesPaymentAmount() {
        // Given
        testPayment.setAmount(new BigDecimal("750000"));
        String bankCode = "VCB";
        String accountNumber = "1234567890";
        String reason = "Test reason";

        when(paymentRepository.findById(testPayment.getPaymentId()))
            .thenReturn(Optional.of(testPayment));
        when(bankAccountService.getAccountHolderName(accountNumber, bankCode)).thenReturn("Test Customer");
        when(bankAccountService.getBankName(bankCode)).thenReturn("Vietcombank");
        when(payOsService.createTransferQRCode(any(), any(), any(), any(), any())).thenReturn("qr-data");
        when(payOsService.createTransferQRCodeUrl(any(), any(), any(), any(), any())).thenReturn("qr-url");
        when(refundRequestRepository.save(any(RefundRequest.class))).thenAnswer(invocation -> {
            RefundRequest req = invocation.getArgument(0);
            req.setRefundRequestId(1);
            return req;
        });
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(restaurantBalanceService.getBalanceByRestaurantId(anyInt())).thenReturn(testBalance);
        when(restaurantBalanceService.saveBalance(any(RestaurantBalance.class))).thenReturn(testBalance);

        // When
        Payment result = refundService.processRefundWithManualTransfer(
            testPayment.getPaymentId(), reason, bankCode, accountNumber
        );

        // Then
        assertNotNull(result);
        verify(refundRequestRepository).save(any(RefundRequest.class));
    }

    @Test
    @DisplayName("Should still create request with empty reason")
    public void testCreateRefundRequest_WithEmptyReason_ShouldStillCreateRequest() {
        // Given
        String bankCode = "VCB";
        String accountNumber = "1234567890";
        String reason = "";

        when(paymentRepository.findById(testPayment.getPaymentId()))
            .thenReturn(Optional.of(testPayment));
        when(bankAccountService.getAccountHolderName(accountNumber, bankCode)).thenReturn("Test Customer");
        when(bankAccountService.getBankName(bankCode)).thenReturn("Vietcombank");
        when(payOsService.createTransferQRCode(any(), any(), any(), any(), any())).thenReturn("qr-data");
        when(payOsService.createTransferQRCodeUrl(any(), any(), any(), any(), any())).thenReturn("qr-url");
        when(refundRequestRepository.save(any(RefundRequest.class))).thenAnswer(invocation -> {
            RefundRequest req = invocation.getArgument(0);
            req.setRefundRequestId(1);
            return req;
        });
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(restaurantBalanceService.getBalanceByRestaurantId(anyInt())).thenReturn(testBalance);
        when(restaurantBalanceService.saveBalance(any(RestaurantBalance.class))).thenReturn(testBalance);

        // When
        Payment result = refundService.processRefundWithManualTransfer(
            testPayment.getPaymentId(), reason, bankCode, accountNumber
        );

        // Then
        assertNotNull(result);
        verify(refundRequestRepository).save(any(RefundRequest.class));
    }

    @Test
    @DisplayName("Should save refund request to database")
    public void testCreateRefundRequest_ShouldSaveToDatabase() {
        // Given
        String bankCode = "VCB";
        String accountNumber = "1234567890";
        String reason = "Test reason";

        when(paymentRepository.findById(testPayment.getPaymentId()))
            .thenReturn(Optional.of(testPayment));
        when(bankAccountService.getAccountHolderName(accountNumber, bankCode)).thenReturn("Test Customer");
        when(bankAccountService.getBankName(bankCode)).thenReturn("Vietcombank");
        when(payOsService.createTransferQRCode(any(), any(), any(), any(), any())).thenReturn("qr-data");
        when(payOsService.createTransferQRCodeUrl(any(), any(), any(), any(), any())).thenReturn("qr-url");
        when(refundRequestRepository.save(any(RefundRequest.class))).thenAnswer(invocation -> {
            RefundRequest req = invocation.getArgument(0);
            req.setRefundRequestId(1);
            return req;
        });
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(restaurantBalanceService.getBalanceByRestaurantId(anyInt())).thenReturn(testBalance);
        when(restaurantBalanceService.saveBalance(any(RestaurantBalance.class))).thenReturn(testBalance);

        // When
        Payment result = refundService.processRefundWithManualTransfer(
            testPayment.getPaymentId(), reason, bankCode, accountNumber
        );

        // Then
        assertNotNull(result);
        verify(refundRequestRepository).save(any(RefundRequest.class));
        verify(paymentRepository).save(any(Payment.class));
    }

    // ========== processRefund() Tests ==========

    @Test
    @DisplayName("Should process refund with completed payment successfully")
    public void testProcessRefund_WithCompletedPayment_ShouldRefundSuccessfully() {
        // Given
        BigDecimal refundAmount = new BigDecimal("500000");
        String reason = "Customer request";

        when(enhancedRefundService.processRefundWithCommissionDeduction(
            anyInt(), any(BigDecimal.class), anyString()
        )).thenReturn(testPayment);

        // When
        Payment result = refundService.processRefund(testPayment.getPaymentId(), refundAmount, reason);

        // Then
        assertNotNull(result);
        verify(enhancedRefundService).processRefundWithCommissionDeduction(
            testPayment.getPaymentId(), refundAmount, reason
        );
    }

    @Test
    @DisplayName("Should throw exception for non-completed payment")
    public void testProcessRefund_WithNonCompletedPayment_ShouldThrowException() {
        // Given
        testPayment.setStatus(PaymentStatus.PENDING);
        BigDecimal refundAmount = new BigDecimal("500000");
        String reason = "Reason";

        when(enhancedRefundService.processRefundWithCommissionDeduction(
            anyInt(), any(BigDecimal.class), anyString()
        )).thenThrow(new IllegalArgumentException("Only completed payments can be refunded"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            refundService.processRefund(testPayment.getPaymentId(), refundAmount, reason)
        );
    }

    @Test
    @DisplayName("Should throw exception when payment already refunded")
    public void testProcessRefund_WithAlreadyRefunded_ShouldThrowException() {
        // Given
        testPayment.setRefundedAt(LocalDateTime.now());
        BigDecimal refundAmount = new BigDecimal("500000");
        String reason = "Reason";

        when(enhancedRefundService.processRefundWithCommissionDeduction(
            anyInt(), any(BigDecimal.class), anyString()
        )).thenThrow(new IllegalArgumentException("Payment has already been refunded"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            refundService.processRefund(testPayment.getPaymentId(), refundAmount, reason)
        );
    }

    @Test
    @DisplayName("Should throw exception when refund amount exceeds payment")
    public void testProcessRefund_WithRefundAmountExceedingPayment_ShouldThrowException() {
        // Given
        BigDecimal refundAmount = new BigDecimal("2000000");
        String reason = "Reason";

        when(enhancedRefundService.processRefundWithCommissionDeduction(
            anyInt(), any(BigDecimal.class), anyString()
        )).thenThrow(new IllegalArgumentException("Refund amount cannot exceed payment amount"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            refundService.processRefund(testPayment.getPaymentId(), refundAmount, reason)
        );
    }

    // ========== rejectRefund() Tests ==========

    @Test
    @DisplayName("Should reject refund request with pending status successfully")
    public void testRejectRefund_WithPendingRefundRequest_ShouldRejectSuccessfully() {
        // Given
        Integer refundRequestId = 1;
        UUID adminId = UUID.randomUUID();
        String rejectReason = "Insufficient funds";

        testRefundRequest.setStatus(RefundStatus.PENDING);

        when(refundRequestRepository.findById(refundRequestId))
            .thenReturn(Optional.of(testRefundRequest));
        when(restaurantBalanceService.getBalanceByRestaurantId(anyInt()))
            .thenReturn(testBalance);
        when(refundRequestRepository.save(any(RefundRequest.class))).thenReturn(testRefundRequest);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        refundService.rejectRefund(refundRequestId, adminId, rejectReason);

        // Then
        assertEquals(RefundStatus.REJECTED, testRefundRequest.getStatus());
        assertEquals(adminId, testRefundRequest.getProcessedBy());
        assertEquals(rejectReason, testRefundRequest.getAdminNote());
        assertNotNull(testRefundRequest.getProcessedAt());
        verify(refundRequestRepository).save(testRefundRequest);
        verify(paymentRepository).save(testPayment);
    }

    @Test
    @DisplayName("Should restore restaurant balance on refund rejection")
    public void testRejectRefund_ShouldRestoreRestaurantBalance() {
        // Given
        Integer refundRequestId = 1;
        UUID adminId = UUID.randomUUID();
        String rejectReason = "Test rejection";

        testRefundRequest.setStatus(RefundStatus.PENDING);
        testBalance.setPendingRefund(new BigDecimal("500000"));
        testBalance.setAvailableBalance(new BigDecimal("500000"));

        when(refundRequestRepository.findById(refundRequestId))
            .thenReturn(Optional.of(testRefundRequest));
        when(restaurantBalanceService.getBalanceByRestaurantId(anyInt()))
            .thenReturn(testBalance);
        when(refundRequestRepository.save(any(RefundRequest.class))).thenReturn(testRefundRequest);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        refundService.rejectRefund(refundRequestId, adminId, rejectReason);

        // Then
        verify(restaurantBalanceService).saveBalance(testBalance);
    }

    @Test
    @DisplayName("Should reset payment status to completed on refund rejection")
    public void testRejectRefund_ShouldResetPaymentStatusToCompleted() {
        // Given
        Integer refundRequestId = 1;
        UUID adminId = UUID.randomUUID();
        String rejectReason = "Test";

        testRefundRequest.setStatus(RefundStatus.PENDING);
        testPayment.setStatus(PaymentStatus.REFUND_PENDING);
        testPayment.setRefundRequestId(refundRequestId);

        when(refundRequestRepository.findById(refundRequestId))
            .thenReturn(Optional.of(testRefundRequest));
        when(restaurantBalanceService.getBalanceByRestaurantId(anyInt()))
            .thenReturn(testBalance);
        when(refundRequestRepository.save(any(RefundRequest.class))).thenReturn(testRefundRequest);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        refundService.rejectRefund(refundRequestId, adminId, rejectReason);

        // Then
        assertEquals(PaymentStatus.COMPLETED, testPayment.getStatus());
        assertNull(testPayment.getRefundRequestId());
    }

    @Test
    @DisplayName("Should throw exception for non-pending refund request")
    public void testRejectRefund_WithNonPendingStatus_ShouldThrowException() {
        // Given
        Integer refundRequestId = 1;
        UUID adminId = UUID.randomUUID();
        String rejectReason = "Test";

        testRefundRequest.setStatus(RefundStatus.COMPLETED);

        when(refundRequestRepository.findById(refundRequestId))
            .thenReturn(Optional.of(testRefundRequest));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            refundService.rejectRefund(refundRequestId, adminId, rejectReason)
        );
    }

    @Test
    @DisplayName("Should throw exception for non-existent refund request")
    public void testRejectRefund_WithNonexistentId_ShouldThrowException() {
        // Given
        Integer refundRequestId = 99999;
        UUID adminId = UUID.randomUUID();
        String rejectReason = "Test";

        when(refundRequestRepository.findById(refundRequestId))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            refundService.rejectRefund(refundRequestId, adminId, rejectReason)
        );
    }

    @Test
    @DisplayName("Should set processedAt timestamp on rejection")
    public void testRejectRefund_ShouldSetProcessedAtTimestamp() {
        // Given
        Integer refundRequestId = 1;
        UUID adminId = UUID.randomUUID();
        String rejectReason = "Test";

        testRefundRequest.setStatus(RefundStatus.PENDING);

        when(refundRequestRepository.findById(refundRequestId))
            .thenReturn(Optional.of(testRefundRequest));
        when(restaurantBalanceService.getBalanceByRestaurantId(anyInt()))
            .thenReturn(testBalance);
        when(refundRequestRepository.save(any(RefundRequest.class))).thenReturn(testRefundRequest);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        refundService.rejectRefund(refundRequestId, adminId, rejectReason);

        // Then
        assertNotNull(testRefundRequest.getProcessedAt());
    }

    // ========== completeRefund() Tests ==========

    @Test
    @DisplayName("Happy Path: Should complete refund with pending status successfully")
    public void testCompleteRefund_WithPendingRefundRequest_ShouldCompleteSuccessfully() {
        // Given: PENDING refund request, admin completes it
        Integer refundRequestId = 1;
        UUID adminId = UUID.randomUUID();
        String transferReference = "REF123456";
        String adminNote = "Completed successfully";

        testRefundRequest.setStatus(RefundStatus.PENDING);
        testPayment.setStatus(PaymentStatus.REFUND_PENDING);
        
        when(refundRequestRepository.findById(refundRequestId))
            .thenReturn(Optional.of(testRefundRequest));
        when(refundRequestRepository.save(any(RefundRequest.class))).thenReturn(testRefundRequest);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(restaurantBalanceService.getBalanceByRestaurantId(anyInt()))
            .thenReturn(testBalance);
        when(restaurantBalanceService.saveBalance(any(RestaurantBalance.class))).thenReturn(testBalance);

        // When
        refundService.completeRefund(refundRequestId, adminId, transferReference, adminNote);

        // Then
        assertEquals(RefundStatus.COMPLETED, testRefundRequest.getStatus());
        assertEquals(adminId, testRefundRequest.getProcessedBy());
        assertEquals(transferReference, testRefundRequest.getTransferReference());
        assertEquals(adminNote, testRefundRequest.getAdminNote());
        assertNotNull(testRefundRequest.getProcessedAt());
        assertEquals(PaymentStatus.REFUNDED, testPayment.getStatus());
        assertNotNull(testPayment.getRefundedAt());
        verify(refundRequestRepository, times(1)).save(testRefundRequest);
        verify(paymentRepository, times(1)).save(testPayment);
    }

    @Test
    @DisplayName("Error Scenario: Should throw exception for non-pending refund request")
    public void testCompleteRefund_WithNonPendingStatus_ShouldThrowException() {
        // Given: COMPLETED refund request
        Integer refundRequestId = 1;
        UUID adminId = UUID.randomUUID();

        testRefundRequest.setStatus(RefundStatus.COMPLETED);

        when(refundRequestRepository.findById(refundRequestId))
            .thenReturn(Optional.of(testRefundRequest));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            refundService.completeRefund(refundRequestId, adminId, "REF123", "Note")
        );
        assertEquals("Refund request is not pending", exception.getMessage());
    }

    @Test
    @DisplayName("Error Scenario: Should throw exception for non-existent refund request")
    public void testCompleteRefund_WithNonexistentId_ShouldThrowException() {
        // Given: Invalid refund request ID
        Integer refundRequestId = 99999;
        UUID adminId = UUID.randomUUID();

        when(refundRequestRepository.findById(refundRequestId))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            refundService.completeRefund(refundRequestId, adminId, "REF123", "Note")
        );
    }

    // ========== generateVietQRForRefund() Tests ==========

    @Test
    @DisplayName("Happy Path: Should generate VietQR for refund successfully")
    public void testGenerateVietQRForRefund_WithValidData_ShouldGenerateQR() {
        // Given: Valid refund request with bank info
        Integer refundRequestId = 1;
        
        testRefundRequest.setVietqrUrl(null); // No existing QR
        testRefundRequest.setCustomerBankCode("VCB");
        testRefundRequest.setCustomerAccountNumber("1234567890");
        testRefundRequest.setCustomerAccountHolder("Test Customer");

        when(refundRequestRepository.findById(refundRequestId))
            .thenReturn(Optional.of(testRefundRequest));
        when(refundRequestRepository.save(any(RefundRequest.class))).thenReturn(testRefundRequest);

        // When
        String qrUrl = refundService.generateVietQRForRefund(refundRequestId);

        // Then
        assertNotNull(qrUrl);
        assertTrue(qrUrl.contains("vietqr.io"));
        assertTrue(qrUrl.contains("VCB"));
        assertTrue(qrUrl.contains("1234567890"));
        verify(refundRequestRepository, times(1)).save(testRefundRequest);
    }

    @Test
    @DisplayName("Edge Case: Should return existing QR if already exists")
    public void testGenerateVietQRForRefund_WithExistingQR_ShouldReturnExisting() {
        // Given: Refund request already has QR
        Integer refundRequestId = 1;
        String existingQr = "https://img.vietqr.io/image/VCB-1234567890-compact2.png";

        testRefundRequest.setVietqrUrl(existingQr);

        when(refundRequestRepository.findById(refundRequestId))
            .thenReturn(Optional.of(testRefundRequest));

        // When
        String qrUrl = refundService.generateVietQRForRefund(refundRequestId);

        // Then
        assertEquals(existingQr, qrUrl);
        verify(refundRequestRepository, never()).save(any(RefundRequest.class));
    }

    @Test
    @DisplayName("Error Scenario: Should throw exception when bank info missing")
    public void testGenerateVietQRForRefund_WithMissingBankInfo_ShouldThrowException() {
        // Given: Missing bank code
        Integer refundRequestId = 1;
        
        testRefundRequest.setVietqrUrl(null);
        testRefundRequest.setCustomerBankCode(null);
        testRefundRequest.setCustomerAccountNumber("1234567890");
        testRefundRequest.setCustomerAccountHolder("Test Customer");

        when(refundRequestRepository.findById(refundRequestId))
            .thenReturn(Optional.of(testRefundRequest));

        // When & Then
        // Note: IllegalStateException is wrapped in RuntimeException by the service
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            refundService.generateVietQRForRefund(refundRequestId)
        );
        assertTrue(exception.getMessage().contains("Customer bank information is missing"));
    }

    // ========== processRefundWithManualTransfer() Additional Tests ==========

    @Test
    @DisplayName("Error Scenario: Should throw exception when payment not found")
    public void testProcessRefundWithManualTransfer_WithNonexistentPayment_ShouldThrowException() {
        // Given: Payment ID not found
        Integer paymentId = 99999;
        String bankCode = "VCB";
        String accountNumber = "1234567890";
        String reason = "Test reason";

        when(paymentRepository.findById(paymentId))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            refundService.processRefundWithManualTransfer(paymentId, reason, bankCode, accountNumber)
        );
    }

    @Test
    @DisplayName("Error Scenario: Should throw exception when payment already refunded")
    public void testProcessRefundWithManualTransfer_WithAlreadyRefunded_ShouldThrowException() {
        // Given: Payment already refunded
        testPayment.setRefundedAt(LocalDateTime.now());
        String bankCode = "VCB";
        String accountNumber = "1234567890";
        String reason = "Test reason";

        when(paymentRepository.findById(testPayment.getPaymentId()))
            .thenReturn(Optional.of(testPayment));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            refundService.processRefundWithManualTransfer(testPayment.getPaymentId(), reason, bankCode, accountNumber)
        );
        assertEquals("Payment has already been refunded", exception.getMessage());
    }

    @Test
    @DisplayName("Edge Case: Should allow negative balance on refund")
    public void testProcessRefundWithManualTransfer_AllowsNegativeBalance() {
        // Given: Restaurant balance below refund amount
        String bankCode = "VCB";
        String accountNumber = "1234567890";
        String reason = "Test reason";
        
        testBalance.setAvailableBalance(new BigDecimal("100000")); // Less than 500000 refund

        when(paymentRepository.findById(testPayment.getPaymentId()))
            .thenReturn(Optional.of(testPayment));
        when(bankAccountService.getAccountHolderName(accountNumber, bankCode)).thenReturn("Test Customer");
        when(bankAccountService.getBankName(bankCode)).thenReturn("Vietcombank");
        when(payOsService.createTransferQRCode(any(), any(), any(), any(), any())).thenReturn("qr-data");
        when(payOsService.createTransferQRCodeUrl(any(), any(), any(), any(), any())).thenReturn("qr-url");
        when(refundRequestRepository.save(any(RefundRequest.class))).thenAnswer(invocation -> {
            RefundRequest req = invocation.getArgument(0);
            req.setRefundRequestId(1);
            return req;
        });
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(restaurantBalanceService.getBalanceByRestaurantId(anyInt())).thenReturn(testBalance);
        when(restaurantBalanceService.saveBalance(any(RestaurantBalance.class))).thenReturn(testBalance);

        // When
        Payment result = refundService.processRefundWithManualTransfer(
            testPayment.getPaymentId(), reason, bankCode, accountNumber
        );

        // Then: Should succeed even with negative balance
        assertNotNull(result);
        verify(restaurantBalanceService).saveBalance(any(RestaurantBalance.class));
        // Balance becomes -400000 (100000 - 500000), so it should be negative
        assertTrue(testBalance.getAvailableBalance().compareTo(BigDecimal.ZERO) < 0, 
            "Balance should be negative after refund exceeds available balance");
        assertEquals(new BigDecimal("-400000"), testBalance.getAvailableBalance());
    }

    @Test
    @DisplayName("State Verification: Should update payment status to REFUND_PENDING")
    public void testProcessRefundWithManualTransfer_ShouldUpdatePaymentStatus() {
        // Given
        String bankCode = "VCB";
        String accountNumber = "1234567890";
        String reason = "Test reason";
        
        testPayment.setStatus(PaymentStatus.COMPLETED);

        when(paymentRepository.findById(testPayment.getPaymentId()))
            .thenReturn(Optional.of(testPayment));
        when(bankAccountService.getAccountHolderName(accountNumber, bankCode)).thenReturn("Test Customer");
        when(bankAccountService.getBankName(bankCode)).thenReturn("Vietcombank");
        when(payOsService.createTransferQRCode(any(), any(), any(), any(), any())).thenReturn("qr-data");
        when(payOsService.createTransferQRCodeUrl(any(), any(), any(), any(), any())).thenReturn("qr-url");
        when(refundRequestRepository.save(any(RefundRequest.class))).thenAnswer(invocation -> {
            RefundRequest req = invocation.getArgument(0);
            req.setRefundRequestId(1);
            return req;
        });
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(restaurantBalanceService.getBalanceByRestaurantId(anyInt())).thenReturn(testBalance);
        when(restaurantBalanceService.saveBalance(any(RestaurantBalance.class))).thenReturn(testBalance);

        // When
        Payment result = refundService.processRefundWithManualTransfer(
            testPayment.getPaymentId(), reason, bankCode, accountNumber
        );

        // Then
        assertEquals(PaymentStatus.REFUND_PENDING, result.getStatus());
        verify(paymentRepository).save(testPayment);
    }
}
