package com.example.booking.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.Customer;
import com.example.booking.domain.CustomerVoucher;
import com.example.booking.domain.Voucher;

import jakarta.persistence.LockModeType;

@Repository
public interface CustomerVoucherRepository extends JpaRepository<CustomerVoucher, Integer> {

    Optional<CustomerVoucher> findByCustomerAndVoucher(Customer customer, Voucher voucher);

    List<CustomerVoucher> findByCustomer_CustomerId(UUID customerId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT cv FROM CustomerVoucher cv WHERE cv.customer.customerId = :customerId AND cv.voucher.voucherId = :voucherId")
    Optional<CustomerVoucher> findByCustomerIdAndVoucherIdForUpdate(@Param("customerId") UUID customerId, 
                                                                    @Param("voucherId") Integer voucherId);
    
    List<CustomerVoucher> findByVoucher_VoucherId(Integer voucherId);
    
    @Query("SELECT cv FROM CustomerVoucher cv WHERE cv.customer.customerId = :customerId AND cv.voucher.status = 'ACTIVE'")
    List<CustomerVoucher> findActiveVouchersByCustomerId(@Param("customerId") UUID customerId);
    
    @Modifying
    @Query("UPDATE CustomerVoucher cv SET cv.timesUsed = cv.timesUsed + 1, cv.lastUsedAt = CURRENT_TIMESTAMP " +
           "WHERE cv.customer.customerId = :customerId AND cv.voucher.voucherId = :voucherId")
    int incrementUsage(@Param("customerId") UUID customerId, @Param("voucherId") Integer voucherId);
    
    @Query("SELECT COUNT(cv) FROM CustomerVoucher cv WHERE cv.voucher.voucherId = :voucherId")
    Long countByVoucherId(@Param("voucherId") Integer voucherId);
}


