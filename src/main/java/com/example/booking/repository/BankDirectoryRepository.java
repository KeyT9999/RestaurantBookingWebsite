package com.example.booking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.BankDirectory;

@Repository
public interface BankDirectoryRepository extends JpaRepository<BankDirectory, Integer> {
    
    /**
     * Tìm bank theo BIN code
     */
    Optional<BankDirectory> findByBin(String bin);
    
    /**
     * Tìm bank theo code (VCB, TCB, etc)
     */
    Optional<BankDirectory> findByCode(String code);
    
    /**
     * Lấy tất cả banks active
     */
    List<BankDirectory> findByIsActiveTrueOrderByShortNameAsc();
    
    /**
     * Kiểm tra BIN có tồn tại không
     */
    boolean existsByBin(String bin);
}

