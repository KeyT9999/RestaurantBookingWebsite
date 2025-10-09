package com.example.booking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.RestaurantBankAccount;

@Repository
public interface RestaurantBankAccountRepository extends JpaRepository<RestaurantBankAccount, Integer> {
    
    /**
     * Tìm tài khoản ngân hàng theo restaurant ID
     */
    @Query("SELECT rba FROM RestaurantBankAccount rba WHERE rba.restaurant.restaurantId = :restaurantId")
    List<RestaurantBankAccount> findByRestaurantId(@Param("restaurantId") Integer restaurantId);
    
    /**
     * Tìm tài khoản mặc định của restaurant
     */
    @Query("SELECT rba FROM RestaurantBankAccount rba WHERE rba.restaurant.restaurantId = :restaurantId AND rba.isDefault = true")
    Optional<RestaurantBankAccount> findByRestaurantIdAndIsDefaultTrue(@Param("restaurantId") Integer restaurantId);
    
    /**
     * Kiểm tra xem số tài khoản đã tồn tại cho restaurant này chưa
     */
    @Query("SELECT COUNT(rba) > 0 FROM RestaurantBankAccount rba WHERE rba.restaurant.restaurantId = :restaurantId AND rba.accountNumber = :accountNumber")
    boolean existsByRestaurantIdAndAccountNumber(@Param("restaurantId") Integer restaurantId, @Param("accountNumber") String accountNumber);
    
    /**
     * Kiểm tra xem số tài khoản đã tồn tại cho restaurant này chưa (trừ account hiện tại)
     */
    @Query("SELECT COUNT(rba) > 0 FROM RestaurantBankAccount rba WHERE rba.restaurant.restaurantId = :restaurantId AND rba.accountNumber = :accountNumber AND rba.accountId != :accountId")
    boolean existsByRestaurantIdAndAccountNumberAndAccountIdNot(
        @Param("restaurantId") Integer restaurantId, 
        @Param("accountNumber") String accountNumber, 
        @Param("accountId") Integer accountId
    );
    
    /**
     * Đếm số tài khoản của restaurant
     */
    @Query("SELECT COUNT(rba) FROM RestaurantBankAccount rba WHERE rba.restaurant.restaurantId = :restaurantId")
    long countByRestaurantId(@Param("restaurantId") Integer restaurantId);
    
    /**
     * Bỏ tất cả tài khoản mặc định của restaurant
     */
    @Modifying
    @Query("UPDATE RestaurantBankAccount rba SET rba.isDefault = false WHERE rba.restaurant.restaurantId = :restaurantId")
    void unsetDefaultForRestaurant(@Param("restaurantId") Integer restaurantId);
    
    /**
     * Tìm tài khoản theo ID và restaurant ID (để kiểm tra ownership)
     */
    @Query("SELECT rba FROM RestaurantBankAccount rba WHERE rba.accountId = :accountId AND rba.restaurant.restaurantId = :restaurantId")
    Optional<RestaurantBankAccount> findByAccountIdAndRestaurantId(@Param("accountId") Integer accountId, @Param("restaurantId") Integer restaurantId);
}