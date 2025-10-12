package com.example.booking.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.ContractStatus;
import com.example.booking.domain.RestaurantContract;

/**
 * Repository cho RestaurantContract entity
 */
@Repository
public interface RestaurantContractRepository extends JpaRepository<RestaurantContract, Integer> {
    
    /**
     * Tìm hợp đồng theo restaurant ID
     */
    List<RestaurantContract> findByRestaurantIdOrderByCreatedAtDesc(Integer restaurantId);
    
    /**
     * Tìm hợp đồng theo owner ID
     */
    List<RestaurantContract> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId);
    
    /**
     * Tìm hợp đồng theo trạng thái
     */
    List<RestaurantContract> findByStatusOrderByCreatedAtDesc(ContractStatus status);
    
    /**
     * Tìm hợp đồng đang hoạt động theo restaurant ID
     */
    @Query("SELECT c FROM RestaurantContract c WHERE c.restaurantId = :restaurantId AND c.status = 'ACTIVE'")
    Optional<RestaurantContract> findActiveContractByRestaurantId(@Param("restaurantId") Integer restaurantId);
    
    /**
     * Tìm hợp đồng đang hoạt động theo owner ID
     */
    @Query("SELECT c FROM RestaurantContract c WHERE c.ownerId = :ownerId AND c.status = 'ACTIVE'")
    Optional<RestaurantContract> findActiveContractByOwnerId(@Param("ownerId") UUID ownerId);
    
    /**
     * Tìm hợp đồng chờ chữ ký của owner
     */
    @Query("SELECT c FROM RestaurantContract c WHERE c.ownerId = :ownerId AND c.signedByOwner = false AND c.status IN ('DRAFT', 'PENDING_OWNER_SIGNATURE')")
    List<RestaurantContract> findPendingOwnerSignatureContracts(@Param("ownerId") UUID ownerId);
    
    /**
     * Tìm hợp đồng chờ chữ ký của admin
     */
    @Query("SELECT c FROM RestaurantContract c WHERE c.signedByOwner = true AND c.signedByAdmin = false AND c.status = 'PENDING_ADMIN_SIGNATURE'")
    List<RestaurantContract> findPendingAdminSignatureContracts();
    
    /**
     * Tìm hợp đồng sắp hết hạn (trong vòng 30 ngày)
     */
    @Query("SELECT c FROM RestaurantContract c WHERE c.contractEndDate BETWEEN :now AND :expiryDate AND c.status = 'ACTIVE'")
    List<RestaurantContract> findContractsExpiringSoon(@Param("now") LocalDateTime now, @Param("expiryDate") LocalDateTime expiryDate);
    
    /**
     * Tìm hợp đồng đã hết hạn
     */
    @Query("SELECT c FROM RestaurantContract c WHERE c.contractEndDate < :now AND c.status = 'ACTIVE'")
    List<RestaurantContract> findExpiredContracts(@Param("now") LocalDateTime now);
    
    /**
     * Đếm hợp đồng theo trạng thái
     */
    long countByStatus(ContractStatus status);
    
    /**
     * Đếm hợp đồng đang hoạt động
     */
    @Query("SELECT COUNT(c) FROM RestaurantContract c WHERE c.status = 'ACTIVE'")
    long countActiveContracts();
    
    /**
     * Đếm hợp đồng chờ ký
     */
    @Query("SELECT COUNT(c) FROM RestaurantContract c WHERE c.status IN ('DRAFT', 'PENDING_OWNER_SIGNATURE', 'PENDING_ADMIN_SIGNATURE')")
    long countPendingContracts();
    
    /**
     * Tìm hợp đồng mới nhất theo restaurant ID
     */
    @Query("SELECT c FROM RestaurantContract c WHERE c.restaurantId = :restaurantId ORDER BY c.createdAt DESC")
    Optional<RestaurantContract> findLatestContractByRestaurantId(@Param("restaurantId") Integer restaurantId);
    
    /**
     * Tìm hợp đồng theo khoảng thời gian tạo
     */
    @Query("SELECT c FROM RestaurantContract c WHERE c.createdAt BETWEEN :startDate AND :endDate ORDER BY c.createdAt DESC")
    List<RestaurantContract> findContractsByCreatedDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Tìm hợp đồng theo khoảng thời gian bắt đầu hiệu lực
     */
    @Query("SELECT c FROM RestaurantContract c WHERE c.contractStartDate BETWEEN :startDate AND :endDate ORDER BY c.contractStartDate DESC")
    List<RestaurantContract> findContractsByStartDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
