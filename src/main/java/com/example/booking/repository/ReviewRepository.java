package com.example.booking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.Review;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.Customer;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    
    /**
     * Tìm review theo customer và restaurant (đảm bảo unique constraint)
     */
    Optional<Review> findByCustomerAndRestaurant(Customer customer, RestaurantProfile restaurant);
    
    /**
     * Kiểm tra customer đã review restaurant chưa
     */
    boolean existsByCustomerAndRestaurant(Customer customer, RestaurantProfile restaurant);
    
    /**
     * Lấy tất cả review của một restaurant
     */
    List<Review> findByRestaurantOrderByCreatedAtDesc(RestaurantProfile restaurant);
    
    /**
     * Lấy review của restaurant với phân trang
     */
    Page<Review> findByRestaurantOrderByCreatedAtDesc(RestaurantProfile restaurant, Pageable pageable);
    
    /**
     * Lấy review của restaurant theo rating
     */
    List<Review> findByRestaurantAndRatingOrderByCreatedAtDesc(RestaurantProfile restaurant, Integer rating);
    
    /**
     * Lấy review của customer
     */
    List<Review> findByCustomerOrderByCreatedAtDesc(Customer customer);
    
    /**
     * Đếm số review của restaurant
     */
    long countByRestaurant(RestaurantProfile restaurant);
    
    /**
     * Đếm số review theo rating của restaurant
     */
    long countByRestaurantAndRating(RestaurantProfile restaurant, Integer rating);
    
    /**
     * Tính rating trung bình của restaurant
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.restaurant = :restaurant")
    Double getAverageRatingByRestaurant(@Param("restaurant") RestaurantProfile restaurant);
    
    /**
     * Lấy distribution của rating (rating -> count)
     */
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.restaurant = :restaurant GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> getRatingDistributionByRestaurant(@Param("restaurant") RestaurantProfile restaurant);
    
    /**
     * Lấy review mới nhất của restaurant
     */
    @Query("SELECT r FROM Review r WHERE r.restaurant = :restaurant ORDER BY r.createdAt DESC")
    List<Review> findRecentReviewsByRestaurant(@Param("restaurant") RestaurantProfile restaurant, Pageable pageable);
    
    /**
     * Lấy review của customer cho restaurant cụ thể
     */
    @Query("SELECT r FROM Review r WHERE r.customer = :customer AND r.restaurant = :restaurant")
    Optional<Review> findByCustomerAndRestaurantId(@Param("customer") Customer customer, @Param("restaurant") RestaurantProfile restaurant);
    
    /**
     * Xóa review của customer cho restaurant cụ thể
     */
    void deleteByCustomerAndRestaurant(Customer customer, RestaurantProfile restaurant);
}
