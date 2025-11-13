package com.example.booking.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.domain.Customer;
import com.example.booking.domain.Review;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.dto.ReviewDto;
import com.example.booking.dto.ReviewForm;
import com.example.booking.dto.ReviewStatisticsDto;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.ReviewRepository;

@Service
@Transactional
public class ReviewService {
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private RestaurantProfileRepository restaurantProfileRepository;
    
    @Autowired
    private ReviewNotificationService reviewNotificationService;
    
    /**
     * Tạo hoặc cập nhật review
     * Đảm bảo mỗi customer chỉ có thể review một restaurant một lần
     */
    public Review createOrUpdateReview(ReviewForm form, UUID customerId) {
        validateReviewData(form, customerId);
        
        // Validate customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        
        // Validate restaurant
        RestaurantProfile restaurant = restaurantProfileRepository.findById(form.getRestaurantId())
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        
        // Kiểm tra xem customer đã review restaurant này chưa
        Optional<Review> existingReview = reviewRepository.findByCustomerAndRestaurant(customer, restaurant);
        
        Review review;
        boolean isNewReview = false;
        if (existingReview.isPresent()) {
            // Cập nhật review hiện tại
            review = existingReview.get();
            review.setRating(form.getRating());
            review.setComment(form.getComment());
            System.out.println("✅ Updating existing review: " + review.getReviewId());
        } else {
            // Tạo review mới
            review = new Review(customer, restaurant, form.getRating(), form.getComment());
            isNewReview = true;
            System.out.println("✅ Creating new review");
        }
        
        Review savedReview = reviewRepository.save(review);
        System.out.println("✅ Review saved with ID: " + savedReview.getReviewId());
        
        // Send notification for new reviews
        if (isNewReview) {
            try {
                reviewNotificationService.notifyNewReviewToRestaurant(savedReview);
            } catch (Exception e) {
                System.err.println("❌ Failed to send review notification: " + e.getMessage());
            }
        }
        
        return savedReview;
    }
    
    /**
     * Xóa review của customer
     */
    public void deleteReview(Integer reviewId, UUID customerId) {
        System.out.println("🔍 ReviewService.deleteReview() called");
        System.out.println("   Review ID: " + reviewId);
        System.out.println("   Customer ID: " + customerId);
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        
        // Kiểm tra quyền sở hữu
        if (!review.getCustomer().getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("You can only delete your own reviews");
        }
        
        // Kiểm tra thời gian có thể xóa (30 ngày)
        if (!review.isEditable()) {
            throw new IllegalArgumentException("Review cannot be deleted after 30 days");
        }
        
        reviewRepository.delete(review);
        System.out.println("✅ Review deleted successfully");
    }
    
    /**
     * Lấy review theo ID
     */
    @Transactional(readOnly = true)
    public Optional<Review> getReviewById(Integer reviewId) {
        return reviewRepository.findById(reviewId);
    }
    
    /**
     * Lấy tất cả review của restaurant
     */
    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsByRestaurant(Integer restaurantId) {
        RestaurantProfile restaurant = restaurantProfileRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        
        List<Review> reviews = reviewRepository.findByRestaurantOrderByCreatedAtDesc(restaurant);
        
        return reviews.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy review của restaurant với phân trang
     */
    @Transactional(readOnly = true)
    public Page<ReviewDto> getReviewsByRestaurant(Integer restaurantId, Pageable pageable) {
        RestaurantProfile restaurant = restaurantProfileRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        
        Page<Review> reviews = reviewRepository.findByRestaurantOrderByCreatedAtDesc(restaurant, pageable);
        
        return reviews.map(this::convertToDto);
    }
    
    /**
     * Lấy review của restaurant theo rating
     */
    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsByRestaurantAndRating(Integer restaurantId, Integer rating) {
        RestaurantProfile restaurant = restaurantProfileRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        
        List<Review> reviews = reviewRepository.findByRestaurantAndRatingOrderByCreatedAtDesc(restaurant, rating);
        
        return reviews.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy review của customer
     */
    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsByCustomer(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        
        List<Review> reviews = reviewRepository.findByCustomerOrderByCreatedAtDesc(customer);
        
        return reviews.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Kiểm tra customer đã review restaurant chưa
     */
    @Transactional(readOnly = true)
    public boolean hasCustomerReviewedRestaurant(UUID customerId, Integer restaurantId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        
        RestaurantProfile restaurant = restaurantProfileRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        
        return reviewRepository.existsByCustomerAndRestaurant(customer, restaurant);
    }
    
    /**
     * Lấy review của customer cho restaurant cụ thể
     */
    @Transactional(readOnly = true)
    public Optional<ReviewDto> getCustomerReviewForRestaurant(UUID customerId, Integer restaurantId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        
        RestaurantProfile restaurant = restaurantProfileRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        
        Optional<Review> review = reviewRepository.findByCustomerAndRestaurant(customer, restaurant);
        
        return review.map(this::convertToDto);
    }
    
    /**
     * Tính rating trung bình của restaurant
     */
    @Transactional(readOnly = true)
    public double getAverageRatingByRestaurant(Integer restaurantId) {
        RestaurantProfile restaurant = restaurantProfileRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        
        Double avgRating = reviewRepository.getAverageRatingByRestaurant(restaurant);
        return avgRating != null ? avgRating : 0.0;
    }
    
    /**
     * Đếm số review của restaurant
     */
    @Transactional(readOnly = true)
    public long getReviewCountByRestaurant(Integer restaurantId) {
        RestaurantProfile restaurant = restaurantProfileRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        
        return reviewRepository.countByRestaurant(restaurant);
    }
    
    /**
     * Lấy thống kê review của restaurant
     */
    @Transactional(readOnly = true)
    public ReviewStatisticsDto getRestaurantReviewStatistics(Integer restaurantId) {
        RestaurantProfile restaurant = restaurantProfileRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        
        // Tính rating trung bình
        double averageRating = getAverageRatingByRestaurant(restaurantId);
        
        // Đếm tổng số review
        long totalReviews = getReviewCountByRestaurant(restaurantId);
        
        // Lấy distribution của rating
        List<Object[]> distributionData = reviewRepository.getRatingDistributionByRestaurant(restaurant);
        Map<Integer, Integer> ratingDistribution = new HashMap<>();
        
        for (Object[] data : distributionData) {
            Integer rating = (Integer) data[0];
            Long count = (Long) data[1];
            ratingDistribution.put(rating, count.intValue());
        }
        
        return new ReviewStatisticsDto(averageRating, (int) totalReviews, ratingDistribution);
    }
    
    /**
     * Lấy review mới nhất của restaurant
     */
    @Transactional(readOnly = true)
    public List<ReviewDto> getRecentReviewsByRestaurant(Integer restaurantId, int limit) {
        RestaurantProfile restaurant = restaurantProfileRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        
        Pageable pageable = PageRequest.of(0, limit);
        List<Review> reviews = reviewRepository.findRecentReviewsByRestaurant(restaurant, pageable);
        
        return reviews.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert Review entity to ReviewDto
     */
    public ReviewDto convertToDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setReviewId(review.getReviewId());
        dto.setRestaurantId(review.getRestaurant().getRestaurantId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCustomerName(review.getCustomerName());
        dto.setCustomerAvatar(review.getCustomer().getUser().getProfileImageUrl());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setRestaurantName(review.getRestaurantName());
        dto.setEditable(review.isEditable());
        
        return dto;
    }
    
    /**
     * Validate review data
     */
    private void validateReviewData(ReviewForm form, UUID customerId) {
        if (form == null) {
            throw new IllegalArgumentException("Review form cannot be null");
        }
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        if (form.getRestaurantId() == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        if (form.getRating() == null || form.getRating() < 1 || form.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }
}
