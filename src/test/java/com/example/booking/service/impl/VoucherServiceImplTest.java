package com.example.booking.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.booking.domain.Customer;
import com.example.booking.domain.DiscountType;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.Voucher;
import com.example.booking.domain.VoucherStatus;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.CustomerVoucherRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.UserRepository;
import com.example.booking.repository.VoucherRedemptionRepository;
import com.example.booking.repository.VoucherRepository;
import com.example.booking.service.VoucherService;

@ExtendWith(MockitoExtension.class)
@DisplayName("VoucherServiceImpl Unit Tests")
class VoucherServiceImplTest {

	@Mock
	private VoucherRepository voucherRepository;

	@Mock
	private CustomerVoucherRepository customerVoucherRepository;

	@Mock
	private VoucherRedemptionRepository redemptionRepository;

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private RestaurantProfileRepository restaurantProfileRepository;

	@InjectMocks
	private VoucherServiceImpl voucherService;

	private Voucher mockVoucher;
	private Customer mockCustomer;
	private RestaurantProfile mockRestaurant;

	@BeforeEach
	void setUp() {

		mockVoucher = new Voucher();
		mockVoucher.setVoucherId(1);
		mockVoucher.setCode("TEST2024");
		mockVoucher.setDescription("Test voucher");
		mockVoucher.setDiscountType(DiscountType.PERCENT);
		mockVoucher.setDiscountValue(BigDecimal.valueOf(10));
		mockVoucher.setStartDate(LocalDate.now().minusDays(7));
		mockVoucher.setEndDate(LocalDate.now().plusDays(7));
		mockVoucher.setStatus(VoucherStatus.ACTIVE);
		mockVoucher.setMinOrderAmount(BigDecimal.valueOf(100000));
		mockVoucher.setMaxDiscountAmount(BigDecimal.valueOf(50000));
		mockVoucher.setGlobalUsageLimit(100);
		mockVoucher.setPerCustomerLimit(1);

		mockCustomer = new Customer();
		mockCustomer.setCustomerId(UUID.randomUUID());

		mockRestaurant = new RestaurantProfile();
		mockRestaurant.setRestaurantId(1);
	}

	@Test
	@DisplayName("findByCode - should return voucher when exists")
	void findByCode_WhenExists_ShouldReturnVoucher() {
		when(voucherRepository.findByCodeIgnoreCase("TEST2024")).thenReturn(Optional.of(mockVoucher));

		Optional<Voucher> result = voucherService.findByCode("TEST2024");

		assertTrue(result.isPresent());
		assertEquals(mockVoucher, result.get());
	}

	@Test
	@DisplayName("findByCode - should return empty when not exists")
	void findByCode_WhenNotExists_ShouldReturnEmpty() {
		when(voucherRepository.findByCodeIgnoreCase("INVALID")).thenReturn(Optional.empty());

		Optional<Voucher> result = voucherService.findByCode("INVALID");

		assertFalse(result.isPresent());
	}

	@Test
	@DisplayName("validate - should pass for valid voucher")
	void validate_WithValidVoucher_ShouldPass() {
		mockVoucher.setRestaurant(null); // Global voucher
		VoucherService.ValidationRequest req = new VoucherService.ValidationRequest(
				"TEST2024", 1, LocalDateTime.now().plusHours(2), 4, mockCustomer, BigDecimal.valueOf(200000));

		when(voucherRepository.findByCodeIgnoreCase("TEST2024")).thenReturn(Optional.of(mockVoucher));

		VoucherService.ValidationResult result = voucherService.validate(req);

		assertTrue(result.valid());
	}

	@Test
	@DisplayName("calculateDiscount - percent discount")
	void calculateDiscount_WithPercent_ShouldCalculateCorrectly() {
		BigDecimal orderAmount = BigDecimal.valueOf(200000);
		BigDecimal discount = voucherService.calculateDiscount(mockVoucher, orderAmount);

		assertEquals(BigDecimal.valueOf(20000), discount);
	}

	@Test
	@DisplayName("calculateDiscount - should respect max discount")
	void calculateDiscount_WhenExceedsMax_ShouldCapAtMax() {
		BigDecimal largeAmount = BigDecimal.valueOf(1000000);
		BigDecimal discount = voucherService.calculateDiscount(mockVoucher, largeAmount);

		assertEquals(mockVoucher.getMaxDiscountAmount(), discount);
	}

	@Test
	@DisplayName("getVoucherById - should return voucher")
	void getVoucherById_WhenExists_ShouldReturnVoucher() {
		when(voucherRepository.findById(1)).thenReturn(Optional.of(mockVoucher));

		Voucher result = voucherService.getVoucherById(1);

		assertNotNull(result);
		assertEquals(mockVoucher, result);
	}

	@Test
	@DisplayName("getAllVouchers - should return all")
	void getAllVouchers_ShouldReturnAll() {
		List<Voucher> vouchers = Arrays.asList(mockVoucher);
		when(voucherRepository.findAllWithRestaurant()).thenReturn(vouchers);

		List<Voucher> result = voucherService.getAllVouchers();

		assertEquals(1, result.size());
	}

	@Test
	@DisplayName("countRedemptionsByVoucherId - should return count")
	void countRedemptionsByVoucherId_ShouldReturnCount() {
		when(redemptionRepository.countByVoucherId(1)).thenReturn(25L);

		Long result = voucherService.countRedemptionsByVoucherId(1);

		assertEquals(25L, result);
	}

	// ========== validate() Additional Tests ==========

	@Test
	@DisplayName("validate - should fail when voucher is inactive")
	void validate_WithInactiveVoucher_ShouldFail() {
		mockVoucher.setStatus(VoucherStatus.INACTIVE);
		VoucherService.ValidationRequest req = new VoucherService.ValidationRequest(
				"TEST2024", 1, LocalDateTime.now().plusHours(2), 4, mockCustomer, BigDecimal.valueOf(200000));

		when(voucherRepository.findByCodeIgnoreCase("TEST2024")).thenReturn(Optional.of(mockVoucher));

		VoucherService.ValidationResult result = voucherService.validate(req);

		assertFalse(result.valid());
		assertEquals("INACTIVE", result.reason());
	}

	@Test
	@DisplayName("validate - should fail when voucher not started")
	void validate_WithNotStartedVoucher_ShouldFail() {
		mockVoucher.setStartDate(LocalDate.now().plusDays(1));
		VoucherService.ValidationRequest req = new VoucherService.ValidationRequest(
				"TEST2024", 1, LocalDateTime.now().plusHours(2), 4, mockCustomer, BigDecimal.valueOf(200000));

		when(voucherRepository.findByCodeIgnoreCase("TEST2024")).thenReturn(Optional.of(mockVoucher));

		VoucherService.ValidationResult result = voucherService.validate(req);

		assertFalse(result.valid());
		assertEquals("NOT_STARTED", result.reason());
	}

	@Test
	@DisplayName("validate - should fail when voucher expired")
	void validate_WithExpiredVoucher_ShouldFail() {
		mockVoucher.setEndDate(LocalDate.now().minusDays(1));
		VoucherService.ValidationRequest req = new VoucherService.ValidationRequest(
				"TEST2024", 1, LocalDateTime.now().plusHours(2), 4, mockCustomer, BigDecimal.valueOf(200000));

		when(voucherRepository.findByCodeIgnoreCase("TEST2024")).thenReturn(Optional.of(mockVoucher));

		VoucherService.ValidationResult result = voucherService.validate(req);

		assertFalse(result.valid());
		assertEquals("EXPIRED", result.reason());
	}

	@Test
	@DisplayName("validate - should fail when restaurant scope mismatch")
	void validate_WithRestaurantScopeMismatch_ShouldFail() {
		mockVoucher.setRestaurant(mockRestaurant);
		mockRestaurant.setRestaurantId(1);
		VoucherService.ValidationRequest req = new VoucherService.ValidationRequest(
				"TEST2024", 2, LocalDateTime.now().plusHours(2), 4, mockCustomer, BigDecimal.valueOf(200000));

		when(voucherRepository.findByCodeIgnoreCase("TEST2024")).thenReturn(Optional.of(mockVoucher));

		VoucherService.ValidationResult result = voucherService.validate(req);

		assertFalse(result.valid());
		assertEquals("RESTAURANT_SCOPE_MISMATCH", result.reason());
	}

	@Test
	@DisplayName("validate - should fail when min order amount not met")
	void validate_WithMinOrderNotMet_ShouldFail() {
		mockVoucher.setMinOrderAmount(BigDecimal.valueOf(200000));
		VoucherService.ValidationRequest req = new VoucherService.ValidationRequest(
				"TEST2024", 1, LocalDateTime.now().plusHours(2), 4, mockCustomer, BigDecimal.valueOf(50000));

		when(voucherRepository.findByCodeIgnoreCase("TEST2024")).thenReturn(Optional.of(mockVoucher));

		VoucherService.ValidationResult result = voucherService.validate(req);

		assertFalse(result.valid());
		assertEquals("MIN_ORDER_NOT_MET", result.reason());
	}

	@Test
	@DisplayName("validate - should fail with empty code")
	void validate_WithEmptyCode_ShouldFail() {
		VoucherService.ValidationRequest req = new VoucherService.ValidationRequest(
				"", 1, LocalDateTime.now().plusHours(2), 4, mockCustomer, BigDecimal.valueOf(200000));

		VoucherService.ValidationResult result = voucherService.validate(req);

		assertFalse(result.valid());
		assertEquals("EMPTY_CODE", result.reason());
	}

	@Test
	@DisplayName("validate - should fail when voucher not found")
	void validate_WithVoucherNotFound_ShouldFail() {
		VoucherService.ValidationRequest req = new VoucherService.ValidationRequest(
				"INVALID", 1, LocalDateTime.now().plusHours(2), 4, mockCustomer, BigDecimal.valueOf(200000));

		when(voucherRepository.findByCodeIgnoreCase("INVALID")).thenReturn(Optional.empty());

		VoucherService.ValidationResult result = voucherService.validate(req);

		assertFalse(result.valid());
		assertEquals("NOT_FOUND", result.reason());
	}

	// ========== applyToBooking() Tests ==========

	@Test
	@DisplayName("applyToBooking - should apply voucher successfully")
	void applyToBooking_WithValidVoucher_ShouldApply() {
		mockVoucher.setRestaurant(null);
		mockVoucher.setGlobalUsageLimit(100);
		mockVoucher.setPerCustomerLimit(1);

		VoucherService.ApplyRequest req = new VoucherService.ApplyRequest(
				"TEST2024", null, mockCustomer.getCustomerId(), BigDecimal.valueOf(200000), 1);

		when(voucherRepository.findByCodeForUpdate("TEST2024")).thenReturn(Optional.of(mockVoucher));
		when(redemptionRepository.countByVoucherIdForUpdate(1)).thenReturn(50L);
		when(redemptionRepository.countByVoucherIdAndCustomerIdForUpdate(1, mockCustomer.getCustomerId()))
				.thenReturn(0L);
		when(redemptionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		VoucherService.ApplyResult result = voucherService.applyToBooking(req);

		assertTrue(result.success());
		assertNotNull(result.discountApplied());
		verify(redemptionRepository).save(any());
	}

	@Test
	@DisplayName("applyToBooking - should fail when voucher not found")
	void applyToBooking_WithVoucherNotFound_ShouldFail() {
		VoucherService.ApplyRequest req = new VoucherService.ApplyRequest(
				"INVALID", null, mockCustomer.getCustomerId(), BigDecimal.valueOf(200000), 1);

		when(voucherRepository.findByCodeForUpdate("INVALID")).thenReturn(Optional.empty());

		VoucherService.ApplyResult result = voucherService.applyToBooking(req);

		assertFalse(result.success());
		assertEquals("NOT_FOUND", result.reason());
	}

	@Test
	@DisplayName("applyToBooking - should fail when global limit reached")
	void applyToBooking_WithGlobalLimitReached_ShouldFail() {
		mockVoucher.setGlobalUsageLimit(100);
		VoucherService.ApplyRequest req = new VoucherService.ApplyRequest(
				"TEST2024", null, mockCustomer.getCustomerId(), BigDecimal.valueOf(200000), 1);

		when(voucherRepository.findByCodeForUpdate("TEST2024")).thenReturn(Optional.of(mockVoucher));
		when(redemptionRepository.countByVoucherIdForUpdate(1)).thenReturn(100L);

		VoucherService.ApplyResult result = voucherService.applyToBooking(req);

		assertFalse(result.success());
		assertEquals("GLOBAL_LIMIT_REACHED", result.reason());
	}

	@Test
	@DisplayName("applyToBooking - should fail when per customer limit reached")
	void applyToBooking_WithPerCustomerLimitReached_ShouldFail() {
		mockVoucher.setPerCustomerLimit(1);
		VoucherService.ApplyRequest req = new VoucherService.ApplyRequest(
				"TEST2024", null, mockCustomer.getCustomerId(), BigDecimal.valueOf(200000), 1);

		when(voucherRepository.findByCodeForUpdate("TEST2024")).thenReturn(Optional.of(mockVoucher));
		when(redemptionRepository.countByVoucherIdForUpdate(1)).thenReturn(50L);
		when(redemptionRepository.countByVoucherIdAndCustomerIdForUpdate(1, mockCustomer.getCustomerId()))
				.thenReturn(1L);

		VoucherService.ApplyResult result = voucherService.applyToBooking(req);

		assertFalse(result.success());
		assertEquals("PER_CUSTOMER_LIMIT_REACHED", result.reason());
	}

	// ========== calculateDiscount() Additional Tests ==========

	@Test
	@DisplayName("calculateDiscount - fixed discount")
	void calculateDiscount_WithFixed_ShouldCalculateCorrectly() {
		mockVoucher.setDiscountType(DiscountType.FIXED);
		mockVoucher.setDiscountValue(BigDecimal.valueOf(30000));
		BigDecimal orderAmount = BigDecimal.valueOf(200000);
		BigDecimal discount = voucherService.calculateDiscount(mockVoucher, orderAmount);

		assertEquals(BigDecimal.valueOf(30000), discount);
	}

	@Test
	@DisplayName("calculateDiscount - fixed discount should not exceed order amount")
	void calculateDiscount_WithFixedExceedingOrder_ShouldCapAtOrder() {
		mockVoucher.setDiscountType(DiscountType.FIXED);
		mockVoucher.setDiscountValue(BigDecimal.valueOf(300000));
		BigDecimal orderAmount = BigDecimal.valueOf(200000);
		BigDecimal discount = voucherService.calculateDiscount(mockVoucher, orderAmount);

		assertEquals(orderAmount, discount);
	}

	@Test
	@DisplayName("calculateDiscount - should return zero for null voucher")
	void calculateDiscount_WithNullVoucher_ShouldReturnZero() {
		BigDecimal discount = voucherService.calculateDiscount(null, BigDecimal.valueOf(200000));
		assertEquals(BigDecimal.ZERO, discount);
	}

	@Test
	@DisplayName("calculateDiscount - should return zero for null order amount")
	void calculateDiscount_WithNullOrderAmount_ShouldReturnZero() {
		BigDecimal discount = voucherService.calculateDiscount(mockVoucher, null);
		assertEquals(BigDecimal.ZERO, discount);
	}

	// ========== createAdminVoucher() Tests ==========

	@Test
	@DisplayName("createAdminVoucher - should create voucher successfully")
	void createAdminVoucher_WithValidData_ShouldCreate() {
		com.example.booking.domain.User admin = new com.example.booking.domain.User();
		admin.setRole(com.example.booking.domain.UserRole.ADMIN);

		VoucherService.VoucherCreateDto dto = new VoucherService.VoucherCreateDto(
				"ADMIN2024", "Admin voucher", "PERCENT", BigDecimal.valueOf(15),
				LocalDate.now(), LocalDate.now().plusDays(30), 100, 1,
				BigDecimal.valueOf(100000), BigDecimal.valueOf(50000), null, VoucherStatus.ACTIVE);

		when(userRepository.findAll()).thenReturn(List.of(admin));
		when(voucherRepository.save(any(Voucher.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Voucher result = voucherService.createAdminVoucher(dto);

		assertNotNull(result);
		assertEquals("ADMIN2024", result.getCode());
		assertNull(result.getRestaurant()); // Global voucher
		verify(voucherRepository).save(any(Voucher.class));
	}

	// ========== createRestaurantVoucher() Tests ==========

	@Test
	@DisplayName("createRestaurantVoucher - should create voucher successfully")
	void createRestaurantVoucher_WithValidData_ShouldCreate() {
		com.example.booking.domain.User owner = new com.example.booking.domain.User();
		owner.setRole(com.example.booking.domain.UserRole.RESTAURANT_OWNER);

		VoucherService.VoucherCreateDto dto = new VoucherService.VoucherCreateDto(
				"REST2024", "Restaurant voucher", "PERCENT", BigDecimal.valueOf(20),
				LocalDate.now(), LocalDate.now().plusDays(30), 50, 1,
				BigDecimal.valueOf(50000), BigDecimal.valueOf(30000), 1, VoucherStatus.ACTIVE);

		when(userRepository.findAll()).thenReturn(List.of(owner));
		when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(mockRestaurant));
		when(voucherRepository.save(any(Voucher.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Voucher result = voucherService.createRestaurantVoucher(1, dto);

		assertNotNull(result);
		assertEquals("REST2024", result.getCode());
		assertEquals(mockRestaurant, result.getRestaurant());
		verify(voucherRepository).save(any(Voucher.class));
	}

	@Test
	@DisplayName("createRestaurantVoucher - should throw exception when restaurant not found")
	void createRestaurantVoucher_WithInvalidRestaurant_ShouldThrowException() {
		com.example.booking.domain.User owner = new com.example.booking.domain.User();
		owner.setRole(com.example.booking.domain.UserRole.RESTAURANT_OWNER);

		VoucherService.VoucherCreateDto dto = new VoucherService.VoucherCreateDto(
				"REST2024", "Restaurant voucher", "PERCENT", BigDecimal.valueOf(20),
				LocalDate.now(), LocalDate.now().plusDays(30), 50, 1,
				BigDecimal.valueOf(50000), BigDecimal.valueOf(30000), 999, VoucherStatus.ACTIVE);

		when(userRepository.findAll()).thenReturn(List.of(owner));
		when(restaurantProfileRepository.findById(999)).thenReturn(Optional.empty());

		assertThrows(RuntimeException.class, () -> voucherService.createRestaurantVoucher(999, dto));
	}

	// ========== getVouchersByRestaurant() Tests ==========

	@Test
	@DisplayName("getVouchersByRestaurant - should return vouchers")
	void getVouchersByRestaurant_ShouldReturnVouchers() {
		List<Voucher> vouchers = List.of(mockVoucher);
		when(voucherRepository.findByRestaurant_RestaurantId(1)).thenReturn(vouchers);

		List<Voucher> result = voucherService.getVouchersByRestaurant(1);

		assertEquals(1, result.size());
		assertEquals(mockVoucher, result.get(0));
	}

	// ========== assignVoucherToCustomers() Tests ==========

	@Test
	@DisplayName("assignVoucherToCustomers - should assign voucher to customers")
	void assignVoucherToCustomers_WithValidData_ShouldAssign() {
		List<UUID> customerIds = List.of(mockCustomer.getCustomerId());

		when(voucherRepository.findById(1)).thenReturn(Optional.of(mockVoucher));
		when(customerRepository.findById(mockCustomer.getCustomerId())).thenReturn(Optional.of(mockCustomer));
		when(customerVoucherRepository.findByCustomerIdAndVoucherIdForUpdate(mockCustomer.getCustomerId(), 1))
				.thenReturn(Optional.empty());
		when(customerVoucherRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		assertDoesNotThrow(() -> voucherService.assignVoucherToCustomers(1, customerIds));

		verify(customerVoucherRepository).save(any());
	}

	@Test
	@DisplayName("assignVoucherToCustomers - should skip if already assigned")
	void assignVoucherToCustomers_WithAlreadyAssigned_ShouldSkip() {
		List<UUID> customerIds = List.of(mockCustomer.getCustomerId());
		com.example.booking.domain.CustomerVoucher existing = new com.example.booking.domain.CustomerVoucher();

		when(voucherRepository.findById(1)).thenReturn(Optional.of(mockVoucher));
		when(customerVoucherRepository.findByCustomerIdAndVoucherIdForUpdate(mockCustomer.getCustomerId(), 1))
				.thenReturn(Optional.of(existing));

		assertDoesNotThrow(() -> voucherService.assignVoucherToCustomers(1, customerIds));

		verify(customerVoucherRepository, never()).save(any());
	}

	// ========== updateVoucher() Tests ==========

	@Test
	@DisplayName("updateVoucher - should update voucher successfully")
	void updateVoucher_WithValidData_ShouldUpdate() {
		VoucherService.VoucherCreateDto dto = new VoucherService.VoucherCreateDto(
				"UPDATED", "Updated voucher", "PERCENT", BigDecimal.valueOf(25),
				LocalDate.now(), LocalDate.now().plusDays(30), 200, 2,
				BigDecimal.valueOf(150000), BigDecimal.valueOf(75000), 1, VoucherStatus.ACTIVE);

		when(voucherRepository.findById(1)).thenReturn(Optional.of(mockVoucher));
		when(voucherRepository.save(any(Voucher.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Voucher result = voucherService.updateVoucher(1, dto);

		assertNotNull(result);
		verify(voucherRepository).save(any(Voucher.class));
	}

	@Test
	@DisplayName("updateVoucher - should throw exception when voucher not found")
	void updateVoucher_WithInvalidId_ShouldThrowException() {
		VoucherService.VoucherCreateDto dto = new VoucherService.VoucherCreateDto(
				"UPDATED", "Updated voucher", "PERCENT", BigDecimal.valueOf(25),
				LocalDate.now(), LocalDate.now().plusDays(30), 200, 2,
				BigDecimal.valueOf(150000), BigDecimal.valueOf(75000), 1, VoucherStatus.ACTIVE);

		when(voucherRepository.findById(999)).thenReturn(Optional.empty());

		assertThrows(RuntimeException.class, () -> voucherService.updateVoucher(999, dto));
	}

	// ========== deleteVoucher() Tests ==========

	@Test
	@DisplayName("deleteVoucher - should delete voucher successfully")
	void deleteVoucher_WithValidId_ShouldDelete() {
		when(voucherRepository.findById(1)).thenReturn(Optional.of(mockVoucher));
		doNothing().when(voucherRepository).delete(mockVoucher);

		assertDoesNotThrow(() -> voucherService.deleteVoucher(1));

		verify(voucherRepository).delete(mockVoucher);
	}

	@Test
	@DisplayName("deleteVoucher - should throw exception when voucher not found")
	void deleteVoucher_WithInvalidId_ShouldThrowException() {
		when(voucherRepository.findById(999)).thenReturn(Optional.empty());

		assertThrows(RuntimeException.class, () -> voucherService.deleteVoucher(999));
	}

	// ========== pauseVoucher() Tests ==========

	@Test
	@DisplayName("pauseVoucher - should pause voucher successfully")
	void pauseVoucher_WithValidId_ShouldPause() {
		when(voucherRepository.findById(1)).thenReturn(Optional.of(mockVoucher));
		when(voucherRepository.save(any(Voucher.class))).thenAnswer(invocation -> invocation.getArgument(0));

		assertDoesNotThrow(() -> voucherService.pauseVoucher(1));

		assertEquals(VoucherStatus.INACTIVE, mockVoucher.getStatus());
		verify(voucherRepository).save(mockVoucher);
	}

	// ========== resumeVoucher() Tests ==========

	@Test
	@DisplayName("resumeVoucher - should resume voucher successfully")
	void resumeVoucher_WithValidId_ShouldResume() {
		mockVoucher.setStatus(VoucherStatus.INACTIVE);
		when(voucherRepository.findById(1)).thenReturn(Optional.of(mockVoucher));
		when(voucherRepository.save(any(Voucher.class))).thenAnswer(invocation -> invocation.getArgument(0));

		assertDoesNotThrow(() -> voucherService.resumeVoucher(1));

		assertEquals(VoucherStatus.ACTIVE, mockVoucher.getStatus());
		verify(voucherRepository).save(mockVoucher);
	}

	// ========== expireVoucher() Tests ==========

	@Test
	@DisplayName("expireVoucher - should expire voucher successfully")
	void expireVoucher_WithValidId_ShouldExpire() {
		when(voucherRepository.findById(1)).thenReturn(Optional.of(mockVoucher));
		when(voucherRepository.save(any(Voucher.class))).thenAnswer(invocation -> invocation.getArgument(0));

		assertDoesNotThrow(() -> voucherService.expireVoucher(1));

		assertEquals(VoucherStatus.EXPIRED, mockVoucher.getStatus());
		verify(voucherRepository).save(mockVoucher);
	}

	// ========== revokeVoucherFromCustomer() Tests ==========

	@Test
	@DisplayName("revokeVoucherFromCustomer - should revoke voucher successfully")
	void revokeVoucherFromCustomer_WithValidData_ShouldRevoke() {
		com.example.booking.domain.CustomerVoucher customerVoucher = new com.example.booking.domain.CustomerVoucher();

		when(customerRepository.findById(mockCustomer.getCustomerId())).thenReturn(Optional.of(mockCustomer));
		when(voucherRepository.findById(1)).thenReturn(Optional.of(mockVoucher));
		when(customerVoucherRepository.findByCustomerAndVoucher(mockCustomer, mockVoucher))
				.thenReturn(Optional.of(customerVoucher));
		doNothing().when(customerVoucherRepository).delete(customerVoucher);

		assertDoesNotThrow(() -> voucherService.revokeVoucherFromCustomer(1, mockCustomer.getCustomerId()));

		verify(customerVoucherRepository).delete(customerVoucher);
	}

	@Test
	@DisplayName("revokeVoucherFromCustomer - should handle when not assigned")
	void revokeVoucherFromCustomer_WithNotAssigned_ShouldHandleGracefully() {
		when(customerRepository.findById(mockCustomer.getCustomerId())).thenReturn(Optional.of(mockCustomer));
		when(voucherRepository.findById(1)).thenReturn(Optional.of(mockVoucher));
		when(customerVoucherRepository.findByCustomerAndVoucher(mockCustomer, mockVoucher))
				.thenReturn(Optional.empty());

		assertDoesNotThrow(() -> voucherService.revokeVoucherFromCustomer(1, mockCustomer.getCustomerId()));

		verify(customerVoucherRepository, never()).delete(any());
	}

	// ========== isVoucherApplicableToRestaurant() Tests ==========

	@Test
	@DisplayName("isVoucherApplicableToRestaurant - should return true for global voucher")
	void isVoucherApplicableToRestaurant_WithGlobalVoucher_ShouldReturnTrue() {
		mockVoucher.setRestaurant(null);

		boolean result = voucherService.isVoucherApplicableToRestaurant(mockVoucher, mockRestaurant);

		assertTrue(result);
	}

	@Test
	@DisplayName("isVoucherApplicableToRestaurant - should return true for matching restaurant")
	void isVoucherApplicableToRestaurant_WithMatchingRestaurant_ShouldReturnTrue() {
		mockVoucher.setRestaurant(mockRestaurant);

		boolean result = voucherService.isVoucherApplicableToRestaurant(mockVoucher, mockRestaurant);

		assertTrue(result);
	}

	@Test
	@DisplayName("isVoucherApplicableToRestaurant - should return false for different restaurant")
	void isVoucherApplicableToRestaurant_WithDifferentRestaurant_ShouldReturnFalse() {
		mockVoucher.setRestaurant(mockRestaurant);
		RestaurantProfile otherRestaurant = new RestaurantProfile();
		otherRestaurant.setRestaurantId(999);

		boolean result = voucherService.isVoucherApplicableToRestaurant(mockVoucher, otherRestaurant);

		assertFalse(result);
	}

	// ========== getVouchersByCustomer() Tests ==========

	@Test
	@DisplayName("getVouchersByCustomer - should return assigned and global vouchers")
	void getVouchersByCustomer_ShouldReturnVouchers() {
		com.example.booking.domain.CustomerVoucher customerVoucher = new com.example.booking.domain.CustomerVoucher();
		customerVoucher.setVoucher(mockVoucher);

		when(customerVoucherRepository.findByCustomerId(mockCustomer.getCustomerId()))
				.thenReturn(List.of(customerVoucher));
		when(voucherRepository.findGlobalVouchers()).thenReturn(List.of());

		List<Voucher> result = voucherService.getVouchersByCustomer(mockCustomer.getCustomerId());

		assertNotNull(result);
		assertEquals(1, result.size());
	}

	// ========== countRedemptionsByVoucherIdAndCustomerId() Tests ==========

	@Test
	@DisplayName("countRedemptionsByVoucherIdAndCustomerId - should return count")
	void countRedemptionsByVoucherIdAndCustomerId_ShouldReturnCount() {
		when(redemptionRepository.countByVoucherIdAndCustomerId(1, mockCustomer.getCustomerId()))
				.thenReturn(5L);

		Long result = voucherService.countRedemptionsByVoucherIdAndCustomerId(1, mockCustomer.getCustomerId());

		assertEquals(5L, result);
	}

	// ========== findByCode() Additional Tests ==========

	@Test
	@DisplayName("findByCode - should handle null code")
	void findByCode_WithNullCode_ShouldReturnEmpty() {
		Optional<Voucher> result = voucherService.findByCode(null);

		assertTrue(result.isEmpty());
		verify(voucherRepository, never()).findByCodeIgnoreCase(any());
	}

	@Test
	@DisplayName("findByCode - should handle blank code")
	void findByCode_WithBlankCode_ShouldReturnEmpty() {
		Optional<Voucher> result = voucherService.findByCode("   ");

		assertTrue(result.isEmpty());
		verify(voucherRepository, never()).findByCodeIgnoreCase(any());
	}

	@Test
	@DisplayName("findByCode - should trim code")
	void findByCode_WithWhitespace_ShouldTrim() {
		when(voucherRepository.findByCodeIgnoreCase("TEST2024")).thenReturn(Optional.of(mockVoucher));

		Optional<Voucher> result = voucherService.findByCode("  TEST2024  ");

		assertTrue(result.isPresent());
		verify(voucherRepository).findByCodeIgnoreCase("TEST2024");
	}
}

