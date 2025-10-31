package com.example.booking.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.example.booking.domain.VoucherStatus;
import com.example.booking.domain.DiscountType;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.Voucher;

/**
 * Unit tests for VoucherRepository
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("VoucherRepository Tests")
public class VoucherRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VoucherRepository voucherRepository;

    @Test
    @DisplayName("shouldFindActiveVouchers_successfully")
    void shouldFindActiveVouchers_successfully() {
        // Given
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantName("Test Restaurant");
        entityManager.persist(restaurant);

        Voucher voucher = new Voucher();
        voucher.setRestaurant(restaurant);
        voucher.setCode("TEST2024");
        voucher.setDiscountValue(new BigDecimal("100000"));
        voucher.setStatus(VoucherStatus.ACTIVE);
        voucher.setDiscountType(DiscountType.FIXED);
        voucher.setStartDate(LocalDate.now().minusDays(1));
        voucher.setEndDate(LocalDate.now().plusDays(30));
        entityManager.persistAndFlush(voucher);

        // When
        List<Voucher> activeVouchers = voucherRepository.findByStatus(VoucherStatus.ACTIVE);

        // Then
        assertTrue(activeVouchers.size() > 0);
    }
}

