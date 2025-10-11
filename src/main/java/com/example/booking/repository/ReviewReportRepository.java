package com.example.booking.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.Review;
import com.example.booking.domain.ReviewReport;
import com.example.booking.domain.ReviewReportStatus;

@Repository
public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {

    Page<ReviewReport> findByRestaurantOrderByCreatedAtDesc(RestaurantProfile restaurant, Pageable pageable);

    Page<ReviewReport> findByOwnerOrderByCreatedAtDesc(RestaurantOwner owner, Pageable pageable);

    Page<ReviewReport> findByStatusOrderByCreatedAtDesc(ReviewReportStatus status, Pageable pageable);

    Optional<ReviewReport> findByReview(Review review);

    Optional<ReviewReport> findTopByReviewReviewIdOrderByCreatedAtDesc(Integer reviewId);

    List<ReviewReport> findByReviewReviewId(Integer reviewId);

    void deleteByReviewReviewId(Integer reviewId);

    long countByRestaurantAndStatus(RestaurantProfile restaurant, ReviewReportStatus status);

    long countByStatus(ReviewReportStatus status);

    List<ReviewReport> findByResolvedByAdminId(UUID adminId);
}

