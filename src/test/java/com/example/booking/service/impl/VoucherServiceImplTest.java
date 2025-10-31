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
}

